package com.github.auties00.cobalt.message.receipt;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.model.privacy.PrivacySettingType;
import com.github.auties00.cobalt.model.privacy.PrivacySettingValue;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for sending message receipts (delivery, read, played, retry, nack).
 * <p>
 * Receipt types:
 * <ul>
 *   <li><b>Delivery receipt</b>: Sent when message is received (double checkmark)</li>
 *   <li><b>Read receipt</b>: Sent when message is viewed (blue checkmark)</li>
 *   <li><b>Played receipt</b>: Sent for voice messages when played</li>
 *   <li><b>Retry receipt</b>: Sent when decryption fails, requests re-transmission</li>
 *   <li><b>Nack receipt</b>: Sent when parsing/protocol errors occur</li>
 * </ul>
 */
public final class MessageReceiptHandler {
    private static final System.Logger LOGGER = System.getLogger("ReceiptService");

    private final WhatsAppClient client;

    public MessageReceiptHandler(WhatsAppClient client) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    /**
     * Sends a read receipt for a single message.
     *
     * @param message the message to mark as read
     */
    public void sendReadReceipt(ChatMessageInfo message) {
        Objects.requireNonNull(message, "message cannot be null");
        sendReceipt(message, MessageReceiptType.READ);
    }

    /**
     * Sends read receipts for multiple messages.
     *
     * @param messages the messages to mark as read
     */
    public void sendReadReceipts(List<ChatMessageInfo> messages) {
        Objects.requireNonNull(messages, "messages cannot be null");
        if (messages.isEmpty()) {
            return;
        }

        // Group messages by chat for batch receipts
        var messagesByChat = new HashMap<Jid, List<ChatMessageInfo>>();
        for (var msg : messages) {
            messagesByChat.computeIfAbsent(msg.chatJid(), _ -> new ArrayList<>()).add(msg);
        }

        // Send batch receipts per chat
        for (var entry : messagesByChat.entrySet()) {
            var chatJid = entry.getKey();
            var chatMessages = entry.getValue();

            if (chatMessages.size() == 1) {
                sendReceipt(chatMessages.getFirst(), MessageReceiptType.READ);
            } else {
                sendBatchReceipt(chatJid, chatMessages, MessageReceiptType.READ);
            }
        }
    }

    /**
     * Sends a played receipt for a voice message.
     *
     * @param message the voice message that was played
     */
    public void sendPlayedReceipt(ChatMessageInfo message) {
        Objects.requireNonNull(message, "message cannot be null");
        sendReceipt(message, MessageReceiptType.PLAYED);
    }

    /**
     * Sends a delivery receipt for a message.
     * Usually called automatically when receiving a message.
     *
     * @param message the message to acknowledge delivery
     */
    public void sendDeliveryReceipt(ChatMessageInfo message) {
        Objects.requireNonNull(message, "message cannot be null");
        sendReceipt(message, MessageReceiptType.DELIVERY);
    }

    /**
     * Sends a retry receipt indicating decryption failure.
     * This requests the sender to re-transmit the message.
     * <p>
     * Retry receipt structure:
     * <pre>{@code
     * <receipt id="{msg_id}" to="{chat_jid}" type="retry" t="{timestamp}">
     *   <retry v="1" count="{retry_count}" reason="{reason}"/>
     *   <participant jid="{sender_jid}"/>  <!-- for groups -->
     * </receipt>
     * }</pre>
     *
     * @param messageId  the message ID that failed to decrypt
     * @param chatJid    the chat JID
     * @param senderJid  the sender JID (participant for groups)
     * @param reason     the retry reason
     * @param retryCount the current retry count
     */
    public void sendRetryReceipt(
            String messageId,
            Jid chatJid,
            Jid senderJid,
            WhatsAppMessageException.Receive.RetryReason reason,
            int retryCount
    ) {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(chatJid, "chatJid cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");

        var timestamp = System.currentTimeMillis() / 1000;

        var retryNode = new NodeBuilder()
                .description("retry")
                .attribute("v", "1")
                .attribute("count", retryCount)
                .attribute("reason", reason.protocolValue())
                .build();

        var children = new ArrayList<Node>();
        children.add(retryNode);

        // Add participant node for group messages
        if (chatJid.hasServer(JidServer.groupOrCommunity()) && senderJid != null) {
            var participantNode = new NodeBuilder()
                    .description("participant")
                    .attribute("jid", senderJid)
                    .build();
            children.add(participantNode);
        }

        var stanza = new NodeBuilder()
                .description("receipt")
                .attribute("id", messageId)
                .attribute("to", chatJid)
                .attribute("type", MessageReceiptType.RETRY.value())
                .attribute("t", timestamp)
                .content(children)
                .build();

        client.sendNodeWithNoResponse(stanza);

        LOGGER.log(System.Logger.Level.DEBUG, "Sent retry receipt for message {0} with reason {1}",
                messageId, reason);
    }

