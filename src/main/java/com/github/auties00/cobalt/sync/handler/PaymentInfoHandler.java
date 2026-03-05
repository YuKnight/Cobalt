package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.payment.PaymentInfoAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles payment info actions.
 *
 * <p>Per WhatsApp Web {@code WAWebPaymentInfoSync}, this action is gated behind
 * SMB (Small and Medium Business) mode and the
 * {@code order_details_payment_instructions_sync_enabled} AB prop. The web client
 * only supports SET operations and extracts {@code paymentInfoAction.cpi} (a string),
 * then fires a {@code setCPIInfo} frontend event. Since this is an SMB-specific
 * frontend operation with no equivalent in this client's data model, the mutation
 * is acknowledged but not applied locally.
 *
 * <p>Index format: ["payment_info"]
 */
public final class PaymentInfoHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code PaymentInfoHandler}.
     */
    public static final PaymentInfoHandler INSTANCE = new PaymentInfoHandler();

    private PaymentInfoHandler() {

    }

    @Override
    public String actionName() {
        return PaymentInfoAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return PaymentInfoAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return PaymentInfoAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return true;
    }
}
