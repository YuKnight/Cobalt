package com.github.auties00.cobalt.socket.layer.tunnel.impl;

import com.github.auties00.cobalt.socket.threading.SocketClientInboundResult;
import com.github.auties00.cobalt.socket.threading.SocketClientPendingRead;
import com.github.auties00.cobalt.socket.layer.tunnel.SocketClientTunnelLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Default implementation of {@link SocketClientTunnelLayerContext} that handles
 * the pre-tunnel and post-tunnel phases of a connection.
 *
 * <p>Before the connection is fully established ({@code tunnelled = false}),
 * this context handles blocking reads posted by handshake threads (proxy
 * handshake, WebSocket upgrade, etc.).  After the connection transitions
 * to asynchronous mode ({@code tunnelled = true}), it becomes a pure
 * passthrough to the next layer above.
 */
final class CommonSocketTunnelLayerContext implements SocketClientTunnelLayerContext {
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    /**
     * The next layer context in the chain (above tunnel).
     * Set via {@link #setNextLayer(SocketClientLayerContext)} by auto-chaining.
     */
    private volatile SocketClientLayerContext nextLayer;

    /**
     * Whether the tunnel has been established.
     *
     * <p>Transitions from {@code false} to {@code true} exactly once
     * after the handshake phase succeeds.  Once {@code true}, never
     * reverts.  Read and written exclusively by the selector thread.
     */
    private boolean tunnelled;

    /**
     * The pending binary read request, or {@code null} if no read is
     * in progress.
     *
     * <p>Used only during the pre-tunnel phase.  Written by the thread
     * calling {@code readBinary}; read and cleared by the selector thread.
     */
    private volatile SocketClientPendingRead pendingBinaryRead;

    /**
     * Creates a tunnel layer context in the pre-tunnel (not yet
     * tunnelled) state.
     */
    public CommonSocketTunnelLayerContext() {
        this.tunnelled = false;
    }

    /**
     * Sets the next layer context in the inbound processing chain.
     *
     * @param next the next layer context
     */
    @Override
    public void setNextLayer(SocketClientLayerContext next) {
        this.nextLayer = next;
    }

    /**
     * Returns the appropriate inbound target based on the tunnel phase.
     *
     * <p>Pre-tunnel: returns the pending read's destination buffer.
     * Post-tunnel: delegates to the next layer's inbound target.
     *
     * @return the buffer to read into
     */
    @Override
    public ByteBuffer inboundTarget() {
        if (tunnelled) {
            var next = nextLayer;
            return next != null ? next.inboundTarget() : EMPTY_BUFFER;
        } else {
            var read = pendingBinaryRead;
            if (read != null) {
                return read.buffer;
            } else {
                return EMPTY_BUFFER;
            }
        }
    }

    /**
     * Processes inbound bytes based on the tunnel phase.
     *
     * <p>Pre-tunnel: updates the pending read request and notifies the
     * waiting handshake thread.
     * Post-tunnel: delegates to the next layer.
     *
     * @param bytesRead the number of bytes read, or -1 for end-of-stream
     * @return the processing result
     * @throws IOException if layer processing fails
     */
    @Override
    public SocketClientInboundResult processInbound(int bytesRead) throws IOException {
        if (!tunnelled) {
            return processPreTunnelRead(bytesRead);
        }
        var next = nextLayer;
        if (next == null) {
            return new SocketClientInboundResult.Buffering();
        }
        return next.processInbound(bytesRead);
    }

    /**
     * Processes a pre-tunnel read completion.
     *
     * @param bytesRead the number of bytes read, or -1 for EOS
     * @return the processing result
     */
    private SocketClientInboundResult processPreTunnelRead(int bytesRead) {
        var pendingRead = pendingBinaryRead;
        if (pendingRead == null) {
            return new SocketClientInboundResult.Buffering();
        }

        if (bytesRead == -1) {
            pendingRead.length = -1;
            synchronized (pendingRead.lock) {
                pendingRead.lock.notifyAll();
            }
            return new SocketClientInboundResult.Close();
        }

        if (bytesRead == 0) {
            return new SocketClientInboundResult.Buffering();
        }

        if (pendingRead.length == -1) {
            pendingRead.length = 0;
        }
        pendingRead.length += bytesRead;

        if (!pendingRead.fullRead || !pendingRead.buffer.hasRemaining()) {
            pendingBinaryRead = null;
            synchronized (pendingRead.lock) {
                pendingRead.lock.notifyAll();
            }
        }

        return new SocketClientInboundResult.Continue();
    }

    /**
     * Sets the pending binary read request.
     *
     * <p>Called by the handshake thread to post a read request
     * that the selector will fulfill.
     *
     * @param read the pending read request
     * @return {@code true} if the read was posted, {@code false} if
     *         another read is already pending
     */
    @Override
    public boolean setPendingRead(SocketClientPendingRead read) {
        if (pendingBinaryRead != null) {
            return false;
        }
        pendingBinaryRead = read;
        return true;
    }

    /**
     * Marks the tunnel as established.
     *
     * <p>After this call, this context becomes a pure passthrough to
     * the next layer.
     */
    @Override
    public void markTunnelled() {
        this.tunnelled = true;
    }

    /**
     * Returns whether the tunnel has been established.
     *
     * @return {@code true} if tunnelled
     */
    @Override
    public boolean isTunnelled() {
        return tunnelled;
    }

    @Override
    public void onDisconnect() {
        var pendingRead = pendingBinaryRead;
        if (pendingRead != null) {
            pendingRead.length = -1;
            pendingBinaryRead = null;
            synchronized (pendingRead.lock) {
                pendingRead.lock.notifyAll();
            }
        }
        var next = nextLayer;
        if (next != null) {
            next.onDisconnect();
        }
    }

    /**
     * Delegates outbound bytes up the chain so that any security layer
     * above this tunnel gets to wrap them before they reach the channel.
     *
     * <p>Without this override, the default {@link SocketClientLayerContext#processOutbound}
     * would write directly to the channel and short-circuit any TLS layer
     * that sits above the tunnel in the new linked-list ordering (where
     * end-to-end TLS is above the tunnel because the factory composes it
     * as an outer wrapper around the proxy tunnel).
     *
     * @param channel the socket channel to write to
     * @param buffers the data buffers
     * @param offset  the offset into the buffers array
     * @param count   the number of buffers to process
     * @return {@code true} if all data was written successfully
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public boolean processOutbound(SocketChannel channel, ByteBuffer[] buffers, int offset, int count) throws IOException {
        var next = nextLayer;
        if (next != null) {
            return next.processOutbound(channel, buffers, offset, count);
        }
        channel.write(buffers, offset, count);
        return true;
    }
}
