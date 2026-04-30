package com.github.auties00.cobalt.socket.layer.tunnel.impl;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.layer.tunnel.SocketClientTunnelLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * No-op tunnel layer used when no proxy is configured.
 *
 * <p>Acts as a pass-through: every I/O method delegates straight to
 * the inner layer. Its only purpose is to register a
 * {@link CommonSocketTunnelLayerContext} during connection setup so
 * blocking {@code readBinary()} calls work during the handshake
 * phase before the connection transitions to asynchronous mode.
 */
public final class DirectSocketClientTunnelLayer implements SocketClientTunnelLayer {
    /**
     * The inner layer that performs the actual I/O.
     */
    private final SocketClientLayer<?> innerLayer;

    /**
     * Creates a direct tunnel layer wrapping {@code innerLayer}.
     *
     * @param innerLayer the layer below, typically a transport layer
     */
    public DirectSocketClientTunnelLayer(SocketClientLayer<?> innerLayer) {
        this.innerLayer = innerLayer;
    }

    /**
     * Connects the inner layer and registers a
     * {@link CommonSocketTunnelLayerContext} so blocking reads work
     * during the handshake phase.
     *
     * @param address  the remote endpoint
     * @param listener the listener that receives inbound events
     * @throws IOException if the connection fails
     */
    @Override
    public void connect(InetSocketAddress address, SocketClientLayerListener listener) throws IOException {
        innerLayer.connect(address, listener);
        innerLayer.registerLayerContext(new CommonSocketTunnelLayerContext());
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
