package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onUserPreference onUserPreference}
 * event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event delivers the
 * {@code user_preferences} webhook change value (a marketing opt-out or opt-in).
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudUserPreferenceListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener of a user-preference update.
     *
     * @param whatsapp the client emitting the event
     * @param value    the raw {@code user_preferences} change value
     */
    void onUserPreference(CloudWhatsAppClient whatsapp, JSONObject value);
}
