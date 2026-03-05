package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.AgentAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles agent actions.
 *
 * <p>This handler processes mutations that manage business account agents
 * (customer service representatives).
 *
 * <p>Index format: ["agentAction", "agentId"]
 */
public final class AgentActionHandler implements WebAppStateActionHandler {
    public static final AgentActionHandler INSTANCE = new AgentActionHandler();

    private AgentActionHandler() {

    }

    @Override
    public String actionName() {
        return AgentAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return AgentAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return AgentAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web creates/merges agent records on SET (with name, deviceId, isDeleted)
        // and removes agents on REMOVE. No agent model exists in the Java store,
        // so this is a no-op.
        return true;
    }
}
