package com.github.auties00.cobalt.socket.layer.security;

import com.github.auties00.cobalt.socket.WhatsAppSslEngineFactory;
import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.security.impl.PlainSecurityLayer;
import com.github.auties00.cobalt.socket.layer.security.impl.TlsSecurityLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.util.Objects;

/**
 * Security layer that adds optional encryption over an existing
 * connection.
 *
 * <p>Two factories are exposed: {@link #newTls(SocketClientLayer, WhatsAppSslEngineFactory)}
 * for TLS encryption and {@link #newPlain(SocketClientLayer)} for a
 * transparent pass-through used when this position in the stack does
 * not need encryption.
 *
 * <p>The security layer is positionally polymorphic: the same
 * implementation is reused at both the proxy hop (client-to-proxy)
 * and the end-to-end hop (client-to-target). The role is determined
 * entirely by where the caller places the layer in the stack.
 */
public interface SocketClientSecurityLayer extends SocketClientLayer<SocketClientLayerContext> {
    /**
     * Starts the security handshake and blocks until it completes.
     *
     * @throws IOException if the handshake fails
     */
    void startHandshake() throws IOException;

    /**
     * Creates a TLS security layer wrapping {@code innerLayer}.
     *
     * @param innerLayer    the layer below (transport or tunnel)
     * @param engineFactory the factory used to create the {@link javax.net.ssl.SSLEngine}
     * @return a new TLS security layer
     */
    static SocketClientSecurityLayer newTls(SocketClientLayer<?> innerLayer, WhatsAppSslEngineFactory engineFactory) {
        Objects.requireNonNull(innerLayer, "innerLayer cannot be null");
        Objects.requireNonNull(engineFactory, "engineFactory cannot be null");
        return new TlsSecurityLayer(innerLayer, engineFactory);
    }

    /**
     * Creates a no-op security layer that simply delegates to
     * {@code innerLayer}.
     *
     * @param innerLayer the layer below
     * @return a new plain security layer
     */
    static SocketClientSecurityLayer newPlain(SocketClientLayer<?> innerLayer) {
        Objects.requireNonNull(innerLayer, "innerLayer cannot be null");
        return new PlainSecurityLayer(innerLayer);
    }
}
