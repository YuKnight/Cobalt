package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles share own phone number actions.
 *
 * <p>The WhatsApp Web implementation ({@code WAWebShareOwnPnSync}) calls
 * {@code updateLidMetadataJob} to update LID metadata with {@code shareOwnPn: true}.
 * This is an internal background job with no equivalent data model mutation in the
 * Java codebase, so this handler is intentionally a no-op.
 *
 * <p>Index format: ["shareOwnPn", "lidJid"]
 */
public final class ShareOwnPnHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code ShareOwnPnHandler}.
     */
    public static final ShareOwnPnHandler INSTANCE = new ShareOwnPnHandler();

    private ShareOwnPnHandler() {

    }

    @Override
    public String actionName() {
        return "shareOwnPn";
    }

    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR;
    }

    @Override
    public int version() {
        return 8;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return true;
    }
}
