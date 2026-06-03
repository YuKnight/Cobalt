package com.github.auties00.cobalt.listener.linked;

import com.github.auties00.cobalt.client.LinkedWhatsAppClientListener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.message.MessageInfo;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onMessageReply onMessageReply} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface LinkedMessageReplyListener extends WhatsAppLinkedListener {
    /**
     * Notifies the listener that a message has been sent in reply to a
     * previous message.
     *
     * @param whatsapp the client emitting the event
     * @param response the reply message
     * @param quoted   the message being replied to
     */
    void onMessageReply(LinkedWhatsAppClient whatsapp, MessageInfo response, MessageInfo quoted);
}
