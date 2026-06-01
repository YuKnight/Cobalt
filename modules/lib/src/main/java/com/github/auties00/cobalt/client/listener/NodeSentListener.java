package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.node.Node;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onNodeSent onNodeSent} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface NodeSentListener extends WhatsAppListener {
    /**
     * Notifies the listener that a node has been sent to the WhatsApp
     * server.
     *
     * @param whatsapp the client emitting the event
     * @param outgoing the node that was sent
     */
    void onNodeSent(LinkedWhatsAppClient whatsapp, Node outgoing);
}
