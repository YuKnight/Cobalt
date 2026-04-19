package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.PrimaryVersionAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles primary version actions.
 *
 * <p>This handler processes mutations that track the WhatsApp primary client
 * version string for either the {@code "current"} or {@code "session_start"}
 * checkpoint. WhatsApp Web's implementation simply validates each mutation in
 * the batch, increments local counters for unsupported and malformed entries,
 * and emits {@code WALogger.WARN} traces. There is no application side effect
 * beyond the per-mutation result classification, so Cobalt mirrors the same
 * validation pipeline without persisting any value.
 *
 * <p>Index format: {@code ["primaryVersion", "current"|"session_start"]}.
 *
 * @implNote WAWebPrimaryVersionSync — extends {@code AccountSyncdActionBase}
 *           with {@code collectionName = WASyncdConst.CollectionName.RegularLow},
 *           {@code getAction()} returning {@code WASyncdConst.Actions.PrimaryVersion}
 *           ({@code "primary_version"}), and {@code getVersion()} returning
 *           the literal {@code 7}
 */
@WhatsAppWebModule(moduleName = "WAWebPrimaryVersionSync")
public final class PrimaryVersionHandler implements WebAppStateActionHandler {
    /**
     * Sub-index value identifying the {@code "current"} primary version
     * checkpoint inside the index parts array.
     *
     * @implNote WAWebPrimaryVersionSync — module-level constant
     *           {@code u.CURRENT = "current"}
     */
    @WhatsAppWebExport(moduleName = "WAWebPrimaryVersionSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    private static final String INDEX_CURRENT = "current"; // WAWebPrimaryVersionSync: u.CURRENT = "current"

    /**
     * Sub-index value identifying the {@code "session_start"} primary version
     * checkpoint inside the index parts array.
     *
     * @implNote WAWebPrimaryVersionSync — module-level constant
     *           {@code u.SESSION_START = "session_start"}
     */
    @WhatsAppWebExport(moduleName = "WAWebPrimaryVersionSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    private static final String INDEX_SESSION_START = "session_start"; // WAWebPrimaryVersionSync: u.SESSION_START = "session_start"

    /**
     * The singleton instance of {@code PrimaryVersionHandler}.
     *
     * @implNote WAWebPrimaryVersionSync.default — WA Web exports a single
     *           pre-instantiated handler ({@code d = new c; l.default = d})
     */
    @WhatsAppWebExport(moduleName = "WAWebPrimaryVersionSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final PrimaryVersionHandler INSTANCE = new PrimaryVersionHandler();

    /**
     * Constructs the singleton instance.
     *
     * @implNote WAWebPrimaryVersionSync — WA Web instantiates the handler once
     *           via {@code new c()} and exports it as the module default
     */
    @WhatsAppWebExport(moduleName = "WAWebPrimaryVersionSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private PrimaryVersionHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPrimaryVersionSync.getAction — returns
     *           {@code WASyncdConst.Actions.PrimaryVersion} which equals
     *           {@code "primary_version"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPrimaryVersionSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return PrimaryVersionAction.ACTION_NAME; // WAWebPrimaryVersionSync.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPrimaryVersionSync — sets {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     *           in the constructor
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPrimaryVersionSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return PrimaryVersionAction.COLLECTION_NAME; // WAWebPrimaryVersionSync constructor: e.collectionName = RegularLow
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPrimaryVersionSync.getVersion — returns the literal {@code 7}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPrimaryVersionSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return PrimaryVersionAction.ACTION_VERSION; // WAWebPrimaryVersionSync.getVersion: return 7
    }

    /**
     * {@inheritDoc}
     *
     * <p>WhatsApp Web only exposes a batch entry point ({@code applyMutations}).
     * Cobalt's interface contract additionally requires a single-mutation path,
     * which is implemented here by delegating to {@link #applyMutationResult}.
     *
     * @implNote ADAPTED: WAWebPrimaryVersionSync.applyMutations — WA Web only
     *           processes mutations as a batch; the single-mutation entry point
     *           is a Cobalt interface adaptation
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPrimaryVersionSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: single-path adapter for batch-only WA Web entry
    }

    /**
     * {@inheritDoc}
     *
     * <p>Per WhatsApp Web {@code WAWebPrimaryVersionSync.applyMutations}: maps
     * each mutation in the batch to its per-mutation classification. The
     * mapping logic mirrors WA Web exactly:
     * <ul>
     *   <li>Non-{@code SET} operations are acknowledged as {@code UNSUPPORTED}.</li>
     *   <li>If {@code indexParts[1]} is missing/empty or is not one of
     *       {@code "current"} or {@code "session_start"}, the mutation is
     *       classified as {@code MALFORMED} via
     *       {@link #malformedActionIndex()}.</li>
     *   <li>If {@code value.primaryVersionAction.version} is missing, the
     *       mutation is classified as {@code MALFORMED} via
     *       {@link #malformedActionValue()}.</li>
     *   <li>Otherwise, the mutation is classified as {@code SUCCESS}.</li>
     * </ul>
     *
     * <p>WhatsApp Web also tracks {@code WALogger.WARN} counters for the
     * unsupported and malformed-value paths, which Cobalt intentionally
     * omits as logging/telemetry is not replicated.
     *
     * @implNote WAWebPrimaryVersionSync.applyMutations
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPrimaryVersionSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.DIRECT)
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        // WAWebPrimaryVersionSync.applyMutations: var n = this, r = 0, a = 0
        // r and a are local counters for the WALogger WARN telemetry, intentionally omitted in Cobalt
        var results = new ArrayList<MutationApplicationResult>(mutations.size()); // WAWebPrimaryVersionSync.applyMutations: var i = t.map(...)
        for (var mutation : mutations) { // WAWebPrimaryVersionSync.applyMutations: t.map(function(e) {...})
            results.add(applyMutationResult(client, mutation)); // WAWebPrimaryVersionSync.applyMutations: per-mutation classification
        }
        // WAWebPrimaryVersionSync.applyMutations: r > 0 && WALogger.WARN("syncd: primary version sync, %s operations not supported", r) — telemetry skipped
        // WAWebPrimaryVersionSync.applyMutations: a > 0 && WALogger.WARN("syncd: primary version sync, %s malformed mutations", a) — telemetry skipped
        return results; // WAWebPrimaryVersionSync.applyMutations: return i
    }

    /**
     * {@inheritDoc}
     *
     * <p>Single-mutation adapter that mirrors the per-mutation classification
     * inside WhatsApp Web's batch entry point exactly. The validation order
     * follows WA Web:
     * <ol>
     *   <li>If {@code operation !== "set"}, return {@code UNSUPPORTED}.</li>
     *   <li>Parse the index, take {@code indexParts[1]}; if it is missing,
     *       empty, or not one of {@code "current"} or {@code "session_start"},
     *       return {@link #malformedActionIndex()}.</li>
     *   <li>If {@code value.primaryVersionAction.version} is missing, return
     *       {@link #malformedActionValue()}.</li>
     *   <li>Otherwise, return {@code SUCCESS}.</li>
     * </ol>
     *
     * @implNote WAWebPrimaryVersionSync.applyMutations — per-mutation arrow
     *           function inside the {@code t.map} callback
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPrimaryVersionSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebPrimaryVersionSync.applyMutations: if (e.operation !== "set")
            return MutationApplicationResult.unsupported(); // WAWebPrimaryVersionSync.applyMutations: r++, return {actionState: Unsupported}
        }

        var indexArray = JSON.parseArray(mutation.index()); // WAWebPrimaryVersionSync.applyMutations: var i = e.indexParts
        var subIndex = indexArray.getString(1); // WAWebPrimaryVersionSync.applyMutations: var s = i[1]
        if (subIndex == null || subIndex.isEmpty() || (!subIndex.equals(INDEX_CURRENT) && !subIndex.equals(INDEX_SESSION_START))) { // WAWebPrimaryVersionSync.applyMutations: if (!s || s !== u.CURRENT && s !== u.SESSION_START)
            return malformedActionIndex(); // WAWebPrimaryVersionSync.applyMutations: return n.malformedActionIndex()
        }

        if (!(mutation.value().action().orElse(null) instanceof PrimaryVersionAction action) || action.version().isEmpty()) { // WAWebPrimaryVersionSync.applyMutations: var c = (t = l.primaryVersionAction) == null ? void 0 : t.version; if (c == null)
            return malformedActionValue(); // WAWebPrimaryVersionSync.applyMutations: a++, return WAWebSyncdIndexUtils.malformedActionValue(n.collectionName)
        }

        return MutationApplicationResult.success(); // WAWebPrimaryVersionSync.applyMutations: return {actionState: Success}
    }
}
