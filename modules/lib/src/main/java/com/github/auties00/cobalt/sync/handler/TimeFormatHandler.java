package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.TimeFormatAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Applies {@code time_format} mutations decoded from app state sync.
 *
 * <p>Handles the {@link TimeFormatAction} sync action in the
 * {@link SyncPatchType#REGULAR_LOW} collection. A mutation of this type
 * carries the user's preferred 12/24 hour time format as chosen on the
 * paired companion device, and instructs every other linked client to
 * update its own time format preference to match.
 *
 * <p>On WhatsApp Web the handler forwards the new value to the frontend
 * via {@code WAWebBackendApi.frontendFireAndForget("setIs24Hour", {is24Hour: a})}.
 * Cobalt does not ship a UI layer, so instead it persists the new value
 * directly into {@link com.github.auties00.cobalt.store.WhatsAppStore} via
 * {@link com.github.auties00.cobalt.store.WhatsAppStore#setTwentyFourHourFormat(boolean)}.
 *
 * @implNote WAWebTimeFormatSync.default — concrete subclass of
 *           {@code WAWebSyncdAction.AccountSyncdActionBase} with
 *           {@code collectionName = RegularLow}, {@code getVersion() = 7},
 *           {@code getAction() = Actions.TimeFormat} and
 *           {@code applyMutations()} implementing the per-mutation time
 *           format apply.
 */
public final class TimeFormatHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of this handler.
     *
     * <p>WA Web instantiates the handler exactly once at module evaluation
     * time via {@code var u = new s; l.default = u;}. Cobalt mirrors that by
     * exposing a module-level constant.
     *
     * @implNote WAWebTimeFormatSync — {@code var u = new s; l.default = u}
     */
    public static final TimeFormatHandler INSTANCE = new TimeFormatHandler();

    /**
     * Creates a new {@code TimeFormatHandler}.
     *
     * <p>The constructor is private because callers should always go through
     * {@link #INSTANCE}, matching the WA Web module-level singleton.
     *
     * @implNote WAWebTimeFormatSync — hidden {@code function r()} constructor
     *           that only initializes {@code this.collectionName = RegularLow}
     */
    private TimeFormatHandler() {

    }

    /**
     * Returns the action name this handler processes.
     *
     * @implNote WAWebTimeFormatSync.getAction — returns
     *           {@code WASyncdConst.Actions.TimeFormat}, which resolves to
     *           the string {@code "time_format"}
     * @return the constant {@link TimeFormatAction#ACTION_NAME}
     */
    @Override
    public String actionName() {
        return TimeFormatAction.ACTION_NAME; // WAWebTimeFormatSync.getAction -> Actions.TimeFormat
    }

    /**
     * Returns the sync collection this handler's action belongs to.
     *
     * <p>On WA Web this is set on the prototype inside the constructor as
     * {@code this.collectionName = CollectionName.RegularLow}.
     *
     * @implNote WAWebTimeFormatSync — {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     * @return the constant {@link TimeFormatAction#COLLECTION_NAME}, always
     *         {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return TimeFormatAction.COLLECTION_NAME; // WAWebTimeFormatSync -> CollectionName.RegularLow
    }

    /**
     * Returns the mutation format version this handler supports.
     *
     * @implNote WAWebTimeFormatSync.getVersion — {@code return 7}
     * @return the constant {@link TimeFormatAction#ACTION_VERSION}, always {@code 7}
     */
    @Override
    public int version() {
        return TimeFormatAction.ACTION_VERSION; // WAWebTimeFormatSync.getVersion -> 7
    }

    /**
     * Applies a single decoded time format mutation.
     *
     * <p>Thin bridge over {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * that reduces the richer {@link MutationApplicationResult} state to a
     * legacy boolean: {@code true} only for {@link SyncActionState#SUCCESS},
     * {@code false} for {@code MALFORMED}, {@code UNSUPPORTED}, {@code SKIPPED}
     * and {@code FAILED}.
     *
     * @implNote ADAPTED: WAWebTimeFormatSync.applyMutations — the WA Web
     *           inner async callback returns a {@code SyncActionState}; Cobalt
     *           exposes both a boolean and a richer result through two methods
     * @param client   the WhatsApp client the mutation is being applied to
     * @param mutation the trusted, decoded mutation to apply
     * @return {@code true} if the apply succeeded, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebTimeFormatSync.applyMutations
    }

    /**
     * Applies a single decoded time format mutation and returns the detailed result.
     *
     * <p>This method implements the body of the WA Web per-mutation callback
     * passed to {@code a.map(function(e) { ... })} inside
     * {@code applyMutations(t)}. The order of checks mirrors WA Web exactly:
     * <ol>
     *   <li><b>Operation filter</b> — WA Web: {@code if (e.operation !== "set")
     *       return r++, {actionState: Unsupported}}. Cobalt returns
     *       {@link MutationApplicationResult#unsupported()}.</li>
     *   <li><b>Missing timeFormatAction or null field</b> — WA Web reads
     *       {@code var a = (t = e.value.timeFormatAction) == null ? void 0 :
     *       t.isTwentyFourHourFormatEnabled; return a == null ?
     *       n.malformedActionIndex() : ...}. Cobalt checks that the decoded
     *       action is a {@link TimeFormatAction} and returns
     *       {@link MutationApplicationResult#malformed()} otherwise. The
     *       sub-case where {@code timeFormatAction} exists but
     *       {@code isTwentyFourHourFormatEnabled} is {@code null} is folded
     *       into the {@code false} branch via the existing nullable boolean
     *       accessor on {@link TimeFormatAction#isTwentyFourHourFormatEnabled()},
     *       per Cobalt's nullable boolean coalescing convention.</li>
     *   <li><b>Apply the new time format</b> — WA Web fires
     *       {@code WAWebBackendApi.frontendFireAndForget("setIs24Hour",
     *       {is24Hour: a})} to push the value into the frontend. Cobalt has
     *       no frontend, so it persists the value into
     *       {@link com.github.auties00.cobalt.store.WhatsAppStore#setTwentyFourHourFormat(boolean)}.</li>
     *   <li><b>Success</b> — returns {@link MutationApplicationResult#success()},
     *       matching WA Web's {@code {actionState: Success}}.</li>
     * </ol>
     *
     * <p>WA Web also tracks an {@code r} counter of unsupported operations
     * and emits a single {@code WALogger.WARN("time format sync: %s operations
     * not supported", r)} after the batch completes. That telemetry is
     * intentionally omitted in Cobalt as logging noise with no behavioral
     * impact.
     *
     * <p>WA Web does NOT consult any AB prop before applying the mutation:
     * even though {@code md_syncd_24_hour_time_format_sync_enabled} is
     * registered in {@code WAWebABPropsConfigs}, it is never read by
     * {@code WAWebTimeFormatSync} or any other module. Cobalt therefore
     * applies every well-formed {@code SET} mutation unconditionally.
     *
     * @implNote WAWebTimeFormatSync.applyMutations — per-mutation body
     * @param client   the WhatsApp client the mutation is being applied to
     * @param mutation the trusted, decoded mutation to apply
     * @return {@link MutationApplicationResult#unsupported()} for non-{@code SET}
     *         operations; {@link MutationApplicationResult#malformed()} if the
     *         decoded action is not a {@link TimeFormatAction};
     *         {@link MutationApplicationResult#success()} otherwise
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // WAWebTimeFormatSync.applyMutations: if (e.operation !== "set") return r++, {actionState: Unsupported}
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        // WAWebTimeFormatSync.applyMutations: var a = (t = e.value.timeFormatAction) == null ? void 0 : t.isTwentyFourHourFormatEnabled; return a == null ? n.malformedActionIndex() : ...
        if (!(mutation.value().action().orElse(null) instanceof TimeFormatAction action)) {
            return malformedActionIndex();
        }

        // ADAPTED: WAWebTimeFormatSync.applyMutations:
        //   WAWebBackendApi.frontendFireAndForget("setIs24Hour", {is24Hour: a})
        // Cobalt has no frontend, so the value is persisted into the store
        // instead. The nullable Boolean field is coalesced to false via the
        // existing accessor on TimeFormatAction.
        client.store().setTwentyFourHourFormat(action.isTwentyFourHourFormatEnabled()); // ADAPTED: WAWebTimeFormatSync.applyMutations -> frontendFireAndForget("setIs24Hour", ...)

        // NO_WA_BASIS: the WA Web "r" unsupported counter and the trailing
        // WALogger.WARN("time format sync: %s operations not supported", r)
        // are intentionally dropped as telemetry-only logging.

        // WAWebTimeFormatSync.applyMutations: return {actionState: Success}
        return MutationApplicationResult.success();
    }
}
