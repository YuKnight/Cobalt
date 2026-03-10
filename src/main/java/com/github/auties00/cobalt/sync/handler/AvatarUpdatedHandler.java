package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.AvatarUpdatedAction;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles avatar updated actions.
 *
 * <p>Per WhatsApp Web {@code WAWebStickersAvatarUpdatedSyncAction}, only SET
 * operations are supported. The handler validates that the action value contains
 * a non-null {@code eventType}.
 *
 * <p>Index format: ["avatar_updated_action"]
 */
public final class AvatarUpdatedHandler implements WebAppStateActionHandler {
    public static final AvatarUpdatedHandler INSTANCE = new AvatarUpdatedHandler();

    private AvatarUpdatedHandler() {

    }

    @Override
    public String actionName() {
        return AvatarUpdatedAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return AvatarUpdatedAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return AvatarUpdatedAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!client.abPropsService().getBool(ABProp.ENABLE_AVATARS_ON_WEB_COMPANION)) {
            return MutationApplicationResult.unsupported();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof AvatarUpdatedAction action)) {
            return MutationApplicationResult.malformed();
        }

        var eventType = action.eventType().orElse(null);
        if (eventType == null) {
            return MutationApplicationResult.malformed();
        }

        switch (eventType) {
            case CREATED, UPDATED -> client.store().setHasAvatar(true);
            case DELETED -> client.store().setHasAvatar(false);
        }
        return MutationApplicationResult.success();
    }
}
