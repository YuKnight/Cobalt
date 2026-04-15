package com.github.auties00.cobalt.socket.layer.threading;

import com.github.auties00.cobalt.socket.WhatsAppLayerContext;
import com.github.auties00.cobalt.socket.layer.application.websocket.WebSocketLayerContext;
import com.github.auties00.cobalt.socket.layer.security.PlainSocketClientLayerContext;
import com.github.auties00.cobalt.socket.layer.security.TlsSocketClientLayerContext;
import com.github.auties00.cobalt.socket.layer.security.TransportTlsLayerContext;
import com.github.auties00.cobalt.socket.layer.security.TunnelTlsLayerContext;
import com.github.auties00.cobalt.socket.layer.transport.SocketClientTransportLayerContext;
import com.github.auties00.cobalt.socket.layer.tunnel.SocketClientTunnelLayerContext;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Per-connection context attached to a {@link java.nio.channels.SelectionKey}
 * as its attachment.
 *
 * <p>Layer contexts are stored as explicit typed fields in a fixed
 * bottom-to-top order: transport security, tunnel security, tunnel,
 * websocket, app.  When a new context is registered, the chain is
 * rebuilt automatically so the inbound processing pipeline is wired
 * correctly.
 */
final class SocketClientContext {
    /**
     * Transport-level state: connection lifecycle, pending writes.
     */
    private final SocketClientTransportLayerContext transportContext;

    /**
     * Transport-level security context (TLS or plain passthrough).
     */
    private SocketClientLayerContext transportSecurity;

    /**
     * Tunnel-level security context (TLS or plain passthrough).
     */
    private SocketClientLayerContext tunnelSecurity;

    /**
     * Tunnel layer context (proxy handshake and blocking reads).
     */
    private SocketClientTunnelLayerContext tunnel;

    /**
     * WebSocket framing layer context.
     */
    private WebSocketLayerContext websocket;

    /**
     * WhatsApp application layer context (Noise protocol).
     */
    private WhatsAppLayerContext app;

    /**
     * Creates a context with the given transport context.
     *
     * @param transportContext the transport-level state
     */
    SocketClientContext(SocketClientTransportLayerContext transportContext) {
        this.transportContext = Objects.requireNonNull(transportContext);
    }

    /**
     * Creates a new connection context wrapping the given transport context.
     *
     * @param transportContext the transport-level state for the connection
     * @return a new {@code SocketClientContext}
     */
    static SocketClientContext newConnectionContext(SocketClientTransportLayerContext transportContext) {
        return new SocketClientContext(transportContext);
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
        if (transportSecurity != null) return transportSecurity;
        if (tunnelSecurity != null) return tunnelSecurity;
        if (tunnel != null) return tunnel;
        if (websocket != null) return websocket;
        return app;
    }

    /**
     * Registers a layer context and rebuilds the processing chain.
     *
     * <p>The concrete type of the context determines which field is set.
     * Transport and tunnel TLS are distinguished by their sealed
     * subclass ({@link TransportTlsLayerContext} vs
     * {@link TunnelTlsLayerContext}).
     *
     * @param layerContext the context to register
     */
    void addLayerContext(SocketClientLayerContext layerContext) {
        switch (layerContext) {
            case TransportTlsLayerContext tls -> this.transportSecurity = tls;
            case TunnelTlsLayerContext tls -> this.tunnelSecurity = tls;
            case PlainSocketClientLayerContext.Transport plain -> this.transportSecurity = plain;
            case PlainSocketClientLayerContext.Tunnel plain -> this.tunnelSecurity = plain;
            case SocketClientTunnelLayerContext t -> this.tunnel = t;
            case WebSocketLayerContext ws -> this.websocket = ws;
            case WhatsAppLayerContext wa -> this.app = wa;
        }
        rebuildChain();
    }

    /**
     * Returns the tunnel layer context, if present.
     *
     * @return an optional containing the tunnel context, or empty if
     *         no tunnel layer is registered
     */
    Optional<SocketClientTunnelLayerContext> tunnelContext() {
        return Optional.ofNullable(tunnel);
    }

    /**
     * Finds the first layer context that is currently handshaking.
     *
     * @return an optional containing the handshaking context, or empty
     *         if no layer is handshaking
     */
    Optional<SocketClientLayerContext> findHandshakingContext() {
        return findFirst(SocketClientLayerContext::isHandshaking);
    }

    /**
     * Returns whether any layer context has pending output data.
     *
     * @return {@code true} if any layer has pending output
     */
    boolean hasPendingOutput() {
        return findFirst(SocketClientLayerContext::hasPendingOutput).isPresent();
    }

    /**
     * Finds the first layer context that has pending delegated tasks.
     *
     * @return an optional containing the context with pending tasks,
     *         or empty if none
     */
    Optional<SocketClientLayerContext> findTasksPendingContext() {
        return findFirst(SocketClientLayerContext::isTasksPending);
    }

    /**
     * Drains buffered data from all layer contexts into their next layers.
     *
     * @return {@code true} if all layers drained successfully,
     *         {@code false} if any layer signalled close
     * @throws IOException if layer processing fails
     */
    boolean drainAllLayers() throws IOException {
        if (transportSecurity != null && !transportSecurity.drainToNextLayer()) return false;
        if (tunnelSecurity != null && !tunnelSecurity.drainToNextLayer()) return false;
        if (tunnel != null && !tunnel.drainToNextLayer()) return false;
        if (websocket != null && !websocket.drainToNextLayer()) return false;
        if (app != null && !app.drainToNextLayer()) return false;
        return true;
    }

    /**
     * Rebuilds the {@code nextLayer} chain by walking the fields in
     * bottom-to-top order and linking each context to the next.
     */
    private void rebuildChain() {
        linkLayers(transportSecurity, tunnelSecurity, tunnel, websocket, app);
    }

    /**
     * Links non-null contexts in the given order, setting each one's
     * {@code nextLayer} to the next non-null context.
     */
    private static void linkLayers(SocketClientLayerContext... layers) {
        SocketClientLayerContext previous = null;
        for (var ctx : layers) {
            if (ctx == null) continue;
            if (previous != null) {
                previous.setNextLayer(ctx);
            }
            previous = ctx;
        }
    }

    /**
     * Returns the first layer context matching the predicate, walking
     * fields in bottom-to-top order.
     */
    private Optional<SocketClientLayerContext> findFirst(Predicate<SocketClientLayerContext> predicate) {
        if (transportSecurity != null && predicate.test(transportSecurity)) return Optional.of(transportSecurity);
        if (tunnelSecurity != null && predicate.test(tunnelSecurity)) return Optional.of(tunnelSecurity);
        if (tunnel != null && predicate.test(tunnel)) return Optional.of(tunnel);
        if (websocket != null && predicate.test(websocket)) return Optional.of(websocket);
        if (app != null && predicate.test(app)) return Optional.of(app);
        return Optional.empty();
    }
}
