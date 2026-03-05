package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.MarketingMessageBroadcastAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles marketing message broadcast actions.
 *
 * <p>This handler processes mutations that associate sent message IDs with
 * premium/marketing messages. Per WhatsApp Web (WAWebPremiumMessageBroadcastSync),
 * on SET the web client looks up the premium message by id (index[1]) in the
 * PremiumMessageCollection. If found, it associates the sent message id (index[2])
 * with the premium message via WAWebPremiumMessageAddSendAction. Non-SET operations
 * are unsupported. If the premium message is not found, the mutation is treated as
 * an orphan.
 *
 * <p>Since the store has no premium/marketing message storage, this handler is a no-op.
 *
 * <p>Index format: ["marketingMessageBroadcast", premiumMessageId, messageId]
 */
public final class MarketingMessageBroadcastHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code MarketingMessageBroadcastHandler}.
     */
    public static final MarketingMessageBroadcastHandler INSTANCE = new MarketingMessageBroadcastHandler();

    private MarketingMessageBroadcastHandler() {

    }

    @Override
    public String actionName() {
        return MarketingMessageBroadcastAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return MarketingMessageBroadcastAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return MarketingMessageBroadcastAction.ACTION_VERSION;
    }

    /**
     * Applies a marketing message broadcast mutation.
     *
     * <p>Per WhatsApp Web, on SET the web client finds the premium message by
     * premiumMessageId (index[1]) and associates the sent messageId (index[2])
     * with it. If the premium message doesn't exist, the mutation is orphaned.
     * Only SET is supported; REMOVE is unsupported.
     *
     * <p>No-op: the store has no premium/marketing message storage.
     *
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} always
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web associates sent message IDs with premium messages in PremiumMessageCollection.
        // No equivalent storage exists in the store.
        return true;
    }
}
