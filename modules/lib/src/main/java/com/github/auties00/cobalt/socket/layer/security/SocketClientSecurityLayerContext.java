package com.github.auties00.cobalt.socket.layer.security;

import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

/**
 * Marker interface for the layer context that a security layer
 * publishes to the selector pipeline.
 *
 * <p>A connection holds at most two security contexts, one for the
 * proxy hop and one for the end-to-end hop. Each is either a TLS
 * context or absent (a plain security layer registers no context at
 * all).
 */
public interface SocketClientSecurityLayerContext extends SocketClientLayerContext {
}
