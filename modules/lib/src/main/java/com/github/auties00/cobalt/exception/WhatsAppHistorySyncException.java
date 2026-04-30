package com.github.auties00.cobalt.exception;

/**
 * Thrown when the message history transfer from the primary phone to a
 * newly linked companion device fails.
 *
 * <p>WhatsApp ships chat history to a freshly paired companion as a
 * sequence of encrypted blobs uploaded by the primary phone and
 * downloaded, decrypted, and applied by the companion. Failures can
 * happen at any stage: downloading the blob, decrypting it, parsing the
 * embedded protobuf, or persisting the resulting messages. Any of those
 * conditions raises this exception.
 *
 * <p>History sync failures are non-fatal. The session continues to
 * receive new messages and a partial or missing history can be retried
 * later when the primary phone is online again.
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
     * Returns whether the failure invalidates the current session.
     *
     * <p>History sync failures only affect the population of past
     * messages on this device. The session itself stays usable.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }
}
