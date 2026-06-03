package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.github.auties00.cobalt.client.CloudWhatsAppClient;
import com.github.auties00.cobalt.model.message.MessageInfo;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onNewMessage onNewMessage} event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event is raised for
 * each inbound message decoded from a webhook delivery whose change field is {@code messages}.
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudNewMessageListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener that a new message has been received.
     *
     * @param whatsapp the client emitting the event
     * @param info     the message that was received
     */
    void onNewMessage(CloudWhatsAppClient whatsapp, MessageInfo info);
}
