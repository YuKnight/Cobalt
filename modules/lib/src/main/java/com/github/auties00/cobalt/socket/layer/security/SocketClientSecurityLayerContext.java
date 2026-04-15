package com.github.auties00.cobalt.socket.layer.security;

import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

/**
 * A security layer context in the socket client stack.
 *
 * <p>Every connection has two security layer contexts: one for
 * transport-level security and one for tunnel-level security.
 * Each may be TLS or plain (no-op passthrough).
 */
public non-sealed interface SocketClientSecurityLayerContext extends SocketClientLayerContext {
}
