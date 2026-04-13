package com.github.auties00.cobalt.model.newsletter;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A capability that may be enabled for a newsletter, such as polls,
 * questions, or administrative features.
 */
@ProtobufEnum
public enum NewsletterCapability {
    /**
     * The capability is not known or was not recognized.
     */
    UNKNOWN(0),

    /**
     * Access to newsletter analytics and insights.
     */
    INSIGHTS(1),

    /**
     * Ability to create photo polls.
     */
    PHOTO_POLLS(2),

    /**
     * Ability to create questions.
     */
    QUESTIONS(3),

    /**
     * Receive admin notifications.
     */
    ADMIN_NOTIFICATIONS(4),

    /**
     * Ability to invite new admins.
     */
    INVITE_ADMINS_BUTTON(5),

    /**
     * Ability to invite followers.
     */
    INVITE_FOLLOWERS(6),

    /**
     * Ability to create quizzes.
     */
    QUIZ(7),

    /**
     * Ability to share sticker packs.
     */
    SHARE_STICKER_PACKS(8),

    /**
     * Ability to play music.
     */
    MUSIC(9),

    /**
     * Admin profile feature.
     */
    ADMIN_PROFILE(10),

    /**
     * Pinning nudge feature.
     */
    PINNING_NUDGE(11),

    /**
     * Thread menu feature.
     */
    THREAD_MENU(12);

    private static final Map<String, NewsletterCapability> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(key -> key.name().toLowerCase(), Function.identity()));

    final int index;

    NewsletterCapability(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    /**
     * Returns the {@code NewsletterCapability} constant matching the given
     * case-insensitive name, or {@link #UNKNOWN} if no match is found.
     *
     * @param name the capability name, may be {@code null}
     * @return the matching capability constant, never {@code null}
     */
    static NewsletterCapability of(String name) {
        return name == null ? UNKNOWN : BY_NAME.getOrDefault(name.toLowerCase(), UNKNOWN);
    }

    @Override
    public String toString() {
        return name();
    }
}
