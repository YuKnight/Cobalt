package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.privacy.PrivacySettingRelayAllCalls;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles VoIP relay all calls setting actions.
 */
public final class VoipRelayAllCallsHandler implements WebAppStateActionHandler {
    public static final VoipRelayAllCallsHandler INSTANCE = new VoipRelayAllCallsHandler();

    private VoipRelayAllCallsHandler() {

    }

    @Override
    public String actionName() {
        return PrivacySettingRelayAllCalls.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return PrivacySettingRelayAllCalls.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return PrivacySettingRelayAllCalls.ACTION_VERSION;
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

        if (!(mutation.value().action().orElse(null) instanceof PrivacySettingRelayAllCalls action)) {
            return MutationApplicationResult.malformed();
        }

        client.store().setRelayAllCalls(action.isEnabled());
        return MutationApplicationResult.success();
    }
}
