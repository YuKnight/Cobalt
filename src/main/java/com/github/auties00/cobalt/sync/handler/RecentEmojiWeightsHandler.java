package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.RecentEmojiWeightsAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

public final class RecentEmojiWeightsHandler implements WebAppStateActionHandler {
    public static final RecentEmojiWeightsHandler INSTANCE = new RecentEmojiWeightsHandler();

    private RecentEmojiWeightsHandler() {

    }

    @Override
    public String actionName() {
        return RecentEmojiWeightsAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return RecentEmojiWeightsAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return RecentEmojiWeightsAction.ACTION_VERSION;
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

        if (!(mutation.value().action().orElse(null) instanceof RecentEmojiWeightsAction)) {
            return MutationApplicationResult.malformed();
        }

        client.store().setRecentEmojiWeights(((RecentEmojiWeightsAction) mutation.value().action().orElseThrow()).weights());
        return MutationApplicationResult.success();
    }
}
