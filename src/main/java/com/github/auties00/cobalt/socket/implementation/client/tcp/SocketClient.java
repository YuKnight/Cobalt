package com.github.auties00.cobalt.socket.implementation.client.tcp;

import com.github.auties00.cobalt.client.WhatsAppClientProxy;
import com.github.auties00.cobalt.socket.implementation.tunnel.SocketClientTunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class SocketClient {
    private final SocketClientTunnel tunnel;

    private SocketClient(SocketClientTunnel tunnel) {
        this.tunnel = tunnel;
    }

    public static SocketClient newSocketClient(WhatsAppClientProxy proxy) {
        var tunnel = SocketClientTunnel.newSocketClientTunnel(proxy);
        return new SocketClient(tunnel);
    }

    public void connect(InetSocketAddress address, SocketClientListener listener) throws IOException, InterruptedException {
        tunnel.connect(address, listener);
    }

    public void disconnect() throws IOException {
        tunnel.disconnect();
    }

    public void sendBinary(ByteBuffer... buffers) {
        tunnel.sendBinary(buffers);
    }

    public int readBinary(ByteBuffer buffer, boolean fully) throws IOException {
        return tunnel.readBinary(buffer, fully);
    }

    public boolean isConnected() {
        return tunnel.isConnected();
    }
}
