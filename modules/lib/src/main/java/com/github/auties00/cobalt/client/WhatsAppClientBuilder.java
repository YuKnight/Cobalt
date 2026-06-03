package com.github.auties00.cobalt.client;

/**
 * Root builder that branches into the two {@link WhatsAppClient} flavours.
 *
 * <p>This is the single entry point reachable through {@link WhatsAppClient#builder()}. It exposes
 * {@link #linked()} for the socket-based Web/Mobile clients and {@link #cloud()} for the Cloud API
 * client; each returns a flavour-specific sub-builder that guides the caller through the steps that
 * apply to that transport.
 *
 * @see WhatsAppClient
 * @see LinkedWhatsAppClientBuilder
 * @see CloudWhatsAppClientBuilder
 */
public final class WhatsAppClientBuilder {
    /**
     * The shared root builder, accessed via {@link WhatsAppClient#builder()}.
     */
    static final WhatsAppClientBuilder INSTANCE = new WhatsAppClientBuilder();

    /**
     * Private singleton constructor; obtain the instance via {@link WhatsAppClient#builder()}.
     */
    private WhatsAppClientBuilder() {

    }

    /**
     * Returns the builder for the socket-based {@link LinkedWhatsAppClient} flavours.
     *
     * <p>The returned builder offers the web companion linking flow, the mobile registration flow,
     * and a low-level custom-store flow.
     *
     * @return the Linked client builder
     */
    public LinkedWhatsAppClientBuilder linked() {
        return LinkedWhatsAppClientBuilder.INSTANCE;
    }

    /**
     * Returns the builder for the {@link CloudWhatsAppClient} flavour.
     *
     * <p>The returned builder collects the Cloud credentials (access token, phone number id, WhatsApp
     * Business Account id) and the webhook receiver configuration before producing the client.
     *
     * @return a fresh Cloud client builder
     */
    public CloudWhatsAppClientBuilder cloud() {
        return new CloudWhatsAppClientBuilder();
    }
}
