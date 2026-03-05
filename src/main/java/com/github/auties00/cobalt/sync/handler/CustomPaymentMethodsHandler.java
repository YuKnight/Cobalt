package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.payment.CustomPaymentMethodsAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles custom payment methods actions.
 *
 * <p>Per WhatsApp Web {@code WAWebCustomPaymentMethodsSync}, this action is gated
 * behind SMB (Small and Medium Business) mode and the
 * {@code payments_br_pix_phase_1_seller_sync_enabled} AB prop. The web client only
 * supports SET operations and extracts
 * {@code customPaymentMethodsAction.customPaymentMethods} (a list), then fires a
 * {@code setCustomPaymentMethods} frontend event. Since this is an SMB-specific
 * frontend operation with no equivalent in this client's data model, the mutation
 * is acknowledged but not applied locally.
 *
 * <p>Index format: ["custom_payment_methods"]
 */
public final class CustomPaymentMethodsHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code CustomPaymentMethodsHandler}.
     */
    public static final CustomPaymentMethodsHandler INSTANCE = new CustomPaymentMethodsHandler();

    private CustomPaymentMethodsHandler() {

    }

    @Override
    public String actionName() {
        return CustomPaymentMethodsAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return CustomPaymentMethodsAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return CustomPaymentMethodsAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return true;
    }
}
