package com.github.auties00.cobalt.model.sync.action.chat;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link QuickReplyAction}.
 *
 * <p>The sync index produced is {@code ["quick_reply", quickReplyId]}.
 *
 * @param quickReplyId the unique identifier of the quick reply template
 */
public record QuickReplyActionArgs(String quickReplyId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the quick reply identifier
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{quickReplyId};
    }
}
