package com.github.auties00.cobalt.listener.linked;

import com.github.auties00.cobalt.client.LinkedWhatsAppClientListener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.message.MessageInfo;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onMessageDeleted onMessageDeleted} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface LinkedMessageDeletedListener extends WhatsAppLinkedListener {
    /**
     * Notifies the listener that a message has been deleted.
     *
     * @param whatsapp the client emitting the event
     * @param info     the message that was deleted
     * @param everyone {@code true} if the message was deleted for
     *                 everyone, {@code false} if deleted only for the user
     */
    void onMessageDeleted(LinkedWhatsAppClient whatsapp, MessageInfo info, boolean everyone);
}
