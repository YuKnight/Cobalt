package com.github.auties00.cobalt.client;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.cloud.CloudApiClient;
import com.github.auties00.cobalt.cloud.CloudMessageEncoder;
import com.github.auties00.cobalt.store.CloudWhatsAppStore;
import com.github.auties00.cobalt.cloud.CloudWebhookDecoder;
import com.github.auties00.cobalt.cloud.CloudWebhookServer;
import com.github.auties00.cobalt.listener.cloud.CloudAccountUpdateListener;
import com.github.auties00.cobalt.listener.cloud.CloudBusinessCapabilityListener;
import com.github.auties00.cobalt.listener.cloud.CloudCallListener;
import com.github.auties00.cobalt.listener.cloud.CloudConnectedListener;
import com.github.auties00.cobalt.listener.cloud.CloudDisconnectedListener;
import com.github.auties00.cobalt.listener.cloud.CloudErrorListener;
import com.github.auties00.cobalt.listener.cloud.CloudFlowListener;
import com.github.auties00.cobalt.listener.cloud.CloudHistoryListener;
import com.github.auties00.cobalt.listener.cloud.CloudMessageEchoListener;
import com.github.auties00.cobalt.listener.cloud.CloudMessageStatusListener;
import com.github.auties00.cobalt.listener.cloud.CloudNewMessageListener;
import com.github.auties00.cobalt.listener.cloud.CloudNodeReceivedListener;
import com.github.auties00.cobalt.listener.cloud.CloudPhoneNumberListener;
import com.github.auties00.cobalt.listener.cloud.CloudTemplateCategoryListener;
import com.github.auties00.cobalt.listener.cloud.CloudTemplateQualityListener;
import com.github.auties00.cobalt.listener.cloud.CloudTemplateStatusListener;
import com.github.auties00.cobalt.listener.cloud.CloudUserPreferenceListener;
import com.github.auties00.cobalt.listener.cloud.WhatsAppCloudListener;
import com.github.auties00.cobalt.listener.WhatsAppListener;
import com.github.auties00.cobalt.model.business.profile.BusinessProfile;
import com.github.auties00.cobalt.model.business.profile.BusinessProfileBuilder;
import com.github.auties00.cobalt.model.cloud.CloudCallEvent;
import com.github.auties00.cobalt.model.cloud.CloudFlow;
import com.github.auties00.cobalt.model.cloud.CloudMessageQr;
import com.github.auties00.cobalt.model.cloud.CloudMessageTemplate;
import com.github.auties00.cobalt.model.cloud.CloudPhoneNumber;
import com.github.auties00.cobalt.model.cloud.CloudRegistrationResult;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidProvider;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.model.message.MessageKey;
import com.github.auties00.cobalt.model.message.MessageKeyBuilder;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Production implementation of {@link CloudWhatsAppClient}.
 *
 * <p>This class wires the Cloud transport ({@link CloudApiClient}), the message encoder/decoder
 * ({@link CloudMessageEncoder}, {@link CloudWebhookDecoder}), the built-in webhook receiver
 * ({@link CloudWebhookServer}), a listener registry, and the configurable error handler. Outbound
 * operations translate to graph requests; inbound webhook deliveries are decoded and dispatched to the
 * registered listeners.
 */
public final class LiveCloudWhatsAppClient implements CloudWhatsAppClient {
    /**
     * The credential and webhook configuration.
     */
    private final CloudWhatsAppStore store;

    /**
     * The configurable error handler.
     */
    private final WhatsAppClientErrorHandler errorHandler;

    /**
     * The HTTP/JSON transport.
     */
    private final CloudApiClient api;

    /**
     * The registered listeners.
     */
    private final CopyOnWriteArraySet<WhatsAppListener> listeners;

    /**
     * The last inbound message id seen per chat, used to adapt {@link #markChatAsRead(JidProvider)}.
     */
    private final Map<Jid, String> lastInboundByChat;

