package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastCampaignAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles business broadcast campaign actions.
 *
 * <p>This handler processes mutations for business broadcast campaigns.
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
     * <p>Per WhatsApp Web (WAWebBroadcastCampaignSync), on SET the web client validates
     * that the action value is present and that {@code broadcastJid}, {@code deviceId},
     * and {@code status} are all non-null. On REMOVE, the campaign is removed from
     * storage. The campaignId from index[1] must be present or the mutation is malformed.
     *
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was acknowledged, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index());
        var campaignId = indexArray.getString(1);
        if (campaignId == null || campaignId.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        var campaigns = new java.util.HashMap<>(client.store().businessBroadcastCampaigns());
        if (mutation.operation() == SyncdOperation.REMOVE) {
            campaigns.remove(campaignId);
            client.store().setBusinessBroadcastCampaigns(campaigns);
            return MutationApplicationResult.success();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof BusinessBroadcastCampaignAction action)) {
            return MutationApplicationResult.malformed();
        }

        if (action.broadcastJid().isEmpty() || action.deviceId().isEmpty() || action.status().isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        campaigns.put(campaignId, action);
        client.store().setBusinessBroadcastCampaigns(campaigns);
        return MutationApplicationResult.success();
    }
}
