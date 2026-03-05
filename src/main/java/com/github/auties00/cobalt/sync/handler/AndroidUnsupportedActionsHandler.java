package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.AndroidUnsupportedActions;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles Android unsupported actions.
 *
 * <p>This handler processes mutations that declare which actions are unsupported
 * on Android devices. The allowed flags are acknowledged but not acted upon,
 * as this client does not need to enforce Android-specific restrictions.
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
        // Web source (WAWebAndroidUnsupportedActionsSync): only SET is supported.
        // Reads value.androidUnsupportedActions (must be non-null).
        // If allowed is true, sets the primaryAllowsAllMutations flag in localStorage.
        // No equivalent flag exists in the Java data model.
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        if (!(mutation.value().action().orElse(null) instanceof AndroidUnsupportedActions)) {
            return false;
        }

        return true;
    }
}
