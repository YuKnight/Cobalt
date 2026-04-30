package com.github.auties00.cobalt.socket.layer;

import java.nio.ByteBuffer;

/**
 * Receives the events that the layer stack surfaces once a connection
 * has been established.
 *
 * <p>Datagram delivery is serialized through a single-threaded virtual
 * executor so consecutive {@link #onDatagram(ByteBuffer)} calls never
 * race against each other; {@link #onClose()} may be invoked from any
 * thread, including the selector thread during teardown.
 */
public interface SocketClientLayerListener {
    /**
     * Receives a complete datagram assembled from the inbound stream.
     *
     * <p>The buffer is in read mode and contains exactly the payload
     * bytes described by the preceding length prefix; the layer stack
     * does not reuse it after this call so the listener may keep or
     * consume it freely.
     *
     * @param datagram the complete datagram payload
     */
    void onDatagram(ByteBuffer datagram);

    /**
     * Receives notification that the connection has been closed.
     *
     * <p>No further events are delivered for this connection after this
     * method returns.
     */
    void onClose();
}
