package com.github.auties00.cobalt.socket.layer.transport;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.transport.impl.TcpSocketClientTransportLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

public interface SocketClientTransportLayer
        extends SocketClientLayer<SocketClientLayerContext> {
    static SocketClientTransportLayer newTcpTransport() {
        return new TcpSocketClientTransportLayer();
    }
}
