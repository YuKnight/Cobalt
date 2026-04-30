package com.github.auties00.cobalt.socket.layer.transport.impl;

import com.github.auties00.cobalt.socket.threading.SocketClientInboundResult;
import com.github.auties00.cobalt.socket.layer.transport.SocketClientTransportLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TCP implementation of {@link SocketClientTransportLayerContext}.
 *
 * <p>Owns only the 16 KiB inbound buffer and the link to the next
 * layer; per-connection state (pending writes, connection lock,
 * connected flag) lives on the selector's {@code AttachmentData}.
 */
final class TcpSocketClientTransportLayerContext implements SocketClientTransportLayerContext {
    /**
     * Direct buffer that the selector fills with raw channel bytes.
     */
    private final ByteBuffer inboundBuffer = ByteBuffer.allocateDirect(16384);

    /**
     * Next layer in the inbound chain; {@code null} until an upper
     * layer registers itself during connection setup.
     */
    private volatile SocketClientLayerContext nextLayer;

    /**
     * Creates a transport context for a new connection.
     */
    TcpSocketClientTransportLayerContext() {
    }

    @Override
    public ByteBuffer inboundTarget() {
        return inboundBuffer;
    }

    @Override
    public SocketClientInboundResult processInbound(int bytesRead) throws IOException {
        if (bytesRead == -1) {
            return new SocketClientInboundResult.Close();
        }
        if (nextLayer == null) {
            return new SocketClientInboundResult.Buffering();
        }
        inboundBuffer.flip();
        var result = nextLayer.feedFromSource(inboundBuffer);
        inboundBuffer.compact();
        return result;
    }

    @Override
    public void setNextLayer(SocketClientLayerContext next) {
        this.nextLayer = next;
    }

    @Override
    public void onDisconnect() {
        if (nextLayer != null) {
            nextLayer.onDisconnect();
        }
    }
}
