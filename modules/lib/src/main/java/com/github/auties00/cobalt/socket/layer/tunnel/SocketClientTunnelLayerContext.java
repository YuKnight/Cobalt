package com.github.auties00.cobalt.socket.layer.tunnel;

import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientPendingRead;

/**
 * A layer context that handles the pre-tunnel and post-tunnel phases of
 * a connection.
 *
 * <p>Before the connection is fully established, this context handles
 * blocking reads posted by handshake threads (proxy handshake, WebSocket
 * upgrade, etc.).  After the connection transitions to asynchronous mode,
 * it becomes a pure passthrough to the next layer above.
 */
public interface SocketClientTunnelLayerContext extends SocketClientLayerContext {
    /**
     * Sets the pending binary read request.
     *
     * <p>Called by the handshake thread to post a read request
     * that the selector will fulfill.
     *
     * @param read the pending read request
     * @return {@code true} if the read was posted, {@code false} if
     *         another read is already pending
     */
    boolean setPendingRead(SocketClientPendingRead read);

    /**
     * Marks the tunnel as established.
     *
     * <p>After this call, this context becomes a pure passthrough to
     * the next layer.
     */
    void markTunnelled();

    /**
     * Returns whether the tunnel has been established.
     *
     * @return {@code true} if tunnelled
     */
    boolean isTunnelled();
}
