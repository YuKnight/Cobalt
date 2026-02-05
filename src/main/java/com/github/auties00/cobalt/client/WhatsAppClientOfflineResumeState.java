package com.github.auties00.cobalt.client;

/**
 * Represents the state of offline message resume processing.
 * <p>
 * Per WhatsApp Web WAWebOfflineResumeConst.ResumeStatus:
 * When a client connects/reconnects, it goes through an offline resume phase
 * where queued messages from the server are processed. This state tracks
 * the progress of that phase.
 */
public enum WhatsAppClientOfflineResumeState {
    /**
     * Initial state before any offline resume processing has started.
     * This is the state when the client first connects before receiving
     * the offline_preview IB from the server.
     */
    INIT,

    /**
     * Currently processing offline messages after a restart/reconnect.
     * The client has received offline_preview and is processing the
     * backlog of messages. During this phase, some operations like
     * immediate device syncs should be deferred.
     */
    RESUME_ON_RESTART,

    /**
     * Offline resume processing is complete.
     * All offline messages have been delivered and processed.
     * Normal operations can proceed.
     */
    COMPLETE
}
