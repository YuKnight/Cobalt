package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.DeleteMessageForMeAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles delete message for me actions.
 *
 * <p>This handler processes mutations that delete messages locally
 * (not for everyone in the chat).
 *
 * <p>Index format: ["deleteMessageForMe", "chatJid", "messageId", "fromMe", "participant"]
 */
public final class DeleteMessageForMeHandler implements WebAppStateActionHandler {
    public static final DeleteMessageForMeHandler INSTANCE = new DeleteMessageForMeHandler();

    private DeleteMessageForMeHandler() {

    }

    @Override
    public String actionName() {
        return DeleteMessageForMeAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return DeleteMessageForMeAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return DeleteMessageForMeAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        if (!(mutation.value().action().orElse(null) instanceof DeleteMessageForMeAction _)) {
            return false;
        }

        var indexArray = JSON.parseArray(mutation.index());
        var chatJidString = indexArray.getString(1);
        var messageId = indexArray.getString(2);
        // var fromMe = indexArray.getString(3);
        // var participant = indexArray.getString(4);

        var chatJid = Jid.of(chatJidString);

        var chat = client.store()
                .findChatByJid(chatJid);
        if (chat.isEmpty()) {
            return false;
        }

        client.store()
                .findMessageById(chat.get(), messageId)
                .ifPresent(chatMessageInfo -> chat.get().removeMessage(chatMessageInfo.id()));

        return true;
    }
}
