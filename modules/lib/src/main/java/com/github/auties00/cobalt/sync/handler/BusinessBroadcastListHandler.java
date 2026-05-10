package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.business.BroadcastListParticipant;
import com.github.auties00.cobalt.model.business.BroadcastListParticipantBuilder;
import com.github.auties00.cobalt.model.business.BusinessBroadcastListBuilder;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.business.BroadcastListParticipantAction;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastListAction;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastListActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles business broadcast list sync actions.
 *
 * <p>This handler processes mutations for business broadcast lists as part
 * of the app state synchronization pipeline. It handles both inbound
 * mutation application ({@code applyMutation}) and outbound mutation
 * building ({@code getBroadcastListMutation}, {@code getDeleteBroadcastListMutation}).
 *
 * <p>The WA Web class is a singleton extending {@code AccountSyncdActionBase}
 * with {@code collectionName = Regular}, {@code version = 1}, and
 * {@code action = "business_broadcast_list"}.
 *
 * <p>Index format: {@code ["business_broadcast_list", listId]}
 */
@WhatsAppWebModule(moduleName = "WAWebBroadcastListSync")
public final class BusinessBroadcastListHandler implements WebAppStateActionHandler {
    /**
     * Logger for broadcast list sync operations.
     */
    private static final Logger LOGGER = Logger.getLogger(BusinessBroadcastListHandler.class.getName());

