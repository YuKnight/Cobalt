package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LabelReorderingAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles label reordering actions.
 *
 * <p>This handler processes mutations that reorder chat labels by updating
 * label sort order. Only SET operations are supported; other operations are
 * acknowledged as unsupported.
 *
 * <p>Index format: ["label_reordering"]
 */
public final class LabelReorderingHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code LabelReorderingHandler}.
     */
    public static final LabelReorderingHandler INSTANCE = new LabelReorderingHandler();

    private LabelReorderingHandler() {

    }

    @Override
    public String actionName() {
        return LabelReorderingAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return LabelReorderingAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return LabelReorderingAction.ACTION_VERSION;
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

        if (!(mutation.value().action().orElse(null) instanceof LabelReorderingAction action)) {
            return MutationApplicationResult.malformed();
        }

        if (action.sortedLabelIds().isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        var labelsById = client.store().labels()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.github.auties00.cobalt.model.preference.Label::id,
                        java.util.function.Function.identity()
                ));
        for (int i = 0; i < action.sortedLabelIds().size(); i++) {
            var labelId = action.sortedLabelIds().get(i);
            var label = labelsById.get(labelId);
            if (label != null) {
                label.setOrderIndex(i);
            }
        }

        return MutationApplicationResult.success();
    }
}
