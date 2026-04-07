package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastInsightsAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles business broadcast insights sync actions.
 *
 * <p>Per WhatsApp Web, this handler processes mutations for business
 * broadcast campaign delivery statistics (recipient, delivered, read,
 * replied, and quick reply counts). It is synced via the {@code REGULAR}
 * collection. The handler supports both SET (upsert) and REMOVE operations,
 * and tracks per-batch counters for SET, REMOVE, and malformed mutations.
 *
 * <p>Index format: {@code ["business_broadcast_insights_sync", campaignId]}
 *
 * @implNote WAWebBusinessBroadcastInsightsSync.default — singleton instance of the
 *           {@code AccountSyncdActionBase} subclass with
 *           {@code collectionName = Regular}, {@code getVersion() = 1},
 *           {@code getAction() = "business_broadcast_insights_sync"}
 */
public final class BusinessBroadcastInsightsHandler implements WebAppStateActionHandler {
    /**
     * Logger for broadcast insights sync operations.
     *
     * @implNote ADAPTED: WAWebBusinessBroadcastInsightsSync uses WALogger; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(BusinessBroadcastInsightsHandler.class.getName()); // ADAPTED: WALogger

    /**
     * The singleton instance of {@code BusinessBroadcastInsightsHandler}.
     *
     * @implNote WAWebBusinessBroadcastInsightsSync — module-level {@code m = new d()} singleton,
     *           exported as {@code l.default = m}
     */
    public static final BusinessBroadcastInsightsHandler INSTANCE = new BusinessBroadcastInsightsHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebBusinessBroadcastInsightsSync — class {@code d} constructor sets
     *           {@code this.collectionName = o("WASyncdConst").CollectionName.Regular}
     */
    private BusinessBroadcastInsightsHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebBusinessBroadcastInsightsSync.getAction — returns
     *           {@code o("WASyncdConst").Actions.BusinessBroadcastInsights}
     *           which is {@code "business_broadcast_insights_sync"}
     */
    @Override
    public String actionName() {
        return BusinessBroadcastInsightsAction.ACTION_NAME; // WAWebBusinessBroadcastInsightsSync.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebBusinessBroadcastInsightsSync — constructor sets
     *           {@code this.collectionName = o("WASyncdConst").CollectionName.Regular}
     */
    @Override
    public SyncPatchType collectionName() {
        return BusinessBroadcastInsightsAction.COLLECTION_NAME; // WAWebBusinessBroadcastInsightsSync: collectionName = Regular
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebBusinessBroadcastInsightsSync.getVersion — returns {@code 1}
     */
    @Override
    public int version() {
        return BusinessBroadcastInsightsAction.ACTION_VERSION; // WAWebBusinessBroadcastInsightsSync.getVersion
    }

    /**
     * Applies a business broadcast insights mutation.
     *
     * <p>Per WhatsApp Web ({@code WAWebBusinessBroadcastInsightsSync.applyMutations}),
     * delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@code SUCCESS}.
     *
     * @implNote WAWebBusinessBroadcastInsightsSync.applyMutations — per-mutation logic
     *           within the batch handler's {@code Promise.all(mutations.map(...))}
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // WAWebBusinessBroadcastInsightsSync.applyMutations
    }

    /**
     * Applies a business broadcast insights mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web ({@code WAWebBusinessBroadcastInsightsSync.applyMutations}),
     * the per-mutation logic:
     * <ol>
     *   <li>Extracts {@code campaignId} from {@code indexParts[1]}; returns
     *       {@code malformedActionIndex()} if missing</li>
     *   <li>For SET operations: extracts the {@code businessBroadcastInsightsAction}
     *       value; returns {@code malformedActionValue(collectionName)} if absent;
     *       otherwise calls {@code upsertInsightsStorage(campaignId, action, timestamp)}</li>
     *   <li>For REMOVE operations: calls
     *       {@code removeInsightsStorage(campaignId)}</li>
     *   <li>For unrecognised operations: throws an error (caught by outer try/catch
     *       returning {@code Failed})</li>
     *   <li>Wraps the entire logic in try/catch, returning {@code Failed} on error</li>
     * </ol>
     *
     * @implNote WAWebBusinessBroadcastInsightsSync.applyMutations — per-mutation logic within
     *           the batch handler's {@code Promise.all(mutations.map(...))}
     * @param client   the WhatsApp client
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        try { // WAWebBusinessBroadcastInsightsSync.applyMutations: try { ... } catch(e) { return {actionState: Failed} }
            var indexArray = JSON.parseArray(mutation.index()); // ADAPTED: WAWebBusinessBroadcastInsightsSync uses e.indexParts (pre-parsed); Cobalt parses from JSON string
            var campaignId = indexArray.getString(1); // WAWebBusinessBroadcastInsightsSync.applyMutations: var n = t[1]
            if (campaignId == null || campaignId.isEmpty()) { // WAWebBusinessBroadcastInsightsSync.applyMutations: if (!n) return r.malformedActionIndex()
                return malformedActionIndex(); // WAWebBusinessBroadcastInsightsSync.applyMutations: r.malformedActionIndex()
            }

            var insights = new HashMap<>(client.store().businessBroadcastInsights()); // ADAPTED: WAWebBusinessBroadcastInsightsSync uses IndexedDB table; Cobalt uses ConcurrentHashMap store
            if (mutation.operation() == SyncdOperation.SET) { // WAWebBusinessBroadcastInsightsSync.applyMutations: s.operation === "set" && "value" in s && "timestamp" in s
                if (!(mutation.value().action().orElse(null) instanceof BusinessBroadcastInsightsAction action)) { // WAWebBusinessBroadcastInsightsSync.applyMutations: var p = u.businessBroadcastInsightsAction; if (!p)
                    return malformedActionValue(); // WAWebBusinessBroadcastInsightsSync.applyMutations: return a++, o("WAWebSyncdIndexUtils").malformedActionValue(r.collectionName)
                }

                insights.put(campaignId, action); // ADAPTED: WAWebBusinessBroadcastInsightsSync.applyMutations: yield o("WAWebBizBroadcastInsightsStorageUtils").upsertInsightsStorage(n, {...}, c)
                client.store().setBusinessBroadcastInsights(insights); // ADAPTED: store update via setter
                return MutationApplicationResult.success(); // WAWebBusinessBroadcastInsightsSync.applyMutations: {actionState: Success}
            }

            if (mutation.operation() == SyncdOperation.REMOVE) { // WAWebBusinessBroadcastInsightsSync.applyMutations: s.operation === "remove"
                insights.remove(campaignId); // ADAPTED: WAWebBusinessBroadcastInsightsSync.applyMutations: yield o("WAWebBizBroadcastInsightsStorageUtils").removeInsightsStorage(n)
                client.store().setBusinessBroadcastInsights(insights); // ADAPTED: store update via setter
                return MutationApplicationResult.success(); // WAWebBusinessBroadcastInsightsSync.applyMutations: {actionState: Success}
            }

            return MutationApplicationResult.failed(); // WAWebBusinessBroadcastInsightsSync.applyMutations: throw Error("Match: No case succesfully matched...") — caught by outer try/catch returning Failed
        } catch (Exception e) { // WAWebBusinessBroadcastInsightsSync.applyMutations: catch(e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebBusinessBroadcastInsightsSync.applyMutations: {actionState: o("WASyncdConst").SyncActionState.Failed}
        }
    }

    /**
     * Applies a batch of business broadcast insights mutations.
     *
     * <p>Per WhatsApp Web ({@code WAWebBusinessBroadcastInsightsSync.applyMutations}), the batch
     * handler first checks the {@code isBizBroadcastSendWebEnabledNoExposure()} gating flag.
     * If the feature is not enabled, all mutations are returned as {@code Unsupported}.
     * Otherwise, each mutation is processed individually and counters are maintained for
     * SET operations, REMOVE operations, and malformed mutations. After the batch, warnings
     * are logged for each non-zero counter, and a {@code refreshBroadcastCampaignState}
     * event is fired if any SET or REMOVE operations succeeded.
     *
     * <p>In Cobalt, the AB prop gating check and the frontend fire-and-forget call are
     * intentionally omitted (AB props and frontend API are not replicated). The malformed,
     * SET, and REMOVE count warnings are preserved via logging.
     *
     * @implNote WAWebBusinessBroadcastInsightsSync.applyMutations — batch entry point with
     *           {@code isBizBroadcastSendWebEnabledNoExposure()} gating,
     *           {@code Promise.all(mutations.map(...))}, SET/REMOVE/malformed count warnings,
     *           and {@code frontendFireAndForget("refreshBroadcastCampaignState", ...)}
     * @param client    the WhatsAppClient instance linked to the mutations
     * @param mutations the batch of mutations to apply
     * @return a list of results parallel to the input
     */
    @Override
    public List<Boolean> applyMutationBatch(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        // ADAPTED: WAWebBusinessBroadcastInsightsSync.applyMutations checks isBizBroadcastSendWebEnabledNoExposure()
        // and returns all Unsupported if false — Cobalt omits AB prop gating
        var malformedCount = 0; // WAWebBusinessBroadcastInsightsSync.applyMutations: var a = 0
        var setCount = 0; // WAWebBusinessBroadcastInsightsSync.applyMutations: var i = 0
        var removeCount = 0; // WAWebBusinessBroadcastInsightsSync.applyMutations: var d = 0
        var results = new ArrayList<Boolean>(mutations.size());
        for (var mutation : mutations) { // ADAPTED: WAWebBusinessBroadcastInsightsSync.applyMutations uses Promise.all(t.map(...))
            var result = applyMutationResult(client, mutation);
            if (result.actionState() == SyncActionState.MALFORMED) { // WAWebBusinessBroadcastInsightsSync.applyMutations: a++ on malformed
                malformedCount++;
            }
            if (result.actionState() == SyncActionState.SUCCESS && mutation.operation() == SyncdOperation.SET) { // WAWebBusinessBroadcastInsightsSync.applyMutations: i++ on SET
                setCount++;
            }
            if (result.actionState() == SyncActionState.SUCCESS && mutation.operation() == SyncdOperation.REMOVE) { // WAWebBusinessBroadcastInsightsSync.applyMutations: d++ on REMOVE
                removeCount++;
            }
            results.add(result.actionState() == SyncActionState.SUCCESS);
        }
        if (setCount > 0) { // WAWebBusinessBroadcastInsightsSync.applyMutations: i > 0 && o("WALogger").WARN(...)
            LOGGER.warning("BBI SyncD received " + setCount + " SET operations"); // WAWebBusinessBroadcastInsightsSync.applyMutations: WALogger.WARN("BBI SyncD received N SET operations => ...")
        }
        if (removeCount > 0) { // WAWebBusinessBroadcastInsightsSync.applyMutations: d > 0 && o("WALogger").WARN(...)
            LOGGER.warning("BBI SyncD received " + removeCount + " REMOVE operations"); // WAWebBusinessBroadcastInsightsSync.applyMutations: WALogger.WARN("BBI SyncD received N REMOVE operations for campaigns => ...")
        }
        if (malformedCount > 0) { // WAWebBusinessBroadcastInsightsSync.applyMutations: a > 0 && o("WALogger").WARN(...)
            LOGGER.warning("BBI sync: " + malformedCount + " malformed mutations"); // WAWebBusinessBroadcastInsightsSync.applyMutations: WALogger.WARN("BBI sync: N malformed mutations")
        }
        // ADAPTED: WAWebBusinessBroadcastInsightsSync.applyMutations: (i > 0 || d > 0) && o("WAWebBackendApi").frontendFireAndForget("refreshBroadcastCampaignState", {broadcastJids: []})
        // — frontend UI refresh omitted, no Cobalt equivalent
        return results;
    }
}
