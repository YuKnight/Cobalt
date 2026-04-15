package com.github.auties00.cobalt.model.sync.action.business;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link BusinessBroadcastAssociationAction}.
 *
 * <p>The sync index produced is
 * {@code ["broadcast_jid", broadcastListId, recipientJid]}.
 * This action associates (or disassociates) a recipient JID with a business
 * broadcast list.
 *
 * @param broadcastListId the identifier of the business broadcast list
 * @param recipientJid    the JID string of the recipient being added to or removed from the list
 */
public record BusinessBroadcastAssociationActionArgs(String broadcastListId, String recipientJid) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a two-element array containing the broadcast list identifier and recipient JID
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{broadcastListId, recipientJid};
    }
}
