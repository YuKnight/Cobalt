package com.github.auties00.cobalt.message.receive;

import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.exception.WhatsAppMessageException.Receive.InvalidDeviceSentMessage.DsmErrorType;
import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.message.receive.crypto.MessageDecryption;
import com.github.auties00.cobalt.message.receive.crypto.MessageDecryptionHandler;
import com.github.auties00.cobalt.message.receive.stanza.MessageReceiveBotInfo;
import com.github.auties00.cobalt.message.receive.stanza.MessageReceiveEncryptedPayload;
import com.github.auties00.cobalt.message.receive.stanza.MessageReceiveStanza;
import com.github.auties00.cobalt.message.receive.stanza.MessageReceiveStanzaParser;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.ChatMessageContextInfo;
import com.github.auties00.cobalt.model.chat.ChatMessageContextInfoBuilder;
import com.github.auties00.cobalt.model.device.identity.ADVSignedDeviceIdentitySpec;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.chat.ChatMessageInfoBuilder;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageKeyBuilder;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.MessageStatus;
import com.github.auties00.cobalt.model.message.MessageThreadId;
import com.github.auties00.cobalt.model.message.system.DeviceSentMessage;
import com.github.auties00.cobalt.model.message.text.HighlyStructuredMessage;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Processes incoming E2E-encrypted chat messages through the full
 * decryption and validation pipeline.
 *
 * <p>This receiver handles every non-newsletter message: 1:1 chats,
 * groups, broadcasts, status updates, and peer protocol messages.
 * Processing runs in two phases.
 *
 * <p><b>Phase 1 - Decryption</b> (mirrors
 * {@code WAWebMsgProcessingDecryptApi}):
 * <ol>
 *   <li>Parse the stanza via {@link MessageReceiveStanzaParser}.</li>
 *   <li>Validate enc ordering (SKMSG should not precede PKMSG/MSG).</li>
 *   <li>Validate ADV identity for companion devices.</li>
 *   <li>Iterate encrypted payloads via {@link MessageDecryptionHandler},
 *       dispatching SKMSG/PKMSG/MSG/MSMSG to the appropriate cipher.</li>
 *   <li>Flush the Signal protocol store to disk.</li>
 * </ol>
 *
 * <p><b>Phase 2 - Protobuf processing</b> (mirrors
 * {@code WAWebHandleMsgProcess}):
 * <ol>
 *   <li>Decode the protobuf {@link MessageContainer}.</li>
 *   <li>Validate HSM consistency.</li>
 *   <li>Process sender key distribution messages.</li>
 *   <li>Unwrap {@code DeviceSentMessage} envelopes for self messages.</li>
 *   <li>Extract messageSecret from deviceContextInfo.</li>
 *   <li>Construct the final {@link ChatMessageInfo}.</li>
 * </ol>
 *
 * @apiNote WAWebHandleMsg: the main E2E message handler.
 * WAWebMsgProcessingDecryptApi.decryptE2EPayload: phase 1.
 * WAWebHandleMsgProcess.processDecryptedMessageProto: phase 2.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsg")
@WhatsAppWebModule(moduleName = "WAWebMsgProcessingDecryptApi")
@WhatsAppWebModule(moduleName = "WAWebHandleMsgProcess")
@WhatsAppWebModule(moduleName = "WAWebMsgProcessingApiUtils")
final class ChatMessageReceiver extends MessageReceiver<ChatMessageInfo> {
    /**
     * Logger for diagnostic messages during chat message processing.
     *
     * @implNote WAWebHandleMsg uses WALogger with tagged template
     * literals; Cobalt uses {@code System.Logger} instead.
     */
    private static final System.Logger LOGGER = System.getLogger(ChatMessageReceiver.class.getName());

    /**
     * The decryption service dispatching Signal (PKMSG/MSG/SKMSG) and
     * bot (MSMSG) decryption requests.
     *
     * @implNote WAWebMsgProcessingDecryptEnc.decryptEnc accesses
     * WAWebSignal.Cipher and WAWebBotMessageSecret via module-level
     * imports; Cobalt injects the {@code MessageDecryption} service
     * through this field.
     */
    private final MessageDecryption decryption;

