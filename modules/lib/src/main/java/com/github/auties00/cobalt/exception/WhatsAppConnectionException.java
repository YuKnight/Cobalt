package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when the initial WebSocket connection attempt to the WhatsApp server fails.
 * <p>
 * This exception represents failures that occur during the first connection attempt, before any
 * successful communication with the server. It is distinct from {@link WhatsAppReconnectionException}
 * which represents failures during reconnection attempts after a previous successful connection.
 *
 * <h2>Connection Process</h2>
 * Establishing a WhatsApp connection involves several steps:
 * <ol>
 *   <li><b>DNS Resolution:</b> Resolve the WhatsApp server hostname</li>
 *   <li><b>TCP Connection:</b> Establish TCP connection to the server</li>
 *   <li><b>TLS Handshake:</b> Negotiate TLS parameters and verify certificates</li>
 *   <li><b>WebSocket Upgrade:</b> Upgrade HTTP connection to WebSocket</li>
 *   <li><b>Noise Handshake:</b> Complete the Noise protocol handshake for encryption</li>
 * </ol>
 *
 * <h2>Possible Causes</h2>
 * <ul>
 *   <li><b>Network unavailability:</b> No internet connection or DNS resolution failure</li>
 *   <li><b>Server unreachable:</b> WhatsApp servers are down or blocked</li>
 *   <li><b>TLS/SSL failure:</b> Certificate validation failed or unsupported cipher suites</li>
 *   <li><b>Firewall/Proxy:</b> Connection blocked by network infrastructure</li>
 *   <li><b>Noise handshake failure:</b> Server rejected the cryptographic handshake</li>
 *   <li><b>Connection timeout:</b> Server did not respond within the timeout period</li>
 * </ul>
 *
 * <h2>Fatality</h2>
 * Connection exceptions are always fatal as there is no established session to recover.
 * The client should retry the connection with appropriate backoff.
 *
 * @see WhatsAppReconnectionException for failures during reconnection attempts
 * @see WhatsAppSessionException.Closed for operations on closed connections
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
     * @param cause   the underlying cause of the connection failure (e.g., IOException)
     */
    public WhatsAppConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns whether this exception represents a fatal error.
     * <p>
     * Connection exceptions are always fatal as they indicate no session was established.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFatal() {
        return true;
    }
}
