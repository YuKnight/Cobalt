package com.github.auties00.cobalt.socket.layer.transport;

import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

/**
 * Marker interface for the transport-level layer context, the head of
 * the chain and the only one the selector reads bytes into directly.
 *
 * <p>Per-connection state that is not really transport state (the
 * outbound write queue, the connection lock, the {@code connected}
 * flag) lives on the selector's {@code AttachmentData}; this
 * interface only identifies the transport position in the chain.
 */
public interface SocketClientTransportLayerContext extends SocketClientLayerContext {
}
