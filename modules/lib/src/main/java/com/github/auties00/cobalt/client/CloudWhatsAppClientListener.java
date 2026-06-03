package com.github.auties00.cobalt.client;

import com.github.auties00.cobalt.listener.cloud.*;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.client.CloudWhatsAppClient;
import com.github.auties00.cobalt.model.cloud.CloudCallEvent;
import com.github.auties00.cobalt.model.cloud.CloudMessageStatus;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.model.message.MessageKey;

/**
 * Aggregator listener for every event a {@link CloudWhatsAppClient} emits.
 *
 * <p>This interface extends each single-method Cloud listener in this package and supplies an empty
 * default implementation for every callback, so an embedder can implement only the events they care
 * about. It mirrors the structure of {@code LinkedWhatsAppClientListener}: register an instance once
 * through {@link CloudWhatsAppClient#addListener(WhatsAppCloudListener)} to observe everything, or register
 * any of the single-method relatives as a lambda to observe one event in isolation.
 *
 * @see CloudWhatsAppClient
 */
public non-sealed interface CloudWhatsAppClientListener extends WhatsAppCloudListener, CloudNewMessageListener,
        CloudMessageStatusListener,
        CloudMessageEchoListener,
        CloudCallListener,
        CloudTemplateStatusListener,
        CloudTemplateQualityListener,
        CloudTemplateCategoryListener,
        CloudPhoneNumberListener,
        CloudAccountUpdateListener,
        CloudBusinessCapabilityListener,
        CloudUserPreferenceListener,
        CloudFlowListener,
        CloudHistoryListener,
        CloudConnectedListener,
        CloudDisconnectedListener,
        CloudNodeReceivedListener,
        CloudErrorListener {
    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onNewMessage(CloudWhatsAppClient whatsapp, MessageInfo info) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onMessageStatus(CloudWhatsAppClient whatsapp, MessageKey key, CloudMessageStatus status) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onMessageEcho(CloudWhatsAppClient whatsapp, MessageInfo info) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onCall(CloudWhatsAppClient whatsapp, CloudCallEvent event) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onTemplateStatus(CloudWhatsAppClient whatsapp, JSONObject value) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onTemplateQuality(CloudWhatsAppClient whatsapp, JSONObject value) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onTemplateCategory(CloudWhatsAppClient whatsapp, JSONObject value) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onPhoneNumberUpdate(CloudWhatsAppClient whatsapp, JSONObject value) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onAccountUpdate(CloudWhatsAppClient whatsapp, JSONObject value) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onBusinessCapabilityUpdate(CloudWhatsAppClient whatsapp, JSONObject value) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onUserPreference(CloudWhatsAppClient whatsapp, JSONObject value) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onFlowStatus(CloudWhatsAppClient whatsapp, JSONObject value) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onHistorySync(CloudWhatsAppClient whatsapp, JSONObject value) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onConnected(CloudWhatsAppClient whatsapp) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onDisconnected(CloudWhatsAppClient whatsapp) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onWebhookReceived(CloudWhatsAppClient whatsapp, JSONObject envelope) {

    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation does nothing; override to handle the event.
     */
    @Override
    default void onError(CloudWhatsAppClient whatsapp, Throwable error) {

    }
}
