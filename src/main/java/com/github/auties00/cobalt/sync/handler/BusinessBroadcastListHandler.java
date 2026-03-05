package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastListAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles business broadcast list actions.
 *
 * <p>This handler processes mutations for business broadcast lists. Per WhatsApp Web
 * (WAWebBroadcastListSync), on SET the web client extracts the audience expression
 * (either label-based from labelIds or explicit from participant lidJids), list name,
 * and id (from index[1]), then upserts into broadcast list storage. On REMOVE, the
 * web client removes the broadcast list from storage. The handler is gated behind
 * {@code isBizBroadcastSendWebEnabled()}.
 *
 * <p>Since the store has no broadcast list storage, this handler is a no-op.
 *
 * <p>Index format: ["business_broadcast_list", listId]
 */
public final class BusinessBroadcastListHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code BusinessBroadcastListHandler}.
     */
    public static final BusinessBroadcastListHandler INSTANCE = new BusinessBroadcastListHandler();

    private BusinessBroadcastListHandler() {

    }

    @Override
    public String actionName() {
        return BusinessBroadcastListAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return BusinessBroadcastListAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return BusinessBroadcastListAction.ACTION_VERSION;
    }

    /**
     * Applies a business broadcast list mutation.
     *
     * <p>Per WhatsApp Web, on SET the web client builds an audience expression
     * (label-based if labelIds are present, explicit from participant lidJids
     * otherwise) and upserts the broadcast list with its name and id into
     * IndexedDB storage. On REMOVE, the broadcast list is deleted from storage.
     *
     * <p>No-op: the store has no broadcast list storage.
     *
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} always
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web manages broadcast lists in WAWebBroadcastListStorageUtils (IndexedDB).
        // No equivalent storage exists in the store.
        return true;
    }
}
