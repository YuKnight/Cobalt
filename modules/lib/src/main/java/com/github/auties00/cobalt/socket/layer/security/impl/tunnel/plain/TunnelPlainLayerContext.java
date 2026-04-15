package com.github.auties00.cobalt.socket.layer.security.impl.tunnel.plain;

import com.github.auties00.cobalt.socket.layer.security.SocketClientTunnelSecurityLayerContext;
import com.github.auties00.cobalt.socket.layer.security.impl.PlainSocketClientLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

/**
 * A plain (no-op) layer context for tunnel-level security.
 */
public final class TunnelPlainLayerContext extends PlainSocketClientLayerContext implements SocketClientTunnelSecurityLayerContext {
    public TunnelPlainLayerContext(SocketClientLayerContext nextLayer) {
        super(nextLayer);
    }
}
