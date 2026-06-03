package com.github.auties00.cobalt.message;

import com.github.auties00.cobalt.ack.AckParser;
import com.github.auties00.cobalt.ack.AckResult;
import com.github.auties00.cobalt.ack.CallAck;
import com.github.auties00.cobalt.call.internal.signaling.CallStanza;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.device.DeviceService;
import com.github.auties00.cobalt.media.transcode.MediaTranscoderService;
import com.github.auties00.cobalt.message.crypto.SignalCryptoLocks;
import com.github.auties00.cobalt.message.receive.LiveMessageReceivingService;
import com.github.auties00.cobalt.message.receive.MessageReceivingService;
import com.github.auties00.cobalt.message.receive.crypto.MessageDecryption;
import com.github.auties00.cobalt.message.send.LiveMessageSendingService;
import com.github.auties00.cobalt.message.send.MessageSendingService;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryptedPayload;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryption;
import com.github.auties00.cobalt.migration.LidMigrationService;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.device.identity.ADVSignedDeviceIdentitySpec;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.MessageContainerBuilder;
import com.github.auties00.cobalt.model.message.MessageContainerSpec;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.model.message.call.CallOfferMessageBuilder;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Live implementation of {@link MessageService} that fans message traffic between the outbound
 * send pipeline and the inbound receive pipeline behind a single facade.
 *
 * <p>The two sub-services are assembled from the supplied collaborators in the constructor and
 * share the {@link LinkedWhatsAppClient#store() client store}, so the send and receive sides
 * observe a single source of truth for sessions, devices, and pending-message caches.
 *
 * @implNote This implementation collapses WA Web's two separate entry points,
 * {@code WAWebSendMsgJob.encryptAndSendMsg} for outbound fanout and
 * {@code WAWebCommsHandleMessagingStanza.handleMessagingStanza} for inbound
 * dispatch, into one facade that owns no state of its own.
 */
public final class LiveMessageService implements MessageService {
    /**
     * Holds the owning client; used by {@link #sendCall(Jid, String, byte[], boolean)} to
     * dispatch the offer stanza and read the call host's JID off the account store.
     */
    private final LinkedWhatsAppClient client;

    /**
     * Holds the {@link MessageEncryption} used to wrap the call-key plaintext per peer device.
     */
    private final MessageEncryption encryption;

    /**
     * Holds the {@link MessageDecryption} used by
     * {@link #processCall(Jid, MessageEncryptionType, byte[])}.
     */
    private final MessageDecryption decryption;

    /**
     * Holds the {@link DeviceService} used by {@link #sendCall(Jid, String, byte[], boolean)}
     * to sync the peer's device list and ensure Signal sessions exist before encryption.
     */
    private final DeviceService deviceService;

    /**
     * Holds the {@link LidMigrationService} used by
     * {@link #sendCall(Jid, String, byte[], boolean)} to resolve self and peer to the
     * call's canonical addressing mode (LID where migrated, PN otherwise).
     */
    private final LidMigrationService lidMigrationService;

    /**
     * Holds the outbound pipeline owning device fetch, fanout, encryption, and
     * stanza emission.
     */
    private final MessageSendingService sendingService;

    /**
     * Holds the inbound pipeline owning stanza parsing, Signal decryption, and
     * {@link MessageInfo} construction.
     */
    private final MessageReceivingService receivingService;


    /**
     * Wires the send and receive pipelines from the supplied collaborators.
     *
     * <p>The two pipelines share the {@link LinkedWhatsAppClient#store() client store}
     * via the supplied {@link MessageEncryption} and {@link MessageDecryption}, which
     * must themselves share a single {@link SignalCryptoLocks} registry so a concurrent
     * encrypt and decrypt for the same device session or sender-key chain serialise rather
     * than racing the non-atomic Signal ratchet. The caller (typically
     * {@code LiveLinkedWhatsAppClient}) constructs both encryption helpers from one
     * lock registry and hands them in here.
     *
     * @param client              the {@link LinkedWhatsAppClient} used to send
     *                            stanzas and to register inbound stanza
     *                            handlers
     * @param encryption          the {@link MessageEncryption} used by the outbound
     *                            pipeline; the same instance must be shared with any
     *                            other service that encrypts to Signal sessions (the
     *                            call-signaling layer's offer fanout)
     * @param decryption          the {@link MessageDecryption} used by the inbound
     *                            pipeline; must share its lock registry with {@code encryption}
     * @param deviceService       the {@link DeviceService} consulted to
     *                            resolve per-user device lists before each
     *                            fanout
     * @param lidMigrationService the {@link LidMigrationService} that gates
     *                            the PN-to-LID stanza rewrite in the
     *                            user-chat send path
     * @param abPropsService      the {@link ABPropsService} consulted to
     *                            gate optional protocol behaviour
     * @param wamService             the {@link WamService} forwarded to
     *                               the sending pipeline for end-to-end
     *                               telemetry events
     * @param mediaTranscoderService the {@link MediaTranscoderService}
     *                               threaded into the sending pipeline
     *                               for link-preview decoration
     * @throws NullPointerException if any argument is {@code null}
     */
    public LiveMessageService(
            LinkedWhatsAppClient client,
            MessageEncryption encryption,
            MessageDecryption decryption,
            DeviceService deviceService,
            LidMigrationService lidMigrationService,
            ABPropsService abPropsService,
            WamService wamService,
            MediaTranscoderService mediaTranscoderService
    ) {
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(encryption, "encryption");
        Objects.requireNonNull(decryption, "decryption");
        Objects.requireNonNull(deviceService, "deviceService");
        Objects.requireNonNull(lidMigrationService, "lidMigrationService");
        Objects.requireNonNull(abPropsService, "abPropsService");
        Objects.requireNonNull(wamService, "wamService");
        Objects.requireNonNull(mediaTranscoderService, "mediaTranscoderService");

        this.client = client;
        this.encryption = encryption;
        this.decryption = decryption;
        this.deviceService = deviceService;
        this.lidMigrationService = lidMigrationService;
        var store = client.store();
        this.sendingService = new LiveMessageSendingService(client, encryption, deviceService, lidMigrationService, abPropsService, wamService, mediaTranscoderService);
        this.receivingService = new LiveMessageReceivingService(store, decryption);
    }

    @Override
    public AckResult send(Jid chatJid, MessageContainer container) {
        return sendingService.send(chatJid, container);
    }

    @Override
    public AckResult send(MessageInfo messageInfo) {
        return sendingService.send(messageInfo);
    }

    @Override
    public AckResult sendPeer(Jid targetDevice, ChatMessageInfo messageInfo) {
        return sendingService.sendPeer(targetDevice, messageInfo);
    }

    @Override
    public MessageInfo process(Node node) {
        return receivingService.process(node);
    }

    @Override
    public void clearPendingMessages() {
        receivingService.clearPendingMessages();
    }

    @Override
    public CallAck sendCall(Jid peer, String callId, byte[] callKey, boolean video) {
        Objects.requireNonNull(peer, "peer cannot be null");
        Objects.requireNonNull(callId, "callId cannot be null");
        Objects.requireNonNull(callKey, "callKey cannot be null");

        var store = client.store();
        var selfJid = store.accountStore().jid()
                .orElseThrow(() -> new IllegalStateException("Not logged in"));
        // Resolve self LID with device suffix preserved; fall back to PN when no LID is mapped.
        var selfUserLid = store.accountStore().lid().map(Jid::toUserJid).orElse(null);
        var resolvedSelf = selfUserLid != null && selfJid.hasDevice()
                ? selfUserLid.withDevice(selfJid.device())
                : selfJid;
        // Resolve peer LID; fall back to the input when no mapping exists.
        var peerLid = lidMigrationService.toLid(peer);
        var resolvedPeer = peerLid != null ? peerLid : peer;

        // Build the call-key envelope plaintext, wrapped as a MessageContainer{call:Call{callKey}}.
        var callOffer = new CallOfferMessageBuilder().callKey(callKey).build();
        var container = new MessageContainerBuilder().call(callOffer).build();
        var plaintext = MessageContainerSpec.encode(container);

        // Resolve the per-call <privacy> payload. WA Web embeds the peer's server-issued
        // trusted-contact token (delivered out-of-band via <notification type="privacy_token">)
        // verbatim; the receiver's voip wasm validates the bytes against the token it issued and
        // rejects with status 70019 when they do not match. When no token is cached yet, vouch
        // for the peer (which triggers the server to deliver the reciprocal token) and wait
        // briefly for the privacy-token notification to land.
        var privacyBytes = resolvePeerTcToken(resolvedPeer);

        // Sync the peer's device list in the resolved addressing mode.
        var deviceLists = deviceService.syncAndGetDeviceList(List.of(resolvedPeer));
        var peerDeviceJids = new ArrayList<Jid>();
        for (var list : deviceLists) {
            for (var device : list.devices()) {
                peerDeviceJids.add(list.userJid().withDevice(device.id()));
            }
        }

        // TEMP EXPERIMENT: force fresh Signal sessions (PKMSG) for every peer device to test whether a
        // stale/one-sided session on the recipient's primary causes it to silently reject the call.
        for (var deviceJid : peerDeviceJids) {
            try {
                store.signalStore().removeSession(deviceJid.toSignalAddress());
            } catch (RuntimeException _) {
            }
        }

        // Ensure a Signal session exists for every peer device before per-device encryption.
        deviceService.ensureSessions(peerDeviceJids);

        // Encrypt the plaintext per peer device.
        var destinationPayloads = new ArrayList<MessageEncryptedPayload>(peerDeviceJids.size());
        for (var deviceJid : peerDeviceJids) {
            destinationPayloads.add(encryption.encryptForDevice(deviceJid, plaintext));
        }
        System.out.println("[CALL-FANOUT] resolvedPeer=" + resolvedPeer
                + " selfDevice=" + selfJid.device()
                + " peerDevices=" + peerDeviceJids
                + " encTypes=" + destinationPayloads.stream()
                        .map(p -> p.recipientJid() + "=" + p.type() + "/" + p.ciphertext().length).toList());

        var deviceIdentity = store.signalStore().signedDeviceIdentity()
                .map(ADVSignedDeviceIdentitySpec::encode)
                .orElse(new byte[0]);

        var ackNode = client.sendNode(CallStanza.offer(
                resolvedPeer, resolvedSelf, callId, video,
                privacyBytes, null, destinationPayloads, deviceIdentity, null, null));
        var parsed = AckParser.parse(ackNode);
        if (parsed instanceof CallAck callAck) {
            return callAck;
        }
        throw new IllegalStateException(
                "Server returned a non-call ACK for the call offer: " + parsed);
    }

    @Override
    public CallAck sendGroupCall(Jid group, java.util.Collection<Jid> participants, String callId,
                                 byte[] callKey, boolean video) {
        Objects.requireNonNull(group, "group cannot be null");
        Objects.requireNonNull(participants, "participants cannot be null");
        Objects.requireNonNull(callId, "callId cannot be null");
        Objects.requireNonNull(callKey, "callKey cannot be null");

        var store = client.store();
        var selfJid = store.accountStore().jid()
                .orElseThrow(() -> new IllegalStateException("Not logged in"));
        var selfUserLid = store.accountStore().lid().map(Jid::toUserJid).orElse(null);
        // The device-less user LID identifies the caller as a <user> in <group_info>; the captured
        // member-received offer shows the call-creator rewritten to this device-less form on delivery.
        var resolvedSelf = selfUserLid != null ? selfUserLid : selfJid.toUserJid();
        // The OUTGOING offer's call-creator carries the caller's device suffix, exactly as the captured
        // working 1:1 caller offer does (`<user>:<device>@lid`); the server strips it to the device-less
        // form before fanning out. A companion that sends a device-less creator has its offer dropped.
        var resolvedSelfDevice = selfJid.hasDevice() ? resolvedSelf.withDevice(selfJid.device()) : resolvedSelf;
        // caller_pn is the caller's phone-number JID (user@s.whatsapp.net), captured on real group
        // offers; the server uses it to identify the caller.
        var callerPn = selfJid.toUserJid().withServer(com.github.auties00.cobalt.model.jid.JidServer.user());

        var deviceIdentity = store.signalStore().signedDeviceIdentity()
                .map(ADVSignedDeviceIdentitySpec::encode)
                .orElse(new byte[0]);

        // Shape matched verbatim to a captured WA Web SENT group offer: the <call> targets the
        // CALL-ID @call (NOT the group jid); the <offer> carries only call-id/call-creator/group-jid;
        // and <group_info> is BARE (no attributes) listing one <user jid=..> per participant (the caller
        // first) with each user's device JIDs, and a <capability> on the caller's own device. The server
        // adds state/user_pn/transaction-id/media/connected-limit/joinable/relay when it fans the offer
        // out and when it acks the caller; the caller sends none of those.
        var capabilityBytes = new byte[]{0x01, 0x05, (byte) 0xF7, 0x09, (byte) 0xE4, (byte) 0xBB, 0x13};
        // Each <user> lists that account's FULL device set (so the per-user phash the server computes
        // over the offer matches its records; phashV2 = sha256 of the sorted legacy-full device WIDs).
        // The caller is listed first and the <capability> rides on the caller's own device.
        var userJids = new java.util.LinkedHashSet<Jid>();
        userJids.add(resolvedSelf);
        for (var participant : participants) {
            var lid = lidMigrationService.toLid(participant);
            userJids.add((lid != null ? lid : participant).toUserJid());
        }
        var groupInfoChildren = new java.util.ArrayList<Node>();
        for (var list : deviceService.syncAndGetDeviceList(new java.util.ArrayList<>(userJids))) {
            var userJid = list.userJid().toUserJid();
            var deviceNodes = new java.util.ArrayList<Node>(list.devices().size());
            for (var device : list.devices()) {
                var deviceJid = userJid.withDevice(device.id());
                var deviceBuilder = new NodeBuilder()
                        .description("device")
                        .attribute("jid", deviceJid);
                if (deviceJid.equals(resolvedSelfDevice)) {
                    deviceBuilder.content(new NodeBuilder()
                            .description("capability")
                            .attribute("ver", "1")
                            .content(capabilityBytes)
                            .build());
                }
                deviceNodes.add(deviceBuilder.build());
            }
            groupInfoChildren.add(new NodeBuilder()
                    .description("user")
                    .attribute("jid", userJid)
                    .content(deviceNodes)
                    .build());
        }
        var groupInfo = new NodeBuilder()
                .description("group_info")
                .content(groupInfoChildren)
                .build();

        var callTarget = com.github.auties00.cobalt.model.jid.Jid.of(callId + "@call");
        var ackNode = client.sendNode(CallStanza.offer(
                callTarget, resolvedSelfDevice, callId, video,
                null, null, java.util.List.of(), deviceIdentity, group, null, callerPn, groupInfo));
        var parsed = AckParser.parse(ackNode);
        if (parsed instanceof CallAck callAck) {
            return callAck;
        }
        throw new IllegalStateException(
                "Server returned a non-call ACK for the group-call offer: " + parsed);
    }

    @Override
    public byte[] processCall(Jid senderJid, MessageEncryptionType encType, byte[] ciphertext) {
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        Objects.requireNonNull(encType, "encType cannot be null");
        Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        return decryption.decryptFromDevice(ciphertext, senderJid, encType);
    }

    /**
     * Time the call-offer path waits for a reciprocal {@code <notification type="privacy_token">}
     * to land after issuing our own trusted-contact token.
     */
    private static final Duration TC_TOKEN_AWAIT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Resolves the peer's trusted-contact token bytes that go into the offer's {@code <privacy>}
     * child, vouching for the peer and waiting briefly when no token is cached yet.
     *
     * @param peer the peer JID, already mapped to the call's canonical addressing mode
     * @return the TC token bytes; never {@code null}
     * @throws com.github.auties00.cobalt.exception.WhatsAppCallSetupException if no token is
     *                                                                        available after vouching
     */
    private byte[] resolvePeerTcToken(Jid peer) {
        var peerUser = peer.toUserJid();
        var cached = readChatTcToken(peerUser);
        if (cached != null) {
            return cached;
        }

        try {
            client.issueTrustedContactToken(peerUser);
        } catch (RuntimeException _) {
            // Best-effort: even when vouching fails, the chat may already have a recent token.
        }

        try {
            var awaited = client.awaitTrustedContactToken(peerUser, null, TC_TOKEN_AWAIT_TIMEOUT);
            if (awaited.isPresent()) {
                return awaited.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        var afterIssue = readChatTcToken(peerUser);
        if (afterIssue != null) {
            return afterIssue;
        }

        throw new IllegalStateException(
                "No trusted-contact token available for peer " + peer
                        + "; the receiver will reject the call offer with status 70019.");
    }

    /**
     * Returns the chat-cached TC token for the given peer, looking up by both the raw JID and its
     * LID-mapped form, or {@code null} when no chat or token exists yet.
     *
     * @param peer the peer user JID
     * @return the cached token bytes, or {@code null}
     */
    private byte[] readChatTcToken(Jid peer) {
        var chatStore = client.store().chatStore();
        var chat = chatStore.findChatByJid(peer).orElse(null);
        if (chat == null) {
            var lid = lidMigrationService.toLid(peer);
            if (lid != null) {
                chat = chatStore.findChatByJid(lid).orElse(null);
            }
        }
        return chat == null ? null : chat.tcToken().orElse(null);
    }
}
