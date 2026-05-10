package com.github.auties00.cobalt.call.audio.opus;

import com.github.auties00.cobalt.call.audio.opus.bindings.Opus;

import java.lang.foreign.MemorySegment;

/**
 * Thrown when libopus encode or decode fails — wraps a non-zero
 * {@code OPUS_*} error code or a Java-side invariant violation.
 * Surfaces libopus's {@code opus_strerror} message when a numeric
 * code is the cause.
 */
public class OpusCodecException extends RuntimeException {
    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message
     */
    public OpusCodecException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public OpusCodecException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds an exception whose message includes libopus's textual
     * description of the {@code OPUS_*} error code.
     *
     * @param prefix  human-readable context ("encode failed")
     * @param errCode the libopus error code (negative for failures)
     * @return a new exception ready to throw
     */
    static OpusCodecException fromErr(String prefix, int errCode) {
        return new OpusCodecException(prefix + ": " + errString(errCode) + " (code " + errCode + ")");
    }

    /**
     * Reads libopus's static error string for the given error code.
     *
     * @param errCode the libopus error code
     * @return the error string, or "unknown" if the lookup fails
     */
    private static String errString(int errCode) {
        try {
            var ptr = Opus.opus_strerror(errCode);
            if (ptr.equals(MemorySegment.NULL)) {
                return "unknown";
            }
            return ptr.reinterpret(Long.MAX_VALUE).getString(0);
        } catch (Throwable t) {
            return "unknown";
        }
    }
}
