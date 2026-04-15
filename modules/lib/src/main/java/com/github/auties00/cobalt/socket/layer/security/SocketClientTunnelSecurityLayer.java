package com.github.auties00.cobalt.socket.layer.security;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.WhatsAppSslEngineFactory;

import java.util.Objects;

/**
 * A tunnel-level security layer that provides TLS encryption over
 * an established proxy tunnel.
 */
public sealed interface SocketClientTunnelSecurityLayer
        extends SocketClientSecurityLayer<TlsSocketClientLayerContext>
        permits TlsSocketClientSecurityLayer {

    /**
     * Creates a TLS tunnel security layer wrapping the given inner layer.
     *
     * @param innerLayer    the tunnel layer below TLS
     * @param engineFactory a factory that produces a configured
     *                      {@link SSLEngine} for the given peer address
     * @return a new TLS security layer
     */
    static SocketClientTunnelSecurityLayer newTlsTunnel(SocketClientLayer<?> innerLayer, WhatsAppSslEngineFactory engineFactory) {
        Objects.requireNonNull(innerLayer, "innerLayer cannot be null");
        Objects.requireNonNull(engineFactory, "engineFactory cannot be null");
        return new TlsSocketClientSecurityLayer(innerLayer, engineFactory, SocketClientTunnelSecurityLayer.class);
    }
}