    /**
     * Sends a retry receipt for a ChatMessageInfo that failed to decrypt.
     *
     * @param message    the message info (may have empty message content)
     * @param reason     the retry reason
     * @param retryCount the current retry count
     */
    public void sendRetryReceipt(ChatMessageInfo message, WhatsAppMessageException.Receive.RetryReason reason, int retryCount) {
        sendRetryReceipt(message.id(), message.chatJid(), message.senderJid(), reason, retryCount);
    }

    /**
     * Sends a nack receipt indicating a parsing or protocol error.
     * The sender should not retry sending this message.
     * <p>
     * Nack receipt structure:
     * <pre>{@code
     * <receipt id="{msg_id}" to="{chat_jid}" type="nack" t="{timestamp}">
     *   <error code="{error_code}"/>
     * </receipt>
     * }</pre>
     *
     * @param messageId the message ID that failed to parse
     * @param chatJid   the chat JID
     * @param senderJid the sender JID (optional, for groups)
     * @param errorCode the error code (e.g., "400", "500")
     */
    public void sendNackReceipt(String messageId, Jid chatJid, Optional<Jid> senderJid, String errorCode) {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(chatJid, "chatJid cannot be null");
        Objects.requireNonNull(errorCode, "errorCode cannot be null");

        var timestamp = System.currentTimeMillis() / 1000;

        var errorNode = new NodeBuilder()
                .description("error")
                .attribute("code", errorCode)
                .build();

        var children = new ArrayList<Node>();
        children.add(errorNode);

        // Add participant node for group messages
        if (chatJid.hasServer(JidServer.groupOrCommunity()) && senderJid.isPresent()) {
            var participantNode = new NodeBuilder()
                    .description("participant")
                    .attribute("jid", senderJid.get())
                    .build();
            children.add(participantNode);
        }

        var stanza = new NodeBuilder()
                .description("receipt")
                .attribute("id", messageId)
                .attribute("to", chatJid)
                .attribute("type", MessageReceiptType.NACK.value())
                .attribute("t", timestamp)
                .content(children)
                .build();

        client.sendNodeWithNoResponse(stanza);

        LOGGER.log(System.Logger.Level.DEBUG, "Sent nack receipt for message {0} with error code {1}",
                messageId, errorCode);
    }

    /**
     * Sends a nack receipt with default error code "400".
     *
     * @param messageId the message ID
     * @param chatJid   the chat JID
     * @param senderJid the sender JID (optional, for groups)
     */
    public void sendNackReceipt(String messageId, Jid chatJid, Optional<Jid> senderJid) {
        sendNackReceipt(messageId, chatJid, senderJid, "400");
    }

