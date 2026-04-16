package com.github.auties00.cobalt.model.chat.group;

import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.contact.Contact;
import it.auties.protobuf.annotation.ProtobufEnum;

/**
 * Represents the administrative actions that can be performed on a participant
 * within a WhatsApp group chat.
 *
 * <p>These actions correspond to the group management operations available to
 * administrators through the WhatsApp client. Each action modifies a
 * {@link Contact}'s relationship with a group {@link Chat} (one where
 * {@link Chat#isGroupOrCommunity()} returns {@code true}). The actions can be
 * executed using the participant management methods in {@code WhatsAppClient}.
 *
 * <p>The {@link #data()} method returns the lowercase protocol-level identifier
 * for the action, which is used in the XMPP stanza sent to the server.
 */
@ProtobufEnum
public enum GroupAction {
    /**
     * Adds a contact as a new participant to the group.
     */
    ADD,

    /**
     * Removes an existing participant from the group.
     */
    REMOVE,

    /**
     * Promotes a regular participant to group administrator, granting them
     * elevated permissions such as editing group settings and managing members.
     */
    PROMOTE,

    /**
     * Demotes a group administrator back to a regular participant, revoking
     * their administrative permissions.
     */
    DEMOTE;

    /**
     * Returns the protocol-level identifier for this action.
     *
     * <p>The returned string is the lowercase form of the enum constant name
     * and is used as the action type in XMPP stanzas sent to the WhatsApp
     * server when performing group participant operations.
     *
     * @return the lowercase action identifier, never {@code null}
     */
    public String data() {
        return name().toLowerCase();
    }
}
