package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.payment.PaymentTosAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.List;

/**
 * Handles payment terms of service sync actions.
 *
 * <p>Per WhatsApp Web {@code WAWebPaymentTosSync}, this handler processes the
 * {@code "payment_tos"} sync action in the {@code RegularLow} collection at
 * version {@code 7}. The handler is restricted to SMB (Small/Medium Business)
 * platforms with the {@code payments_br_pix_on_web} AB prop enabled, and only
 * {@code SET} operations are supported.
 *
 * <p>On {@code SET}, validates that {@code paymentTosAction} is non-{@code null}
 * and persists the accepted payment terms of service to the store via
 * {@code setPaymentTos}.
 *
 * <p>Index format: {@code ["payment_tos"]}
 *
 * @implNote WAWebPaymentTosSync.default — singleton instance {@code m = new d()}
 *           exported as {@code l.default = m} where {@code d} extends
 *           {@code AccountSyncdActionBase}
 */
public final class PaymentTosHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code PaymentTosHandler}.
     *
     * @implNote WAWebPaymentTosSync — module-level {@code m = new d; l.default = m}
     */
    public static final PaymentTosHandler INSTANCE = new PaymentTosHandler();

    /**
     * Creates a new {@code PaymentTosHandler}.
     *
     * @implNote WAWebPaymentTosSync.d — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     */
    private PaymentTosHandler() {

    }

    /**
     * Returns the action name for payment terms of service mutations.
     *
     * @implNote WAWebPaymentTosSync.getAction — returns
     *           {@code WASyncdConst.Actions.PaymentTos} which is
     *           {@code "payment_tos"}
     * @return the action name {@code "payment_tos"}
     */
    @Override
    public String actionName() {
        return PaymentTosAction.ACTION_NAME; // WAWebPaymentTosSync.getAction
    }

    /**
     * Returns the collection name for payment terms of service mutations.
     *
     * @implNote WAWebPaymentTosSync.collectionName — set in constructor to
     *           {@code WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return PaymentTosAction.COLLECTION_NAME; // WAWebPaymentTosSync: collectionName = WASyncdConst.CollectionName.RegularLow
    }

    /**
     * Returns the mutation format version for payment terms of service mutations.
     *
     * @implNote WAWebPaymentTosSync.getVersion — returns {@code 7}
     * @return {@code 7}
     */
    @Override
    public int version() {
        return PaymentTosAction.ACTION_VERSION; // WAWebPaymentTosSync.getVersion
    }

    /**
     * Applies a single payment terms of service mutation.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebPaymentTosSync.applyMutations — WA Web returns
     *           {@code WASyncdConst.SyncActionState} values directly; Cobalt
     *           wraps them in {@link MutationApplicationResult} for type safety
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if applied successfully, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebPaymentTosSync.applyMutations
    }

    /**
     * Applies a single payment terms of service mutation and returns a
     * detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebPaymentTosSync.applyMutations}:
     * <ol>
     *   <li>If the platform is not SMB ({@code isSMB() !== true}), returns
     *       {@code Unsupported} (WA Web logs a WARN "Payment Tos sync:
     *       operation not supported, app is not SMB" and returns
     *       {@code Unsupported} for the entire batch).</li>
     *   <li>If the AB prop {@code payments_br_pix_on_web} is not {@code true},
     *       returns {@code Unsupported} (WA Web logs a WARN "Payment Tos sync:
     *       unsupported, ABProp check failed" and returns {@code Unsupported}
     *       for the entire batch).</li>
     *   <li>If the operation is not {@code "set"}, returns {@code Unsupported}
     *       (WA Web increments an unsupported-count warning at end of batch).</li>
     *   <li>If {@code mutation.value.paymentTosAction} is {@code null},
     *       returns {@code Malformed} via
     *       {@code WAWebSyncdIndexUtils.malformedActionValue(collectionName)}
     *       (WA Web increments a malformed-count warning at end of batch).</li>
     *   <li>Otherwise calls
     *       {@code WAWebUserPrefsPaymentTos.setPaymentTos(action)} and returns
     *       {@code Success}.</li>
     * </ol>
     *
     * <p>WA Web's {@code WALogger.WARN} calls for the unsupported/malformed
     * batch counters and the SMB/ABProp gate failures are intentionally
     * omitted in Cobalt; the return semantics are preserved exactly.
     *
     * @implNote WAWebPaymentTosSync.applyMutations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // WAWebPaymentTosSync.applyMutations: if (WAWebMobilePlatforms.isSMB() !== true) return ... Unsupported
        var platform = client.store().device().platform(); // ADAPTED: WAWebMobilePlatforms.isSMB — checks c === u.SMBA || c === u.SMBI where SMBA = "smba" (ANDROID_BUSINESS) and SMBI = "smbi" (IOS_BUSINESS)
        if (platform != ClientPlatformType.IOS_BUSINESS && platform != ClientPlatformType.ANDROID_BUSINESS) {
            return MutationApplicationResult.unsupported(); // WAWebPaymentTosSync.applyMutations: WALogger.WARN("Payment Tos sync: operation not supported, app is not SMB"); return t.map(() => ({actionState: Unsupported}))
        }

        // WAWebPaymentTosSync.applyMutations: if (WAWebABProps.getABPropConfigValue("payments_br_pix_on_web") !== true) return ... Unsupported
        if (!client.abPropsService().getBool(ABProp.PAYMENTS_BR_PIX_ON_WEB)) {
            return MutationApplicationResult.unsupported(); // WAWebPaymentTosSync.applyMutations: WALogger.WARN("Payment Tos sync: unsupported, ABProp check failed"); return t.map(() => ({actionState: Unsupported}))
        }

        // WAWebPaymentTosSync.applyMutations: if (e.operation !== "set") { r++; return {actionState: Unsupported} }
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported(); // WAWebPaymentTosSync.applyMutations: r++, return {actionState: Unsupported}
        }

        // WAWebPaymentTosSync.applyMutations: var t = e.value.paymentTosAction; if (t == null) { a++; return malformedActionValue(n.collectionName) }
        if (!(mutation.value().action().orElse(null) instanceof PaymentTosAction action)) {
            return malformedActionValue(); // WAWebSyncdIndexUtils.malformedActionValue(n.collectionName)
        }

        // WAWebPaymentTosSync.applyMutations: r("WAWebUserPrefsPaymentTos").setPaymentTos(t)
        client.store().setPaymentTos(action); // ADAPTED: WAWebUserPrefsPaymentTos.setPaymentTos -> WhatsAppStore.setPaymentTos
        return MutationApplicationResult.success(); // WAWebPaymentTosSync.applyMutations: {actionState: SyncActionState.Success}
    }

    /**
     * Builds a pending SET mutation for payment terms of service.
     *
     * <p>Per WhatsApp Web {@code WAWebPaymentTosSync.getPaymentTosSetMutation}:
     * <ol>
     *   <li>Captures the current time via {@code WATimeUtils.unixTimeMs()}</li>
     *   <li>Wraps the action in a value object:
     *       {@code {paymentTosAction: action}}</li>
     *   <li>Delegates to {@code WAWebSyncdActionUtils.buildPendingMutation} with
     *       collection={@code RegularLow}, indexArgs={@code []},
     *       operation={@code SET}, version={@code 7},
     *       action={@code "payment_tos"}</li>
     * </ol>
     *
     * <p>This builder is invoked by {@code WAWebPaymentsTosJob} when the user
     * accepts the payment terms of service and the app needs to sync the new
     * state to the server.
     *
     * @implNote WAWebPaymentTosSync.getPaymentTosSetMutation
     * @param action the payment terms of service action to build the mutation for
     * @return the pending mutation ready for sync upload
     */
    public SyncPendingMutation getPaymentTosSetMutation(PaymentTosAction action) {
        var timestamp = Instant.now(); // WAWebPaymentTosSync.getPaymentTosSetMutation: var t = unixTimeMs()
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: t
                .paymentTosAction(action) // WAWebPaymentTosSync.getPaymentTosSetMutation: {paymentTosAction: e}
                .build();
        var index = JSON.toJSONString(List.of(actionName())); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs)) where indexArgs = []
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, binarySyncAction, version, operation, timestamp, action }
                index, // WAWebSyncdActionUtils.buildPendingMutation: index
                value, // WAWebSyncdActionUtils.buildPendingMutation: binarySyncAction
                SyncdOperation.SET, // WAWebPaymentTosSync.getPaymentTosSetMutation: operation: SyncdMutation$SyncdOperation.SET
                timestamp, // WAWebSyncdActionUtils.buildPendingMutation: timestamp
                version() // WAWebSyncdActionUtils.buildPendingMutation: version: this.getVersion()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
