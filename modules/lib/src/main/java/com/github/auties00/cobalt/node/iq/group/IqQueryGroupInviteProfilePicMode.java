package com.github.auties00.cobalt.node.iq.group;

/**
 * Closed set of dispatch modes for an
 * {@link IqQueryGroupInviteProfilePicRequest}. Selects between the
 * invite-link wire shape (used when previewing the group avatar from a
 * {@code chat.whatsapp.com/...} share) and the invite-message wire
 * shape (used when previewing it from an in-chat invite stanza
 * carrying an {@code add_request} payload).
 */
public enum IqQueryGroupInviteProfilePicMode {
    /**
     * The invite-link mode — used when previewing a group avatar
     * for a {@code chat.whatsapp.com/...} share.
     */
    INVITE_LINK,

    /**
     * The invite-message mode — used when previewing a group avatar
     * for an in-chat invite stanza with an {@code add_request}
     * payload.
     */
    INVITE_MESSAGE
}
