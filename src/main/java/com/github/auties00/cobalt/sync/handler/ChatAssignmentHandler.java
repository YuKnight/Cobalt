package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.ChatAssignmentAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles chat assignment actions.
 *
 * <p>This handler processes mutations that assign chats to agents
 * in business accounts.
 *
 * <p>Index format: ["chatAssignmentAction", "chatJid"]
 */
public final class ChatAssignmentHandler implements WebAppStateActionHandler {
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
        // Web creates/merges chat assignment records on SET (mapping chatId to agentId)
        // with chatOpenedByAgent=false, and creates system messages for assignment changes.
        // REMOVE is unsupported. No chat assignment model exists in the Java store,
        // so this is a no-op.
        return true;
    }
}
