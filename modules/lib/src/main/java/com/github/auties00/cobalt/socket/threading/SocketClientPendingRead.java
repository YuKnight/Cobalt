package com.github.auties00.cobalt.socket.threading;

import java.nio.ByteBuffer;

/**
 * One blocking read request submitted to the selector while the
 * connection is still in a synchronous handshake phase.
 *
 * <p>The requesting virtual thread allocates the request, posts it to
 * the relevant layer context and parks on {@link #lock}. The selector
 * thread reads bytes from the channel into {@link #buffer}, updates
 * {@link #length} and signals the lock once the request is satisfied.
 *
 * <p>A request is satisfied when either:
 * <ul>
 * <li>{@link #fullRead} is {@code false} and at least one read has
 *     completed, or</li>
 * <li>{@link #fullRead} is {@code true} and the buffer is full.</li>
 * </ul>
 *
 * <p>If the channel reaches end-of-stream first, the selector sets
 * {@link #length} to {@code -1} and notifies the lock so the waiter
 * can throw.
 */
public final class SocketClientPendingRead {
    /**
     * Destination buffer that the selector fills.
     */
    public final ByteBuffer buffer;

    /**
     * When {@code true} the selector keeps reading until {@link #buffer}
     * is full; when {@code false} it completes the request after the
     * first successful read.
     */
    public final boolean fullRead;

    /**
     * Monitor used to park the requesting thread until the selector
     * either satisfies or fails the read.
     */
    public final Object lock;

    /**
     * Total bytes read so far, or {@code -1} when the channel reached
     * end-of-stream before any data was transferred.
     */
    public int length;

    /**
     * Creates a pending read request.
     *
     * @param buffer   the destination buffer
     * @param fullRead {@code true} to fill the buffer completely,
     *                 {@code false} to return after one read
     */
    public SocketClientPendingRead(ByteBuffer buffer, boolean fullRead) {
        this.buffer = buffer;
        this.fullRead = fullRead;
        this.lock = new Object();
        this.length = -1;
    }
}
