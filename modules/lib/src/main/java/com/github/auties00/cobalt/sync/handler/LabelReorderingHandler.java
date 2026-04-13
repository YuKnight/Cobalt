package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.preference.Label;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LabelReorderingAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

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
public final class LabelReorderingHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code LabelReorderingHandler}.
     *
     * @implNote WAWebLabelReorderingSync.default — WA Web exports a single module
     *           instance {@code m = new d()}; Cobalt mirrors this with a singleton
     */
    public static final LabelReorderingHandler INSTANCE = new LabelReorderingHandler();

    /**
     * Constructs the singleton handler.
     *
     * @implNote WAWebLabelReorderingSync.default constructor — sets
     *           {@code collectionName = CollectionName.Regular}; in Cobalt the
     *           collection is returned via {@link #collectionName()} from the
     *           action's static {@code COLLECTION_NAME} constant
     */
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
    public SyncPatchType collectionName() {
        return LabelReorderingAction.COLLECTION_NAME; // WAWebLabelReorderingSync.default constructor: collectionName = Regular
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebLabelReorderingSync.default.getVersion — returns {@code 3}
     */
    @Override
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
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebLabelReorderingSync.default.applyMutations
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
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
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
}
