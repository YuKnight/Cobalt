package com.github.auties00.cobalt.socket.layer.transport;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.threading.SocketClientLayerContext;

public sealed interface SocketClientTransportLayer
        extends SocketClientLayer<SocketClientLayerContext>
        permits TcpSocketClientTransportLayer {
    static SocketClientTransportLayer newTcpTransport() {
        return new TcpSocketClientTransportLayer();
    }
}