    /**
     * The singleton instance of {@code BusinessBroadcastListHandler}.
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final BusinessBroadcastListHandler INSTANCE = new BusinessBroadcastListHandler();

    /**
     * Creates a new {@code BusinessBroadcastListHandler}.
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private BusinessBroadcastListHandler() {

    }

    /**
     * Returns the action name for this handler.
     * @return the action name string
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return BusinessBroadcastListAction.ACTION_NAME;
    }

    /**
     * Returns the sync collection this handler's action belongs to.
     * @return the sync patch type for the Regular collection
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return BusinessBroadcastListAction.COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for this handler.
     * @return the version number
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return BusinessBroadcastListAction.ACTION_VERSION;
    }

    /**
     * Applies a business broadcast list mutation and returns the detailed result.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastListSync.applyMutations}), each
     * mutation is processed as follows:
     * <ol>
     *   <li>Extract {@code indexParts[1]} as the list ID. If falsy, return
     *       {@code malformedActionIndex()}.</li>
     *   <li>On SET: extract the {@code businessBroadcastListAction} from the
     *       sync value. If absent, return {@code malformedActionValue()}.
     *       Then update broadcast list storage with the audience expression,
     *       list name, and resolved participants.</li>
     *   <li>On REMOVE: remove the broadcast list from storage.</li>
     * </ol>
     *
     * <p>WA Web also checks {@code isBizBroadcastSendWebEnabledNoExposure()}
     * and returns {@code Unsupported} if the feature is not enabled. Cobalt
     * does not replicate AB prop gating checks (architectural decision).
     *
     * <p>WA Web wraps each mutation in a try/catch returning {@code Failed}
     * on error. Per Cobalt's error model, exceptions propagate instead.
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        try {
            var indexArray = JSON.parseArray(mutation.index()); // ADAPTED: WAWebBroadcastListSync.applyMutations: var t = e.indexParts (pre-parsed in WA Web)
            var listId = indexArray.getString(1);
            if (listId == null || listId.isEmpty()) {
                return SyncdIndexUtils.malformedActionIndex(collectionName().name(), actionName());
            }

            if (mutation.operation() == SyncdOperation.SET) {
                if (!(mutation.value().action().orElse(null) instanceof BusinessBroadcastListAction action)) {
                    return SyncdIndexUtils.malformedActionValue(collectionName().name());
                }

                //   c = u.audienceExpression, d = u.labelIds, m = u.listName, p = u.participants
                //   _ = getMaybeMeLidUser()?.toString()
                //   f = (p ?? []).filter(e => e.lidJid !== _)
                //   g = c != null ? parseAudienceExpressionJson(c) : null
                //   h = g != null ? g : (d ?? []).length > 0 ? createLabelPredicateExpression(d ?? []) : createExplicitExpression(f.map(e => e.lidJid))
                //   yield updateBroadcastListStorage({audienceExpression: h, id: n, listName: m ?? ""})
                // ADAPTED: Cobalt stores the protobuf action directly in a flat typed quintet; it does not
                // compile audience expressions nor resolve labels/participants into a predicate object, because
                // the AudienceExpression DSL (WAWebAudienceExpressionTypes) is not ported and storage collapses
                // the multi-IDB-table layout of WAWebBroadcastListStorageUtils into a single record.
                List<BroadcastListParticipant> mirroredParticipants = null;
                if (!action.participants().isEmpty()) {
                    mirroredParticipants = new ArrayList<>(action.participants().size());
                    for (var p : action.participants()) {
                        mirroredParticipants.add(new BroadcastListParticipantBuilder()
                                .lidJid(p.lidJid())
                                .pnJid(p.pnJid().orElse(null))
                                .build());
                    }
                }
                List<String> mirroredLabelIds = action.labelIds().isEmpty() ? null : new ArrayList<>(action.labelIds());
                client.store().putBusinessBroadcastList(new BusinessBroadcastListBuilder()
                        .id(listId)
                        .deleted(action.deleted())
                        .participants(mirroredParticipants)
                        .listName(action.listName().orElse(null))
                        .labelIds(mirroredLabelIds)
                        .audienceExpression(action.audienceExpression().orElse(null))
                        .build()); // ADAPTED: typed-quintet upsert
                return MutationApplicationResult.success();
            }

            if (mutation.operation() == SyncdOperation.REMOVE) {
                // ADAPTED: Cobalt removes from the typed quintet instead of multiple IDB tables
                client.store().removeBusinessBroadcastList(listId); // ADAPTED: typed-quintet remove
                return MutationApplicationResult.success();
            }

            return MutationApplicationResult.failed();
        } catch (Exception e) {
            return MutationApplicationResult.failed();
        }
    }

    /**
     * Applies a batch of business broadcast list mutations.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastListSync.applyMutations}), the batch handler
     * iterates through mutations via {@code Promise.all(t.map(...))}, increments a malformed
     * counter each time a mutation is rejected for a missing {@code businessBroadcastListAction},
     * and emits a {@code WALogger.WARN("broadcast list sync: N malformed mutations")} message
     * when the batch finishes with a non-zero counter.
     * @param client    the WhatsAppClient instance linked to the mutations
     * @param mutations the batch of mutations to apply
     * @return a list of results parallel to the input
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public List<MutationApplicationResult> applyMutationBatch(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        var malformedCount = 0;
        var results = new ArrayList<MutationApplicationResult>(mutations.size());
        for (var mutation : mutations) { // ADAPTED: WAWebBroadcastListSync.applyMutations uses yield Promise.all(t.map(...))
            var result = applyMutation(client, mutation);
            if (result.actionState() == SyncActionState.MALFORMED) {
                malformedCount++;
            }
            results.add(result);
        }
        if (malformedCount > 0) {
            LOGGER.warning("broadcast list sync: " + malformedCount + " malformed mutations");
        }
        return results;
    }

    /**
     * Builds a pending SET mutation for creating or updating a business broadcast list with
     * a {@code null} audience expression.
     *
     * <p>Convenience overload that delegates to
     * {@link #getBroadcastListMutation(String, List, String, Instant, String)} with a
     * {@code null} audience expression, matching the common WA Web caller path where the
     * broadcast list is defined purely by its explicit participant snapshot.
     * @param listId       the broadcast list identifier (index arg)
     * @param participants the list of broadcast list participants
     * @param listName     the name of the broadcast list
     * @param timestamp    the mutation timestamp
     * @return a pending mutation ready for outbound sync
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "getBroadcastListMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getBroadcastListMutation(
            String listId,
            List<BroadcastListParticipantAction> participants,
            String listName,
            Instant timestamp
    ) {
        return getBroadcastListMutation(listId, participants, listName, timestamp, null); // ADAPTED: WAWebBroadcastListSync.getBroadcastListMutation defaults audience expression to null
    }

    /**
     * Builds a pending SET mutation for creating or updating a business broadcast list.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastListSync.getBroadcastListMutation}), this method
     * creates a sync action value containing the business broadcast list action with the
     * supplied participants, list name, an always-empty label id list, and a serialized
     * audience expression. The mutation is built as a SET operation via
     * {@code WAWebSyncdActionUtils.buildPendingMutation}.
     *
     * <p>WA Web compiles the audience expression object through
     * {@code WAWebAudienceExpressionTypes.serializeAudienceExpression(i)} before persisting it
     * on the wire. Cobalt accepts the already-serialized JSON string directly because the
     * AudienceExpression DSL is not ported; callers must supply the serialized form or
     * {@code null} to clear it.
     * @param listId             the broadcast list identifier (index arg)
     * @param participants       the list of broadcast list participants
     * @param listName           the name of the broadcast list
     * @param timestamp          the mutation timestamp
     * @param audienceExpression the pre-serialized audience expression, or {@code null}
     * @return a pending mutation ready for outbound sync
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "getBroadcastListMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getBroadcastListMutation(
            String listId,
            List<BroadcastListParticipantAction> participants,
            String listName,
            Instant timestamp,
            String audienceExpression
    ) {
        var action = new BusinessBroadcastListActionBuilder()
                .participants(participants)
                .listName(listName)
                .labelIds(List.of())
                .audienceExpression(audienceExpression)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .businessBroadcastListAction(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName(), listId));
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
     * Builds a pending REMOVE mutation for deleting a business broadcast list.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastListSync.getDeleteBroadcastListMutation}),
     * this method creates a sync action value with an empty payload and builds
     * a REMOVE operation via {@code WAWebSyncdActionUtils.buildPendingMutation}.
     * @param listId    the broadcast list identifier to remove
     * @param timestamp the mutation timestamp
     * @return a pending mutation ready for outbound sync
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "getDeleteBroadcastListMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getDeleteBroadcastListMutation(String listId, Instant timestamp) {
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .build();
        var index = JSON.toJSONString(List.of(actionName(), listId));
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
