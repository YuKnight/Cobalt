package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.contact.Contact;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onNewContact onNewContact} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface NewContactListener extends WhatsAppListener {
    /**
     * Notifies the listener that a new contact has been added to the
     * contact list.
     *
     * @param whatsapp the client emitting the event
     * @param contact  the new contact
     */
    void onNewContact(LinkedWhatsAppClient whatsapp, Contact contact);
}
