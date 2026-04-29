package com.github.auties00.cobalt.model.chat;

import it.auties.protobuf.annotation.ProtobufEnum;

/**
 * Represents a permission policy for group or community actions in WhatsApp.
 *
 * <p>WhatsApp groups and communities use boolean toggles to control which
 * members can perform certain actions, such as editing the group subject,
 * changing the group description, sending messages in announcement-only
 * groups, adding new participants, or sharing the group's invite link.
 * When the toggle is on, the action is restricted to administrators; when
 * the toggle is off, every member of the group is allowed to perform it.
 * This enum provides a human-readable mapping over those boolean toggles.
 *
 * <p>The same enum also covers the {@code member_link_mode} setting that
 * controls whether the group invite link can be reset only by admins or by
 * any member; the helpers {@link #ofMemberLinkModeMexType(String)} and
 * {@link #ofMemberLinkModeStanzaToken(String)} translate the corresponding
 * server tokens into the appropriate constant.
 */
@ProtobufEnum
public enum ChatPolicy {
    /**
     * The action is open to every group or community member, including
     * regular participants and administrators.
     */
    ANYONE,

    /**
     * The action is restricted to group or community administrators only;
     * regular participants are not allowed to perform it.
     */
    ADMINS;

    /**
     * Returns the {@code ChatPolicy} corresponding to a WhatsApp boolean toggle.
     *
     * <p>A value of {@code true} maps to {@link #ADMINS} (restricted), while
     * {@code false} maps to {@link #ANYONE} (open to all members).
     *
     * @param input the boolean toggle value from WhatsApp
     * @return {@link #ADMINS} if {@code input} is {@code true},
     *         {@link #ANYONE} otherwise
     */
    public static ChatPolicy of(boolean input) {
        return input ? ADMINS : ANYONE;
    }

    /**
     * Returns the {@code ChatPolicy} corresponding to a WhatsApp MEX-type
     * string used by GraphQL responses for the {@code member_link_mode} field.
     *
     * <p>{@code null} input and any unknown value default to {@link #ADMINS},
     * matching the server's behavior of leaving the previous value in place
     * when the field is omitted or carries an unknown enum addition.
     * Recognized values:
     * <ul>
     *   <li>{@code "ALL_MEMBER_LINK"} maps to {@link #ANYONE}.</li>
     *   <li>{@code "ADMIN_LINK"} maps to {@link #ADMINS}.</li>
     * </ul>
     *
     * @param mexType the MEX-type string from the server, or {@code null}
     * @return the corresponding {@code ChatPolicy}, never {@code null}
     */
    public static ChatPolicy ofMemberLinkModeMexType(String mexType) {
        if (mexType == null) {
            return ADMINS;
        }
        return switch (mexType) {
            case "ALL_MEMBER_LINK" -> ANYONE;
            case "ADMIN_LINK" -> ADMINS;
            default -> ADMINS;
        };
    }

    /**
     * Returns the {@code ChatPolicy} corresponding to a WhatsApp stanza-level
     * token from the text content of {@code <member_link_mode>} children of
     * group notification stanzas. The wire-side casing is lowercase, distinct
     * from the uppercase MEX/GraphQL tokens accepted by
     * {@link #ofMemberLinkModeMexType(String)}.
     *
     * <p>{@code null} input and any unknown value default to {@link #ADMINS}.
     * Recognized values:
     * <ul>
     *   <li>{@code "all_member_link"} maps to {@link #ANYONE}.</li>
     *   <li>{@code "admin_link"} maps to {@link #ADMINS}.</li>
     * </ul>
     *
     * @param stanzaToken the lowercase token from a group stanza, or
     *                    {@code null}
     * @return the corresponding {@code ChatPolicy}, never {@code null}
     */
    public static ChatPolicy ofMemberLinkModeStanzaToken(String stanzaToken) {
        if (stanzaToken == null) {
            return ADMINS;
        }
        return switch (stanzaToken) {
            case "all_member_link" -> ANYONE;
            case "admin_link" -> ADMINS;
            default -> ADMINS;
        };
    }
}
