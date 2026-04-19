package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.StickerAction;
import com.github.auties00.cobalt.model.sync.action.media.StickerActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.List;

/**
 * Handles favorite sticker actions.
 *
 * <p>This handler processes mutations that add or remove stickers from the
 * favorites collection. The handler routes mutations whose action name is
 * {@code "favoriteSticker"} (the {@code FavoriteSticker} entry in
 * {@code WASyncdConst.Actions}) and whose collection is {@code RegularLow}.
 *
 * <p>Per WhatsApp Web {@code WAWebStickersFavoriteSyncAction.applyMutations},
 * for each mutation:
 * <ol>
 *   <li>Non-{@code SET} operations are acknowledged with {@code UNSUPPORTED}.</li>
 *   <li>The sticker file hash is read from {@code indexParts[1]}; an empty or
 *       missing value yields {@code malformedActionIndex}.</li>
 *   <li>The decoded {@code stickerAction} sub-message must be present and the
 *       {@code isFavorite} flag must be present; otherwise the mutation is
 *       reported as {@code malformedActionValue}.</li>
 *   <li>If the {@code "favorite_sticker"} primary feature flag is not enabled,
 *       the mutation is reported as an {@code Orphan} with model id equal to
 *       the sticker hash and model type {@code "FavoriteSticker"}.</li>
 *   <li>When {@code isFavorite} is {@code true}, the sticker is added to the
 *       favorite-stickers collection if it is not already present (mirroring
 *       {@code FavoriteStickerCollection.addOrUpdateStickers}, which filters
 *       out stickers whose id is already in the collection).</li>
 *   <li>When {@code isFavorite} is {@code false}, the sticker is removed from
 *       the favorite-stickers collection if it is present (mirroring
 *       {@code FavoriteStickerCollection.removeAndSave}); a removal targeting
 *       a sticker that is not in the collection is a no-op success.</li>
 * </ol>
 *
 * <p>Index format: {@code ["favoriteSticker", stickerFileHash]}.
 *
 * @implNote WAWebStickersFavoriteSyncAction — extends {@code AccountSyncdActionBase}
 *           with {@code collectionName = WASyncdConst.CollectionName.RegularLow},
 *           {@code getAction()} returning {@code WASyncdConst.Actions.FavoriteSticker}
 *           ({@code "favoriteSticker"}), and {@code getVersion()} returning {@code 7}
 */
@WhatsAppWebModule(moduleName = "WAWebStickersFavoriteSyncAction")
public final class FavoriteStickerHandler implements WebAppStateActionHandler {
    /**
     * The {@code "favorite_sticker"} primary feature flag name.
     *
     * <p>Per WhatsApp Web {@code WAWebMiscGatingUtils.isFavoriteStickersEnabled}:
     * {@code function d() { return WAWebPrimaryFeatures.primaryFeatureEnabled("favorite_sticker"); }}.
     * The handler must consult the primary device's reported feature set rather
     * than any AB prop, since favorite-sticker sync is gated on the primary's
     * support for the feature, not on a per-companion experiment.
     *
     * @implNote WAWebMiscGatingUtils.isFavoriteStickersEnabled — primary feature key
     */
    private static final String FAVORITE_STICKER_FEATURE = "favorite_sticker"; // WAWebMiscGatingUtils.isFavoriteStickersEnabled: primaryFeatureEnabled("favorite_sticker")

