package com.github.auties00.cobalt.socket.layer.security.impl;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.layer.security.SocketClientSecurityLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * No-op security layer that forwards every operation to the layer it
 * wraps.
 *
 * <p>It deliberately registers no layer context with the selector;
 * the chain therefore skips this position entirely so the next inner
 * context's {@code nextLayer} points directly at the next outer
 * context, with no placeholder objects or wasted link traversals.
 */
public final class PlainSecurityLayer implements SocketClientSecurityLayer {
    /**
     * The layer that receives every delegated operation.
     */
    private final SocketClientLayer<?> innerLayer;

    /**
     * Creates a plain security layer wrapping {@code innerLayer}.
     *
     * @param innerLayer the layer below
     */
    public PlainSecurityLayer(SocketClientLayer<?> innerLayer) {
        this.innerLayer = innerLayer;
    }

    /**
     * No-op handshake; this layer never encrypts anything.
     */
    @Override
    public void startHandshake() throws IOException {
    }

    @Override
    public void connect(InetSocketAddress address, SocketClientLayerListener listener) throws IOException {
        innerLayer.connect(address, listener);
    }

    @Override
    public void disconnect() {
        innerLayer.disconnect();
    }

    @Override
    public boolean isConnected() {
        return innerLayer.isConnected();
    }

    @Override
    public void sendBinary(ByteBuffer... buffers) throws IOException {
        innerLayer.sendBinary(buffers);
    }

    @Override
    public int readBinary(ByteBuffer buffer, boolean fully) throws IOException {
        return innerLayer.readBinary(buffer, fully);
    }

    @Override
    public void finishConnect() throws IOException {
        innerLayer.finishConnect();
    }

    @Override
    public void finishConnect(ByteBuffer leftover) throws IOException {
        innerLayer.finishConnect(leftover);
    }

    @Override
    public void startHandshake(SocketClientLayerContext tlsContext, long timeout) throws IOException {
        innerLayer.startHandshake(tlsContext, timeout);
    }

    @Override
    public void registerLayerContext(SocketClientLayerContext context) throws IOException {
        innerLayer.registerLayerContext(context);
    }
}