    /**
     * Whether the client is currently connected.
     */
    private final AtomicBoolean connected;

    /**
     * The webhook receiver, or {@code null} when the receiver is disabled.
     */
    private volatile CloudWebhookServer webhookServer;

    /**
     * The latch released on disconnect, used by {@link #waitForDisconnection()}.
     */
    private volatile CountDownLatch disconnectLatch;

    /**
     * Constructs a new live Cloud client.
     *
     * @param store      the credential and webhook configuration
     * @param errorHandler the configurable error handler
     * @param httpClient   the HTTP client backing the transport
     * @throws NullPointerException if any argument is {@code null}
     */
    LiveCloudWhatsAppClient(CloudWhatsAppStore store, WhatsAppClientErrorHandler errorHandler,
                            java.net.http.HttpClient httpClient) {
        this.store = Objects.requireNonNull(store, "store must not be null");
        this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler must not be null");
        this.api = new CloudApiClient(httpClient, store.accessToken(), store.apiVersion(),
                store.appSecret().orElse(null));
        this.listeners = new CopyOnWriteArraySet<>();
        this.lastInboundByChat = new ConcurrentHashMap<>();
        this.connected = new AtomicBoolean();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient connect() {
        if (!connected.compareAndSet(false, true)) {
            throw new IllegalStateException("client already connected");
        }
        api.get(store.phoneNumberId(), Map.of("fields", "id"));
        disconnectLatch = new CountDownLatch(1);
        if (store.hasWebhookReceiver()) {
            var server = new CloudWebhookServer(
                    store.webhookBindAddress().orElse(null),
                    store.webhookPort().orElseThrow(),
                    store.webhookPath(),
                    store.webhookVerifyToken().orElseThrow(),
                    store.appSecret().orElse(null),
                    this::dispatchEnvelope,
                    this::fireError);
            server.start();
            this.webhookServer = server;
        }
        forEach(CloudConnectedListener.class, listener -> listener.onConnected(this));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        if (!connected.compareAndSet(true, false)) {
            return;
        }
        var server = webhookServer;
        if (server != null) {
            server.stop();
            webhookServer = null;
        }
        forEach(CloudDisconnectedListener.class, listener -> listener.onDisconnected(this));
        var latch = disconnectLatch;
        if (latch != null) {
            latch.countDown();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconnect() {
        disconnect();
        connect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return connected.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient waitForDisconnection() {
        var latch = disconnectLatch;
        if (latch != null) {
            try {
                latch.await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addListener(WhatsAppCloudListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener must not be null"));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient removeListener(WhatsAppCloudListener listener) {
        listeners.remove(listener);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageKey sendMessage(JidProvider recipient, MessageContainer message) {
        var body = CloudMessageEncoder.encode(recipient, message);
        var response = api.post(store.phoneNumberId() + "/messages", body);
        return new MessageKeyBuilder()
                .id(firstMessageId(response))
                .parentJid(recipient.toJid())
                .fromMe(true)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(MessageInfo messageInfo) {
        var recipient = messageInfo.key().parentJid()
                .orElseThrow(() -> new IllegalArgumentException("messageInfo key must carry a parentJid"));
        sendMessage(recipient, messageInfo.message());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addReaction(MessageKey messageKey, String emoji) {
        sendReaction(messageKey, emoji);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeReaction(MessageKey messageKey) {
        sendReaction(messageKey, "");
    }

    /**
     * Posts a reaction message for the given target with the given emoji.
     *
     * @param messageKey the key of the message to react to
     * @param emoji      the reaction emoji, or the empty string to clear it
     */
    private void sendReaction(MessageKey messageKey, String emoji) {
        var recipient = messageKey.parentJid()
                .orElseThrow(() -> new IllegalArgumentException("messageKey must carry a parentJid"));
        var body = new JSONObject();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", recipient.user());
        body.put("type", "reaction");
        var reaction = new JSONObject();
        reaction.put("message_id", messageKey.id().orElseThrow(
                () -> new IllegalArgumentException("messageKey must carry an id")));
        reaction.put("emoji", emoji);
        body.put("reaction", reaction);
        api.post(store.phoneNumberId() + "/messages", body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markChatAsRead(JidProvider chat) {
        var lastInbound = lastInboundByChat.get(chat.toJid());
        if (lastInbound == null) {
            return;
        }
        var body = new JSONObject();
        body.put("messaging_product", "whatsapp");
        body.put("status", "read");
        body.put("message_id", lastInbound);
        api.post(store.phoneNumberId() + "/messages", body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendTypingIndicator(MessageKey inboundMessage) {
        var body = new JSONObject();
        body.put("messaging_product", "whatsapp");
        body.put("status", "read");
        body.put("message_id", inboundMessage.id().orElseThrow(
                () -> new IllegalArgumentException("inboundMessage must carry an id")));
        var indicator = new JSONObject();
        indicator.put("type", "text");
        body.put("typing_indicator", indicator);
        api.post(store.phoneNumberId() + "/messages", body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String uploadMedia(byte[] data, String mimeType, String filename) {
        var response = api.uploadMedia(store.phoneNumberId(), data, mimeType, filename);
        return response.getString("id");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String uploadMedia(Path file, String mimeType) {
        try {
            var data = Files.readAllBytes(file);
            return uploadMedia(data, mimeType, file.getFileName().toString());
        } catch (java.io.IOException exception) {
            throw new IllegalArgumentException("failed to read media file: " + file, exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] downloadMedia(String mediaId) {
        return api.download(queryMediaUrl(mediaId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI queryMediaUrl(String mediaId) {
        var response = api.get(mediaId, Map.of());
        return URI.create(response.getString("url"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMedia(String mediaId) {
        api.delete(mediaId, Map.of());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<BusinessProfile> queryBusinessProfile() {
        var response = api.get(store.phoneNumberId() + "/whatsapp_business_profile",
                Map.of("fields", "about,address,description,email,websites,vertical,profile_picture_url"));
        var data = response.getJSONArray("data");
        if (data == null || data.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseBusinessProfile(data.getJSONObject(0)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void editBusinessProfile(BusinessProfile profile) {
        var body = new JSONObject();
        body.put("messaging_product", "whatsapp");
        profile.description().ifPresent(value -> body.put("description", value));
        profile.address().ifPresent(value -> body.put("address", value));
        profile.email().ifPresent(value -> body.put("email", value));
        if (!profile.websites().isEmpty()) {
            var websites = new JSONArray();
            for (var website : profile.websites()) {
                websites.add(website.toString());
            }
            body.put("websites", websites);
        }
        api.post(store.phoneNumberId() + "/whatsapp_business_profile", body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void blockContact(JidProvider contact) {
        api.post(store.phoneNumberId() + "/block_users", blockUsersBody(contact));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unblockContact(JidProvider contact) {
        api.delete(store.phoneNumberId() + "/block_users", blockUsersBody(contact));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Jid> queryBlockedUsers() {
        var response = api.get(store.phoneNumberId() + "/block_users", Map.of());
        var data = response.getJSONArray("data");
        var result = new ArrayList<Jid>();
        if (data != null) {
            for (var index = 0; index < data.size(); index++) {
                var user = data.getJSONObject(index).getString("wa_id");
                if (user != null) {
                    result.add(Jid.of(user, JidServer.user()));
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudMessageTemplate createMessageTemplate(CloudMessageTemplate template) {
        var body = new JSONObject();
        body.put("name", template.name());
        body.put("language", template.language());
        body.put("category", template.category());
        template.components().ifPresent(components -> body.put("components", components));
        var response = api.post(requireWaba() + "/message_templates", body);
        return new CloudMessageTemplate(response.getString("id"), template.name(), template.language(),
                template.category(), response.getString("status"), template.components().orElse(null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CloudMessageTemplate> queryMessageTemplates() {
        var response = api.get(requireWaba() + "/message_templates", Map.of());
        var data = response.getJSONArray("data");
        var result = new ArrayList<CloudMessageTemplate>();
        if (data != null) {
            for (var index = 0; index < data.size(); index++) {
                result.add(parseTemplate(data.getJSONObject(index)));
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CloudMessageTemplate> queryMessageTemplate(String name) {
        var response = api.get(requireWaba() + "/message_templates", Map.of("name", name));
        var data = response.getJSONArray("data");
        if (data == null || data.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseTemplate(data.getJSONObject(0)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void editMessageTemplate(String templateId, CloudMessageTemplate template) {
        var body = new JSONObject();
        body.put("category", template.category());
        template.components().ifPresent(components -> body.put("components", components));
        api.post(templateId, body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMessageTemplate(String name) {
        api.delete(requireWaba() + "/message_templates", Map.of("name", name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudRegistrationResult registerPhoneNumber(String pin) {
        var body = new JSONObject();
        body.put("messaging_product", "whatsapp");
        body.put("pin", pin);
        var response = api.post(store.phoneNumberId() + "/register", body);
        return new CloudRegistrationResult(!response.containsKey("success") || response.getBooleanValue("success"), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deregisterPhoneNumber() {
        api.post(store.phoneNumberId() + "/deregister", new JSONObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestVerificationCode(String codeMethod, String language) {
        api.postForm(store.phoneNumberId() + "/request_code",
                Map.of("code_method", codeMethod, "language", language));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verifyCode(String code) {
        api.postForm(store.phoneNumberId() + "/verify_code", Map.of("code", code));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTwoStepPin(String pin) {
        var body = new JSONObject();
        body.put("pin", pin);
        api.post(store.phoneNumberId(), body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudPhoneNumber queryPhoneNumber() {
        var response = api.get(store.phoneNumberId(),
                Map.of("fields", "id,display_phone_number,verified_name,quality_rating,code_verification_status,status"));
        return parsePhoneNumber(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CloudPhoneNumber> queryPhoneNumbers() {
        var response = api.get(requireWaba() + "/phone_numbers", Map.of());
        var data = response.getJSONArray("data");
        var result = new ArrayList<CloudPhoneNumber>();
        if (data != null) {
            for (var index = 0; index < data.size(); index++) {
                result.add(parsePhoneNumber(data.getJSONObject(index)));
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableCalling() {
        applyCallingStatus("enabled");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disableCalling() {
        applyCallingStatus("disabled");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeApp() {
        api.post(requireWaba() + "/subscribed_apps", new JSONObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> querySubscribedApps() {
        var response = api.get(requireWaba() + "/subscribed_apps", Map.of());
        var data = response.getJSONArray("data");
        var result = new ArrayList<String>();
        if (data != null) {
            for (var index = 0; index < data.size(); index++) {
                var app = data.getJSONObject(index).getJSONObject("whatsapp_business_api_data");
                if (app != null) {
                    result.add(app.getString("id"));
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeApp() {
        api.delete(requireWaba() + "/subscribed_apps", Map.of());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudFlow createFlow(CloudFlow flow) {
        var body = new JSONObject();
        body.put("name", flow.name());
        if (!flow.categories().isEmpty()) {
            var categories = new JSONArray();
            categories.addAll(flow.categories());
            body.put("categories", categories);
        }
        var response = api.post(requireWaba() + "/flows", body);
        return new CloudFlow(response.getString("id"), flow.name(), "DRAFT", flow.categories());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CloudFlow> queryFlows() {
        var response = api.get(requireWaba() + "/flows", Map.of());
        var data = response.getJSONArray("data");
        var result = new ArrayList<CloudFlow>();
        if (data != null) {
            for (var index = 0; index < data.size(); index++) {
                result.add(parseFlow(data.getJSONObject(index)));
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishFlow(String flowId) {
        api.post(flowId + "/publish", new JSONObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deprecateFlow(String flowId) {
        api.post(flowId + "/deprecate", new JSONObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudMessageQr createMessageQr(String prefilledMessage) {
        var body = new JSONObject();
        body.put("prefilled_message", prefilledMessage);
        body.put("generate_qr_image", "PNG");
        var response = api.post(store.phoneNumberId() + "/message_qrdls", body);
        return parseMessageQr(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CloudMessageQr> queryMessageQrs() {
        var response = api.get(store.phoneNumberId() + "/message_qrdls", Map.of());
        var data = response.getJSONArray("data");
        var result = new ArrayList<CloudMessageQr>();
        if (data != null) {
            for (var index = 0; index < data.size(); index++) {
                result.add(parseMessageQr(data.getJSONObject(index)));
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMessageQr(String code) {
        api.delete(store.phoneNumberId() + "/message_qrdls/" + code, Map.of());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addNewMessageListener(CloudNewMessageListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addMessageStatusListener(CloudMessageStatusListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addMessageEchoListener(CloudMessageEchoListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addCallListener(CloudCallListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addTemplateStatusListener(CloudTemplateStatusListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addTemplateQualityListener(CloudTemplateQualityListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addTemplateCategoryListener(CloudTemplateCategoryListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addPhoneNumberListener(CloudPhoneNumberListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addAccountUpdateListener(CloudAccountUpdateListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addBusinessCapabilityListener(CloudBusinessCapabilityListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addUserPreferenceListener(CloudUserPreferenceListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addFlowListener(CloudFlowListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addHistoryListener(CloudHistoryListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addConnectedListener(CloudConnectedListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addDisconnectedListener(CloudDisconnectedListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addNodeReceivedListener(CloudNodeReceivedListener listener) {
        return addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CloudWhatsAppClient addErrorListener(CloudErrorListener listener) {
        return addListener(listener);
    }

    /**
     * Dispatches a verified webhook envelope to the registered listeners.
     *
     * @param envelope the webhook envelope
     */
    private void dispatchEnvelope(JSONObject envelope) {
        forEach(CloudNodeReceivedListener.class, listener -> listener.onWebhookReceived(this, envelope));
        var entries = envelope.getJSONArray("entry");
        if (entries == null) {
            return;
        }
        for (var entryIndex = 0; entryIndex < entries.size(); entryIndex++) {
            var changes = entries.getJSONObject(entryIndex).getJSONArray("changes");
            if (changes == null) {
                continue;
            }
            for (var changeIndex = 0; changeIndex < changes.size(); changeIndex++) {
                var change = changes.getJSONObject(changeIndex);
                dispatchChange(change.getString("field"), change.getJSONObject("value"));
            }
        }
    }

    /**
     * Dispatches a single webhook change to the listeners that match its field.
     *
     * @param field the change field
     * @param value the change value
     */
    private void dispatchChange(String field, JSONObject value) {
        if (field == null || value == null) {
            return;
        }
        switch (field) {
            case "messages" -> dispatchMessages(value);
            case "smb_message_echoes" -> dispatchEchoes(value);
            case "calls" -> dispatchCalls(value);
            case "message_template_status_update" ->
                    forEach(CloudTemplateStatusListener.class, listener -> listener.onTemplateStatus(this, value));
            case "message_template_quality_update" ->
                    forEach(CloudTemplateQualityListener.class, listener -> listener.onTemplateQuality(this, value));
            case "template_category_update" ->
                    forEach(CloudTemplateCategoryListener.class, listener -> listener.onTemplateCategory(this, value));
            case "phone_number_name_update", "phone_number_quality_update" ->
                    forEach(CloudPhoneNumberListener.class, listener -> listener.onPhoneNumberUpdate(this, value));
            case "account_update", "account_alerts", "account_review_update" ->
                    forEach(CloudAccountUpdateListener.class, listener -> listener.onAccountUpdate(this, value));
            case "business_capability_update" ->
                    forEach(CloudBusinessCapabilityListener.class, listener -> listener.onBusinessCapabilityUpdate(this, value));
            case "user_preferences" ->
                    forEach(CloudUserPreferenceListener.class, listener -> listener.onUserPreference(this, value));
            case "flows" ->
                    forEach(CloudFlowListener.class, listener -> listener.onFlowStatus(this, value));
            case "history" ->
                    forEach(CloudHistoryListener.class, listener -> listener.onHistorySync(this, value));
            default -> {
                // Unmodelled field; the raw envelope was already delivered to the node listeners.
            }
        }
    }

    /**
     * Decodes and dispatches inbound messages and outbound statuses of a {@code messages} change.
     *
     * @param value the change value
     */
    private void dispatchMessages(JSONObject value) {
        for (var info : CloudWebhookDecoder.decodeMessages(value)) {
            info.key().parentJid().ifPresent(chat ->
                    info.key().id().ifPresent(id -> lastInboundByChat.put(chat, id)));
            forEach(CloudNewMessageListener.class, listener -> listener.onNewMessage(this, info));
        }
        for (var status : CloudWebhookDecoder.decodeStatuses(value)) {
            forEach(CloudMessageStatusListener.class,
                    listener -> listener.onMessageStatus(this, status.key(), status.status()));
        }
    }

    /**
     * Decodes and dispatches the business message echoes of an {@code smb_message_echoes} change.
     *
     * @param value the change value
     */
    private void dispatchEchoes(JSONObject value) {
        for (var info : CloudWebhookDecoder.decodeMessages(value)) {
            forEach(CloudMessageEchoListener.class, listener -> listener.onMessageEcho(this, info));
        }
    }

    /**
     * Decodes and dispatches the calling events of a {@code calls} change.
     *
     * @param value the change value
     */
    private void dispatchCalls(JSONObject value) {
        var calls = value.getJSONArray("calls");
        if (calls == null) {
            return;
        }
        for (var index = 0; index < calls.size(); index++) {
            var call = calls.getJSONObject(index);
            var timestampValue = call.getLong("timestamp");
            var event = new CloudCallEvent(
                    call.getString("id"),
                    call.getString("event"),
                    call.getString("from"),
                    callSdp(call),
                    timestampValue == null ? null : Instant.ofEpochSecond(timestampValue));
            forEach(CloudCallListener.class, listener -> listener.onCall(this, event));
        }
    }

    /**
     * Extracts the SDP description carried on a calling event, if any.
     *
     * @param call the call object
     * @return the SDP description, or {@code null} when absent
     */
    private static String callSdp(JSONObject call) {
        var store = call.getJSONObject("store");
        return store == null ? null : store.getString("sdp");
    }

    /**
     * Invokes the given action for each registered listener that matches the listener type, routing
     * any failure to the error listeners and the error handler.
     *
     * @param type   the listener type to match
     * @param action the action to run for each matching listener
     * @param <T>    the listener type
     */
    private <T extends WhatsAppListener> void forEach(Class<T> type, Consumer<T> action) {
        for (var listener : listeners) {
            if (type.isInstance(listener)) {
                try {
                    action.accept(type.cast(listener));
                } catch (RuntimeException exception) {
                    fireError(exception);
                }
            }
        }
    }

    /**
     * Routes a processing failure to the error listeners and the configurable error handler.
     *
     * @param error the failure
     */
    private void fireError(Throwable error) {
        for (var listener : listeners) {
            if (listener instanceof CloudErrorListener errorListener) {
                try {
                    errorListener.onError(this, error);
                } catch (RuntimeException ignored) {
                    // A failing error listener must not mask the original failure.
                }
            }
        }
    }

    /**
     * Returns the WhatsApp Business Account id, requiring it to be configured.
     *
     * @return the WABA id
     * @throws IllegalStateException if no WABA id was configured
     */
    private String requireWaba() {
        return store.whatsappBusinessAccountId().orElseThrow(
                () -> new IllegalStateException("operation requires a whatsappBusinessAccountId"));
    }

    /**
     * Posts a calling-status change to the phone number's settings edge.
     *
     * @param status the calling status, {@code "ENABLED"} or {@code "DISABLED"}
     */
    private void applyCallingStatus(String status) {
        var settings = new JSONObject();
        var calling = new JSONObject();
        calling.put("status", status);
        settings.put("calling", calling);
        api.post(store.phoneNumberId() + "/settings", settings);
    }

    /**
     * Builds the {@code block_users} request body for a single contact.
     *
     * @param contact the contact
     * @return the request body
     */
    private static JSONObject blockUsersBody(JidProvider contact) {
        var body = new JSONObject();
        body.put("messaging_product", "whatsapp");
        var users = new JSONArray();
        var user = new JSONObject();
        user.put("user", contact.toJid().user());
        users.add(user);
        body.put("block_users", users);
        return body;
    }

    /**
     * Extracts the {@code wamid} of the first sent message in a messages response.
     *
     * @param response the messages response
     * @return the message id, or {@code null} when absent
     */
    private static String firstMessageId(JSONObject response) {
        var messages = response.getJSONArray("messages");
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        return messages.getJSONObject(0).getString("id");
    }

    /**
     * Parses a phone-number node into a {@link CloudPhoneNumber}.
     *
     * @param node the phone-number node
     * @return the parsed phone number
     */
    private CloudPhoneNumber parsePhoneNumber(JSONObject node) {
        return new CloudPhoneNumber(
                node.getString("id") != null ? node.getString("id") : store.phoneNumberId(),
                node.getString("display_phone_number"),
                node.getString("verified_name"),
                node.getString("quality_rating"),
                node.getString("code_verification_status"),
                node.getString("status"));
    }

    /**
     * Parses a template node into a {@link CloudMessageTemplate}.
     *
     * @param node the template node
     * @return the parsed template
     */
    private static CloudMessageTemplate parseTemplate(JSONObject node) {
        return new CloudMessageTemplate(
                node.getString("id"),
                node.getString("name"),
                node.getString("language"),
                node.getString("category"),
                node.getString("status"),
                node.getJSONArray("components"));
    }

    /**
     * Parses a flow node into a {@link CloudFlow}.
     *
     * @param node the flow node
     * @return the parsed flow
     */
    private static CloudFlow parseFlow(JSONObject node) {
        var categories = new ArrayList<String>();
        var array = node.getJSONArray("categories");
        if (array != null) {
            for (var index = 0; index < array.size(); index++) {
                categories.add(array.getString(index));
            }
        }
        return new CloudFlow(node.getString("id"), node.getString("name"), node.getString("status"), categories);
    }

    /**
     * Parses a QR short-link node into a {@link CloudMessageQr}.
     *
     * @param node the QR node
     * @return the parsed QR short-link
     */
    private static CloudMessageQr parseMessageQr(JSONObject node) {
        return new CloudMessageQr(
                node.getString("code"),
                node.getString("prefilled_message"),
                node.getString("deep_link_url"),
                node.getString("qr_image_url"));
    }

    /**
     * Parses a business-profile node into a {@link BusinessProfile}.
     *
     * @param node the business-profile node
     * @return the parsed business profile
     */
    private BusinessProfile parseBusinessProfile(JSONObject node) {
        var websites = new ArrayList<URI>();
        var array = node.getJSONArray("websites");
        if (array != null) {
            for (var index = 0; index < array.size(); index++) {
                websites.add(URI.create(array.getString(index)));
            }
        }
        return new BusinessProfileBuilder()
                .jid(Jid.of(store.phoneNumberId(), JidServer.user()))
                .description(node.getString("description"))
                .address(node.getString("address"))
                .email(node.getString("email"))
                .websites(websites)
                .build();
    }
}