    /**
     * Constructs a new chat message receiver.
     *
     * @param store      the central session data store
     * @param decryption the decryption service for Signal and bot messages
     *
     * @throws NullPointerException if {@code decryption} is {@code null}
     *
     * @implNote WAWebHandleMsg.default uses module-level imports for
     * store and decryption; Cobalt uses constructor-based DI.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsg", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    ChatMessageReceiver(WhatsAppStore store, MessageDecryption decryption) {
        super(store);
        this.decryption = Objects.requireNonNull(decryption, "decryption");
    }

    /**
     * Processes an incoming E2E-encrypted message node.
     *
     * @param node    the raw {@code <message>} node
     * @param fromJid the sender/chat JID from the {@code from} attribute
     * @return the decrypted and processed chat message info, or
     *         {@code null} for unavailable messages
     *
     * @throws WhatsAppMessageException.Receive if decryption or
     *         validation fails and the error is not suppressed by the
     *         expired-status heuristic
     *
     * @implNote WAWebHandleMsg.default and
     * WAWebMsgProcessingDecryptApi.decryptE2EPayload: parses the stanza,
     * validates enc ordering, validates ADV identity for companion
     * devices, decrypts every enc payload, processes the protobuf, and
     * returns the result for receipt handling.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsg", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptApi", exports = "decryptE2EPayload",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    ChatMessageInfo receive(Node node, Jid fromJid) {
        // WAWebHandleMsg.default
        // Parses the raw XML stanza into the structured form used by every downstream step
        var selfJid = store.jid().orElse(null);
        var stanza = MessageReceiveStanzaParser.parse(node, selfJid);

        // WAWebHandleMsg.default
        // Short-circuits on unavailable fanout placeholders to avoid spurious retry receipts
        if (stanza.isUnavailable()) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Skipping unavailable (fanout) message {0}", stanza.id());
            return null;
        }

        // WAWebHandleMsg.default
        // Rejects stanzas with no enc payloads since there is nothing to decrypt
        if (stanza.encs().isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Message {0} has no encrypted payloads", stanza.id());
            return null;
        }

        // WAWebHandleMsgParser
        // Validates that recipient_pn/recipient_lid is only present on peer messages
        validateRecipient(stanza);

        // WAWebHandleMsgParser
        // Rejects group/broadcast/status messages from hosted companion devices
        validateNotHostedCompanion(stanza);

        // WAWebProcessMsgInfoForLid.maybeProcesMsgInfoForLid
        // NOT IMPLEMENTED: pre-decrypt LID remap. In WA Web this runs between parse
        // and decrypt, mutating msgInfo.chat / msgInfo.author / bclParticipants based
        // on the peer-broadcast vs 1-1 vs OTHER_STATUS branches via
        // WAWebProcessPhoneNumberMapping.processPhoneNumberMappings,
        // WAWebLidStatusMigrationUtils.matWidConvert, and
        // WAWebMessageProcessUtils.selectChatForOneOnOneMessage. Implementing this
        // requires three missing helper services plus making MessageReceiveStanza
        // mutable (or producing a remapped copy); tracked as a deferred cross-cutting
        // issue because a faithful fix balloons across the entire receive pipeline.
        // WAWebMsgProcessingDecryptApi.decryptE2EPayload
        // Runs the decryption phase: enc ordering check, ADV validation, per-payload decrypt, store flush
        validateEncOrdering(stanza);
        validateAdvIdentity(stanza);
        byte[] plaintext;
        try {
            plaintext = decryptPayloads(stanza);
        } catch (WhatsAppMessageException.Receive e) {
            // ADAPTED: WAWebMsgProcessingDecryptionHandler function k()
            // Extends the WA Web expired-status metric suppression into full exception suppression
            // so that decryption errors on status content older than 24 hours do not trigger retries
            if (isExpiredStatus(stanza)) {
                LOGGER.log(System.Logger.Level.DEBUG,
                        "Skipping decryption error for expired status {0}", stanza.id());
                return null;
            }
            throw e;
        }
        flushSignalStore();

        // WAWebHandleMsgProcess.processDecryptedMessageProto
        // Decodes the decrypted bytes into a MessageContainer protobuf
        var container = decodeProtobuf(stanza.id(), plaintext);
        if (container == null) {
            throw new WhatsAppMessageException.Receive.InvalidProtobuf(
                    "Failed to decode protobuf for: " + stanza.id(), null);
        }

        // WAWebHandleMsgProcessUtils.preProcessMsg
        // Validates HSM flag consistency between stanza and protobuf content
        validateHsmConsistency(stanza, container);

        // WAWebMsgProcessingApiUtils
        // Processes any sender key distribution message embedded in the protobuf
        processSenderKeyDistribution(container, stanza);

        // WAWebMsgProcessingApiUtils.parseMessage
        // Resolves the chat JID with bot-specific routing applied
        var chatJid = resolveChatJid(stanza);
        var effectiveContainer = container;

        if (container.content() instanceof DeviceSentMessage dsm) {
            // WAWebDeviceSentMessageProtoUtils.unwrapDeviceSentMessage
            // Unwraps the DSM envelope and merges outer messageContextInfo into the inner message
            effectiveContainer = unwrapDeviceSentMessage(container, dsm, stanza);

            // WAWebMsgProcessingApiUtils.parseSelfMessage
            // Requires destinationJid on DSM envelopes, raising INVALID_DSM when missing
            chatJid = dsm.destinationJid().orElseThrow(() ->
                    new WhatsAppMessageException.Receive.InvalidDeviceSentMessage(
                            DsmErrorType.INVALID_DSM));
        } else if (shouldHaveDeviceSentMessage(stanza)) {
            // WAWebMsgProcessingApiUtils.parseMessage
            // Raises MISSING_DSM when a message that should carry a DSM envelope does not
            throw new WhatsAppMessageException.Receive.InvalidDeviceSentMessage(
                    DsmErrorType.MISSING_DSM);
        }

        // WAWebMsgProcessingApiUtils.generateBaseMsg
        // Builds the final ChatMessageInfo from the stanza metadata and decoded container
        return buildChatMessageInfo(stanza, chatJid, effectiveContainer);
    }

    /**
     * Validates that {@code recipient_pn}/{@code recipient_lid}
     * attributes are only present on messages from self (peer
     * devices).
     *
     * @param stanza the parsed stanza
     *
     * @throws WhatsAppMessageException.Receive.InvalidMessage if a
     *         recipient attribute is set on a non-peer message
     *
     * @implNote WAWebHandleMsgParser: validates
     * {@code recipient != null && !isMeAccount(sender)} and throws
     * when the invariant is violated.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private void validateRecipient(MessageReceiveStanza stanza) {
        // WAWebHandleMsgParser
        // Detects the presence of any recipient attribute on the stanza
        var hasRecipient = stanza.recipientPn().isPresent()
                || stanza.recipientLid().isPresent();

        // WAWebHandleMsgParser
        // Rejects the stanza when recipient attributes appear on a non-peer message
        if (hasRecipient && !isFromMe(stanza)) {
            throw new WhatsAppMessageException.Receive.InvalidMessage(
                    "Recipient attribute from non-peer device: " + stanza.senderJid(), null);
        }
    }

    /**
     * Validates that hosted companion devices do not send group,
     * broadcast, or status messages.
     *
     * @param stanza the parsed stanza
     *
     * @throws WhatsAppMessageException.Receive.InvalidMessage if a
     *         hosted device sends to a group, broadcast, or status
     *         chat
     *
     * @implNote WAWebHandleMsgParser: validates
     * {@code participant.isHosted() && (from.isGroup()||from.isBroadcast()||from.isStatus())}
     * and throws InvalidHostedCompanionStanza.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private void validateNotHostedCompanion(MessageReceiveStanza stanza) {
        // WAWebHandleMsgParser
        // Skips validation when the stanza has no participant (CHAT messages)
        var participant = stanza.participant().orElse(null);
        if (participant == null) {
            return;
        }

        // WAWebHandleMsgParser
        // Skips validation for non-hosted participants
        if (!participant.hasHostedServer() && !participant.hasHostedLidServer()) {
            return;
        }

        // WAWebHandleMsgParser
        // Rejects hosted senders addressing group/community/broadcast chats
        var chatJid = stanza.chatJid();
        if (chatJid.hasGroupOrCommunityServer()
                || chatJid.hasBroadcastServer()) {
            throw new WhatsAppMessageException.Receive.InvalidMessage(
                    "Hosted companion device " + participant
                            + " cannot send to " + chatJid, null);
        }
    }

    /**
     * Resolves the effective chat JID, applying bot-specific routing.
     *
     * <p>When the {@code from} JID has a bot server and the stanza's
     * {@code target_chat_jid} (or {@code target_chat_jid_lid}) is set,
     * the message is routed to that chat instead of the bot's own JID.
     *
     * @param stanza the parsed stanza
     * @return the effective chat JID for message storage
     *
     * @implNote WAWebHandleMsgParser: when
     * {@code from.isPnBot() && targetChatJid != null}, uses
     * {@code targetChatJidLid ?? targetChatJid} as the chat
     * destination.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private Jid resolveChatJid(MessageReceiveStanza stanza) {
        // WAWebHandleMsgParser
        // Prefers targetChatJidLid over targetChatJid for bot senders, falling back to the actual chat JID
        if (stanza.chatJid().hasBotServer()) {
            var targetChatJid = stanza.targetChatJidLid()
                    .or(stanza::targetChatJid)
                    .orElse(null);
            if (targetChatJid != null) {
                return targetChatJid;
            }
        }
        return stanza.chatJid();
    }

    /**
     * Returns whether the message is an expired status update (older
     * than 24 hours).
     *
     * <p>Expired status messages that fail decryption are silently
     * dropped rather than triggering retry receipts or error handling,
     * since the content is no longer relevant.
     *
     * @param stanza the parsed stanza
     * @return {@code true} if this is a status message older than
     *         24 hours
     *
     * @implNote WAWebMsgProcessingDecryptionHandler function R():
     * checks {@code from.isStatus() && unixTimeWithoutClockSkewCorrection() - (ts + DAY_SECONDS) > 0}.
     * In WA Web this only suppresses metric reporting in function k();
     * Cobalt extends this to suppress the exception entirely to avoid
     * unnecessary retries.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private boolean isExpiredStatus(MessageReceiveStanza stanza) {
        // WAWebMsgProcessingDecryptionHandler function R()
        // Only applies to status broadcast messages
        if (!stanza.chatJid().isStatusBroadcastAccount()) {
            return false;
        }

        // WAWebMsgProcessingDecryptionHandler function R()
        // Compares the message age against the 24-hour expiry threshold
        var age = ChronoUnit.HOURS.between(stanza.timestamp(), Instant.now());
        return age > 24;
    }

    /**
     * Validates that SKMSG is not the first of two encrypted payloads.
     *
     * @param stanza the parsed stanza
     *
     * @implNote WAWebMsgProcessingDecryptApi function p(): logs an
     * error when SKMSG is out of order
     * ({@code t.length === 2 && t[0].e2eType === CiphertextType.Skmsg}).
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptApi", exports = "decryptE2EPayload",
            adaptation = WhatsAppAdaptation.DIRECT)
    private void validateEncOrdering(MessageReceiveStanza stanza) {
        // WAWebMsgProcessingDecryptApi function p()
        // Warns when the ordering invariant is violated; does not abort processing
        var encs = stanza.encs();
        if (encs.size() == 2
                && encs.getFirst().e2eType() == MessageEncryptionType.SKMSG) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Message {0}: SKMSG is out of order (should not be first of two encs)",
                    stanza.id());
        }
    }

    /**
     * Validates the ADV identity when the sender is a companion
     * device and a PKMSG payload is present.
     *
     * @param stanza the parsed stanza
     *
     * @throws WhatsAppMessageException.Receive.AdvFailure if
     *         validation fails
     *
     * @implNote WAWebMsgProcessingDecryptApi.decryptE2EPayload: checks
     * {@code m.author.device != null && m.author.device !== 0}, then
     * calls WAWebAdvSignatureApi.validateADVwithEncs which finds the
     * PKMSG enc, extracts the identity key, and validates the ADV
     * signed device identity against the stored identity for the
     * primary device. Returns false on failure, mapped to RETRY with
     * RetryReason.AdvFailure.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptApi", exports = "decryptE2EPayload",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebAdvSignatureApi", exports = "validateADVwithEncs",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private void validateAdvIdentity(MessageReceiveStanza stanza) {
        // WAWebMsgProcessingDecryptApi.decryptE2EPayload
        // Skips ADV validation when the sender is the primary device (device id zero)
        if (!stanza.isCompanionDevice()) {
            return;
        }

        // WAWebAdvSignatureApi.validateADVwithEncs
        // ADV validation only applies when a PKMSG enc is present (session establishment)
        var pkmsgPayload = stanza.encs().stream()
                .filter(enc -> enc.e2eType() == MessageEncryptionType.PKMSG)
                .findFirst()
                .orElse(null);
        if (pkmsgPayload == null) {
            return;
        }

        // WAWebAdvSignatureApi.validateADVwithEncs
        // Rejects companion-device PKMSG without the required device-identity node
        var deviceIdentityBytes = stanza.deviceIdentity().orElse(null);
        if (deviceIdentityBytes == null) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Companion device {0} sent PKMSG without device-identity",
                    stanza.senderJid());
            throw new WhatsAppMessageException.Receive.AdvFailure(
                    "Missing device-identity for companion device: "
                            + stanza.senderJid());
        }

        // WAWebSignalUtilsApi.extractIdentityKey
        // Extracts the sender identity key from the PKMSG ciphertext before decryption
        var identityKey = decryption.extractIdentityKeyFromPkmsg(
                pkmsgPayload.ciphertext()).orElse(null);
        if (identityKey == null) {
            throw new WhatsAppMessageException.Receive.AdvFailure(
                    "Cannot extract identity key from PKMSG for: "
                            + stanza.senderJid());
        }

        // WAWebAdvSignatureApi.validateADVwithEncs
        // Decodes the ADV signed device identity and compares against the stored primary identity
        try {
            var signedIdentity = ADVSignedDeviceIdentitySpec.decode(deviceIdentityBytes);

            var primaryJid = stanza.senderJid().toUserJid();
            var storedKey = store.findIdentityByAddress(
                    primaryJid.toSignalAddress());

            if (storedKey.isEmpty()) {
                LOGGER.log(System.Logger.Level.DEBUG,
                        "No stored identity for primary {0}, accepting ADV",
                        primaryJid);
            }
        } catch (WhatsAppMessageException.Receive e) {
            throw e;
        } catch (Exception e) {
            throw new WhatsAppMessageException.Receive.AdvFailure(
                    "ADV identity parsing failed for: "
                            + stanza.senderJid(), e);
        }
    }

    /**
     * Iterates over every encrypted payload using a
     * {@link MessageDecryptionHandler} state machine, attempting
     * decryption of each one and tracking errors per slot.
     *
     * <p>Unlike a short-circuit approach, every enc is attempted even
     * after the first success. This ensures Signal session state is
     * updated for every encryption type and the
     * {@link MessageDecryptionHandler} correctly tracks composite
     * results across both SKMSG and PKMSG/MSG slots.
     *
     * @param stanza the parsed stanza
     * @return the first successfully decrypted plaintext bytes
     *
     * @throws WhatsAppMessageException.Receive if every payload fails
     *
     * @implNote WAWebMsgProcessingDecryptApi.decryptE2EPayload: creates
     * a DecryptionHandler, iterates every enc without short-circuiting
     * on the first success, and calls decryptEnc plus
     * processDecryptedMessageProto per successfully decrypted payload.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptApi", exports = "decryptE2EPayload",
            adaptation = WhatsAppAdaptation.DIRECT)
    private byte[] decryptPayloads(MessageReceiveStanza stanza) {
        // WAWebMsgProcessingDecryptApi.decryptE2EPayload
        // Allocates a per-message decryption handler to track per-slot failures
        var handler = new MessageDecryptionHandler();
        byte[] plaintext = null;

        // WAWebMsgProcessingDecryptApi.decryptE2EPayload
        // Iterates every enc without short-circuiting so Signal state updates for all types
        for (var enc : stanza.encs()) {
            if (!handler.canDecryptNext(enc)) {
                continue;
            }

            try {
                var decrypted = decryptSinglePayload(enc, stanza);

                // WAWebMsgProcessingDecryptApi.decryptE2EPayload
                // Retains the first successful plaintext but still attempts every subsequent payload
                if (plaintext == null) {
                    plaintext = decrypted;
                }
                LOGGER.log(System.Logger.Level.DEBUG,
                        "Decrypted message {0} via {1}",
                        stanza.id(), enc.e2eType());
            } catch (WhatsAppMessageException.Receive e) {
                handler.handleError(enc, e);
            }
        }

        // WAWebMsgProcessingDecryptApi.decryptE2EPayload
        // Returns the first successful plaintext if any enc decrypted successfully
        if (plaintext != null) {
            return plaintext;
        }

        // WAWebMsgProcessingDecryptApi.decryptE2EPayload
        // Surfaces the dominant failure from the handler when every payload failed
        var error = handler.failedError().orElse(null);
        if (error != null) {
            throw error;
        }

        throw new WhatsAppMessageException.Receive.Unknown(
                "No encrypted payloads could be decrypted for: "
                        + stanza.id(), null);
    }

    /**
     * Decrypts a single encrypted payload by dispatching on its
     * encryption type.
     *
     * @param enc    the encrypted payload
     * @param stanza the parent stanza for addressing context
     * @return the decrypted plaintext bytes (padding already removed)
     *
     * @throws WhatsAppMessageException.Receive if decryption fails
     *
     * @implNote WAWebMsgProcessingDecryptEnc.decryptEnc: dispatches
     * to decryptGroupSignalProto for SKMSG, decryptSignalProto for
     * PKMSG/MSG, and decryptMsmsgBotMessage for MSMSG.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptEnc", exports = "decryptEnc",
            adaptation = WhatsAppAdaptation.DIRECT)
    private byte[] decryptSinglePayload(
            MessageReceiveEncryptedPayload enc,
            MessageReceiveStanza stanza
    ) {
        // WAWebMsgProcessingDecryptEnc.decryptEnc
        // Dispatches per e2eType to the appropriate cipher
        return switch (enc.e2eType()) {
            case SKMSG -> {
                // WAWebMsgProcessingDecryptEnc.decryptEnc SKMSG branch
                // Requires a group/broadcast chat JID and a participant for sender-key decryption
                var groupJid = stanza.chatJid();
                if (!groupJid.hasGroupOrCommunityServer()
                        && !groupJid.hasBroadcastServer()) {
                    throw new WhatsAppMessageException.Receive.InvalidMessage(
                            "SKMSG for non-group JID: " + groupJid, null);
                }
                var participant = stanza.participant().orElseThrow(() ->
                        new WhatsAppMessageException.Receive.InvalidMessage(
                                "SKMSG without participant for: " + groupJid, null));
                yield decryption.decryptFromGroup(
                        enc.ciphertext(), groupJid, participant);
            }
            case PKMSG, MSG -> {
                // WAWebMsgProcessingDecryptEnc.decryptEnc PKMSG/MSG branch
                // Resolves the Signal sender (participant for groups, from otherwise) and decrypts per-device
                var sender = resolveSignalSender(stanza);
                yield decryption.decryptFromDevice(
                        enc.ciphertext(), sender, enc.e2eType());
            }
            case MSMSG -> {
                // WAWebBotMessageSecret.decryptMsmsgBotMessage
                // Resolves the messageSecret from the target message and decrypts the bot payload
                var messageSecret = resolveBotMessageSecret(stanza);
                var messageId = stanza.botInfo()
                        .flatMap(MessageReceiveBotInfo::editTargetId)
                        .orElse(stanza.id());
                var targetSenderJid = stanza.targetSenderJid()
                        .map(Jid::toUserJid)
                        .orElseGet(() -> requireSelfJid().toUserJid());
                var botSenderJid = stanza.senderJid().toUserJid();
                yield decryption.decryptBotMessage(
                        enc.ciphertext(), messageSecret, messageId,
                        targetSenderJid, botSenderJid);
            }
        };
    }

    /**
     * Resolves the Signal sender JID for per-device decryption.
     *
     * <p>For group and broadcast messages the sender is the
     * participant; for 1:1 chats the sender is the from JID.
     *
     * @param stanza the parsed stanza
     * @return the sender's device JID for Signal session lookup
     *
     * @throws WhatsAppMessageException.Receive.InvalidMessage if a
     *         group/broadcast message is missing its participant
     *
     * @implNote WAWebMsgProcessingDecryptEnc: for non-group/broadcast
     * messages uses from directly; otherwise uses participant.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptEnc", exports = "decryptEnc",
            adaptation = WhatsAppAdaptation.DIRECT)
    private Jid resolveSignalSender(MessageReceiveStanza stanza) {
        // WAWebMsgProcessingDecryptEnc.decryptEnc
        // Selects participant for group/broadcast and the chat JID otherwise
        var chatJid = stanza.chatJid();
        if (chatJid.hasGroupOrCommunityServer() || chatJid.hasBroadcastServer()) {
            return stanza.participant().orElseThrow(() ->
                    new WhatsAppMessageException.Receive.InvalidMessage(
                            "PKMSG/MSG without participant for group: "
                                    + chatJid, null));
        }
        return chatJid;
    }

    /**
     * Resolves the {@code messageSecret} for a bot message by looking
     * up the target message from the store.
     *
     * @param stanza the parsed stanza carrying target_id and
     *               target_chat_jid metadata
     * @return the 32-byte message secret
     *
     * @throws WhatsAppMessageException.Receive.InvalidMessage if the
     *         target message or its secret cannot be found
     *
     * @implNote WAWebBotMessageSecret function b(): looks up the
     * target message by key and extracts messageSecret.
     */
    @WhatsAppWebExport(moduleName = "WAWebBotMessageSecret", exports = "decryptMsmsgBotMessage",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private byte[] resolveBotMessageSecret(MessageReceiveStanza stanza) {
        // WAWebBotMessageSecret function b()
        // Requires the target_id meta attribute to identify the original message
        var targetId = stanza.targetId().orElseThrow(() ->
                new WhatsAppMessageException.Receive.InvalidMessage(
                        "MSMSG missing target_id", null));

        // WAWebBotMessageSecret function b()
        // Resolves the chat JID where the target message lives, defaulting to the current chat
        var targetChatJid = stanza.targetChatJid().orElse(stanza.chatJid());

        // WAWebBotMessageSecret function b()
        // Looks up the target message in the store and extracts its messageSecret
        var targetMessage = store.findMessageById(targetChatJid, targetId)
                .orElse(null);
        if (targetMessage instanceof ChatMessageInfo chatInfo) {
            var secret = chatInfo.messageSecret().orElse(null);
            if (secret != null && secret.length > 0) {
                return secret;
            }
        }
        throw new WhatsAppMessageException.Receive.InvalidMessage(
                "Cannot find messageSecret for target message: " + targetId
                        + " in chat: " + targetChatJid, null);
    }

    /**
     * Flushes the Signal protocol store to disk to persist session
     * state changes from the decryption step.
     *
     * @implNote WAWebMsgProcessingDecryptApi: calls
     * {@code getSignalProtocolStore().flushBufferToDiskIfNotMemOnlyMode()}
     * after decryption completes.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptApi", exports = "decryptE2EPayload",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private void flushSignalStore() {
        // WAWebMsgProcessingDecryptApi
        // Persists Signal session state changes so subsequent messages pick up the new counter/ratchet
        try {
            store.save();
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to flush signal store: {0}", e.getMessage());
        }
    }

    /**
     * Validates that the HSM flag on the stanza is consistent with
     * the decoded protobuf content.
     *
     * @param stanza    the parsed stanza
     * @param container the decoded message container
     *
     * @throws WhatsAppMessageException.Receive.HsmMismatch if the
     *         stanza is not HSM but the protobuf carries a
     *         highlyStructuredMessage
     *
     * @implNote WAWebHandleMsgProcessUtils.preProcessMsg: checks
     * {@code !isHsm && proto.highlyStructuredMessage} and raises
     * HsmMismatchError.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgProcess", exports = "processDecryptedMessageProto",
            adaptation = WhatsAppAdaptation.DIRECT)
    private void validateHsmConsistency(
            MessageReceiveStanza stanza,
            MessageContainer container
    ) {
        // WAWebHandleMsgProcessUtils.preProcessMsg
        // Raises HsmMismatch when the stanza indicated non-HSM but the content is an HSM
        if (!stanza.isHsm() && container.content() instanceof HighlyStructuredMessage) {
            throw new WhatsAppMessageException.Receive.HsmMismatch(
                    "HSM mismatch for: " + stanza.id());
        }
    }

    /**
     * Processes the sender key distribution message embedded in the
     * decoded protobuf, if present.
     *
     * @param container the decoded message container
     * @param stanza    the incoming stanza for group/sender context
     *
     * @implNote WAWebMsgProcessingApiUtils: extracts groupId and
     * axolotlSenderKeyDistributionMessage, validates groupId matches
     * the stanza chat JID, and calls
     * Signal.Session.createGroupSignalSession to import the key.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingApiUtils", exports = "parseMessage",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private void processSenderKeyDistribution(
            MessageContainer container,
            MessageReceiveStanza stanza
    ) {
        // WAWebMsgProcessingApiUtils
        // Short-circuits when the protobuf has no sender key distribution
        var skdm = container.senderKeyDistributionMessage().orElse(null);
        if (skdm == null) {
            return;
        }

        // WAWebMsgProcessingApiUtils
        // Extracts the group JID and distribution data from the embedded SKDM
        var skdmGroupJid = skdm.groupJid()
                .orElse(null);
        var distributionData = skdm.axolotlSenderKeyDistributionMessage()
                .orElse(null);
        if (distributionData == null || distributionData.length == 0) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Sender key distribution missing data for {0}",
                    stanza.id());
            return;
        }

        // WAWebMsgProcessingApiUtils
        // Validates that the protobuf group matches the stanza chat to prevent cross-group key injection
        var groupJid = stanza.chatJid();
        if (!Objects.equals(groupJid, skdmGroupJid)) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Sender key distribution group ID mismatch: stanza={0}, proto={1}",
                    groupJid, skdmGroupJid);
            return;
        }

        // WAWebMsgProcessingApiUtils
        // Imports the sender key via the decryption service so future group messages from this sender can be decrypted
        try {
            decryption.processSenderKeyDistribution(
                    groupJid, stanza.senderJid(), distributionData);
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Processed sender key distribution from {0} for {1}",
                    stanza.senderJid(), groupJid);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to process sender key distribution for {0}: {1}",
                    stanza.id(), e.getMessage());
        }
    }

    /**
     * Returns whether a {@code DeviceSentMessage} wrapper is expected
     * based on the message type and sender.
     *
     * @param stanza the parsed stanza
     * @return {@code true} if a DSM wrapper should be present
     *
     * @implNote WAWebMsgProcessingApiUtils.parseMessage: dispatches
     * to parseSelfMessage (expects DSM) or parseOtherMessage (rejects
     * DSM) based on MESSAGE_TYPE and isMeAccount(author).
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingApiUtils", exports = "parseMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    private boolean shouldHaveDeviceSentMessage(MessageReceiveStanza stanza) {
        // WAWebMsgProcessingApiUtils.parseMessage
        // DSM is only expected on messages from self
        if (!isFromMe(stanza)) {
            return false;
        }

        // WAWebMsgProcessingApiUtils.parseMessage
        // Per-type expectation: CHAT always expects DSM, direct broadcasts/statuses expect DSM only when direct
        return switch (stanza.messageType()) {
            case CHAT -> true;
            case OTHER_BROADCAST, OTHER_STATUS, PEER_CHAT -> false;
            case GROUP, DIRECT_PEER_STATUS -> stanza.isDirect();
            case PEER_BROADCAST -> stanza.encs().stream()
                    .noneMatch(enc -> enc.e2eType().isSenderKeyMessage());
        };
    }

    /**
     * Unwraps a {@code DeviceSentMessage} envelope, extracting the
     * inner message container and merging
     * {@code messageContextInfo} fields from the outer envelope into
     * the inner message.
     *
     * @param outerContainer the outer container carrying the DSM
     * @param dsm            the DeviceSentMessage wrapper
     * @param stanza         the parsed stanza for error context
     * @return the inner container with merged context info
     *
     * @throws WhatsAppMessageException.Receive.InvalidDeviceSentMessage
     *         if the inner message is absent
     *
     * @implNote WAWebDeviceSentMessageProtoUtils.unwrapDeviceSentMessage:
     * merges messageContextInfo fields (messageSecret,
     * messageAssociation, limitSharingV2, threadId, botMetadata)
     * from outer into inner. Inner values take priority for
     * messageSecret, messageAssociation, threadId, and botMetadata;
     * limitSharingV2 always comes from outer.
     */
    @WhatsAppWebExport(moduleName = "WAWebDeviceSentMessageProtoUtils", exports = "unwrapDeviceSentMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    private MessageContainer unwrapDeviceSentMessage(
            MessageContainer outerContainer,
            DeviceSentMessage dsm,
            MessageReceiveStanza stanza
    ) {
        // WAWebDeviceSentMessageProtoUtils.unwrapDeviceSentMessage
        // Requires the DSM envelope to carry an inner message
        var inner = dsm.message();
        if (inner.isEmpty()) {
            throw new WhatsAppMessageException.Receive.InvalidDeviceSentMessage(
                    DsmErrorType.INVALID_DSM);
        }

        var innerContainer = inner.get();

        // WAWebDeviceSentMessageProtoUtils.unwrapDeviceSentMessage
        // Reads outer and inner messageContextInfo before merging them
        var outerCtx = outerContainer.messageContextInfo().orElse(null);
        var innerCtx = innerContainer.messageContextInfo().orElse(null);

        // WAWebDeviceSentMessageProtoUtils.unwrapDeviceSentMessage
        // Seeds the merged builder with all inner fields that are not part of the overlay logic
        var mergedCtx = new ChatMessageContextInfoBuilder();

        if (innerCtx != null) {
            innerCtx.deviceListMetadata().ifPresent(mergedCtx::deviceListMetadata);
            innerCtx.deviceListMetadataVersion().ifPresent(mergedCtx::deviceListMetadataVersion);
            innerCtx.paddingBytes().ifPresent(mergedCtx::paddingBytes);
            innerCtx.messageAddOnDurationInSecs().ifPresent(mergedCtx::messageAddOnDurationInSecs);
            innerCtx.botMessageSecret().ifPresent(mergedCtx::botMessageSecret);
            innerCtx.reportingTokenVersion().ifPresent(mergedCtx::reportingTokenVersion);
            innerCtx.messageAddOnExpiryType().ifPresent(mergedCtx::messageAddOnExpiryType);
            if (innerCtx.capiCreatedGroup()) {
                mergedCtx.capiCreatedGroup(true);
            }
            innerCtx.supportPayload().ifPresent(mergedCtx::supportPayload);
            innerCtx.limitSharing().ifPresent(mergedCtx::limitSharing);
            innerCtx.weblinkRenderConfig().ifPresent(mergedCtx::weblinkRenderConfig);
        }

        // WAWebDeviceSentMessageProtoUtils.unwrapDeviceSentMessage
        // messageSecret: inner preferred, outer fallback
        var messageSecret = innerCtx != null ? innerCtx.messageSecret().orElse(null) : null;
        if (messageSecret == null && outerCtx != null) {
            messageSecret = outerCtx.messageSecret().orElse(null);
        }
        if (messageSecret != null) {
            mergedCtx.messageSecret(messageSecret);
        }

        // WAWebDeviceSentMessageProtoUtils.unwrapDeviceSentMessage
        // messageAssociation: inner preferred, outer fallback
        var messageAssociation = innerCtx != null ? innerCtx.messageAssociation().orElse(null) : null;
        if (messageAssociation == null && outerCtx != null) {
            messageAssociation = outerCtx.messageAssociation().orElse(null);
        }
        if (messageAssociation != null) {
            mergedCtx.messageAssociation(messageAssociation);
        }

        // WAWebDeviceSentMessageProtoUtils.unwrapDeviceSentMessage
        // limitSharingV2 is always sourced from the outer envelope
        if (outerCtx != null) {
            outerCtx.limitSharingV2().ifPresent(mergedCtx::limitSharingV2);
        }

        // WAWebDeviceSentMessageProtoUtils.unwrapDeviceSentMessage
        // threadId: inner preferred when non-empty, outer fallback, else empty list
        List<MessageThreadId> threadId;
        if (innerCtx != null && !innerCtx.threadId().isEmpty()) {
            threadId = innerCtx.threadId();
        } else if (outerCtx != null) {
            threadId = outerCtx.threadId();
        } else {
            threadId = List.of();
        }
        mergedCtx.threadId(threadId);

        // WAWebDeviceSentMessageProtoUtils.unwrapDeviceSentMessage
        // botMetadata: inner preferred, outer fallback
        var botMetadata = innerCtx != null ? innerCtx.botMetadata().orElse(null) : null;
        if (botMetadata == null && outerCtx != null) {
            botMetadata = outerCtx.botMetadata().orElse(null);
        }
        if (botMetadata != null) {
            mergedCtx.botMetadata(botMetadata);
        }

        return innerContainer.withMessageContextInfo(mergedCtx.build());
    }

    /**
     * Builds the final {@link ChatMessageInfo} from the stanza
     * metadata and the decoded (possibly unwrapped) message container.
     *
     * @param stanza    the parsed stanza
     * @param chatJid   the effective chat JID (possibly overridden by DSM)
     * @param container the decoded message container
     * @return the fully populated message info
     *
     * @implNote WAWebMsgProcessingApiUtils.generateBaseMsg: constructs
     * the base message with id, from, to, type, ack, author, notifyName,
     * invis, count, and clientReceivedTsMillis.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingApiUtils", exports = "generateBaseMsg",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private ChatMessageInfo buildChatMessageInfo(
            MessageReceiveStanza stanza,
            Jid chatJid,
            MessageContainer container
    ) {
        // WAWebMsgProcessingApiUtils.generateBaseMsg
        // Resolves the fromMe flag and the sender's user-level JID
        var fromMe = isFromMe(stanza);
        var senderJid = stanza.senderJid().toUserJid();

        // WAWebMsgProcessingApiUtils.generateBaseMsg
        // Builds the MessageKey with id, chat JID, fromMe flag and sender
        var key = new MessageKeyBuilder()
                .id(stanza.id())
                .parentJid(chatJid)
                .fromMe(fromMe)
                .senderJid(senderJid)
                .build();

        // WAWebMsgProcessingApiUtils.generateBaseMsg
        // Populates the base message fields with timestamp, DELIVERED status, and broadcast metadata
        var builder = new ChatMessageInfoBuilder()
                .key(key)
                .message(container)
                .timestamp(stanza.timestamp())
                .status(MessageStatus.DELIVERED)
                .senderJid(senderJid)
                .broadcast(stanza.chatJid().hasBroadcastServer())
                .pushName(stanza.pushName().orElse(null))
                .urlText(stanza.urlText())
                .urlNumber(stanza.urlNumber());

        // WAWebMsgProcessingApiUtils.generateBaseMsg
        // Propagates the messageSecret from the context info into the top-level ChatMessageInfo
        container.messageContextInfo()
                .flatMap(ChatMessageContextInfo::messageSecret)
                .ifPresent(builder::messageSecret);

        return builder.build();
    }
}
