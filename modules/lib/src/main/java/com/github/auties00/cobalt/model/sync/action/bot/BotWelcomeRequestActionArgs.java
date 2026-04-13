package com.github.auties00.cobalt.model.sync.action.bot;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link BotWelcomeRequestAction}.
 *
 * <p>The sync index produced is {@code ["bot_welcome_request", chatJid]}.
 *
 * @param chatJid the JID of the bot chat for which a welcome message was requested
 */
public record BotWelcomeRequestActionArgs(Jid chatJid) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the chat JID string
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{chatJid.toString()};
    }
}
