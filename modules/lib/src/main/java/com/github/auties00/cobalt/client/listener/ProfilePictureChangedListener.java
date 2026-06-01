package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onProfilePictureChanged onProfilePictureChanged} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface ProfilePictureChangedListener extends WhatsAppListener {
    /**
     * Notifies the listener that a contact's profile picture has changed.
     *
     * @param whatsapp the client emitting the event
     * @param jid      the contact whose profile picture changed
     */
    void onProfilePictureChanged(LinkedWhatsAppClient whatsapp, Jid jid);
}
