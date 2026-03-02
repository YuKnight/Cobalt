package com.github.auties00.cobalt.model.sync.action.chat;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link ClearChatAction}.
 *
 * <p>The sync index produced is
 * {@code ["clearChat", chatJid, deleteStarred, deleteMedia]}
 * where each boolean flag is serialized as {@code "1"} (true) or {@code "0"} (false).
 *
 * @param chatJid       the JID of the chat being cleared
 * @param deleteStarred whether starred messages should also be deleted
 * @param deleteMedia   whether media files should also be deleted
 */
public record ClearChatActionArgs(Jid chatJid, boolean deleteStarred, boolean deleteMedia) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a three-element array containing the chat JID string and both boolean flags
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{chatJid.toString(), deleteStarred ? "1" : "0", deleteMedia ? "1" : "0"};
    }
}
