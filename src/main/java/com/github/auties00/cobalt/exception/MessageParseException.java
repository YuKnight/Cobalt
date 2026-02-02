package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when message parsing or protobuf decoding fails.
 */
public final class MessageParseException extends MessageException {
    private final String errorCode;

    public MessageParseException(String message) {
        super(message);
        this.errorCode = "400";
    }

    public MessageParseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MessageParseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "400";
    }

    public MessageParseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code to send in the nack receipt.
     *
     * @return the error code
     */
    public String errorCode() {
        return errorCode;
    }
}
