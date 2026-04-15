package com.github.auties00.cobalt.socket.layer.security.impl;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * A no-op security layer that passes data through without encryption.
 *
 * <p>Used when no TLS is configured for a given security slot, ensuring
 * the layer chain always has all 6 layers and registration order
 * naturally determines the chain linkage.
 */
public abstract class PlainSocketClientSecurityLayer {
    private final SocketClientLayer<?> innerLayer;
    private boolean contextRegistered;

    protected PlainSocketClientSecurityLayer(SocketClientLayer<?> innerLayer) {
        this.innerLayer = innerLayer;
    }

    /**
     * Creates the appropriate plain layer context subclass.
     *
     * @param nextLayer the next layer context in the chain
     * @return a new plain context
     */
    protected abstract PlainSocketClientLayerContext createLayerContext(SocketClientLayerContext nextLayer);

    public void startHandshake() throws IOException {
        // No-op — no security handshake needed
    }

    public void connect(InetSocketAddress address, SocketClientLayerListener listener) throws IOException {
        innerLayer.connect(address, listener);
    }

    public void disconnect() {
        innerLayer.disconnect();
    }

    public boolean isConnected() {
        return innerLayer.isConnected();
    }

    public void sendBinary(ByteBuffer... buffers) throws IOException {
        innerLayer.sendBinary(buffers);
    }

    public int readBinary(ByteBuffer buffer, boolean fully) throws IOException {
        return innerLayer.readBinary(buffer, fully);
    }

    public void finishConnect() throws IOException {
        innerLayer.finishConnect();
    }

    public void finishConnect(ByteBuffer leftover) throws IOException {
        innerLayer.finishConnect(leftover);
    }

    public void startHandshake(SocketClientLayerContext tlsContext, long timeout) throws IOException {
        innerLayer.startHandshake(tlsContext, timeout);
    }

    public void registerLayerContext(SocketClientLayerContext context) throws IOException {
        if (!contextRegistered) {
            contextRegistered = true;
            innerLayer.registerLayerContext(createLayerContext(context));
        }
        innerLayer.registerLayerContext(context);
    }
}
