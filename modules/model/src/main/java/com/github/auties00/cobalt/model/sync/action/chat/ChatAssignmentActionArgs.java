package com.github.auties00.cobalt.model.sync.action.chat;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link ChatAssignmentAction}.
 *
 * <p>The sync index produced is {@code ["agentChatAssignment", chatJid]}.
 *
 * @param chatJid the JID of the chat being assigned to an agent
 */
public record ChatAssignmentActionArgs(Jid chatJid) implements SyncActionArgs {
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
