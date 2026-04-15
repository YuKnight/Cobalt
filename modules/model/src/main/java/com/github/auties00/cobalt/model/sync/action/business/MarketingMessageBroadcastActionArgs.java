package com.github.auties00.cobalt.model.sync.action.business;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link MarketingMessageBroadcastAction}.
 *
 * <p>The sync index produced is
 * {@code ["marketingMessageBroadcast", marketingMessageId, broadcastMessageId]}.
 *
 * @param marketingMessageId the unique identifier of the marketing (premium) message template
 * @param broadcastMessageId the identifier of the individual broadcast message sent from the template
 */
public record MarketingMessageBroadcastActionArgs(String marketingMessageId, String broadcastMessageId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a two-element array containing the marketing message and broadcast message identifiers
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{marketingMessageId, broadcastMessageId};
    }
}
