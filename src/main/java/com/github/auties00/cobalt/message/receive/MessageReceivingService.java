package com.github.auties00.cobalt.message.receive;

import com.github.auties00.cobalt.exception.ADVValidationException;
import com.github.auties00.cobalt.exception.MessageDecryptionException;
import com.github.auties00.cobalt.exception.MessageParseException;
import com.github.auties00.cobalt.message.receive.decryption.MessageDecryptionService;
import com.github.auties00.cobalt.message.receive.stanza.MessageReceiveStanzaParser.EncNode;
import com.github.auties00.cobalt.message.receive.stanza.MessageReceiveStanzaParser.ParsedMessageStanza;
import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.chat.ChatBuilder;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.info.ChatMessageInfoBuilder;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.model.ChatMessageKey;
import com.github.auties00.cobalt.model.message.model.MessageContainer;
import com.github.auties00.cobalt.model.message.model.MessageStatus;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.groups.SignalGroupCipher;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main service for processing received messages.
 * Coordinates decryption, validation, parsing, and storage of incoming messages.
 * This is the counterpart to {@link com.github.auties00.cobalt.message.send.MessageSendingService}.
 */
public final class MessageReceivingService {
    private static final System.Logger LOGGER = System.getLogger("MessageReceivingService");

    private final WhatsAppStore store;
    private final MessageDecryptionService decryptionService;

    // Track recently processed message IDs to detect duplicates
    private final Set<String> recentMessageIds = ConcurrentHashMap.newKeySet();
    private static final int MAX_RECENT_IDS = 10000;

