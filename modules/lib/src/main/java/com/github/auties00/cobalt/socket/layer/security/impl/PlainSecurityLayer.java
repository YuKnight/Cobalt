package com.github.auties00.cobalt.socket.layer.security.impl;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.layer.security.SocketClientSecurityLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * A plain (no-op) security layer that transparently delegates all
 * operations to the inner layer.
 *
 * <p>No layer context is registered because the layer has no state to
 * maintain and performs no transformation of inbound or outbound bytes.
 * The linked chain therefore skips this position entirely, which means the
 * next inner context's {@code nextLayer} points directly at the next outer
 * context — no wasted link traversals and no placeholder objects.
 */
public final class PlainSecurityLayer implements SocketClientSecurityLayer {
    private final SocketClientLayer<?> innerLayer;

    /**
     * Creates a plain security layer wrapping the given inner layer.
     *
     * @param innerLayer the layer below
     */
    public PlainSecurityLayer(SocketClientLayer<?> innerLayer) {
        this.innerLayer = innerLayer;
    }

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
