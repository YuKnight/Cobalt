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
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastCampaignAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
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
@WhatsAppWebModule(moduleName = "WAWebBroadcastCampaignSync")
public final class BusinessBroadcastCampaignHandler implements WebAppStateActionHandler {
    /**
     * Logger for broadcast campaign sync operations.
     *
     * @implNote ADAPTED: WAWebBroadcastCampaignSync uses WALogger; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(BusinessBroadcastCampaignHandler.class.getName());

    /**
     * The singleton instance of {@code BusinessBroadcastCampaignHandler}.
     *
     * @implNote WAWebBroadcastCampaignSync — module-level {@code c = new u()} singleton,
     *           exported as {@code l.default = c}
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastCampaignSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final BusinessBroadcastCampaignHandler INSTANCE = new BusinessBroadcastCampaignHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebBroadcastCampaignSync — class {@code u} constructor sets
     *           {@code this.collectionName = o("WASyncdConst").CollectionName.Regular}
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastCampaignSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
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
    @WhatsAppWebExport(moduleName = "WAWebBroadcastCampaignSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return BusinessBroadcastCampaignAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebBroadcastCampaignSync — constructor sets
     *           {@code this.collectionName = o("WASyncdConst").CollectionName.Regular}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastCampaignSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return BusinessBroadcastCampaignAction.COLLECTION_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastCampaignSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return BusinessBroadcastCampaignAction.ACTION_VERSION;
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
    @WhatsAppWebExport(moduleName = "WAWebBroadcastCampaignSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
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
    @WhatsAppWebExport(moduleName = "WAWebBroadcastCampaignSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        try {
            var indexArray = JSON.parseArray(mutation.index()); // ADAPTED: WAWebBroadcastCampaignSync uses e.indexParts (pre-parsed); Cobalt parses from JSON string
            var campaignId = indexArray.getString(1);
            if (campaignId == null || campaignId.isEmpty()) {
                return malformedActionIndex();
            }

            var campaigns = new HashMap<>(client.store().businessBroadcastCampaigns()); // ADAPTED: WAWebBroadcastCampaignSync uses IndexedDB table; Cobalt uses ConcurrentHashMap store
            if (mutation.operation() == SyncdOperation.SET) {
                if (!(mutation.value().action().orElse(null) instanceof BusinessBroadcastCampaignAction action)) {
                    return malformedActionValue();
                }

                if (action.broadcastJid().isEmpty() || action.deviceId().isEmpty() || action.status().isEmpty()) {
                    return malformedActionValue();
                }

                campaigns.put(campaignId, action); // ADAPTED: WAWebBroadcastCampaignSync.applyMutations: yield o("WAWebBizBroadcastCampaignStorageUtils").upsertCampaignStorage(n, c, u)
                client.store().setBusinessBroadcastCampaigns(campaigns);
                return MutationApplicationResult.success();
            }

            if (mutation.operation() == SyncdOperation.REMOVE) {
                campaigns.remove(campaignId); // ADAPTED: WAWebBroadcastCampaignSync.applyMutations: yield o("WAWebBizBroadcastCampaignStorageUtils").removeCampaignStorage(n)
                client.store().setBusinessBroadcastCampaigns(campaigns);
                return MutationApplicationResult.success();
            }

            return MutationApplicationResult.failed();
        } catch (Exception e) {
            return MutationApplicationResult.failed();
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
    @WhatsAppWebExport(moduleName = "WAWebBroadcastCampaignSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public List<Boolean> applyMutationBatch(WhatsAppClient client, WamService wamService, List<DecryptedMutation.Trusted> mutations) {
        // ADAPTED: WAWebBroadcastCampaignSync.applyMutations checks isBizBroadcastSendWebEnabledNoExposure()
        // and returns all Unsupported if false — Cobalt omits AB prop gating
        var malformedCount = 0;
        var results = new ArrayList<Boolean>(mutations.size());
        for (var mutation : mutations) { // ADAPTED: WAWebBroadcastCampaignSync.applyMutations uses Promise.all(t.map(...))
            var result = applyMutationResult(client, wamService, mutation);
            if (result.actionState() == SyncActionState.MALFORMED) {
                malformedCount++;
            }
            results.add(result.actionState() == SyncActionState.SUCCESS);
        }
        if (malformedCount > 0) {
            LOGGER.warning("broadcast campaign sync: " + malformedCount + " malformed mutations");
        }
        // ADAPTED: WAWebBroadcastCampaignSync.applyMutations: i.size > 0 && o("WAWebBackendApi").frontendFireAndForget("refreshBroadcastCampaignState", ...)
        // — frontend UI refresh omitted, no Cobalt equivalent
        return results;
    }

    /**
     * Builds a pending SET mutation for creating or updating a business broadcast campaign.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastCampaignSync.getCampaignMutation}),
     * this method wraps the supplied {@link BusinessBroadcastCampaignAction} into a
     * {@code SyncActionValue} whose payload field is
     * {@code businessBroadcastCampaignAction}, then delegates to
     * {@code WAWebSyncdActionUtils.buildPendingMutation} with {@code action =
     * getAction()}, {@code indexArgs = [campaignId]}, {@code collection =
     * Regular}, {@code version = 1}, and {@code operation = SET}.
     *
     * @implNote WAWebBroadcastCampaignSync.getCampaignMutation — {@code var e = {businessBroadcastCampaignAction: n};
     *           return WAWebSyncdActionUtils.buildPendingMutation({action: this.getAction(), indexArgs: [t],
     *           collection: this.collectionName, value: e, version: this.getVersion(),
     *           operation: SyncdMutation$SyncdOperation.SET, timestamp: r})}
     * @param campaignId the business broadcast campaign identifier (index arg)
     * @param action     the campaign action payload
     * @param timestamp  the mutation timestamp
     * @return a pending mutation ready for outbound sync
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastCampaignSync", exports = "getCampaignMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getCampaignMutation(
            String campaignId,
            BusinessBroadcastCampaignAction action,
            Instant timestamp
    ) {
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .businessBroadcastCampaignAction(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName(), campaignId));
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0);
    }

    /**
     * Builds a pending REMOVE mutation for deleting a business broadcast campaign.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastCampaignSync.getDeleteCampaignMutation}),
     * this method delegates to {@code WAWebSyncdActionUtils.buildPendingMutation}
     * with an empty value, {@code indexArgs = [campaignId]}, and
     * {@code operation = REMOVE}.
     *
     * @implNote WAWebBroadcastCampaignSync.getDeleteCampaignMutation —
     *           {@code return WAWebSyncdActionUtils.buildPendingMutation({action: this.getAction(),
     *           indexArgs: [t], collection: this.collectionName, value: {}, version: this.getVersion(),
     *           operation: SyncdMutation$SyncdOperation.REMOVE, timestamp: n})}
     * @param campaignId the business broadcast campaign identifier to remove
     * @param timestamp  the mutation timestamp
     * @return a pending mutation ready for outbound sync
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastCampaignSync", exports = "getDeleteCampaignMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getDeleteCampaignMutation(String campaignId, Instant timestamp) {
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .build();
        var index = JSON.toJSONString(List.of(actionName(), campaignId));
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.REMOVE,
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0);
    }
}
