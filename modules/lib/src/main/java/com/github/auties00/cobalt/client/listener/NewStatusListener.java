package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onNewStatus onNewStatus} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface NewStatusListener extends WhatsAppListener {
    /**
     * Notifies the listener that a new status update has been received.
     *
     * @param whatsapp the client emitting the event
     * @param status   the new status message
     */
    void onNewStatus(LinkedWhatsAppClient whatsapp, ChatMessageInfo status);
}
