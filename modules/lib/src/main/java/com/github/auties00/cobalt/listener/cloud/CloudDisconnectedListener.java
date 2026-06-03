package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onDisconnected onDisconnected}
 * event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event is raised once
 * the webhook receiver has stopped.
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudDisconnectedListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener that the client has disconnected.
     *
     * @param whatsapp the client emitting the event
     */
    void onDisconnected(CloudWhatsAppClient whatsapp);
}
