package com.github.auties00.cobalt.message.send.util;

import com.github.auties00.cobalt.model.jid.Jid;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Prevents duplicate messages from being sent during retries.
 *
 * @apiNote WAWebMessageDedupUtils
 */
public final class MessageDedup {
    private static final System.Logger LOGGER = System.getLogger("MessageDedup");
    private static final Duration DEFAULT_DEDUP_WINDOW = Duration.ofMinutes(5);
    private static final String KEY_SEPARATOR = "_";

    private final ConcurrentMap<String, Entry> pendingMessages = new ConcurrentHashMap<>();
    private final Duration dedupWindow;

    public MessageDedup() {
        this(DEFAULT_DEDUP_WINDOW);
    }

    public MessageDedup(Duration dedupWindow) {
        this.dedupWindow = dedupWindow;
    }

    /**
     * Attempts to mark a message as pending for sending.
     *
     * @return true if the message can be sent, false if it's a duplicate
     */
    public boolean tryMarkPending(String messageId, Jid chatJid, String encryptionContext) {
        cleanupExpired();

        var compositeKey = buildCompositeKey(messageId, chatJid, encryptionContext);
        var now = Instant.now();
        var existing = pendingMessages.putIfAbsent(compositeKey, new Entry(now));

        if (existing == null) {
            LOGGER.log(System.Logger.Level.TRACE, "Marked message {0} as pending", messageId);
            return true;
        }

        if (isExpired(existing, now)) {
            if (pendingMessages.replace(compositeKey, existing, new Entry(now))) {
                LOGGER.log(System.Logger.Level.TRACE, "Replaced expired pending entry for message {0}", messageId);
                return true;
            }
        }

        LOGGER.log(System.Logger.Level.DEBUG, "Message {0} is already pending, skipping duplicate send", messageId);
        return false;
    }

    /**
     * Marks a message as completed.
     */
    public void markCompleted(String messageId, Jid chatJid, String encryptionContext) {
        var compositeKey = buildCompositeKey(messageId, chatJid, encryptionContext);
        var removed = pendingMessages.remove(compositeKey);
        if (removed != null) {
            LOGGER.log(System.Logger.Level.TRACE, "Marked message {0} as completed", messageId);
        }
    }

    /**
     * Checks if a message is currently pending.
     */
    public boolean isPending(String messageId, Jid chatJid, String encryptionContext) {
        var compositeKey = buildCompositeKey(messageId, chatJid, encryptionContext);
        var entry = pendingMessages.get(compositeKey);
        if (entry == null) {
            return false;
        }
        return !isExpired(entry, Instant.now());
    }

    /**
     * Clears all pending messages.
     */
    public void clear() {
        var count = pendingMessages.size();
        pendingMessages.clear();
        if (count > 0) {
            LOGGER.log(System.Logger.Level.DEBUG, "Cleared {0} pending messages", count);
        }
    }

    /**
     * Returns the number of pending messages.
     */
    public int size() {
        return pendingMessages.size();
    }

    private String buildCompositeKey(String messageId, Jid chatJid, String encryptionContext) {
        var sb = new StringBuilder();
        sb.append(messageId);
        sb.append(KEY_SEPARATOR);
        sb.append(chatJid != null ? chatJid.toString() : "null");
        if (encryptionContext != null && !encryptionContext.isEmpty()) {
            sb.append(KEY_SEPARATOR);
            sb.append(encryptionContext);
        }
        return sb.toString();
    }

    private void cleanupExpired() {
        var now = Instant.now();
        pendingMessages.entrySet().removeIf(entry -> isExpired(entry.getValue(), now));
    }

    private boolean isExpired(Entry entry, Instant now) {
        return Duration.between(entry.createdAt(), now).compareTo(dedupWindow) > 0;
    }

    private record Entry(Instant createdAt) {}
}
