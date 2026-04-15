package com.github.auties00.cobalt.model.sync.action.contact;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link LidContactAction}.
 *
 * <p>The sync index produced is {@code ["lid_contact", contactLidJid]}.
 *
 * @param contactLidJid the LID-based JID of the contact being synced
 */
public record LidContactActionArgs(Jid contactLidJid) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the contact LID JID string
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{contactLidJid.toString()};
    }
}
