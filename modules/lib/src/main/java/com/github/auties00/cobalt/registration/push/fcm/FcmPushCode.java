package com.github.auties00.cobalt.registration.push.fcm;

import java.io.IOException;

/**
 * Single-value, set-once synchronisation primitive used to hand the
 * verification code received over the FCM MCS stream to the caller of
 * {@link FcmClient#getPushCode()}.
 *
 * <p>The producer ({@link FcmMcsConnection}) calls {@link #deliver}
 * once a {@code registration_code} entry is observed in an incoming
 * {@code app_data} payload. The consumer ({@link FcmClient}) blocks in
 * {@link #waitForCode()} until either the value is delivered or
 * {@link #close()} is invoked.
 *
 * <p>The implementation uses plain {@code synchronized} + {@code wait}
 * / {@code notifyAll} rather than {@link java.util.concurrent.CompletableFuture}
 * or {@link java.util.concurrent.locks.ReentrantLock}: JEP 491 (JDK
 * 24) removed carrier-thread pinning on {@code synchronized}, which
 * makes wait/notify fully virtual-thread friendly and cheaper than the
 * lock-based alternatives.
 */
final class FcmPushCode {
    /**
     * Lock guarding {@link #code} and {@link #closed}. A dedicated
     * monitor (rather than {@code synchronized (this)}) hides the
     * locking strategy from callers that may inadvertently synchronise
     * on the holder.
     */
    private final Object lock;

    /**
     * The verification code value once delivered. {@code null} until
     * the first {@link #deliver(String)} call lands. Stays set for the
     * lifetime of the holder so a code arriving before
     * {@link #waitForCode()} is called is not lost.
     */
    private String code;

    /**
     * Set by {@link #close()} so any thread parked in
     * {@link #waitForCode()} unblocks and surfaces an
     * {@link IOException} rather than waiting forever.
     */
    private boolean closed;

    /**
     * Constructs an empty holder. The caller is expected to publish
     * the instance to its single producer and one or more consumers
     * via a happens-before edge (typically by storing it in a
     * {@code final} field on a containing object).
     */
    FcmPushCode() {
        this.lock = new Object();
    }

    /**
     * Blocks the calling thread until either {@link #deliver(String)}
     * stores a value or {@link #close()} releases the waiters.
     * Returns immediately if a value was already delivered before the
     * call.
     *
     * <p>Safe to call from multiple threads concurrently. Every
     * caller observes the same delivered value.
     *
     * @return the delivered verification code
     * @throws InterruptedException if the caller is interrupted while
     *                              waiting
     * @throws IOException          if the holder was closed before any
     *                              code arrived
     */
    String waitForCode() throws InterruptedException, IOException {
        synchronized (lock) {
            while (code == null && !closed) {
                lock.wait();
            }
            if (code == null) {
                throw new IOException("FcmPushCode is closed");
            }
            return code;
        }
    }

    /**
     * Stores the first verification code seen and wakes every waiter
     * blocked in {@link #waitForCode()}. Subsequent invocations are
     * no-ops because WhatsApp's registration flow only ever sends one
     * code per session, and replays after MCS reconnect must surface
     * the original value rather than overwrite it.
     *
     * @param code the verification code value, or {@code null} if the
     *             {@code app_data} entry carried no value (in which
     *             case the call is silently dropped)
     */
    void deliver(String code) {
        if (code == null) {
            return;
        }
        synchronized (lock) {
            if (this.code == null) {
                this.code = code;
                lock.notifyAll();
            }
        }
    }

    /**
     * Marks the holder closed and wakes every pending waiter so they
     * can observe the close and throw. Idempotent.
     */
    void close() {
        synchronized (lock) {
            if (closed) {
                return;
            }
            closed = true;
            lock.notifyAll();
        }
    }
}
