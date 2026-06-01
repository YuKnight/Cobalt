package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncAction;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onWebAppStateAction onWebAppStateAction} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface WebAppStateActionListener extends WhatsAppListener {
    /**
     * Notifies the listener that an app-state action has been received
     * from WhatsApp Web.
     *
     * <p>This event is only triggered for web client connections.
     *
     * @param whatsapp the client emitting the event
     * @param action   the action that was executed
     * @param index    the data associated with this action
     */
    void onWebAppStateAction(LinkedWhatsAppClient whatsapp, SyncAction action, String index);
}
