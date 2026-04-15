package com.github.auties00.cobalt.model.sync.action.business;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link CtwaPerCustomerDataSharingAction}.
 *
 * <p>The sync index produced is {@code ["ctwaPerCustomerDataSharing", accountLidJid]}.
 *
 * @param accountLidJid the account LID JID identifying the customer whose data-sharing preference is being synced
 */
public record CtwaPerCustomerDataSharingActionArgs(Jid accountLidJid) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the account LID JID string
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{accountLidJid.toString()};
    }
}
