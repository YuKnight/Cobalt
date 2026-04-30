package com.github.auties00.cobalt.exception;

/**
 * Thrown for server-driven runtime conditions that do not fit any of
 * the more specific exception types and that should be observed
 * through the configurable error handler without forcing a disconnect.
 *
 * <p>The WhatsApp servers occasionally deliver informational or
 * transient error stanzas that do not map cleanly to a session, stream,
 * message, or media failure. Surfacing them as this exception lets the
 * configurable error handler log the event or run custom telemetry
 * while the session keeps running.
 */
public final class WhatsAppServerRuntimeException extends WhatsAppException {
    /**
     * Constructs a new server runtime exception with the specified detail message.
     *
     * @param message the detail message describing the server-side condition
     */
    public WhatsAppServerRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new server runtime exception with the specified detail message
     * and underlying cause.
     *
     * @param message the detail message describing the server-side condition
     * @param cause   the underlying cause of this exception
     */
    public WhatsAppServerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns whether the failure invalidates the current session.
     *
     * <p>Server runtime exceptions are observational and never tear the
     * session down on their own.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }
}
