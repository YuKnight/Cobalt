package com.github.auties00.cobalt.registration.push.apns;

import com.github.auties00.cobalt.registration.push.apns.courier.ApnsPayloadTag;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;

/**
 * Immutable settings that pin an {@link ApnsClient} to a fixed set of
 * iOS bundle identifiers. The courier connection only delivers pushes
 * for topics whose SHA-1 hash appears in the {@link ApnsPayloadTag#FILTER}
 * subscription, so the topics must be known up front.
 *
 * <p>For WhatsApp the canonical list is the four bundle ids the iOS app
 * registers on launch:
 *
 * <pre>{@code
 *   ApnsConfig.of(
 *       "net.whatsapp.WhatsApp",
 *       "net.whatsapp.WhatsApp.voip",
 *       "net.whatsapp.WhatsAppSMB",
 *       "net.whatsapp.WhatsAppSMB.voip");
 * }</pre>
 *
 * <p>Stored inside {@link ApnsSession} so a serialized session round-trips
 * its topic list along with the credentials.
 */
@ProtobufMessage(name = "ApnsConfig")
public final class ApnsConfig {
    /**
     * Configuration for WhatsApp's consumer iOS app
     * ({@code net.whatsapp.WhatsApp}). The first entry is the messaging
     * topic surfaced by {@link ApnsClient#getPushToken()}; the
     * {@code .voip} entry is also subscribed so the courier delivers
     * the VoIP-flavoured silent pushes used during phone-number
     * registration.
     */
    public static final ApnsConfig WHATSAPP_PERSONAL = new ApnsConfig(List.of(
            "net.whatsapp.WhatsApp",
            "net.whatsapp.WhatsApp.voip"));

    /**
     * Configuration for WhatsApp Business' iOS app
     * ({@code net.whatsapp.WhatsAppSMB}). Same shape as
     * {@link #WHATSAPP_PERSONAL} but pinned to the business bundle
     * identifiers.
     */
    public static final ApnsConfig WHATSAPP_BUSINESS = new ApnsConfig(List.of(
            "net.whatsapp.WhatsAppSMB",
            "net.whatsapp.WhatsAppSMB.voip"));

    /**
     * Bundle identifiers we subscribe to. The wire protocol filters
     * pushes by the SHA-1 hash of these strings; they must match the
     * iOS app's {@code CFBundleIdentifier} exactly (case-sensitive).
     * The first entry is treated as the primary messaging topic by
     * {@link ApnsClient#getPushToken()}.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    List<String> topics;

    ApnsConfig(List<String> topics) {
        this.topics = topics == null ? List.of() : List.copyOf(topics);
    }

    /**
     * Convenience factory for the common case of a hard-coded topic
     * list.
     *
     * @param topics one or more bundle identifiers to subscribe to
     * @return a new immutable config
     */
    public static ApnsConfig of(String... topics) {
        return new ApnsConfig(List.of(topics));
    }

    /**
     * @return the configured bundle identifiers, never {@code null}
     */
    public List<String> topics() {
        return topics;
    }
}
