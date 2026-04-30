package com.github.auties00.cobalt.socket.layer.tunnel.impl;

import com.github.auties00.cobalt.socket.threading.SocketClientInboundResult;
import com.github.auties00.cobalt.socket.threading.SocketClientPendingRead;
import com.github.auties00.cobalt.socket.layer.tunnel.SocketClientTunnelLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;
import com.github.auties00.cobalt.util.DataUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Default implementation of {@link SocketClientTunnelLayerContext}.
 *
 * <p>While {@link #tunnelled} is {@code false} the context routes
 * inbound bytes into the blocking reads posted by handshake threads
 * (proxy handshake, WebSocket upgrade); once tunnelled it becomes a
 * transparent passthrough to the next layer above.
 */
final class CommonSocketTunnelLayerContext implements SocketClientTunnelLayerContext {
    /**
     * Layer above the tunnel; receives the decoded bytes once the
     * tunnel is established.
     */
    private volatile SocketClientLayerContext nextLayer;

    /**
     * Layer below the tunnel; receives outbound bytes on their way to
     * the channel.
     */
    private volatile SocketClientLayerContext prevLayer;

    /**
     * Whether the tunnel has been established.
     *
     * <p>Latches from {@code false} to {@code true} exactly once when
     * the handshake succeeds and never reverts. Read and written
     * exclusively by the selector thread.
     */
    private boolean tunnelled;

    /**
     * Pending binary read request, or {@code null} when no read is in
     * flight. Used only during the pre-tunnel phase.
     */
    private volatile SocketClientPendingRead pendingBinaryRead;

    /**
     * Creates a tunnel context in the pre-tunnel state.
     */
    public CommonSocketTunnelLayerContext() {
        this.tunnelled = false;
    }

    @Override
    public void setNextLayer(SocketClientLayerContext next) {
        this.nextLayer = next;
    }

    @Override
    public void setPrevLayer(SocketClientLayerContext prev) {
        this.prevLayer = prev;
    }

    @Override
    public SocketClientLayerContext prevLayer() {
        return prevLayer;
    }

    /**
     * Returns the buffer to read into.
     *
     * <p>While a proxy-handshake pending read is in flight, returns
     * that read's destination buffer; otherwise delegates to the next
     * layer so post-handshake bytes flow up the chain (typically into
     * a TLS layer above the tunnel).
     *
     * @return the buffer to read into
     */
    @Override
    public ByteBuffer inboundTarget() {
        var read = pendingBinaryRead;
        if (read != null) {
            return read.buffer;
        }
        var next = nextLayer;
        return next != null ? next.inboundTarget() : DataUtils.EMPTY_BYTE_BUFFER;
    }

    /**
     * Processes inbound bytes.
     *
     * <p>When a pending read is active, updates it and notifies the
     * waiting handshake thread; otherwise delegates straight to the
     * next layer so bytes continue up the chain.
     *
     * @param bytesRead the number of bytes read, or {@code -1} on
     *                  end-of-stream
     * @return the processing result
     * @throws IOException if layer processing fails
     */
    @Override
    public SocketClientInboundResult processInbound(int bytesRead) throws IOException {
        if (pendingBinaryRead != null) {
            return processPreTunnelRead(bytesRead);
        }
        var next = nextLayer;
        if (next == null) {
            return new SocketClientInboundResult.Buffering();
        }
        return next.processInbound(bytesRead);
    }

    /**
     * Updates the pending read with the bytes that just arrived and
     * notifies its waiter when the request has been satisfied.
     *
     * @param bytesRead the number of bytes read, or {@code -1} on
     *                  end-of-stream
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
     * Posts a pending blocking read request to this tunnel.
     *
     * <p>Refused if a read is already in flight or if the tunnel has
     * already been marked tunnelled, since post-tunnel reads belong
     * to a higher layer.
     *
     * @param read the pending read request
     * @return {@code true} if the request was accepted
     */
    @Override
    public boolean setPendingRead(SocketClientPendingRead read) {
        if (tunnelled || pendingBinaryRead != null) {
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
}
