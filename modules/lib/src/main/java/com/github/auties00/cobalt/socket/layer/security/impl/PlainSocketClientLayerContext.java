package com.github.auties00.cobalt.socket.layer.security.impl;

import com.github.auties00.cobalt.socket.threading.SocketClientInboundResult;
import com.github.auties00.cobalt.socket.layer.security.SocketClientSecurityLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * A no-op security layer context that passes data through without
 * encryption.
 *
 * <p>Used when no TLS is configured for a given security slot
 * (transport or tunnel), ensuring the layer chain is always complete
 * and registration order naturally determines the {@code nextLayer}
 * linkage.
 */
public abstract class PlainSocketClientLayerContext implements SocketClientSecurityLayerContext {
    private final SocketClientLayerContext nextLayer;

    protected PlainSocketClientLayerContext(SocketClientLayerContext nextLayer) {
        this.nextLayer = nextLayer;
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
