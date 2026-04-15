package com.github.auties00.cobalt.socket.layer.security;

import com.github.auties00.cobalt.socket.layer.threading.SocketClientInboundResult;
import com.github.auties00.cobalt.socket.layer.threading.SocketClientLayerContext;

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
public sealed abstract class PlainSocketClientLayerContext implements SocketClientLayerContext {
    private final SocketClientLayerContext nextLayer;

    private PlainSocketClientLayerContext(SocketClientLayerContext nextLayer) {
        this.nextLayer = nextLayer;
    }

    /**
     * Creates a plain transport security context.
     *
     * @param nextLayer the next layer in the chain
     * @return a new plain transport context
     */
    public static PlainSocketClientLayerContext newPlainTransportContext(SocketClientLayerContext nextLayer) {
        return new Transport(nextLayer);
    }

    /**
     * Creates a plain tunnel security context.
     *
     * @param nextLayer the next layer in the chain
     * @return a new plain tunnel context
     */
    public static PlainSocketClientLayerContext newPlainTunnelContext(SocketClientLayerContext nextLayer) {
        return new Tunnel(nextLayer);
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

    static final class Transport extends PlainSocketClientLayerContext {
        Transport(SocketClientLayerContext nextLayer) {
            super(nextLayer);
        }
    }

    static final class Tunnel extends PlainSocketClientLayerContext {
        Tunnel(SocketClientLayerContext nextLayer) {
            super(nextLayer);
        }
    }
}
