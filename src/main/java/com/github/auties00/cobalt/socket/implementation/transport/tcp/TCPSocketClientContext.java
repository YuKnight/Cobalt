package com.github.auties00.cobalt.socket.implementation.transport.tcp;

import com.github.auties00.cobalt.socket.implementation.client.tcp.SocketClientListener;
import com.github.auties00.cobalt.socket.implementation.context.AbstractSocketClientContext;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class TCPSocketClientContext extends AbstractSocketClientContext {
    /**
     * Socket client listener.
     */
    public final SocketClientListener socketClientListener;

    /**
     * Creates a context for a new connection.
     *
     * @param socketClientListener  the callback to receive completed inbound datagrams
     */
    public TCPSocketClientContext(SocketClientListener socketClientListener) {
        this.socketClientListener = Objects.requireNonNull(socketClientListener, "socketClientListener cannot be null");
    }

    @Override
    public void onDatagram(ByteBuffer datagram) {
        socketClientListener.onDatagram(datagram);
    }

    @Override
    public void onClose() {
        socketClientListener.onClose();
    }
}
