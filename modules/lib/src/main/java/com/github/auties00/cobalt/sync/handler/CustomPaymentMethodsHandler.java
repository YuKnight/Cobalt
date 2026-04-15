package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.payment.CustomPaymentMethodsAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.List;

/**
 * Handles custom payment methods sync actions.
 *
 * <p>Per WhatsApp Web {@code WAWebCustomPaymentMethodsSync}, this handler
 * processes the {@code "custom_payment_methods"} sync action in the
 * {@code RegularLow} collection at version 7. Only SET operations are
 * supported, and the handler is restricted to SMB (Small/Medium Business)
 * platforms with the {@code payments_br_pix_phase_1_seller_sync_enabled}
 * AB prop enabled.
 *
 * <p>On SET, validates that
 * {@code customPaymentMethodsAction.customPaymentMethods} is non-{@code null},
 * then persists the custom payment methods to the store.
 *
 * <p>Index format: {@code ["custom_payment_methods"]}
 *
 * @implNote WAWebCustomPaymentMethodsSync.default — singleton instance of the
 *           handler class that extends {@code AccountSyncdActionBase}
 */
public final class CustomPaymentMethodsHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code CustomPaymentMethodsHandler}.
     *
     * @implNote WAWebCustomPaymentMethodsSync.default — {@code new d} (module-level singleton)
     */
    public static final CustomPaymentMethodsHandler INSTANCE = new CustomPaymentMethodsHandler();

    /**
     * Creates a new {@code CustomPaymentMethodsHandler}.
     *
     * @implNote WAWebCustomPaymentMethodsSync.d — constructor sets
     *           {@code collectionName = WASyncdConst.CollectionName.RegularLow}
     */
    private CustomPaymentMethodsHandler() {

    }

    /**
     * Returns the action name for custom payment methods.
     *
     * @implNote WAWebCustomPaymentMethodsSync.getAction — returns
     *           {@code WASyncdConst.Actions.CustomPaymentMethods} which is
     *           {@code "custom_payment_methods"}
     * @return the action name {@code "custom_payment_methods"}
     */
    @Override
    public String actionName() {
        return CustomPaymentMethodsAction.ACTION_NAME; // WAWebCustomPaymentMethodsSync.getAction
    }

    /**
     * Returns the collection name for custom payment methods.
     *
     * @implNote WAWebCustomPaymentMethodsSync.collectionName — set in constructor to
     *           {@code WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return CustomPaymentMethodsAction.COLLECTION_NAME; // WAWebCustomPaymentMethodsSync: collectionName = WASyncdConst.CollectionName.RegularLow
    }

    /**
     * Returns the mutation format version for custom payment methods.
     *
     * @implNote WAWebCustomPaymentMethodsSync.getVersion — returns {@code 7}
     * @return {@code 7}
     */
    @Override
    public int version() {
        return CustomPaymentMethodsAction.ACTION_VERSION; // WAWebCustomPaymentMethodsSync.getVersion
    }

    /**
     * Applies a single custom payment methods mutation.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebCustomPaymentMethodsSync.applyMutations — WA Web returns
     *           {@code SyncActionState} values directly; Cobalt wraps in
     *           {@link MutationApplicationResult} for type safety
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if applied successfully, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS; // ADAPTED: WAWebCustomPaymentMethodsSync.applyMutations
    }

    /**
     * Applies a single custom payment methods mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebCustomPaymentMethodsSync.applyMutations}:
     * <ol>
     *   <li>If the platform is not SMB ({@code isSMB() !== true}), returns
     *       {@code Unsupported} for all mutations in the batch.</li>
     *   <li>If the AB prop {@code payments_br_pix_phase_1_seller_sync_enabled}
     *       is not {@code true}, returns {@code Unsupported} for all mutations.</li>
     *   <li>If the operation is not {@code "set"}, returns {@code Unsupported}.</li>
     *   <li>If {@code customPaymentMethodsAction.customPaymentMethods} is
     *       {@code null}, returns {@code Malformed} via
     *       {@code WAWebSyncdIndexUtils.malformedActionValue}.</li>
     *   <li>Otherwise calls
     *       {@code WAWebBackendApi.frontendFireAndForget("setCustomPaymentMethods", ...)}
     *       and returns {@code Success}.</li>
     * </ol>
     *
     * @implNote WAWebCustomPaymentMethodsSync.applyMutations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // WAWebCustomPaymentMethodsSync.applyMutations: if (isSMB() !== true) return ... Unsupported
        var platform = client.store().device().platform(); // ADAPTED: WAWebMobilePlatforms.isSMB — checks c === u.SMBA || c === u.SMBI where SMBA = "smba" (ANDROID_BUSINESS) and SMBI = "smbi" (IOS_BUSINESS)
        if (platform != ClientPlatformType.IOS_BUSINESS && platform != ClientPlatformType.ANDROID_BUSINESS) {
            return MutationApplicationResult.unsupported();
        }

        // WAWebCustomPaymentMethodsSync.applyMutations: if (getABPropConfigValue("payments_br_pix_phase_1_seller_sync_enabled") !== true) return ... Unsupported
        if (!client.abPropsService().getBool(ABProp.PAYMENTS_BR_PIX_PHASE_1_SELLER_SYNC_ENABLED)) {
            return MutationApplicationResult.unsupported();
        }

        // WAWebCustomPaymentMethodsSync.applyMutations: if (e.operation !== "set") return ... Unsupported
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        // WAWebCustomPaymentMethodsSync.applyMutations: var i = e.value.customPaymentMethodsAction?.customPaymentMethods; if (i == null) return malformedActionValue(...)
        if (!(mutation.value().action().orElse(null) instanceof CustomPaymentMethodsAction action)) {
            return malformedActionValue(); // WAWebSyncdIndexUtils.malformedActionValue(n.collectionName)
        }

        // WAWebCustomPaymentMethodsSync.applyMutations: frontendFireAndForget("setCustomPaymentMethods", {customPaymentMethods: i})
        client.store().setCustomPaymentMethods(action.customPaymentMethods()); // ADAPTED: WAWebBackendApi.frontendFireAndForget -> direct store call
        return MutationApplicationResult.success(); // WAWebCustomPaymentMethodsSync.applyMutations: {actionState: SyncActionState.Success}
    }

    /**
     * Builds a pending SET mutation for custom payment methods.
     *
     * <p>Per WhatsApp Web {@code WAWebCustomPaymentMethodsSync.getCustomPaymentMethodSetMutation}:
     * <ol>
     *   <li>Captures the current time via {@code WATimeUtils.unixTimeMs()}</li>
     *   <li>Wraps the action in a value object:
     *       {@code {customPaymentMethodsAction: action}}</li>
     *   <li>Delegates to {@code WAWebSyncdActionUtils.buildPendingMutation} with
     *       collection={@code RegularLow}, indexArgs={@code []},
     *       operation={@code SET}, version={@code 7},
     *       action={@code "custom_payment_methods"}</li>
     * </ol>
     *
     * @implNote WAWebCustomPaymentMethodsSync.getCustomPaymentMethodSetMutation
     * @param action the custom payment methods action to build the mutation for
     * @return the pending mutation ready for sync upload
     */
    public SyncPendingMutation getCustomPaymentMethodSetMutation(CustomPaymentMethodsAction action) {
        var timestamp = Instant.now(); // WAWebCustomPaymentMethodsSync.getCustomPaymentMethodSetMutation: var t = unixTimeMs()
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: t
                .customPaymentMethodsAction(action) // WAWebCustomPaymentMethodsSync.getCustomPaymentMethodSetMutation: {customPaymentMethodsAction: e}
                .build();
        var index = JSON.toJSONString(List.of(actionName())); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs)) where indexArgs = []
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, binarySyncAction, version, operation, timestamp, action }
                index, // WAWebSyncdActionUtils.buildPendingMutation: index
                value, // WAWebSyncdActionUtils.buildPendingMutation: binarySyncAction
                SyncdOperation.SET, // WAWebCustomPaymentMethodsSync.getCustomPaymentMethodSetMutation: operation: SyncdMutation$SyncdOperation.SET
                timestamp, // WAWebSyncdActionUtils.buildPendingMutation: timestamp
                version() // WAWebSyncdActionUtils.buildPendingMutation: version: this.getVersion()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
