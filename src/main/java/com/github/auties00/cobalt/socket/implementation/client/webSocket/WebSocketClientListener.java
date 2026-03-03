package com.github.auties00.cobalt.socket.implementation.client.webSocket;

import java.nio.ByteBuffer;

public interface WebSocketClientListener {
    void onDatagram(ByteBuffer buffer);
    void onPing(ByteBuffer message);
    void onPong(ByteBuffer message);
    void onClose(int statusCode, String reason);
}
