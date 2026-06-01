package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.privacy.StatusPrivacySetting;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onStatusPrivacyChanged onStatusPrivacyChanged} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface StatusPrivacyChangedListener extends WhatsAppListener {
    /**
     * Notifies the listener that the Status story privacy setting
     * was refreshed against the server.
     *
     * @apiNote
     * Fires each time {@link LinkedWhatsAppClient#refreshStatusPrivacy()}
     * commits a fresh server-authoritative value, regardless of
     * whether the value changed.
     *
     * @param whatsapp      the client emitting the event
     * @param statusPrivacy the new authoritative status privacy setting
     */
    void onStatusPrivacyChanged(LinkedWhatsAppClient whatsapp, StatusPrivacySetting statusPrivacy);
}
