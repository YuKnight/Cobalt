package com.github.auties00.cobalt.model.newsletter;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The role of the current viewer within a newsletter, determining their
 * level of access and permissions.
 */
@ProtobufEnum
public enum NewsletterViewerRole {
    /**
     * The role is not known or was not provided by the server.
     */
    UNKNOWN(0),

    /**
     * The viewer is the owner of the newsletter.
     */
    OWNER(1),

    /**
     * The viewer is a subscriber of the newsletter.
     */
    SUBSCRIBER(2),

    /**
     * The viewer is an administrator of the newsletter.
     */
    ADMIN(3),

    /**
     * The viewer is a guest with limited access to the newsletter.
     */
    GUEST(4);

    final int index;

    private static final Map<String, NewsletterViewerRole> BY_NAME = Arrays.stream(NewsletterViewerRole.values())
            .collect(Collectors.toMap(entry -> entry.name().toLowerCase(), role -> role));

    private static final Map<Integer, NewsletterViewerRole> BY_INDEX = Arrays.stream(NewsletterViewerRole.values())
            .collect(Collectors.toMap(entry -> entry.index, role -> role));

    /**
     * Returns the {@code NewsletterViewerRole} constant matching the given
     * case-insensitive name, or {@link #UNKNOWN} if no match is found.
     *
     * @param name the role name, may be {@code null}
     * @return the matching role constant, never {@code null}
     */
    public static NewsletterViewerRole of(String name) {
        return name == null ? UNKNOWN : BY_NAME.getOrDefault(name.toLowerCase(), UNKNOWN);
    }

    /**
     * Returns the {@code NewsletterViewerRole} constant matching the given
     * protobuf index, or {@link #UNKNOWN} if no match is found.
     *
     * @param index the protobuf enum index, may be {@code null}
     * @return the matching role constant, never {@code null}
     */
    public static NewsletterViewerRole of(Integer index) {
        return index == null ? UNKNOWN : BY_INDEX.getOrDefault(index, UNKNOWN);
    }

    NewsletterViewerRole(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return name();
    }
}
