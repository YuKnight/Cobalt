package com.github.auties00.cobalt.message.send;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.device.DeviceService;
import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.message.send.ack.AckParser;
import com.github.auties00.cobalt.message.send.ack.AckResult;
import com.github.auties00.cobalt.message.send.ack.NackReason;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryptedPayload;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryption;
import com.github.auties00.cobalt.message.send.senderkey.SenderKeyDistribution;
import com.github.auties00.cobalt.message.send.stanza.*;
import com.github.auties00.cobalt.message.send.token.ContentBindingToken;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.ChatMessageContextInfoBuilder;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.chat.ChatMetadata;
import com.github.auties00.cobalt.model.chat.group.GroupMetadata;
import com.github.auties00.cobalt.model.chat.group.GroupParticipant;
import com.github.auties00.cobalt.model.chat.group.GroupParticipantBuilder;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.MessageContainerSpec;
import com.github.auties00.cobalt.model.message.event.EncEventResponseMessage;
import com.github.auties00.cobalt.model.message.poll.PollUpdateMessage;
import com.github.auties00.cobalt.model.message.security.EncCommentMessage;
import com.github.auties00.cobalt.model.message.security.EncReactionMessage;
import com.github.auties00.cobalt.model.message.system.ProtocolMessage;
import com.github.auties00.cobalt.model.message.text.ExtendedTextMessage;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.wam.event.AddressingModeMismatchEventBuilder;
import com.github.auties00.cobalt.wam.event.MdDeviceSyncAckEventBuilder;
import com.github.auties00.cobalt.wam.event.MdGroupParticipantMissAckEventBuilder;
import com.github.auties00.cobalt.wam.event.PrekeysDepletionEventBuilder;
import com.github.auties00.cobalt.wam.type.AddressingMode;
import com.github.auties00.cobalt.wam.type.ClientGroupSizeBucket;
import com.github.auties00.cobalt.wam.type.MessageType;
import com.github.auties00.cobalt.wam.type.MismatchOriginType;
import com.github.auties00.cobalt.wam.type.PrekeysFetchContext;
import com.github.auties00.cobalt.wam.type.TypeOfGroupEnum;
import com.github.auties00.cobalt.wam.type.WamSizeBuckets;

import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sends messages to group chats ({@code group@g.us}).
 *
 * <p>The primary path uses sender-key (SKMSG) encryption: the message is
 * encrypted once with the group sender key, and a separate sender-key
 * distribution message is sent individually to new members who don't
 * yet have the key.
 *
 * <p>When the server returns a phash mismatch, the delta devices receive
 * the message as a group-direct fanout (per-device encryption).
 *
 * @apiNote WAWebSendGroupMsgJob.encryptAndSendGroupMsg: queues the send
 * per group, resolves group data and participant lists, dispatches to
 * SKMSG or DIRECT path.
 * WAWebSendGroupSkmsgJob.encryptAndSendSenderKeyMsg: SKMSG path with
 * phash verification, SK distribution, and addressing mode handling.
 * WAWebSendGroupDirectJob.encryptAndSendGroupDirectMsg: DIRECT path
 * used for resends after phash mismatch.
 */
@WhatsAppWebModule(moduleName = "WAWebSendGroupMsgJob")
@WhatsAppWebModule(moduleName = "WAWebSendGroupSkmsgJob")
@WhatsAppWebModule(moduleName = "WAWebSendGroupDirectJob")
@WhatsAppWebModule(moduleName = "WAWebSendGroupKeyDistributionMsgJob")
final class GroupMessageSender extends MessageSender<ChatMessageInfo> {
    /**
     * Logger for group send diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(GroupMessageSender.class.getName());


    /**
     * The encryption service for sender-key and per-device encryption.
     *
     * @implNote ADAPTED: WAWebEncryptMsgProtobuf is module-level;
     * Cobalt uses constructor-based DI.
     */
    private final MessageEncryption encryption;

    /**
     * The device service for fanout resolution and session management.
     *
     * @implNote ADAPTED: WAWebDBDeviceListFanout, WAWebManageE2ESessionsJob
     * are module-level; Cobalt uses constructor-based DI.
     */
    private final DeviceService deviceService;

    /**
     * The AB props service for feature flag and configuration lookups.
     *
     * @implNote ADAPTED: WAWebABProps is module-level; Cobalt uses
     * constructor-based DI.
     */
    private final ABPropsService abPropsService;

    /**
     * The sender-key distribution service for SK distribution encryption.
     *
     * @implNote ADAPTED: WAWebGetGroupKeyDistributionMsg is module-level;
     * Cobalt uses constructor-based DI.
     */
    private final SenderKeyDistribution senderKeyDistribution;

    /**
     * The bot stanza builder.
     *
     * @implNote ADAPTED: bot node building is inline in WA Web;
     * Cobalt delegates to {@link BotStanza}.
     */
    private final BotStanza botStanza;

    /**
     * The business stanza builder for payment native flow messages.
     *
     * @implNote ADAPTED: WA Web builds biz nodes inline; Cobalt
     * delegates to {@link BizStanza}.
     */
    private final BizStanza bizStanza;

    /**
     * The meta stanza builder for the {@code <meta>} child node.
     *
     * @implNote ADAPTED: WAWebSendMsgMetaNode is module-level; Cobalt
     * delegates to {@link MetaStanza}.
     */
    private final MetaStanza metaStanza;

    /**
     * The reporting stanza builder for reporting tokens.
     *
     * @implNote ADAPTED: WAWebReportingTokenUtils is module-level;
     * Cobalt delegates to {@link ReportingStanza}.
     */
    private final ReportingStanza reportingStanza;

    /**
     * Per-group locks used to serialise sender-key encryption per group.
     *
     * <p>The Signal sender-key counter must increase monotonically per
     * group per sender, so concurrent sends to the same group are
     * serialised on this lock map.
     *
     * @implNote WAWebSendMsgQueueMap.sendMsgQueueMap: WA Web keeps a
     * per-group task queue keyed by group JID string; Cobalt replaces
     * the queue with a lock map.
     */
    private final ConcurrentMap<String, ReentrantLock> locks;

