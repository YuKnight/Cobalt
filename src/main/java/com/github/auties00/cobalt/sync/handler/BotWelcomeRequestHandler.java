package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.bot.BotWelcomeRequestAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles bot welcome request actions.
 *
 * <p>Index format: ["bot_welcome_request", "chatJid"]
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
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() == SyncdOperation.REMOVE) {
            return MutationApplicationResult.unsupported();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        var indexArray = JSON.parseArray(mutation.index());
        var chatJidString = indexArray.getString(1);
        if (chatJidString == null || chatJidString.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        if (!(mutation.value().action().orElse(null) instanceof BotWelcomeRequestAction action)) {
            return MutationApplicationResult.malformed();
        }

        var chatJid = Jid.of(chatJidString);
        var chat = client.store().findChatByJid(chatJid);
        if (chat.isEmpty()) {
            return MutationApplicationResult.orphan(chatJidString, "Chat");
        }

        var states = new java.util.HashMap<>(client.store().botWelcomeRequestStates());
        states.put(chat.get().toJid().toString(), action.isSent());
        client.store().setBotWelcomeRequestStates(states);
        return MutationApplicationResult.success();
    }
}
