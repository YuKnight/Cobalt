package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onLoggedIn onLoggedIn} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface LoggedInListener extends WhatsAppListener {
    /**
     * Notifies the listener that a successful connection and login to a
     * WhatsApp account has been established.
     *
     * <p>When this event fires, data such as chats and contacts may not
     * yet be loaded into memory. Use the corresponding event handlers for
     * specific data types, such as
     * {@link ChatsListener#onChats onChats} and
     * {@link ContactsListener#onContacts onContacts}.
     *
     * @param whatsapp the client emitting the event
     */
    void onLoggedIn(LinkedWhatsAppClient whatsapp);
}
