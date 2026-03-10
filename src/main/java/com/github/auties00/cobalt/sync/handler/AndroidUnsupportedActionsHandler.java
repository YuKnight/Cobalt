package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.AndroidUnsupportedActions;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles Android unsupported actions.
 *
 * <p>This handler processes mutations that declare which actions are unsupported
 * on Android devices. On SET, reads the {@code allowed} flag from the mutation
 * value. Other operations are acknowledged as unsupported.
 *
 * <p>Index format: ["android_unsupported_actions"]
 */
public final class AndroidUnsupportedActionsHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code AndroidUnsupportedActionsHandler}.
     */
    public static final AndroidUnsupportedActionsHandler INSTANCE = new AndroidUnsupportedActionsHandler();

    private AndroidUnsupportedActionsHandler() {

    }

    @Override
    public String actionName() {
        return AndroidUnsupportedActions.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return AndroidUnsupportedActions.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return AndroidUnsupportedActions.ACTION_VERSION;
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

        if (!(mutation.value().action().orElse(null) instanceof AndroidUnsupportedActions action)) {
            return MutationApplicationResult.malformed();
        }

        if (action.allowed()) {
            client.store().setPrimaryAllowsAllMutations(true);
        }

        return MutationApplicationResult.success();
    }
}
