package com.github.auties00.cobalt.socket.layer.security;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.WhatsAppSslEngineFactory;
import com.github.auties00.cobalt.socket.layer.threading.SocketClientLayerContext;

import java.util.Objects;

/**
 * A tunnel-level security layer that provides optional TLS encryption
 * over an established proxy tunnel.
 */
public sealed interface SocketClientTunnelSecurityLayer
        extends SocketClientSecurityLayer<TlsSocketClientLayerContext>
        permits SocketClientTunnelSecurityLayer.TlsImpl, SocketClientTunnelSecurityLayer.PlainImpl {

    static SocketClientTunnelSecurityLayer newTlsTunnel(SocketClientLayer<?> innerLayer, WhatsAppSslEngineFactory engineFactory) {
        Objects.requireNonNull(innerLayer, "innerLayer cannot be null");
        Objects.requireNonNull(engineFactory, "engineFactory cannot be null");
        return new TlsImpl(innerLayer, engineFactory);
    }

    static SocketClientTunnelSecurityLayer newPlainTunnel(SocketClientLayer<?> innerLayer) {
        Objects.requireNonNull(innerLayer, "innerLayer cannot be null");
        return new PlainImpl(innerLayer);
    }

    final class TlsImpl extends TlsSocketClientSecurityLayer implements SocketClientTunnelSecurityLayer {
        TlsImpl(SocketClientLayer<?> innerLayer, WhatsAppSslEngineFactory engineFactory) {
            super(innerLayer, engineFactory);
        }

        @Override
        TlsSocketClientLayerContext createLayerContext() {
            return TlsSocketClientLayerContext.newTunnelTlsContext();
        }
    }

    final class PlainImpl extends PlainSocketClientSecurityLayer implements SocketClientTunnelSecurityLayer {
        PlainImpl(SocketClientLayer<?> innerLayer) {
            super(innerLayer);
        }

        @Override
        PlainSocketClientLayerContext createLayerContext(SocketClientLayerContext nextLayer) {
            return PlainSocketClientLayerContext.newPlainTunnelContext(nextLayer);
        }
    }
}
