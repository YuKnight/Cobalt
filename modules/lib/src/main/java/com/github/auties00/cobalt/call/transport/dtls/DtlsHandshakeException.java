package com.github.auties00.cobalt.call.transport.dtls;

import java.io.IOException;

/**
 * Thrown when the DTLS-SRTP handshake fails: peer fingerprint
 * mismatch, unsupported SRTP profile, alert received, etc.
 */
public class DtlsHandshakeException extends IOException {
    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message
     */
    public DtlsHandshakeException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public DtlsHandshakeException(String message, Throwable cause) {
        super(message, cause);
    }
}
