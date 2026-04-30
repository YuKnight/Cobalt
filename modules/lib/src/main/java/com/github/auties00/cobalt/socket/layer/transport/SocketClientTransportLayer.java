package com.github.auties00.cobalt.socket.layer.transport;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.transport.impl.TcpSocketClientTransportLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

/**
 * Transport layer at the very bottom of the socket stack; the only
 * layer that talks directly to the NIO channel and the central
 * selector.
 */
public interface SocketClientTransportLayer
        extends SocketClientLayer<SocketClientLayerContext> {
    /**
     * Creates a TCP transport layer backed by a non-blocking
     * {@link java.nio.channels.SocketChannel} registered with
     * {@link com.github.auties00.cobalt.socket.threading.SocketClientSelector}.
     *
     * @return a new TCP transport layer
     */
    static SocketClientTransportLayer newTcpTransport() {
        return new TcpSocketClientTransportLayer();
    }
}
