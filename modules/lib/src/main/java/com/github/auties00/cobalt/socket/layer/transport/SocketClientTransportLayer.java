package com.github.auties00.cobalt.socket.layer.transport;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.transport.tcp.TcpSocketClientTransportLayer;

public sealed interface SocketClientTransportLayer
        extends SocketClientLayer
        permits TcpSocketClientTransportLayer {
    static SocketClientTransportLayer newTcpTransport() {
        return new TcpSocketClientTransportLayer();
    }
}
