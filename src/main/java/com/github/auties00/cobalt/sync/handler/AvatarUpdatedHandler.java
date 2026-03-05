package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.AvatarUpdatedAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles avatar updated actions.
 *
 * <p>Index format: ["avatar_updated_action", ...]
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
        // Web: WAWebStickersAvatarUpdatedSyncAction — only SET is supported.
        // Checks eventType (CREATED/UPDATED -> hasAvatar=true, DELETED -> hasAvatar=false)
        // and removes all recent avatar stickers.
        // No equivalent store methods for hasAvatar or avatar stickers in the Java data model.
        return true;
    }
}
