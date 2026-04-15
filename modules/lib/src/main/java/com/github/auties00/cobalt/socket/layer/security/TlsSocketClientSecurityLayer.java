package com.github.auties00.cobalt.socket.layer.security;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.WhatsAppSslEngineFactory;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.layer.threading.SocketClientLayerContext;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * A security layer that provides TLS encryption over an existing
 * connection.
 *
 * <p>This layer wraps an inner layer (typically a tunnel layer) and adds
 * TLS encryption.  It can be used for both transport-level TLS (secure
 * WebSocket, end-to-end to the target) and tunnel-level TLS (HTTPS
 * proxy, client-to-proxy encryption).
 *
 * <p>The TLS handshake is initiated during {@link #connect(InetSocketAddress,
 * SocketClientLayerListener)} after the inner layer connects.  The
 * handshake is driven by the selector thread through the
 * {@link TlsSocketClientLayerContext} and blocks the calling virtual thread until
 * it completes.
 *
 * <p>The {@code registrationKey} determines which map key the
 * {@link TlsSocketClientLayerContext} is registered under, allowing the selector to
 * distinguish transport TLS from tunnel TLS in HTTPS proxy scenarios.
 */
public final class TlsSocketClientSecurityLayer implements SocketClientTunnelSecurityLayer, SocketClientTransportSecurityLayer {
    /**
     * A reasonable amount of time in ms before the handshake times out.
     */
    private static final int HANDSHAKE_TIMEOUT = 30_000;

    /**
     * The inner layer that provides raw I/O.
     */
    private final SocketClientLayer<?> innerLayer;

    /**
     * The factory that creates a configured {@link SSLEngine} for a
     * given peer address.
     */
    private final WhatsAppSslEngineFactory engineFactory;

    /**
     * The map key used when registering the {@link TlsSocketClientLayerContext}.
     *
     * <p>Transport TLS uses {@code SocketClientTransportSecurityLayer.class},
     * tunnel TLS uses {@code SocketClientTunnelSecurityLayer.class}.
     */
    private final Class<?> registrationKey;

    /**
     * The peer address captured during {@link #connect(InetSocketAddress,
     * SocketClientLayerListener)} for hostname verification.
     */
    private InetSocketAddress peerAddress;

    /**
     * Creates a TLS security layer wrapping the given inner layer.
     *
     * @param innerLayer      the layer below TLS
     * @param engineFactory   a factory that produces a configured
     *                        {@link SSLEngine} for the given peer address
     * @param registrationKey the map key for the TLS layer context
     */
    public TlsSocketClientSecurityLayer(SocketClientLayer<?> innerLayer, WhatsAppSslEngineFactory engineFactory, Class<?> registrationKey) {
        this.innerLayer = innerLayer;
        this.engineFactory = engineFactory;
        this.registrationKey = registrationKey;
    }

    /**
     * Starts the TLS handshake: creates the {@link TlsSocketClientLayerContext},
     * registers it with the selector pipeline, and blocks until the
     * handshake completes.
     *
     * @throws IOException if the handshake fails
     */
    @Override
    public void startHandshake() throws IOException {
        var engine = engineFactory.createSSLEngine(peerAddress);
        var tlsLayerContext = new TlsSocketClientLayerContext();
        tlsLayerContext.initSsl(engine);
        innerLayer.registerLayerContext(registrationKey, tlsLayerContext);
        innerLayer.startHandshake(tlsLayerContext, HANDSHAKE_TIMEOUT);
    }

    /**
     * Connects the inner layer, then performs the TLS handshake.
     *
     * <p>The inner layer's {@code connect()} establishes the underlying
     * connection (TCP, proxy tunnel, etc.).  After it returns, this
     * method creates a {@link TlsSocketClientLayerContext}, registers it in the
     * selector pipeline, and blocks until the TLS handshake completes.
     *
     * <p>TLS registers its context eagerly via
     * {@link #registerLayerContext(Class, SocketClientLayerContext)} so
     * that it appears <em>before</em> the tunnel context in the ordered
     * map — the selector must read encrypted bytes into TLS first.
     *
     * @param address  the remote endpoint (used for TLS hostname verification)
     * @param listener the callback for events
     * @throws IOException if the connection or TLS handshake fails
     */
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
    public void registerLayerContext(Class<?> key, SocketClientLayerContext context) throws IOException {
        innerLayer.registerLayerContext(key, context);
    }
}
