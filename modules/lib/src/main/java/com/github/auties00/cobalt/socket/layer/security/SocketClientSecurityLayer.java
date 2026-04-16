package com.github.auties00.cobalt.socket.layer.security;

import com.github.auties00.cobalt.socket.WhatsAppSslEngineFactory;
import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.security.impl.PlainSecurityLayer;
import com.github.auties00.cobalt.socket.layer.security.impl.TlsSecurityLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.util.Objects;

/**
 * A security layer that provides optional encryption over an existing
 * connection.
 *
 * <p>Two factories are provided: {@link #newTls(SocketClientLayer,
 * WhatsAppSslEngineFactory)} for TLS encryption, and
 * {@link #newPlain(SocketClientLayer)} for a transparent passthrough used
 * when the position in the stack does not require encryption.
 *
 * <p>The security layer is positionally polymorphic — the same concrete
 * implementation is reused wherever encryption is needed, whether that is
 * the proxy hop (client-to-proxy) or the end-to-end hop (client-to-target).
 * The position is determined entirely by how the caller composes the stack,
 * not by the layer type.
 */
public interface SocketClientSecurityLayer extends SocketClientLayer<SocketClientLayerContext> {
    /**
     * Starts the security handshake and blocks until it completes.
     *
     * @throws IOException if the handshake fails
     */
    void startHandshake() throws IOException;

    /**
     * Creates a TLS security layer wrapping the given inner layer.
     *
     * @param innerLayer    the layer below (typically the transport or a tunnel)
     * @param engineFactory the factory for creating {@link javax.net.ssl.SSLEngine} instances
     * @return a new TLS security layer
     */
    static SocketClientSecurityLayer newTls(SocketClientLayer<?> innerLayer, WhatsAppSslEngineFactory engineFactory) {
        Objects.requireNonNull(innerLayer, "innerLayer cannot be null");
        Objects.requireNonNull(engineFactory, "engineFactory cannot be null");
        return new TlsSecurityLayer(innerLayer, engineFactory);
    }

    /**
     * Creates a plain (no-op) security layer wrapping the given inner layer.
     *
     * @param innerLayer the layer below
     * @return a new plain security layer
     */
    static SocketClientSecurityLayer newPlain(SocketClientLayer<?> innerLayer) {
        Objects.requireNonNull(innerLayer, "innerLayer cannot be null");
        return new PlainSecurityLayer(innerLayer);
    }
}
