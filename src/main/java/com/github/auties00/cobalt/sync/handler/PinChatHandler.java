package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.PinAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;

/**
 * Handles pin chat actions.
 *
 * <p>This handler processes mutations that pin or unpin chats to the top of the chat list.
 *
 * <p>Index format: ["pinAction", "chatJid", "timestamp"]
 */
public final class PinChatHandler implements WebAppStateActionHandler {
    public static final PinChatHandler INSTANCE = new PinChatHandler();

    private PinChatHandler() {

    }

    @Override
    public String actionName() {
        return PinAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return PinAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return PinAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        if (!(mutation.value().action().orElse(null) instanceof PinAction action)) {
            return false;
        }

        var chatJidString = JSON.parseArray(mutation.index()).getString(1);
        var chatJid = Jid.of(chatJidString);

        var chat = client.store()
                .findChatByJid(chatJid);
        if (chat.isEmpty()) {
            return false;
        }

        if (action.pinned()) {
            chat.get().setPinnedTimestamp(Instant.ofEpochSecond(mutation.timestamp().getEpochSecond()));
            chat.get().setArchived(false);
        } else {
            chat.get().setPinnedTimestamp(null);
        }

        return true;
    }
}
