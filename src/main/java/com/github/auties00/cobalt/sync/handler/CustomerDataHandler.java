package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.CustomerDataAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles customer data (CRM) actions.
 *
 * <p>Per WhatsApp Web {@code WAWebCustomerDataSync}, this handler processes
 * mutations for business customer relationship data associated with chat JIDs.
 *
 * <p>Index format: {@code ["customer_data", chatJid]}
 *
 * @implNote WAWebCustomerDataSync
 */
public final class CustomerDataHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code CustomerDataHandler}.
     */
    public static final CustomerDataHandler INSTANCE = new CustomerDataHandler();

    private CustomerDataHandler() {
    }

    @Override
    public String actionName() {
        return CustomerDataAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return CustomerDataAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return CustomerDataAction.ACTION_VERSION;
    }

    /**
     * Applies a customer data mutation.
     *
     * <p>Per WhatsApp Web {@code WAWebCustomerDataSync.applyMutations}:
     * on SET, the customer data action is extracted and stored for the chat JID.
     * On REMOVE, the customer data for the chat JID is removed.
     *
     * @implNote WAWebCustomerDataSync.applyMutations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was acknowledged
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index()); // WAWebCustomerDataSync.applyMutations — indexParts
        if (indexArray.size() < 2) {
            return MutationApplicationResult.malformed(); // WAWebCustomerDataSync.applyMutations — missing chatJid
        }

        var chatJidString = indexArray.getString(1); // WAWebCustomerDataSync.applyMutations — indexParts[1]
        if (chatJidString == null || chatJidString.isBlank()) {
            return MutationApplicationResult.malformed(); // WAWebCustomerDataSync.applyMutations — malformedActionValue
        }

        var chatJid = Jid.of(chatJidString); // WAWebCustomerDataSync.applyMutations — validateChatJid(u)

        if (mutation.operation() == SyncdOperation.SET) { // WAWebCustomerDataSync.applyMutations — operation === "set"
            if (mutation.value() == null) {
                return MutationApplicationResult.success(); // WAWebCustomerDataSync.applyMutations — if (!s) return success
            }

            if (!(mutation.value().action().orElse(null) instanceof CustomerDataAction)) {
                return MutationApplicationResult.malformed(); // WAWebCustomerDataSync.applyMutations — customerDataAction == null
            }

            // ADAPTED: WAWebCustomerDataSync.$CustomerDataSync$p_1 — addOrEditCustomerData
            // Cobalt does not have a dedicated customer data store; the action is acknowledged
            return MutationApplicationResult.success();
        } else if (mutation.operation() == SyncdOperation.REMOVE) { // WAWebCustomerDataSync.applyMutations — operation === "remove"
            // ADAPTED: WAWebCustomerDataSync.$CustomerDataSync$p_2 — removeCustomerDataByChatJid
            // Cobalt does not have a dedicated customer data store; the action is acknowledged
            return MutationApplicationResult.success();
        } else {
            return MutationApplicationResult.unsupported(); // WAWebCustomerDataSync.applyMutations — unsupported operation
        }
    }
}
