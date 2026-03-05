package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.DetectedOutcomesStatusAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles detected outcomes status actions.
 *
 * <p>Index format: ["detected_outcomes_status_action"]
 */
public final class DetectedOutcomesStatusHandler implements WebAppStateActionHandler {
    public static final DetectedOutcomesStatusHandler INSTANCE = new DetectedOutcomesStatusHandler();

    private DetectedOutcomesStatusHandler() {

    }

    @Override
    public String actionName() {
        return DetectedOutcomesStatusAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return DetectedOutcomesStatusAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return DetectedOutcomesStatusAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web source (WAWebDetectedOutcomesStatusSync): only SET is supported.
        // Reads detectedOutcomesStatusAction.isEnabled (must be non-null).
        // Sends a frontend API call (ctwaDetectedOutcomeOnboardingStatusUpdate)
        // to update the CTWA detected outcome onboarding status in the UI.
        // No equivalent data model operation exists in the Java codebase.
        if (mutation.operation() != SyncdOperation.SET) {
            return false;
        }

        if (!(mutation.value().action().orElse(null) instanceof DetectedOutcomesStatusAction)) {
            return false;
        }

        return true;
    }
}
