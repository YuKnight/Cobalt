package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.NctSaltSyncAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles NCT (Notification Content Tokenizer) salt sync actions.
 *
 * <p>Per WhatsApp Web ({@code WAWebNctSaltSync}), the salt is used for
 * privacy-preserving notification content processing. On SET, the salt
 * bytes are stored locally. Missing salt on a SET operation is treated
 * as a malformed action.
 *
 * <p>Index format: ["nct_salt_sync"]
 */
public final class NctSaltSyncHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code NctSaltSyncHandler}.
     */
    public static final NctSaltSyncHandler INSTANCE = new NctSaltSyncHandler();

    private NctSaltSyncHandler() {

    }

    @Override
    public String actionName() {
        return NctSaltSyncAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return NctSaltSyncAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return NctSaltSyncAction.ACTION_VERSION;
    }

    /**
     * Applies an NCT salt sync mutation.
     *
     * <p>Per WhatsApp Web, on SET the salt bytes are extracted from the
     * action value, base64-encoded, and stored in user preferences.
     * A missing salt is logged as a warning and treated as malformed.
     *
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was acknowledged
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() == SyncdOperation.REMOVE) {
            client.store().setNctSalt(null);
            return MutationApplicationResult.success();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof NctSaltSyncAction action)) {
            return MutationApplicationResult.malformed();
        }

        var salt = action.salt().orElse(null);
        if (salt == null) {
            return MutationApplicationResult.malformed();
        }

        client.store().setNctSalt(salt);
        return MutationApplicationResult.success();
    }
}
