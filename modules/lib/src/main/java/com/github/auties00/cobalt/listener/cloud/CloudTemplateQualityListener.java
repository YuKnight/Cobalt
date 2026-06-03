package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onTemplateQuality onTemplateQuality}
 * event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event delivers the
 * {@code message_template_quality_update} webhook change value.
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudTemplateQualityListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener of a message-template quality update.
     *
     * @param whatsapp the client emitting the event
     * @param value    the raw {@code message_template_quality_update} change value
     */
    void onTemplateQuality(CloudWhatsAppClient whatsapp, JSONObject value);
}
