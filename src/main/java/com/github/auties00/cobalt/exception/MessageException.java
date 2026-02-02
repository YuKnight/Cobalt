package com.github.auties00.cobalt.exception;

/**
 * Base exception for message processing errors.
 */
public sealed class MessageException extends RuntimeException
        permits MessageDecryptionException, MessageParseException {
    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageException(Throwable cause) {
        super(cause);
    }
}
