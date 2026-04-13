package com.github.auties00.cobalt.model.sync.action.contact;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link UserStatusMuteAction}.
 *
 * <p>The sync index produced is {@code ["userStatusMute", contactJid]}.
 *
 * @param contactJid the JID of the contact whose status updates are being muted or unmuted
 */
public record UserStatusMuteActionArgs(Jid contactJid) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the contact JID string
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{contactJid.toString()};
    }
}
