package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.LockChatAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles lock chat actions.
 */
public final class LockChatHandler implements WebAppStateActionHandler {
    public static final LockChatHandler INSTANCE = new LockChatHandler();

    private LockChatHandler() {

    }

    @Override
    public String actionName() {
        return LockChatAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return LockChatAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return LockChatAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof LockChatAction action)) {
            return MutationApplicationResult.malformed();
        }

        var chatJidString = JSON.parseArray(mutation.index()).getString(1);
        if (chatJidString == null || chatJidString.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        var chat = client.store().findChatByJid(Jid.of(chatJidString));
        if (chat.isEmpty()) {
            return MutationApplicationResult.orphan(chatJidString, "Chat");
        }

        chat.get().setLocked(action.locked());
        if (action.locked()) {
            chat.get().setArchived(false);
            chat.get().setPinnedTimestamp(null);
        }
        return MutationApplicationResult.success();
    }
}
