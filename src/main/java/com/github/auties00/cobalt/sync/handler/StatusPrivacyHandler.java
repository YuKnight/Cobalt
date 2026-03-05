package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.StatusPrivacyAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles status privacy actions.
 *
 * <p>This handler processes mutations that control the privacy settings for
 * status updates (e.g., who can see the user's status). The mutation is
 * acknowledged but not applied locally.
 *
 * <p>Index format: ["status_privacy"]
 */
public final class StatusPrivacyHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code StatusPrivacyHandler}.
     */
    public static final StatusPrivacyHandler INSTANCE = new StatusPrivacyHandler();

    private StatusPrivacyHandler() {

    }

    @Override
    public String actionName() {
        return StatusPrivacyAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return StatusPrivacyAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return StatusPrivacyAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web: WAWebStatusPrivacySettingSync — only SET is supported.
        // Extracts statusPrivacy.mode (CONTACTS, ALLOW_LIST, DENY_LIST, CLOSE_FRIENDS)
        // and statusPrivacy.userJid (allow/deny list JIDs), then stores them in IndexedDB
        // and fires a BackendEventBus.triggerUpdateStatusPrivacySettings event.
        // No equivalent store methods for status privacy settings in the Java data model.
        return true;
    }
}
