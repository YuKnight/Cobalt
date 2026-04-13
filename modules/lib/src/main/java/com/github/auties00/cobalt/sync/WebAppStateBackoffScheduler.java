package com.github.auties00.cobalt.sync;

import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.util.SchedulerUtils;

import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Schedules retries for failed app state sync operations using exponential backoff.
 *
 * <p>Per WhatsApp Web, this scheduler uses a single global attempt counter ({@code W})
 * shared across all collections, rather than per-collection counters. The backoff delay
 * is computed as {@code BASE_DELAY_MS * 2^attempt}, capped at {@code MAX_DELAY_MS}.
 * A sticky server-suggested backoff floor ({@code q}) persists across retries and is
 * only updated when a new server backoff value is explicitly provided.
 *
 * <p>If the elapsed time since the first failure exceeds the finite failure expiry
 * window ({@value #FINITE_FAILURE_EXPIRY_MS} ms), the retry is rejected and the
 * collection should be moved to fatal state.
 *
 * @implNote WAWebSyncd (te, ne, ae — retry scheduling and backoff computation;
 *     W — global attempt counter; q — sticky server backoff floor; O — setTimeout handle),
 *     WASyncdConst (BACKOFF_MIN_TIMEOUT = 1e3, BACKOFF_MAX_TIMEOUT = 1e3*60*60,
 *     BACKOFF_BASE = 2, FINITE_FAILURE_EXPIRY_DURATION = DAY_MILLISECONDS * 2),
 *     WAWebSyncdCollectionsStateMachine (getExpiredCollections — expiry check logic)
 */
public final class WebAppStateBackoffScheduler implements Closeable {
    /**
     * Minimum backoff delay in milliseconds.
     *
     * @implNote WASyncdConst.BACKOFF_MIN_TIMEOUT (s = 1e3)
     */
    private static final long BASE_DELAY_MS = 1000; // WASyncdConst.BACKOFF_MIN_TIMEOUT (s = 1e3)

    /**
     * Maximum backoff delay in milliseconds (1 hour).
     *
     * @implNote WASyncdConst.BACKOFF_MAX_TIMEOUT (u = 1e3 * 60 * 60)
     */
    private static final long MAX_DELAY_MS = 3_600_000; // WASyncdConst.BACKOFF_MAX_TIMEOUT (u = 1e3*60*60)

    /**
     * Exponential backoff base multiplier.
     *
     * @implNote WASyncdConst.BACKOFF_BASE (c = 2)
     */
    private static final int MULTIPLIER = 2; // WASyncdConst.BACKOFF_BASE (c = 2)

    /**
     * Maximum duration (in milliseconds) that a collection may remain in finite retry
     * state before being considered expired and moved to fatal.
     *
     * @implNote WASyncdConst.FINITE_FAILURE_EXPIRY_DURATION (d = DAY_MILLISECONDS * 2 = 172800000)
     */
    private static final long FINITE_FAILURE_EXPIRY_MS = 2 * 24 * 60 * 60 * 1000L; // WASyncdConst.FINITE_FAILURE_EXPIRY_DURATION (d = DAY_MILLISECONDS * 2)

    /**
     * Per-collection pending retry futures, replacing WA Web's single global timeout handle.
     *
     * @implNote ADAPTED: WAWebSyncd.O (single setTimeout handle) mapped to per-collection map
     *     for concurrent collection-level retries on virtual threads
     */
    private final ConcurrentHashMap<SyncPatchType, CompletableFuture<?>> pendingRetries; // ADAPTED: WAWebSyncd.O (single setTimeout handle -> per-collection map)

    /**
     * Global attempt counter shared across all collections.
     *
     * @implNote WAWebSyncd.W (var W = 0)
     */
    private final AtomicInteger globalAttemptCounter; // WAWebSyncd.W (var W = 0)

    /**
     * Sticky server-suggested backoff floor in milliseconds. This value persists across
     * retries and is only updated when a new server backoff is explicitly provided via
     * {@link #updateServerBackoff(long)}.
     *
     * @implNote WAWebSyncd.q (var q = 0 — module-level sticky server backoff)
     */
    private final AtomicLong stickyServerBackoffMs; // WAWebSyncd.q (var q = 0)

    /**
     * Constructs a new {@code WebAppStateBackoffScheduler} with zeroed counters.
     *
     * @implNote WAWebSyncd module initialization (var W = 0, q = 0, O = undefined)
     */
    public WebAppStateBackoffScheduler() {
        this.pendingRetries = new ConcurrentHashMap<>();
        this.globalAttemptCounter = new AtomicInteger(0);
        this.stickyServerBackoffMs = new AtomicLong(0);
    }

    /**
     * Schedules a retry with exponential backoff using the global attempt counter,
     * optionally updating the sticky server backoff floor.
     *
     * <p>If {@code serverBackoffMs} is non-null, the sticky server backoff floor is updated
     * to that value before computing the delay. This preserves backward compatibility with
     * callers that pass the server backoff directly.
     *
     * @implNote WAWebSyncd.te (retry scheduling), WAWebSyncd.ne (backoff computation),
     *     WAWebSyncd.ee (q = serverBackoff when ErrorRetry),
     *     WAWebSyncdCollectionsStateMachine.getExpiredCollections (expiry check)
     * @param collectionName       the collection to retry
     * @param firstFailureTimestamp the timestamp of the first failure in this series
     * @param serverBackoffMs      the server-suggested backoff in milliseconds, or {@code null}
     *                             to use the existing sticky value
     * @param retryAction          the action to execute on retry
     * @return {@code true} if the retry was scheduled, {@code false} if the failure window expired
     */
    public boolean scheduleRetry(SyncPatchType collectionName, long firstFailureTimestamp, Long serverBackoffMs, Runnable retryAction) {
        if (serverBackoffMs != null) {
            stickyServerBackoffMs.set(serverBackoffMs); // WAWebSyncd.ee: q = i[0].serverBackoff || 0
        }
        return scheduleRetry(collectionName, firstFailureTimestamp, retryAction);
    }

    /**
     * Schedules a retry with exponential backoff using the global attempt counter.
     *
     * <p>The global attempt counter is incremented on each call. The delay is computed
     * using the current attempt number and the sticky server backoff floor. If the
     * elapsed time since the first failure exceeds the finite failure expiry window,
     * the retry is rejected and this method returns {@code false}.
     *
     * @implNote WAWebSyncd.te (retry scheduling), WAWebSyncd.ne (backoff computation),
     *     WAWebSyncdCollectionsStateMachine.getExpiredCollections (expiry check)
     * @param collectionName       the collection to retry
     * @param firstFailureTimestamp the timestamp of the first failure in this series
     * @param retryAction          the action to execute on retry
     * @return {@code true} if the retry was scheduled, {@code false} if the failure window expired
     */
    public boolean scheduleRetry(SyncPatchType collectionName, long firstFailureTimestamp, Runnable retryAction) {
        // WAWebSyncdCollectionsStateMachine.getExpiredCollections: check finiteFailureStartTime + FINITE_FAILURE_EXPIRY_DURATION < unixTimeMs()
        var elapsed = System.currentTimeMillis() - firstFailureTimestamp;
        if (elapsed > FINITE_FAILURE_EXPIRY_MS) { // WAWebSyncdCollectionsStateMachine.getExpiredCollections
            return false;
        }

        // ADAPTED: WAWebSyncd.ae: clearTimeout(O) before calling te()
        cancelRetry(collectionName);

        // WAWebSyncd.te: ne(W, q) computes delay using current W, then W += 1 inside callback
        var attempt = globalAttemptCounter.getAndIncrement(); // WAWebSyncd.W
        var delayMs = calculateBackoff(attempt, stickyServerBackoffMs.get()); // WAWebSyncd.ne(W, q)

        // WAWebSyncd.te: O = setTimeout(..., ne(W, q))
        var future = SchedulerUtils.scheduleDelayed(Duration.ofMillis(delayMs), () -> {
            pendingRetries.remove(collectionName);
            retryAction.run();
        });
        pendingRetries.put(collectionName, future);

        return true;
    }

    /**
     * Updates the sticky server backoff floor and resets the global attempt counter.
     *
     * <p>Per WhatsApp Web, when the server returns an {@code ErrorRetry} result with a
     * backoff value, the module-level {@code q} variable is updated and the attempt
     * counter {@code W} is reset to 0. The updated {@code q} value persists across
     * subsequent retries until explicitly updated again.
     *
     * @implNote WAWebSyncd.ee: {@code i.length > 0 && (q = i[0].serverBackoff || 0, W = 0)}
     * @param serverBackoffMs the server-suggested backoff floor in milliseconds
     */
    public void updateServerBackoff(long serverBackoffMs) {
        stickyServerBackoffMs.set(serverBackoffMs); // WAWebSyncd.ee: q = i[0].serverBackoff || 0
        globalAttemptCounter.set(0); // WAWebSyncd.ee: W = 0
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
     * @implNote WAWebSyncd.ae: {@code O != null && clearTimeout(O)}
     * @param collectionName the collection whose retry to cancel
     * @return {@code true} if a pending retry was cancelled, {@code false} otherwise
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
     * Resets the global attempt counter without changing the sticky server backoff.
     *
     * <p>Should be called when a sync succeeds to reset the backoff progression.
     * Note that this only resets {@code W}; the sticky server backoff {@code q}
     * is preserved and will continue to be used as a floor for future retries.
     *
     * @implNote WAWebSyncd.ee: success path resets W indirectly by completing without error;
     *     WAWebSyncdServerSync.S: success clears retry state
     */
    public void resetAttemptCounter() {
        globalAttemptCounter.set(0); // WAWebSyncd: W = 0
    }

    /**
     * Closes this scheduler, cancelling all pending retries and resetting all state.
     *
     * @implNote ADAPTED: no direct WA Web equivalent; Cobalt lifecycle cleanup
     */
    @Override
    public void close() {
        for (var future : pendingRetries.values()) {
            future.cancel(true);
        }
        pendingRetries.clear();
        globalAttemptCounter.set(0);
        stickyServerBackoffMs.set(0); // NO_WA_BASIS: cleanup for Cobalt lifecycle
    }
}
