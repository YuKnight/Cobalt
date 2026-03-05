package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastCampaignAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles business broadcast campaign actions.
 *
 * <p>This handler processes mutations for business broadcast campaigns. Per WhatsApp
 * Web (WAWebBroadcastCampaignSync), on SET the web client validates that broadcastJid,
 * deviceId, and status are present in the action, then upserts the campaign (by id
 * from index[1]) with the action data and timestamp into campaign storage. On REMOVE,
 * the web client removes the campaign from storage. The handler is gated behind
 * {@code isBizBroadcastSendWebEnabled()}.
 *
 * <p>Since the store has no broadcast campaign storage, this handler is a no-op.
 *
 * <p>Index format: ["business_broadcast_campaign", campaignId]
 */
public final class BusinessBroadcastCampaignHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code BusinessBroadcastCampaignHandler}.
     */
    public static final BusinessBroadcastCampaignHandler INSTANCE = new BusinessBroadcastCampaignHandler();

    private BusinessBroadcastCampaignHandler() {

    }

    @Override
    public String actionName() {
        return BusinessBroadcastCampaignAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return BusinessBroadcastCampaignAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return BusinessBroadcastCampaignAction.ACTION_VERSION;
    }

    /**
     * Applies a business broadcast campaign mutation.
     *
     * <p>Per WhatsApp Web, on SET the web client validates that broadcastJid,
     * deviceId, and status are non-null, then upserts the campaign into IndexedDB
     * via WAWebBizBroadcastCampaignStorageUtils. On REMOVE, the campaign is
     * deleted from storage.
     *
     * <p>No-op: the store has no broadcast campaign storage.
     *
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} always
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web manages broadcast campaigns in WAWebBizBroadcastCampaignStorageUtils (IndexedDB).
        // No equivalent storage exists in the store.
        return true;
    }
}
