package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.github.auties00.cobalt.client.CloudWhatsAppClient;
import com.github.auties00.cobalt.model.cloud.CloudMessageStatus;
import com.github.auties00.cobalt.model.message.MessageKey;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onMessageStatus onMessageStatus}
 * event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event is raised for
 * each {@code statuses[]} entry of a webhook delivery whose change field is {@code messages}.
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudMessageStatusListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener that an outbound message changed status.
     *
     * @param whatsapp the client emitting the event
     * @param key      the key of the message whose status changed
     * @param status   the new status
     */
    void onMessageStatus(CloudWhatsAppClient whatsapp, MessageKey key, CloudMessageStatus status);
}
