package com.github.auties00.cobalt.message.send;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.device.icdc.IcdcResult;
import com.github.auties00.cobalt.exception.WhatsAppCorruptedStoreException;
import com.github.auties00.cobalt.message.send.ack.AckResult;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryptedPayload;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryption;
import com.github.auties00.cobalt.message.send.icdc.IcdcEnricher;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.ChatKeepType;
import com.github.auties00.cobalt.model.device.identity.ADVSignedDeviceIdentitySpec;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.EmptyMessage;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.MessageContainerSpec;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.model.message.contact.ContactMessage;
import com.github.auties00.cobalt.model.message.contact.ContactsArrayMessage;
import com.github.auties00.cobalt.model.message.event.EncEventResponseMessage;
import com.github.auties00.cobalt.model.message.event.EventMessage;
import com.github.auties00.cobalt.model.message.group.GroupInviteMessage;
import com.github.auties00.cobalt.model.message.interactive.InteractiveMessage;
import com.github.auties00.cobalt.model.message.interactive.InteractiveResponseMessage;
import com.github.auties00.cobalt.model.message.location.LiveLocationMessage;
import com.github.auties00.cobalt.model.message.location.LocationMessage;
import com.github.auties00.cobalt.model.message.media.*;
import com.github.auties00.cobalt.model.message.newsletter.NewsletterAdminInviteMessage;
import com.github.auties00.cobalt.model.message.poll.PollCreationMessage;
import com.github.auties00.cobalt.model.message.poll.PollResultSnapshotMessage;
import com.github.auties00.cobalt.model.message.poll.PollUpdateMessage;
import com.github.auties00.cobalt.model.message.security.EncReactionMessage;
import com.github.auties00.cobalt.model.message.security.SecretEncMessage;
import com.github.auties00.cobalt.model.message.system.*;
import com.github.auties00.cobalt.model.message.text.ExtendedTextMessage;
import com.github.auties00.cobalt.model.message.text.ReactionMessage;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.wam.event.E2eMessageSendEventBuilder;
import com.github.auties00.cobalt.wam.type.AddressingMode;
import com.github.auties00.cobalt.wam.type.AgentEngagementEnumType;
import com.github.auties00.cobalt.wam.type.E2eCiphertextType;
import com.github.auties00.cobalt.wam.type.E2eDestination;
import com.github.auties00.cobalt.wam.type.EditType;
import com.github.auties00.cobalt.wam.type.EncryptionTypeCode;
import com.github.auties00.cobalt.wam.type.MediaType;

import java.io.IOException;
import java.util.*;

