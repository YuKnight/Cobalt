package com.github.auties00.cobalt.message.receive.receipt;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.message.receive.stanza.MessageReceiveStanza;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.identity.ADVSignedDeviceIdentitySpec;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.util.DataUtils;
import com.github.auties00.libsignal.key.SignalIdentityPublicKey;
import com.github.auties00.libsignal.key.SignalPreKeyPair;

import java.util.Objects;

/**
 * Sends receipt stanzas in response to incoming messages.
 *
 * <p>After an incoming message is decrypted and processed, the client must send a
 * receipt back to the server indicating the outcome. The receipt type and content
 * vary depending on the result of processing:
 * <ul>
 *   <li><b>Delivery receipt</b>: the message was successfully decrypted and stored;
 *       acknowledges delivery to the sender.</li>
 *   <li><b>Retry receipt</b>: decryption failed with a retryable error; asks the
 *       sender to re-encrypt and resend, optionally bundling a fresh prekey for
 *       session re-establishment.</li>
 *   <li><b>Nack receipt</b>: the message was received but could not be parsed or
 *       validated; rejects the message with an error code.</li>
 *   <li><b>Bot invoke response ack</b>: the message was received from a bot sender;
 *       sends a specialised ack with {@code class="message"} and {@code type="text"}.</li>
 *   <li><b>Plain ack</b>: for messages that do not need a full delivery receipt
 *       (unavailable placeholders, media notify).</li>
 * </ul>
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgSendReceipt")
@WhatsAppWebModule(moduleName = "WAWebSendDeliveryReceiptJob")
@WhatsAppWebModule(moduleName = "WAWebSendRetryReceiptJob")
@WhatsAppWebModule(moduleName = "WAWebHandleMsgSendAck")
@WhatsAppWebModule(moduleName = "WAWebSendReceiptJobCommon")
public final class MessageReceiptHandler {
    /**
     * Logger for receipt construction and sending diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(MessageReceiptHandler.class.getName());

    /**
     * Retry count at which the prekey bundle is included in the retry receipt for
     * session re-establishment.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendRetryReceiptJob", exports = "sendRetryReceipt",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final int RETRY_KEY_BUNDLE_THRESHOLD = 2;

    /**
     * Client used to send receipt stanzas over the wire.
     */
    private final WhatsAppClient client;

    /**
     * Store used to access registration info, keys, and JID state.
     */
    private final WhatsAppStore store;

    /**
     * Constructs a new receipt handler.
     *
     * @param client the client used to send receipt stanzas
     * @throws NullPointerException if {@code client} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgSendReceipt", exports = "sendReceipt",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageReceiptHandler(WhatsAppClient client) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
        this.store = client.store();
    }

    /**
     * Sends a delivery receipt for a successfully decrypted message, assuming the
     * message is active (not inactive).
     *
     * <p>Convenience overload that delegates to
     * {@link #sendDeliveryReceipt(MessageReceiveStanza, MessageInfo, boolean)} with
     * {@code hasInactiveMsg = false}.
     *
     * @param stanza the parsed incoming stanza
     * @param info   the successfully processed message info, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgSendReceipt", exports = "sendReceipt",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void sendDeliveryReceipt(MessageReceiveStanza stanza, MessageInfo info) {
        sendDeliveryReceipt(stanza, info, false);
    }

    /**
     * Sends a delivery receipt for a successfully decrypted message.
     *
     * <p>The receipt type is determined by the message context:
     * <ul>
     *   <li>Peer messages use {@link MessageReceiptType#PEER}.</li>
     *   <li>Messages from self (companion device) use {@link MessageReceiptType#SENDER}.</li>
     *   <li>Inactive messages (when {@code hasInactiveMsg} is {@code true} and the
     *       sender is not self) use {@link MessageReceiptType#INACTIVE}.</li>
     *   <li>All other messages use {@link MessageReceiptType#DELIVERY}.</li>
     * </ul>
     *
     * @param stanza         the parsed incoming stanza
     * @param info           the successfully processed message info, or {@code null}
     * @param hasInactiveMsg whether processing produced an inactive-message flag
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgSendReceipt", exports = "sendReceipt",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSendDeliveryReceiptJob", exports = "sendDeliveryReceiptsAfterDecryption",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void sendDeliveryReceipt(MessageReceiveStanza stanza, MessageInfo info, boolean hasInactiveMsg) {
        var from = resolveFrom(stanza);
        var participant = resolveReceiptParticipant(stanza);
        var isSender = isSenderReceipt(from, participant);
        var receiptType = resolveDeliveryReceiptType(stanza, isSender, hasInactiveMsg);

        var isPeer = stanza.isPeer();
        var recipientJid = resolveRecipientForReceipt(stanza);
        var shouldSetRecipient = !isPeer && isSender && recipientJid != null;

        var toJid = from.toUserJid();
        var receipt = new NodeBuilder()
                .description("receipt")
                .attribute("id", stanza.id())
                .attribute("type", receiptType.protocolValue())
                .attribute("to", toJid)
                .attribute("participant",
                        (from.hasGroupOrCommunityServer() || from.hasBroadcastServer()) && participant != null
                                ? participant
                                : null)
                .attribute("recipient",
                        shouldSetRecipient ? recipientJid.toUserJid() : null);
        client.sendNodeWithNoResponse(receipt.build());
    }

    /**
     * Sends a retry receipt requesting the sender to re-encrypt and resend the message.
     *
     * <p>The retry receipt carries the failure reason code and the current retry
     * count. From the second attempt onward the receipt also includes the device's
     * registration id and a prekey bundle so the sender can re-establish the Signal
     * session.
     *
     * @param stanza      the parsed incoming stanza
     * @param retryReason the reason for the decryption failure
     * @param retryCount  the current retry attempt number (1-based)
     */
    @WhatsAppWebExport(moduleName = "WAWebSendRetryReceiptJob", exports = "sendRetryReceipt",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void sendRetryReceipt(
            MessageReceiveStanza stanza,
            WhatsAppMessageException.Receive.RetryReason retryReason,
            int retryCount
    ) {
        // Skip retry when talking to a bot via a non-bot chat. The bot has no useful retry semantics in that case.
        var from = resolveFrom(stanza);
        var participant = resolveReceiptParticipant(stanza);
        if (!from.hasBotServer() && participant != null && participant.hasBotServer()) {
            return;
        }

        var retryNode = new NodeBuilder()
                .description("retry")
                .attribute("v", "1")
                .attribute("count", retryCount)
                .attribute("id", stanza.id())
                .attribute("t", String.valueOf(stanza.timestamp().getEpochSecond()))
                .attribute("error", retryReason.protocolValue())
                .build();

        var registrationNode = new NodeBuilder()
                .description("registration")
                .content(DataUtils.intToBytes(store.registrationId(), 4))
                .build();

        var keysNode = retryCount >= RETRY_KEY_BUNDLE_THRESHOLD
                ? buildKeyBundleNode()
                : null;

        Jid toJid;
        Jid participantAttr = null;
        Jid recipientAttr = null;
        String categoryAttr = null;

        if (from.hasUserServer() || from.hasLidServer()) {
            // 1:1 messages address the sender device directly.
            toJid = from;
            var selfJid = store.jid().orElse(null);
            if (selfJid != null && from.toUserJid().equals(selfJid.toUserJid())) {
                // Self-sent messages receive a peer category or a recipient attribute based on isPeer.
                if (stanza.isPeer()) {
                    categoryAttr = "peer";
                } else {
                    var recipientJid = resolveRecipientForReceipt(stanza);
                    if (recipientJid != null) {
                        recipientAttr = recipientJid.toUserJid();
                    }
                }
            }
        } else {
            // Group and broadcast messages address the chat and carry the participant attribute.
            toJid = from.toUserJid();
            if (participant != null) {
                participantAttr = participant;
            }
        }

        var receipt = new NodeBuilder()
                .description("receipt")
                .attribute("id", stanza.id())
                .attribute("type", MessageReceiptType.RETRY.protocolValue())
                .attribute("to", toJid)
                .attribute("participant", participantAttr)
                .attribute("recipient", recipientAttr)
                .attribute("category", categoryAttr)
                .content(retryNode, registrationNode, keysNode);
        client.sendNodeWithNoResponse(receipt.build());
    }

    /**
     * Sends a bot invoke response ack for messages received from bot senders.
     *
     * <p>Bot messages receive a specialised ack node (not a receipt node) with
     * {@code class="message"} and {@code type="text"}. The addressing differs from
     * normal receipts:
     * <ul>
     *   <li>For 1:1 chats: {@code to = author, recipient = chat}.</li>
     *   <li>For groups/broadcasts: {@code to = chat, participant = author}.</li>
     * </ul>
     *
     * @param stanza the parsed incoming stanza
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "sendBotInvokeResponseAcks",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void sendBotInvokeResponseAck(MessageReceiveStanza stanza) {
        var chatJid = stanza.chatJid();
        Jid to;
        Jid recipient = null;
        Jid participantJid = null;

        if (!chatJid.hasGroupOrCommunityServer() && !chatJid.hasBroadcastServer()) {
            // CHAT type: to = author (sender), recipient = chat.
            to = stanza.senderJid();
            recipient = chatJid.toUserJid();
        } else {
            // Non-CHAT type: to = chat, participant = author.
            to = chatJid;
            participantJid = stanza.participant()
                    .map(Jid::toUserJid)
                    .orElse(null);
        }

        var ack = new NodeBuilder()
                .description("ack")
                .attribute("id", stanza.id())
                .attribute("to", to)
                .attribute("recipient", recipient)
                .attribute("participant", participantJid)
                .attribute("class", "message")
                .attribute("type", "text");
        client.sendNodeWithNoResponse(ack.build());
    }

    /**
     * Returns whether the message sender is a bot that requires a bot-specific
     * receipt rather than a normal delivery receipt.
     *
     * @param stanza the parsed incoming stanza
     * @return {@code true} if the sender is a bot
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgSendReceipt", exports = "sendReceipt",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean isBotSender(MessageReceiveStanza stanza) {
        return !stanza.chatJid().hasBotServer()
                && stanza.senderJid().hasBotServer();
    }

    /**
     * Sends a negative acknowledgment (NACK) for a message that failed validation
     * or protobuf parsing, without a failure reason.
     *
     * <p>Convenience overload that delegates to
     * {@link #sendNackReceipt(MessageReceiveStanza, int, Integer)} with
     * {@code failureReason = null}.
     *
     * @param stanza    the parsed incoming stanza
     * @param errorCode the integer error code to include in the NACK
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgSendAck", exports = "sendNack",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void sendNackReceipt(MessageReceiveStanza stanza, int errorCode) {
        sendNackReceipt(stanza, errorCode, null);
    }

    /**
     * Sends a negative acknowledgment (NACK) for a message that failed validation or
     * protobuf parsing.
     *
     * <p>The NACK is an {@code <ack>} stanza with an {@code error} attribute carrying
     * the integer error code. For {@code InvalidProtobuf} errors (code 491), a
     * {@code <meta>} child with a {@code failure_reason} attribute is included when
     * available.
     *
     * @param stanza        the parsed incoming stanza
     * @param errorCode     the integer error code to include in the NACK
     * @param failureReason the optional failure reason for InvalidProtobuf errors
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgSendAck", exports = "sendNack",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void sendNackReceipt(MessageReceiveStanza stanza, int errorCode, Integer failureReason) {
        Node metaNode = null;
        if (errorCode == 491 && failureReason != null) {
            metaNode = new NodeBuilder()
                    .description("meta")
                    .attribute("failure_reason", failureReason)
                    .build();
        }

        var ack = new NodeBuilder()
                .description("ack")
                .attribute("id", stanza.id())
                .attribute("class", "message")
                .attribute("from", store.jid().orElse(null))
                .attribute("to", resolveFrom(stanza))
                .attribute("participant",
                        resolveReceiptParticipant(stanza))
                .attribute("type", stanza.stanzaType())
                .attribute("error", errorCode)
                .content(metaNode);
        client.sendNodeWithNoResponse(ack.build());
    }

    /**
     * Sends a plain ack for messages that do not need a full delivery receipt (for
     * example unavailable or fanout placeholders, media notify).
     *
     * @param stanza the parsed incoming stanza
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgSendAck", exports = "sendAck",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void sendAck(MessageReceiveStanza stanza) {
        var ack = new NodeBuilder()
                .description("ack")
                .attribute("id", stanza.id())
                .attribute("class", "message")
                .attribute("from", store.jid().orElse(null))
                .attribute("to", resolveFrom(stanza))
                .attribute("participant",
                        resolveReceiptParticipant(stanza))
                .attribute("type", stanza.stanzaType());
        client.sendNodeWithNoResponse(ack.build());
    }

    /**
     * Builds a {@code <keys>} node carrying the identity key, a one-time prekey, the
     * signed prekey, and the device-identity for session re-establishment.
     *
     * @return the keys node, or {@code null} if the bundle cannot be built
     */
    @WhatsAppWebExport(moduleName = "WAWebSendRetryReceiptJob", exports = "sendRetryReceipt",
            adaptation = WhatsAppAdaptation.DIRECT)
    private Node buildKeyBundleNode() {
        try {
            var preKey = store.hasPreKeys()
                    ? store.preKeys().getFirst()
                    : SignalPreKeyPair.random(1);
            if (!store.hasPreKeys()) {
                store.addPreKey(preKey);
            }

            var typeNode = new NodeBuilder()
                    .description("type")
                    .content(new byte[]{SignalIdentityPublicKey.type()})
                    .build();

            var identityNode = new NodeBuilder()
                    .description("identity")
                    .content(store.identityKeyPair().publicKey().toEncodedPoint())
                    .build();

            var preKeyIdNode = new NodeBuilder()
                    .description("id")
                    .content(DataUtils.intToBytes(preKey.id(), 3))
                    .build();
            var preKeyValueNode = new NodeBuilder()
                    .description("value")
                    .content(preKey.publicKey().toEncodedPoint())
                    .build();
            var preKeyNode = new NodeBuilder()
                    .description("key")
                    .content(preKeyIdNode, preKeyValueNode)
                    .build();

            var signedKeyPair = store.signedKeyPair();
            var skeyIdNode = new NodeBuilder()
                    .description("id")
                    .content(DataUtils.intToBytes(signedKeyPair.id(), 3))
                    .build();
            var skeyValueNode = new NodeBuilder()
                    .description("value")
                    .content(signedKeyPair.publicKey().toEncodedPoint())
                    .build();
            var skeySigNode = new NodeBuilder()
                    .description("signature")
                    .content(signedKeyPair.signature())
                    .build();
            var skeyNode = new NodeBuilder()
                    .description("skey")
                    .content(skeyIdNode, skeyValueNode, skeySigNode)
                    .build();

            var deviceIdentityNode = store.signedDeviceIdentity()
                    .map(id -> new NodeBuilder()
                            .description("device-identity")
                            .content(ADVSignedDeviceIdentitySpec.encode(id))
                            .build())
                    .orElse(null);

            return new NodeBuilder()
                    .description("keys")
                    .content(typeNode, identityNode, preKeyNode, skeyNode, deviceIdentityNode)
                    .build();
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to build key bundle for retry receipt: {0}", e.getMessage());
            return null;
        }
    }

    /**
     * Resolves the {@code from} value used for receipt addressing.
     *
     * <p>For 1:1 chat messages the {@code from} is the sender's JID; for group,
     * broadcast, and status messages the {@code from} is the chat JID.
     *
     * @param stanza the parsed incoming stanza
     * @return the resolved from JID
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingApiUtils", exports = "getFrom",
            adaptation = WhatsAppAdaptation.DIRECT)
    private Jid resolveFrom(MessageReceiveStanza stanza) {
        var chatJid = stanza.chatJid();
        if (!chatJid.hasGroupOrCommunityServer()
                && !chatJid.hasBroadcastServer()
                && !chatJid.isStatusBroadcastAccount()) {
            return stanza.senderJid();
        }

        return chatJid;
    }

    /**
     * Resolves the participant attribute used for receipt stanzas.
     *
     * <p>For non-CHAT messages the participant is the sender's device JID. For CHAT
     * messages the participant is {@code null}.
     *
     * @param stanza the parsed incoming stanza
     * @return the participant JID, or {@code null} for 1:1 chats
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgSendReceipt", exports = "sendReceipt",
            adaptation = WhatsAppAdaptation.DIRECT)
    private Jid resolveReceiptParticipant(MessageReceiveStanza stanza) {
        var chatJid = stanza.chatJid();
        if (chatJid.hasGroupOrCommunityServer()
                || chatJid.hasBroadcastServer()
                || chatJid.isStatusBroadcastAccount()) {
            return stanza.participant().orElse(null);
        }
        return null;
    }

    /**
     * Resolves the recipient JID for receipt stanzas.
     *
     * <p>The recipient is only relevant for CHAT-type messages sent from our own
     * account (companion-device messages). It is derived from
     * {@code originalBotRecipient}, {@code preMatChat}, or the chat JID itself.
     *
     * @param stanza the parsed incoming stanza
     * @return the recipient JID, or {@code null} if not applicable
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgSendReceipt", exports = "sendReceipt",
            adaptation = WhatsAppAdaptation.DIRECT)
    private Jid resolveRecipientForReceipt(MessageReceiveStanza stanza) {
        var chatJid = stanza.chatJid();
        if (chatJid.hasGroupOrCommunityServer()
                || chatJid.hasBroadcastServer()
                || chatJid.isStatusBroadcastAccount()) {
            return null;
        }

        var selfJid = store.jid().orElse(null);
        if (selfJid == null) {
            return null;
        }

        if (!stanza.senderJid().toUserJid().equals(selfJid.toUserJid())) {
            return null;
        }

        // Cobalt does not track originalBotRecipient or preMatChat, so the chat JID is the only available value.
        return chatJid;
    }

    /**
     * Returns whether the receipt should use the {@code sender} type.
     *
     * <p>A sender receipt is sent when the message is from our own account (from a
     * companion device), indicating that we as sender know the message was delivered.
     *
     * @param from        the resolved from JID
     * @param participant the resolved participant JID, or {@code null}
     * @return {@code true} if the sender receipt type should be used
     */
    @WhatsAppWebExport(moduleName = "WAWebSendDeliveryReceiptJob", exports = "sendDeliveryReceiptsAfterDecryption",
            adaptation = WhatsAppAdaptation.DIRECT)
    private boolean isSenderReceipt(Jid from, Jid participant) {
        var selfJid = store.jid().orElse(null);
        if (selfJid == null) {
            return false;
        }

        var selfUser = selfJid.toUserJid();

        if ((from.hasUserServer() || from.hasLidServer())
                && from.toUserJid().equals(selfUser)) {
            return true;
        }

        if (participant != null && participant.toUserJid().equals(selfUser)) {
            return true;
        }

        return false;
    }

    /**
     * Determines the delivery receipt type based on the message context.
     *
     * @param stanza         the parsed incoming stanza
     * @param isSender       whether the message is from our own account
     * @param hasInactiveMsg whether processing produced an inactive flag
     * @return the resolved receipt type
     */
    @WhatsAppWebExport(moduleName = "WAWebSendDeliveryReceiptJob", exports = "sendDeliveryReceiptsAfterDecryption",
            adaptation = WhatsAppAdaptation.DIRECT)
    private MessageReceiptType resolveDeliveryReceiptType(
            MessageReceiveStanza stanza,
            boolean isSender,
            boolean hasInactiveMsg
    ) {
        if (stanza.isPeer()) {
            return MessageReceiptType.PEER;
        }

        if (isSender) {
            return MessageReceiptType.SENDER;
        }

        if (hasInactiveMsg) {
            return MessageReceiptType.INACTIVE;
        }

        // Active delivery drops the type attribute via the DELIVERY null protocol value.
        return MessageReceiptType.DELIVERY;
    }
}
