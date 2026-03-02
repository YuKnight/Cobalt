package com.github.auties00.cobalt.socket.implementation.threading;

import com.github.auties00.cobalt.socket.implementation.SocketClientListener;
import com.github.auties00.cobalt.socket.implementation.websocket.frame.WebSocketFrameDecoder;

public final class WebSocketContext extends SocketContext {
    /**
     * Stateful websocket parser context. Initialized lazily when websocket
     * framing is enabled for this channel.
     */
    public WebSocketFrameDecoder webSocketFrameDecoder;

    /**
     * Creates a context for a new connection.
     *
     * @param listener the callback to receive completed inbound datagrams
     */
    public WebSocketContext(SocketClientListener listener) {
        super(listener);
    }
}
