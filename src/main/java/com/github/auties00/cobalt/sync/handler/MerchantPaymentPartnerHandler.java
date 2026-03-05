package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.payment.MerchantPaymentPartnerAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles merchant payment partner actions.
 *
 * <p>This handler processes mutations for merchant payment partner configuration.
 * Per WhatsApp Web (WAWebMerchantPaymentPartnerSync), the handler is gated behind
 * an SMB (small-medium business) check and the {@code payments_br_merchant_psp_account_status_sync}
 * ABProp. On SET, the web client extracts the action and stores it via
 * {@code WAWebUserPrefsMerchantPaymentPartner.setMerchantPaymentPartner()}. Only SET
 * is supported; REMOVE is unsupported.
 *
 * <p>Since the store has no merchant payment partner storage, this handler is a no-op.
 *
 * <p>Index format: ["merchant_payment_partner"]
 */
public final class MerchantPaymentPartnerHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code MerchantPaymentPartnerHandler}.
     */
    public static final MerchantPaymentPartnerHandler INSTANCE = new MerchantPaymentPartnerHandler();

    private MerchantPaymentPartnerHandler() {

    }

    @Override
    public String actionName() {
        return MerchantPaymentPartnerAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return MerchantPaymentPartnerAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return MerchantPaymentPartnerAction.ACTION_VERSION;
    }

    /**
     * Applies a merchant payment partner mutation.
     *
     * <p>Per WhatsApp Web, this is gated behind isSMB() and an ABProp check.
     * On SET, the web client stores the merchant payment partner action data
     * (status, country, gatewayName, credentialId) into user preferences.
     * Only SET is supported; REMOVE is unsupported.
     *
     * <p>No-op: the store has no merchant payment partner storage.
     *
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} always
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web stores merchant payment partner data in user preferences
        // (WAWebUserPrefsMerchantPaymentPartner). No equivalent storage exists in the store.
        return true;
    }
}
