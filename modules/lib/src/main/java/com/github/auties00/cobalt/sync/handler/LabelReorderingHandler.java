package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.preference.Label;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LabelReorderingAction;
import com.github.auties00.cobalt.model.sync.action.contact.LabelReorderingActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.List;

/**
 * Handles the {@code label_reordering} sync action by applying the new label
 * sort order published by the server.
 *
 * <p>This handler processes mutations that reorder chat labels by updating
 * each matching {@link Label}'s {@code orderIndex} to its position in the
 * {@code sortedLabelIds} list. Only {@link SyncdOperation#SET} operations are
 * supported; any other operation is reported back as {@code UNSUPPORTED}.
 *
 * <p>Per {@code WAWebLabelReorderingSync.default.applyMutations}, a mutation
 * is considered malformed when the embedded {@code labelReorderingAction}
 * value is missing or its {@code sortedLabelIds} array is null/empty. In that
 * case the handler returns a malformed result tagged with the collection
 * name.
 *
 * <p>Index format: {@code ["label_reordering"]}
 *
 * @implNote WAWebLabelReorderingSync.default — class extends
 *           {@code AccountSyncdActionBase} with {@code collectionName = Regular},
 *           {@code getVersion() = 3}, {@code getAction() = LabelReordering}, and
 *           {@code applyMutations()} as the per-mutation apply logic
 */
