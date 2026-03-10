package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.RemoveRecentStickerAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles remove recent sticker actions.
 */
public final class RemoveRecentStickerHandler implements WebAppStateActionHandler {
    public static final RemoveRecentStickerHandler INSTANCE = new RemoveRecentStickerHandler();

    private RemoveRecentStickerHandler() {
    }

    @Override
    public String actionName() {
        return RemoveRecentStickerAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return RemoveRecentStickerAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return RemoveRecentStickerAction.ACTION_VERSION;
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

        var stickerHash = JSON.parseArray(mutation.index()).getString(1);
        if (stickerHash == null || stickerHash.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        var sticker = client.store().findRecentSticker(stickerHash);
        if (sticker.isEmpty()) {
            return MutationApplicationResult.orphan(stickerHash, "RecentSticker");
        }

        var action = mutation.value().action().orElse(null) instanceof RemoveRecentStickerAction entry ? entry : null;
        var lastStickerSentTs = action != null
                ? action.lastStickerSentTs().map(java.time.Instant::getEpochSecond).orElse(null)
                : null;
        var stickerTimestamp = sticker.get().timestamp().orElse(0L);
        if (lastStickerSentTs == null || stickerTimestamp <= lastStickerSentTs) {
            client.store().removeRecentSticker(stickerHash);
        }

        return MutationApplicationResult.success();
    }
}
