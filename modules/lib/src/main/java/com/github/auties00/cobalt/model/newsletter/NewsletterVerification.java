package com.github.auties00.cobalt.model.newsletter;

import it.auties.protobuf.annotation.ProtobufDeserializer;
import it.auties.protobuf.annotation.ProtobufSerializer;

import java.util.Objects;

/**
 * The verification status of a newsletter, indicating whether it has been
 * verified by WhatsApp.
 *
 * <p>This class is serialized as a string: {@code "ON"} for verified
 * newsletters and {@code "OFF"} for unverified ones.
 */
public class NewsletterVerification {
    private static final String ENABLED_JSON_VALUE = "ON";
    private static final String DISABLED_JSON_VALUE = "OFF";

    private static final NewsletterVerification ENABLED = new NewsletterVerification(true);
    private static final NewsletterVerification DISABLED = new NewsletterVerification(false);

    private final boolean verified;

    /**
     * Constructs a new {@code NewsletterVerification} with the specified
     * verified state.
     *
     * @param verified {@code true} if the newsletter is verified
     */
    private NewsletterVerification(boolean verified) {
        this.verified = verified;
    }

    /**
     * Returns the singleton instance representing a verified newsletter.
     *
     * @return the verified instance, never {@code null}
     */
    public static NewsletterVerification enabled() {
        return ENABLED;
    }

    /**
     * Returns the singleton instance representing an unverified newsletter.
     *
     * @return the unverified instance, never {@code null}
     */
    public static NewsletterVerification disabled() {
        return DISABLED;
    }

    /**
     * Deserializes a {@code NewsletterVerification} from the given string
     * value.
     *
     * @param value the serialized value ({@code "ON"} or {@code "OFF"})
     * @return the corresponding {@code NewsletterVerification} instance
     */
    @ProtobufDeserializer
    static NewsletterVerification deserialize(String value) {
        return ENABLED_JSON_VALUE.equals(value) ? ENABLED : DISABLED;
    }

    /**
     * Serializes this instance to its string representation.
     *
     * @return {@code "ON"} if verified, {@code "OFF"} otherwise
     */
    @ProtobufSerializer
    String serialize() {
        return verified ? ENABLED_JSON_VALUE : DISABLED_JSON_VALUE;
    }

    /**
     * Returns whether the newsletter is verified.
     *
     * @return {@code true} if verified, {@code false} otherwise
     */
    public boolean verified() {
        return verified;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof NewsletterVerification that
                            && verified == that.verified;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(verified);
    }
}
