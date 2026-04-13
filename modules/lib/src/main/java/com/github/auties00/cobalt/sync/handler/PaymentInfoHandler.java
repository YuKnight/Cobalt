package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.payment.PaymentInfoAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles payment info sync actions.
 *
 * <p>Per WhatsApp Web {@code WAWebPaymentInfoSync}, this handler processes
 * the {@code "payment_info"} sync action in the {@code RegularLow} collection
 * at version {@code 7}. The handler is restricted to SMB (Small/Medium
 * Business) platforms with the
 * {@code order_details_payment_instructions_sync_enabled} AB prop enabled,
 * and only {@code SET} operations are supported.
 *
 * <p>On {@code SET}, validates that {@code paymentInfoAction.cpi} is a
 * non-{@code null} string and persists the CPI info to the store via
 * {@code setPaymentInstructionCpi}.
 *
 * <p>Index format: {@code ["payment_info"]}
 *
 * @implNote WAWebPaymentInfoSync.default — singleton instance
 *           {@code m = new d()} exported as {@code l.default = m} where
 *           {@code d} extends {@code AccountSyncdActionBase}
 */
public final class PaymentInfoHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code PaymentInfoHandler}.
     *
     * @implNote WAWebPaymentInfoSync — module-level
     *           {@code m = new d(); l.default = m}
     */
    public static final PaymentInfoHandler INSTANCE = new PaymentInfoHandler();

    /**
     * Creates a new {@code PaymentInfoHandler}.
     *
     * @implNote WAWebPaymentInfoSync.d — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     */
    private PaymentInfoHandler() {

    }

    /**
     * Returns the action name for payment info mutations.
     *
     * @implNote WAWebPaymentInfoSync.getAction — returns
     *           {@code WASyncdConst.Actions.PaymentInfo} which is
     *           {@code "payment_info"}
     * @return the action name {@code "payment_info"}
     */
    @Override
    public String actionName() {
        return PaymentInfoAction.ACTION_NAME; // WAWebPaymentInfoSync.getAction
    }

    /**
     * Returns the collection name for payment info mutations.
     *
     * @implNote WAWebPaymentInfoSync — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return PaymentInfoAction.COLLECTION_NAME; // WAWebPaymentInfoSync: collectionName = WASyncdConst.CollectionName.RegularLow
    }

    /**
     * Returns the mutation format version for payment info mutations.
     *
     * @implNote WAWebPaymentInfoSync.getVersion — returns {@code 7}
     * @return {@code 7}
     */
    @Override
    public int version() {
        return PaymentInfoAction.ACTION_VERSION; // WAWebPaymentInfoSync.getVersion
    }

    /**
     * Applies a single payment info mutation.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebPaymentInfoSync.applyMutations — WA Web returns
     *           {@code WASyncdConst.SyncActionState} values directly; Cobalt
     *           wraps them in {@link MutationApplicationResult} for type safety
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if applied successfully, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebPaymentInfoSync.applyMutations
    }

    /**
     * Applies a single payment info mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebPaymentInfoSync.applyMutations}:
     * <ol>
     *   <li>If the platform is not SMB ({@code isSMB() !== true}), returns
     *       {@code Unsupported} (WA Web logs a WARN
     *       {@code "payment info sync: operation not supported, app is not SMB"}
     *       and returns {@code Unsupported} for the entire batch).</li>
     *   <li>If the AB prop
     *       {@code order_details_payment_instructions_sync_enabled} is not
     *       {@code true}, returns {@code Unsupported} (WA Web logs a WARN
     *       {@code "payment info sync: unsupported, ABProp not passed"}
     *       and returns {@code Unsupported} for the entire batch).</li>
     *   <li>If the operation is not {@code "set"}, returns {@code Unsupported}
     *       (WA Web increments the {@code r} counter and at end of batch logs
     *       {@code "payment info sync: <r> operations not supported"}).</li>
     *   <li>If {@code mutation.value.paymentInfoAction?.cpi} is not a
     *       {@code string}, returns {@code Malformed} via
     *       {@code WAWebSyncdIndexUtils.malformedActionValue(collectionName)}
     *       (WA Web increments the {@code a} counter and at end of batch
     *       logs {@code "cpi payment info sync: <a> malformed mutations"}).</li>
     *   <li>Otherwise calls
     *       {@code WAWebBackendApi.frontendFireAndForget("setCPIInfo", {cpiInfo: i})}
     *       which routes via {@code WAWebPaymentInfoSyncBridgeApi.setCPIInfo}
     *       to {@code WAWebPaymentInfo.PaymentInfo.setCPIInfo} which diffs
     *       against the current value, calls
     *       {@code WAWebUserPrefsPaymentInfo.setCPIInfo(n)}, and triggers
     *       the {@code CPI_INFO_CHANGE_EVENT}. Returns {@code Success}.</li>
     * </ol>
     *
     * <p>WA Web's {@code WALogger.WARN} calls for the unsupported/malformed
     * batch counters and the SMB/ABProp gate failures are intentionally
     * omitted in Cobalt; the return semantics are preserved exactly. The
     * diff-against-current and event-emission logic in
     * {@code WAWebPaymentInfo.setCPIInfo} is collapsed into the single
     * store setter {@link com.github.auties00.cobalt.store.WhatsAppStore#setPaymentInstructionCpi(String)}.
     *
     * @implNote WAWebPaymentInfoSync.applyMutations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // WAWebPaymentInfoSync.applyMutations: if (WAWebMobilePlatforms.isSMB() !== true) return ... Unsupported
        var platform = client.store().device().platform(); // ADAPTED: WAWebMobilePlatforms.isSMB — checks c === u.SMBA || c === u.SMBI where SMBA = "smba" (ANDROID_BUSINESS) and SMBI = "smbi" (IOS_BUSINESS)
        if (platform != ClientPlatformType.IOS_BUSINESS && platform != ClientPlatformType.ANDROID_BUSINESS) {
            return MutationApplicationResult.unsupported(); // WAWebPaymentInfoSync.applyMutations: WALogger.WARN("payment info sync: operation not supported, app is not SMB"); return t.map(() => ({actionState: Unsupported}))
        }

        // WAWebPaymentInfoSync.applyMutations: if (WAWebABProps.getABPropConfigValue("order_details_payment_instructions_sync_enabled") !== true) return ... Unsupported
        if (!client.abPropsService().getBool(ABProp.ORDER_DETAILS_PAYMENT_INSTRUCTIONS_SYNC_ENABLED)) {
            return MutationApplicationResult.unsupported(); // WAWebPaymentInfoSync.applyMutations: WALogger.WARN("payment info sync: unsupported, ABProp not passed"); return t.map(() => ({actionState: Unsupported}))
        }

        // WAWebPaymentInfoSync.applyMutations: if (e.operation !== "set") { r++; return {actionState: Unsupported} }
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported(); // WAWebPaymentInfoSync.applyMutations: r++, return {actionState: Unsupported}
        }

        // WAWebPaymentInfoSync.applyMutations: var i = (t = e.value.paymentInfoAction) == null ? void 0 : t.cpi
        if (!(mutation.value().action().orElse(null) instanceof PaymentInfoAction action)) {
            return malformedActionValue(); // WAWebPaymentInfoSync.applyMutations: typeof i != "string" (paymentInfoAction missing) -> a++, WAWebSyncdIndexUtils.malformedActionValue(n.collectionName)
        }

        // WAWebPaymentInfoSync.applyMutations: typeof i != "string" ? (a++, malformedActionValue(n.collectionName)) : ...
        var cpi = action.cpi().orElse(null);
        if (cpi == null) {
            return malformedActionValue(); // WAWebPaymentInfoSync.applyMutations: a++, WAWebSyncdIndexUtils.malformedActionValue(n.collectionName)
        }

        // WAWebPaymentInfoSync.applyMutations: WAWebBackendApi.frontendFireAndForget("setCPIInfo", {cpiInfo: i}) ->
        // WAWebPaymentInfoSyncBridgeApi.setCPIInfo({cpiInfo: e}) ->
        // WAWebPaymentInfo.PaymentInfo.setCPIInfo(n) -> if (current != n) WAWebUserPrefsPaymentInfo.setCPIInfo(n); this.trigger(CPI_INFO_CHANGE_EVENT)
        client.store().setPaymentInstructionCpi(cpi); // ADAPTED: WAWebBackendApi.frontendFireAndForget("setCPIInfo") -> WAWebPaymentInfoSyncBridgeApi.setCPIInfo -> WAWebPaymentInfo.setCPIInfo -> WAWebUserPrefsPaymentInfo.setCPIInfo collapsed into WhatsAppStore.setPaymentInstructionCpi
        return MutationApplicationResult.success(); // WAWebPaymentInfoSync.applyMutations: {actionState: SyncActionState.Success}
    }
}
