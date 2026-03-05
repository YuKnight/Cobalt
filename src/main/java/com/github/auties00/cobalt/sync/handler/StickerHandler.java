package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.StickerAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles sticker actions.
 *
 * <p>This handler processes mutations related to sticker packs and usage.
 *
 * <p>Index format: ["stickerAction", "stickerHash"]
 */
public final class StickerHandler implements WebAppStateActionHandler {
    public static final StickerHandler INSTANCE = new StickerHandler();

    private StickerHandler() {

    }

    @Override
    public String actionName() {
        return StickerAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return StickerAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return StickerAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web: WAWebStickersFavoriteSyncAction — only SET is supported
        if (mutation.operation() != SyncdOperation.SET) {
            return true;
        }

        if (!(mutation.value().action().orElse(null) instanceof StickerAction action)) {
            return false;
        }

        var indexArray = JSON.parseArray(mutation.index());
        var stickerHash = indexArray.getString(1);
        if (stickerHash == null) {
            return false;
        }

        // Web uses isFavorite to decide add vs remove
        if (action.isFavorite()) {
            client.store()
                    .addRecentSticker(stickerHash, action.toSticker());
        } else {
            client.store()
                    .removeRecentSticker(stickerHash);
        }

        return true;
    }
}
