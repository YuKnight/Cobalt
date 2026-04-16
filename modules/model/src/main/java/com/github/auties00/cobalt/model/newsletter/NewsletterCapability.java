package com.github.auties00.cobalt.model.newsletter;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enumerates optional features that may be enabled for a newsletter.
 *
 * <p>The server reports an arbitrary subset of these constants per
 * newsletter, allowing clients to hide or show the corresponding UI
 * affordances. Admins use these flags to opt the newsletter into premium
 * or experimental features such as analytics, polls, quizzes, and music
 * sharing.
 */
@ProtobufEnum
public enum NewsletterCapability {
    /**
     * The capability was not reported by the server or is not recognized
     * by this version of the client.
     */
    UNKNOWN(0),

    /**
     * The newsletter exposes analytics and insights dashboards to its
     * admins.
     */
    INSIGHTS(1),

    /**
     * Admins may publish polls that include photo attachments.
     */
    PHOTO_POLLS(2),

    /**
     * Admins may publish question posts that collect text responses from
     * subscribers.
     */
    QUESTIONS(3),

    /**
     * The newsletter receives admin-targeted notifications.
     */
    ADMIN_NOTIFICATIONS(4),

    /**
     * The admin UI surfaces a dedicated button for inviting additional
     * admins.
     */
    INVITE_ADMINS_BUTTON(5),

    /**
     * Admins may generate invite links or QR codes aimed at followers.
     */
    INVITE_FOLLOWERS(6),

    /**
     * Admins may publish quiz posts with multiple-choice questions.
     */
    QUIZ(7),

    /**
     * Admins may share sticker packs directly into the newsletter.
     */
    SHARE_STICKER_PACKS(8),

    /**
     * Admins may attach music tracks to published messages.
     */
    MUSIC(9),

    /**
     * The newsletter exposes the admin-profile attribution feature.
     */
    ADMIN_PROFILE(10),

    /**
     * The client surfaces a nudge encouraging admins to pin important
     * messages.
     */
    PINNING_NUDGE(11),

    /**
     * The client surfaces the thread-menu feature inside the newsletter
     * view.
     */
    THREAD_MENU(12);

    /**
     * Lookup table from the lowercase enum name to the constant, used by
     * {@link #of(String)} for case-insensitive parsing.
     */
    private static final Map<String, NewsletterCapability> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(key -> key.name().toLowerCase(), Function.identity()));

    /**
     * The protobuf wire index associated with this constant.
     */
    final int index;

    /**
     * Constructs a new enum constant bound to the supplied protobuf wire
     * index.
     *
     * @param index the protobuf wire index
     */
    NewsletterCapability(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    /**
     * Returns the constant whose name matches the supplied string,
     * case-insensitively.
     *
     * @param name the capability name as reported by the server, may be
     *             {@code null}
     * @return the matching capability constant, or {@link #UNKNOWN} when
     *         {@code name} is {@code null} or does not match any constant
     */
    static NewsletterCapability of(String name) {
        return name == null ? UNKNOWN : BY_NAME.getOrDefault(name.toLowerCase(), UNKNOWN);
    }

    /**
     * Returns the name of this enum constant.
     *
     * @return the constant name as written in source code
     */
    @Override
    public String toString() {
        return name();
    }
}
