package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.KeyExpirationAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles sentinel actions for sync key expiration.
 *
 * <p>This handler processes mutations that signal sync key expiration. The actual
 * key expiration logic is handled elsewhere in the key management subsystem; this
 * handler acknowledges the mutation.
 *
 * <p>Index format: ["sentinel", collectionName]
 */
public final class SentinelHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code SentinelHandler}.
     */
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
        // Web source (WAWebSentinelMutationSync): only SET is supported.
        // Reads value.keyExpiration.expiredKeyEpoch and calls
        // expireSyncKeyInTransaction(epoch) to expire the sync key.
        // No equivalent sync key expiration API exists in the Java store;
        // key management is handled separately.
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        if (!(mutation.value().action().orElse(null) instanceof KeyExpirationAction action)) {
            return false;
        }

        return action.expiredKeyEpoch().isPresent();
    }
}
