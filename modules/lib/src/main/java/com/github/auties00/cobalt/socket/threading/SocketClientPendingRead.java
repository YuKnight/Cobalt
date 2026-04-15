package com.github.auties00.cobalt.socket.threading;

import java.nio.ByteBuffer;

/**
 * A pending read request submitted to the selector during the pre-tunnel
 * phase of a proxied connection.
 *
 * <p>The requesting thread constructs a {@code SocketClientPendingRead}, posts it
 * to the tunnel layer context, and blocks on {@link #lock}.  The
 * selector thread reads bytes from the channel into {@link #buffer},
 * updates {@link #length}, and notifies the lock when the request is
 * satisfied.
 *
 * <p>A request is considered satisfied when either:
 * <ul>
 * <li>{@link #fullRead} is {@code false} and at least one read
 *     operation has completed, or
 * <li>{@link #fullRead} is {@code true} and the buffer has no
 *     remaining capacity.
 * </ul>
 *
 * <p>If the channel reaches end-of-stream before the request is
 * satisfied, the selector sets {@link #length} to {@code -1} and
 * notifies the lock.
 */
public final class SocketClientPendingRead {
    /**
     * The destination buffer.
     */
    public final ByteBuffer buffer;

    /**
     * If {@code true}, the selector continues reading until the buffer
     * is completely filled.  If {@code false}, the selector completes
     * the request after a single successful read.
     */
    public final boolean fullRead;

    /**
     * Monitor used to block the requesting thread until the selector
     * has satisfied or failed this read request.
     */
    public final Object lock;

    /**
     * The total number of bytes read, or {@code -1} if the channel
     * reached end-of-stream before any data was transferred.
     */
    public int length;

    /**
     * Creates a pending read request.
     *
     * @param buffer   the destination buffer
     * @param fullRead {@code true} to fill the buffer completely;
     *                 {@code false} to return after a single read
     */
    public SocketClientPendingRead(ByteBuffer buffer, boolean fullRead) {
        this.buffer = buffer;
        this.fullRead = fullRead;
        this.lock = new Object();
        this.length = -1;
    }
}
