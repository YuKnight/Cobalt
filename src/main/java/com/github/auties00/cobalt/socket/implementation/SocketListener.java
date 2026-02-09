package com.github.auties00.cobalt.socket.implementation;

import java.nio.ByteBuffer;

public interface SocketListener {
    void onDatagram(ByteBuffer buffer);

    void onClose();
}
