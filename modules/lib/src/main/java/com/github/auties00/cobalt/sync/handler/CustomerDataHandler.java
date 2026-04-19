package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
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
@WhatsAppWebModule(moduleName = "WAWebCustomerDataSync")
public final class CustomerDataHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code CustomerDataHandler}.
     *
     * @implNote WAWebCustomerDataSync — module-level singleton: {@code var p = new m; l.default = p}
     */
    @WhatsAppWebExport(moduleName = "WAWebCustomerDataSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final CustomerDataHandler INSTANCE = new CustomerDataHandler();

    /**
     * Creates a new {@code CustomerDataHandler}.
     *
     * @implNote WAWebCustomerDataSync — private constructor for singleton pattern
     */
    @WhatsAppWebExport(moduleName = "WAWebCustomerDataSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private CustomerDataHandler() {
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebCustomerDataSync.getAction — returns {@code WASyncdConst.Actions.CustomerData}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCustomerDataSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return CustomerDataAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebCustomerDataSync constructor — {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCustomerDataSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return CustomerDataAction.COLLECTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebCustomerDataSync.getVersion — returns {@code 1}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCustomerDataSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
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
     * @implNote ADAPTED: WAWebCustomerDataSync.applyMutations — WA Web returns
     *           {@code WASyncdConst.SyncActionState} values directly; Cobalt wraps
     *           them in {@link MutationApplicationResult} for type safety
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was acknowledged
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCustomerDataSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies a customer data mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebCustomerDataSync.applyMutations}, each mutation
     * is processed as follows:
     * <ul>
     *   <li><b>SET:</b> validates chatJid from index, validates the customerDataAction
     *       field on the value, then stores the customer data record via
     *       {@code $CustomerDataSync$p_1}.</li>
     *   <li><b>REMOVE:</b> if chatJid is present and valid, removes the customer data
     *       record via {@code $CustomerDataSync$p_2}; otherwise silently succeeds.</li>
     *   <li><b>Unknown:</b> returns unsupported.</li>
     * </ul>
     *
     * @implNote WAWebCustomerDataSync.applyMutations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCustomerDataSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index()); // WAWebCustomerDataSync.applyMutations — var n = t.indexParts
        var chatJidString = indexArray.size() >= 2 ? indexArray.getString(1) : null; // WAWebCustomerDataSync.applyMutations — u = n[1]

        if (mutation.operation() == SyncdOperation.SET) { // WAWebCustomerDataSync.applyMutations — t.operation === "set"
            if (chatJidString == null || chatJidString.isBlank()) { // WAWebCustomerDataSync.applyMutations — if (!u)
                return malformedActionValue(); // WAWebCustomerDataSync.applyMutations — return a++, malformedActionValue(r.collectionName)
            }

            var chatJid = Jid.of(chatJidString); // ADAPTED: WAWebCustomerDataSync.applyMutations — validateChatJid(u); Cobalt uses Jid.of which is more lenient than validateChatJid
            if (chatJid == null) { // WAWebCustomerDataSync.applyMutations — if (c == null)
                return malformedActionValue(); // WAWebCustomerDataSync.applyMutations — return a++, malformedActionValue(r.collectionName)
            }

            if (mutation.value() == null) { // WAWebCustomerDataSync.applyMutations — if (s)
                return MutationApplicationResult.success(); // WAWebCustomerDataSync.applyMutations — value is falsy, fall through to success
            }

            if (!(mutation.value().action().orElse(null) instanceof CustomerDataAction)) { // WAWebCustomerDataSync.applyMutations — var d = s.customerDataAction; if (d == null)
                return malformedActionValue(); // WAWebCustomerDataSync.applyMutations — return i++, malformedActionValue(r.collectionName)
            }

            // ADAPTED: WAWebCustomerDataSync.$CustomerDataSync$p_1 — addOrEditCustomerData + frontendFireAndForget
            // Cobalt does not have a dedicated customer data store; the action is acknowledged
            return MutationApplicationResult.success(); // WAWebCustomerDataSync.applyMutations — return {actionState: Success}
        } else if (mutation.operation() == SyncdOperation.REMOVE) { // WAWebCustomerDataSync.applyMutations — t.operation === "remove"
            if (chatJidString != null && !chatJidString.isBlank()) { // WAWebCustomerDataSync.applyMutations — if (u)
                // WAWebCustomerDataSync.applyMutations — var p = validateChatJid(u); p != null && $p_2(p)
                // ADAPTED: WAWebCustomerDataSync.$CustomerDataSync$p_2 — removeCustomerDataByChatJid + frontendFireAndForget
                // Cobalt does not have a dedicated customer data store; valid JID removal is acknowledged
            }
            return MutationApplicationResult.success(); // WAWebCustomerDataSync.applyMutations — return {actionState: Success}
        } else {
            return MutationApplicationResult.unsupported(); // WAWebCustomerDataSync.applyMutations — return {actionState: Unsupported}
        }
    }
}
