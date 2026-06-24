package com.github.auties00.cobalt.store;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The persistent state backing a {@link com.github.auties00.cobalt.client.cloud.CloudWhatsAppClient}.
 *
 * <p>The Cloud transport is stateless beyond its credentials, so this store holds only the access
 * token and identifiers that address the WhatsApp Business Account assets plus the configuration of
 * the inbound webhook receiver. It is the Cloud analogue of {@link LinkedWhatsAppStore}, but it carries
 * credentials and endpoint configuration rather than Signal keys and synced collections, and it is a
 * protobuf message so a session can be serialised and restored.
 */
@ProtobufMessage
public final class CloudWhatsAppStore implements WhatsAppStore {
    /**
     * The system-user access token authenticating every request.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    String accessToken;

    /**
     * The phone number id whose edges send messages and manage the profile.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    String phoneNumberId;

    /**
     * The WhatsApp Business Account id used by template and phone-number management edges, or
     * {@code null} when those operations are not used.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    String whatsappBusinessAccountId;

    /**
     * The business portfolio id used by partner onboarding edges, or {@code null} when unused.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    String businessId;

    /**
     * The graph API version segment, for example {@code v23.0}.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    String apiVersion;

    /**
     * The app secret used for {@code appsecret_proof} and webhook signature verification, or
     * {@code null} when proofs and signature checks are disabled.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    String appSecret;

    /**
     * The webhook verify token echoed during the subscription handshake, or {@code null} when the
     * built-in receiver is disabled.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.STRING)
    String webhookVerifyToken;

    /**
     * The bind address of the webhook receiver, or {@code null} to bind the wildcard address.
     */
    @ProtobufProperty(index = 8, type = ProtobufType.STRING)
    String webhookBindAddress;

    /**
     * The TCP port of the webhook receiver, or {@code null} when the built-in receiver is disabled.
     */
    @ProtobufProperty(index = 9, type = ProtobufType.INT32)
    Integer webhookPort;

    /**
     * The URL path the webhook receiver listens on.
     */
    @ProtobufProperty(index = 10, type = ProtobufType.STRING)
    String webhookPath;

    /**
     * The Meta app id addressed by the Resumable Upload API, or {@code null} when resumable uploads
     * are not used.
     *
     * <p>The Resumable Upload API creates an upload session under the {@code /{APP_ID}/uploads} edge,
     * so unlike the phone-number and WABA edges it is keyed by the application rather than a business
     * asset. It is unset until configured because the other Cloud edges do not need it.
     */
    @ProtobufProperty(index = 11, type = ProtobufType.STRING)
    String appId;

    /**
     * The id of the last inbound message seen per chat, keyed by the bare chat JID string.
     *
     * <p>The Cloud transport marks a chat as read by posting a {@code status: read} update for a message
     * id rather than for a chat, so {@link com.github.auties00.cobalt.client.cloud.CloudWhatsAppClient#markChatAsRead}
     * needs the last inbound message id of the chat. Holding the mapping here rather than in client
     * memory lets it survive a serialise/restore cycle, so a restored client can still mark a chat read
     * for a message that arrived before the restore.
     */
    @ProtobufProperty(index = 12, type = ProtobufType.MAP, mapKeyType = ProtobufType.STRING, mapValueType = ProtobufType.STRING)
    final ConcurrentMap<String, String> lastInboundMessageIdByChat;

    /**
     * Constructs a new Cloud store.
     *
     * @param accessToken               the system-user access token
     * @param phoneNumberId             the operating phone number id
     * @param whatsappBusinessAccountId the WhatsApp Business Account id, or {@code null}
     * @param businessId                the business portfolio id, or {@code null}
     * @param apiVersion                the graph API version segment
     * @param appSecret                 the app secret, or {@code null}
     * @param webhookVerifyToken        the webhook verify token, or {@code null}
     * @param webhookBindAddress        the webhook bind address, or {@code null}
     * @param webhookPort               the webhook port, or {@code null} to disable the receiver
     * @param webhookPath               the webhook URL path
     * @param appId                     the Meta app id used by the Resumable Upload API, or {@code null}
     * @param lastInboundMessageIdByChat the last inbound message id per chat, or {@code null} for an
     *                                   empty map
     */
    CloudWhatsAppStore(String accessToken, String phoneNumberId, String whatsappBusinessAccountId,
                       String businessId, String apiVersion, String appSecret, String webhookVerifyToken,
                       String webhookBindAddress, Integer webhookPort, String webhookPath, String appId,
                       ConcurrentMap<String, String> lastInboundMessageIdByChat) {
        this.accessToken = accessToken;
        this.phoneNumberId = phoneNumberId;
        this.whatsappBusinessAccountId = whatsappBusinessAccountId;
        this.businessId = businessId;
        this.apiVersion = apiVersion;
        this.appSecret = appSecret;
        this.webhookVerifyToken = webhookVerifyToken;
        this.webhookBindAddress = webhookBindAddress;
        this.webhookPort = webhookPort;
        this.webhookPath = webhookPath;
        this.appId = appId;
        this.lastInboundMessageIdByChat = Objects.requireNonNullElseGet(lastInboundMessageIdByChat, ConcurrentHashMap::new);
    }

    /**
     * Returns the system-user access token.
     *
     * @return the access token
     */
    public String accessToken() {
        return accessToken;
    }

    /**
     * Returns the operating phone number id.
     *
     * @return the phone number id
     */
    public String phoneNumberId() {
        return phoneNumberId;
    }

