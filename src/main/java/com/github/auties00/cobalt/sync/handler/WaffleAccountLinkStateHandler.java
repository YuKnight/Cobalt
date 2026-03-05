package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.WaffleAccountLinkStateAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles waffle account link state actions.
 *
 * <p>Index format: ["waffle_account_link_state"]
 */
public final class WaffleAccountLinkStateHandler implements WebAppStateActionHandler {
    public static final WaffleAccountLinkStateHandler INSTANCE = new WaffleAccountLinkStateHandler();

    private WaffleAccountLinkStateHandler() {

    }

    @Override
    public String actionName() {
        return WaffleAccountLinkStateAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return WaffleAccountLinkStateAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return WaffleAccountLinkStateAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web source (WAWebWaffleAccountLinkStateSync): only SET is supported
        // (and only when account linking is enabled via gating).
        // Reads waffleAccountLinkStateAction.linkState (must be non-null).
        // Picks the mutation with the latest timestamp across the batch,
        // maps linkState to an AccountLinkState enum, stores it to IndexedDB
        // via createOrUpdateAccountLinkingState, and sends a peer data
        // operation request (WAFFLE_LINKING_NONCE_FETCH).
        // No equivalent account linking storage exists in the Java data model.
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        if (!(mutation.value().action().orElse(null) instanceof WaffleAccountLinkStateAction action)) {
            return false;
        }

        return action.linkState().isPresent();
    }
}
