package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.media.FavoritesAction;
import com.github.auties00.cobalt.model.sync.action.media.FavoritesActionBuilder;
import com.github.auties00.cobalt.model.sync.action.media.FavoritesActionFavoriteBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Handles favorites sync actions.
 *
 * <p>Per WhatsApp Web, the favorites handler extends {@code AccountSyncdActionBase}
 * and manages the user's favorite chats collection. It uses collection
 * {@code RegularHigh}, action name {@code "favorites"}, and version {@code 1}.
 *
 * <p>The handler applies only the mutation with the latest timestamp from a batch,
 * replacing the entire favorites collection rather than processing each mutation
 * sequentially.
 *
 * @implNote WAWebFavoritesSync.default
 */
public final class FavoritesHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of the favorites handler.
     *
     * <p>Per WhatsApp Web, a single instance {@code m = new d()} is exported
     * as the module's default export.
     *
     * @implNote WAWebFavoritesSync: {@code var m = new d(); l.default = m}
     */
    public static final FavoritesHandler INSTANCE = new FavoritesHandler();

    /**
     * Logger for diagnostic messages.
     *
     * @implNote ADAPTED: WAWebFavoritesSync uses WALogger; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(FavoritesHandler.class.getName()); // ADAPTED: WAWebFavoritesSync — WALogger

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebFavoritesSync: handler is instantiated once as module-level singleton
     */
    private FavoritesHandler() {

    }

    /**
     * Returns the action name for favorites sync.
     *
     * @implNote WAWebFavoritesSync.getAction — returns {@code WASyncdConst.Actions.Favorites}
     *           which is {@code "favorites"}
     * @return the action name {@code "favorites"}
     */
    @Override
    public String actionName() {
        return FavoritesAction.ACTION_NAME; // WAWebFavoritesSync.getAction: return o("WASyncdConst").Actions.Favorites
    }

    /**
     * Returns the sync collection for favorites.
     *
     * @implNote WAWebFavoritesSync constructor — sets {@code this.collectionName = o("WASyncdConst").CollectionName.RegularHigh}
     * @return {@link SyncPatchType#REGULAR_HIGH}
     */
    @Override
    public SyncPatchType collectionName() {
        return FavoritesAction.COLLECTION_NAME; // WAWebFavoritesSync: this.collectionName = o("WASyncdConst").CollectionName.RegularHigh
    }

    /**
     * Returns the mutation format version for favorites.
     *
     * @implNote WAWebFavoritesSync.getVersion — returns {@code 1}
     * @return the version number {@code 1}
     */
    @Override
    public int version() {
        return FavoritesAction.ACTION_VERSION; // WAWebFavoritesSync.getVersion: return 1
    }

    /**
     * Applies a single favorites mutation.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebFavoritesSync.applyMutations — WA Web only has the batch method;
     *           this single-mutation method is a Cobalt convenience wrapper
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebFavoritesSync.applyMutations — single-mutation convenience
    }

    /**
     * Applies a batch of favorites mutations, keeping only the latest by timestamp.
     *
     * <p>Per WhatsApp Web {@code WAWebFavoritesSync.applyMutations}: iterates all mutations,
     * returning {@code malformedActionValue} for non-SET operations and mutations
     * whose {@code favoritesAction.favorites} is {@code null}. Tracks the mutation
     * with the latest timestamp and applies only that one to the store. All valid
     * mutations receive a {@code Success} result.
     *
     * <p>After identifying the latest mutation, resolves each favorite's JID via
     * the chat store, with LID-to-phone fallback, and replaces the entire
     * favorites collection in the store.
     *
     * @implNote WAWebFavoritesSync.applyMutations
     * @param client    the WhatsApp client instance
     * @param mutations the batch of mutations to apply
     * @return a list of results parallel to the input
     */
    @Override
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        DecryptedMutation.Trusted latest = null; // WAWebFavoritesSync.applyMutations: var i
        var unsupportedCount = 0; // WAWebFavoritesSync.applyMutations: var l = 0
        var malformedCount = 0; // WAWebFavoritesSync.applyMutations: var d = 0
        var results = new ArrayList<MutationApplicationResult>(mutations.size()); // WAWebFavoritesSync.applyMutations: var m = t.map(function(e) {...})
        for (var mutation : mutations) { // WAWebFavoritesSync.applyMutations: t.map(function(e) {...})
            if (mutation.operation() != SyncdOperation.SET) { // WAWebFavoritesSync.applyMutations: e.operation !== "set"
                unsupportedCount++; // WAWebFavoritesSync.applyMutations: l++
                results.add(malformedActionValue()); // WAWebFavoritesSync.applyMutations: return o("WAWebSyncdIndexUtils").malformedActionValue(r.collectionName)
                continue;
            }

            if (!(mutation.value().action().orElse(null) instanceof FavoritesAction)) { // WAWebFavoritesSync.applyMutations: ((t=e.value.favoritesAction)==null?void 0:t.favorites)==null
                malformedCount++; // WAWebFavoritesSync.applyMutations: d++
                results.add(malformedActionValue()); // WAWebFavoritesSync.applyMutations: return o("WAWebSyncdIndexUtils").malformedActionValue(r.collectionName)
                continue;
            }

            if (latest == null || mutation.timestamp().compareTo(latest.timestamp()) > 0) { // WAWebFavoritesSync.applyMutations: (i == null || e.timestamp > i.timestamp) && (i = e)
                latest = mutation;
            }
            results.add(MutationApplicationResult.success()); // WAWebFavoritesSync.applyMutations: return {actionState: o("WASyncdConst").SyncActionState.Success}
        }

        if (unsupportedCount > 0) { // WAWebFavoritesSync.applyMutations: l > 0 && o("WALogger").WARN(...)
            LOGGER.warning("favorites sync: " + unsupportedCount + " operations not supported"); // WAWebFavoritesSync.applyMutations: WALogger.WARN("favorites sync: " + l + " operations not supported")
        }
        if (malformedCount > 0) { // WAWebFavoritesSync.applyMutations: d > 0 && o("WALogger").WARN(...)
            LOGGER.warning("favorites sync: " + malformedCount + " malformed mutations"); // WAWebFavoritesSync.applyMutations: WALogger.WARN("favorites sync: " + d + " malformed mutations")
        }

        if (latest != null) { // WAWebFavoritesSync.applyMutations: if (p != null)
            applyLatestMutation(client, latest); // WAWebFavoritesSync.applyMutations: core application logic for the latest mutation
        }

        return results; // WAWebFavoritesSync.applyMutations: return m
    }

    /**
     * Applies a single favorites mutation and returns a detailed result.
     *
     * <p>Validates the operation type and action type, then resolves and
     * stores the favorites. This method is the single-mutation entry point
     * that performs the same validation as the batch path but always applies
     * the provided mutation.
     *
     * @implNote ADAPTED: WAWebFavoritesSync.applyMutations — WA Web only has the batch path;
     *           this method extracts the single-mutation validation and application logic
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebFavoritesSync.applyMutations: e.operation !== "set"
            return malformedActionValue(); // WAWebFavoritesSync.applyMutations: return o("WAWebSyncdIndexUtils").malformedActionValue(r.collectionName)
        }

        if (!(mutation.value().action().orElse(null) instanceof FavoritesAction)) { // WAWebFavoritesSync.applyMutations: ((t=e.value.favoritesAction)==null?void 0:t.favorites)==null
            return malformedActionValue(); // WAWebFavoritesSync.applyMutations: return o("WAWebSyncdIndexUtils").malformedActionValue(r.collectionName)
        }

        applyLatestMutation(client, mutation); // WAWebFavoritesSync.applyMutations: core application logic
        return MutationApplicationResult.success(); // WAWebFavoritesSync.applyMutations: {actionState: SyncActionState.Success}
    }

    /**
     * Applies the favorites from the latest (or only) mutation to the store.
     *
     * <p>Per WhatsApp Web {@code WAWebFavoritesSync.applyMutations}: after identifying
     * the latest mutation, extracts the favorites list, filters out entries with
     * {@code null} IDs, resolves each via the chat store (with LID-to-phone
     * fallback), and replaces the entire favorites collection.
     *
     * <p>Resolution order per WA Web:
     * <ol>
     *   <li>{@code resolveChatForMutationIndex(createWid(id))} — chat table lookup</li>
     *   <li>If failed and {@code isLidMigrated() && wid.isLid()}: {@code getPhoneNumber(wid)}</li>
     *   <li>Otherwise: use raw ID as-is</li>
     * </ol>
     *
     * @implNote WAWebFavoritesSync.applyMutations (inner application block after latest selection)
     * @param client   the WhatsApp client instance
     * @param mutation the mutation containing the favorites to apply
     */
    private void applyLatestMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var action = (FavoritesAction) mutation.value().action().orElseThrow(); // WAWebFavoritesSync.applyMutations: var p = i.value.favoritesAction.favorites
        var favorites = new ArrayList<Jid>(); // WAWebFavoritesSync.applyMutations: var f = p.reduce(...)
        for (var favorite : action.favorites()) { // WAWebFavoritesSync.applyMutations: p.reduce(function(e, t) {...})
            var rawId = favorite.id().orElse(null); // WAWebFavoritesSync.applyMutations: var n = t.id
            if (rawId == null) { // WAWebFavoritesSync.applyMutations: if (n != null)
                continue;
            }

            var rawJid = Jid.of(rawId); // WAWebFavoritesSync.applyMutations: o("WAWebWidFactory").createWid(e.id)
            var resolved = client.store().findChatByJid(rawJid) // WAWebFavoritesSync.applyMutations: yield o("WAWebSyncdGetChat").resolveChatForMutationIndex(...)
                    .map(entry -> entry.jid()) // WAWebFavoritesSync.applyMutations: if (t.success === true) return {id: t.chat.id}
                    .or(() -> rawJid.hasLidServer() ? client.store().findPhoneByLid(rawJid) : Optional.<Jid>empty()) // WAWebFavoritesSync.applyMutations: if (isLidMigrated() && n.isLid()) { var r = getPhoneNumber(n); if (r != null) return {id: r.toString()} }
                    .orElse(rawJid); // WAWebFavoritesSync.applyMutations: return {id: e.id}
            favorites.add(resolved); // WAWebFavoritesSync.applyMutations: f.push(...)
        }

        client.store().setFavoriteChats(favorites); // WAWebFavoritesSync.applyMutations: yield o("WAWebDBFavoriteDatabaseApi").setFavorites(h)
        // WAWebFavoritesSync.applyMutations: o("WAWebBackendApi").frontendFireAndForget("setFavoriteCollection", ...) — ADAPTED: Cobalt does not have frontend event dispatching
        // WAWebFavoritesSync.applyMutations: o("WALogger").LOG("[favorites] set ok, resolved " + g + " of " + h.length) — logging skipped
    }

    /**
     * Builds a pending mutation for syncing local favorites changes to the server.
     *
     * <p>Per WhatsApp Web {@code WAWebFavoritesSync.getFavoritesMutation}: takes
     * the current list of favorites with order indices, resolves each to its
     * mutation index JID, sorts by order index, and builds a SET mutation
     * containing the full favorites list.
     *
     * <p>In WA Web, each favorite is resolved via {@code getWidMutationIndexForWid}
     * which converts user JIDs to their LID mutation index when LID migration is
     * active. In Cobalt, the JID is used directly as the mutation index since the
     * LID mapping is handled by the store layer.
     *
     * @implNote WAWebFavoritesSync.getFavoritesMutation
     * @param favoriteJids the ordered list of favorite chat JIDs
     * @param timestamp    the mutation timestamp
     * @return the pending mutation for the favorites action
     */
    public SyncPendingMutation getFavoritesMutation(List<Jid> favoriteJids, Instant timestamp) {
        var favoriteEntries = favoriteJids.stream() // WAWebFavoritesSync.getFavoritesMutation: a.sort(function(e,t){return e.orderIndex-t.orderIndex}).map(function(e){var t=e.mutationIndex; return {id:t}})
                .map(jid -> new FavoritesActionFavoriteBuilder() // WAWebFavoritesSync.getFavoritesMutation: {id: t}
                        .id(jid.toString()) // WAWebFavoritesSync.getFavoritesMutation: id: e.mutationIndex — ADAPTED: Cobalt uses JID string directly
                        .build())
                .toList();
        var action = new FavoritesActionBuilder() // WAWebFavoritesSync.getFavoritesMutation: {favoritesAction: {favorites: [...]}}
                .favorites(favoriteEntries) // WAWebFavoritesSync.getFavoritesMutation: favorites: a.sort(...).map(...)
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: t
                .favoritesAction(action) // WAWebFavoritesSync.getFavoritesMutation: {favoritesAction: ...}
                .build();
        var index = JSON.toJSONString(List.of(actionName())); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs)) where indexArgs = []
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, ... }
                index, // WAWebSyncdActionUtils.buildPendingMutation: index
                value, // WAWebSyncdActionUtils.buildPendingMutation: binarySyncAction
                SyncdOperation.SET, // WAWebFavoritesSync.getFavoritesMutation: operation: SyncdMutation$SyncdOperation.SET
                timestamp, // WAWebSyncdActionUtils.buildPendingMutation: timestamp
                version() // WAWebSyncdActionUtils.buildPendingMutation: version: this.getVersion()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