    /**
     * Returns the WhatsApp Business Account id.
     *
     * @return an {@link Optional} carrying the WABA id, or empty when unset
     */
    public Optional<String> whatsappBusinessAccountId() {
        return Optional.ofNullable(whatsappBusinessAccountId);
    }

    /**
     * Returns the business portfolio id.
     *
     * @return an {@link Optional} carrying the business id, or empty when unset
     */
    public Optional<String> businessId() {
        return Optional.ofNullable(businessId);
    }

    /**
     * Returns the graph API version segment.
     *
     * @return the API version, for example {@code v23.0}
     */
    public String apiVersion() {
        return apiVersion;
    }

    /**
     * Returns the app secret used for proofs and signature verification.
     *
     * @return an {@link Optional} carrying the app secret, or empty when unset
     */
    public Optional<String> appSecret() {
        return Optional.ofNullable(appSecret);
    }

    /**
     * Returns the webhook verify token.
     *
     * @return an {@link Optional} carrying the verify token, or empty when the receiver is disabled
     */
    public Optional<String> webhookVerifyToken() {
        return Optional.ofNullable(webhookVerifyToken);
    }

    /**
     * Returns the webhook bind address.
     *
     * @return an {@link Optional} carrying the bind address, or empty to bind the wildcard address
     */
    public Optional<String> webhookBindAddress() {
        return Optional.ofNullable(webhookBindAddress);
    }

    /**
     * Returns the webhook port.
     *
     * @return an {@link OptionalInt} carrying the port, or empty when the receiver is disabled
     */
    public OptionalInt webhookPort() {
        return webhookPort == null || webhookPort == 0 ? OptionalInt.empty() : OptionalInt.of(webhookPort);
    }

    /**
     * Returns the webhook URL path.
     *
     * @return the webhook path
     */
    public String webhookPath() {
        return webhookPath;
    }

    /**
     * Returns the Meta app id used by the Resumable Upload API.
     *
     * @return an {@link Optional} carrying the app id, or empty when resumable uploads are not used
     */
    public Optional<String> appId() {
        return Optional.ofNullable(appId);
    }

    /**
     * Returns the live, mutable mapping of bare chat JID string to the id of the last inbound message
     * seen in that chat.
     *
     * <p>The returned map is the backing {@link ConcurrentMap}, so reads and writes through it are
     * thread-safe and reflected in the persisted state; prefer {@link #recordLastInboundMessageId(String, String)}
     * and {@link #lastInboundMessageId(String)} for the common record-and-lookup operations.
     *
     * @return the last-inbound-message-id map
     */
    public Map<String, String> lastInboundMessageIdByChat() {
        return lastInboundMessageIdByChat;
    }

    /**
     * Records the id of the last inbound message seen in a chat, replacing any earlier id.
     *
     * @param chatJid   the bare chat JID string
     * @param messageId the id of the most recent inbound message of the chat
     * @throws NullPointerException if {@code chatJid} or {@code messageId} is {@code null}
     */
    public void recordLastInboundMessageId(String chatJid, String messageId) {
        Objects.requireNonNull(chatJid, "chatJid must not be null");
        Objects.requireNonNull(messageId, "messageId must not be null");
        lastInboundMessageIdByChat.put(chatJid, messageId);
    }

    /**
     * Returns the id of the last inbound message seen in a chat.
     *
     * @param chatJid the bare chat JID string
     * @return an {@link Optional} carrying the last inbound message id, or empty when none has been
     *         recorded for the chat
     * @throws NullPointerException if {@code chatJid} is {@code null}
     */
    public Optional<String> lastInboundMessageId(String chatJid) {
        Objects.requireNonNull(chatJid, "chatJid must not be null");
        return Optional.ofNullable(lastInboundMessageIdByChat.get(chatJid));
    }

    /**
     * Returns whether the built-in webhook receiver is configured.
     *
     * <p>The receiver requires both a port and a verify token; when either is missing the client runs
     * send-only and inbound deliveries must be handled by the embedder.
     *
     * @return {@code true} if both a webhook port and a verify token are configured
     */
    public boolean hasWebhookReceiver() {
        return webhookPort != null && webhookPort != 0 && webhookVerifyToken != null;
    }

    /**
     * Returns whether two Cloud stores carry the same configuration.
     *
     * @param other the object to compare with
     * @return {@code true} if {@code other} is a {@code CloudWhatsAppStore} with equal fields
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof CloudWhatsAppStore that
                && Objects.equals(accessToken, that.accessToken)
                && Objects.equals(phoneNumberId, that.phoneNumberId)
                && Objects.equals(whatsappBusinessAccountId, that.whatsappBusinessAccountId)
                && Objects.equals(businessId, that.businessId)
                && Objects.equals(apiVersion, that.apiVersion)
                && Objects.equals(appSecret, that.appSecret)
                && Objects.equals(webhookVerifyToken, that.webhookVerifyToken)
                && Objects.equals(webhookBindAddress, that.webhookBindAddress)
                && Objects.equals(webhookPort, that.webhookPort)
                && Objects.equals(webhookPath, that.webhookPath)
                && Objects.equals(appId, that.appId)
                && Objects.equals(lastInboundMessageIdByChat, that.lastInboundMessageIdByChat);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return the hash code of this store's configuration
     */
    @Override
    public int hashCode() {
        return Objects.hash(accessToken, phoneNumberId, whatsappBusinessAccountId, businessId, apiVersion,
                appSecret, webhookVerifyToken, webhookBindAddress, webhookPort, webhookPath, appId,
                lastInboundMessageIdByChat);
    }
}
