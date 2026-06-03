package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;

/**
 * A functional interface for the {@link CloudWhatsAppClientListener#onTemplateCategory onTemplateCategory}
 * event.
 *
 * <p>{@link CloudWhatsAppClientListener} extends this interface and supplies an empty default
 * implementation, so the event can also be observed in isolation as a lambda. The event delivers the
 * {@code template_category_update} webhook change value.
 *
 * @see CloudWhatsAppClientListener
 */
@FunctionalInterface
public non-sealed interface CloudTemplateCategoryListener extends WhatsAppCloudListener {
    /**
     * Notifies the listener of a message-template category update.
     *
     * @param whatsapp the client emitting the event
     * @param value    the raw {@code template_category_update} change value
     */
    void onTemplateCategory(CloudWhatsAppClient whatsapp, JSONObject value);
}
