package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.payment.MerchantPaymentPartnerAction;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles merchant payment partner actions.
 *
 * <p>Per WhatsApp Web {@code WAWebMerchantPaymentPartnerSync}, only SET is
 * supported. On SET, validates that {@code merchantPaymentPartnerAction} is
 * non-{@code null}.
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

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!client.abPropsService().getBool(ABProp.PAYMENTS_BR_MERCHANT_PSP_ACCOUNT_STATUS_SYNC)) {
            return MutationApplicationResult.unsupported();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof MerchantPaymentPartnerAction action)) {
            return MutationApplicationResult.malformed();
        }

        client.store().setMerchantPaymentPartner(action);
        return MutationApplicationResult.success();
    }
}
