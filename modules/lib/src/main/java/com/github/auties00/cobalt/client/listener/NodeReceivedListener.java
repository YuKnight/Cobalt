package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.node.Node;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onNodeReceived onNodeReceived} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface NodeReceivedListener extends WhatsAppListener {
    /**
     * Notifies the listener that a node has been received from the
     * WhatsApp server.
     *
     * @param whatsapp the client emitting the event
     * @param incoming the node that was received
     */
    void onNodeReceived(LinkedWhatsAppClient whatsapp, Node incoming);
}