    /**
     * Creates a new group message sender.
     *
     * @param client                the WhatsApp client
     * @param encryption            the encryption service
     * @param deviceService         the device service
     * @param abPropsService        the AB props service
     * @param senderKeyDistribution the SK distribution service
     * @param botStanza             the bot stanza builder
     * @param bizStanza             the business stanza builder
     * @param metaStanza            the meta stanza builder
     * @param reportingStanza       the reporting stanza builder
     *
     * @implNote ADAPTED: WAWebSendGroupMsgJob uses module-level imports;
     * Cobalt uses constructor-based DI.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendGroupMsgJob", exports = "encryptAndSendGroupMsg",
            adaptation = WhatsAppAdaptation.ADAPTED)
    GroupMessageSender(
            WhatsAppClient client,
            MessageEncryption encryption,
            DeviceService deviceService,
            ABPropsService abPropsService,
            SenderKeyDistribution senderKeyDistribution,
            BotStanza botStanza,
            BizStanza bizStanza,
            MetaStanza metaStanza,
            ReportingStanza reportingStanza
    ) {
        super(client);
        this.encryption = Objects.requireNonNull(encryption, "encryption");
        this.deviceService = Objects.requireNonNull(deviceService, "deviceService");
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService");
        this.senderKeyDistribution = Objects.requireNonNull(senderKeyDistribution, "senderKeyDistribution");
        this.botStanza = Objects.requireNonNull(botStanza, "botStanza");
        this.bizStanza = Objects.requireNonNull(bizStanza, "bizStanza");
        this.metaStanza = Objects.requireNonNull(metaStanza, "metaStanza");
        this.reportingStanza = Objects.requireNonNull(reportingStanza, "reportingStanza");
        this.locks = new ConcurrentHashMap<>();
    }

    /**
     * Executes the given task while holding the lock for {@code key},
     * ensuring mutual exclusion with other tasks enqueued under the same key.
     *
     * <p>Tasks enqueued under different keys may run concurrently.
     *
     * @param <T>  the result type
     * @param key  the queue key (typically the group JID string)
     * @param task the task to execute
     * @return the result produced by {@code task}
     * @throws NullPointerException if any argument is {@code null}
     * @throws Exception if {@code task} throws
     *
     * @apiNote WAWebSendMsgQueueMap.sendMsgQueueMap.enqueue: serialises
     * the send task per group JID string key.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgQueueMap", exports = "sendMsgQueueMap",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private  <T> T enqueue(String key, Callable<T> task) throws Exception {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(task, "task");
        var lock = locks.computeIfAbsent(key, _ -> new ReentrantLock());
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }

    @WhatsAppWebExport(moduleName = "WAWebSendGroupMsgJob", exports = "encryptAndSendGroupMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSendGroupSkmsgJob", exports = "encryptAndSendSenderKeyMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    @Override
    AckResult send(Jid groupJid, ChatMessageInfo messageInfo) {
        waitForOfflineDelivery();
        try {
            // WAWebSendGroupMsgJob.encryptAndSendGroupMsg: enqueue per group to
            // serialise sender-key encryption (monotonic counter requirement)
            return enqueue(groupJid.toString(), () -> {
                var rawContainer = messageInfo.message();

                // WAWebSendGroupSkmsgJob: resolve group metadata for addressing mode
                var chatMetadata = store.findChatMetadata(groupJid).orElse(null);
                var isCag = chatMetadata instanceof GroupMetadata gm
                        && gm.isDefaultSubgroup();
                var isCagAddon = isCag && isCagAddonMessage(rawContainer);
                var isLidAddressingMode = (chatMetadata != null && chatMetadata.isLidAddressingMode())
                        || isCagAddon;
                var addressingMode = isLidAddressingMode ? "lid" : "pn";

                // WAWebSendGroupSkmsgJob: determine sender JID based on addressing mode
                // isCagAddon || isLidAddressingMode → getMeDeviceLid, else getMeDevicePn
                var senderJid = isLidAddressingMode ? selfLidOrPn() : requireSelfJid();

                // WAWebSendGroupSkmsgJob: get group fanout (all devices + phash)
                // WAWebSendGroupSkmsgJob.encryptAndSendSenderKeyMsg: phash includes
                // sender's own device JID via [].concat(M, [F])
                var fanout = deviceService.getGroupFanout(groupJid, senderJid);
                var allDevices = fanout.devices();
                var phash = fanout.phash();

                // WAWebApiParticipantStore.getGroupSenderKeyListFromParticipantRecord:
                // split devices into those needing SK distribution and those with keys
                var skDistribDevices = new ArrayList<Jid>();
                var skExistingDevices = new ArrayList<Jid>();
                for (var device : allDevices) {
                    if (store.hasSenderKeyDistributed(groupJid, device)) {
                        skExistingDevices.add(device);
                    } else {
                        skDistribDevices.add(device);
                    }
                }

                // WAWebSendGroupMsgJob.filterIncorrectlyAddressedDevices:
                // filter devices by addressing mode - LID groups keep only
                // LID-addressed devices, non-LID groups keep only
                // PN-addressed devices
                if (isLidAddressingMode) {
                    skDistribDevices.removeIf(d -> !d.hasLidServer());
                    skExistingDevices.removeIf(d -> !d.hasLidServer());
                } else {
                    // WAWebSendGroupMsgJob.filterIncorrectlyAddressedDevices:
                    // non-LID groups (including non-CAG) filter out LID devices
                    skDistribDevices.removeIf(Jid::hasLidServer);
                    skExistingDevices.removeIf(Jid::hasLidServer);
                }

                // WAWebSendGroupMsgJob.encryptAndSendGroupMsg: apply CAPI flag
                // WAWebE2EProtoGenerator.updateGroupMsgProtoWithCapiFlag:
                // sets capiCreatedGroup=true on messageContextInfo when group
                // has CAPI capabilities
                var isCapiGroup = chatMetadata instanceof GroupMetadata gm2
                        && gm2.hasCapi();
                var container = isCapiGroup
                        ? applyCapiFlag(rawContainer)
                        : rawContainer;

                // WAWebSendGroupSkmsgJob: rotate sender key if needed
                // (triggered when participants are removed from the group)
                var rotateKey = store.clearKeyRotation(groupJid);
                if (rotateKey) {
                    // WAWebSignal.Session.deleteGroupSenderKeyInfo
                    encryption.rotateSenderKey(groupJid, senderJid);
                    // After rotation, all devices need redistribution
                    skDistribDevices.addAll(skExistingDevices);
                    skExistingDevices.clear();
                }

                // WAWebMsgRcatUtils.genContentBindingForMsg: generate RCAT content bindings
                var participantUserJids = Stream.concat(skDistribDevices.stream(), skExistingDevices.stream())
                        .map(Jid::toUserJid)
                        .distinct()
                        .toList();
                var contentBindings = generateContentBindings(messageInfo, participantUserJids);

                // WAWebSendGroupSkmsgJob: create receipt records for
                // all filtered SK devices (skList + skDistribList)
                var allSkDevices = Stream.concat(skDistribDevices.stream(), skExistingDevices.stream())
                        .toList();
                store.createOrMergeReceiptRecords(messageInfo.key().id().orElseThrow(), allSkDevices);

                // WAWebSendGroupSkmsgJob: get sender key bytes and encrypt SK distribution
                // WAWebGetGroupKeyDistributionMsg: populates ICDC per device
                var senderKeyBytes = encryption.getSenderKeyBytes(groupJid, senderJid);
                List<MessageEncryptedPayload> skDistPayloads;
                if (skDistribDevices.isEmpty()) {
                    skDistPayloads = List.of();
                } else {
                    // WAWebSendGroupSkmsgJob: yield g(l, P, i) — ensureE2ESessions for the
                    // skDistribList and emit PrekeysDepletionEvent before encrypting.
                    var depletedPrekeyCount = deviceService.ensureSessions(skDistribDevices);
                    // WAWebSendGroupSkmsgJob -> WAWebPostPrekeysDepletionMetric.maybePostPrekeysDepletionMetric:
                    // {count, prekeysFetchReason: SEND_MESSAGE, messageType: GROUP,
                    // deviceSizeBucket: r.deviceSizeBucket}. The bucket in WA Web is the
                    // group's cached total-device bucket; Cobalt uses the total fanout set
                    // (skDistribDevices + skExistingDevices) as the closest equivalent.
                    emitPrekeysDepletionEvents(depletedPrekeyCount, MessageType.GROUP, allSkDevices.size());
                    skDistPayloads = senderKeyDistribution.encrypt(groupJid, senderKeyBytes, skDistribDevices);
                }

                // WAWebSendGroupSkmsgJob: bot feedback messages skip SKMSG
                // encryption and phash (delivered only via <bot> node)
                var isBotFeedback = container.content() instanceof ProtocolMessage pm
                        && pm.type().orElse(null) == ProtocolMessage.Type.BOT_FEEDBACK_MESSAGE;

                // WAWebEncryptMsgProtobuf.encryptMsgSenderKey: encrypt with sender key
                // WAWebSendGroupSkmsgJob: E = g ? null : enc(...), skip for bot feedback
                byte[] skmsgCiphertext;
                if (isBotFeedback) {
                    skmsgCiphertext = null;
                } else {
                    var plaintext = MessageContainerSpec.encode(container);
                    try {
                        skmsgCiphertext = encryption.encryptForGroup(groupJid, senderJid, plaintext)
                                .ciphertext();
                        // WAWebEncryptMsgProtobuf.encryptMsgSenderKey: emit E2eMessageSend (id 476)
                        // with e2eSuccessful=true from the finally-committed event.
                        emitE2eMessageSendSenderKeyEvent(
                                groupJid, container,
                                com.github.auties00.cobalt.wam.type.E2eDestination.GROUP,
                                isLidAddressingMode, true);
                    } catch (RuntimeException skmsgError) {
                        // WAWebEncryptMsgProtobuf.encryptMsgSenderKey: on failure flips
                        // e2eSuccessful=false and sets weight=1 before the finally commit.
                        emitE2eMessageSendSenderKeyEvent(
                                groupJid, container,
                                com.github.auties00.cobalt.wam.type.E2eDestination.GROUP,
                                isLidAddressingMode, false);
                        throw skmsgError;
                    }
                }

                // WAWebSendGroupSkmsgJob: build participants node
                // SK distribution → <to> with <enc> + optional <content_binding>
                // No distribution but bindings → <to> with just <content_binding>
                // WAWebSendGroupSkmsgJob: for bot feedback, skip SK distribution
                // participants but keep content binding participants if applicable
                var decryptFail = resolveDecryptFail(container);
                Node participantsNode;
                if (!isBotFeedback && !skDistPayloads.isEmpty()) {
                    participantsNode = ParticipantsStanza.buildSenderKeyDistribution(
                            skDistPayloads, contentBindings, decryptFail);
                } else if (contentBindings != null) {
                    participantsNode = ParticipantsStanza.buildContentBindingOnly(
                            skExistingDevices, contentBindings);
                } else {
                    participantsNode = null;
                }

                // WAWebSendGroupSkmsgJob: build open group bot node when applicable
                // WAWebBotGroupGatingUtils.isOpenGroupBotSendEnabled: AB prop gate
                var isOpenBotGroup = chatMetadata != null && chatMetadata.isOpenBotGroup()
                        && abPropsService.getBool(ABProp.WEB_AI_GROUP_OPEN_SUPPORT)
                        && abPropsService.getBool(ABProp.AI_GROUP_PARTICIPATION_ENABLED);
                Node openBotNode = null;
                if (isOpenBotGroup) {
                    // WAWebSendGroupSkmsgJob function L: ensure sessions with
                    // the bot device before encrypting
                    deviceService.ensureSessions(List.of(Jid.metaAiBotAccount()));
                    store.createOrMergeReceiptRecords(
                            messageInfo.key().id().orElseThrow(), List.of(Jid.metaAiBotAccount()));
                    openBotNode = botStanza.buildForGroup(messageInfo, true);
                }

                // WAWebSendGroupSkmsgJob: identity node when any pkmsg
                // Also needed when open group bot encryption produced pkmsg
                var needsIdentity = ParticipantsStanza.requiresIdentityNode(skDistPayloads);
                if (!needsIdentity && openBotNode != null) {
                    needsIdentity = openBotNode.streamChild("to")
                            .flatMap(to -> to.streamChild("enc"))
                            .anyMatch(enc -> "pkmsg".equals(enc.getAttributeAsString("type", null)));
                }
                var identityNode = needsIdentity ? buildIdentityNode() : null;

                // WAWebSendGroupSkmsgJob: build and send the stanza
                // Use the existing botStanza for 1:1 bot/feedback, or open group bot
                var mediaType = resolveMediaType(container);
                var botNode = openBotNode != null
                        ? openBotNode
                        : botStanza.build(messageInfo, groupJid);
                // WAWebSendGroupSkmsgJob: phash is dropped for bot feedback messages
                var stanzaPhash = isBotFeedback ? null : phash;
                var stanza = GroupSkmsgFanoutStanza.build(
                        messageInfo.key().id().orElseThrow(),
                        groupJid,
                        resolveStanzaType(container),
                        stanzaPhash,
                        skmsgCiphertext,
                        mediaType,
                        decryptFail,
                        resolveEditAttribute(container),
                        addressingMode,
                        participantsNode,
                        identityNode,
                        metaStanza.buildChat(groupJid, container, null),
                        bizStanza.buildGroup(container),
                        botNode,
                        reportingStanza.build(messageInfo, requireSelfJid(), groupJid),
                        SenderContentBindingStanza.build(senderJid, contentBindings)
                );

                // WAWebSendMsgCommonApi.updateIdentityRange: track identity keys
                // for all participant devices (skList + skDistribList)
                store.updateIdentityRange(allSkDevices);

                flushStore();
                var ackNode = client.sendNode(stanza);
                var ack = AckParser.parse(ackNode);

                // WAWebSendGroupSkmsgJob: handle ack errors
                if (!ack.isSuccess()) {
                    var errorCode = ack.error().orElse(-1);
                    switch (errorCode) {
                        case NackReason.STALE_GROUP_ADDRESSING_MODE -> {
                            // WAWebSendGroupSkmsgJob: error 421 → query group, migrate, mark FAILED
                            LOGGER.log(System.Logger.Level.WARNING,
                                    "encryptAndSendSenderKeyMsg: ack with error code 421 for {0}, refreshing metadata",
                                    groupJid);
                            // Flip addressing mode so next retry uses the correct one
                            migrateAddressingMode(groupJid, !isLidAddressingMode);
                            throw new WhatsAppMessageException.Send.Unknown(
                                    "Stale group addressing mode for " + groupJid, null);
                        }
                        default -> {
                            throw new WhatsAppMessageException.Send.Unknown(
                                    "Invalid ack from server for group " + groupJid
                                    + ", error: " + errorCode, null);
                        }
                    }
                }

                // WAWebApiParticipantStore.markHasSenderKey: mark SK as distributed
                for (var device : skDistribDevices) {
                    store.markSenderKeyDistributed(groupJid, device);
                }

                // WAWebSendGroupSkmsgJob: handle phash mismatch → resend as group direct
                // WAWebResendGroupMsg: uses the filtered SK device list (oldList = M)
                var serverPhash = ack.phash().orElse(null);
                if (serverPhash != null && !serverPhash.equals(phash)) {
                    LOGGER.log(System.Logger.Level.DEBUG,
                            "encryptAndSendSenderKeyMsg: phash mismatch for {0}, server: {1}",
                            messageInfo.key().id(), serverPhash);
                    // WAWebSendGroupSkmsgJob: resendPersistedGroupMsgWrapper({...,
                    //   serverAddressingMode: J.success.addressingMode})
                    // The server-reported addressing mode from the ack is forwarded
                    // so WAWebPostMdDeviceSyncAckMetric can populate it on the
                    // MdDeviceSyncAck event.
                    var serverAddressingMode = ack.addressingMode().orElse(null);
                    resendAsGroupDirect(groupJid, messageInfo, allSkDevices,
                            addressingMode, serverAddressingMode, chatMetadata, senderJid);
                }

                // WAWebSendGroupSkmsgJob: handle addressing mode mismatch
                // WAWebGroupHandleAddressingModeMismatch.handleAddressingModeMismatch
                ack.addressingMode().ifPresent(serverMode -> {
                    if (!serverMode.equals(addressingMode)) {
                        LOGGER.log(System.Logger.Level.INFO,
                                "Addressing mode mismatch for {0}: local={1}, server={2}, migrating",
                                groupJid, addressingMode, serverMode);
                        // WAWebWamAddressingModeMismatchReporter.logAddressingModeMismatch:
                        // emit AddressingModeMismatch (id 4750) before migrating so that
                        // mismatches observed on outgoing group SKMSG acks are reported
                        // with MISMATCH_ORIGIN_TYPE.ACK_OUTGOING_MESSAGE.
                        client.wamService().commit(new AddressingModeMismatchEventBuilder()
                                .localAddressingMode(wamAddressingMode(addressingMode))
                                .serverAddressingMode(wamAddressingMode(serverMode))
                                .mismatchOrigin(MismatchOriginType.ACK_OUTGOING_MESSAGE)
                                .build());
                        migrateAddressingMode(groupJid, "lid".equals(serverMode));
                    }
                });

                return ack;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send group message to " + groupJid, e);
        }
    }

    /**
     * Sends a standalone sender-key distribution to the group without
     * any message content.
     *
     * <p>This is used to pre-distribute sender keys to group participants
     * that do not yet possess them, independent of sending an actual
     * message.  The stanza carries only the per-device encrypted
     * {@code SenderKeyDistributionMessage} payloads with
     * {@code device_fanout="false"} and {@code type="text"}.
     *
     * <p>If all devices already possess the sender key (the distribution
     * list is empty), this method returns immediately without sending
     * anything.
     *
     * @param groupJid the group JID to distribute keys for
     * @param msgId    the message ID to use for the distribution stanza
     * @throws NullPointerException if any argument is {@code null}
     *
     * @implNote WAWebSendMsgJob.encryptAndSendKeyDistributionMsg: validates
     * {@code id} and {@code remote}, then delegates to
     * WAWebSendGroupKeyDistributionMsgJob.encryptAndSendGroupKeyDistributionMsg
     * for group JIDs.
     * WAWebSendGroupKeyDistributionMsgJob.encryptAndSendGroupKeyDistributionMsg:
     * gets participant table, resolves SK distribution list, rotates key
     * if needed, encrypts distribution messages, builds a minimal stanza
     * with {@code device_fanout="false"}, sends and marks SK as distributed.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendGroupKeyDistributionMsgJob",
            exports = "encryptAndSendGroupKeyDistributionMsg", adaptation = WhatsAppAdaptation.DIRECT)
    void sendKeyDistribution(Jid groupJid, String msgId) {
        Objects.requireNonNull(groupJid, "groupJid");
        Objects.requireNonNull(msgId, "msgId");

        // WAWebSendMsgJob.encryptAndSendKeyDistributionMsg: wait for offline delivery
        waitForOfflineDelivery();

        try {
            // WAWebSendGroupKeyDistributionMsgJob: enqueue per group
            enqueue(groupJid.toString(), () -> {
                // WAWebSendGroupKeyDistributionMsgJob: get group fanout
                // WAWebSendGroupKeyDistributionMsgJob: phash always uses
                // getMeDevicePnOrThrow_DO_NOT_USE() (PN device JID)
                var fanout = deviceService.getGroupFanout(groupJid, requireSelfJid());
                var allDevices = fanout.devices();

                // WAWebSendGroupKeyDistributionMsgJob: split into SK distribution and existing
                // WAWebApiParticipantStore.getGroupSenderKeyListFromParticipantRecord
                var skDistribDevices = new ArrayList<Jid>();
                var skExistingDevices = new ArrayList<Jid>();
                for (var device : allDevices) {
                    if (store.hasSenderKeyDistributed(groupJid, device)) {
                        skExistingDevices.add(device);
                    } else {
                        skDistribDevices.add(device);
                    }
                }

                // WAWebSendGroupKeyDistributionMsgJob: if skDistribList is empty, skip
                if (skDistribDevices.isEmpty()) {
                    LOGGER.log(System.Logger.Level.DEBUG,
                            "encryptAndSendGroupKeyDistributionMsg: skip sending {0}: " +
                                    "sender key distribution list is empty", groupJid);
                    return null;
                }

                // WAWebSendGroupKeyDistributionMsgJob: create receipt records
                var allSkDevices = Stream.concat(skDistribDevices.stream(), skExistingDevices.stream())
                        .toList();
                store.createOrMergeReceiptRecords(msgId, allSkDevices);

                // WAWebSendGroupKeyDistributionMsgJob: determine sender JID
                // from addressing mode (all LID → getMeDeviceLid, else getMeDevicePn)
                var allLid = skDistribDevices.stream().allMatch(Jid::hasLidServer);
                var senderJid = allLid ? selfLidOrPn() : requireSelfJid();

                // WAWebSendGroupKeyDistributionMsgJob: rotate sender key if needed
                var rotateKey = store.clearKeyRotation(groupJid);
                if (rotateKey) {
                    encryption.rotateSenderKey(groupJid, senderJid);
                }

                // WAWebSendGroupKeyDistributionMsgJob: get sender key info
                // and encrypt distribution messages
                var senderKeyBytes = encryption.getSenderKeyBytes(groupJid, senderJid);
                // WAWebSendGroupKeyDistributionMsgJob: yield ensureE2ESessions(f, !1, DEFAULT).
                // WA Web does NOT emit the prekeys-depletion metric for this standalone
                // sender-key distribution job, so neither does Cobalt.
                deviceService.ensureSessions(skDistribDevices);
                var skDistPayloads = senderKeyDistribution.encrypt(
                        groupJid, senderKeyBytes, skDistribDevices);

                // WAWebSendGroupKeyDistributionMsgJob: compute phash
                var phash = fanout.phash();

                // WAWebSendGroupKeyDistributionMsgJob: build participants node
                Node participantsNode = null;
                if (!skDistPayloads.isEmpty()) {
                    participantsNode = ParticipantsStanza.buildSenderKeyDistribution(
                            skDistPayloads, null, "hide");
                }

                // WAWebSendGroupKeyDistributionMsgJob: build identity node
                var needsIdentity = ParticipantsStanza.requiresIdentityNode(skDistPayloads);
                var identityNode = needsIdentity ? buildIdentityNode() : null;

                // WAWebSendGroupKeyDistributionMsgJob: build stanza
                // type="text", device_fanout="false", decrypt-fail not set on stanza
                var metaNode = new NodeBuilder()
                        .description("meta")
                        .attribute("appdata", "default")
                        .build();
                var encNode = new NodeBuilder()
                        .description("enc")
                        .attribute("v", String.valueOf(MessageEncryption.CIPHERTEXT_VERSION))
                        .attribute("type", MessageEncryptionType.SKMSG.protocolValue())
                        .attribute("decrypt-fail", "hide")
                        .build();
                var stanza = new NodeBuilder()
                        .description("message")
                        .attribute("id", msgId)
                        .attribute("to", groupJid)
                        .attribute("phash", phash)
                        .attribute("type", "text")
                        .attribute("device_fanout", "false")
                        .content(metaNode, encNode, participantsNode, identityNode);

                // WAWebSendGroupKeyDistributionMsgJob: flush and send
                flushStore();
                var ackNode = client.sendNode(stanza);
                var ack = AckParser.parse(ackNode);

                // WAWebSendGroupKeyDistributionMsgJob: if error, throw
                if (ack.error().isPresent()) {
                    throw new WhatsAppMessageException.Send.Unknown(
                            "encryptAndSendSenderKeyMsg: Invalid ack from server for " + groupJid, null);
                }

                // WAWebSendGroupKeyDistributionMsgJob: mark SK as distributed
                for (var device : skDistribDevices) {
                    store.markSenderKeyDistributed(groupJid, device);
                }

                return ack;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send key distribution to " + groupJid, e);
        }
    }

    /**
     * Generates RCAT content bindings for all group participants.
     *
     * <p>Only applicable for URL messages with a messageSecret when the
     * group size is within the configured RCAT limit.
     *
     * @return the per-recipient RCAT tags, or {@code null} if not applicable
     *
     * @apiNote WAWebMsgRcatUtils.genContentBindingForMsg: checks type=CHAT,
     * isUrlMessage, isSentByMe, messageSecret != null,
     * recipients.length <= maximum_group_size_for_rcat.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgRcatUtils", exports = "genContentBindingForMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    private Map<Jid, byte[]> generateContentBindings(
            ChatMessageInfo messageInfo,
            List<Jid> participantJids
    ) {
        var messageSecret = messageInfo.messageSecret().orElse(null);
        if (messageSecret == null) {
            return null;
        }

        // WAWebMsgRcatUtils: only for URL messages (extendedTextMessage with matchedText)
        var message = messageInfo.message().content();
        if (!(message instanceof ExtendedTextMessage text) || text.matchedText().isEmpty()) {
            return null;
        }

        // WAWebMsgRcatUtils: check group size limit
        var maxGroupSize = abPropsService.getInt(ABProp.MAXIMUM_GROUP_SIZE_FOR_RCAT);
        if (participantJids.size() > maxGroupSize) {
            return null;
        }

        var contentId = ContentBindingToken.resolveContentId(text.matchedText().get());
        var selfJid = requireSelfJid().toUserJid();

        try {
            return ContentBindingToken.generate(
                    messageInfo.key().id().orElseThrow(), messageSecret,
                    selfJid, participantJids, contentId);
        } catch (GeneralSecurityException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to generate content bindings: {0}", e.getMessage());
            return null;
        }
    }

    /**
     * Returns a copy of the container with {@code capiCreatedGroup = true}
     * set on the message context info.
     *
     * <p>If the container already has a message context info, the flag
     * is set in place on the mutable context info object.  Otherwise,
     * a new context info is created with only the flag set.
     *
     * @param container the original message container
     * @return the container with the CAPI flag applied
     *
     * @implNote WAWebE2EProtoGenerator.updateGroupMsgProtoWithCapiFlag:
     * deep-clones the proto and sets
     * {@code messageContextInfo.capiCreatedGroup = true}.
     */
    @WhatsAppWebExport(moduleName = "WAWebE2EProtoGenerator", exports = "updateGroupMsgProtoWithCapiFlag",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MessageContainer applyCapiFlag(MessageContainer container) {
        var existingCtxInfo = container.messageContextInfo().orElse(null);
        if (existingCtxInfo != null) {
            existingCtxInfo.setCapiCreatedGroup(true);
            return container;
        }

        return container.withMessageContextInfo(
                new ChatMessageContextInfoBuilder()
                        .capiCreatedGroup(true)
                        .build());
    }

    /**
     * Determines whether a message is a CAG addon type that should be
     * sent to LID-addressed participants.
     *
     * @param container the message container
     * @return {@code true} for reactions, comments, event responses,
     *         and poll votes
     *
     * @apiNote WAWebSendGroupMsgJob.isCagAddon: returns {@code true}
     * for reaction_enc, comment, event_response, poll_vote, and
     * protocol addon revokes.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendGroupMsgJob", exports = "isCagAddon",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isCagAddonMessage(MessageContainer container) {
        return switch (container.content()) {
            case EncReactionMessage _, EncCommentMessage _, EncEventResponseMessage _, PollUpdateMessage _ -> true;
            default -> false;
        };
    }

    /**
     * Resends the message to delta devices using group-direct (per-device)
     * encryption after a phash mismatch.
     *
     * <p>Before re-querying the group, emits a {@code MdDeviceSyncAck}
     * WAM event mirroring
     * {@link com.github.auties00.cobalt.wam.event.MdDeviceSyncAckEvent}.
     *
     * @param groupJid               the target group JID
     * @param messageInfo            the outgoing message being resent
     * @param originalDevices        the device list from the initial send
     *                               (used to compute the delta)
     * @param addressingMode         the local addressing mode used to build
     *                               the stanza ({@code "lid"} or {@code "pn"})
     * @param serverAddressingMode   the addressing mode returned on the
     *                               server ack (may be {@code null})
     * @param groupMetadataCandidate the group metadata used to derive the
     *                               WAM {@code localAddressingMode} property
     *                               for the emitted {@code MdDeviceSyncAck}
     *                               event (may be {@code null})
     * @param senderJid              the sender device JID used for this send
     *                               (selected earlier in
     *                               {@link #send(Jid, ChatMessageInfo)} based
     *                               on the group addressing mode); its server
     *                               determines the {@code isLid} property of
     *                               the emitted {@code MdDeviceSyncAck} event
     *
     * @apiNote WAWebResendGroupMsg.resendGroupMsg: re-queries the group,
     * computes delta device list, sends via sendDirectMsgToDeviceList
     * with GROUP_DIRECT fanout type.
     * WAWebPostMdDeviceSyncAckMetric.postMdDeviceSyncAckMetric: emits
     * MdDeviceSyncAck (id 2180) at the top of the resend path.
     */
    @WhatsAppWebExport(moduleName = "WAWebResendGroupMsg", exports = "resendGroupMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSendGroupDirectJob", exports = "encryptAndSendGroupDirectMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebPostMdDeviceSyncAckMetric",
            exports = "postMdDeviceSyncAckMetric", adaptation = WhatsAppAdaptation.DIRECT)
    private void resendAsGroupDirect(
            Jid groupJid,
            ChatMessageInfo messageInfo,
            Collection<Jid> originalDevices,
            String addressingMode,
            String serverAddressingMode,
            ChatMetadata groupMetadataCandidate,
            Jid senderJid
    ) {
        // WAWebResendGroupMsg.resendGroupMsg: before re-querying the group,
        // WAWebPostMdDeviceSyncAckMetric.postMdDeviceSyncAckMetric emits
        // MdDeviceSyncAck (id 2180). For the group branch:
        //   revoke               = isRevokeMsg(msg)
        //   chatType             = getMessageChatTypeFromWid(groupJid) -> GROUP
        //   isLid                = msgRecord.data.from.isLid() (sender device)
        //   localAddressingMode  = getAddressingModeMetricsFromGroupMetadata(groupData)
        //   serverAddressingMode = getWamAddressingModeFromString(serverAddressingMode)
        var senderIsLid = senderJid != null && senderJid.hasLidServer();
        AddressingMode localWamMode = null;
        if (groupMetadataCandidate instanceof GroupMetadata gm) {
            // WAWebWamAddressingModeUtils.getAddressingModeMetricsFromGroupMetadata:
            // returns null when isLidAddressingMode is null/undefined - Cobalt stores
            // a primitive boolean so the mapping is unconditional here.
            localWamMode = gm.isLidAddressingMode() ? AddressingMode.LID : AddressingMode.PN;
        }
        client.wamService().commit(new MdDeviceSyncAckEventBuilder()
                .revoke(UserMessageSender.isRevokeMessage(messageInfo))
                .chatType(UserMessageSender.chatTypeFromJid(groupJid))
                .isLid(senderIsLid)
                .localAddressingMode(localWamMode)
                .serverAddressingMode(wamAddressingMode(serverAddressingMode))
                .build());

        // WAWebResendGroupMsg: re-query group, get refreshed fanout
        // Sender JID for phash: resend path does not emit phash in
        // the stanza, so the exact sender is not critical here
        var refreshedFanout = deviceService.getGroupFanout(groupJid, requireSelfJid());

        // WAWebResendGroupMsg.resendGroupMsg: after sendQueryGroup refreshes the
        // participant record, WAWebMaybePostMdGroupSyncMetrics.maybePostGroupSyncMetrics
        // diffs the original SK user list against the fresh participant record and
        // emits MdGroupParticipantMissAck (id 4146) when either side is non-empty.
        var refreshedMetadata = store.findChatMetadata(groupJid).orElse(null);
        emitMdGroupParticipantMissAck(messageInfo, originalDevices, refreshedMetadata);

        // WAWebResendGroupMsg: delta = refreshed - original
        var originalJids = originalDevices.stream()
                .map(Jid::toString)
                .collect(Collectors.toUnmodifiableSet());
        var deltaDevices = refreshedFanout.devices().stream()
                .filter(device -> !originalJids.contains(device.toString()))
                .toList();

        if (deltaDevices.isEmpty()) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "No new devices after group phash resync for {0}", groupJid);
            return;
        }

