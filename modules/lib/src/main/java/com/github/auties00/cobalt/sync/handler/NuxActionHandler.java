package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.device.NuxAction;
import com.github.auties00.cobalt.model.sync.action.device.NuxActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Handles NUX (New User Experience) sync mutations.
 *
 * <p>This handler processes mutations that track completion of onboarding
 * steps and new feature introductions. Per WhatsApp Web
 * {@code WAWebNuxSync}, the handler belongs to the {@code RegularLow}
 * collection, uses version {@code 7}, and routes on action name
 * {@code "nux"}.
 *
 * <p>Index format: {@code ["nux", nuxKey]}
 *
 * <p>On {@code SET}, the handler validates that {@code indexParts[1]} (the
 * {@code nuxKey}) is a string and extracts the {@code acknowledged} flag
 * from the nested {@code nuxAction} value. If {@code nuxAction} is absent,
 * {@code acknowledged} defaults to {@code false}. The resolved state is
 * written to the local NUX store.
 *
 * <p>All non-{@code SET} operations are classified as {@code UNSUPPORTED}.
 *
 * @implNote WAWebNuxSync.default — singleton instance of the NUX sync
 *           handler extending {@code WAWebSyncdAction.AccountSyncdActionBase}
 *           ({@code var d = (function(t){...})(o("WAWebSyncdAction").AccountSyncdActionBase),
 *           m = new d; l.default = m})
 */
