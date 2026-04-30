package com.github.auties00.cobalt.wam;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.wam.model.WamEventSpec;

import java.util.concurrent.CompletableFuture;

/**
 * A pending event awaiting flush, paired with its commit timestamp and
 * an optional flush completion future.
 *
 * @param event             the WAM event
 * @param commitTimeSeconds the Unix epoch seconds at which the event
 *                          was committed
 * @param flushFuture       a future completed when the buffer
 *                          containing this event is flushed, or
 *                          {@code null} for fire-and-forget commits
 */
@WhatsAppWebModule(moduleName = "WAWebWam")
record WamPendingEvent(WamEventSpec event, long commitTimeSeconds, CompletableFuture<Void> flushFuture) {
    /**
     * Constructs a fire-and-forget pending event with no flush future.
     *
     * @param event             the WAM event
     * @param commitTimeSeconds the Unix epoch seconds at which the
     *                          event was committed
     */
    WamPendingEvent(WamEventSpec event, long commitTimeSeconds) {
        this(event, commitTimeSeconds, null);
    }
}
