package com.github.auties00.cobalt.listener.linked;

import com.github.auties00.cobalt.client.LinkedWhatsAppClientListener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onLocaleChanged onLocaleChanged} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface LinkedLocaleChangedListener extends WhatsAppLinkedListener {
    /**
     * Notifies the listener that the user's locale settings have changed.
     *
     * @param whatsapp  the client emitting the event
     * @param oldLocale the previous locale
     * @param newLocale the new locale
     */
    void onLocaleChanged(LinkedWhatsAppClient whatsapp, String oldLocale, String newLocale);
}
