package com.github.auties00.cobalt.message.receive;

import com.github.auties00.cobalt.exception.WhatsAppAdvValidationException;
import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.message.receive.crypto.MessageDecryption;
import com.github.auties00.cobalt.message.receive.MessageReceiveStanzaParser.EncNode;
import com.github.auties00.cobalt.message.receive.MessageReceiveStanzaParser.ParsedMessageStanza;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.info.ChatMessageInfoBuilder;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.MessageContainer;
import com.github.auties00.cobalt.model.message.common.MessageStatus;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.groups.SignalGroupCipher;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main service for processing received messages.
 * Coordinates decryption, validation, parsing, and storage of incoming messages.
 * <p>
 * The {@link #process(ParsedMessageStanza)} method returns a {@link ChatMessageInfo} on success,
 * returns {@code null} for messages that should be silently skipped (duplicates, not for this device),
 * or throws a {@link WhatsAppMessageException.Receive} subclass for errors that require receipts.
 */
public final class MessageReceivingService {
    private static final System.Logger LOGGER = System.getLogger("MessageReceivingService");

    private final WhatsAppStore store;
    private final MessageDecryption decryptionService;

    private final Set<String> recentMessageIds = ConcurrentHashMap.newKeySet();
    private static final int MAX_RECENT_IDS = 10000;

    public MessageReceivingService(
            WhatsAppStore store,
            SignalSessionCipher sessionCipher,
            SignalGroupCipher groupCipher
    ) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.decryptionService = new MessageDecryption(store, sessionCipher, groupCipher);
    }

    /**
     * Processes a received message stanza.
     *
     * @param stanza the parsed message stanza
     * @return the processed message info, or {@code null} if the message should be skipped
     *         (duplicate or not for this device)
     * @throws WhatsAppMessageException.Receive if decryption or validation fails
     */
    public ChatMessageInfo process(ParsedMessageStanza stanza) {
        Objects.requireNonNull(stanza, "stanza cannot be null");

        var messageId = stanza.id();
        var chatJid = stanza.chatJid();
        var senderJid = stanza.senderJid();

        LOGGER.log(System.Logger.Level.DEBUG, "Processing message {0} from {1} in chat {2}",
                messageId, senderJid, chatJid);

        if (isDuplicate(messageId)) {
            var suppressNotification = stanza.shouldSuppressDuplicateNotification();
            if (suppressNotification) {
                LOGGER.log(System.Logger.Level.TRACE, "Suppressed duplicate message: {0}", messageId);
            } else {
                LOGGER.log(System.Logger.Level.DEBUG, "Duplicate message detected: {0}", messageId);
            }
            return null;
        }

        if (!isForThisDevice(stanza)) {
            LOGGER.log(System.Logger.Level.DEBUG, "Message not for this device: {0}", messageId);
            return null;
        }

        if (stanza.deviceIdentity().isPresent()) {
            try {
                // TODO: Validate ADV
            } catch (WhatsAppAdvValidationException e) {
                LOGGER.log(System.Logger.Level.WARNING, "ADV validation failed for {0}: {1}",
                        senderJid, e.getMessage());
                throw new WhatsAppMessageException.Receive.AdvFailure(e.getMessage(), e);
            }
        }

        MessageContainer messageContainer = decryptMessage(stanza);

        processSenderKeyDistributionIfPresent(messageContainer, chatJid, senderJid);

        var unwrappedContainer = messageContainer.unbox();
        if (unwrappedContainer == null) {
            unwrappedContainer = messageContainer;
        }

        var messageInfo = buildMessageInfo(stanza, unwrappedContainer);

        markAsProcessed(messageId);

        LOGGER.log(System.Logger.Level.DEBUG, "Successfully processed message {0}", messageId);
        return messageInfo;
    }

    /**
     * Decrypts the message from the stanza.
     *
     * @throws WhatsAppMessageException.Receive if decryption fails
     */
    private MessageContainer decryptMessage(ParsedMessageStanza stanza) {
        var chatJid = stanza.chatJid();
        var senderJid = stanza.senderJid();
        var encNodes = stanza.encNodes();

        if (encNodes.isEmpty()) {
            throw new WhatsAppMessageException.Receive.InvalidProtobuf("No enc nodes in message stanza");
        }

        if (stanza.isGroupMessage()) {
            return decryptGroupMessage(encNodes, chatJid, senderJid);
        } else {
            return decryptPrivateMessage(encNodes, senderJid);
        }
    }

    /**
     * Decrypts a group message, trying skmsg first then falling back to pkmsg/msg.
     */
    private MessageContainer decryptGroupMessage(List<EncNode> encNodes, Jid groupJid, Jid senderJid) {
        WhatsAppMessageException.Receive lastException = null;

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
            } catch (WhatsAppMessageException.Receive e) {
                lastException = e;
                LOGGER.log(System.Logger.Level.DEBUG, "SKMSG decryption failed ({0}), trying fallback: {1}",
                        e.retryReason(), e.getMessage());
            }
        }

        for (var encNode : encNodes) {
            if (encNode.isSenderKeyMessage()) {
                continue;
            }
            try {
                return decryptionService.decryptAndDecodeFromDevice(
                        encNode.ciphertext(),
                        senderJid,
                        encNode.type()
                );
            } catch (WhatsAppMessageException.Receive e) {
                if (lastException == null || isMoreSpecificException(e, lastException)) {
                    lastException = e;
                }
                LOGGER.log(System.Logger.Level.DEBUG, "Fallback decryption failed with {0}: {1}",
                        encNode.type(), e.getMessage());
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new WhatsAppMessageException.Receive.Unknown("All decryption attempts failed for group message");
    }

    /**
     * Determines if exception1 is more specific than exception2 for error reporting.
     */
    private boolean isMoreSpecificException(WhatsAppMessageException.Receive e1, WhatsAppMessageException.Receive e2) {
        return getExceptionSpecificity(e1) > getExceptionSpecificity(e2);
    }

    /**
     * Returns a specificity score for the exception type.
     */
    private int getExceptionSpecificity(WhatsAppMessageException.Receive e) {
        return switch (e) {
            case WhatsAppMessageException.Receive.NoSession _ -> 10;
            case WhatsAppMessageException.Receive.NoSenderKey _ -> 10;
            case WhatsAppMessageException.Receive.InvalidSignature _ -> 9;
            case WhatsAppMessageException.Receive.InvalidSenderKey _ -> 8;
            case WhatsAppMessageException.Receive.InvalidMessage _ -> 7;
            case WhatsAppMessageException.Receive.AdvFailure _ -> 6;
            case WhatsAppMessageException.Receive.InvalidDeviceSentMessage _ -> 5;
            case WhatsAppMessageException.Receive.DuplicateMessage _ -> 4;
            case WhatsAppMessageException.Receive.InvalidKey _ -> 3;
            case WhatsAppMessageException.Receive.BadMac _ -> 3;
            case WhatsAppMessageException.Receive.Unknown _ -> 1;
            default -> 2;
        };
    }

    /**
     * Decrypts a private (1:1) message.
     */
    private MessageContainer decryptPrivateMessage(List<EncNode> encNodes, Jid senderJid) {
        WhatsAppMessageException.Receive lastException = null;

        var sortedNodes = encNodes.stream()
                .filter(enc -> !enc.isSenderKeyMessage())
                .sorted((a, b) -> {
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
            } catch (WhatsAppMessageException.Receive e) {
                if (lastException == null || isMoreSpecificException(e, lastException)) {
                    lastException = e;
                }
                LOGGER.log(System.Logger.Level.DEBUG, "Decryption failed with {0} ({1}): {2}",
                        encNode.type(), e.retryReason(), e.getMessage());
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new WhatsAppMessageException.Receive.Unknown("All decryption attempts failed for private message");
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
     * Checks if a message is from the current user.
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
        if (stanza.isNewsletterMessage()) {
            return true;
        }

        if (stanza.to().isEmpty()) {
            return true;
        }

        return store.jid()
                .map(myJid -> {
                    var toJid = stanza.to().get();
                    return myJid.toUserJid().equals(toJid.toUserJid()) ||
                            myJid.equals(toJid);
                })
                .orElse(true);
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

        if (recentMessageIds.size() > MAX_RECENT_IDS) {
            var iterator = recentMessageIds.iterator();
            int toRemove = MAX_RECENT_IDS / 2;
            while (iterator.hasNext() && toRemove > 0) {
                iterator.next();
                iterator.remove();
                toRemove--;
            }
        }
    }
}
