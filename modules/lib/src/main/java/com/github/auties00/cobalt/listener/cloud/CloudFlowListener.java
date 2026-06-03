package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onFlowStatus onFlowStatus} event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event delivers the
 * {@code flows} webhook change value (a Flow status transition).
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudFlowListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener of a Flow status update.
     *
     * @param whatsapp the client emitting the event
     * @param value    the raw {@code flows} change value
     */
    void onFlowStatus(CloudWhatsAppClient whatsapp, JSONObject value);
}
