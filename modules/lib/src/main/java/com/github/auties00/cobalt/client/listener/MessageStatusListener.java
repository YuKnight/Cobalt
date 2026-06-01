package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.message.MessageInfo;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onMessageStatus onMessageStatus} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface MessageStatusListener extends WhatsAppListener {
    /**
     * Notifies the listener that a message's status has changed (sent,
     * delivered, read).
     *
     * @param whatsapp the client emitting the event
     * @param info     the message whose status changed
     */
    void onMessageStatus(LinkedWhatsAppClient whatsapp, MessageInfo info);
}
