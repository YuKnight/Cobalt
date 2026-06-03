package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the
 * {@link CloudWhatsAppClientListener#onBusinessCapabilityUpdate onBusinessCapabilityUpdate} event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event delivers the
 * {@code business_capability_update} webhook change value (the messaging limit tier).
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudBusinessCapabilityListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener of a business-capability update.
     *
     * @param whatsapp the client emitting the event
     * @param value    the raw {@code business_capability_update} change value
     */
    void onBusinessCapabilityUpdate(CloudWhatsAppClient whatsapp, JSONObject value);
}
