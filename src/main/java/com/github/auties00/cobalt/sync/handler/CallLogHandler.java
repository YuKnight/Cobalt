package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.call.CallLogAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles call log actions.
 *
 * <p>Index format: ["call_log", ...]
 */
public final class CallLogHandler implements WebAppStateActionHandler {
    public static final CallLogHandler INSTANCE = new CallLogHandler();

    private CallLogHandler() {

    }

    @Override
    public String actionName() {
        return CallLogAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return CallLogAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return CallLogAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web source (WAWebCallLogSync) on SET:
        // - Extracts callLogAction.callLogRecord from mutation value
        // - Checks pairing timestamp: skips if mutation timestamp <= pairing time
        // - Determines shouldHideInConversation based on whether the mutation
        //   happened more than 1 minute ago
        // - Calls generateCallLogFromCallSyncRecord to create a VoIP call log entry
        // On REMOVE: accepts silently (returns Success)
        // The call log generation is web VoIP layer specific, so this is a no-op.
        return true;
    }
}
