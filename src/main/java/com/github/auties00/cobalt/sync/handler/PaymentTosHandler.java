package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.payment.PaymentTosAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles payment terms of service actions.
 *
 * <p>Per WhatsApp Web {@code WAWebPaymentTosSync}, this action is gated behind
 * SMB (Small and Medium Business) mode and the {@code payments_br_pix_on_web}
 * AB prop. The web client only supports SET operations and extracts the
 * {@code paymentTosAction} value (containing {@code paymentNotice} and
 * {@code accepted} fields), then persists it via {@code setPaymentTos} to
 * browser-local UserPrefs. Since this is an SMB-specific browser-local storage
 * operation with no equivalent in this client's data model, the mutation is
 * acknowledged but not applied locally.
 *
 * <p>Index format: ["payment_tos"]
 */
public final class PaymentTosHandler implements WebAppStateActionHandler {
    public static final PaymentTosHandler INSTANCE = new PaymentTosHandler();

    private PaymentTosHandler() {

    }

    @Override
    public String actionName() {
        return PaymentTosAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return PaymentTosAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return PaymentTosAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return true;
    }
}
