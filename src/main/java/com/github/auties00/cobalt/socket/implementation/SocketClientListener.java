package com.github.auties00.cobalt.socket.implementation;

import java.nio.ByteBuffer;

public interface SocketClientListener {
    void onDatagram(ByteBuffer buffer);

    void onClose();
}
