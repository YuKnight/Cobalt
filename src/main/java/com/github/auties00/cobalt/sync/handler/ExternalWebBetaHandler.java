package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.ExternalWebBetaAction;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles external web beta actions.
 *
 * <p>This handler processes mutations that control external web beta enrollment
 * status. On SET, reads the {@code isOptIn} flag from the mutation value and
 * updates the store. Other operations are acknowledged as unsupported.
 *
 * <p>Index format: ["external_web_beta"]
 */
public final class ExternalWebBetaHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code ExternalWebBetaHandler}.
     */
    public static final ExternalWebBetaHandler INSTANCE = new ExternalWebBetaHandler();

    private ExternalWebBetaHandler() {

    }

    @Override
    public String actionName() {
        return ExternalWebBetaAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return ExternalWebBetaAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return ExternalWebBetaAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!client.abPropsService().getBool(ABProp.EXTERNAL_BETA_CAN_JOIN)) {
            return MutationApplicationResult.unsupported();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof ExternalWebBetaAction action)) {
            return MutationApplicationResult.malformed();
        }

        client.store().setExternalWebBeta(action.isOptIn());
        return MutationApplicationResult.success();
    }
}
