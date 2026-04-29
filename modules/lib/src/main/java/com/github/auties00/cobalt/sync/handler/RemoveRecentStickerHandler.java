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
import com.github.auties00.cobalt.model.sync.action.media.RemoveRecentStickerAction;
import com.github.auties00.cobalt.model.sync.action.media.RemoveRecentStickerActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.List;

/**
 * Handles {@code removeRecentSticker} app-state sync mutations.
 *
 * <p>Per WhatsApp Web {@code WAWebStickersRemoveRecentSyncAction.applyMutations}:
 * <ol>
 *   <li>If the {@code "recent_sticker"} primary feature is not enabled, every
 *       mutation in the batch is reported as {@code Unsupported}.</li>
 *   <li>Non-{@code SET} operations are acknowledged with {@code Unsupported}.</li>
 *   <li>The sticker file hash is read from {@code indexParts[1]}; a missing value
 *       yields {@code malformedActionIndex}.</li>
 *   <li>The {@code lastStickerSentTs} field is read from
 *       {@code value.removeRecentStickerAction} via an optional chain; either a
 *       missing sub-message or a missing field results in {@code null}.</li>
 *   <li>The recent-stickers collection is queried by hash; a missing entry is
 *       reported as {@code Orphan} (with no model id/type).</li>
 *   <li>If {@code lastStickerSentTs} is {@code null}, OR the local sticker's
 *       {@code timestamp} is less than or equal to {@code lastStickerSentTs},
 *       the entry is removed from the recent-stickers collection.</li>
 *   <li>The mutation always returns {@code Success} after reaching the
 *       collection lookup step (whether or not the removal was performed).</li>
 * </ol>
 *
 * <p>Index format: {@code ["removeRecentSticker", stickerFileHash]}.
 *
 * @implNote WAWebStickersRemoveRecentSyncAction — extends
 *           {@code AccountSyncdActionBase} with
 *           {@code collectionName = WASyncdConst.CollectionName.RegularLow},
 *           {@code getAction()} returning
 *           {@code WASyncdConst.Actions.RemoveRecentSticker}
 *           ({@code "removeRecentSticker"}), and {@code getVersion()} returning
 *           the literal {@code 7}
 */
@WhatsAppWebModule(moduleName = "WAWebStickersRemoveRecentSyncAction")
public final class RemoveRecentStickerHandler implements WebAppStateActionHandler {
    /**
     * The {@code "recent_sticker"} primary feature flag name.
     *
     * <p>Per WhatsApp Web {@code WAWebMiscGatingUtils.isRecentStickersMDEnabled}:
     * {@code function c() { return WAWebPrimaryFeatures.primaryFeatureEnabled("recent_sticker"); }}.
     * The handler must consult the primary device's reported feature set rather
     * than any AB prop, since recent-sticker sync is gated on the primary's
     * support for the feature, not on a per-companion experiment.
     *
     * @implNote WAWebMiscGatingUtils.isRecentStickersMDEnabled — primary feature key
     */
    @WhatsAppWebExport(moduleName = "WAWebMiscGatingUtils", exports = "isRecentStickersMDEnabled", adaptation = WhatsAppAdaptation.ADAPTED)
    private static final String RECENT_STICKER_FEATURE = "recent_sticker"; // WAWebMiscGatingUtils.isRecentStickersMDEnabled: primaryFeatureEnabled("recent_sticker")

