package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.bot.BotWelcomeRequestAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles bot welcome request actions.
 *
 * <p>Per WhatsApp Web {@code WAWebBotWelcomeRequestSync}, this action only supports
 * SET operations (REMOVE is unsupported). The web client extracts
 * {@code botWelcomeRequestAction.isSent} (boolean), resolves a chat from index[1],
 * and updates the chat's {@code hasRequestedWelcomeMsg} field in IndexedDB. Since
 * this client's chat model does not track the welcome message request state, the
 * mutation is acknowledged but not applied locally.
 *
 * <p>Index format: ["bot_welcome_request", chatJid]
 */
public final class BotWelcomeRequestHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code BotWelcomeRequestHandler}.
     */
    public static final BotWelcomeRequestHandler INSTANCE = new BotWelcomeRequestHandler();

    private BotWelcomeRequestHandler() {

    }

    @Override
    public String actionName() {
        return BotWelcomeRequestAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return BotWelcomeRequestAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return BotWelcomeRequestAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return true;
    }
}
