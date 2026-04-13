package com.github.auties00.cobalt.model.sync.action.business;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link BusinessBroadcastCampaignAction}.
 *
 * <p>The sync index produced is {@code ["business_broadcast_campaign", campaignId]}.
 *
 * @param campaignId the unique identifier of the business broadcast campaign
 */
public record BusinessBroadcastCampaignActionArgs(String campaignId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the campaign identifier
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{campaignId};
    }
}
