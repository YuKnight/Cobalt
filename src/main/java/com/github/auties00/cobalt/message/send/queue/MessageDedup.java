package com.github.auties00.cobalt.message.send.queue;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread-safe cache of in-flight message IDs used to detect and suppress
 * duplicate send attempts.
 *
 * <p>Before a message enters the send pipeline, callers should check
 * {@link #isPending(String)} to determine whether an identical send is
 * already in progress.  If not, {@link #add(String)} registers the ID.
 * When the send completes (successfully or not), {@link #remove(String)}
 * clears the entry.
 *
 * <p>A single ID may be added more than once (the cache keeps a reference
 * count); the ID is considered pending until all matching {@code remove}
 * calls have been made.
 *
 * @apiNote WAWebMessageDedupUtils: manages a cache of pending messages
 * to handle message deduplication.  {@code addPendingMessage} increments
 * a counter; {@code hasPendingMessage} checks existence;
 * {@code maybeClearPendingMessages} clears the entire cache when the
 * offline delivery epoch resets to 0.
 */
public final class MessageDedup {
    /**
     * Map from message ID to its pending reference count.
     */
    private final ConcurrentMap<String, AtomicInteger> pending;

    /**
     * Creates a new, empty deduplication cache.
     */
    public MessageDedup() {
        this.pending = new ConcurrentHashMap<>();
    }

    /**
     * Registers a message ID as pending.
     *
     * <p>If the ID is already pending, its reference count is incremented.
     *
     * @param messageId the message ID
     * @return the new reference count for this ID
     * @throws NullPointerException if {@code messageId} is {@code null}
     *
     * @apiNote WAWebMessageDedupUtils.addPendingMessage: increments the
     * counter for the given key and returns the new total.
     */
    public int add(String messageId) {
        Objects.requireNonNull(messageId, "messageId");
        return pending.computeIfAbsent(messageId, _ -> new AtomicInteger())
                .incrementAndGet();
    }

    /**
     * Returns whether a message ID is currently pending.
     *
     * @param messageId the message ID
     * @return {@code true} if at least one send for this ID is in flight
     * @throws NullPointerException if {@code messageId} is {@code null}
     *
     * @apiNote WAWebMessageDedupUtils.hasPendingMessage: returns true
     * if the key exists in the cache.
     */
    public boolean isPending(String messageId) {
        Objects.requireNonNull(messageId, "messageId");
        var count = pending.get(messageId);
        return count != null && count.get() > 0;
    }

    /**
     * Decrements the reference count for a message ID and removes the
     * entry entirely when the count reaches zero.
     *
     * @param messageId the message ID
     * @throws NullPointerException if {@code messageId} is {@code null}
     */
    public void remove(String messageId) {
        Objects.requireNonNull(messageId, "messageId");
        pending.computeIfPresent(messageId, (_, count) -> count.decrementAndGet() <= 0 ? null : count);
    }

    /**
     * Removes all entries from the cache.
     *
     * @apiNote WAWebMessageDedupUtils.maybeClearPendingMessages: clears the
     * cache when the offline delivery epoch resets to 0.
     */
    public void clear() {
        pending.clear();
    }

    /**
     * Returns the number of distinct message IDs currently pending.
     *
     * @return the cache size
     */
    public int size() {
        return pending.size();
    }
}
