package com.github.auties00.cobalt.call.transport.sctp.datachannel;

/**
 * Thrown when a WebRTC DataChannel operation fails: malformed
 * {@link DcepMessage DCEP message} on the wire, attempt to use a
 * channel in the wrong {@link DataChannelState state}, stream-id
 * collision, or unsupported channel-type.
 *
 * <p>Wraps usrsctp-level failures from the underlying
 * {@link com.github.auties00.cobalt.call.transport.sctp.SctpAssociation}
 * by chaining the {@link com.github.auties00.cobalt.call.transport.sctp.SctpException}
 * as the cause.
 */
public class DataChannelException extends RuntimeException {
    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message
     */
    public DataChannelException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public DataChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
