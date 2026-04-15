package com.github.auties00.cobalt.socket.layer.security.impl.transport.plain;

import com.github.auties00.cobalt.socket.layer.security.SocketClientTransportSecurityLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientInboundResult;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * A plain (no-op) layer context for transport-level security.
 *
 * <p>All operations are delegated to the next layer in the chain.
 */
public final class TransportPlainLayerContext implements SocketClientTransportSecurityLayerContext {
    private volatile SocketClientLayerContext nextLayer;

    public TransportPlainLayerContext(SocketClientLayerContext nextLayer) {
        this.nextLayer = nextLayer;
    }

    @Override
    public void setNextLayer(SocketClientLayerContext next) {
        this.nextLayer = next;
    }

    @Override
    public ByteBuffer inboundTarget() {
        return nextLayer.inboundTarget();
    }

    @Override
    public SocketClientInboundResult processInbound(int bytesRead) throws IOException {
        return nextLayer.processInbound(bytesRead);
    }

    @Override
    public SocketClientInboundResult feedFromSource(ByteBuffer source) throws IOException {
        return nextLayer.feedFromSource(source);
    }

    @Override
    public boolean processOutbound(SocketChannel channel, ByteBuffer[] buffers, int offset, int count) throws IOException {
        return nextLayer.processOutbound(channel, buffers, offset, count);
    }

    @Override
    public void onDisconnect() {
        nextLayer.onDisconnect();
    }
}
