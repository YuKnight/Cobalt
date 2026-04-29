package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.payment.MerchantPaymentPartnerAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

/**
 * Handles merchant payment partner sync actions.
 *
 * <p>Per WhatsApp Web {@code WAWebMerchantPaymentPartnerSync}, this handler
 * processes the {@code "merchant_payment_partner"} sync action in the
 * {@code RegularLow} collection at version {@code 7}. The handler is
 * restricted to SMB (Small/Medium Business) platforms with the
 * {@code payments_br_merchant_psp_account_status_sync} AB prop enabled, and
 * only {@code SET} operations are supported.
 *
 * <p>On {@code SET}, validates that {@code merchantPaymentPartnerAction} is
 * non-{@code null} and persists the partner to the store via
 * {@code setMerchantPaymentPartner}.
 *
 * <p>Index format: {@code ["merchant_payment_partner"]}
 *
 * @implNote WAWebMerchantPaymentPartnerSync.default — singleton instance
 *           {@code m = new d()} exported as {@code l.default = p} where
 *           {@code d} extends {@code AccountSyncdActionBase}
 */
public final class MerchantPaymentPartnerHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code MerchantPaymentPartnerHandler}.
     *
     * @implNote WAWebMerchantPaymentPartnerSync — module-level
     *           {@code m = new d(); Object.freeze(m); var p = m; l.default = p}
     */
    public static final MerchantPaymentPartnerHandler INSTANCE = new MerchantPaymentPartnerHandler();

    /**
     * Creates a new {@code MerchantPaymentPartnerHandler}.
     *
     * @implNote WAWebMerchantPaymentPartnerSync.d — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     */
    private MerchantPaymentPartnerHandler() {

    }

    /**
     * Returns the action name for merchant payment partner mutations.
     *
     * @implNote WAWebMerchantPaymentPartnerSync.getAction — returns
     *           {@code WASyncdConst.Actions.MerchantPaymentPartner} which is
     *           {@code "merchant_payment_partner"}
     * @return the action name {@code "merchant_payment_partner"}
     */
    @Override
    public String actionName() {
        return MerchantPaymentPartnerAction.ACTION_NAME; // WAWebMerchantPaymentPartnerSync.getAction
    }

    /**
     * Returns the collection name for merchant payment partner mutations.
     *
     * @implNote WAWebMerchantPaymentPartnerSync — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return MerchantPaymentPartnerAction.COLLECTION_NAME; // WAWebMerchantPaymentPartnerSync: collectionName = WASyncdConst.CollectionName.RegularLow
    }

    /**
     * Returns the mutation format version for merchant payment partner mutations.
     *
     * @implNote WAWebMerchantPaymentPartnerSync.getVersion — returns {@code 7}
     * @return {@code 7}
     */
    @Override
    public int version() {
        return MerchantPaymentPartnerAction.ACTION_VERSION; // WAWebMerchantPaymentPartnerSync.getVersion
    }

    /**
     * Applies a single merchant payment partner mutation.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebMerchantPaymentPartnerSync.applyMutations — WA Web
     *           returns {@code WASyncdConst.SyncActionState} values directly;
     *           Cobalt wraps them in {@link MutationApplicationResult} for type
     *           safety
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if applied successfully, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebMerchantPaymentPartnerSync.applyMutations
    }

    /**
     * Applies a single merchant payment partner mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebMerchantPaymentPartnerSync.applyMutations}:
     * <ol>
     *   <li>If the platform is not SMB ({@code isSMB() !== true}), returns
     *       {@code Unsupported} (WA Web logs a WARN and returns
     *       {@code Unsupported} for the entire batch).</li>
     *   <li>If the AB prop
     *       {@code payments_br_merchant_psp_account_status_sync} is not
     *       {@code true}, returns {@code Unsupported} (WA Web logs a WARN
     *       and returns {@code Unsupported} for the entire batch).</li>
     *   <li>If the operation is not {@code "set"}, returns {@code Unsupported}
     *       (WA Web increments an unsupported-count warning at end of batch).</li>
     *   <li>If {@code mutation.value.merchantPaymentPartnerAction} is
     *       {@code null}, returns {@code Malformed} via
     *       {@code WAWebSyncdIndexUtils.malformedActionValue(collectionName)}
     *       (WA Web increments a malformed-count warning at end of batch).</li>
     *   <li>Otherwise calls
     *       {@code WAWebUserPrefsMerchantPaymentPartner.setMerchantPaymentPartner(action)}
     *       and returns {@code Success}.</li>
     * </ol>
     *
     * <p>WA Web's {@code WALogger.WARN} calls for the unsupported/malformed
     * batch counters and the SMB/ABProp gate failures are intentionally
     * omitted in Cobalt; the return semantics are preserved exactly.
     *
     * @implNote WAWebMerchantPaymentPartnerSync.applyMutations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        // WAWebMerchantPaymentPartnerSync.applyMutations: if (WAWebMobilePlatforms.isSMB() !== true) return ... Unsupported
        var platform = client.store().device().platform(); // ADAPTED: WAWebMobilePlatforms.isSMB — checks c === u.SMBA || c === u.SMBI where SMBA = "smba" (ANDROID_BUSINESS) and SMBI = "smbi" (IOS_BUSINESS)
        if (platform != ClientPlatformType.IOS_BUSINESS && platform != ClientPlatformType.ANDROID_BUSINESS) {
            return MutationApplicationResult.unsupported(); // WAWebMerchantPaymentPartnerSync.applyMutations: WALogger.WARN("[MerchantPaymentPartner] unsupported: not SMB"); return t.map(() => ({actionState: Unsupported}))
        }

        // WAWebMerchantPaymentPartnerSync.applyMutations: if (WAWebABProps.getABPropConfigValue("payments_br_merchant_psp_account_status_sync") !== true) return ... Unsupported
        if (!client.abPropsService().getBool(ABProp.PAYMENTS_BR_MERCHANT_PSP_ACCOUNT_STATUS_SYNC)) {
            return MutationApplicationResult.unsupported(); // WAWebMerchantPaymentPartnerSync.applyMutations: WALogger.WARN("[MerchantPaymentPartner] unsupported: ABProp failed"); return t.map(() => ({actionState: Unsupported}))
        }

        // WAWebMerchantPaymentPartnerSync.applyMutations: if (e.operation !== "set") { a++; return {actionState: Unsupported} }
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported(); // WAWebMerchantPaymentPartnerSync.applyMutations: a++, return {actionState: Unsupported}
        }

        // WAWebMerchantPaymentPartnerSync.applyMutations: var t = e.value.merchantPaymentPartnerAction; if (t == null) { i++; return malformedActionValue(n.collectionName) }
        if (!(mutation.value().action().orElse(null) instanceof MerchantPaymentPartnerAction action)) {
            return malformedActionValue(); // WAWebSyncdIndexUtils.malformedActionValue(n.collectionName)
        }

        // WAWebMerchantPaymentPartnerSync.applyMutations: r("WAWebUserPrefsMerchantPaymentPartner").setMerchantPaymentPartner(t)
        client.store().setMerchantPaymentPartner(action); // ADAPTED: WAWebUserPrefsMerchantPaymentPartner.setMerchantPaymentPartner -> WhatsAppStore.setMerchantPaymentPartner
        return MutationApplicationResult.success(); // WAWebMerchantPaymentPartnerSync.applyMutations: {actionState: SyncActionState.Success}
    }
}