@WhatsAppWebModule(moduleName = "WAWebLabelReorderingSync")
public final class LabelReorderingHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code LabelReorderingHandler}.
     *
     * @implNote WAWebLabelReorderingSync.default — WA Web exports a single module
     *           instance {@code m = new d()}; Cobalt mirrors this with a singleton
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final LabelReorderingHandler INSTANCE = new LabelReorderingHandler();

    /**
     * Constructs the singleton handler.
     *
     * @implNote WAWebLabelReorderingSync.default constructor — sets
     *           {@code collectionName = CollectionName.Regular}; in Cobalt the
     *           collection is returned via {@link #collectionName()} from the
     *           action's static {@code COLLECTION_NAME} constant
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private LabelReorderingHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebLabelReorderingSync.default.getAction — returns
     *           {@code WASyncdConst.Actions.LabelReordering} which maps to the
     *           {@code "label_reordering"} action constant
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return LabelReorderingAction.ACTION_NAME; // WAWebLabelReorderingSync.default.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebLabelReorderingSync.default.collectionName — set in
     *           constructor to {@code CollectionName.Regular}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return LabelReorderingAction.COLLECTION_NAME; // WAWebLabelReorderingSync.default constructor: collectionName = Regular
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebLabelReorderingSync.default.getVersion — returns {@code 3}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return LabelReorderingAction.ACTION_VERSION; // WAWebLabelReorderingSync.default.getVersion -> 3
    }

    /**
     * {@inheritDoc}
     *
     * @implNote ADAPTED: WAWebLabelReorderingSync.default.applyMutations — WA Web
     *           returns {@code {actionState}} objects; Cobalt wraps them in
     *           {@link MutationApplicationResult} and maps success to
     *           {@code true}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebLabelReorderingSync.default.applyMutations
    }

    /**
     * {@inheritDoc}
     *
     * <p>Applies the reordering by updating each matching label's
     * {@code orderIndex} to its zero-based position in
     * {@link LabelReorderingAction#sortedLabelIds()}. Labels referenced by the
     * action but not present in the store are silently skipped, mirroring WA
     * Web's {@code bulkGet} behavior in
     * {@code WAWebDBLabelsReorder.updateLabelsSortOrder}. Labels present in the
     * store but not referenced by the action retain their existing
     * {@code orderIndex}.
     *
     * @implNote WAWebLabelReorderingSync.default.applyMutations and
     *           WAWebDBLabelsReorder.updateLabelsSortOrder — validates the
     *           mutation envelope, then applies the sort-order update using the
     *           in-memory store instead of WA Web's IndexedDB {@code label}
     *           table. WAM telemetry
     *           ({@code generateLabelReorderHash}/{@code logLabelSyncEvent}) and
     *           the {@code frontendFireAndForget("reorderLabels", ...)} renderer
     *           notification are intentionally omitted in Cobalt.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebLabelReorderingSync.default.applyMutations: if (n.operation === "set")
            return MutationApplicationResult.unsupported(); // WAWebLabelReorderingSync.default.applyMutations: WARN("operation not supported"); return { actionState: Unsupported }
        }

        if (!(mutation.value().action().orElse(null) instanceof LabelReorderingAction action)) { // WAWebLabelReorderingSync.default.applyMutations: a = r?.labelReorderingAction; a == null
            return malformedActionValue(); // WAWebLabelReorderingSync.default.applyMutations: WAWebSyncdIndexUtils.malformedActionValue(t.collectionName)
        }

        var sortedLabelIds = action.sortedLabelIds(); // WAWebLabelReorderingSync.default.applyMutations: a.sortedLabelIds
        if (sortedLabelIds.isEmpty()) { // WAWebLabelReorderingSync.default.applyMutations: a.sortedLabelIds == null || !Array.isArray(a.sortedLabelIds) || a.sortedLabelIds.length === 0
            return malformedActionValue(); // WAWebLabelReorderingSync.default.applyMutations: WAWebSyncdIndexUtils.malformedActionValue(t.collectionName)
        }

        // ADAPTED: WAWebDBLabelsReorder.updateLabelsSortOrder — WA Web builds a
        // Map<labelId, position>, stringifies ids, bulkGets them from IndexedDB,
        // then merges { orderIndex: position } into each found row. Cobalt uses
        // findLabel() against the in-memory store which is equivalent to
        // bulkGet + non-null filter.
        for (var position = 0; position < sortedLabelIds.size(); position++) { // WAWebDBLabelsReorder.updateLabelsSortOrder: t.reduce((e, t, n) => e.set(t, n), new Map())
            var labelId = sortedLabelIds.get(position); // WAWebDBLabelsReorder.updateLabelsSortOrder: t[n] (Integer id from the action)
            var labelIdString = String.valueOf(labelId); // WAWebDBLabelsReorder.updateLabelsSortOrder: t.map(e => String(e))
            var label = client.store().findLabel(labelIdString).orElse(null); // WAWebDBLabelsReorder.updateLabelsSortOrder: a.bulkGet(l); u.forEach(e => { if (e != null) ... })
            if (label != null) { // WAWebDBLabelsReorder.updateLabelsSortOrder: if (e != null)
                label.setOrderIndex(position); // WAWebDBLabelsReorder.updateLabelsSortOrder: a.merge(e.id, { orderIndex: t })
            }
        }

        return MutationApplicationResult.success(); // WAWebLabelReorderingSync.default.applyMutations: return { actionState: Success }
    }

    /**
     * Builds a pending SET mutation for reordering the user's chat labels.
     *
     * <p>The mutation carries the full ordered list of integer label
     * identifiers (matching the on-wire {@code INT32} type of
     * {@link LabelReorderingAction#sortedLabelIds()}). Per WhatsApp Web the
     * reorder action uses an empty index (Cobalt still prefixes the action
     * name as the canonical first element to stay consistent with every
     * other handler's index layout).
     *
     * @implNote ADAPTED: WA Web ships no public "getMutation" for
     *           {@code WAWebLabelReorderingSync}; Cobalt materialises the
     *           mutation here so that {@code reorderLabels} can be exposed on
     *           {@link WhatsAppClient}. The shape follows the sibling
     *           {@code WAWebLabelSync.default.getLabelMutation} pattern:
     *           build a {@link LabelReorderingAction}, wrap it in a
     *           {@link SyncActionValueBuilder}, emit index {@code [actionName]}
     *           and wrap the raw mutation in a {@link SyncPendingMutation}.
     * @param sortedLabelIds the full ordered list of integer label identifiers
     * @param timestamp      the mutation timestamp
     * @return the pending mutation for the reorder operation
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getReorderLabelsMutation(
            List<Integer> sortedLabelIds,
            Instant timestamp
    ) {
        var action = new LabelReorderingActionBuilder() // ADAPTED: WAWebLabelReorderingSync has no public getter; Cobalt mirrors the sibling WAWebLabelSync.default.getLabelMutation shape
                .sortedLabelIds(sortedLabelIds) // WAWebLabelReorderingSync.default.applyMutations: a.sortedLabelIds
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp
                .labelReorderingAction(action) // WAWebLabelReorderingSync.default.applyMutations: value.labelReorderingAction
                .build();
        var index = JSON.toJSONString(List.of(actionName())); // WAWebSyncdActionUtils.buildIndex: JSON.stringify([action]); empty indexArgs
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0);
    }
}
