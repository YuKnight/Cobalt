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
 * TLS implementation of {@link SocketClientSecurityLayer}.
 *
 * <p>Positionally polymorphic: composes anywhere in the stack and
 * behaves identically at the proxy hop (raw transport plus TLS) or at
 * the end-to-end hop (tunnel plus TLS).
 */
public final class TlsSecurityLayer implements SocketClientSecurityLayer {
    /**
     * Maximum time in milliseconds to wait for the TLS handshake.
     */
    private static final int HANDSHAKE_TIMEOUT = 30_000;

    /**
     * The layer below, providing the raw byte stream.
     */
    private final SocketClientLayer<?> innerLayer;

    /**
     * Factory used to instantiate the {@link javax.net.ssl.SSLEngine}.
     */
    private final WhatsAppSslEngineFactory engineFactory;

    /**
     * Peer address captured at {@link #connect(InetSocketAddress, SocketClientLayerListener)}
     * time and reused when the engine is created.
     */
    private InetSocketAddress peerAddress;

    /**
     * Creates a TLS security layer wrapping {@code innerLayer}.
     *
     * @param innerLayer    the layer below
     * @param engineFactory the factory used for the SSL engine
     */
    public TlsSecurityLayer(SocketClientLayer<?> innerLayer, WhatsAppSslEngineFactory engineFactory) {
        this.innerLayer = innerLayer;
        this.engineFactory = engineFactory;
    }

    /**
     * Creates a fresh {@link TlsLayerContext}, registers it with the
     * inner layer and drives the handshake to completion.
     *
     * @throws IOException if registration or the handshake fails
     */
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
