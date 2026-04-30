package com.github.auties00.cobalt.socket.layer.application.websocket.frame;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Materialised WebSocket frame split into a header buffer and a
 * payload buffer.
 *
 * <p>The two buffers are kept separate so the selector can hand them
 * to a gather-write call without an intermediate copy.
 */
public final class WebSocketEncodedFrame {
    /**
     * The pre-built frame header, including FIN/opcode byte, payload
     * length encoding and the 4-byte mask key.
     */
    private final ByteBuffer header;

    /**
     * The masked payload, or an empty buffer for length-0 frames.
     */
    private final ByteBuffer payload;

    /**
     * Creates an encoded frame with the given header and payload.
     *
     * @param header  the frame header (including masking key)
     * @param payload the masked payload
     */
    public WebSocketEncodedFrame(ByteBuffer header, ByteBuffer payload) {
        this.header = header;
        this.payload = payload;
    }

    /**
     * Returns the frame header.
     *
     * @return the header buffer in read mode
     */
    public ByteBuffer header() {
        return header;
    }

    /**
     * Returns the frame payload.
     *
     * @return the payload buffer in read mode
     */
    public ByteBuffer payload() {
        return payload;
    }

    /**
     * Returns whether this frame equals another by comparing the header
     * and payload buffers structurally.
     *
     * @param obj the reference object
     * @return {@code true} if {@code obj} is an equivalent encoded frame
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WebSocketEncodedFrame) obj;
        return Objects.equals(this.header, that.header) &&
                Objects.equals(this.payload, that.payload);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(header, payload);
    }

    /**
     * Returns a string representation including the header and payload
     * buffers.
     *
     * @return a debug-oriented string
     */
    @Override
    public String toString() {
        return "WebSocketEncodedFrame[" +
                "header=" + header + ", " +
                "payload=" + payload + ']';
    }
}
