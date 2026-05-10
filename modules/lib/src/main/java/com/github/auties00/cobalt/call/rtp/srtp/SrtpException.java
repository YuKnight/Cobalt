package com.github.auties00.cobalt.call.rtp.srtp;

/**
 * Thrown when SRTP/SRTCP packet protection or unprotection fails.
 * Wraps the underlying {@link java.security.GeneralSecurityException}
 * (for cipher/HMAC initialisation or transformation errors), or
 * stands on its own to report packet-format violations, replay
 * detection, and authentication-tag mismatches.
 */
public class SrtpException extends RuntimeException {
    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message
     */
    public SrtpException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public SrtpException(String message, Throwable cause) {
        super(message, cause);
    }
}
