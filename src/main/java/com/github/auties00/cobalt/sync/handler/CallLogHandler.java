package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.call.CallLogAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import com.alibaba.fastjson2.JSON;

/**
 * Handles call log actions.
 *
 * <p>Index format: ["call_log", "peerJid", "callId", "isFromMe"]
 */
public final class CallLogHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code CallLogHandler}.
     */
    public static final CallLogHandler INSTANCE = new CallLogHandler();

    private CallLogHandler() {

    }

    @Override
    public String actionName() {
        return CallLogAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return CallLogAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return CallLogAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index());
        if (indexArray.size() < 4) {
            return MutationApplicationResult.malformed();
        }

        var peer = indexArray.getString(1);
        var callId = indexArray.getString(2);
        var fromMe = indexArray.getString(3);
        if (peer == null || callId == null || fromMe == null) {
            return MutationApplicationResult.malformed();
        }

        var key = peer + "|" + callId + "|" + fromMe;
        var states = new java.util.HashMap<>(client.store().callLogStates());
        if (mutation.operation() == SyncdOperation.REMOVE) {
            states.remove(key);
            client.store().setCallLogStates(states);
            return MutationApplicationResult.success();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof CallLogAction action)) {
            return MutationApplicationResult.malformed();
        }

        var log = action.log().orElse(null);
        if (log == null) {
            return MutationApplicationResult.malformed();
        }

        states.put(key, log);
        client.store().setCallLogStates(states);
        return MutationApplicationResult.success();
    }
}
