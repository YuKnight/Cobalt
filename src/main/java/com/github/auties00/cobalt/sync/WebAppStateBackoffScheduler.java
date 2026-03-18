package com.github.auties00.cobalt.sync;

import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.util.SchedulerUtils;

import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Schedules retries for failed app state sync operations using exponential backoff.
 *
 * <p>Per WhatsApp Web, this scheduler uses a single global attempt counter shared
 * across all collections, rather than per-collection counters. The backoff delay
 * is computed as {@code BASE_DELAY_MS * 2^attempt}, capped at {@code MAX_DELAY_MS}.
 *
 * <p>If the elapsed time since the first failure exceeds the finite failure expiry
 * window ({@value #FINITE_FAILURE_EXPIRY_MS} ms), the retry is rejected.
 *
 * @implNote WAWebSyncd (te, ne, W, q variables), WASyncdConst (BACKOFF_MIN_TIMEOUT,
 *     BACKOFF_MAX_TIMEOUT, BACKOFF_BASE, FINITE_FAILURE_EXPIRY_DURATION),
 *     WAWebSyncdCollectionsStateMachine (getExpiredCollections)
 */
public final class WebAppStateBackoffScheduler implements Closeable {
    private static final long BASE_DELAY_MS = 1000; // WASyncdConst.BACKOFF_MIN_TIMEOUT (s = 1e3)
    private static final long MAX_DELAY_MS = 3_600_000; // WASyncdConst.BACKOFF_MAX_TIMEOUT (u = 1e3*60*60)
    private static final int MULTIPLIER = 2; // WASyncdConst.BACKOFF_BASE (c = 2)
    private static final long FINITE_FAILURE_EXPIRY_MS = 2 * 24 * 60 * 60 * 1000L; // WASyncdConst.FINITE_FAILURE_EXPIRY_DURATION (d = DAY_MILLISECONDS * 2)

    private final ConcurrentHashMap<SyncPatchType, CompletableFuture<?>> pendingRetries; // ADAPTED: WAWebSyncd.O (single setTimeout handle → per-collection map)
    private final AtomicInteger globalAttemptCounter; // WAWebSyncd.W (var W = 0)

    /**
     * Constructs a new {@code WebAppStateBackoffScheduler}.
     */
    public WebAppStateBackoffScheduler() {
        this.pendingRetries = new ConcurrentHashMap<>();
        this.globalAttemptCounter = new AtomicInteger(0);
    }

    /**
     * Schedules a retry with exponential backoff using the global attempt counter.
     *
     * <p>The global attempt counter is incremented on each call. If a server-suggested
     * backoff is provided, the actual delay is the maximum of the computed backoff and
     * the server suggestion.
     *
     * @implNote WAWebSyncd.te, WAWebSyncdCollectionsStateMachine.getExpiredCollections
     * @param collectionName       the collection to retry
     * @param firstFailureTimestamp the timestamp of the first failure in this series
     * @param serverBackoffMs      the server-suggested backoff in milliseconds, or {@code null}
     * @param retryAction          the action to execute on retry
     * @return {@code true} if the retry was scheduled, {@code false} if the failure window expired
     */
    public boolean scheduleRetry(SyncPatchType collectionName, long firstFailureTimestamp, Long serverBackoffMs, Runnable retryAction) {
        // WAWebSyncdCollectionsStateMachine.getExpiredCollections: check finiteFailureStartTime + FINITE_FAILURE_EXPIRY_DURATION < unixTimeMs()
        var elapsed = System.currentTimeMillis() - firstFailureTimestamp;
        if (elapsed > FINITE_FAILURE_EXPIRY_MS) { // WAWebSyncdCollectionsStateMachine.getExpiredCollections
            return false;
        }

        // ADAPTED: WAWebSyncd.ae: clearTimeout(O) before calling te()
        cancelRetry(collectionName);

        // WAWebSyncd.te: ne(W, q) computes delay using current W, then W += 1 inside callback
        var attempt = globalAttemptCounter.getAndIncrement(); // WAWebSyncd.W
        var delayMs = calculateBackoff(attempt, serverBackoffMs != null ? serverBackoffMs : 0); // WAWebSyncd.ne(W, q)

        // WAWebSyncd.te: O = setTimeout(..., ne(W, q))
        var future = SchedulerUtils.scheduleDelayed(Duration.ofMillis(delayMs), () -> {
            pendingRetries.remove(collectionName);
            retryAction.run();
        });
        pendingRetries.put(collectionName, future);

        return true;
    }

    /**
     * Computes the backoff delay for a given attempt number and server-suggested floor.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncd.ne}: the formula is
     * {@code min(max(pow(BASE, attempt) * MIN_TIMEOUT, serverBackoff), MAX_TIMEOUT)}.
     * The server backoff is used as a floor but the final result is always capped
     * at {@code MAX_DELAY_MS}.
     *
     * @implNote WAWebSyncd.ne
     * @param attemptNumber the current retry attempt (0-based)
     * @param serverBackoff the server-suggested backoff floor in milliseconds
     * @return the computed delay in milliseconds
     */
    private long calculateBackoff(int attemptNumber, long serverBackoff) {
        var computed = (long) (BASE_DELAY_MS * Math.pow(MULTIPLIER, attemptNumber)); // WAWebSyncd.ne: Math.pow(w, e) * N

        var delay = Math.max(computed, serverBackoff); // WAWebSyncd.ne: Math.max(n, t)

        return Math.min(delay, MAX_DELAY_MS); // WAWebSyncd.ne: Math.min(..., M)
    }

    /**
     * Cancels a pending retry for the specified collection.
     *
     * @implNote WAWebSyncd.ae (clearTimeout(O))
     * @param collectionName the collection whose retry to cancel
     * @return {@code true} if a pending retry was cancelled
     */
    public boolean cancelRetry(SyncPatchType collectionName) {
        var future = pendingRetries.remove(collectionName);
        if (future != null) {
            future.cancel(false);
            return true;
        }
        return false;
    }

    /**
     * Resets the global attempt counter.
     *
     * <p>Should be called when a sync succeeds or on reconnect to reset the
     * backoff progression.
     *
     * @implNote WAWebSyncd.ee (W = 0 on ErrorRetry results from sync)
     */
    public void resetAttemptCounter() {
        globalAttemptCounter.set(0);
    }

    @Override
    public void close() {
        for (var future : pendingRetries.values()) {
            future.cancel(true);
        }
        pendingRetries.clear();
        globalAttemptCounter.set(0);
    }
}
