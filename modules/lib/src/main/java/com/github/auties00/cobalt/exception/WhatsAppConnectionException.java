package com.github.auties00.cobalt.exception;

/**
 * Thrown when Cobalt cannot establish the initial connection to the
 * WhatsApp servers.
 *
 * <p>The connection attempt covers DNS resolution, TCP, TLS, the
 * WebSocket upgrade, and the Noise XX handshake that authenticates the
 * client. Any failure along that path raises this exception. It is
 * distinct from {@link WhatsAppReconnectionException}, which is raised
 * by retries after a previously successful session was lost.
 *
 * <p>Connection failures are fatal because there is no live session to
 * recover. The configurable error handler decides whether the caller
 * should give up or schedule another connection attempt.
 *
 * @see WhatsAppReconnectionException
 * @see WhatsAppSessionException.Closed
 */
public final class WhatsAppConnectionException extends WhatsAppException {

    /**
     * Constructs a new connection exception with the specified detail message.
     *
     * @param message the detail message describing the connection failure
     */
    public WhatsAppConnectionException(String message) {
        super(message);
    }

    /**
     * Constructs a new connection exception with the specified detail message and cause.
     *
     * @param message the detail message describing the connection failure
     * @param cause   the underlying cause of the connection failure
     */
    public WhatsAppConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns whether the failure invalidates the current session.
     *
     * <p>Connection exceptions are always fatal because no session has
     * been established at the point they are thrown.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFatal() {
        return true;
    }
}