    /**
     * The singleton instance of {@code RemoveRecentStickerHandler}.
     *
     * @implNote WAWebStickersRemoveRecentSyncAction.default — WA Web exports a
     *           single pre-instantiated handler ({@code d = new c; l.default = d})
     */
    @WhatsAppWebExport(moduleName = "WAWebStickersRemoveRecentSyncAction", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final RemoveRecentStickerHandler INSTANCE = new RemoveRecentStickerHandler();

    /**
     * Constructs the singleton instance.
     *
     * @implNote WAWebStickersRemoveRecentSyncAction — WA Web instantiates the
     *           handler once via {@code new c()} and exports it as the module default
     */
    @WhatsAppWebExport(moduleName = "WAWebStickersRemoveRecentSyncAction", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private RemoveRecentStickerHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebStickersRemoveRecentSyncAction.getAction — returns
     *           {@code WASyncdConst.Actions.RemoveRecentSticker} which equals
     *           {@code "removeRecentSticker"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersRemoveRecentSyncAction", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return RemoveRecentStickerAction.ACTION_NAME; // WAWebStickersRemoveRecentSyncAction.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebStickersRemoveRecentSyncAction — sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     *           in the constructor
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersRemoveRecentSyncAction", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return RemoveRecentStickerAction.COLLECTION_NAME; // WAWebStickersRemoveRecentSyncAction constructor: this.collectionName = RegularLow
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebStickersRemoveRecentSyncAction.getVersion — returns the
     *           literal {@code 7}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersRemoveRecentSyncAction", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return RemoveRecentStickerAction.ACTION_VERSION; // WAWebStickersRemoveRecentSyncAction.getVersion: return 7
    }

    /**
     * {@inheritDoc}
     *
     * <p>Single-mutation adapter that mirrors the WhatsApp Web batch logic for
     * a list of size one and reduces the per-mutation outcome to a boolean: a
     * {@code SUCCESS} result is mapped to {@code true}, all other states are
     * mapped to {@code false}.
     *
     * @implNote ADAPTED: WAWebStickersRemoveRecentSyncAction.applyMutations — WA
     *           Web only defines a batch entry point; this single-mutation path
     *           delegates to {@link #applyMutationResult} and reduces the outcome
     *           to a boolean
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersRemoveRecentSyncAction", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: single-path adapter for batch-only WA Web entry
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implements the body of
     * {@code WAWebStickersRemoveRecentSyncAction.applyMutations} for a single
     * mutation. The WA Web batch counter logging that aggregates the
     * {@code notSupported} and {@code malformed} buckets via {@code WALogger.WARN}
     * is intentionally omitted (WAM/telemetry).
     *
     * @implNote WAWebStickersRemoveRecentSyncAction.applyMutations
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersRemoveRecentSyncAction", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        // WAWebStickersRemoveRecentSyncAction.applyMutations:
        //   if (!WAWebMiscGatingUtils.isRecentStickersMDEnabled())
        //     return WALogger.WARN("syncd: remove recent sticker operation not supported"),
        //            t.map(() => ({actionState: Unsupported}))
        // WAWebMiscGatingUtils.isRecentStickersMDEnabled:
        //   return WAWebPrimaryFeatures.primaryFeatureEnabled("recent_sticker")
        if (!client.store().primaryFeatures().contains(RECENT_STICKER_FEATURE)) { // WAWebMiscGatingUtils.isRecentStickersMDEnabled
            return MutationApplicationResult.unsupported(); // WAWebStickersRemoveRecentSyncAction.applyMutations: t.map(() => ({actionState: Unsupported}))
        }

        // WAWebStickersRemoveRecentSyncAction.applyMutations:
        //   if (e.operation !== "set") return r++, {actionState: Unsupported}
        if (mutation.operation() != SyncdOperation.SET) { // WAWebStickersRemoveRecentSyncAction.applyMutations: e.operation !== "set"
            return MutationApplicationResult.unsupported(); // WAWebStickersRemoveRecentSyncAction.applyMutations: r++, {actionState: Unsupported}
        }

        // WAWebStickersRemoveRecentSyncAction.applyMutations:
        //   var i = e.indexParts, l = i[1]
        //   if (l == null) return a++, n.malformedActionIndex()
        var indexArray = JSON.parseArray(mutation.index()); // WAWebStickersRemoveRecentSyncAction.applyMutations: var i = e.indexParts
        var stickerHash = indexArray.getString(1); // WAWebStickersRemoveRecentSyncAction.applyMutations: var l = i[1]
        if (stickerHash == null) { // WAWebStickersRemoveRecentSyncAction.applyMutations: if (l == null)
            return malformedActionIndex(); // WAWebStickersRemoveRecentSyncAction.applyMutations: a++, n.malformedActionIndex()
        }

        // WAWebStickersRemoveRecentSyncAction.applyMutations:
        //   var s = (t = e.value.removeRecentStickerAction) == null ? void 0 : t.lastStickerSentTs
        // The optional-chain returns undefined when either the sub-message OR the
        // lastStickerSentTs field is missing; both cases collapse into c == null below.
        var action = mutation.value().action().orElse(null) instanceof RemoveRecentStickerAction entry ? entry : null; // WAWebStickersRemoveRecentSyncAction.applyMutations: t = e.value.removeRecentStickerAction
        var lastStickerSentTs = action == null ? null : action.lastStickerSentTs().orElse(null); // WAWebStickersRemoveRecentSyncAction.applyMutations: s = t == null ? void 0 : t.lastStickerSentTs

        // WAWebStickersRemoveRecentSyncAction.applyMutations:
        //   var u = WAWebRecentStickerCollectionMd.RecentStickerCollectionMd.get(l)
        //   if (!u) return {actionState: Orphan}
        var sticker = client.store().findRecentSticker(stickerHash); // WAWebStickersRemoveRecentSyncAction.applyMutations: WAWebRecentStickerCollectionMd.RecentStickerCollectionMd.get(l)
        if (sticker.isEmpty()) { // WAWebStickersRemoveRecentSyncAction.applyMutations: if (!u)
            return MutationApplicationResult.orphan(); // WAWebStickersRemoveRecentSyncAction.applyMutations: {actionState: Orphan}
        }

        // WAWebStickersRemoveRecentSyncAction.applyMutations:
        //   var c = WALongInt.maybeNumberOrThrowIfTooLarge(s)
        //   (c == null || WALongInt.numberOrThrowIfTooLarge(u.timestamp) <= c)
        //     && WAWebRecentStickerCollectionMd.RecentStickerCollectionMd.removeAndSave(u)
        //
        // WALongInt.maybeNumberOrThrowIfTooLarge: returns null when the input is null/undefined,
        // otherwise asserts the value is a safe Number and returns it.
        // WALongInt.numberOrThrowIfTooLarge: asserts the value is a safe Number and returns it.
        // In Cobalt both timestamps are already plain longs, so the safe-integer guard is a no-op.
        var stickerTimestamp = sticker.get().timestamp().orElse(0L); // WAWebStickersRemoveRecentSyncAction.applyMutations: u.timestamp
        if (lastStickerSentTs == null || stickerTimestamp <= toEpochComparable(lastStickerSentTs)) { // WAWebStickersRemoveRecentSyncAction.applyMutations: c == null || u.timestamp <= c
            client.store().removeRecentSticker(stickerHash); // WAWebStickersRemoveRecentSyncAction.applyMutations: WAWebRecentStickerCollectionMd.RecentStickerCollectionMd.removeAndSave(u)
        }

        return MutationApplicationResult.success(); // WAWebStickersRemoveRecentSyncAction.applyMutations: {actionState: Success}
    }

    /**
     * Converts an {@link Instant} representing the WhatsApp Web
     * {@code lastStickerSentTs} field to a comparable {@code long} value in the
     * same units as {@code Sticker.timestamp()}.
     *
     * <p>Per WhatsApp Web {@code WAWebStickersRemoveRecentSyncAction.applyMutations}
     * the {@code lastStickerSentTs} field carries the raw value of the recent
     * sticker's {@code timestamp} (set via {@code WATimeUtils.unixTimeMs}), and
     * the comparison is performed numerically without any unit conversion.
     *
     * <p>Cobalt's {@link RemoveRecentStickerAction#lastStickerSentTs()} accessor
     * exposes the field as an {@link Instant} (decoded via
     * {@code InstantSecondsMixin}) so the millisecond value stored on the wire is
     * reinterpreted as seconds. To preserve the original numeric comparison this
     * helper unwraps the {@code Instant} back to its raw epoch-second value.
     *
     * @apiNote The unit mismatch between {@code Sticker.timestamp()}
     *          (millis-since-epoch in WA Web) and {@code lastStickerSentTs}
     *          (decoded as epoch-seconds in Cobalt) is a bug in the
     *          {@code RemoveRecentStickerAction} protobuf model — the field
     *          should be exposed as a raw {@code OptionalLong} so that no unit
     *          assumption is encoded.
     * @implNote WAWebStickersRemoveRecentSyncAction.applyMutations — adapter for
     *           Cobalt's Instant-typed accessor
     * @param instant the {@link Instant} read from the protobuf, must not be {@code null}
     * @return the epoch-second value of {@code instant}
     */
    @WhatsAppWebExport(moduleName = "WAWebStickersRemoveRecentSyncAction", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    private static long toEpochComparable(Instant instant) {
        return instant.getEpochSecond(); // WAWebStickersRemoveRecentSyncAction.applyMutations: numeric value of lastStickerSentTs
    }

    /**
     * Builds a pending outgoing mutation that removes a sticker from the
     * recent-stickers collection across linked devices.
     *
     * <p>Per WhatsApp Web {@code WAWebStickersRemoveRecentSyncAction}: emits a
     * SET mutation at {@code ["removeRecentSticker", stickerFileHash]} in the
     * REGULAR_LOW collection with {@code version = 7} and a
     * {@code removeRecentStickerAction} sub-message carrying the current
     * timestamp as {@code lastStickerSentTs}. Receiving devices compare this
     * timestamp against their local recent-sticker entry to decide whether to
     * remove it.
     *
     * @implNote WAWebStickersRemoveRecentSyncAction — outgoing SET mutation
     *           shape mirrors the inbound payload that
     *           {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     *           consumes
     * @param stickerHash the sticker file hash used as the mutation index
     * @return the pending mutation ready to be pushed via
     *         {@link com.github.auties00.cobalt.sync.WebAppStateService#pushPatches}
     */
    @WhatsAppWebExport(moduleName = "WAWebStickersRemoveRecentSyncAction", exports = "generateRemoveStickerMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getRemoveRecentStickerMutation(String stickerHash) {
        var timestamp = Instant.now(); // WAWebSyncdActionUtils.buildPendingMutation: timestamp: unixTime()
        var action = new RemoveRecentStickerActionBuilder() // WAWebStickersRemoveRecentSyncAction: {removeRecentStickerAction: {lastStickerSentTs: ...}}
                .lastStickerSentTs(timestamp) // WAWebStickersRemoveRecentSyncAction.applyMutations: lastStickerSentTs field
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: i
                .removeRecentStickerAction(action) // WAWebStickersRemoveRecentSyncAction: {removeRecentStickerAction: ...}
                .build();
        var index = JSON.toJSONString(List.of(RemoveRecentStickerAction.ACTION_NAME, stickerHash)); // WAWebSyncdActionUtils.buildPendingMutation
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET, // WAWebStickersRemoveRecentSyncAction: operation: SyncdOperation.SET
                timestamp,
                RemoveRecentStickerAction.ACTION_VERSION // WAWebStickersRemoveRecentSyncAction.getVersion: 7
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
