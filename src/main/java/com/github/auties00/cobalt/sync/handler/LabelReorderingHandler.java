package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LabelReorderingAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles label reordering actions.
 *
 * <p>This handler processes mutations that reorder chat labels. The reordering
 * is acknowledged but not applied locally, as Cobalt does not currently maintain
 * a label ordering model.
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
        // Web calls WAWebDBLabelsReorder.updateLabelsSortOrder(sortedLabelIds) on SET;
        // REMOVE is unsupported. No label ordering model exists in the Java store,
        // so this is a no-op.
        return true;
    }
}
