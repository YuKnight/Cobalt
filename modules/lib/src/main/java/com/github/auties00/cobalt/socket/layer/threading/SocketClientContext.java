package com.github.auties00.cobalt.socket.layer.threading;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.security.TlsSocketClientLayerContext;
import com.github.auties00.cobalt.socket.layer.transport.SocketClientTransportLayerContext;

import java.util.*;

/**
 * Per-connection context attached to a {@link java.nio.channels.SelectionKey}
 * as its attachment.
 *
 * <p>Layer contexts are stored in a {@link SequencedMap} preserving
 * bottom-to-top insertion order.  When a new context is registered and
 * the previous topmost context has no {@code nextLayer} set, the map
 * auto-chains them so the inbound processing pipeline is wired
 * automatically.
 */
final class SocketClientContext {
    /**
     * Transport-level state: connection lifecycle, pending writes.
     */
    private final SocketClientTransportLayerContext transportContext;

    /**
     * Layer processing contexts, keyed by layer class, in bottom-to-top
     * insertion order.
     */
    private final SequencedMap<Class<?>, SocketClientLayerContext> layerContextMap;

    /**
     * Creates a context with the given transport context.
     *
     * @param transportContext the transport-level state
     */
    SocketClientContext(SocketClientTransportLayerContext transportContext) {
        this.transportContext = Objects.requireNonNull(transportContext);
        this.layerContextMap = new LinkedHashMap<>();
    }

    /**
     * Returns the transport-level context.
     *
     * @return the transport context, never {@code null}
     */
    SocketClientTransportLayerContext transportContext() {
        return transportContext;
    }

    /**
     * Returns the bottommost processing layer context.
     *
     * <p>This is the first context in the chain — the one that the
     * selector reads bytes into and calls {@code processInbound()} on.
     *
     * @return the bottom layer context, or {@code null} if no layers
     *         are registered
     */
    SocketClientLayerContext bottomProcessingContext() {
        return layerContextMap.isEmpty() ? null : layerContextMap.firstEntry().getValue();
    }

    /**
     * Retrieves a layer context by its layer class.
     *
     * <p>The type parameter {@code C} is inferred from the layer class's
     * type parameter binding.  For example, passing
     * {@code SocketClientTransportSecurityLayer.class} (which extends
     * {@code SocketClientLayer<TlsLayerContext>}) returns
     * {@code Optional<TlsLayerContext>}.
     *
     * @param <C>   the context type, inferred from the layer's type parameter
     * @param clazz the layer class
     * @return an optional containing the context, or empty if not present
     */
    @SuppressWarnings("unchecked")
    <C extends SocketClientLayerContext> Optional<C> getLayerContext(Class<? extends SocketClientLayer<C>> clazz) {
        var result = layerContextMap.get(clazz);
        if (result == null) {
            return Optional.empty();
        }

        return Optional.of((C) result);
    }

    /**
     * Appends a layer context at the top of the processing chain.
     *
     * @param clazz        the layer class key
     * @param layerContext the context to register
     * @return the previous topmost context (the one that should link
     *         to the new context), or {@code null} if the map was empty
     *         or the key was already registered
     */
    void addLayerContext(Class<?> clazz, SocketClientLayerContext layerContext) {
        if (layerContextMap.containsKey(clazz)) {
            return;
        }

        if (layerContext instanceof TlsSocketClientLayerContext) {
            var previousBottom = layerContextMap.isEmpty() ? null : layerContextMap.firstEntry().getValue();
            layerContextMap.putFirst(clazz, layerContext);
            if (previousBottom != null) {
                layerContext.setNextLayer(previousBottom);
            }
        } else {
            var previousTop = layerContextMap.isEmpty() ? null : layerContextMap.lastEntry().getValue();
            layerContextMap.put(clazz, layerContext);
            if (previousTop != null) {
                previousTop.setNextLayer(layerContext);
            }
        }
    }
}