@WhatsAppWebModule(moduleName = "WAWebNuxSync")
public final class NuxActionHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code NuxActionHandler}.
     *
     * @implNote WAWebNuxSync.default — {@code var m = new d; l.default = m}
     */
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final NuxActionHandler INSTANCE = new NuxActionHandler();

    /**
     * Creates the singleton NUX sync handler.
     *
     * @implNote WAWebNuxSync — constructor of class {@code d} extending
     *           {@code AccountSyncdActionBase} that assigns
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     */
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private NuxActionHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebNuxSync.getAction — returns
     *           {@code WASyncdConst.Actions.Nux} (value: {@code "nux"})
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return NuxAction.ACTION_NAME; // WAWebNuxSync.getAction -> WASyncdConst.Actions.Nux = "nux"
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebNuxSync — {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     *           (value: {@code "regular_low"})
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return NuxAction.COLLECTION_NAME; // WAWebNuxSync.collectionName = WASyncdConst.CollectionName.RegularLow = "regular_low"
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebNuxSync.getVersion — returns {@code 7}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return NuxAction.ACTION_VERSION; // WAWebNuxSync.getVersion -> 7
    }

    /**
     * Applies a NUX mutation and returns whether it succeeded.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and checks if the result state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebNuxSync.applyMutations — WA Web returns
     *           {@code WASyncdConst.SyncActionState.Success} directly; Cobalt
     *           wraps the outcome in {@link MutationApplicationResult} and
     *           extracts the boolean here
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully,
     *         {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS; // WAWebNuxSync.applyMutations -> SyncActionState.Success
    }

    /**
     * Applies a NUX mutation and returns the detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebNuxSync.applyMutations}, the handler
     * iterates the batch and, for each mutation:
     * <ol>
     *   <li>If {@code operation !== "set"}, records {@code Unsupported} and
     *       moves on (Cobalt returns {@link MutationApplicationResult#unsupported()})</li>
     *   <li>Reads {@code indexParts[1]} as the {@code nuxKey}; if it is not
     *       a string, records {@code malformedActionIndex()} and moves on</li>
     *   <li>Collects {@code {nuxKey, acknowledged: value.nuxAction?.acknowledged === true,
     *       timestamp: Number(value.timestamp)}} into a list</li>
     * </ol>
     *
     * <p>After the loop, WA Web logs the unsupported/malformed counts via
     * {@code WALogger.WARN} and, if the collected list is non-empty, calls
     * {@code WAWebUserPrefsNuxPreferences.updateNuxSyncList(list)} which
     * merges each entry into the {@code NUX_LIST} set (acknowledged keys)
     * and the {@code NUX_DATA} map (full {@code {acknowledged, timestamp}}
     * record).
     *
     * <p>In Cobalt, the store is simplified to a single
     * {@code Map<String, Boolean>} indexed by {@code nuxKey}, so the
     * timestamp from {@code NUX_DATA} is dropped; the {@code acknowledged}
     * flag is still written for both {@code true} and {@code false} values
     * (matching the {@code NUX_DATA} merge semantics). WAM logging is
     * intentionally omitted. Unlike WA Web, which iterates the whole batch
     * inside the handler, Cobalt processes mutations one-by-one through the
     * shared {@link WebAppStateActionHandler} interface; the cumulative
     * behavior is equivalent because the NUX handler has no batch-level
     * deduplication.
     *
     * <p>Note that, unlike the previous Cobalt implementation, there is no
     * {@code ABProp.NUX_SYNC} gate: {@code WAWebNuxSync} does not check any
     * AB prop, and {@code WAWebCollectionHandlerActions} registers the NUX
     * handler unconditionally.
     *
     * @implNote WAWebNuxSync.applyMutations — per-mutation logic inside the
     *           {@code t.map(function(e){...})} body;
     *           WAWebUserPrefsNuxPreferences.updateNuxSyncList — persists
     *           the collected list to the NUX_LIST set and NUX_DATA map
     * @param client   the WhatsApp client
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebNuxSync.applyMutations: if (e.operation !== "set") return {actionState: Unsupported}
            return MutationApplicationResult.unsupported(); // WAWebNuxSync.applyMutations: {actionState: SyncActionState.Unsupported}
        }

        var indexArray = JSON.parseArray(mutation.index()); // WAWebNuxSync.applyMutations: e.indexParts
        var nuxKey = indexArray.getString(1); // WAWebNuxSync.applyMutations: var s = e.indexParts[1]
        if (nuxKey == null) { // WAWebNuxSync.applyMutations: WATypeUtils.isString(s) — null/non-string branch
            return malformedActionIndex(); // WAWebNuxSync.applyMutations: n.malformedActionIndex()
        }

        // WAWebNuxSync.applyMutations: ((t = e.value.nuxAction) == null ? void 0 : t.acknowledged) === true
        // If nuxAction is absent, `acknowledged` defaults to false (WA Web does NOT
        // return malformed in this branch — it still records the nux key with
        // acknowledged=false and returns Success).
        var nuxAction = mutation.value().action().orElse(null);
        var acknowledged = nuxAction instanceof NuxAction action && action.acknowledged(); // WAWebNuxSync.applyMutations: t.acknowledged === true — NuxAction.acknowledged() already coalesces null to false

        // WAWebUserPrefsNuxPreferences.updateNuxSyncList: merges {nuxKey, acknowledged}
        // into the NUX_LIST set (add on true, remove on false) and the NUX_DATA map
        // ({acknowledged, timestamp}). Cobalt drops the timestamp and stores just the
        // boolean.
        var states = new HashMap<>(client.store().nuxStates()); // ADAPTED: WAWebUserPrefsNuxPreferences.updateNuxSyncList — Cobalt uses ConcurrentHashMap store instead of two separate UserPrefs keys
        states.put(nuxKey, acknowledged); // WAWebUserPrefsNuxPreferences.updateNuxSyncList: t.add(o) / t.delete(o) + n[o] = {acknowledged, timestamp}
        client.store().setNuxStates(states); // WAWebUserPrefsNuxPreferences.updateNuxSyncList: WAWebUserPrefsStore.set(KEYS.NUX_LIST, ...) + WAWebUserPrefsStore.set(KEYS.NUX_DATA, ...)

        return MutationApplicationResult.success(); // WAWebNuxSync.applyMutations: {actionState: SyncActionState.Success}
    }

    /**
     * Builds a pending mutation for acknowledging or unacknowledging a NUX
     * item.
     *
     * <p>Per WhatsApp Web {@code WAWebNuxSync.$NuxSync$p_1}:
     * <ol>
     *   <li>Wraps the {@code acknowledged} flag in a
     *       {@code {nuxAction: {acknowledged}}} value</li>
     *   <li>Calls {@code WAWebSyncdActionUtils.buildPendingMutation} with
     *       {@code collection}, {@code indexArgs = [nuxKey]}, value,
     *       version, operation {@code SET}, and the supplied timestamp</li>
     * </ol>
     *
     * @implNote WAWebNuxSync.$NuxSync$p_1 — {@code a.$NuxSync$p_1 =
     *           function(t, n, r){var e = {nuxAction: {acknowledged: r}};
     *           return WAWebSyncdActionUtils.buildPendingMutation({...})}}
     * @param nuxKey       the NUX identifier (the {@code indexArgs[0]} entry)
     * @param timestamp    the mutation timestamp
     * @param acknowledged whether the NUX item is acknowledged
     * @return the pending mutation for the NUX action
     */
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "$NuxSync$p_1", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getNuxMutation(String nuxKey, Instant timestamp, boolean acknowledged) {
        Objects.requireNonNull(nuxKey, "nuxKey cannot be null"); // ADAPTED: defensive null check not present in WA Web
        Objects.requireNonNull(timestamp, "timestamp cannot be null"); // ADAPTED: defensive null check not present in WA Web
        var action = new NuxActionBuilder() // WAWebNuxSync.$NuxSync$p_1: var e = {nuxAction: {acknowledged: r}}
                .acknowledged(acknowledged) // WAWebNuxSync.$NuxSync$p_1: acknowledged: r
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: e
                .nuxAction(action) // WAWebNuxSync.$NuxSync$p_1: {nuxAction: ...}
                .build();
        var index = JSON.toJSONString(List.of(actionName(), nuxKey)); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs)) where indexArgs = [t]
        var pendingMutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, binarySyncAction, version, operation, timestamp, action }
                index,
                value,
                SyncdOperation.SET, // WAWebNuxSync.$NuxSync$p_1: operation: SyncdMutation$SyncdOperation.SET
                timestamp,
                version() // WAWebNuxSync.$NuxSync$p_1: version: this.getVersion()
        );
        return new SyncPendingMutation(pendingMutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }

    /**
     * Acknowledges a NUX item in the local store and builds the pending
     * mutation to push the change to the server.
     *
     * <p>Per WhatsApp Web {@code WAWebNuxSync.acknowledgeNux}, the method
     * is a thin wrapper over {@code $NuxSync$p_2(nuxKey, true)}. The
     * underlying {@code $NuxSync$p_2} implementation:
     * <ol>
     *   <li>Reads the current millisecond timestamp via
     *       {@code WATimeUtils.unixTimeMs()}</li>
     *   <li>Calls {@code WAWebUserPrefsNuxPreferences.updateNuxSyncList}
     *       with the new entry to update the local NUX store optimistically</li>
     *   <li>Builds a pending mutation via {@code $NuxSync$p_1}</li>
     *   <li>Submits it through {@code WAWebSyncdCoreApi.lockForSync([], [mutation],
     *       () =&gt; Promise.resolve())}</li>
     *   <li>Returns the timestamp</li>
     * </ol>
     *
     * <p>In Cobalt, the sync submission is left to the caller: this method
     * applies the local store update and returns the pending mutation so the
     * caller can enqueue it via the app-state sync pipeline.
     *
     * @implNote WAWebNuxSync.acknowledgeNux — {@code a.acknowledgeNux =
     *           function(e){return this.$NuxSync$p_2(e, true)}};
     *           WAWebNuxSync.$NuxSync$p_2 — {@code var r = WATimeUtils.unixTimeMs();
     *           WAWebUserPrefsNuxPreferences.updateNuxSyncList([...]);
     *           var a = this.$NuxSync$p_1(e, r, t);
     *           yield WAWebSyncdCoreApi.lockForSync([], [a], () =&gt; Promise.resolve());
     *           return r}
     * @param client the WhatsApp client owning the local NUX store
     * @param nuxKey the NUX identifier to acknowledge
     * @return the pending mutation carrying the {@code acknowledged=true} update
     */
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "acknowledgeNux", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation acknowledgeNux(WhatsAppClient client, String nuxKey) {
        return updateNuxState(client, nuxKey, true); // WAWebNuxSync.acknowledgeNux: return this.$NuxSync$p_2(e, true)
    }

    /**
     * Unacknowledges a NUX item in the local store and builds the pending
     * mutation to push the change to the server.
     *
     * <p>Per WhatsApp Web {@code WAWebNuxSync.unAcknowledgeNux}, the method
     * is a thin wrapper over {@code $NuxSync$p_2(nuxKey, false)}. See
     * {@link #acknowledgeNux(WhatsAppClient, String)} for the full
     * description of {@code $NuxSync$p_2}.
     *
     * @implNote WAWebNuxSync.unAcknowledgeNux — {@code a.unAcknowledgeNux =
     *           function(e){return this.$NuxSync$p_2(e, false)}}
     * @param client the WhatsApp client owning the local NUX store
     * @param nuxKey the NUX identifier to unacknowledge
     * @return the pending mutation carrying the {@code acknowledged=false} update
     */
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "unAcknowledgeNux", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation unAcknowledgeNux(WhatsAppClient client, String nuxKey) {
        return updateNuxState(client, nuxKey, false); // WAWebNuxSync.unAcknowledgeNux: return this.$NuxSync$p_2(e, false)
    }

    /**
     * Shared implementation of {@link #acknowledgeNux(WhatsAppClient, String)}
     * and {@link #unAcknowledgeNux(WhatsAppClient, String)}.
     *
     * <p>Mirrors WA Web's {@code $NuxSync$p_2} private helper: stamps the
     * current time, updates the local NUX store optimistically, and
     * returns a freshly built pending mutation.
     *
     * @implNote WAWebNuxSync.$NuxSync$p_2 — private helper shared by
     *           {@code acknowledgeNux} and {@code unAcknowledgeNux}
     * @param client       the WhatsApp client owning the local NUX store
     * @param nuxKey       the NUX identifier
     * @param acknowledged whether the NUX item should be marked as acknowledged
     * @return the pending mutation carrying the requested update
     */
    @WhatsAppWebExport(moduleName = "WAWebNuxSync", exports = "$NuxSync$p_2", adaptation = WhatsAppAdaptation.ADAPTED)
    private SyncPendingMutation updateNuxState(WhatsAppClient client, String nuxKey, boolean acknowledged) {
        Objects.requireNonNull(client, "client cannot be null"); // ADAPTED: defensive null check not present in WA Web
        Objects.requireNonNull(nuxKey, "nuxKey cannot be null"); // ADAPTED: defensive null check not present in WA Web
        var timestamp = Instant.now(); // WAWebNuxSync.$NuxSync$p_2: var r = WATimeUtils.unixTimeMs()
        // WAWebNuxSync.$NuxSync$p_2: WAWebUserPrefsNuxPreferences.updateNuxSyncList([{nuxKey: e, acknowledged: t, timestamp: r}])
        var states = new HashMap<>(client.store().nuxStates()); // ADAPTED: WAWebUserPrefsNuxPreferences.updateNuxSyncList — Cobalt uses ConcurrentHashMap store instead of two separate UserPrefs keys
        states.put(nuxKey, acknowledged);
        client.store().setNuxStates(states);
        return getNuxMutation(nuxKey, timestamp, acknowledged); // WAWebNuxSync.$NuxSync$p_2: var a = this.$NuxSync$p_1(e, r, t)
        // WAWebNuxSync.$NuxSync$p_2: yield WAWebSyncdCoreApi.lockForSync([], [a], () => Promise.resolve())
        // ADAPTED: sync submission is delegated to the caller in Cobalt
    }
}
