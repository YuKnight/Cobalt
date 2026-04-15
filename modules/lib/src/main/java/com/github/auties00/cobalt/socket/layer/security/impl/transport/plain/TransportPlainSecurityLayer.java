package com.github.auties00.cobalt.socket.layer.security.impl.transport.plain;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.security.impl.PlainSocketClientLayerContext;
import com.github.auties00.cobalt.socket.layer.security.SocketClientTransportSecurityLayer;
import com.github.auties00.cobalt.socket.layer.security.impl.PlainSocketClientSecurityLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

/**
 * Transport-level plain (no encryption) security layer implementation.
 */
public final class TransportPlainSecurityLayer extends PlainSocketClientSecurityLayer implements SocketClientTransportSecurityLayer {
    public TransportPlainSecurityLayer(SocketClientLayer<?> innerLayer) {
        super(innerLayer);
    }

    @Override
    protected PlainSocketClientLayerContext createLayerContext(SocketClientLayerContext nextLayer) {
        return new TransportPlainLayerContext(nextLayer);
    }
}