    public MessageReceivingService(
            WhatsAppStore store,
            SignalSessionCipher sessionCipher,
            SignalGroupCipher groupCipher
    ) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.decryptionService = new MessageDecryptionService(store, sessionCipher, groupCipher);
    }

    /**
     * Processes a received message stanza.
     *
     * @param stanza the parsed message stanza
     * @return the result of processing
     */
    public MessageReceiveResult process(ParsedMessageStanza stanza) {
        Objects.requireNonNull(stanza, "stanza cannot be null");

        var messageId = stanza.id();
        var chatJid = stanza.chatJid();
        var senderJid = stanza.senderJid();

        LOGGER.log(System.Logger.Level.DEBUG, "Processing message {0} from {1} in chat {2}",
                messageId, senderJid, chatJid);

        try {
            // 1. Check for duplicates
            // Edge case 58: suppress notification if hideFail flag set or reaction/poll vote
            if (isDuplicate(messageId)) {
                var suppressNotification = stanza.shouldSuppressDuplicateNotification();
                if (suppressNotification) {
                    LOGGER.log(System.Logger.Level.TRACE, "Suppressed duplicate message: {0}", messageId);
                } else {
                    LOGGER.log(System.Logger.Level.DEBUG, "Duplicate message detected: {0}", messageId);
                }
                return new MessageReceiveResult.Duplicate(messageId, suppressNotification);
            }

            // 2. Check if message is for this device
            if (!isForThisDevice(stanza)) {
                return new MessageReceiveResult.NotForThisDevice(messageId, "Not addressed to this device");
            }

            // 3. Validate ADV for companion devices
            if (stanza.deviceIdentity().isPresent()) {
                try {
                    // TODO: Validate ADV
                } catch (ADVValidationException e) {
                    LOGGER.log(System.Logger.Level.WARNING, "ADV validation failed for {0}: {1}",
                            senderJid, e.getMessage());
                    return new MessageReceiveResult.AdvValidationFailure(messageId, e.getMessage());
                }
            }

            // 4. Decrypt the message
            MessageContainer messageContainer;
            try {
                messageContainer = decryptMessage(stanza);
            } catch (MessageDecryptionException e) {
                LOGGER.log(System.Logger.Level.WARNING, "Decryption failed for message {0}: {1}",
                        messageId, e.getMessage());
                return new MessageReceiveResult.DecryptionFailure(e.reason(), messageId, e);
            }

            // 5. Process sender key distribution if present
            processSenderKeyDistributionIfPresent(messageContainer, chatJid, senderJid);

            // 6. Unwrap wrapper messages (deviceSent, ephemeral, viewOnce)
            var unwrappedContainer = messageContainer.unbox();
            if (unwrappedContainer == null) {
                unwrappedContainer = messageContainer;
            }

            // 7. Build ChatMessageInfo
            var messageInfo = buildMessageInfo(stanza, unwrappedContainer);

            // 8. Mark as processed
            markAsProcessed(messageId);

            LOGGER.log(System.Logger.Level.DEBUG, "Successfully processed message {0}", messageId);
            return new MessageReceiveResult.Success(messageInfo);

        } catch (MessageParseException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Parse error for message {0}: {1}",
                    messageId, e.getMessage());
            return new MessageReceiveResult.ParseError(e.errorCode(), messageId, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.ERROR, "Unexpected error processing message {0}: {1}",
                    messageId, e.getMessage(), e);
            return new MessageReceiveResult.ParseError("500", messageId, e.getMessage());
        }
    }

    /**
     * Decrypts the message from the stanza.
     */
    private MessageContainer decryptMessage(ParsedMessageStanza stanza) {
        var chatJid = stanza.chatJid();
        var senderJid = stanza.senderJid();
        var encNodes = stanza.encNodes();

        if (encNodes.isEmpty()) {
            throw new MessageParseException(stanza.id(), "No enc nodes in message stanza");
        }

        // For groups, try skmsg first (most common)
        if (stanza.isGroupMessage()) {
            return decryptGroupMessage(encNodes, chatJid, senderJid);
        } else {
            return decryptPrivateMessage(encNodes, senderJid);
        }
    }

    /**
     * Decrypts a group message, trying skmsg first then falling back to pkmsg/msg.
     * <p>
     * Edge case handling:
     * - If SKMSG fails with NO_SENDER_KEY, try pkmsg/msg (sender key distribution message)
     * - Propagate the most relevant exception if all attempts fail
     */
    private MessageContainer decryptGroupMessage(java.util.List<EncNode> encNodes, Jid groupJid, Jid senderJid) {
        MessageDecryptionException lastException = null;

        // Try skmsg first (sender key encryption, most common for groups)
        var skmsgNode = encNodes.stream()
                .filter(EncNode::isSenderKeyMessage)
                .findFirst();

        if (skmsgNode.isPresent()) {
            try {
                return decryptionService.decryptAndDecodeFromGroup(
                        skmsgNode.get().ciphertext(),
                        groupJid,
                        senderJid
                );
            } catch (MessageDecryptionException e) {
                lastException = e;
                LOGGER.log(System.Logger.Level.DEBUG, "SKMSG decryption failed ({0}), trying fallback: {1}",
                        e.reason(), e.getMessage());
                // Fall through to try pkmsg/msg
            }
        }

        // Fallback: try pkmsg/msg (used for sender key distribution)
        for (var encNode : encNodes) {
            if (encNode.isSenderKeyMessage()) {
                continue; // Already tried
            }
            try {
                return decryptionService.decryptAndDecodeFromDevice(
                        encNode.ciphertext(),
                        senderJid,
                        encNode.type()
                );
            } catch (MessageDecryptionException e) {
                // Keep the more specific exception (e.g., SESSION_NOT_FOUND over DECRYPT_FAILED)
                if (lastException == null || isMoreSpecificReason(e.reason(), lastException.reason())) {
                    lastException = e;
                }
                LOGGER.log(System.Logger.Level.DEBUG, "Fallback decryption failed with {0}: {1}",
                        encNode.type(), e.getMessage());
                // Continue to next enc node
            }
        }

        // All attempts failed - throw the most relevant exception
        if (lastException != null) {
            throw lastException;
        }
        throw new MessageDecryptionException(
                MessageDecryptionException.Reason.DECRYPT_FAILED,
                "All decryption attempts failed for group message"
        );
    }

    /**
     * Determines if reason1 is more specific than reason2 for error reporting.
     * More specific reasons give better retry receipt feedback to the sender.
     */
    private boolean isMoreSpecificReason(MessageDecryptionException.Reason reason1, MessageDecryptionException.Reason reason2) {
        // Priority order: specific reasons > generic DECRYPT_FAILED
        var specificity = java.util.Map.of(
                MessageDecryptionException.Reason.SESSION_NOT_FOUND, 10,
                MessageDecryptionException.Reason.NO_SENDER_KEY, 10,
                MessageDecryptionException.Reason.UNTRUSTED_IDENTITY, 9,
                MessageDecryptionException.Reason.INVALID_SENDER_KEY, 8,
                MessageDecryptionException.Reason.INVALID_MESSAGE, 7,
                MessageDecryptionException.Reason.ADV_FAILURE, 6,
                MessageDecryptionException.Reason.INVALID_DSM, 5,
                MessageDecryptionException.Reason.DUPLICATE_MESSAGE, 4,
                MessageDecryptionException.Reason.DECRYPT_FAILED, 1
        );
        return specificity.getOrDefault(reason1, 0) > specificity.getOrDefault(reason2, 0);
    }

    /**
     * Decrypts a private (1:1) message.
     * <p>
     * Edge case handling:
     * - Try pkmsg first if present (establishes new session)
     * - Fall back to msg if pkmsg fails or isn't present
     * - Propagate the most relevant exception if all attempts fail
     */
    private MessageContainer decryptPrivateMessage(java.util.List<EncNode> encNodes, Jid senderJid) {
        MessageDecryptionException lastException = null;

        // Prefer pkmsg (establishes session) over msg
        var sortedNodes = encNodes.stream()
                .filter(enc -> !enc.isSenderKeyMessage()) // skmsg not used for 1:1
                .sorted((a, b) -> {
                    // PKMSG first (can establish session), then MSG
                    if (a.isPreKeyMessage() && !b.isPreKeyMessage()) return -1;
                    if (!a.isPreKeyMessage() && b.isPreKeyMessage()) return 1;
                    return 0;
                })
                .toList();

        for (var encNode : sortedNodes) {
            try {
                return decryptionService.decryptAndDecodeFromDevice(
                        encNode.ciphertext(),
                        senderJid,
                        encNode.type()
                );
            } catch (MessageDecryptionException e) {
                if (lastException == null || isMoreSpecificReason(e.reason(), lastException.reason())) {
                    lastException = e;
                }
                LOGGER.log(System.Logger.Level.DEBUG, "Decryption failed with {0} ({1}): {2}",
                        encNode.type(), e.reason(), e.getMessage());
                // Continue to next enc node
            }
        }

        // All attempts failed - throw the most relevant exception
        if (lastException != null) {
            throw lastException;
        }
        throw new MessageDecryptionException(
                MessageDecryptionException.Reason.DECRYPT_FAILED,
                "All decryption attempts failed for private message"
        );
    }

    /**
     * Processes sender key distribution message if present.
     */
    private void processSenderKeyDistributionIfPresent(MessageContainer container, Jid chatJid, Jid senderJid) {
        var senderKeyDist = container.senderKeyDistributionMessage();
        if (senderKeyDist.isEmpty()) {
            return;
        }

        try {
            var groupJid = senderKeyDist.get().groupJid();
            var distributionData = senderKeyDist.get().data();

            if (distributionData != null && distributionData.length > 0) {
                decryptionService.processSenderKeyDistribution(groupJid, senderJid, distributionData);
                LOGGER.log(System.Logger.Level.DEBUG, "Processed sender key distribution for group {0} from {1}",
                        groupJid, senderJid);
            }
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to process sender key distribution: {0}", e.getMessage());
        }
    }

    /**
     * Builds a ChatMessageInfo from the parsed stanza and decrypted message.
     */
    private ChatMessageInfo buildMessageInfo(ParsedMessageStanza stanza, MessageContainer messageContainer) {
        var chatJid = stanza.chatJid();
        var senderJid = stanza.senderJid();
        var fromMe = isFromMe(senderJid);

        var messageKey = new ChatMessageKey(
                chatJid,
                fromMe,
                stanza.id(),
                fromMe ? null : senderJid
        );

        return new ChatMessageInfoBuilder()
                .key(messageKey)
                .message(messageContainer)
                .timestampSeconds(stanza.timestamp())
                .status(MessageStatus.DELIVERED)
                .senderJid(fromMe ? null : senderJid)
                .pushName(stanza.notifyStr().orElse(null))
                .broadcast(stanza.isStatusMessage())
                .build();
    }

    /**
     * Checks if a message is from the current user (this device or another device of the same user).
     */
    private boolean isFromMe(Jid senderJid) {
        return store.jid()
                .map(myJid -> myJid.toUserJid().equals(senderJid.toUserJid()))
                .orElse(false);
    }

    /**
     * Checks if this message is intended for this device.
     */
    private boolean isForThisDevice(ParsedMessageStanza stanza) {
        // Newsletter messages are for everyone
        if (stanza.isNewsletterMessage()) {
            return true;
        }

        // Check the "to" attribute matches our JID
        if (stanza.to().isEmpty()) {
            return true; // No explicit recipient, assume it's for us
        }

        return store.jid()
                .map(myJid -> {
                    var toJid = stanza.to().get();
                    // Check if the user part matches (ignoring device)
                    return myJid.toUserJid().equals(toJid.toUserJid()) ||
                            myJid.equals(toJid);
                })
                .orElse(true); // If we don't know our JID, accept the message
    }

    /**
     * Checks if a message has already been processed (duplicate detection).
     */
    private boolean isDuplicate(String messageId) {
        return recentMessageIds.contains(messageId);
    }

    /**
     * Marks a message as processed.
     */
    private void markAsProcessed(String messageId) {
        recentMessageIds.add(messageId);

        // Cleanup old entries if needed
        if (recentMessageIds.size() > MAX_RECENT_IDS) {
            // Remove approximately half of the entries
            var iterator = recentMessageIds.iterator();
            int toRemove = MAX_RECENT_IDS / 2;
            while (iterator.hasNext() && toRemove > 0) {
                iterator.next();
                iterator.remove();
                toRemove--;
            }
        }
    }

    /**
     * Gets or creates a chat for the given JID.
     */
    public Chat getOrCreateChat(Jid chatJid) {
        return store.findChatByJid(chatJid)
                .orElseGet(() -> {
                    var chat = new ChatBuilder()
                            .jid(chatJid)
                            .build();
                    store.addChat(chat);
                    return chat;
                });
    }
}
