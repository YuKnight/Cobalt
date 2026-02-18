package com.github.auties00.cobalt.model.newsletter;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The privacy setting of a newsletter, indicating whether it is publicly
 * discoverable or restricted to invited members only.
 */
@ProtobufEnum
public enum NewsletterPrivacy {
    /**
     * The privacy setting is not known or was not provided by the server.
     */
    UNKNOWN(0),

    /**
     * The newsletter is publicly discoverable and can be found through search.
     */
    PUBLIC(1),

    /**
     * The newsletter is private and can only be joined via an invite link.
     */
    PRIVATE(2);

    private static final Map<String, NewsletterPrivacy> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(key -> key.name().toLowerCase(), Function.identity()));

    final int index;

    NewsletterPrivacy(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    /**
     * Returns the {@code NewsletterPrivacy} constant matching the given
     * case-insensitive name, or {@link #UNKNOWN} if no match is found.
     *
     * @param name the privacy type name, may be {@code null}
     * @return the matching privacy constant, never {@code null}
     */
    static NewsletterPrivacy of(String name) {
        return name == null ? UNKNOWN : BY_NAME.getOrDefault(name.toLowerCase(), UNKNOWN);
    }

    @Override
    public String toString() {
        return name();
    }
}
