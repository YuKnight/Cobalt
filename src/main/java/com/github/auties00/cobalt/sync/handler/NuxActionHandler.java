package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.NuxAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles NUX (New User Experience) actions.
 *
 * <p>This handler processes mutations that track completion of onboarding steps
 * and new feature introductions.
 *
 * <p>Index format: ["nux", "nuxId"]
 */
public final class NuxActionHandler implements WebAppStateActionHandler {

    public static final NuxActionHandler INSTANCE = new NuxActionHandler();

    private NuxActionHandler() {

    }

    @Override
    public String actionName() {
        return NuxAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return NuxAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return NuxAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web source (WAWebNuxSync): on SET, reads indexParts[1] as nuxKey,
        // reads value.nuxAction.acknowledged and value.timestamp,
        // then calls updateNuxSyncList to persist NUX state in localStorage.
        // REMOVE operations are unsupported.
        // No equivalent NUX preference storage exists in the Java data model.
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        return true;
    }
}
