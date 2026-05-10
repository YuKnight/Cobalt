package com.github.auties00.cobalt.call.transport.sctp;

/**
 * Thrown when a usrsctp operation fails — wraps either a non-zero
 * return code from the C library or a Java-side invariant violation
 * (closed socket, wrong-sized buffer, etc.). The exception is unchecked
 * so it composes with the {@link AutoCloseable} lifecycle of
 * {@link SctpAssociation} without forcing every caller to declare it.
 */
public class SctpException extends RuntimeException {
    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message
     */
    public SctpException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public SctpException(String message, Throwable cause) {
        super(message, cause);
    }
}
