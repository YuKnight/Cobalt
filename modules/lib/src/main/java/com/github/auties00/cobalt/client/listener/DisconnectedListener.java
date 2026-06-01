package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientDisconnectReason;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onDisconnected onDisconnected} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface DisconnectedListener extends WhatsAppListener {
    /**
     * Notifies the listener that the connection to WhatsApp has been
     * terminated.
     *
     * @param whatsapp the client emitting the event
     * @param reason   the reason for disconnection
     * @see WhatsAppClientDisconnectReason
     */
    void onDisconnected(LinkedWhatsAppClient whatsapp, WhatsAppClientDisconnectReason reason);
}
