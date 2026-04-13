package com.github.auties00.cobalt.model.sync.action.bot;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link AiThreadRenameAction}.
 *
 * <p>The sync index produced is {@code ["ai_thread_rename", botJid, threadId]}.
 *
 * @param botJid   the bot JID that owns the AI thread
 * @param threadId the identifier of the AI thread being renamed
 */
public record AiThreadRenameActionArgs(Jid botJid, String threadId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a two-element array containing the bot JID string and the thread identifier
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{botJid.toString(), threadId};
    }
}
