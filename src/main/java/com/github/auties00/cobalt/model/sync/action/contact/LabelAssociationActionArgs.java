package com.github.auties00.cobalt.model.sync.action.contact;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link LabelAssociationAction}.
 *
 * <p>The sync index produced is {@code ["label_jid", labelId, chatJid]}.
 * This action associates (or disassociates) a label with a chat or contact.
 *
 * @param labelId the unique identifier of the label
 * @param chatJid the JID string of the chat or contact being labeled
 */
public record LabelAssociationActionArgs(String labelId, String chatJid) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a two-element array containing the label identifier and the chat JID string
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{labelId, chatJid};
    }
}
