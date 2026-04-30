package com.github.auties00.cobalt.socket.layer.tunnel;

import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientPendingRead;

/**
 * Layer context that handles the pre-tunnel and post-tunnel phases
 * of a connection.
 *
 * <p>Before the tunnel is established this context routes inbound
 * bytes into blocking reads posted by handshake threads (proxy
 * handshake, WebSocket upgrade); afterwards it becomes a transparent
 * passthrough to the next layer above.
 */
public interface SocketClientTunnelLayerContext extends SocketClientLayerContext {
    /**
     * Posts a pending blocking read request that the selector will
     * fulfil through the chain.
     *
     * @param read the pending read request
     * @return {@code true} if the request was accepted, {@code false}
     *         if another read is already pending
     */
    boolean setPendingRead(SocketClientPendingRead read);

    /**
     * Marks the tunnel as established. After this call the context
     * becomes a pure passthrough.
     */
    void markTunnelled();

    /**
     * Returns whether the tunnel has been established.
     *
     * @return {@code true} if tunnelled
     */
    boolean isTunnelled();
}
