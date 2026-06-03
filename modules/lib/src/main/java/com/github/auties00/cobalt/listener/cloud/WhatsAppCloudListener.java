package com.github.auties00.cobalt.listener.cloud;

import com.github.auties00.cobalt.client.CloudWhatsAppClientListener;

import com.github.auties00.cobalt.listener.WhatsAppListener;

/**
 * The sealed marker for every event a {@link com.github.auties00.cobalt.client.CloudWhatsAppClient}
 * emits.
 *
 * <p>Each single-method Cloud listener in this package extends this marker, and the aggregator
 * {@link CloudWhatsAppClientListener} extends them all. Registering a listener of this type observes
 * the Cloud webhook event stream; the concrete event is recovered by the dispatcher through a
 * pattern-match.
 */
public sealed interface WhatsAppCloudListener extends WhatsAppListener permits
        CloudAccountUpdateListener,
        CloudBusinessCapabilityListener,
        CloudCallListener,
        CloudConnectedListener,
        CloudDisconnectedListener,
        CloudErrorListener,
        CloudFlowListener,
        CloudHistoryListener,
        CloudMessageEchoListener,
        CloudMessageStatusListener,
        CloudNewMessageListener,
        CloudNodeReceivedListener,
        CloudPhoneNumberListener,
        CloudTemplateCategoryListener,
        CloudTemplateQualityListener,
        CloudTemplateStatusListener,
        CloudUserPreferenceListener,
        CloudWhatsAppClientListener {
}