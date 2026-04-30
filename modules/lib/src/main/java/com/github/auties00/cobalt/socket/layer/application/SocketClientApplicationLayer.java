package com.github.auties00.cobalt.socket.layer.application;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;

/**
 * Marker interface for the application-level layers that sit at the
 * top of the socket stack.
 *
 * <p>Application layers handle protocol-specific framing such as
 * WebSocket frames or the WhatsApp int24 datagram envelope. They are
 * always parameterised with the matching application-level context
 * type, which gives the selector a typed handle on the topmost layer
 * of each connection.
 *
 * @param <C> the type of application layer context produced by this
 *            layer
 */
public interface SocketClientApplicationLayer<C extends SocketClientApplicationLayerContext>
        extends SocketClientLayer<SocketClientApplicationLayerContext> {
}
