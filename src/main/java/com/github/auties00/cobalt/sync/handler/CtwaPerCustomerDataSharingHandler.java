package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.CtwaPerCustomerDataSharingAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles CTWA per-customer data sharing actions.
 *
 * <p>Index format: ["ctwaPerCustomerDataSharing", "accountLid"]
 */
public final class CtwaPerCustomerDataSharingHandler implements WebAppStateActionHandler {
    public static final CtwaPerCustomerDataSharingHandler INSTANCE = new CtwaPerCustomerDataSharingHandler();

    private CtwaPerCustomerDataSharingHandler() {

    }

    @Override
    public String actionName() {
        return CtwaPerCustomerDataSharingAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return CtwaPerCustomerDataSharingAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return CtwaPerCustomerDataSharingAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web source (WAWebCtwaPerCustomerDataSharingSync): supports both SET and REMOVE.
        // On SET: reads indexParts[1] as accountLid (must be non-null),
        // reads ctwaPerCustomerDataSharingAction.isCtwaPerCustomerDataSharingEnabled (must be non-null),
        // stores the mapping (lidRawString -> dataSharing3pdEnabled) in IndexedDB,
        // and fires a frontend event to maybe generate a system message.
        // On REMOVE: reads indexParts[1] as accountLid, removes the entry from IndexedDB.
        // Other operations are unsupported.
        // No equivalent per-customer data sharing storage exists in the Java data model.
        var indexArray = JSON.parseArray(mutation.index());
        var accountLid = indexArray.getString(1);

        switch (mutation.operation()) {
            case SET -> {
                if (accountLid == null) {
                    return false;
                }

                if (!(mutation.value().action().orElse(null) instanceof CtwaPerCustomerDataSharingAction)) {
                    return false;
                }
            }
            case REMOVE -> {
                // Web removes the entry from IndexedDB; nothing to do here
            }
        }

        return true;
    }
}
