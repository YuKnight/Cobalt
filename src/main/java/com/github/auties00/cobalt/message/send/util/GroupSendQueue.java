package com.github.auties00.cobalt.message.send.util;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Queue for serializing message sends per group using per-group locks.
 * <p>
 * Per WhatsApp Web WAWebSendMsgQueueMap: ensures messages to the same group
 * are sent in order, preventing race conditions in sender key distribution.
 * <p>
 * With virtual threads, blocking on a lock is cheap and idiomatic.
 *
 * @apiNote WAWebSendMsgQueueMap.sendMsgQueueMap
 */
public final class GroupSendQueue {
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * Executes an operation while holding the lock for the specified group.
     *
     * @param <T>       the return type
     * @param groupJid  the group JID
     * @param operation the operation to execute
     * @return the operation result
     */
    public <T> T execute(Jid groupJid, Supplier<T> operation) {
        var lock = locks.computeIfAbsent(groupJid.toString(), k -> new ReentrantLock());
        lock.lock();
        try {
            return operation.get();
        } finally {
            lock.unlock();
        }
    }
}
