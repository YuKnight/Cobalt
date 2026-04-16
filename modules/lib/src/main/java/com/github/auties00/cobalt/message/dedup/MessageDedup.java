package com.github.auties00.cobalt.message.dedup;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory cache of in-flight message keys used to suppress duplicate
 * send or receive attempts during the offline-delivery replay window.
 *
 * <p>When WhatsApp reconnects it redelivers every message that was not
 * acknowledged before the disconnect. Without a guard the same message would
 * be processed twice: once from the replay stream and once from any queued
 * stanza that had already arrived during the previous session. The dedup
 * cache stores a composite key for every message currently being processed;
 * a caller can ask whether a key is pending via {@link #isPending(String)}
 * before spending work on it and then register it via {@link #add(String)}
 * to take ownership.
 *
 * <p>Entries hold a reference count so a single key can be registered by
 * multiple cooperating call sites without being prematurely evicted. The
 * cache is flushed in bulk via {@link #maybeClear(int)} when the
 * offline-delivery counter reaches zero, matching the WA Web contract that
 * entries survive only for the duration of a single offline replay.
 *
 * @implNote WAWebMessageDedupUtils: owns a module-level {@code Map} named
 * {@code c} keyed by the composite string built from
 * {@code WAWebPendingMessageKey.createPendingMessageKey(key, ts, encs)}. The
 * module exports {@code addPendingMessage}, {@code hasPendingMessage}, and
 * {@code maybeClearPendingMessages}; Cobalt wraps the map behind an instance
 * so each {@link com.github.auties00.cobalt.message.receive.MessageReceivingService}
 * can own its own cache and the storage uses a {@link ConcurrentHashMap} to
 * remain safe under virtual-thread fanout.
 */
@WhatsAppWebModule(moduleName = "WAWebMessageDedupUtils")
public final class MessageDedup {
    /**
     * Logger that mirrors the WA Web tagged-template log messages so that
     * existing log-aggregation patterns still apply.
     *
     * @implNote WAWebMessageDedupUtils: calls {@code WALogger.LOG} with
     * tagged template literals for add, pending, and clear events.
     */
    private static final System.Logger LOGGER = System.getLogger(MessageDedup.class.getName());

    /**
     * Map from composite dedup key to the current reference count.
     *
     * @implNote ADAPTED: WAWebMessageDedupUtils uses the plain module-level
     * {@code c = new Map} which is only safe because the WA Web runtime is
     * single-threaded. Cobalt substitutes a {@link ConcurrentHashMap} to
     * preserve the same semantics under parallel virtual-thread callers.
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = {"addPendingMessage", "hasPendingMessage", "maybeClearPendingMessages"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    private final ConcurrentMap<String, Integer> pending;

    /**
     * Constructs a new, empty dedup cache.
     *
     * @implNote WAWebMessageDedupUtils: the module-level cache is
     * initialised as {@code var c = new Map}. Cobalt uses an instance
     * field so multiple services can own their own dedup state.
     */
    public MessageDedup() {
        this.pending = new ConcurrentHashMap<>();
    }

    /**
     * Registers a message key as pending and returns its new reference
     * count.
     *
     * <p>A key that is not yet present is inserted with count {@code 1};
     * a key that is already present has its count atomically incremented by
     * one.
     *
     * @param key the composite dedup key produced by
     *            {@code createPendingMessageKey}
     * @return the new reference count for this key
     * @throws NullPointerException if {@code key} is {@code null}
     * @implNote WAWebMessageDedupUtils.addPendingMessage: retrieves the
     * current counter (defaulting to 0), adds 1, writes it back via
     * {@code c.set(key, count)}, and returns the new total. Logs
     * {@code "[message-dedup] add message: key, total: count"}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "addPendingMessage",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public int add(String key) {
        Objects.requireNonNull(key, "key");

        // WAWebMessageDedupUtils.addPendingMessage
        // Increments the reference count for this key atomically, inserting 1 for a new entry

        var newCount = pending.merge(key, 1, Integer::sum);

        // WAWebMessageDedupUtils.addPendingMessage
        // Logs the add event mirroring the JS tagged-template log message

        LOGGER.log(System.Logger.Level.DEBUG,
                "[message-dedup] add message: {0}, total: {1}", key, newCount);

        return newCount;
    }

    /**
     * Returns whether a message key is currently registered as pending.
     *
     * <p>When the key is present a debug log entry is emitted with the same
     * format as WA Web so diagnostic output can be correlated across the
     * two implementations.
     *
     * @param key the composite dedup key
     * @return {@code true} if the key has at least one outstanding
     *         reference
     * @throws NullPointerException if {@code key} is {@code null}
     * @implNote WAWebMessageDedupUtils.hasPendingMessage: calls
     * {@code c.get(key)}, returns {@code false} when the value is
     * {@code null}, and otherwise logs and returns {@code true}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "hasPendingMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean isPending(String key) {
        Objects.requireNonNull(key, "key");

        // WAWebMessageDedupUtils.hasPendingMessage
        // Looks up the reference count for this key, returning false when absent

        var count = pending.get(key);
        if (count == null) {
            return false;
        }

        // WAWebMessageDedupUtils.hasPendingMessage
        // Logs the pending hit mirroring the JS tagged-template log message

        LOGGER.log(System.Logger.Level.DEBUG,
                "[message-dedup] message {0} is pending, total: {1}", key, count);
        return true;
    }

    /**
     * Clears every entry when the supplied offline-delivery counter reaches
     * zero.
     *
     * <p>The WA Web contract is that the entire cache is invalidated when
     * the offline-delivery phase ends: any message id that was pending
     * during the replay window is no longer at risk of being duplicated, so
     * the memory is released in bulk rather than per entry.
     *
     * @param count the current offline-delivery counter; the cache is
     *              cleared only when this is exactly zero
     * @implNote WAWebMessageDedupUtils.maybeClearPendingMessages: checks
     * {@code e === 0}, logs
     * {@code "[message-dedup] message cache cleared, total: size"} when
     * {@code c.size > 0}, and then calls {@code c.clear()}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "maybeClearPendingMessages",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void maybeClear(int count) {
        // WAWebMessageDedupUtils.maybeClearPendingMessages
        // Clears the cache only when the offline-delivery counter has reached zero

        if (count == 0) {
            if (!pending.isEmpty()) {
                // WAWebMessageDedupUtils.maybeClearPendingMessages
                // Logs the clear event with the pre-clear size, matching the JS format

                LOGGER.log(System.Logger.Level.DEBUG,
                        "[message-dedup] message cache cleared, total: {0}", pending.size());
            }
            pending.clear();
        }
    }

    /**
     * Unconditionally clears every entry from the cache.
     *
     * <p>Provided as a convenience for callers that have already verified
     * the offline-delivery counter externally and want to drop the cache
     * without rechecking. Most callers should prefer
     * {@link #maybeClear(int)}.
     */
    public void clear() {
        // Convenience helper that reuses maybeClear with a zero counter

        maybeClear(0);
    }

    /**
     * Decrements the reference count for a message key and removes the
     * entry once the count reaches zero.
     *
     * <p>This method has no WA Web counterpart: the JS module only evicts
     * entries in bulk via {@code maybeClearPendingMessages}. Cobalt's
     * send-side dedup pattern registers a key before the send and removes
     * it once the send completes, so per-entry removal is required.
     *
     * @param key the composite dedup key
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public void remove(String key) {
        Objects.requireNonNull(key, "key");

        // Atomically decrements the counter; removes the entry when the counter reaches zero

        pending.compute(key, (_, count) -> {
            if (count == null) {
                return null;
            }
            var decremented = count - 1;
            return decremented <= 0 ? null : decremented;
        });
    }

    /**
     * Returns the number of distinct message keys currently registered.
     *
     * @return the cache size
     * @implNote WAWebMessageDedupUtils.maybeClearPendingMessages: reads
     * {@code c.size} inline for the clear-log message. Cobalt exposes the
     * value as an instance method so callers can use it for diagnostics.
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "maybeClearPendingMessages",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public int size() {
        return pending.size();
    }
}
