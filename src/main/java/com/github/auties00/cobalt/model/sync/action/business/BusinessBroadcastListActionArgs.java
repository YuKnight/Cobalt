package com.github.auties00.cobalt.model.sync.action.business;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link BusinessBroadcastListAction}.
 *
 * <p>The sync index produced is {@code ["business_broadcast_list", broadcastListId]}.
 *
 * @param broadcastListId the unique identifier of the business broadcast list
 */
public record BusinessBroadcastListActionArgs(String broadcastListId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the broadcast list identifier
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{broadcastListId};
    }
}
