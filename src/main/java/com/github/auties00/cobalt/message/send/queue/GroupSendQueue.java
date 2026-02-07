package com.github.auties00.cobalt.message.send.queue;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A per-key serial execution queue that ensures group messages targeting the
 * same chat are encrypted and sent one at a time.
 *
 * <p>WhatsApp's sender-key protocol requires that group messages be serialised
 * per group: each message's sender-key ciphertext counter must increase
 * monotonically, so concurrent encryption for the same group would produce
 * out-of-order counters and decryption failures on the receiving side.
 *
 * <p>Each unique key (typically the group JID string) gets its own
 * {@link ReentrantLock}.  Callers block until the previous task for that
 * key completes, but tasks for different keys execute concurrently.
 *
 * @apiNote WAWebSendMsgQueueMap: a {@code PromiseQueueMap} instance that
 * serialises group message sends per group JID string.
 * WAWebSendGroupMsgJob.encryptAndSendGroupMsg: enqueues the send task via
 * {@code sendMsgQueueMap.enqueue(to.toString(), ...)}.
 */
public final class GroupSendQueue {
    /**
     * Map from queue key to its associated lock.
     * Locks are created lazily and retained for the lifetime of the queue.
     */
    private final ConcurrentMap<String, ReentrantLock> locks;

    /**
     * Creates a new, empty group send queue.
     */
    public GroupSendQueue() {
        this.locks = new ConcurrentHashMap<>();
    }

    /**
     * Executes the given task while holding the lock for {@code key},
     * ensuring mutual exclusion with other tasks enqueued under the same key.
     *
     * <p>Tasks enqueued under different keys may run concurrently.
     *
     * @param <T>  the result type
     * @param key  the queue key (typically the group JID string)
     * @param task the task to execute
     * @return the result produced by {@code task}
     * @throws NullPointerException if any argument is {@code null}
     * @throws Exception if {@code task} throws
     *
     * @apiNote WAWebSendMsgQueueMap.sendMsgQueueMap.enqueue: serialises
     * the send task per group JID string key.
     */
    public <T> T enqueue(String key, Callable<T> task) throws Exception {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(task, "task");
        var lock = locks.computeIfAbsent(key, _ -> new ReentrantLock());
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes the given task while holding the lock for {@code key}.
     *
     * <p>This is a convenience overload for tasks that do not return a value.
     *
     * @param key  the queue key (typically the group JID string)
     * @param task the task to execute
     * @throws NullPointerException if any argument is {@code null}
     * @throws Exception if {@code task} throws
     *
     * @apiNote WAWebSendMsgQueueMap.sendMsgQueueMap.enqueue
     */
    public void enqueue(String key, Runnable task) throws Exception {
        Objects.requireNonNull(task, "task");
        enqueue(key, () -> {
            task.run();
            return null;
        });
    }
}
