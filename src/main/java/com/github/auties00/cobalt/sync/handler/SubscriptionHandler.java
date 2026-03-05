package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.SubscriptionAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles subscription actions.
 *
 * <p>This handler processes mutations that manage newsletter/channel subscriptions.
 *
 * <p>Index format: ["subscriptionAction", "subscriptionId"]
 */
public final class SubscriptionHandler implements WebAppStateActionHandler {
    public static final SubscriptionHandler INSTANCE = new SubscriptionHandler();

    private SubscriptionHandler() {

    }

    @Override
    public String actionName() {
        return SubscriptionAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return SubscriptionAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return SubscriptionAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // No dedicated web module for subscription sync.
        // No subscription model exists in the Java store, so this is a no-op.
        return true;
    }
}
