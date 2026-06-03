package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onHistorySync onHistorySync} event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event delivers the
 * {@code history} webhook change value sent for newly onboarded numbers.
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudHistoryListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener of a history-sync delivery.
     *
     * @param whatsapp the client emitting the event
     * @param value    the raw {@code history} change value
     */
    void onHistorySync(CloudWhatsAppClient whatsapp, JSONObject value);
}
