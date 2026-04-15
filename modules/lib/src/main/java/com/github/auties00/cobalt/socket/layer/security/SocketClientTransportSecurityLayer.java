package com.github.auties00.cobalt.socket.layer.security;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.WhatsAppSslEngineFactory;
import com.github.auties00.cobalt.socket.layer.threading.SocketClientLayerContext;

import java.util.Objects;

/**
 * A transport-level security layer that provides optional TLS encryption.
 */
public sealed interface SocketClientTransportSecurityLayer
        extends SocketClientSecurityLayer<TlsSocketClientLayerContext>
        permits SocketClientTransportSecurityLayer.TlsImpl, SocketClientTransportSecurityLayer.PlainImpl {

    static SocketClientTransportSecurityLayer newTlsTransport(SocketClientLayer<?> transportLayer, WhatsAppSslEngineFactory engineFactory) {
        Objects.requireNonNull(transportLayer, "transportLayer cannot be null");
        Objects.requireNonNull(engineFactory, "engineFactory cannot be null");
        return new TlsImpl(transportLayer, engineFactory);
    }

    static SocketClientTransportSecurityLayer newPlainTransport(SocketClientLayer<?> transportLayer) {
        Objects.requireNonNull(transportLayer, "transportLayer cannot be null");
        return new PlainImpl(transportLayer);
    }

    final class TlsImpl extends TlsSocketClientSecurityLayer implements SocketClientTransportSecurityLayer {
        TlsImpl(SocketClientLayer<?> innerLayer, WhatsAppSslEngineFactory engineFactory) {
            super(innerLayer, engineFactory);
        }

        @Override
        TlsSocketClientLayerContext createLayerContext() {
            return TlsSocketClientLayerContext.newTransportTlsContext();
        }
    }

    final class PlainImpl extends PlainSocketClientSecurityLayer implements SocketClientTransportSecurityLayer {
        PlainImpl(SocketClientLayer<?> innerLayer) {
            super(innerLayer);
        }

        @Override
        PlainSocketClientLayerContext createLayerContext(SocketClientLayerContext nextLayer) {
            return PlainSocketClientLayerContext.newPlainTransportContext(nextLayer);
        }
    }
}
