package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.ExternalWebBetaAction;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles external web beta sync actions.
 *
 * <p>This handler processes incoming mutations that control external web beta
 * enrollment status. On {@code SET}, reads the {@code isOptIn} flag from the
 * mutation value and updates the store. Other operations are acknowledged as
 * unsupported.
 *
 * <p>The action is identified by the {@code "external_web_beta"} action name in
 * {@code SyncActionValue.externalWebBetaAction}. The collection is
 * {@code Regular} and the version is {@code 3}.
 *
 * <p>Per WhatsApp Web, this handler extends {@code AccountSyncdActionBase} and
 * gates on the {@code external_beta_can_join} AB prop. When disabled, all
 * mutations are returned as {@code Unsupported}.
 *
 * @implNote WAWebExternalWebBetaSync — singleton instance exported as {@code default}
 */
public final class ExternalWebBetaHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of the external web beta handler.
     *
     * <p>Per WhatsApp Web, {@code WAWebExternalWebBetaSync} exports a single instance
     * ({@code var p = new m(); l.default = p}).
     *
     * @implNote WAWebExternalWebBetaSync.default — module-level singleton
     */
    public static final ExternalWebBetaHandler INSTANCE = new ExternalWebBetaHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebExternalWebBetaSync — class {@code m} constructor sets
     *           {@code collectionName = WASyncdConst.CollectionName.Regular}
     */
    private ExternalWebBetaHandler() {

    }

    /**
     * Returns the action name for external web beta actions.
     *
     * @implNote WAWebExternalWebBetaSync.getAction — returns
     *           {@code WASyncdConst.Actions.ExternalWebBeta} ({@code "external_web_beta"})
     * @return the action name {@code "external_web_beta"}
     */
    @Override
    public String actionName() {
        return ExternalWebBetaAction.ACTION_NAME; // WAWebExternalWebBetaSync.getAction -> WASyncdConst.Actions.ExternalWebBeta
    }

    /**
     * Returns the sync collection for external web beta actions.
     *
     * @implNote WAWebExternalWebBetaSync — constructor sets
     *           {@code collectionName = WASyncdConst.CollectionName.Regular}
     * @return the sync patch type {@code REGULAR}
     */
    @Override
    public SyncPatchType collectionName() {
        return ExternalWebBetaAction.COLLECTION_NAME; // WAWebExternalWebBetaSync constructor -> WASyncdConst.CollectionName.Regular
    }

    /**
     * Returns the mutation format version for external web beta actions.
     *
     * @implNote WAWebExternalWebBetaSync.getVersion — returns {@code 3}
     * @return the version {@code 3}
     */
    @Override
    public int version() {
        return ExternalWebBetaAction.ACTION_VERSION; // WAWebExternalWebBetaSync.getVersion -> 3
    }

    /**
     * Applies a single external web beta mutation.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebExternalWebBetaSync.applyMutations — WA Web returns
     *           {@code {actionState: ...}} objects; Cobalt delegates to the richer result method
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    /**
     * Applies a single external web beta mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web, the handler first checks if the {@code external_beta_can_join}
     * AB prop is enabled. If not, all mutations are returned as {@code Unsupported}.
     * For {@code SET} operations, the handler extracts the {@code externalWebBetaAction}
     * from the mutation value, validates its presence and the {@code isOptIn} field,
     * then calls {@code WAWebExternalBetaApi.changeOptInStatusForExternalWebBeta(isOptIn)}.
     * Non-{@code SET} operations are returned as {@code Unsupported}.
     *
     * <p>In Cobalt, the {@code changeOptInStatusForExternalWebBeta} call is adapted to
     * a direct store update via {@code setExternalWebBeta(boolean)}, since the API-level
     * side effects (backend restart, AB prop sync, WAM refresh) are not applicable.
     *
     * @implNote WAWebExternalWebBetaSync.applyMutations — per-mutation logic within
     *           the {@code Promise.all(r.map(...))} block
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!client.abPropsService().getBool(ABProp.EXTERNAL_BETA_CAN_JOIN)) { // WAWebExternalWebBetaSync.applyMutations: getABPropConfigValue("external_beta_can_join") !== true
            return MutationApplicationResult.unsupported();
        }

        if (mutation.operation() != SyncdOperation.SET) { // WAWebExternalWebBetaSync.applyMutations: e.operation === "set"
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof ExternalWebBetaAction action)) { // WAWebExternalWebBetaSync.applyMutations: !r -> malformedActionValue
            return malformedActionValue();
        }

        client.store().setExternalWebBeta(action.isOptIn()); // ADAPTED: WAWebExternalWebBetaSync.applyMutations -> WAWebExternalBetaApi.changeOptInStatusForExternalWebBeta(r.isOptIn)
        return MutationApplicationResult.success();
    }
}
