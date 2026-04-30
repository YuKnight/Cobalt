package com.github.auties00.cobalt.socket.layer.application;

import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

/**
 * Marker interface for the layer context that an application-level
 * layer publishes to the selector pipeline.
 *
 * <p>The selector uses this type to locate the topmost layer of a
 * connection (for example to feed leftover bytes from a synchronous
 * protocol upgrade past every crypto layer and into the first
 * application-level buffer).
 */
public interface SocketClientApplicationLayerContext extends SocketClientLayerContext {

}