        LOGGER.log(System.Logger.Level.DEBUG,
                "Resending as group-direct to {0} new devices for {1}",
                deltaDevices.size(), groupJid);

        // WAWebSendDirectMsgToDeviceList: GROUP_DIRECT fanout
        var container = messageInfo.message();
        var depletedPrekeyCount = deviceService.ensureSessions(deltaDevices);
        // WAWebSendMsgCreateFanoutStanza -> WAWebPostPrekeysDepletionMetric.maybePostPrekeysDepletionMetric
        // emits PrekeysDepletionEvent (id 3014) with SEND_MESSAGE / GROUP for GROUP_DIRECT fanoutType.
        emitPrekeysDepletionEvents(depletedPrekeyCount, MessageType.GROUP, deltaDevices.size());
        var senderIcdc = deviceService.computeIcdc(requireSelfJid())
                .orElse(null);
        var payloads = encryptForDevices(encryption, deltaDevices, container, groupJid, senderIcdc, null);
        if (payloads.isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Group direct: encryption failed for all delta devices for {0}",
                    groupJid);
            return;
        }

        var identityNode = ParticipantsStanza.requiresIdentityNode(payloads)
                ? buildIdentityNode() : null;

        // WAWebSendDirectMsgToDeviceList: includes empty <enc type="skmsg">
        // to signal group-direct fanout to the server
        var emptySkmsgNode = new NodeBuilder()
                .description("enc")
                .attribute("v", String.valueOf(MessageEncryption.CIPHERTEXT_VERSION))
                .attribute("type", MessageEncryptionType.SKMSG.protocolValue())
                .attribute("mediatype", resolveMediaType(container))
                .build();

        var stanza = ChatFanoutStanza.build(
                messageInfo.key().id().orElseThrow(),
                groupJid,
                resolveStanzaType(container),
                payloads,
                resolveEditAttribute(container),
                addressingMode,
                null,
                resolveMediaType(container),
                resolveDecryptFail(container),
                resolveNativeFlowName(container),
                null,
                false,
                null,
                null,
                null,
                null,
                identityNode,
                metaStanza.buildChat(groupJid, container, null),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                emptySkmsgNode
        );

        flushStore();
        client.sendNode(stanza);
    }

    /**
     * Emits the {@code MdGroupParticipantMissAck} WAM event (id 4146) when
     * the group's participant record has changed between the original SKMSG
     * fan-out and the post-phash-mismatch re-query.
     *
     * <p>The event is suppressed when no participants were added or removed,
     * matching WA Web's {@code maybePostGroupSyncMetrics} guard.
     *
     * @param messageInfo      the message being resent (drives
     *                         {@code messageIsRevoke})
     * @param originalDevices  the device JIDs from the initial SKMSG send
     *                         (drives {@code isLid} and the removed-side of
     *                         the diff)
     * @param refreshedMetadata the refreshed chat metadata obtained after
     *                          the group re-query (drives the added-side of
     *                          the diff, {@code groupSizeBucket}, and
     *                          {@code typeOfGroup}); may be {@code null}
     *
     * @apiNote WAWebMaybePostMdGroupSyncMetrics.maybePostGroupSyncMetrics:
     * computes {@code added}/{@code removed} via {@code computeParticipantChange},
     * skips when both are zero, otherwise commits the event with
     * {@code messageIsRevoke}, {@code groupSizeBucket}, {@code typeOfGroup},
     * {@code isLid}, {@code participantAddCount}, {@code participantRemoveCount}.
     * WAWebResendGroupMsg.resendGroupMsg: invokes the helper after
     * {@code sendQueryGroup} via function {@code E}, passing the deduped
     * user-wid list derived from the original SKMSG device list.
     */
    @WhatsAppWebExport(moduleName = "WAWebMaybePostMdGroupSyncMetrics",
            exports = "maybePostGroupSyncMetrics", adaptation = WhatsAppAdaptation.DIRECT)
    private void emitMdGroupParticipantMissAck(
            ChatMessageInfo messageInfo,
            Collection<Jid> originalDevices,
            ChatMetadata refreshedMetadata
    ) {
        // WAWebResendGroupMsg.resendGroupMsg: D = Array.from(new Set(S.map(asUserWidOrThrow)))
        // dedupes the original SKMSG device list down to user JIDs before diffing.
        var originalUserJids = originalDevices.stream()
                .map(Jid::toUserJid)
                .map(Jid::toString)
                .collect(Collectors.toUnmodifiableSet());

        // WAWebGroupMsgSendUtils.getParticipantRecord: returns the participant
        // table row for the group; Cobalt reads the equivalent data from the
        // refreshed chat metadata just updated by queryChatMetadata.
        Set<String> currentUserJids;
        if (refreshedMetadata instanceof GroupMetadata gm) {
            currentUserJids = gm.participants().stream()
                    .map(p -> p.userJid().toString())
                    .collect(Collectors.toUnmodifiableSet());
        } else {
            currentUserJids = Set.of();
        }

        // WAWebMaybePostMdGroupSyncMetrics.computeParticipantChange:
        // added = |current \ original|, removed = |original \ current|
        var added = 0;
        for (var jid : currentUserJids) {
            if (!originalUserJids.contains(jid)) {
                added++;
            }
        }
        var removed = 0;
        for (var jid : originalUserJids) {
            if (!currentUserJids.contains(jid)) {
                removed++;
            }
        }

        // WAWebMaybePostMdGroupSyncMetrics: skip commit when no change
        if (added == 0 && removed == 0) {
            return;
        }

        // WAWebMaybePostMdGroupSyncMetrics: p.isLid = t.some(w => w.isLid())
        // The deduped original wid list carries LID servers if any device was
        // addressed via LID on the initial send.
        var isLid = originalDevices.stream().anyMatch(Jid::hasLidServer);

        // WAWebMaybePostMdGroupSyncMetrics: groupSizeBucket uses
        // groupData.participantCount (participant record size, capped at 32
        // minimum via WAWebWamGroupMetricUtils.capCount).
        int participantCount = 0;
        if (refreshedMetadata instanceof GroupMetadata gm) {
            participantCount = gm.participants().size();
        }
        var groupSizeBucket = toGroupSizeBucket(Math.max(participantCount, 32));

        // WAWebMaybePostMdGroupSyncMetrics: typeOfGroup = groupData.wamTypeOfGroup
        // ?? TYPE_OF_GROUP_ENUM.GROUP
        var typeOfGroup = refreshedMetadata instanceof GroupMetadata gm
                ? typeOfGroupFromMetadata(gm)
                : TypeOfGroupEnum.GROUP;

        // WAWebMaybePostMdGroupSyncMetrics: messageIsRevoke =
        // WAWebSendMsgCommonApi.isRevokeMsg(msgProtobuf)
        client.wamService().commit(new MdGroupParticipantMissAckEventBuilder()
                .messageIsRevoke(UserMessageSender.isRevokeMessage(messageInfo))
                .groupSizeBucket(groupSizeBucket)
                .typeOfGroup(typeOfGroup)
                .isLid(isLid)
                .participantAddCount(added)
                .participantRemoveCount(removed)
                .build());
    }

    /**
     * Buckets a participant count into a {@link ClientGroupSizeBucket}.
     *
     * @param count the participant count (already capped to a minimum of 32
     *              by the caller)
     * @return the corresponding bucket, never {@code null}
     *
     * @apiNote WAWebWamNumberToClientGroupSizeBucket: threshold ladder
     * {@code SMALL(<=33)}, {@code MEDIUM(<=65)}, {@code LARGE(<=129)},
     * {@code EXTRA_LARGE(<=257)}, {@code XX_LARGE(<=513)}, {@code LT1024(<=1025)},
     * {@code LT1500(<=1501)}, {@code LT2000(<=2001)}, {@code LT2500(<=2501)},
     * {@code LT3000(<=3001)}, {@code LT3500(<=3501)}, {@code LT4000(<=4001)},
     * {@code LT4500(<=4501)}, {@code LT5000(<=5001)}, else
     * {@code LARGEST_BUCKET}.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamNumberToClientGroupSizeBucket",
            exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    private static ClientGroupSizeBucket toGroupSizeBucket(int count) {
        if (count <= 33) return ClientGroupSizeBucket.SMALL;
        if (count <= 65) return ClientGroupSizeBucket.MEDIUM;
        if (count <= 129) return ClientGroupSizeBucket.LARGE;
        if (count <= 257) return ClientGroupSizeBucket.EXTRA_LARGE;
        if (count <= 513) return ClientGroupSizeBucket.XX_LARGE;
        if (count <= 1025) return ClientGroupSizeBucket.LT1024;
        if (count <= 1501) return ClientGroupSizeBucket.LT1500;
        if (count <= 2001) return ClientGroupSizeBucket.LT2000;
        if (count <= 2501) return ClientGroupSizeBucket.LT2500;
        if (count <= 3001) return ClientGroupSizeBucket.LT3000;
        if (count <= 3501) return ClientGroupSizeBucket.LT3500;
        if (count <= 4001) return ClientGroupSizeBucket.LT4000;
        if (count <= 4501) return ClientGroupSizeBucket.LT4500;
        if (count <= 5001) return ClientGroupSizeBucket.LT5000;
        return ClientGroupSizeBucket.LARGEST_BUCKET;
    }

    /**
     * Maps a {@link GroupMetadata} to the WAM {@link TypeOfGroupEnum} value
     * populated on metrics events.
     *
     * @param metadata the group metadata
     * @return the WAM group type, never {@code null}
     *
     * @apiNote WAWebGroupType.getGroupTypeFromGroupMetadata + groupTypeToWamEnum:
     * {@code defaultSubgroup} -> {@code DEFAULT_SUBGROUP},
     * {@code parentCommunityJid != null} (and not default/general) ->
     * {@code SUBGROUP}, everything else (including community announcement
     * general chat and standalone groups) -> {@code GROUP}.
     */
    @WhatsAppWebExport(moduleName = "WAWebGroupType",
            exports = {"getGroupTypeFromGroupMetadata", "groupTypeToWamEnum"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static TypeOfGroupEnum typeOfGroupFromMetadata(GroupMetadata metadata) {
        if (metadata.isDefaultSubgroup()) {
            return TypeOfGroupEnum.DEFAULT_SUBGROUP;
        }
        if (metadata.isGeneralSubgroup()) {
            return TypeOfGroupEnum.GROUP;
        }
        if (metadata.parentCommunityJid().isPresent()) {
            return TypeOfGroupEnum.SUBGROUP;
        }
        return TypeOfGroupEnum.GROUP;
    }

    /**
     * Migrates a group's addressing mode from PN→LID or LID→PN.
     *
     * <p>Converts each participant's JID to the target addressing mode,
     * rebuilds the group metadata with the updated participants and
     * addressing mode flag, and clears all sender key distribution state
     * so that keys are redistributed on the next send.
     *
     * @param groupJid the group JID
     * @param toLid    {@code true} to migrate to LID, {@code false} to PN
     *
     * @apiNote WAWebGroupHandleAddressingModeMismatch.handleAddressingModeMismatch:
     * calls {@code migrateParticipantInfoAddressingMode} to convert all
     * participant, admin, and sender-key device JIDs, then updates the
     * group metadata's {@code isLidAddressingMode} flag.
     * WAWebDBGroupParticipant.migrateParticipantInfoAddressingMode:
     * maps each JID through {@code toAddressingModeFactory(isLid)},
     * resets all sender keys to not-distributed, and persists the changes.
     */
    @WhatsAppWebExport(moduleName = "WAWebGroupHandleAddressingModeMismatch", exports = "handleAddressingModeMismatch",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebDBGroupParticipant", exports = "migrateParticipantInfoAddressingMode",
            adaptation = WhatsAppAdaptation.DIRECT)
    private void migrateAddressingMode(Jid groupJid, boolean toLid) {
        var metadata = store.findChatMetadata(groupJid).orElse(null);
        if (metadata == null) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Cannot migrate addressing mode for {0}: no metadata", groupJid);
            return;
        }

        // WAWebDBGroupParticipant.migrateParticipantInfoAddressingMode:
        // convert each participant JID to the target addressing mode
        var migratedParticipants = new ArrayList<GroupParticipant>();
        for (var participant : metadata.participants()) {
            var convertedJid = convertJid(participant.userJid(), toLid);
            if (convertedJid != null) {
                migratedParticipants.add(new GroupParticipantBuilder()
                        .userJid(convertedJid)
                        .rank(participant.rank().orElse(null))
                        .build());
            } else {
                // WAWebDBGroupParticipant: if conversion fails, keep original
                LOGGER.log(System.Logger.Level.DEBUG,
                        "No {0} mapping for {1}, keeping original",
                        toLid ? "LID" : "PN", participant.userJid());
                migratedParticipants.add(participant);
            }
        }

        // WAWebDBGroupParticipant: update metadata in place
        metadata.clearParticipants();
        metadata.addAllParticipants(migratedParticipants);
        metadata.setLidAddressingMode(toLid);

        // WAWebDBGroupParticipant: reset all sender keys to not-distributed
        store.clearSenderKeyDistribution(groupJid);

        LOGGER.log(System.Logger.Level.INFO,
                "Migrated addressing mode for {0} to {1} ({2} participants)",
                groupJid, toLid ? "lid" : "pn", migratedParticipants.size());
    }

    /**
     * Converts a JID to the target addressing mode.
     *
     * @param jid   the JID to convert
     * @param toLid {@code true} to convert PN→LID, {@code false} for LID→PN
     * @return the converted JID, or {@code null} if no mapping exists
     *
     * @apiNote WAWebLidMigrationUtils.toAddressingModeFactory: returns a
     * function that converts between PN and LID addressing modes.
     */
    @WhatsAppWebExport(moduleName = "WAWebLidMigrationUtils", exports = "toAddressingModeFactory",
            adaptation = WhatsAppAdaptation.DIRECT)
    private Jid convertJid(Jid jid, boolean toLid) {
        if (toLid) {
            return jid.hasLidServer() ? jid : store.findLidByPhone(jid).orElse(null);
        } else {
            return jid.hasUserServer() ? jid : store.findPhoneByLid(jid).orElse(null);
        }
    }

    /**
     * Converts the stanza addressing-mode string ({@code "lid"} or {@code "pn"})
     * to the WAM {@link AddressingMode} enum used by
     * {@link AddressingModeMismatchEventBuilder}.
     *
     * @param mode the stanza addressing mode string
     * @return the corresponding {@link AddressingMode}, or {@code null} if
     *         {@code mode} is {@code null} or unknown
     *
     * @apiNote WAWebWamAddressingModeUtils.getWamAddressingModeFromString:
     *          maps {@code STANZA_MSG_ADDRESSING_MODE.lid} to
     *          {@code ADDRESSING_MODE.LID}, everything else to
     *          {@code ADDRESSING_MODE.PN}.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamAddressingModeUtils",
            exports = "getWamAddressingModeFromString",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static AddressingMode wamAddressingMode(String mode) {
        if (mode == null) {
            return null;
        }
        return "lid".equals(mode) ? AddressingMode.LID : AddressingMode.PN;
    }

    /**
     * Emits one {@link com.github.auties00.cobalt.wam.event.PrekeysDepletionEvent} per
     * depleted one-time pre-key reported by the last {@code ensureSessions} call.
     *
     * <p>No-op when {@code depletedPrekeyCount} is {@code 0}, matching the early return in
     * {@code WAWebPostPrekeysDepletionMetric.maybePostPrekeysDepletionMetric}.
     *
     * @param depletedPrekeyCount number of depleted one-time pre-keys (may be zero)
     * @param messageType         the WAM message type for this send
     * @param deviceCount         the number of devices in the fanout; used to bucket
     *                            {@code deviceSizeBucket} via
     *                            {@link WamSizeBuckets#numberToSizeBucket(int)}
     *
     * @implNote WAWebPostPrekeysDepletionMetric.maybePostPrekeysDepletionMetric: emits
     *     {@code t} (count) events inside a {@code setTimeout(...,0)}; Cobalt emits
     *     synchronously on the calling virtual thread.
     */
    @WhatsAppWebExport(moduleName = "WAWebPostPrekeysDepletionMetric",
            exports = "maybePostPrekeysDepletionMetric",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private void emitPrekeysDepletionEvents(int depletedPrekeyCount, MessageType messageType, Integer deviceCount) {
        // WAWebPostPrekeysDepletionMetric.maybePostPrekeysDepletionMetric: early-return guard
        if (depletedPrekeyCount <= 0) {
            return;
        }
        var bucket = deviceCount == null ? null : WamSizeBuckets.numberToSizeBucket(deviceCount);
        // WAWebPostPrekeysDepletionMetric.maybePostPrekeysDepletionMetric: for (var e=0;e<t;e++) commit()
        for (var i = 0; i < depletedPrekeyCount; i++) {
            client.wamService().commit(new PrekeysDepletionEventBuilder()
                    .prekeysFetchReason(PrekeysFetchContext.SEND_MESSAGE)
                    .messageType(messageType)
                    .deviceSizeBucket(bucket)
                    .build());
        }
    }
}
