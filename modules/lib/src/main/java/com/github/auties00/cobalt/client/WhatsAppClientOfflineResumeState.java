package com.github.auties00.cobalt.client;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

/**
 * Tracks the progress of the offline message resume phase that runs
 * immediately after a WhatsApp Web client connects or reconnects.
 *
 * <p>When a web companion re-establishes a socket, the server announces how
 * many queued stanzas it will replay with an {@code offline_preview} IB.
 * Until that backlog has been fully delivered, the client is in the middle
 * of an offline resume: certain operations such as immediate device syncs,
 * chat list re-sorting and collection flushes must be deferred so that the
 * in-flight replay can complete deterministically. Once the
 * {@code offline_stanza_complete} marker arrives, the state moves to
 * {@link #COMPLETE} and normal real-time operation resumes.
 *
 * @implNote WAWebOfflineResumeConst.ResumeStatus mirrors the three-state
 * machine defined in WhatsApp Web's offline resume manager. Cobalt uses this
 * state to gate the same operations that WA Web gates via
 * {@code WAWebBlockingOfflineResumeManager}.
 */
@WhatsAppWebModule(moduleName = "WAWebOfflineResumeConst")
public enum WhatsAppClientOfflineResumeState {
    /**
     * Initial state before any offline resume activity has started.
     *
     * <p>This is the state of a freshly constructed {@link WhatsAppClient}
     * and of a reconnecting client that has not yet received the
     * {@code offline_preview} IB from the server.
     *
     * @implNote WAWebOfflineResumeConst.ResumeStatus.INIT: the default value
     * assigned by {@code OfflineBlockingResumeStageManager.$10} on
     * construction.
     */
    INIT,

    /**
     * The client is actively consuming offline stanzas queued on the server
     * while it was disconnected.
     *
     * <p>During this phase the socket is delivering a backlog of messages,
     * receipts and notifications; collection flushes and chat list sorting
     * are temporarily disabled to avoid spurious intermediate states.
     *
     * @implNote WAWebOfflineResumeConst.ResumeStatus.RESUME_ON_RESTART: set
     * by {@code OfflineBlockingResumeStageManager.processOfflinePreview} after
     * the offline preview IB arrives during a cold (re)start.
     */
    RESUME_ON_RESTART,

    /**
     * Offline resume is finished and the client is operating in real time.
     *
     * <p>All offline stanzas have been delivered, the pending device sync
     * has been performed, and deferred operations can proceed normally.
     *
     * @implNote WAWebOfflineResumeConst.ResumeStatus.COMPLETE: set by
     * {@code OfflineBlockingResumeStageManager.processOfflineSessionComplete}
     * once the offline backlog and the per-message queue have both drained.
     */
    COMPLETE
}
