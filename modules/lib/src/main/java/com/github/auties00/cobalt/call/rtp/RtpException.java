package com.github.auties00.cobalt.call.rtp;

/**
 * Thrown when RTP packet encode/decode, jitter-buffer ordering, or
 * SRTP protect/unprotect fails. Wraps both protocol-level errors
 * (truncated header, wrong version, SSRC mismatch) and Java-side
 * invariant violations.
 */
public class RtpException extends RuntimeException {
    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message
     */
    public RtpException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public RtpException(String message, Throwable cause) {
        super(message, cause);
    }
}
