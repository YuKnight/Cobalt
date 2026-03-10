package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.ChatAssignmentAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles chat assignment actions.
 *
 * <p>This handler processes mutations that assign chats to agents
 * in business accounts.
 *
 * <p>Index format: ["agentChatAssignment", "chatJid"]
 */
public final class ChatAssignmentHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code ChatAssignmentHandler}.
     */
    public static final ChatAssignmentHandler INSTANCE = new ChatAssignmentHandler();

    private ChatAssignmentHandler() {

    }

    @Override
    public String actionName() {
        return ChatAssignmentAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return ChatAssignmentAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return ChatAssignmentAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index());
        var chatJidString = indexArray.getString(1);
        if (chatJidString == null || chatJidString.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof ChatAssignmentAction action)) {
            return MutationApplicationResult.malformed();
        }

        var chatJid = Jid.of(chatJidString);
        var chat = client.store().findChatByJid(chatJid);
        if (chat.isEmpty()) {
            return MutationApplicationResult.orphan(chatJidString, "Chat");
        }

        var agentId = action.deviceAgentID().orElse("");
        if (!agentId.isEmpty() && !client.store().agentStates().containsKey(agentId)) {
            return MutationApplicationResult.orphan(agentId, "Agent");
        }

        var states = new java.util.HashMap<>(client.store().chatAssignmentStates());
        if (agentId.isEmpty()) {
            states.remove(chat.get().toJid().toString());
        } else {
            states.put(chat.get().toJid().toString(), agentId);
        }
        client.store().setChatAssignmentStates(states);
        return MutationApplicationResult.success();
    }
}
