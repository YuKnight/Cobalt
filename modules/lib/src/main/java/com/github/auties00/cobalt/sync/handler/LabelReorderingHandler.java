package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.preference.Label;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LabelReorderingAction;
import com.github.auties00.cobalt.model.sync.action.contact.LabelReorderingActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
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
 */
@WhatsAppWebModule(moduleName = "WAWebLabelReorderingSync")
public final class LabelReorderingHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code LabelReorderingHandler}.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final LabelReorderingHandler INSTANCE = new LabelReorderingHandler();

    /**
     * Constructs the singleton handler.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private LabelReorderingHandler() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return LabelReorderingAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return LabelReorderingAction.COLLECTION_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return LabelReorderingAction.ACTION_VERSION;
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
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLabelReorderingSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof LabelReorderingAction action)) {
            return SyncdIndexUtils.malformedActionValue(collectionName().name());
        }

        var sortedLabelIds = action.sortedLabelIds();
        if (sortedLabelIds.isEmpty()) {
            return SyncdIndexUtils.malformedActionValue(collectionName().name());
        }

        // ADAPTED: WAWebDBLabelsReorder.updateLabelsSortOrder — WA Web builds a
        // Map<labelId, position>, stringifies ids, bulkGets them from IndexedDB,
        // then merges { orderIndex: position } into each found row. Cobalt uses
        // findLabel() against the in-memory store which is equivalent to
        // bulkGet + non-null filter.
        for (var position = 0; position < sortedLabelIds.size(); position++) {
            var labelId = sortedLabelIds.get(position);
            var labelIdString = String.valueOf(labelId);
            var label = client.store().findLabel(labelIdString).orElse(null);
            if (label != null) {
                label.setOrderIndex(position);
            }
        }

        return MutationApplicationResult.success();
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
                .sortedLabelIds(sortedLabelIds)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .labelReorderingAction(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName()));
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
