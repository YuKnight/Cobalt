package com.github.auties00.cobalt.model.chat;

import it.auties.protobuf.annotation.ProtobufEnum;

/**
 * Represents a permission policy for group or community actions in WhatsApp.
 *
 * <p>WhatsApp groups and communities use boolean toggles to control which members
 * can perform certain actions such as editing the group subject, changing the group
 * description, or sending messages. When the toggle is {@code true} the action is
 * restricted to administrators; when {@code false} any member may perform it. This
 * enum provides a human-readable mapping over those boolean values.
 *
 * <p>Typical uses include controlling who may edit the group info
 * ({@code announce} and {@code restrict} group settings) and who may send messages
 * in announcement groups.
 */
@ProtobufEnum
public enum ChatPolicy {
    /**
     * Permits all group or community members (both regular participants and
     * administrators) to perform the action.
     */
    ANYONE,

    /**
     * Restricts the action to group or community administrators only.
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
}