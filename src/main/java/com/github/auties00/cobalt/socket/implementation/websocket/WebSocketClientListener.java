package com.github.auties00.cobalt.socket.implementation.websocket;

import com.github.auties00.cobalt.socket.implementation.SocketClientListener;

import java.nio.ByteBuffer;

public interface WebSocketClientListener extends SocketClientListener {
    void onBinary(CharSequence buffer, boolean last);
    void onText(ByteBuffer buffer, boolean last);
    void onPing(ByteBuffer message);
    void onPong(ByteBuffer message);
    void onClose(int statusCode, String reason);
}
