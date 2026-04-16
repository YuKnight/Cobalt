package com.github.auties00.cobalt.socket.layer.security.impl;

import com.github.auties00.cobalt.socket.WhatsAppSslEngineFactory;
import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.layer.security.SocketClientSecurityLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * A concrete security layer that provides TLS encryption over an existing
 * connection.
 *
 * <p>This layer is positionally polymorphic — it can be composed anywhere
 * in the stack (over raw transport for end-to-end TLS, or over a tunnel
 * for client-to-proxy TLS). Its behavior is identical at either position.
 */
public final class TlsSecurityLayer implements SocketClientSecurityLayer {
    private static final int HANDSHAKE_TIMEOUT = 30_000;

    private final SocketClientLayer<?> innerLayer;
    private final WhatsAppSslEngineFactory engineFactory;
    private InetSocketAddress peerAddress;

    /**
     * Creates a TLS security layer wrapping the given inner layer.
     *
     * @param innerLayer    the layer below
     * @param engineFactory the factory for creating {@link javax.net.ssl.SSLEngine} instances
     */
    public TlsSecurityLayer(SocketClientLayer<?> innerLayer, WhatsAppSslEngineFactory engineFactory) {
        this.innerLayer = innerLayer;
        this.engineFactory = engineFactory;
    }

    @Override
    public void startHandshake() throws IOException {
        var ctx = new TlsLayerContext();
        ctx.initSsl(engineFactory.createSSLEngine(peerAddress));
        innerLayer.registerLayerContext(ctx);
        innerLayer.startHandshake(ctx, HANDSHAKE_TIMEOUT);
    }

    @Override
    public void connect(InetSocketAddress address, SocketClientLayerListener listener) throws IOException {
        this.peerAddress = address;
        innerLayer.connect(address, listener);
        startHandshake();
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
