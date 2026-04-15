package com.github.auties00.cobalt.socket.layer.security.impl.transport.plain;

import com.github.auties00.cobalt.socket.layer.security.SocketClientTransportSecurityLayerContext;
import com.github.auties00.cobalt.socket.layer.security.impl.PlainSocketClientLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

/**
 * A plain (no-op) layer context for transport-level security.
 */
public final class TransportPlainLayerContext extends PlainSocketClientLayerContext implements SocketClientTransportSecurityLayerContext {
    public TransportPlainLayerContext(SocketClientLayerContext nextLayer) {
        super(nextLayer);
    }
}
