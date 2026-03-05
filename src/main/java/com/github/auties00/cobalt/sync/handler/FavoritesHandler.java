package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.FavoritesAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles favorites actions.
 *
 * <p>Index format: ["favorites", ...]
 */
public final class FavoritesHandler implements WebAppStateActionHandler {
    public static final FavoritesHandler INSTANCE = new FavoritesHandler();

    private FavoritesHandler() {

    }

    @Override
    public String actionName() {
        return FavoritesAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return FavoritesAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return FavoritesAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        if (!(mutation.value().action().orElse(null) instanceof FavoritesAction action)) {
            return false;
        }

        var jids = action.favorites()
                .stream()
                .flatMap(fav -> fav.id().stream())
                .map(Jid::of)
                .toList();
        client.store().setFavoriteChats(jids);
        return true;
    }
}
