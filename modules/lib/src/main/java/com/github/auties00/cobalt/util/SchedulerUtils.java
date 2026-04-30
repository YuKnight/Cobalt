package com.github.auties00.cobalt.util;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Helper for firing a one-shot task on a virtual thread after a delay
 * without pulling in a dedicated scheduler.
 *
 * @implNote Leans on
 *     {@link CompletableFuture#delayedExecutor(long, TimeUnit, java.util.concurrent.Executor)}
 *     and {@link Thread#startVirtualThread(Runnable)}. WhatsApp Web uses
 *     browser timers instead.
 */
public final class SchedulerUtils {
    /**
     * Prevents instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private SchedulerUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Schedules the given task to run on a fresh virtual thread after the
     * specified delay has elapsed.
     *
     * @param delay the amount of time to wait before running the task
     * @param task  the task to execute
     * @return a future that completes when {@code task} finishes
     */
    public static CompletableFuture<Void> scheduleDelayed(Duration delay, Runnable task) {
        var delayedExecutor = CompletableFuture.delayedExecutor(delay.toNanos(), TimeUnit.NANOSECONDS, Thread::startVirtualThread);
        return CompletableFuture.runAsync(task, delayedExecutor);
    }
}