    /**
     * Sends a receipt for a message.
     */
    private void sendReceipt(ChatMessageInfo message, MessageReceiptType type) {
        // Don't send receipts for our own messages
        if (message.fromMe()) {
            return;
        }

        // Check if receipts are enabled
        if (!shouldSendReceipt(message, type)) {
            LOGGER.log(System.Logger.Level.DEBUG, "Skipping {0} receipt per privacy settings", type);
            return;
        }

        var chatJid = message.chatJid();
        var senderJid = message.senderJid();
        var messageId = message.id();

        var stanza = buildReceiptStanza(chatJid, senderJid, List.of(messageId), type);
        client.sendNodeWithNoResponse(stanza.build());

        LOGGER.log(System.Logger.Level.DEBUG, "Sent {0} receipt for message {1}", type, messageId);
    }

    /**
     * Sends batch receipts for multiple messages in the same chat.
     */
    private void sendBatchReceipt(Jid chatJid, List<ChatMessageInfo> messages, MessageReceiptType type) {
        // Get the sender JID (for group messages)
        var senderJid = messages.getFirst().senderJid();

        // Collect all message IDs
        var messageIds = messages.stream()
                .filter(m -> !m.fromMe())
                .filter(m -> shouldSendReceipt(m, type))
                .map(ChatMessageInfo::id)
                .toList();

        if (messageIds.isEmpty()) {
            return;
        }

        var stanza = buildReceiptStanza(chatJid, senderJid, messageIds, type);
        client.sendNodeWithNoResponse(stanza.build());

        LOGGER.log(System.Logger.Level.DEBUG, "Sent batch {0} receipt for {1} messages",
                type, messageIds.size());
    }

    /**
     * Builds a receipt stanza.
     *
     * <pre>{@code
     * <receipt id="{first_msg_id}" to="{chat_jid}" type="{type}" t="{timestamp}">
     *   <list>
     *     <item id="{additional_msg_id}"/>
     *     ...
     *   </list>
     *   <participant jid="{sender_jid}"/>  <!-- for groups -->
     * </receipt>
     * }</pre>
     */
    private NodeBuilder buildReceiptStanza(
            Jid chatJid,
            Jid senderJid,
            List<String> messageIds,
            MessageReceiptType type
    ) {
        var firstMessageId = messageIds.getFirst();
        var timestamp = System.currentTimeMillis() / 1000;

        var builder = new NodeBuilder()
                .description("receipt")
                .attribute("id", firstMessageId)
                .attribute("to", chatJid)
                .attribute("t", timestamp);

        // Add type attribute (delivery receipts have no type attribute)
        if (type != MessageReceiptType.DELIVERY) {
            builder.attribute("type", type.value());
        }

        var children = new ArrayList<Node>();

        // Add list node for additional message IDs
        if (messageIds.size() > 1) {
            var itemNodes = messageIds.subList(1, messageIds.size()).stream()
                    .map(id -> new NodeBuilder()
                            .description("item")
                            .attribute("id", id)
                            .build())
                    .toList();

            var listNode = new NodeBuilder()
                    .description("list")
                    .content(itemNodes)
                    .build();
            children.add(listNode);
        }

        // Add participant node for group messages
        if (chatJid.hasServer(JidServer.groupOrCommunity()) && senderJid != null) {
            var participantNode = new NodeBuilder()
                    .description("participant")
                    .attribute("jid", senderJid)
                    .build();
            children.add(participantNode);
        }

        if (!children.isEmpty()) {
            builder.content(children);
        }

        return builder;
    }

    /**
     * Determines if a receipt should be sent based on privacy settings.
     */
    private boolean shouldSendReceipt(ChatMessageInfo message, MessageReceiptType type) {
        // Delivery receipts are always sent
        if (type == MessageReceiptType.DELIVERY) {
            return true;
        }

        // Group messages: check group settings
        if (message.chatJid().hasGroupOrCommunityServer()) {
            // Groups always show read receipts
            return true;
        }

        // Private chats: respect privacy settings
        // Default to true if no settings available
        return client.store()
                .privacySettings()
                .stream()
                .noneMatch(entry -> entry.type() == PrivacySettingType.READ_RECEIPTS && entry.value() != PrivacySettingValue.EVERYONE);
    }
}
