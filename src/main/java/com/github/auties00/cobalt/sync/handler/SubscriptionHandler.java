package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.SubscriptionAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

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
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() == SyncdOperation.REMOVE) {
            client.store().setSubscriptionDeactivated(false);
            client.store().setSubscriptionAutoRenewing(false);
            client.store().setSubscriptionExpirationDate(null);
            return MutationApplicationResult.success();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof SubscriptionAction action)) {
            return MutationApplicationResult.malformed();
        }

        client.store().setSubscriptionDeactivated(action.isDeactivated());
        client.store().setSubscriptionAutoRenewing(action.isAutoRenewing());
        client.store().setSubscriptionExpirationDate(action.expirationDate().isPresent() ? action.expirationDate().getAsLong() : null);
        return MutationApplicationResult.success();
    }
}
