package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onPhoneNumberUpdate onPhoneNumberUpdate}
 * event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event delivers the
 * {@code phone_number_name_update} and {@code phone_number_quality_update} webhook change values.
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudPhoneNumberListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener of a phone-number name or quality update.
     *
     * @param whatsapp the client emitting the event
     * @param value    the raw phone-number update change value
     */
    void onPhoneNumberUpdate(CloudWhatsAppClient whatsapp, JSONObject value);
}
