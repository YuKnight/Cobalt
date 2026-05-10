package com.github.auties00.cobalt.call.video.h264;

/**
 * Thrown when an openh264 operation fails — wraps a non-zero return
 * code from a vtable method ({@code Initialize}, {@code EncodeFrame},
 * {@code DecodeFrame2}, etc.) or a Java-side invariant violation
 * (closed codec, wrong frame size, etc.).
 */
public class H264Exception extends RuntimeException {
    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message
     */
    public H264Exception(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public H264Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
