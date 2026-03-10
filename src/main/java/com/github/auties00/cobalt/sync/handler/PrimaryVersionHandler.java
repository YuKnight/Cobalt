package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.PrimaryVersionAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles primary version actions.
 *
 * <p>This handler processes mutations that track the primary WhatsApp client version.
 *
 * <p>Index format: ["primaryVersion", "current"|"session_start"]
 */
public final class PrimaryVersionHandler implements WebAppStateActionHandler {
    private static final String INDEX_CURRENT = "current";
    private static final String INDEX_SESSION_START = "session_start";

    public static final PrimaryVersionHandler INSTANCE = new PrimaryVersionHandler();

    private PrimaryVersionHandler() {

    }

    @Override
    public String actionName() {
        return PrimaryVersionAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return PrimaryVersionAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return PrimaryVersionAction.ACTION_VERSION;
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

        if (!(mutation.value().action().orElse(null) instanceof PrimaryVersionAction action)) {
            return MutationApplicationResult.malformed();
        }

        var indexArray = JSON.parseArray(mutation.index());
        var subIndex = indexArray.getString(1);
        if (subIndex == null || (!subIndex.equals(INDEX_CURRENT) && !subIndex.equals(INDEX_SESSION_START))) {
            return MutationApplicationResult.malformed();
        }

        return action.version().isPresent()
                ? MutationApplicationResult.success()
                : MutationApplicationResult.malformed();
    }
}
