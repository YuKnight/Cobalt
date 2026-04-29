package com.github.auties00.cobalt.socket.layer.tunnel;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.proxy.WhatsAppProxy;
import com.github.auties00.cobalt.socket.layer.tunnel.impl.DirectSocketClientTunnelLayer;
import com.github.auties00.cobalt.socket.layer.tunnel.impl.HttpSocketClientTunnelLayer;
import com.github.auties00.cobalt.socket.layer.tunnel.impl.SocksSocketClientTunnelLayer;

/**
 * A tunnel layer in the socket client stack.
 *
 * <p>Every connection uses a tunnel layer: proxy connections use
 * {@link HttpSocketClientTunnelLayer} or {@link SocksSocketClientTunnelLayer},
 * and direct connections use {@link DirectSocketClientTunnelLayer}.
 * The tunnel layer registers a {@link SocketClientTunnelLayerContext} during
 * {@code connect()} to support blocking reads in the handshake phase.
 */
public interface SocketClientTunnelLayer extends SocketClientLayer<SocketClientTunnelLayerContext> {
    /**
     * Creates a direct (no-proxy) tunnel layer wrapping the given inner layer.
     *
     * @param innerLayer the layer below (typically a transport layer)
     * @return a new direct tunnel layer
     */
    static SocketClientTunnelLayer newDirectTunnel(SocketClientLayer<?> innerLayer) {
        return new DirectSocketClientTunnelLayer(innerLayer);
    }

    /**
     * Creates a SOCKS tunnel layer wrapping the given inner layer.
     *
     * @param socks      the SOCKS proxy configuration
     * @param innerLayer the layer below
     * @return a new SOCKS tunnel layer
     */
    static SocketClientTunnelLayer newSocksTunnel(WhatsAppProxy.Socks socks, SocketClientLayer<?> innerLayer) {
        return new SocksSocketClientTunnelLayer(socks, innerLayer);
    }

    /**
     * Creates an HTTP CONNECT tunnel layer wrapping the given inner layer.
     *
     * @param http       the HTTP proxy configuration
     * @param innerLayer the layer below
     * @return a new HTTP tunnel layer
     */
    static SocketClientTunnelLayer newHttpTunnel(WhatsAppProxy.Http http, SocketClientLayer<?> innerLayer) {
        return new HttpSocketClientTunnelLayer(http, innerLayer);
    }
}
