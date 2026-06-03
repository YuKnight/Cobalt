package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onAccountUpdate onAccountUpdate}
 * event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event delivers the
 * {@code account_update}, {@code account_alerts}, and {@code account_review_update} webhook change
 * values (restrictions, bans, and verification outcomes).
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudAccountUpdateListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener of an account update.
     *
     * @param whatsapp the client emitting the event
     * @param value    the raw account update change value
     */
    void onAccountUpdate(CloudWhatsAppClient whatsapp, JSONObject value);
}
