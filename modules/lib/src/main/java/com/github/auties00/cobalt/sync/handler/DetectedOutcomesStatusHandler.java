package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.DetectedOutcomesStatusAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles detected outcomes status actions.
 *
 * <p>This handler processes mutations that update the CTWA detected outcome
 * onboarding status. On SET, reads the {@code isEnabled} flag from the
 * mutation value and stores it. Other operations are acknowledged as
 * unsupported.
 *
 * <p>Index format: ["detected_outcomes_status_action"]
 *
 * @implNote WAWebDetectedOutcomesStatusSync — singleton handler extending
 *           {@code AccountSyncdActionBase} with {@code collectionName = Regular},
 *           {@code getVersion() = 1}, {@code getAction() = "detected_outcomes_status_action"}
 */
public final class DetectedOutcomesStatusHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code DetectedOutcomesStatusHandler}.
     *
     * @implNote WAWebDetectedOutcomesStatusSync — module-level singleton:
     *           {@code var m = new d; l.default = m}
     */
    public static final DetectedOutcomesStatusHandler INSTANCE = new DetectedOutcomesStatusHandler();

    /**
     * Constructs a new {@code DetectedOutcomesStatusHandler}.
     *
     * @implNote WAWebDetectedOutcomesStatusSync — private constructor mirrors
     *           the module-level singleton instantiation pattern
     */
    private DetectedOutcomesStatusHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebDetectedOutcomesStatusSync.getAction — returns
     *           {@code WASyncdConst.Actions.DetectedOutcomeStatus}
     *           ({@code "detected_outcomes_status_action"})
     */
    @Override
    public String actionName() {
        return DetectedOutcomesStatusAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebDetectedOutcomesStatusSync — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     */
    @Override
    public SyncPatchType collectionName() {
        return DetectedOutcomesStatusAction.COLLECTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebDetectedOutcomesStatusSync.getVersion — returns {@code 1}
     */
    @Override
    public int version() {
        return DetectedOutcomesStatusAction.ACTION_VERSION;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebDetectedOutcomesStatusSync.applyMutations — delegates to
     *           {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     *           and checks for {@code SUCCESS} state
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Per WhatsApp Web {@code WAWebDetectedOutcomesStatusSync.applyMutations}:
     * for each mutation, if the operation is SET, extracts
     * {@code detectedOutcomesStatusAction} from the mutation value. If
     * {@code isEnabled} is {@code null}, the mutation is malformed. Otherwise,
     * sends the onboarding status to the frontend and returns success. Non-SET
     * operations return unsupported.
     *
     * @implNote WAWebDetectedOutcomesStatusSync.applyMutations — per-mutation
     *           logic within the {@code Promise.all(r.map(...))} callback
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebDetectedOutcomesStatusSync.applyMutations: else branch — i++, return {actionState: Unsupported}
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof DetectedOutcomesStatusAction action)) { // WAWebDetectedOutcomesStatusSync.applyMutations: var l = r.detectedOutcomesStatusAction; (l == null) check
            return MutationApplicationResult.malformed(); // WAWebDetectedOutcomesStatusSync.applyMutations: a++, malformedActionValue(t.collectionName)
        }

        // ADAPTED: WAWebDetectedOutcomesStatusSync.applyMutations: (l?.isEnabled) == null -> malformedActionValue
        // Cobalt coalesces null to false per project convention for nullable Boolean fields
        client.store().setDetectedOutcomesEnabled(action.isEnabled()); // ADAPTED: WAWebDetectedOutcomesStatusSync.applyMutations: frontendSendAndReceive("ctwaDetectedOutcomeOnboardingStatusUpdate", {onboardingStatus: l.isEnabled}) — Cobalt stores locally instead of sending to frontend
        return MutationApplicationResult.success(); // WAWebDetectedOutcomesStatusSync.applyMutations: return {actionState: Success}
    }
}
