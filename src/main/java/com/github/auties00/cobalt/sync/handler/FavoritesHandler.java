package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.FavoritesAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles favorites actions.
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
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        DecryptedMutation.Trusted latest = null;
        var results = new ArrayList<MutationApplicationResult>(mutations.size());
        for (var mutation : mutations) {
            if (mutation.operation() != SyncdOperation.SET) {
                results.add(MutationApplicationResult.malformed());
                continue;
            }

            if (!(mutation.value().action().orElse(null) instanceof FavoritesAction)) {
                results.add(MutationApplicationResult.malformed());
                continue;
            }

            if (latest == null || mutation.timestamp().compareTo(latest.timestamp()) > 0) {
                latest = mutation;
            }
            results.add(MutationApplicationResult.success());
        }

        if (latest != null) {
            applyMutationResult(client, latest);
        }

        return results;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.malformed();
        }

        if (!(mutation.value().action().orElse(null) instanceof FavoritesAction action)) {
            return MutationApplicationResult.malformed();
        }

        var favorites = new ArrayList<Jid>();
        for (var favorite : action.favorites()) {
            var rawId = favorite.id().orElse(null);
            if (rawId == null || rawId.isBlank()) {
                continue;
            }

            var rawJid = Jid.of(rawId);
            var resolved = client.store().findChatByJid(rawJid)
                    .map(entry -> entry.jid())
                    .or(() -> client.store().findNewsletterByJid(rawJid).map(entry -> entry.jid()))
                    .or(() -> rawJid.hasLidServer() ? client.store().findPhoneByLid(rawJid) : java.util.Optional.empty())
                    .orElse(rawJid);
            favorites.add(resolved);
        }

        client.store().setFavoriteChats(favorites);
        return MutationApplicationResult.success();
    }
}
