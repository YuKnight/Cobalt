package com.github.auties00.cobalt.message.dedup;

import com.github.auties00.cobalt.message.receive.stanza.MessageReceiveEncryptedPayload;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.message.MessageKey;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory cache of in-flight message keys used to suppress duplicate send
 * or receive attempts during the offline-delivery replay window.
 *
 * <p>When WhatsApp reconnects it redelivers every message that was not
 * acknowledged before the disconnect. Without a guard the same message would
 * be processed twice: once from the replay stream and once from any queued
 * stanza that had already arrived during the previous session. The dedup
 * cache stores a composite key for every message currently being processed.
 * A caller can ask whether a key is pending via {@link #isPending(String)}
 * before spending work on it and then register it via {@link #add(String)}
 * to take ownership.
 *
 * <p>Entries hold a reference count so a single key can be registered by
 * multiple cooperating call sites without being prematurely evicted. The
 * cache is flushed in bulk via {@link #maybeClear(int)} when the
 * offline-delivery counter reaches zero.
 */
@WhatsAppWebModule(moduleName = "WAWebMessageDedupUtils")
public final class MessageDedup {
    /**
     * Logger used for the dedup add, pending-hit, and clear events.
     */
    private static final System.Logger LOGGER = System.getLogger(MessageDedup.class.getName());

    /**
     * Map from composite dedup key to the current reference count.
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = {"addPendingMessage", "hasPendingMessage", "maybeClearPendingMessages"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    private final ConcurrentMap<String, Integer> pending;

    /**
     * Constructs a new, empty dedup cache.
     */
    public MessageDedup() {
        this.pending = new ConcurrentHashMap<>();
    }

    /**
     * Returns whether the pending-message cache is currently enabled by the
     * server-side AB prop.
     *
     * <p>WA Web gates every call to {@code addPendingMessage} behind this
     * check so servers can roll the dedup feature back without a client
     * redeploy. Cobalt callers should do the same. They check this predicate
     * before invoking {@link #add(String)} or
     * {@link #add(MessageKey, Instant, List)}.
     * @param abPropsService the AB props service used to read the
     *                       {@link ABProp#WEB_PENDING_MESSAGE_CACHE_ENABLED} flag
     * @return {@code true} when the server has flipped the
     *         {@code web_pending_message_cache_enabled} AB prop on
     * @throws NullPointerException if {@code abPropsService} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "isPengingMessageCacheEnabled",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static boolean isCacheEnabled(ABPropsService abPropsService) {
        Objects.requireNonNull(abPropsService, "abPropsService");
        return abPropsService.getBool(ABProp.WEB_PENDING_MESSAGE_CACHE_ENABLED);
    }

    /**
     * Registers a message key as pending and returns its new reference count.
     *
     * <p>A key that is not yet present is inserted with count {@code 1}. A
     * key that is already present has its count atomically incremented by
     * one.
     *
     * @param key the composite dedup key produced by
     *            {@link PendingMessageKey#create(MessageKey, Instant, List)}
     * @return the new reference count for this key
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "addPendingMessage",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public int add(String key) {
        Objects.requireNonNull(key, "key");

        var newCount = pending.merge(key, 1, Integer::sum);

        LOGGER.log(System.Logger.Level.DEBUG,
                "[message-dedup] add message: {0}, total: {1}", key, newCount);

        return newCount;
    }

    /**
     * Registers an incoming message as pending using the canonical composite
     * key derived from the message key, timestamp, and encrypted-payload
     * list, and returns its new reference count.
     *
     * @param key       the logical message key
     * @param timestamp the message timestamp, serialised as epoch seconds
     * @param encs      the list of encrypted payloads carried on the incoming
     *                  {@code <message>} stanza
     * @return the new reference count for the composite key
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "addPendingMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    public int add(MessageKey key, Instant timestamp, List<MessageReceiveEncryptedPayload> encs) {
        return add(PendingMessageKey.create(key, timestamp, encs));
    }

    /**
     * Returns whether a message key is currently registered as pending.
     *
     * <p>When the key is present a debug log entry is emitted with the same
     * format as WA Web so diagnostic output can be correlated across the two
     * implementations.
     *
     * @param key the composite dedup key
     * @return {@code true} if the key has at least one outstanding reference
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "hasPendingMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean isPending(String key) {
        Objects.requireNonNull(key, "key");

        var count = pending.get(key);
        if (count == null) {
            return false;
        }

        LOGGER.log(System.Logger.Level.DEBUG,
                "[message-dedup] message {0} is pending, total: {1}", key, count);
        return true;
    }

    /**
     * Returns whether an incoming message is already registered as pending,
     * using the canonical composite key derived from the message key,
     * timestamp, and encrypted-payload list.
     *
     * @param key       the logical message key
     * @param timestamp the message timestamp, serialised as epoch seconds
     * @param encs      the list of encrypted payloads carried on the incoming
     *                  {@code <message>} stanza
     * @return {@code true} if the composite key is already registered
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "hasPendingMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean isPending(MessageKey key, Instant timestamp, List<MessageReceiveEncryptedPayload> encs) {
        return isPending(PendingMessageKey.create(key, timestamp, encs));
    }

    /**
     * Clears every entry when the supplied offline-delivery counter reaches
     * zero.
     *
     * <p>Any message id that was pending during the replay window is no
     * longer at risk of being duplicated once the offline-delivery phase
     * ends, so the memory is released in bulk rather than per entry.
     *
     * @param count the current offline-delivery counter; the cache is cleared
     *              only when this is exactly zero
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "maybeClearPendingMessages",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void maybeClear(int count) {
        if (count == 0) {
            if (!pending.isEmpty()) {
                LOGGER.log(System.Logger.Level.DEBUG,
                        "[message-dedup] message cache cleared, total: {0}", pending.size());
            }
            pending.clear();
        }
    }

    /**
     * Unconditionally clears every entry from the cache.
     *
     * @apiNote Provided as a convenience for callers that have already
     * verified the offline-delivery counter externally and want to drop the
     * cache without rechecking. Most callers should prefer
     * {@link #maybeClear(int)}.
     */
    public void clear() {
        maybeClear(0);
    }

    /**
     * Decrements the reference count for a message key and removes the entry
     * once the count reaches zero.
     * @param key the composite dedup key
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public void remove(String key) {
        Objects.requireNonNull(key, "key");

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
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "maybeClearPendingMessages",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public int size() {
        return pending.size();
    }
}
