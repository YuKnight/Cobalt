package com.github.auties00.cobalt.model.newsletter;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The WAMO (WhatsApp Marketing Offers) subscription status for a newsletter
 * or a viewer's subscription to a newsletter.
 */
@ProtobufEnum
public enum NewsletterWamoSubStatus {
    /**
     * The WAMO subscription status is not known or was not provided.
     */
    UNKNOWN(0),

    /**
     * The WAMO subscription is currently active.
     */
    ACTIVE(1),

    /**
     * The WAMO subscription is currently inactive.
     */
    INACTIVE(2);

    private static final Map<String, NewsletterWamoSubStatus> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(key -> key.name().toLowerCase(), Function.identity()));

    final int index;

    NewsletterWamoSubStatus(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    /**
     * Returns the {@code NewsletterWamoSubStatus} constant matching the given
     * case-insensitive name, or {@link #UNKNOWN} if no match is found.
     *
     * @param name the status name, may be {@code null}
     * @return the matching status constant, never {@code null}
     */
    static NewsletterWamoSubStatus of(String name) {
        return name == null ? UNKNOWN : BY_NAME.getOrDefault(name.toLowerCase(), UNKNOWN);
    }

    @Override
    public String toString() {
        return name();
    }
}
