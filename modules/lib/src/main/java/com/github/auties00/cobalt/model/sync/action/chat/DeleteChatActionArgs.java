package com.github.auties00.cobalt.model.sync.action.chat;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link DeleteChatAction}.
 *
 * <p>The sync index produced is {@code ["deleteChat", chatJid, deleteMediaFiles]}
 * where the boolean flag is serialized as {@code "1"} (true) or {@code "0"} (false).
 *
 * @param chatJid          the JID of the chat being deleted
 * @param deleteMediaFiles whether associated media files should also be deleted
 */
public record DeleteChatActionArgs(Jid chatJid, boolean deleteMediaFiles) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a two-element array containing the chat JID string and the media deletion flag
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{chatJid.toString(), deleteMediaFiles ? "1" : "0"};
    }
}
