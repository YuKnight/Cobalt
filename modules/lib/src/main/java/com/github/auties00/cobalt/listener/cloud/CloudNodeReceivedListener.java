package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onWebhookReceived onWebhookReceived}
 * event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event delivers the
 * raw, signature-verified webhook envelope before it is decoded into typed events, mirroring the
 * Linked client's raw node listener.
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudNodeReceivedListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener of a raw webhook envelope.
     *
     * @param whatsapp the client emitting the event
     * @param envelope the raw {@code object=whatsapp_business_account} envelope
     */
    void onWebhookReceived(CloudWhatsAppClient whatsapp, JSONObject envelope);
}