    /**
     * The singleton instance of {@code FavoriteStickerHandler}.
     *
     * @implNote WAWebStickersFavoriteSyncAction.default — WA Web exports a single
     *           pre-instantiated handler ({@code p = new m; l.default = p})
     */
    @WhatsAppWebExport(moduleName = "WAWebStickersFavoriteSyncAction", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final FavoriteStickerHandler INSTANCE = new FavoriteStickerHandler();

    /**
     * Constructs the singleton instance.
     *
     * @implNote WAWebStickersFavoriteSyncAction — WA Web instantiates the handler
     *           once via {@code new m()} and exports it as the module default
     */
    @WhatsAppWebExport(moduleName = "WAWebStickersFavoriteSyncAction", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private FavoriteStickerHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebStickersFavoriteSyncAction.getAction — returns
     *           {@code WASyncdConst.Actions.FavoriteSticker} which equals
     *           {@code "favoriteSticker"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersFavoriteSyncAction", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return StickerAction.ACTION_NAME; // WAWebStickersFavoriteSyncAction.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebStickersFavoriteSyncAction — sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     *           in the constructor
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersFavoriteSyncAction", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return StickerAction.COLLECTION_NAME; // WAWebStickersFavoriteSyncAction constructor: this.collectionName = RegularLow
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebStickersFavoriteSyncAction.getVersion — returns the literal {@code 7}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersFavoriteSyncAction", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return StickerAction.ACTION_VERSION; // WAWebStickersFavoriteSyncAction.getVersion: return 7
    }

    /**
     * {@inheritDoc}
     *
     * <p>Single-mutation adapter that mirrors the WhatsApp Web batch logic for
     * a list of size one and reduces the per-mutation outcome to a boolean: a
     * {@code SUCCESS} result is mapped to {@code true}, all other states are
     * mapped to {@code false}.
     *
     * @implNote ADAPTED: WAWebStickersFavoriteSyncAction.applyMutations — WA Web only
     *           defines a batch entry point; this single-mutation path delegates to
     *           {@link #applyMutationResult} and reduces the outcome to a boolean
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersFavoriteSyncAction", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: single-path adapter for batch-only WA Web entry
    }

    /**
     * {@inheritDoc}
     *
     * <p>Per WhatsApp Web {@code WAWebStickersFavoriteSyncAction.applyMutations}
     * (per-mutation closure):
     * <ol>
     *   <li>Non-{@code SET} operations short-circuit with {@code UNSUPPORTED}.</li>
     *   <li>The sticker hash is read from {@code indexParts[1]} ({@code u = t[1]}).
     *       An empty or missing value triggers {@code this.malformedActionIndex()}.</li>
     *   <li>The {@code stickerAction} sub-message ({@code c}) must be non-null,
     *       otherwise {@code WAWebSyncdIndexUtils.malformedActionValue} is returned.</li>
     *   <li>The {@code isFavorite} field ({@code g}) is destructured from the
     *       sub-message and must be non-null; otherwise the same
     *       {@code malformedActionValue} branch is taken.</li>
     *   <li>If {@code WAWebMiscGatingUtils.isFavoriteStickersEnabled()} returns
     *       {@code false}, the mutation is reported as
     *       {@code {Orphan, modelId: u, modelType: FavoriteSticker}}.</li>
     *   <li>If {@code isFavorite} is {@code true}, an existing entry for the same
     *       hash short-circuits with {@code Success}; otherwise a new
     *       {@code StickerModel} is constructed with the hash, direct path,
     *       fileEncSha256 (base64-decoded), mediaKey (base64-decoded if present),
     *       mediaKeyTimestamp set to the mutation timestamp, width, height and
     *       mimetype, then handed to
     *       {@code FavoriteStickerCollection.addOrUpdateStickers}.</li>
     *   <li>If {@code isFavorite} is {@code false}, the entry is looked up by
     *       hash; an absent entry yields a no-op {@code Success}, otherwise
     *       {@code FavoriteStickerCollection.removeAndSave(u)} is invoked.</li>
     *   <li>Any thrown error is captured and reported as {@code Failed}.</li>
     * </ol>
     *
     * <p>Cobalt's {@code action.isFavorite()} accessor coalesces a {@code null}
     * protobuf {@code isFavorite} field to {@code false} per the project rule
     * "use existing boolean accessors for nullable Boolean fields". This is an
     * intentional architectural difference from WA Web, which would treat the
     * same case as {@code malformedActionValue}.
     *
     * @implNote WAWebStickersFavoriteSyncAction.applyMutations — per-mutation closure
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersFavoriteSyncAction", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebStickersFavoriteSyncAction.applyMutations: if (e.operation !== "set") return a++, {actionState: Unsupported}
            return MutationApplicationResult.unsupported(); // WAWebStickersFavoriteSyncAction.applyMutations: a++, {actionState: SyncActionState.Unsupported}
        }

        try { // WAWebStickersFavoriteSyncAction.applyMutations: try { ... } catch (e) { return {actionState: Failed} }
            var indexArray = JSON.parseArray(mutation.index()); // WAWebStickersFavoriteSyncAction.applyMutations: var t = e.indexParts
            var stickerHash = indexArray.getString(1); // WAWebStickersFavoriteSyncAction.applyMutations: var u = t[1]
            if (stickerHash == null || stickerHash.isEmpty()) { // WAWebStickersFavoriteSyncAction.applyMutations: if (!u)
                return malformedActionIndex(); // WAWebStickersFavoriteSyncAction.applyMutations: return r.malformedActionIndex()
            }

            if (!(mutation.value().action().orElse(null) instanceof StickerAction action)) { // WAWebStickersFavoriteSyncAction.applyMutations: var c = s.stickerAction; if (c == null)
                return malformedActionValue(); // WAWebStickersFavoriteSyncAction.applyMutations: i++, WAWebSyncdIndexUtils.malformedActionValue(r.collectionName)
            }
            // WAWebStickersFavoriteSyncAction.applyMutations: destructured fields from c — deviceIdHint, directPath, fileEncSha256, height, isFavorite, mediaKey, mimetype, width
            // ADAPTED: WAWebStickersFavoriteSyncAction.applyMutations — WA Web checks if (g == null) on the protobuf isFavorite flag and returns malformedActionValue.
            // Cobalt's StickerAction.isFavorite() accessor coalesces a null protobuf field to false per the project's "no Optional<Boolean>" rule, so the malformed-on-null check is intentionally not replicated here.
            if (!client.store().primaryFeatures().contains(FAVORITE_STICKER_FEATURE)) { // WAWebStickersFavoriteSyncAction.applyMutations: if (!WAWebMiscGatingUtils.isFavoriteStickersEnabled())
                return MutationApplicationResult.orphan(stickerHash, "FavoriteSticker"); // WAWebStickersFavoriteSyncAction.applyMutations: l++, {actionState: Orphan, orphanModel: {modelId: u, modelType: SyncModelType.FavoriteSticker}}
            }

            if (action.isFavorite()) { // WAWebStickersFavoriteSyncAction.applyMutations: if (g)
                if (client.store().findFavouriteSticker(stickerHash).isPresent()) { // WAWebStickersFavoriteSyncAction.applyMutations: if (FavoriteStickerCollection.get(u))
                    return MutationApplicationResult.success(); // WAWebStickersFavoriteSyncAction.applyMutations: m++, {actionState: Success}
                }
                // WAWebStickersFavoriteSyncAction.applyMutations: var b = new StickerModel({id: u, directPath: p, filehash: u, encFilehash: _ ? encodeB64(_) : "", mediaKey: h != null ? encodeB64(h) : "", mediaKeyTimestamp: n, width: C, height: f, mimetype: y})
                var sticker = action.toSticker(); // WAWebStickersFavoriteSyncAction.applyMutations: new StickerModel({...})
                sticker.setTimestamp(mutation.timestamp().getEpochSecond()); // WAWebStickersFavoriteSyncAction.applyMutations: mediaKeyTimestamp: n (and FavoriteStickerModel.timestamp: t)
                client.store().addFavouriteSticker(stickerHash, sticker); // WAWebStickersFavoriteSyncAction.applyMutations: yield FavoriteStickerCollection.addOrUpdateStickers([b], n, d)
            } else { // WAWebStickersFavoriteSyncAction.applyMutations: else (g is false)
                // ADAPTED: WAWebStickersFavoriteSyncAction.applyMutations — WA Web reads the entry first (var v = FavoriteStickerCollection.get(u)) and short-circuits with Success when absent, otherwise calls removeAndSave(u). Cobalt's removeFavouriteSticker is idempotent (returns Optional), so the explicit pre-check would be redundant; the observable outcome is identical.
                client.store().removeFavouriteSticker(stickerHash); // WAWebStickersFavoriteSyncAction.applyMutations: FavoriteStickerCollection.removeAndSave(u)
            }

            return MutationApplicationResult.success(); // WAWebStickersFavoriteSyncAction.applyMutations: return {actionState: Success}
        } catch (Exception e) { // WAWebStickersFavoriteSyncAction.applyMutations: catch (e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebStickersFavoriteSyncAction.applyMutations: {actionState: SyncActionState.Failed}
        }
    }

    /**
     * Builds a pending outgoing mutation for favouriting or unfavouriting a
     * sticker identified by its file hash.
     *
     * <p>Per WhatsApp Web {@code WAWebFavoriteStickerSyncActionUtils.getFavoriteStickerMutation}:
     * constructs the {@code stickerAction} sub-message with at minimum the
     * {@code isFavorite} flag and dispatches it at
     * {@code ["favoriteSticker", stickerFileHash]} in the REGULAR_LOW
     * collection with {@code version = 7}.
     *
     * <p>Outgoing favourite mutations only need to carry the
     * {@code isFavorite} flag; the full media descriptor is propagated from
     * the original sticker record on the primary device when the mutation
     * round-trips through app-state sync, so this helper intentionally leaves
     * the media fields unset.
     *
     * @implNote WAWebStickersFavoriteSyncAction — outgoing SET mutation shape
     *           mirrors the inbound payload that
     *           {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     *           consumes
     * @param stickerHash the sticker file hash used as the mutation index
     * @param favorite    {@code true} to mark the sticker as favourite,
     *                    {@code false} to unfavourite it
     * @return the pending mutation ready to be pushed via
     *         {@link com.github.auties00.cobalt.sync.WebAppStateService#pushPatches}
     */
    @WhatsAppWebExport(moduleName = "WAWebStickersFavoriteSyncAction", exports = "generateFavoriteSyncMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getFavoriteStickerMutation(String stickerHash, boolean favorite) {
        var action = new StickerActionBuilder() // WAWebStickersFavoriteSyncAction: {stickerAction: {isFavorite: a}}
                .isFavorite(favorite) // WAWebStickersFavoriteSyncAction.applyMutations: isFavorite field
                .build();
        var timestamp = Instant.now(); // WAWebSyncdActionUtils.buildPendingMutation: timestamp: unixTime()
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, ...)
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: i
                .stickerAction(action) // WAWebStickersFavoriteSyncAction: {stickerAction: ...}
                .build();
        var index = JSON.toJSONString(List.of(StickerAction.ACTION_NAME, stickerHash)); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs))
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET, // WAWebStickersFavoriteSyncAction: operation: SyncdMutation$SyncdOperation.SET
                timestamp,
                StickerAction.ACTION_VERSION // WAWebStickersFavoriteSyncAction.getVersion: 7
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
