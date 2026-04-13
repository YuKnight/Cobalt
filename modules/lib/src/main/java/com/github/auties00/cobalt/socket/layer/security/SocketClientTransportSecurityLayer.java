package com.github.auties00.cobalt.socket.layer.security;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.security.tls.TlsSocketClientSecurityLayer;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.function.Function;

/**
 * A transport-level security layer that provides TLS encryption.
 */
public sealed interface SocketClientTransportSecurityLayer
        extends SocketClientSecurityLayer
        permits TlsSocketClientSecurityLayer {
    /**
     * Creates a TLS transport security layer wrapping the given transport layer.
     *
     * @param transportLayer the layer below TLS
     * @param engineFactory  a factory that produces a configured
     *                       {@link SSLEngine} for the given peer address
     * @return a new TLS security layer
     */
    static SocketClientTransportSecurityLayer newTlsTransport(SocketClientLayer transportLayer, Function<InetSocketAddress, SSLEngine> engineFactory) {
        Objects.requireNonNull(transportLayer, "transportLayer cannot be null");
        Objects.requireNonNull(engineFactory, "engineFactory cannot be null");
        return new TlsSocketClientSecurityLayer(transportLayer, engineFactory);
    }
}
