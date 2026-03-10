package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.ChatAssignmentOpenedStatusAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles chat assignment opened status actions.
 *
 * <p>This handler processes mutations that track whether an assigned chat
 * has been opened by the agent.
 *
 * <p>Index format: ["agentChatAssignmentOpenedStatus", "chatJid", "agentId"]
 */
public final class ChatAssignmentOpenedStatusHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code ChatAssignmentOpenedStatusHandler}.
     */
    public static final ChatAssignmentOpenedStatusHandler INSTANCE = new ChatAssignmentOpenedStatusHandler();

    private ChatAssignmentOpenedStatusHandler() {

    }

    @Override
    public String actionName() {
        return ChatAssignmentOpenedStatusAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return ChatAssignmentOpenedStatusAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return ChatAssignmentOpenedStatusAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index());
        var chatJidString = indexArray.getString(1);
        var agentId = indexArray.getString(2);
        if (chatJidString == null || agentId == null) {
            return MutationApplicationResult.malformed();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        var chatJid = Jid.of(chatJidString);
        var chat = client.store().findChatByJid(chatJid);
        if (chat.isEmpty()) {
            return MutationApplicationResult.orphan(chatJidString, "Chat");
        }

        if (!(mutation.value().action().orElse(null) instanceof ChatAssignmentOpenedStatusAction action)) {
            return MutationApplicationResult.malformed();
        }

        var assignmentKey = chat.get().toJid().toString();
        if (!agentId.equals(client.store().chatAssignmentStates().get(assignmentKey))) {
            return MutationApplicationResult.orphan(assignmentKey + "_" + agentId, "ChatAssignment");
        }

        var states = new java.util.HashMap<>(client.store().chatAssignmentOpenedStates());
        states.put(assignmentKey + "_" + agentId, action.chatOpened());
        client.store().setChatAssignmentOpenedStates(states);
        return MutationApplicationResult.success();
    }
}
