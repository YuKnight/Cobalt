package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.StickerAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles favorite sticker actions.
 *
 * <p>This handler processes mutations that add or remove stickers from favorites.
 *
 * <p>Index format: ["favoriteStickerAction", "stickerHash"]
 */
public final class FavoriteStickerHandler implements WebAppStateActionHandler {
    public static final FavoriteStickerHandler INSTANCE = new FavoriteStickerHandler();

    private FavoriteStickerHandler() {

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
                    .addFavouriteSticker(stickerHash, action.toSticker());
        } else {
            client.store()
                    .removeFavouriteSticker(stickerHash);
        }

        return true;
    }
}
