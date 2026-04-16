package com.github.auties00.cobalt.exception;

/**
 * Non-fatal exception used for server-driven runtime conditions that should
 * still flow through the central error handler without forcing a disconnect.
 *
 * <p>This exception is raised when the server sends a transient or informational
 * error that does not correspond to one of the specialized exception domains
 * ({@link WhatsAppSessionException}, {@link WhatsAppStreamException},
 * {@link WhatsAppMessageException}, and so on). The configurable error handler
 * decides what to do: typically the event is logged and the client keeps running.
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
     * Returns whether this exception represents a fatal error.
     *
     * <p>Server runtime exceptions are non-fatal; they are raised to let the error
     * handler observe the server-driven condition while keeping the session active.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }
}
