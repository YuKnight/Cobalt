package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastCampaignAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles business broadcast campaign sync actions.
 *
 * <p>This handler processes mutations for business broadcast campaigns,
 * supporting both SET (upsert) and REMOVE operations. On SET, the handler
 * validates that the action value contains non-null {@code broadcastJid},
 * {@code deviceId}, and {@code status} fields before persisting.
 *
 * <p>Index format: {@code ["business_broadcast_campaign", campaignId]}
 *
 * @implNote WAWebBroadcastCampaignSync.default — singleton instance of the
 *           {@code AccountSyncdActionBase} subclass with
 *           {@code collectionName = Regular}, {@code getVersion() = 1},
 *           {@code getAction() = "business_broadcast_campaign"}
 */
public final class BusinessBroadcastCampaignHandler implements WebAppStateActionHandler {
    /**
     * Logger for broadcast campaign sync operations.
     *
     * @implNote ADAPTED: WAWebBroadcastCampaignSync uses WALogger; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(BusinessBroadcastCampaignHandler.class.getName()); // ADAPTED: WALogger

    /**
     * The singleton instance of {@code BusinessBroadcastCampaignHandler}.
     *
     * @implNote WAWebBroadcastCampaignSync — module-level {@code c = new u()} singleton,
     *           exported as {@code l.default = c}
     */
    public static final BusinessBroadcastCampaignHandler INSTANCE = new BusinessBroadcastCampaignHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebBroadcastCampaignSync — class {@code u} constructor sets
     *           {@code this.collectionName = o("WASyncdConst").CollectionName.Regular}
     */
    private BusinessBroadcastCampaignHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebBroadcastCampaignSync.getAction — returns
     *           {@code o("WASyncdConst").Actions.BusinessBroadcastCampaign}
     *           which is {@code "business_broadcast_campaign"}
     */
    @Override
    public String actionName() {
        return BusinessBroadcastCampaignAction.ACTION_NAME; // WAWebBroadcastCampaignSync.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebBroadcastCampaignSync — constructor sets
     *           {@code this.collectionName = o("WASyncdConst").CollectionName.Regular}
     */
    @Override
    public SyncPatchType collectionName() {
        return BusinessBroadcastCampaignAction.COLLECTION_NAME; // WAWebBroadcastCampaignSync: collectionName = Regular
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebBroadcastCampaignSync.getVersion — returns {@code 1}
     */
    @Override
    public int version() {
        return BusinessBroadcastCampaignAction.ACTION_VERSION; // WAWebBroadcastCampaignSync.getVersion
    }

    /**
     * Applies a business broadcast campaign mutation.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastCampaignSync.applyMutations}),
     * on SET the handler validates that the action value is present and that
     * {@code broadcastJid}, {@code deviceId}, and {@code status} are all
     * non-null. If any are missing, the mutation is classified as malformed
     * via {@code WAWebSyncdIndexUtils.malformedActionValue}. On REMOVE, the
     * campaign is removed from storage. The {@code campaignId} from
     * {@code indexParts[1]} must be present or the mutation is classified as
     * malformed via {@code malformedActionIndex}.
     *
     * <p>Per WhatsApp Web, each individual mutation is wrapped in a try/catch;
     * any unhandled error yields {@code SyncActionState.Failed}.
     *
     * @implNote WAWebBroadcastCampaignSync.applyMutations — per-mutation logic
     *           within the batch handler's {@code Promise.all(mutations.map(...))}
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS; // WAWebBroadcastCampaignSync.applyMutations
    }

    /**
     * Applies a business broadcast campaign mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastCampaignSync.applyMutations}),
     * the per-mutation logic:
     * <ol>
     *   <li>Extracts {@code campaignId} from {@code indexParts[1]}; returns
     *       {@code malformedActionIndex()} if missing</li>
     *   <li>For SET operations: validates the {@code businessBroadcastCampaignAction}
     *       value has non-null {@code broadcastJid}, {@code deviceId}, and {@code status};
     *       returns {@code malformedActionValue(collectionName)} if invalid; otherwise
     *       calls {@code upsertCampaignStorage(campaignId, action, timestamp)}</li>
     *   <li>For REMOVE operations: calls
     *       {@code removeCampaignStorage(campaignId)}</li>
     *   <li>Wraps the entire logic in try/catch, returning {@code Failed} on error</li>
     * </ol>
     *
     * @implNote WAWebBroadcastCampaignSync.applyMutations — per-mutation logic within
     *           the batch handler's {@code Promise.all(mutations.map(...))}
     * @param client   the WhatsApp client
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        try { // WAWebBroadcastCampaignSync.applyMutations: try { ... } catch(e) { return {actionState: Failed} }
            var indexArray = JSON.parseArray(mutation.index()); // ADAPTED: WAWebBroadcastCampaignSync uses e.indexParts (pre-parsed); Cobalt parses from JSON string
            var campaignId = indexArray.getString(1); // WAWebBroadcastCampaignSync.applyMutations: var n = t[1]
            if (campaignId == null || campaignId.isEmpty()) { // WAWebBroadcastCampaignSync.applyMutations: if (!n) return r.malformedActionIndex()
                return malformedActionIndex(); // WAWebBroadcastCampaignSync.applyMutations: r.malformedActionIndex()
            }

            var campaigns = new java.util.HashMap<>(client.store().businessBroadcastCampaigns()); // ADAPTED: WAWebBroadcastCampaignSync uses IndexedDB table; Cobalt uses ConcurrentHashMap store
            if (mutation.operation() == SyncdOperation.SET) { // WAWebBroadcastCampaignSync.applyMutations: l.operation === "set"
                if (!(mutation.value().action().orElse(null) instanceof BusinessBroadcastCampaignAction action)) { // WAWebBroadcastCampaignSync.applyMutations: var c = s.businessBroadcastCampaignAction; if (!c || ...)
                    return malformedActionValue(); // WAWebBroadcastCampaignSync.applyMutations: return a++, o("WAWebSyncdIndexUtils").malformedActionValue(r.collectionName)
                }

                if (action.broadcastJid().isEmpty() || action.deviceId().isEmpty() || action.status().isEmpty()) { // WAWebBroadcastCampaignSync.applyMutations: c.broadcastJid == null || c.deviceId == null || c.status == null
                    return malformedActionValue(); // WAWebBroadcastCampaignSync.applyMutations: return a++, o("WAWebSyncdIndexUtils").malformedActionValue(r.collectionName)
                }

                campaigns.put(campaignId, action); // ADAPTED: WAWebBroadcastCampaignSync.applyMutations: yield o("WAWebBizBroadcastCampaignStorageUtils").upsertCampaignStorage(n, c, u)
                client.store().setBusinessBroadcastCampaigns(campaigns); // ADAPTED: store update via setter
                return MutationApplicationResult.success(); // WAWebBroadcastCampaignSync.applyMutations: {actionState: Success}
            }

            if (mutation.operation() == SyncdOperation.REMOVE) { // WAWebBroadcastCampaignSync.applyMutations: l.operation === "remove"
                campaigns.remove(campaignId); // ADAPTED: WAWebBroadcastCampaignSync.applyMutations: yield o("WAWebBizBroadcastCampaignStorageUtils").removeCampaignStorage(n)
                client.store().setBusinessBroadcastCampaigns(campaigns); // ADAPTED: store update via setter
                return MutationApplicationResult.success(); // WAWebBroadcastCampaignSync.applyMutations: {actionState: Success}
            }

            return MutationApplicationResult.failed(); // WAWebBroadcastCampaignSync.applyMutations: throw Error("Match: No case succesfully matched...") — caught by outer try/catch returning Failed
        } catch (Exception e) { // WAWebBroadcastCampaignSync.applyMutations: catch(e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebBroadcastCampaignSync.applyMutations: {actionState: o("WASyncdConst").SyncActionState.Failed}
        }
    }

    /**
     * Applies a batch of business broadcast campaign mutations.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastCampaignSync.applyMutations}), the batch
     * handler first checks the {@code isBizBroadcastSendWebEnabledNoExposure()} gating flag.
     * If the feature is not enabled, all mutations are returned as {@code Unsupported}.
     * Otherwise, each mutation is processed individually and a malformed count is logged
     * after the batch. Additionally, any affected {@code broadcastJid}s are collected and
     * a {@code refreshBroadcastCampaignState} event is fired.
     *
     * <p>In Cobalt, the AB prop gating check and the frontend fire-and-forget call are
     * intentionally omitted (AB props and frontend API are not replicated). The malformed
     * count warning is preserved via logging.
     *
     * @implNote WAWebBroadcastCampaignSync.applyMutations — batch entry point with
     *           {@code isBizBroadcastSendWebEnabledNoExposure()} gating,
     *           {@code Promise.all(mutations.map(...))}, malformed count warning,
     *           and {@code frontendFireAndForget("refreshBroadcastCampaignState", ...)}
     * @param client    the WhatsAppClient instance linked to the mutations
     * @param mutations the batch of mutations to apply
     * @return a list of results parallel to the input
     */
    @Override
    public List<Boolean> applyMutationBatch(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        // ADAPTED: WAWebBroadcastCampaignSync.applyMutations checks isBizBroadcastSendWebEnabledNoExposure()
        // and returns all Unsupported if false — Cobalt omits AB prop gating
        var malformedCount = 0; // WAWebBroadcastCampaignSync.applyMutations: var a = 0
        var results = new ArrayList<Boolean>(mutations.size());
        for (var mutation : mutations) { // ADAPTED: WAWebBroadcastCampaignSync.applyMutations uses Promise.all(t.map(...))
            var result = applyMutationResult(client, mutation);
            if (result.actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.MALFORMED) { // WAWebBroadcastCampaignSync.applyMutations: a++ on malformed
                malformedCount++;
            }
            results.add(result.actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS);
        }
        if (malformedCount > 0) { // WAWebBroadcastCampaignSync.applyMutations: a > 0 && o("WALogger").WARN(...)
            LOGGER.warning("broadcast campaign sync: " + malformedCount + " malformed mutations"); // WAWebBroadcastCampaignSync.applyMutations: WALogger.WARN("broadcast campaign sync: N malformed mutations")
        }
        // ADAPTED: WAWebBroadcastCampaignSync.applyMutations: i.size > 0 && o("WAWebBackendApi").frontendFireAndForget("refreshBroadcastCampaignState", ...)
        // — frontend UI refresh omitted, no Cobalt equivalent
        return results;
    }
}
