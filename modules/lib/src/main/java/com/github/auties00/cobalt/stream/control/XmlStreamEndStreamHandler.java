package com.github.auties00.cobalt.stream.control;

import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.stream.SocketStream;

/**
 * Handles the {@code <xmlstreamend>} stanza emitted by the WhatsApp server
 * when it intends to close the underlying stream without an explicit error.
 *
 * <p>The stanza carries no payload of interest; it simply signals that the
 * server has finished sending data and will close the socket shortly. This
 * handler logs the event for diagnostics and relies on the transport layer
 * to observe the subsequent socket close and tear down the session.
 *
 * @implNote WA Web does not expose a dedicated module for this stanza since
 * stream termination is handled implicitly by the WebSocket/NoiseSocket
 * layer. The Cobalt handler exists to distinguish an orderly server-side
 * shutdown from a transport-level disconnect.
 */
public final class XmlStreamEndStreamHandler implements SocketStream.Handler {
    /**
     * Logger for diagnostic messages when the server signals stream end.
     */
    private static final System.Logger LOGGER = System.getLogger(XmlStreamEndStreamHandler.class.getName());

    /**
     * Constructs a new {@code xmlstreamend} stanza handler.
     *
     * <p>No dependencies are required because the handler only logs the
     * stanza and defers all reconnection behaviour to the socket layer.
     */
    public XmlStreamEndStreamHandler() {
    }

    /**
     * Logs the reception of the {@code <xmlstreamend>} stanza.
     *
     * <p>The socket will be closed by the server shortly after this stanza
     * is delivered; the application layer does not need to take any action
     * here because the transport layer will observe the close and trigger
     * reconnection (or not) based on the client's error handler configuration.
     *
     * @param node the {@code <xmlstreamend>} stanza received from the server
     */
    @Override
    public void handle(Node node) {
        LOGGER.log(System.Logger.Level.INFO, "Received xmlstreamend stanza; waiting for socket close");
    }
}
