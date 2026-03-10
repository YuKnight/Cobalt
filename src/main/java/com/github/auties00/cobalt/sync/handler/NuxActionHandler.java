package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.NuxAction;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles NUX (New User Experience) actions.
 *
 * <p>This handler processes mutations that track completion of onboarding steps
 * and new feature introductions. On SET, reads the nux key from the index and
 * the {@code acknowledged} flag and timestamp from the mutation value.
 * Other operations are acknowledged as unsupported.
 *
 * <p>Index format: ["nux", "nuxId"]
 */
public final class NuxActionHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code NuxActionHandler}.
     */
    public static final NuxActionHandler INSTANCE = new NuxActionHandler();

    private NuxActionHandler() {

    }

    @Override
    public String actionName() {
        return NuxAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return NuxAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return NuxAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!client.abPropsService().getBool(ABProp.NUX_SYNC)) {
            return MutationApplicationResult.unsupported();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        var indexArray = JSON.parseArray(mutation.index());
        var nuxKey = indexArray.getString(1);
        if (nuxKey == null) {
            return MutationApplicationResult.malformed();
        }

        if (!(mutation.value().action().orElse(null) instanceof NuxAction action)) {
            return MutationApplicationResult.malformed();
        }

        var states = new java.util.HashMap<>(client.store().nuxStates());
        states.put(nuxKey, action.acknowledged());
        client.store().setNuxStates(states);
        return MutationApplicationResult.success();
    }
}
