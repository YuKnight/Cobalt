package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.privacy.PrivacySettingRelayAllCalls;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles VoIP relay all calls setting actions.
 *
 * <p>Index format: ["setting_relayAllCalls", ...]
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
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        if (!(mutation.value().action().orElse(null) instanceof PrivacySettingRelayAllCalls action)) {
            return false;
        }

        client.store().setRelayAllCalls(action.isEnabled());
        return true;
    }
}
