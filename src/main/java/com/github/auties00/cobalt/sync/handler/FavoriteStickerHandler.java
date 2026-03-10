package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.StickerAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
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
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof StickerAction action)) {
            return MutationApplicationResult.malformed();
        }

        var indexArray = JSON.parseArray(mutation.index());
        var stickerHash = indexArray.getString(1);
        if (stickerHash == null || stickerHash.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        var featureEnabled = client.abPropsService().getBool(ABProp.STICKER_MD_FAVORITE_STICKERS_ENABLED)
                || client.abPropsService().getBool(ABProp.FAVORITE_STICKER_RMR_SYNC_ENABLED)
                || client.abPropsService().getBool(ABProp.FAVORITE_STICKER_SYNC_AFTER_PAIRING_ENABLED_WEB);
        if (!featureEnabled) {
            return MutationApplicationResult.orphan(stickerHash, "FavoriteSticker");
        }

        if (action.isFavorite()) {
            if (client.store().findFavouriteSticker(stickerHash).isEmpty()) {
                client.store().addFavouriteSticker(stickerHash, action.toSticker());
            }
        } else {
            client.store().removeFavouriteSticker(stickerHash);
        }

        return MutationApplicationResult.success();
    }
}
