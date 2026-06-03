package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onConnected onConnected} event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event is raised once
 * the webhook receiver has started and the access token has been validated.
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudConnectedListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener that the client has connected.
     *
     * @param whatsapp the client emitting the event
     */
    void onConnected(CloudWhatsAppClient whatsapp);
}
