package com.github.auties00.cobalt.model.sync.action.business;

import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link CustomerDataAction}.
 *
 * <p>The sync index produced is {@code ["customer_data", chatJid]}.
 *
 * @implNote WAWebCustomerDataSync — indexArgs: [chatJid]
 * @param chatJid the JID of the chat associated with this customer data
 */
public record CustomerDataActionArgs(String chatJid) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{chatJid};
    }
}
