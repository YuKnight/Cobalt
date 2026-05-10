package com.github.auties00.cobalt.call.transport.ice;

/**
 * Thrown when ICE candidate gathering, connectivity checks, or
 * candidate-pair nomination fail. Wraps both protocol-level errors
 * (malformed STUN response, missing MESSAGE-INTEGRITY, etc.) and
 * Java-side invariant violations.
 */
public class IceException extends RuntimeException {
    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message
     */
    public IceException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public IceException(String message, Throwable cause) {
        super(message, cause);
    }
}
