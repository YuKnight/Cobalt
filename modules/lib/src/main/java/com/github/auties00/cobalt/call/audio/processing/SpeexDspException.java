package com.github.auties00.cobalt.call.audio.processing;

/**
 * Thrown when a libspeexdsp call fails or returns an error code.
 * Wraps {@link Throwable}s thrown by FFM downcalls so callers don't
 * have to catch {@code Throwable} themselves.
 */
public class SpeexDspException extends RuntimeException {
    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message
     */
    public SpeexDspException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public SpeexDspException(String message, Throwable cause) {
        super(message, cause);
    }
}
