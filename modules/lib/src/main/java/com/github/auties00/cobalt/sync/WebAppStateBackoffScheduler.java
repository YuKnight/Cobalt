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
 */
public final class WebAppStateBackoffScheduler implements Closeable {
    /**
     * Minimum backoff delay in milliseconds.
     */
    private static final long BASE_DELAY_MS = 1000;

    /**
     * Maximum backoff delay in milliseconds (1 hour).
     */
    private static final long MAX_DELAY_MS = 3_600_000;

    /**
     * Exponential backoff base multiplier.
     */
    private static final int MULTIPLIER = 2;

    /**
     * Maximum duration (in milliseconds) that a collection may remain in finite retry
     * state before being considered expired and moved to fatal.
     */
    private static final long FINITE_FAILURE_EXPIRY_MS = 2 * 24 * 60 * 60 * 1000L;

    /**
     * Per-collection pending retry futures, replacing WA Web's single global timeout handle.
     */
    private final ConcurrentHashMap<SyncPatchType, CompletableFuture<?>> pendingRetries;

    /**
     * Global attempt counter shared across all collections.
     */
    private final AtomicInteger globalAttemptCounter;

    /**
     * Sticky server-suggested backoff floor in milliseconds. This value persists across
     * retries and is only updated when a new server backoff is explicitly provided via
     * {@link #updateServerBackoff(long)}.
     */
    private final AtomicLong stickyServerBackoffMs;

    /**
     * Constructs a new {@code WebAppStateBackoffScheduler} with zeroed counters.
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
     * <p>When {@code serverBackoffMs} is non-null the sticky server backoff floor is
     * updated to that value before computing the delay, preserving backward
     * compatibility with callers that pass the server backoff directly.
     *
     * @param collectionName        the collection to retry
     * @param firstFailureTimestamp the timestamp of the first failure in this series
     * @param serverBackoffMs       the server-suggested backoff in milliseconds, or
     *                              {@code null} to keep the existing sticky value
     * @param retryAction           the action to execute on retry
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
     * using the current attempt number and the sticky server backoff floor. When the
     * elapsed time since the first failure exceeds the finite failure expiry window
     * the retry is rejected and this method returns {@code false}.
     *
     * @param collectionName        the collection to retry
     * @param firstFailureTimestamp the timestamp of the first failure in this series
     * @param retryAction           the action to execute on retry
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
     * <p>When the server returns an {@code ErrorRetry} result with a backoff value, the
     * module-level {@code q} variable is updated and the attempt counter {@code W} is
     * reset to {@code 0}. The updated {@code q} value persists across subsequent retries
     * until explicitly updated again.
     *
     * @param serverBackoffMs the server-suggested backoff floor in milliseconds
     */
    public void updateServerBackoff(long serverBackoffMs) {
        stickyServerBackoffMs.set(serverBackoffMs);
        globalAttemptCounter.set(0);
    }

    /**
     * Computes the backoff delay for a given attempt number and server-suggested floor.
     *
     * <p>The formula is
     * {@code min(max(pow(BASE, attempt) * MIN_TIMEOUT, serverBackoff), MAX_TIMEOUT)}.
     * The server backoff is used as a floor but the final result is always capped
     * at {@link #MAX_DELAY_MS}.
     *
     * @param attemptNumber the current retry attempt (0-based)
     * @param serverBackoff the server-suggested backoff floor in milliseconds
     * @return the computed delay in milliseconds
     */
    private long calculateBackoff(int attemptNumber, long serverBackoff) {
        var computed = (long) (BASE_DELAY_MS * Math.pow(MULTIPLIER, attemptNumber));
        var delay = Math.max(computed, serverBackoff);
        return Math.min(delay, MAX_DELAY_MS);
    }

    /**
     * Cancels a pending retry for the specified collection.
     *
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
     * <p>Callers invoke this on a successful sync to reset the backoff progression.
     * Only {@code W} is reset, the sticky server backoff {@code q} is preserved and
     * continues to be used as a floor for future retries.
     */
    public void resetAttemptCounter() {
        globalAttemptCounter.set(0);
    }

    /**
     * Closes this scheduler, cancelling all pending retries and resetting all state.
     */
    @Override
    public void close() {
        for (var future : pendingRetries.values()) {
            future.cancel(true);
        }
        pendingRetries.clear();
        globalAttemptCounter.set(0);
        stickyServerBackoffMs.set(0);
    }
}
