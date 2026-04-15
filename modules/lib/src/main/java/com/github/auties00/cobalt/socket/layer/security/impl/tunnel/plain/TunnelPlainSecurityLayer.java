package com.github.auties00.cobalt.socket.layer.security.impl.tunnel.plain;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.security.impl.PlainSocketClientLayerContext;
import com.github.auties00.cobalt.socket.layer.security.SocketClientTunnelSecurityLayer;
import com.github.auties00.cobalt.socket.layer.security.impl.PlainSocketClientSecurityLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

/**
 * Tunnel-level plain (no encryption) security layer implementation.
 */
public final class TunnelPlainSecurityLayer extends PlainSocketClientSecurityLayer implements SocketClientTunnelSecurityLayer {
    public TunnelPlainSecurityLayer(SocketClientLayer<?> innerLayer) {
        super(innerLayer);
    }

    @Override
    protected PlainSocketClientLayerContext createLayerContext(SocketClientLayerContext nextLayer) {
        return new TunnelPlainLayerContext(nextLayer);
    }
}
