package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.message.MessageInfo;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onNewMessage onNewMessage} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface NewMessageListener extends WhatsAppListener {
    /**
     * Notifies the listener that a new message has been received.
     *
     * @param whatsapp the client emitting the event
     * @param info     the message that was received
     */
    void onNewMessage(LinkedWhatsAppClient whatsapp, MessageInfo info);
}