/**
 * Base class for all message senders, providing shared cryptographic,
 * stanza-attribute resolution, and encoding utilities.
 *
 * <p>Subclasses implement the chat-type-specific orchestration logic
 * while inheriting common operations such as per-device encryption,
 * stanza type/edit/decrypt-fail resolution, and identity node construction.
 *
 * @apiNote WAWebSendMsgCommonApi: shared utilities used across all send paths.
 * WAWebE2EProtoUtils: type, edit, and decrypt-fail attribute resolution.
 * WAWebAdvSignatureApi: ADV identity node encoding.
 * WAWebBackendJobsCommon: ciphertext version and media type extraction.
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgCommonApi")
@WhatsAppWebModule(moduleName = "WAWebE2EProtoUtils")
@WhatsAppWebModule(moduleName = "WAWebAdvSignatureApi")
@WhatsAppWebModule(moduleName = "WAWebBackendJobsCommon")
abstract sealed class MessageSender<T extends MessageInfo> permits UserMessageSender, GroupMessageSender, StatusMessageSender, NewsletterMessageSender, PeerMessageSender {
    /**
     * Logger for diagnostic output during per-device encryption.
     */
    private static final System.Logger LOGGER = System.getLogger(MessageSender.class.getName());

    /**
     * The WhatsApp client used to send wire stanzas and report failures.
     *
     * @implNote ADAPTED: WAWebSendMsgJob uses module-level imports for
     * the socket and error handling infrastructure; Cobalt injects the
     * client reference via constructor.
     */
    final WhatsAppClient client;

    /**
     * The WhatsApp store holding Signal sessions, device lists,
     * identity records, and other shared state.
     *
     * @implNote ADAPTED: WAWebSignalProtocolStore, WAWebChatCollection,
     * WAWebContactCollection are module-level imports; Cobalt uses the
     * unified {@code WhatsAppStore} facade.
     */
    final WhatsAppStore store;

    /**
     * Creates a new message sender with the specified client.
     *
     * @param client the WhatsApp client for stanza dispatch
     *
     * @implNote ADAPTED: WA Web senders use module-level imports;
     * Cobalt uses constructor-based DI.
     */
    MessageSender(WhatsAppClient client) {
        this.client = Objects.requireNonNull(client, "client");
        this.store = client.store();
    }

    /**
     * Sends a message to the specified chat.
     *
     * @param chatJid     the target JID
     * @param messageInfo the outgoing message with its key, container,
     *                    and metadata
     * @return the server ack result
     */
    abstract AckResult send(Jid chatJid, T messageInfo);

    /**
     * Blocks until the offline message queue has been drained.
     *
     * @apiNote WAWebEventsWaitForOfflineDeliveryEnd: waits for the
     * {@code offline_delivery_end} event before sending.
     */
    @WhatsAppWebExport(moduleName = "WAWebEventsWaitForOfflineDeliveryEnd", exports = "waitForOfflineDeliveryEnd",
            adaptation = WhatsAppAdaptation.DIRECT)
    void waitForOfflineDelivery() {
        store.waitForOfflineDeliveryEnd();
    }

    /**
     * Persists the store's encryption state before sending the wire
     * stanza, ensuring session keys and prekeys survive a crash.
     *
     * @apiNote WAWebEncryptAndSendStatusMsg, WAWebSendMsgJob,
     * WAWebSendGroupSkmsgJob: all call
     * {@code getSignalProtocolStore().flushBufferToDiskIfNotMemOnlyMode()}
     * before the stanza is sent on the wire.
     */
    @WhatsAppWebExport(moduleName = "WAWebSignalProtocolStore", exports = "flushBufferToDiskIfNotMemOnlyMode",
            adaptation = WhatsAppAdaptation.ADAPTED)
    void flushStore() {
        try {
            store.save();
        } catch (IOException ex) {
            client.handleFailure(new WhatsAppCorruptedStoreException(ex));
        }
    }

    /**
     * Returns the JID of the currently logged-in device.
     *
     * @return the self JID
     * @throws IllegalStateException if not logged in
     */
    Jid requireSelfJid() {
        return store.jid().orElseThrow(() ->
                new IllegalStateException("Not logged in"));
    }

    /**
     * Returns the LID of the currently logged-in device, falling back
     * to the PN JID if no LID is registered.
     *
     * @return the self LID or PN JID
     */
    Jid selfLidOrPn() {
        return store.lid().orElseGet(this::requireSelfJid);
    }

    /**
     * Encrypts the message for each device in the list, populating ICDC
     * metadata per device before serialisation.
     *
     * <p>Companion devices (same account as the sender) receive a
     * {@code DeviceSentMessage} wrapper with only the sender's ICDC
     * metadata.  Recipient devices receive both sender and recipient
     * ICDC metadata.
     *
     * @param encryption     the encryption service
     * @param devices        the device JIDs to encrypt for
     * @param container      the message container
     * @param destinationJid the chat recipient JID (used for DeviceSentMessage
     *                       wrapping of companion device payloads)
     * @param senderIcdc     the sender's ICDC metadata, or {@code null}
     * @param recipientIcdc  the recipient's ICDC metadata, or {@code null}
     * @return the encrypted payloads (may be smaller than {@code devices}
     *         if some encryptions failed)
     *
     * @apiNote WAWebSendMsgCreateFanoutStanza: encrypts via
     * encryptMsgProtobuf per device, calling populateICDCMeta for each.
     * WAWebICDCMetaApi.populateICDCMeta: sets deviceListMetadata and
     * deviceListMetadataVersion on the message's messageContextInfo.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebICDCMetaApi", exports = "populateICDCMeta",
            adaptation = WhatsAppAdaptation.DIRECT)
    List<MessageEncryptedPayload> encryptForDevices(
            MessageEncryption encryption,
            Collection<Jid> devices,
            MessageContainer container,
            Jid destinationJid,
            IcdcResult senderIcdc,
            IcdcResult recipientIcdc
    ) {
        var selfJid = store.jid().orElse(null);
        var results = new ArrayList<MessageEncryptedPayload>(devices.size());

        // WAWebICDCMetaApi.populateICDCMeta: for self devices, recipientIcdc is null
        var companionContainer = IcdcEnricher.enrich(container, senderIcdc, null);
        var recipientContainer = IcdcEnricher.enrich(container, senderIcdc, recipientIcdc);

        for (var device : devices) {
            try {
                byte[] devicePlaintext;
                if (selfJid != null && device.toUserJid().equals(selfJid.toUserJid())) {
                    // WAWebDeviceSentMessageProtoUtils.wrapDeviceSentMessage:
                    // wraps the message in a DeviceSentMessage with the
                    // destination JID so companion devices know who the
                    // message was sent to.
                    // If the message has messageContextInfo, it is lifted to
                    // the outer container and removed from the inner message.
                    var innerContextInfo = companionContainer.messageContextInfo().orElse(null);
                    var innerContainer = innerContextInfo != null
                            ? companionContainer.withMessageContextInfo(null)
                            : companionContainer;
                    var deviceSentMessage = new DeviceSentMessageBuilder()
                            .destinationJid(destinationJid)
                            .messageContainer(innerContainer)
                            .build();
                    var wrapped = MessageContainer.of(deviceSentMessage);
                    if (innerContextInfo != null) {
                        wrapped = wrapped.withMessageContextInfo(innerContextInfo);
                    }
                    devicePlaintext = MessageContainerSpec.encode(wrapped);
                } else {
                    devicePlaintext = MessageContainerSpec.encode(recipientContainer);
                }
                var payload = encryption.encryptForDevice(device, devicePlaintext);
                results.add(payload);
                // WAWebEncryptMsgProtobuf -> WAWebPostE2eMessageSendMetric.postSuccessDirectE2eMessageSendMetric:
                // emit E2eMessageSend (id 476) with the resolved ciphertext type on success.
                emitE2eMessageSendEvent(device, container, true, payload.type(), 0);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Encryption fail for {0}: {1}", device, e.getMessage());
                // WAWebEncryptMsgProtobuf -> WAWebPostE2eMessageSendMetric.postFailureDirectE2eMessageSendMetric:
                // emit E2eMessageSend (id 476) with e2eSuccessful=false and weight=1 on failure.
                emitE2eMessageSendEvent(device, container, false, null, 0);
            }
        }

        // WAWebSendMsgCommonApi.updateIdentityRange: track which identity
        // keys were used for this encryption, enabling retry detection
        store.updateIdentityRange(devices);

        return results;
    }

    /**
     * Resolves the stanza {@code type} attribute from the message content.
     *
     * @param container the message container
     * @return one of {@code "text"}, {@code "media"}, {@code "reaction"},
     *         {@code "poll"}, or {@code "event"}
     *
     * @apiNote WAWebE2EProtoUtils.typeAttributeFromProtobuf: unwraps
     * wrapper messages (ephemeral, deviceSent, viewOnce, botInvoke),
     * then pattern-matches on the inner message type.
     * WAWebHandleMsgCommon.STANZA_MSG_TYPES: the possible values.
     */
    @WhatsAppWebExport(moduleName = "WAWebE2EProtoUtils", exports = "typeAttributeFromProtobuf",
            adaptation = WhatsAppAdaptation.DIRECT)
    String resolveStanzaType(MessageContainer container) {
        var message = container.content();
        return switch (message) {
            // WAWebE2EProtoUtils: reactionMessage, encReactionMessage → "reaction"
            case ReactionMessage _ -> "reaction";
            case EncReactionMessage _ -> "reaction";

            // WAWebE2EProtoUtils: eventMessage, encEventResponseMessage,
            // secretEncryptedMessage(EVENT_EDIT) → "event"
            case EventMessage _ -> "event";
            case EncEventResponseMessage _ -> "event";
            case SecretEncMessage s
                    when s.secretEncType().orElse(null) == SecretEncMessage.SecretEncType.EVENT_EDIT -> "event";

            // WAWebE2EProtoUtils: pollCreation*, pollUpdate, pollResultSnapshot → "poll"
            case PollCreationMessage _ -> "poll";
            case PollUpdateMessage _ -> "poll";
            case PollResultSnapshotMessage _ -> "poll";

            // WAWebE2EProtoUtils: extendedTextMessage with non-empty matchedText → "media"
            case ExtendedTextMessage text when text.matchedText().isPresent() -> "media";

            // WAWebE2EProtoUtils: conversation, extendedText, protocolMessage,
            // interactiveMessage, keepInChat, pinInChat, newsletterAdminInvite,
            // requestPhoneNumber, editedMessage → "text"
            case ExtendedTextMessage _ -> "text";
            case ProtocolMessage _ -> "text";
            case InteractiveMessage _ -> "text";
            case InteractiveResponseMessage _ -> "text";
            case KeepInChatMessage _ -> "text";
            case NewsletterAdminInviteMessage _ -> "text";
            case RequestPhoneNumberMessage _ -> "text";
            case EmptyMessage _ -> "text";

            // WAWebE2EProtoUtils: everything else → "media"
            // (image, video, audio, document, sticker, location, contact, etc.)
            default -> "media";
        };
    }

    /**
     * Resolves the stanza {@code edit} attribute from the message content.
     *
     * <p>Equivalent to calling
     * {@code resolveEditAttribute(container, false)}.
     *
     * @param container the message container
     * @return the edit attribute value, or {@code null} for normal messages
     *
     * @implNote WAWebSendMsgCommonApi.editAttribute: checks protobuf
     * message type to determine if this is a revoke (7), admin revoke (8),
     * edit (1), or pin (2).
     * WAWebAck.EDIT_ATTR: the constant values.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "editAttribute",
            adaptation = WhatsAppAdaptation.DIRECT)
    String resolveEditAttribute(MessageContainer container) {
        return resolveEditAttribute(container, false);
    }

    /**
     * Resolves the stanza {@code edit} attribute from the message content,
     * distinguishing admin revokes from sender revokes.
     *
     * @param container      the message container
     * @param isAdminRevoke  {@code true} when the revoking user is a group
     *                       admin revoking another participant's message
     * @return the edit attribute value, or {@code null} for normal messages
     *
     * @implNote WAWebSendMsgCommonApi.editAttribute: uses
     * {@code subtype === "admin_revoke"} to return 8 instead of 7.
     * Also handles secretEncryptedMessage with EVENT_EDIT or MESSAGE_EDIT
     * secretEncType, mapping both to "1" (MESSAGE_EDIT).
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "editAttribute",
            adaptation = WhatsAppAdaptation.DIRECT)
    String resolveEditAttribute(MessageContainer container, boolean isAdminRevoke) {
        var message = container.content();
        return switch (message) {
            // WAWebSendMsgCommonApi: protocolMessage REVOKE →
            // 7 (sender_revoke) or 8 (admin_revoke)
            case ProtocolMessage p when p.type().orElse(null) == ProtocolMessage.Type.REVOKE ->
                    isAdminRevoke ? "8" : "7";

            // WAWebSendMsgCommonApi: protocolMessage MESSAGE_EDIT → 1
            case ProtocolMessage p when p.type().orElse(null) == ProtocolMessage.Type.MESSAGE_EDIT -> "1";

            // WAWebSendMsgCommonApi: secretEncryptedMessage with EVENT_EDIT or
            // MESSAGE_EDIT secretEncType → 1 (MESSAGE_EDIT)
            case SecretEncMessage s
                    when s.secretEncType().orElse(null) == SecretEncMessage.SecretEncType.EVENT_EDIT
                    || s.secretEncType().orElse(null) == SecretEncMessage.SecretEncType.MESSAGE_EDIT -> "1";

            // WAWebSendMsgCommonApi: keepInChatMessage UNDO_KEEP_FOR_ALL → 7
            case KeepInChatMessage keep when isUndoKeepForAll(keep) -> "7";

            // WAWebSendMsgCommonApi: pinInChatMessage → 2
            case PinInChatMessage _ -> "2";

            // WAWebSendMsgCommonApi: reactionMessage with revoked text → 7
            // WAWebReactionsBEUtils.REVOKED_REACTION_TEXT is "" (empty string)
            case ReactionMessage r when r.text().orElse("").isEmpty() -> "7";

            default -> null;
        };
    }

    /**
     * Resolves the {@code decrypt-fail} attribute from the message content.
     *
     * @param container the message container
     * @return {@code "hide"} or {@code null}
     *
     * @apiNote WAWebE2EProtoUtils.decryptFailAttributeFromProtobuf: returns
     * Hide for reactions, poll votes, event responses, edits, revokes,
     * keepInChat, pinInChat, ephemeral sync, welcome messages.
     */
    @WhatsAppWebExport(moduleName = "WAWebE2EProtoUtils", exports = "decryptFailAttributeFromProtobuf",
            adaptation = WhatsAppAdaptation.DIRECT)
    String resolveDecryptFail(MessageContainer container) {
        var message = container.content();
        return switch (message) {
            case ReactionMessage _ -> "hide";
            case EncReactionMessage _ -> "hide";
            case PollUpdateMessage _ -> "hide";
            case KeepInChatMessage _ -> "hide";
            case PinInChatMessage _ -> "hide";
            case EncEventResponseMessage _ -> "hide";
            case SecretEncMessage s
                    when s.secretEncType().orElse(null) == SecretEncMessage.SecretEncType.EVENT_EDIT -> "hide";
            case ProtocolMessage p when p.type().isPresent() -> switch (p.type().get()) {
                case REVOKE, MESSAGE_EDIT, EPHEMERAL_SYNC_RESPONSE, REQUEST_WELCOME_MESSAGE -> "hide";
                default -> null;
            };
            default -> null;
        };
    }

    /**
     * Resolves the {@code mediatype} attribute for the {@code <enc>} node.
     *
     * @param container the message container
     * @return the media type string, or {@code null} for non-media messages
     *
     * @apiNote WAWebBackendJobsCommon.mediaTypeFromProtobuf: extracts the
     * media type from the protobuf, unwrapping deviceSent/ephemeral/viewOnce.
     * WAWebBackendJobsCommon.encodeMaybeMediaType: encodes to stanza string.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobsCommon",
            exports = {"mediaTypeFromProtobuf", "encodeMaybeMediaType"},
            adaptation = WhatsAppAdaptation.DIRECT)
    String resolveMediaType(MessageContainer container) {
        var message = container.content();
        return switch (message) {
            case ImageMessage _ -> "image";
            case VideoMessage v -> v.gifPlayback() ? "gif" : "video";
            case AudioMessage a -> a.ptt() ? "ptt" : "audio";
            case DocumentMessage _ -> "document";
            case StickerMessage _ -> "sticker";
            case LocationMessage l -> l.isLive() ? "livelocation" : "location";
            case LiveLocationMessage _ -> "livelocation";
            case ContactMessage _ -> "vcard";
            case ContactsArrayMessage _ -> "contact_array";
            case GroupInviteMessage _ -> "url";
            case ExtendedTextMessage t when t.matchedText().isPresent() -> "url";
            case PollCreationMessage _ -> null;
            case PollUpdateMessage _ -> null;
            case ReactionMessage _ -> null;
            default -> null;
        };
    }

    /**
     * Resolves the {@code native_flow_name} attribute for the {@code <enc>} node.
     *
     * @param container the message container
     * @return the native flow name, or {@code null}
     *
     * @apiNote WAWebBackendJobsCommon.nativeFlowNameTypeFromProtobuf: extracts
     * the native flow name from interactiveResponseMessage.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobsCommon", exports = "nativeFlowNameTypeFromProtobuf",
            adaptation = WhatsAppAdaptation.DIRECT)
    String resolveNativeFlowName(MessageContainer container) {
        var message = container.content();
        if (!(message instanceof InteractiveResponseMessage irm)) {
            return null;
        }

        var content = irm.content();
        if (content.isEmpty() || !(content.get() instanceof InteractiveResponseMessage.NativeFlowResponseMessage nfr)) {
            return null;
        }

        return nfr.name()
                .orElse(null);
    }

    /**
     * Builds the {@code <device-identity>} node from the stored ADV
     * signed device identity.
     *
     * @return the identity node, or {@code null} if unavailable
     *
     * @apiNote WAWebAdvSignatureApi.getADVEncodedIdentity: serialises
     * the ADV signed device identity to protobuf bytes.
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvSignatureApi", exports = "getADVEncodedIdentity",
            adaptation = WhatsAppAdaptation.DIRECT)
    Node buildIdentityNode() {
        return store.signedDeviceIdentity()
                .map(identity -> new NodeBuilder()
                        .description("device-identity")
                        .content(ADVSignedDeviceIdentitySpec.encode(identity))
                        .build())
                .orElse(null);
    }

    /**
     * Checks whether a KeepInChatMessage represents an "undo keep for all"
     * operation, which uses the sender_revoke edit attribute.
     *
     * @implNote WAWebSendMsgCommonApi: keepInChatMessage with
     * {@code key != null}, {@code key.fromMe === true}, and
     * {@code keepType === UNDO_KEEP_FOR_ALL} maps to SENDER_REVOKE (7).
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "editAttribute",
            adaptation = WhatsAppAdaptation.DIRECT)
    private boolean isUndoKeepForAll(KeepInChatMessage keep) {
        // WAWebSendMsgCommonApi: keepInChatMessage.key != null
        // && keepInChatMessage.key.fromMe === true
        // && keepType === UNDO_KEEP_FOR_ALL
        return keep.key().isPresent()
                && keep.key().get().fromMe()
                && keep.keepType().orElse(null) == ChatKeepType.UNDO_KEEP_FOR_ALL;
    }

    /**
     * Emits the {@code E2eMessageSendEvent} (id 476) for a direct (per-device)
     * Signal encryption attempt.
     *
     * <p>Mirrors {@code WAWebPostE2eMessageSendMetric.postSuccessDirectE2eMessageSendMetric}
     * and {@code postFailureDirectE2eMessageSendMetric}: both helpers build the
     * same event shell keyed by the recipient device JID and message, then flip
     * {@code e2eSuccessful} (and set {@code weight=1} on failure) before committing.
     * Cobalt collapses the two code paths into a single helper invoked from the
     * per-device encryption loop in {@link #encryptForDevices}.
     *
     * @param device     the recipient device JID
     * @param container  the message container being encrypted, or {@code null}
     *                   when the encryption carries no user-visible payload
     *                   (for example sender-key distribution messages)
     * @param success    {@code true} for a successful encryption,
     *                   {@code false} for a failure
     * @param ciphertextType the resolved Signal ciphertext type on success, or
     *                   {@code null} on failure (matches WA Web leaving
     *                   {@code e2eCiphertextType} unset when the type is unknown)
     * @param retryCount the retry count passed through from the sender job;
     *                   {@code 0} for a fresh send
     *
     * @implNote WAWebPostE2eMessageSendMetric: populates
     * {@code e2eCiphertextVersion = CIPHERTEXT_VERSION}, {@code isLid = to.isLid()},
     * {@code retryCount}, {@code editType} defaulting to {@code NOT_EDITED},
     * {@code botType}, {@code e2eDestination}, {@code e2eReceiverDeviceType},
     * {@code encryptionType = COEX} when the device is hosted,
     * {@code e2eCiphertextType}, {@code messageMediaType}, and
     * {@code agentEngagementType} when the device is a bot. Cobalt omits the
     * optional fields that depend on WA-Web-only context (device record lookup
     * for {@code e2eReceiverDeviceType}, bot-specific context for
     * {@code botType}/{@code agentEngagementType}) and populates only the
     * fields that are resolvable from the information available at the Cobalt
     * encryption call site.
     */
    @WhatsAppWebExport(moduleName = "WAWebPostE2eMessageSendMetric",
            exports = {"postSuccessDirectE2eMessageSendMetric", "postFailureDirectE2eMessageSendMetric"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    void emitE2eMessageSendEvent(Jid device, MessageContainer container, boolean success,
                                 com.github.auties00.cobalt.message.MessageEncryptionType ciphertextType,
                                 int retryCount) {
        var builder = new E2eMessageSendEventBuilder()
                // WAWebPostE2eMessageSendMetric: e2eCiphertextVersion = CIPHERTEXT_VERSION
                .e2eCiphertextVersion(com.github.auties00.cobalt.message.send.crypto.MessageEncryption.CIPHERTEXT_VERSION)
                // WAWebPostE2eMessageSendMetric: isLid = to.isLid()
                .isLid(device.hasLidServer())
                // WAWebPostE2eMessageSendMetric: retryCount = n
                .retryCount(retryCount)
                // WAWebPostE2eMessageSendMetric: editType defaults to NOT_EDITED when undefined
                .editType(EditType.NOT_EDITED)
                // WAWebPostE2eMessageSendMetric: e2eDestination via getMetricE2eDestination;
                // per-device direct encryption always targets an INDIVIDUAL recipient
                .e2eDestination(E2eDestination.INDIVIDUAL)
                // WAWebPostE2eMessageSendMetric: flipped to !success on failure, weight=1
                .e2eSuccessful(success);
        // WAWebPostE2eMessageSendMetric: t.isHosted() && (s.encryptionType = COEX)
        if (device.hasServer(com.github.auties00.cobalt.model.jid.JidServer.hosted())) {
            builder.encryptionType(EncryptionTypeCode.COEX);
        }
        // WAWebPostE2eMessageSendMetric: r != null && (s.e2eCiphertextType = getMetricE2eCiphertextType(r))
        if (ciphertextType != null) {
            builder.e2eCiphertextType(mapCiphertextType(ciphertextType));
        }
        // WAWebPostE2eMessageSendMetric: a && (s.messageMediaType = getWamMediaType(a), ...)
        if (container != null) {
            var mediaType = mapMediaType(container);
            if (mediaType != null) {
                builder.messageMediaType(mediaType);
            }
            // WAWebPostE2eMessageSendMetric: t.isBot() → DIRECT_CHAT or INVOKED based on
            // a.id.remote.isBot(). Cobalt cannot resolve the remote key from a bare
            // container at this call site, so the INVOKED vs DIRECT_CHAT distinction
            // falls back to INVOKED when the recipient is a bot device.
            if (device.isBot()) {
                builder.agentEngagementType(AgentEngagementEnumType.INVOKED);
            }
        }
        client.wamService().commit(builder.build());
    }

    /**
     * Emits the {@code E2eMessageSendEvent} (id 476) for a sender-key (SKMSG)
     * group or status encryption attempt.
     *
     * <p>Mirrors {@code WAWebEncryptMsgProtobuf.encryptMsgSenderKey}: the event
     * is constructed with {@code e2eSuccessful=true} before the call, flipped
     * to {@code false} (with {@code weight=1}) on exception, and committed in
     * the {@code finally} block. Cobalt collapses the success and failure
     * paths into a single helper invoked after the SKMSG encryption completes.
     *
     * @param groupOrStatusJid  the SKMSG target JID (group or status broadcast)
     * @param container         the message container being encrypted
     * @param destination       the semantic destination
     *                          ({@link E2eDestination#GROUP} for groups,
     *                          {@link E2eDestination#STATUS} for status)
     * @param isLidAddressingMode whether the SKMSG fanout uses LID addressing
     * @param success           {@code true} for a successful encryption,
     *                          {@code false} for a failure
     *
     * @implNote WAWebEncryptMsgProtobuf.encryptMsgSenderKey: populates
     * {@code e2eSuccessful=true}, {@code e2eCiphertextType = SENDER_KEY_MESSAGE},
     * {@code e2eCiphertextVersion = CIPHERTEXT_VERSION},
     * {@code e2eDestination = GROUP}, {@code messageMediaType} from the message,
     * {@code retryCount=0}, {@code isLid} from {@code i.isLid || message.author?.isLid()},
     * {@code typeOfGroup} from {@code i.wamTypeOfGroup}, {@code editType}
     * from {@code getWamEditType(e)}, {@code localAddressingMode} from
     * {@code getAddressingModeMetricsFromGroupMetadata(i)}. On failure
     * flips {@code e2eSuccessful=false} and sets {@code weight=1}.
     * Cobalt keeps only the fields that can be resolved from the send-path
     * state; {@code typeOfGroup} requires group metadata that is not passed
     * into this helper and is omitted.
     */
    @WhatsAppWebExport(moduleName = "WAWebEncryptMsgProtobuf", exports = "encryptMsgSenderKey",
            adaptation = WhatsAppAdaptation.ADAPTED)
    void emitE2eMessageSendSenderKeyEvent(Jid groupOrStatusJid, MessageContainer container,
                                          E2eDestination destination, boolean isLidAddressingMode,
                                          boolean success) {
        var builder = new E2eMessageSendEventBuilder()
                .e2eSuccessful(success)
                // WAWebEncryptMsgProtobuf.encryptMsgSenderKey: e2eCiphertextType = SENDER_KEY_MESSAGE
                .e2eCiphertextType(E2eCiphertextType.SENDER_KEY_MESSAGE)
                // WAWebEncryptMsgProtobuf.encryptMsgSenderKey: e2eCiphertextVersion = CIPHERTEXT_VERSION
                .e2eCiphertextVersion(com.github.auties00.cobalt.message.send.crypto.MessageEncryption.CIPHERTEXT_VERSION)
                // WAWebEncryptMsgProtobuf.encryptMsgSenderKey: e2eDestination hard-coded to GROUP,
                // but the status fanout reuses the same encryption helper and maps to STATUS in Cobalt
                .e2eDestination(destination)
                // WAWebEncryptMsgProtobuf.encryptMsgSenderKey: retryCount=0 hard-coded
                .retryCount(0)
                // WAWebEncryptMsgProtobuf.encryptMsgSenderKey: isLid = !!i.isLid || message.author?.isLid()
                .isLid(isLidAddressingMode)
                // WAWebEncryptMsgProtobuf.encryptMsgSenderKey: editType = getWamEditType(message)
                .editType(mapEditType(container))
                // WAWebEncryptMsgProtobuf.encryptMsgSenderKey: localAddressingMode from group metadata
                .localAddressingMode(isLidAddressingMode ? AddressingMode.LID : AddressingMode.PN);
        if (container != null) {
            var mediaType = mapMediaType(container);
            if (mediaType != null) {
                builder.messageMediaType(mediaType);
            }
        }
        client.wamService().commit(builder.build());
    }

    /**
     * Maps a {@link com.github.auties00.cobalt.message.MessageEncryptionType}
     * to the corresponding WAM {@link E2eCiphertextType}.
     *
     * @param type the Signal ciphertext type
     * @return the matching WAM enum constant
     *
     * @implNote WAWebBackendJobsCommon.getMetricE2eCiphertextType:
     * maps {@code Skmsg→SENDER_KEY_MESSAGE}, {@code Pkmsg→PREKEY_MESSAGE},
     * {@code Msg→MESSAGE}, {@code Msmsg→MESSAGE_SECRET_MESSAGE}.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobsCommon", exports = "getMetricE2eCiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static E2eCiphertextType mapCiphertextType(com.github.auties00.cobalt.message.MessageEncryptionType type) {
        return switch (type) {
            case MSG -> E2eCiphertextType.MESSAGE;
            case PKMSG -> E2eCiphertextType.PREKEY_MESSAGE;
            case SKMSG -> E2eCiphertextType.SENDER_KEY_MESSAGE;
            case MSMSG -> E2eCiphertextType.MESSAGE_SECRET_MESSAGE;
        };
    }

    /**
     * Maps the content of a {@link MessageContainer} to the corresponding WAM
     * {@link MediaType} classification used by
     * {@code WAWebPostE2eMessageSendMetric}.
     *
     * @param container the outbound message container
     * @return the matching WAM media type, or {@code null} for messages that
     *         do not carry a classifiable media payload
     *
     * @implNote WAWebWamMsgUtils.getWamMediaType / WAWebBackendJobsCommon.getMetricMediaType:
     * walk the protobuf wrappers (deviceSent, ephemeral, viewOnce, etc.), then
     * map each leaf message type to the matching MEDIA_TYPE constant. Cobalt
     * pattern-matches on the sealed message types that already flow through
     * {@link #resolveMediaType}.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getWamMediaType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static MediaType mapMediaType(MessageContainer container) {
        var message = container.content();
        return switch (message) {
            case ImageMessage _ -> MediaType.PHOTO;
            case VideoMessage v -> v.gifPlayback() ? MediaType.GIF : MediaType.VIDEO;
            case AudioMessage a -> a.ptt() ? MediaType.PTT : MediaType.AUDIO;
            case DocumentMessage _ -> MediaType.DOCUMENT;
            case StickerMessage _ -> MediaType.STICKER;
            case LocationMessage l -> l.isLive() ? MediaType.LIVE_LOCATION : MediaType.LOCATION;
            case LiveLocationMessage _ -> MediaType.LIVE_LOCATION;
            case ContactMessage _ -> MediaType.CONTACT;
            case ContactsArrayMessage _ -> MediaType.CONTACT_ARRAY;
            case GroupInviteMessage _ -> MediaType.URL;
            case ExtendedTextMessage t when t.matchedText().isPresent() -> MediaType.URL;
            case ExtendedTextMessage _ -> MediaType.TEXT;
            case ReactionMessage _ -> MediaType.REACTION;
            case EncReactionMessage _ -> MediaType.REACTION;
            case PollCreationMessage _ -> MediaType.POLL_CREATE;
            case PollUpdateMessage _ -> MediaType.POLL_VOTE;
            case PollResultSnapshotMessage _ -> MediaType.POLL_RESULT_SNAPSHOT;
            case EventMessage _ -> MediaType.EVENT_CREATE;
            case EncEventResponseMessage _ -> MediaType.EVENT_RESPOND;
            case KeepInChatMessage _ -> MediaType.KEEP;
            default -> null;
        };
    }

    /**
     * Maps the content of a {@link MessageContainer} to the corresponding WAM
     * {@link EditType} classification.
     *
     * @param container the outbound message container
     * @return the matching WAM edit type ({@link EditType#NOT_EDITED} when the
     *         message is not an edit or revoke)
     *
     * @implNote WAWebMsgGetters.getWamEditType / WAWebBackendJobsCommon.getMetricEditTypeFromMsg:
     * return {@code SENDER_REVOKE} or {@code ADMIN_REVOKE} for revoke messages,
     * {@code EDITED} for message-edit messages, otherwise {@code NOT_EDITED}.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobsCommon", exports = "getMetricEditTypeFromMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static EditType mapEditType(MessageContainer container) {
        if (container == null) {
            return EditType.NOT_EDITED;
        }
        var message = container.content();
        if (message instanceof ProtocolMessage p) {
            var type = p.type().orElse(null);
            if (type == ProtocolMessage.Type.REVOKE) {
                return EditType.SENDER_REVOKE;
            }
            if (type == ProtocolMessage.Type.MESSAGE_EDIT) {
                return EditType.EDITED;
            }
        }
        return EditType.NOT_EDITED;
    }
}
