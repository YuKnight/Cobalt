package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onTemplateStatus onTemplateStatus}
 * event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event delivers the
 * {@code message_template_status_update} webhook change value (a template approved, rejected, paused,
 * or disabled).
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudTemplateStatusListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener of a message-template status update.
     *
     * @param whatsapp the client emitting the event
     * @param value    the raw {@code message_template_status_update} change value
     */
    void onTemplateStatus(CloudWhatsAppClient whatsapp, JSONObject value);
}
