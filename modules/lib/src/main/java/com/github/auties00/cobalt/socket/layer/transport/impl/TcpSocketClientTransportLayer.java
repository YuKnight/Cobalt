package com.github.auties00.cobalt.socket.layer.transport.impl;

import com.github.auties00.cobalt.socket.layer.transport.SocketClientTransportLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientSelector;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientPendingRead;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * TCP transport layer backed by a non-blocking {@link SocketChannel}
 * registered with the central {@link SocketClientSelector}.
 *
 * <p>This is the only layer that talks directly to NIO; every other
 * layer delegates to this one for byte movement and selector control.
 */
public final class TcpSocketClientTransportLayer implements SocketClientTransportLayer {
    /**
     * Underlying NIO socket channel, opened on
     * {@link #connect(InetSocketAddress, SocketClientLayerListener)}.
     */
    private SocketChannel channel;

    /**
     * Creates an unconnected TCP transport layer.
     */
    public TcpSocketClientTransportLayer() {

    }

    @Override
    public void connect(InetSocketAddress address, SocketClientLayerListener listener) throws IOException {
        var transportContext = new TcpSocketClientTransportLayerContext();
        this.channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(address);
        SocketClientSelector.INSTANCE.register(channel, transportContext);
        SocketClientSelector.INSTANCE.awaitConnect(channel, 30_000);
    }

    @Override
    public void disconnect() {
        SocketClientSelector.INSTANCE.unregister(channel);
    }

    @Override
    public boolean isConnected() {
        return SocketClientSelector.INSTANCE.isConnected(channel);
    }

    @Override
    public void sendBinary(ByteBuffer... buffers) throws IOException {
        if (!SocketClientSelector.INSTANCE.addWrite(channel, buffers)) {
            throw new IOException("Failed to enqueue write: channel not registered or closed");
        }
    }

    @Override
    public int readBinary(ByteBuffer buffer, boolean fully) throws IOException {
        var read = new SocketClientPendingRead(buffer, fully);
        if (!SocketClientSelector.INSTANCE.addRead(channel, read)) {
            throw new IOException("Failed to post read request");
        }
        synchronized (read.lock) {
            while (SocketClientSelector.INSTANCE.isConnected(channel) && (read.length == -1 || (fully && read.length >= 0 && read.buffer.hasRemaining()))) {
                try {
                    read.lock.wait(30_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Read interrupted", e);
                }
            }
        }
        return read.length;
    }

    @Override
    public void finishConnect() throws IOException {
        if (!SocketClientSelector.INSTANCE.finishConnect(channel)) {
            throw new IOException("Failed to finish connect: channel not registered or closed");
        }
    }

    @Override
    public void finishConnect(ByteBuffer leftover) throws IOException {
        if (!SocketClientSelector.INSTANCE.finishConnect(channel, leftover)) {
            throw new IOException("Failed to finish connect: channel not registered or closed");
        }
    }

    @Override
    public void startHandshake(SocketClientLayerContext tlsContext, long timeout) throws IOException {
        SocketClientSelector.INSTANCE.startHandshake(channel, tlsContext, timeout);
    }

    @Override
    public void registerLayerContext(SocketClientLayerContext layerContext) throws IOException {
        if (!SocketClientSelector.INSTANCE.registerLayerContext(channel, layerContext)) {
            throw new IOException("Failed to register layer context: channel not registered");
        }
    }
}
