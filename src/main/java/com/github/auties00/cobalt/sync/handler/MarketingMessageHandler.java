package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.MarketingMessageAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles marketing message actions.
 *
 * <p>This handler processes mutations related to premium/marketing message templates.
 * Per WhatsApp Web (WAWebPremiumMessageSync), on SET the web client extracts the
 * marketing message data (id, name, type, isDeleted, message, mediaId) and bulk-creates
 * or merges entries into the PremiumMessageTable and PremiumMessageCollection.
 * Non-SET operations are unsupported.
 *
 * <p>Since the store has no premium/marketing message storage, this handler is a no-op.
 *
 * <p>Index format: ["marketingMessage", messageId]
 */
public final class MarketingMessageHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code MarketingMessageHandler}.
     */
    public static final MarketingMessageHandler INSTANCE = new MarketingMessageHandler();

    private MarketingMessageHandler() {

    }

    @Override
    public String actionName() {
        return MarketingMessageAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return MarketingMessageAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return MarketingMessageAction.ACTION_VERSION;
    }

    /**
     * Applies a marketing message mutation.
     *
     * <p>Per WhatsApp Web, on SET the web client stores the marketing message
     * (id from index[1], name, type, isDeleted, message, mediaId) into IndexedDB
     * and the PremiumMessageCollection. Only SET is supported; REMOVE is unsupported.
     *
     * <p>No-op: the store has no premium/marketing message storage.
     *
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} always
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web stores marketing messages in PremiumMessageTable/PremiumMessageCollection (IndexedDB).
        // No equivalent storage exists in the store.
        return true;
    }
}
