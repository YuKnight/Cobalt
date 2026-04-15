package com.github.auties00.cobalt.socket.layer.security.impl.transport.plain;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.layer.security.SocketClientTransportSecurityLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Transport-level plain (no encryption) security layer implementation.
 *
 * <p>All operations are delegated to the inner layer.  The layer context
 * is registered eagerly on the first {@link #registerLayerContext} call
 * so it appears before the tunnel context in the map.
 */
public final class TransportPlainSecurityLayer implements SocketClientTransportSecurityLayer {
    private final SocketClientLayer<?> innerLayer;
    private boolean contextRegistered;

    public TransportPlainSecurityLayer(SocketClientLayer<?> innerLayer) {
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
        if (!contextRegistered) {
            contextRegistered = true;
            innerLayer.registerLayerContext(new TransportPlainLayerContext(context));
        }
        innerLayer.registerLayerContext(context);
    }
}
