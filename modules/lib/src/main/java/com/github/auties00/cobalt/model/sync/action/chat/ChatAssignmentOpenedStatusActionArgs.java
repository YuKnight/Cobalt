package com.github.auties00.cobalt.model.sync.action.chat;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link ChatAssignmentOpenedStatusAction}.
 *
 * <p>The sync index produced is
 * {@code ["agentChatAssignmentOpenedStatus", chatJid, agentId]}.
 *
 * @param chatJid the JID of the chat whose assignment opened-status is being tracked
 * @param agentId the device-agent identifier that opened the chat
 */
public record ChatAssignmentOpenedStatusActionArgs(Jid chatJid, String agentId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a two-element array containing the chat JID string and the agent identifier
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{chatJid.toString(), agentId};
    }
}
