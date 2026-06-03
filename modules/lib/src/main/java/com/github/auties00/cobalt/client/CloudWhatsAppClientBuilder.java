package com.github.auties00.cobalt.client;

import com.github.auties00.cobalt.model.cloud.CloudApiVersion;
import com.github.auties00.cobalt.store.CloudWhatsAppStoreBuilder;

import java.net.http.HttpClient;
import java.util.Objects;

/**
 * A fluent builder that constructs {@link CloudWhatsAppClient} instances.
 *
 * <p>The builder collects the Cloud credentials (access token, phone number id, and the optional
 * WhatsApp Business Account and business portfolio ids), the graph API version, the app secret used
 * for request proofs and webhook signature verification, and the built-in webhook receiver
 * configuration. {@link #build()} validates that the required credentials are present and produces a
 * configured client.
 *
 * @see CloudWhatsAppClient
 * @see WhatsAppClientBuilder#cloud()
 */
public final class CloudWhatsAppClientBuilder {
    /**
     * The default webhook URL path.
     */
    private static final String DEFAULT_WEBHOOK_PATH = "/webhook";

    /**
     * The system-user access token.
     */
    private String accessToken;

    /**
     * The operating phone number id.
     */
    private String phoneNumberId;

    /**
     * The WhatsApp Business Account id, or {@code null} when management edges are unused.
     */
    private String whatsappBusinessAccountId;

    /**
     * The business portfolio id, or {@code null} when onboarding edges are unused.
     */
    private String businessId;

    /**
     * The graph API version.
     */
    private CloudApiVersion apiVersion = CloudApiVersion.DEFAULT;

    /**
     * The app secret, or {@code null} when proofs and signature checks are disabled.
     */
    private String appSecret;

    /**
     * The webhook verify token, or {@code null} when the receiver is disabled.
     */
    private String webhookVerifyToken;

    /**
     * The webhook bind address, or {@code null} to bind the wildcard address.
     */
    private String webhookBindAddress;

    /**
     * The webhook port, or {@code 0} when the receiver is disabled.
     */
    private int webhookPort;

    /**
     * The webhook URL path.
     */
    private String webhookPath = DEFAULT_WEBHOOK_PATH;

    /**
     * The error handler installed on the future client, or {@code null} to use the default.
     */
    private WhatsAppClientErrorHandler errorHandler;

    /**
     * The HTTP client used by the transport, or {@code null} to use a default-configured one.
     */
    private HttpClient httpClient;

    /**
     * Package-private constructor used by {@link WhatsAppClientBuilder#cloud()}.
     */
    CloudWhatsAppClientBuilder() {

    }

    /**
     * Sets the system-user access token.
     *
     * @param accessToken the access token
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder accessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Sets the operating phone number id.
     *
     * @param phoneNumberId the phone number id
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder phoneNumberId(String phoneNumberId) {
        this.phoneNumberId = phoneNumberId;
        return this;
    }

    /**
     * Sets the WhatsApp Business Account id used by template and phone-number management edges.
     *
     * @param whatsappBusinessAccountId the WABA id, or {@code null} to leave it unset
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder whatsappBusinessAccountId(String whatsappBusinessAccountId) {
        this.whatsappBusinessAccountId = whatsappBusinessAccountId;
        return this;
    }

    /**
     * Sets the business portfolio id used by partner onboarding edges.
     *
     * @param businessId the business id, or {@code null} to leave it unset
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder businessId(String businessId) {
        this.businessId = businessId;
        return this;
    }

    /**
     * Sets the graph API version targeted by every request.
     *
     * @param apiVersion the API version, or {@code null} to keep the default
     *                   ({@link CloudApiVersion#DEFAULT})
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder apiVersion(CloudApiVersion apiVersion) {
        if (apiVersion != null) {
            this.apiVersion = apiVersion;
        }
        return this;
    }

    /**
     * Sets the app secret used for {@code appsecret_proof} and webhook signature verification.
     *
     * @param appSecret the app secret, or {@code null} to disable proofs and signature checks
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder appSecret(String appSecret) {
        this.appSecret = appSecret;
        return this;
    }

    /**
     * Configures the built-in webhook receiver.
     *
     * <p>Both a verify token and a port are required to start the receiver; the verify token is
     * echoed during the subscription handshake and the port is where the receiver binds. Omitting
     * either leaves the client send-only.
     *
     * @param verifyToken the webhook verify token
     * @param port        the TCP port to bind
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder webhook(String verifyToken, int port) {
        this.webhookVerifyToken = verifyToken;
        this.webhookPort = port;
        return this;
    }

    /**
     * Sets the webhook bind address.
     *
     * @param bindAddress the bind address, or {@code null} to bind the wildcard address
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder webhookBindAddress(String bindAddress) {
        this.webhookBindAddress = bindAddress;
        return this;
    }

    /**
     * Sets the webhook URL path.
     *
     * @param path the URL path, or {@code null} to keep the default {@code /webhook}
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder webhookPath(String path) {
        if (path != null) {
            this.webhookPath = path;
        }
        return this;
    }

    /**
     * Sets the error handler that decides how the future client reacts to failures.
     *
     * @param errorHandler the error handler, or {@code null} to use the default terminal-printing
     *                     handler
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder errorHandler(WhatsAppClientErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    /**
     * Sets the HTTP client used by the transport.
     *
     * @param httpClient the HTTP client, or {@code null} to use a default-configured one
     * @return this builder, for chaining
     */
    public CloudWhatsAppClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Builds the configured Cloud client.
     *
     * @return the configured client
     * @throws NullPointerException if no access token or phone number id was supplied
     */
    public CloudWhatsAppClient build() {
        Objects.requireNonNull(accessToken, "accessToken must be set");
        Objects.requireNonNull(phoneNumberId, "phoneNumberId must be set");
        var store = new CloudWhatsAppStoreBuilder()
                .accessToken(accessToken)
                .phoneNumberId(phoneNumberId)
                .whatsappBusinessAccountId(whatsappBusinessAccountId)
                .businessId(businessId)
                .apiVersion(apiVersion.version())
                .appSecret(appSecret)
                .webhookVerifyToken(webhookVerifyToken)
                .webhookBindAddress(webhookBindAddress)
                .webhookPort(webhookPort == 0 ? null : webhookPort)
                .webhookPath(webhookPath)
                .build();
        var resolvedErrorHandler = Objects.requireNonNullElseGet(errorHandler, WhatsAppClientErrorHandler::toTerminal);
        var resolvedHttpClient = Objects.requireNonNullElseGet(httpClient, HttpClient::newHttpClient);
        return new LiveCloudWhatsAppClient(store, resolvedErrorHandler, resolvedHttpClient);
    }
}
