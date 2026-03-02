package com.github.auties00.cobalt.model.sync.action.business;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link MarketingMessageAction}.
 *
 * <p>The sync index produced is {@code ["marketingMessage", marketingMessageId]}.
 *
 * @param marketingMessageId the unique identifier of the marketing (premium) message template
 */
public record MarketingMessageActionArgs(String marketingMessageId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the marketing message identifier
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{marketingMessageId};
    }
}
