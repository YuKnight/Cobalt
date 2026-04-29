package com.github.auties00.cobalt.node.iq.group;

/**
 * Closed set of dispatch modes — selects between the
 * invite-link and invite-message wire shapes.
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
