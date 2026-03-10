package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.KeyExpirationAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles sentinel actions for sync key expiration.
 */
public final class SentinelHandler implements WebAppStateActionHandler {
    public static final SentinelHandler INSTANCE = new SentinelHandler();

    private SentinelHandler() {

    }

    @Override
    public String actionName() {
        return KeyExpirationAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return KeyExpirationAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return KeyExpirationAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof KeyExpirationAction action)) {
            return MutationApplicationResult.malformed();
        }

        var expiredEpoch = action.expiredKeyEpoch();
        if (expiredEpoch.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        client.store().expireAppStateKeysByEpoch(expiredEpoch.getAsInt());
        return MutationApplicationResult.success();
    }
}
