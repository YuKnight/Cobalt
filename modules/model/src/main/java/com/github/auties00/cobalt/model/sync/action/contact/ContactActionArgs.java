package com.github.auties00.cobalt.model.sync.action.contact;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link ContactAction}.
 *
 * <p>The sync index produced is {@code ["contact", contactJid]}.
 *
 * @param contactJid the JID of the contact being synced
 */
public record ContactActionArgs(Jid contactJid) implements SyncActionArgs {
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
