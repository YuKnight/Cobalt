package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.DetectedOutcomesStatusAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

/**
 * Handles detected outcomes status actions.
 *
 * <p>This handler processes mutations that update the CTWA detected outcome
 * onboarding status. On SET, reads the {@code isEnabled} flag from the
 * mutation value and stores it. Other operations are acknowledged as
 * unsupported.
 *
 * <p>Index format: {@code ["detected_outcomes_status_action"]}
 *
 * @implNote WAWebDetectedOutcomesStatusSync — singleton handler extending
 *           {@code AccountSyncdActionBase} with {@code collectionName = Regular},
 *           {@code getVersion() = 1}, {@code getAction() = "detected_outcomes_status_action"}
 */
@WhatsAppWebModule(moduleName = "WAWebDetectedOutcomesStatusSync")
public final class DetectedOutcomesStatusHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code DetectedOutcomesStatusHandler}.
     *
     * @implNote WAWebDetectedOutcomesStatusSync — module-level singleton:
     *           {@code var m = new d; l.default = m}
     */
    @WhatsAppWebExport(moduleName = "WAWebDetectedOutcomesStatusSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final DetectedOutcomesStatusHandler INSTANCE = new DetectedOutcomesStatusHandler();

    /**
     * Constructs a new {@code DetectedOutcomesStatusHandler}.
     *
     * @implNote WAWebDetectedOutcomesStatusSync — private constructor mirrors
     *           the module-level singleton instantiation pattern; in WA Web the
     *           constructor sets {@code this.collectionName = WASyncdConst.CollectionName.Regular}.
     */
    @WhatsAppWebExport(moduleName = "WAWebDetectedOutcomesStatusSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
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
    @WhatsAppWebExport(moduleName = "WAWebDetectedOutcomesStatusSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
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
    @WhatsAppWebExport(moduleName = "WAWebDetectedOutcomesStatusSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return DetectedOutcomesStatusAction.COLLECTION_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDetectedOutcomesStatusSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
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
    @WhatsAppWebExport(moduleName = "WAWebDetectedOutcomesStatusSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
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
     * <p>WA Web wraps each mutation in a try/catch that returns
     * {@code SyncActionState.Failed} on error and logs via {@code WALogger}.
     * Per Cobalt's error model, exceptions propagate instead of being caught
     * inline, and WAM/logger-style batch tallies ({@code a++} malformed and
     * {@code i++} unsupported counters with post-batch {@code WALogger.WARN})
     * are intentionally omitted.
     *
     * @implNote WAWebDetectedOutcomesStatusSync.applyMutations — per-mutation
     *           logic within the {@code Promise.all(r.map(...))} callback
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDetectedOutcomesStatusSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof DetectedOutcomesStatusAction action)) {
            return MutationApplicationResult.malformed();
        }

        // ADAPTED: WAWebDetectedOutcomesStatusSync.applyMutations: (l?.isEnabled) == null -> malformedActionValue
        // Cobalt's DetectedOutcomesStatusAction.isEnabled() accessor coalesces null to false per the
        // nullable boolean accessor convention, so a null protobuf field is treated as false rather
        // than malformed. This matches Cobalt's broader policy for Boolean-backed action fields.
        client.store().setDetectedOutcomesEnabled(action.isEnabled()); // ADAPTED: WAWebDetectedOutcomesStatusSync.applyMutations: frontendSendAndReceive("ctwaDetectedOutcomeOnboardingStatusUpdate", {onboardingStatus: l.isEnabled}) — Cobalt stores locally instead of sending to frontend
        return MutationApplicationResult.success();
    }
}
