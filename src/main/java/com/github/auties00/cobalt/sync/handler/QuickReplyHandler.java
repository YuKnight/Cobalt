package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.preference.QuickReplyBuilder;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.QuickReplyAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles quick reply actions.
 *
 * <p>This handler processes mutations that create, update, or delete quick reply templates
 * for business accounts.
 *
 * <p>Index format: ["quick_reply", "quickReplyId"]
 */
public final class QuickReplyHandler implements WebAppStateActionHandler {
    public static final QuickReplyHandler INSTANCE = new QuickReplyHandler();

    private QuickReplyHandler() {

    }

    @Override
    public String actionName() {
        return QuickReplyAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return QuickReplyAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return QuickReplyAction.ACTION_VERSION;
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

        if (!(mutation.value().action().orElse(null) instanceof QuickReplyAction action)) {
            return MutationApplicationResult.malformed();
        }

        var indexArray = JSON.parseArray(mutation.index());
        var quickReplyId = indexArray.getString(1);
        if (quickReplyId == null) {
            return MutationApplicationResult.malformed();
        }

        if (action.deleted()) {
            client.store().removeQuickReply(quickReplyId);
            return MutationApplicationResult.success();
        }

        var shortcut = action.shortcut().orElse(null);
        var message = action.message().orElse(null);
        if (shortcut == null || shortcut.isEmpty() || message == null || message.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        var count = action.count().orElse(0);
        var quickReply = new QuickReplyBuilder()
                .shortcut(shortcut)
                .message(message)
                .keywords(action.keywords())
                .count(count)
                .build();
        client.store().addQuickReply(quickReply);
        return MutationApplicationResult.success();
    }
}
