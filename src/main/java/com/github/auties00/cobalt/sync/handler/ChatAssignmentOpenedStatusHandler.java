package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.ChatAssignmentOpenedStatusAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles chat assignment opened status actions.
 *
 * <p>This handler processes mutations that track whether an assigned chat
 * has been opened by the agent.
 *
 * <p>Index format: ["chatAssignmentOpenedStatusAction", "chatJid"]
 */
public final class ChatAssignmentOpenedStatusHandler implements WebAppStateActionHandler {
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
        // Web updates the chatOpenedByAgent flag on an existing chat assignment on SET.
        // REMOVE is unsupported. No chat assignment model exists in the Java store,
        // so this is a no-op.
        return true;
    }
}
