package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when message history synchronization fails.
 * <p>
 * History sync is the mechanism WhatsApp uses to transfer existing chat history
 * to newly connected companion devices. When a new device links to an account,
 * the primary device uploads encrypted history chunks that the companion device
 * downloads and decrypts.
 *
 * <h2>History Sync Architecture</h2>
 * The sync process involves:
 * <ol>
 *   <li><b>Initiation:</b> Companion device requests history from primary</li>
 *   <li><b>Upload:</b> Primary device encrypts and uploads history chunks to media servers</li>
 *   <li><b>Notification:</b> Primary device sends download URLs to companion</li>
 *   <li><b>Download:</b> Companion device downloads encrypted history</li>
 *   <li><b>Decryption:</b> Companion device decrypts and imports messages</li>
 * </ol>
 *
 * <h2>History Chunk Types</h2>
 * History is delivered in multiple chunks:
 * <ul>
 *   <li><b>Initial:</b> Most recent messages (fast initial sync)</li>
 *   <li><b>Recent:</b> Recent history from past few days</li>
 *   <li><b>Full:</b> Complete history (may take longer)</li>
 *   <li><b>On-demand:</b> History for specific chats requested later</li>
 * </ul>
 *
 * <h2>Possible Causes</h2>
 * <ul>
 *   <li><b>Download failure:</b> Network issues fetching history data</li>
 *   <li><b>Decryption failure:</b> Invalid or missing history keys</li>
 *   <li><b>Parse failure:</b> Corrupted or malformed history protobuf</li>
 *   <li><b>Storage failure:</b> Unable to persist imported messages</li>
 *   <li><b>Primary unavailable:</b> Primary device offline or unreachable</li>
 * </ul>
 *
 * <h2>Fatality</h2>
 * History sync exceptions are non-fatal. The client can continue operating without
 * full history, and sync can be retried later.
 */
public final class WhatsAppHistorySyncException extends WhatsAppException {

    /**
     * Constructs a new history sync exception with no detail message.
     */
    public WhatsAppHistorySyncException() {
        super();
    }

    /**
     * Constructs a new history sync exception with the specified detail message.
     *
     * @param message the detail message describing the sync failure
     */
    public WhatsAppHistorySyncException(String message) {
        super(message);
    }

    /**
     * Constructs a new history sync exception with a detail message and cause.
     *
     * @param message the detail message describing the sync failure
     * @param cause   the underlying cause of the sync failure
     */
    public WhatsAppHistorySyncException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new history sync exception wrapping the specified cause.
     *
     * @param cause the underlying cause of the sync failure
     */
    public WhatsAppHistorySyncException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns whether this exception represents a fatal error.
     * <p>
     * History sync exceptions are non-fatal. The client can continue operating
     * with partial or no history, and the sync can be retried later.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }
}
