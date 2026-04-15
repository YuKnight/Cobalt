package com.github.auties00.cobalt.model.sync.action.contact;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link PinAction}.
 *
 * <p>The sync index produced is {@code ["pin_v1", chatJid]}.
 *
 * @param chatJid the JID of the chat being pinned or unpinned
 */
public record PinActionArgs(Jid chatJid) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the chat JID string
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{chatJid.toString()};
    }
}
