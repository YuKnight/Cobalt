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
import com.github.auties00.cobalt.model.sync.action.business.BroadcastListParticipantAction;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastListAction;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastListActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles business broadcast list sync actions.
 *
 * <p>This handler processes mutations for business broadcast lists as part
 * of the app state synchronization pipeline. It handles both inbound
 * mutation application ({@code applyMutationResult}) and outbound mutation
 * building ({@code getBroadcastListMutation}, {@code getDeleteBroadcastListMutation}).
 *
 * <p>The WA Web class is a singleton extending {@code AccountSyncdActionBase}
 * with {@code collectionName = Regular}, {@code version = 1}, and
 * {@code action = "business_broadcast_list"}.
 *
 * <p>Index format: {@code ["business_broadcast_list", listId]}
 *
 * @implNote WAWebBroadcastListSync.default
 */
@WhatsAppWebModule(moduleName = "WAWebBroadcastListSync")
public final class BusinessBroadcastListHandler implements WebAppStateActionHandler {
    /**
     * Logger for broadcast list sync operations.
     *
     * @implNote ADAPTED: WAWebBroadcastListSync uses {@code WALogger}; Cobalt uses
     *           {@code java.util.logging}
     */
    private static final Logger LOGGER = Logger.getLogger(BusinessBroadcastListHandler.class.getName()); // ADAPTED: WALogger

    /**
     * The singleton instance of {@code BusinessBroadcastListHandler}.
     *
     * @implNote WAWebBroadcastListSync — module-level singleton: {@code var c = new u; l.default = c}
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final BusinessBroadcastListHandler INSTANCE = new BusinessBroadcastListHandler();

    /**
     * Creates a new {@code BusinessBroadcastListHandler}.
     *
     * @implNote WAWebBroadcastListSync — singleton constructor via
     *           {@code AccountSyncdActionBase} inheritance
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private BusinessBroadcastListHandler() {

    }

    /**
     * Returns the action name for this handler.
     *
     * @implNote WAWebBroadcastListSync.getAction — returns
     *           {@code WASyncdConst.Actions.BusinessBroadcastList} which is
     *           {@code "business_broadcast_list"}
     * @return the action name string
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return BusinessBroadcastListAction.ACTION_NAME; // WAWebBroadcastListSync.getAction
    }

    /**
     * Returns the sync collection this handler's action belongs to.
     *
     * @implNote WAWebBroadcastListSync — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     * @return the sync patch type for the Regular collection
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return BusinessBroadcastListAction.COLLECTION_NAME; // WAWebBroadcastListSync.collectionName = WASyncdConst.CollectionName.Regular
    }

    /**
     * Returns the mutation format version for this handler.
     *
     * @implNote WAWebBroadcastListSync.getVersion — returns {@code 1}
     * @return the version number
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return BusinessBroadcastListAction.ACTION_VERSION; // WAWebBroadcastListSync.getVersion
    }

    /**
     * Applies a business broadcast list mutation and returns the boolean result.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the action state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebBroadcastListSync.applyMutations — WA Web returns
     *           {@code SyncActionState} values; Cobalt adapts to boolean via
     *           the interface contract
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebBroadcastListSync.applyMutations
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
     *
     * @implNote WAWebBroadcastListSync.applyMutations
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        try { // WAWebBroadcastListSync.applyMutations: try { ... } catch(e) { return {actionState: Failed} }
            var indexArray = JSON.parseArray(mutation.index()); // ADAPTED: WAWebBroadcastListSync.applyMutations: var t = e.indexParts (pre-parsed in WA Web)
            var listId = indexArray.getString(1); // WAWebBroadcastListSync.applyMutations: n = t[1]
            if (listId == null || listId.isEmpty()) { // WAWebBroadcastListSync.applyMutations: if (!n) return r.malformedActionIndex()
                return malformedActionIndex(); // WAWebBroadcastListSync.applyMutations: return r.malformedActionIndex()
            }

            if (mutation.operation() == SyncdOperation.SET) { // WAWebBroadcastListSync.applyMutations: i.operation === "set" && "value" in i
                if (!(mutation.value().action().orElse(null) instanceof BusinessBroadcastListAction action)) { // WAWebBroadcastListSync.applyMutations: var u = s.businessBroadcastListAction; if (!u) return malformedActionValue
                    return malformedActionValue(); // WAWebBroadcastListSync.applyMutations: return a++, o("WAWebSyncdIndexUtils").malformedActionValue(r.collectionName)
                }

                // WAWebBroadcastListSync.applyMutations: resolves audience expression from action fields:
                //   c = u.audienceExpression, d = u.labelIds, m = u.listName, p = u.participants
                //   _ = getMaybeMeLidUser()?.toString()
                //   f = (p ?? []).filter(e => e.lidJid !== _)
                //   g = c != null ? parseAudienceExpressionJson(c) : null
                //   h = g != null ? g : (d ?? []).length > 0 ? createLabelPredicateExpression(d ?? []) : createExplicitExpression(f.map(e => e.lidJid))
                //   yield updateBroadcastListStorage({audienceExpression: h, id: n, listName: m ?? ""})
                // ADAPTED: Cobalt stores the protobuf action directly in a flat ConcurrentHashMap; it does not
                // compile audience expressions nor resolve labels/participants into a predicate object, because
                // the AudienceExpression DSL (WAWebAudienceExpressionTypes) is not ported and storage collapses
                // the multi-IDB-table layout of WAWebBroadcastListStorageUtils into a single map.
                var lists = new HashMap<>(client.store().businessBroadcastLists()); // ADAPTED: copy from unmodifiable map
                lists.put(listId, action); // WAWebBroadcastListStorageUtils.updateBroadcastListStorage
                client.store().setBusinessBroadcastLists(lists); // ADAPTED: set back to store
                return MutationApplicationResult.success(); // WAWebBroadcastListSync.applyMutations: {actionState: Success}
            }

            if (mutation.operation() == SyncdOperation.REMOVE) { // WAWebBroadcastListSync.applyMutations: i.operation === "remove"
                // WAWebBroadcastListSync.applyMutations: yield o("WAWebBroadcastListStorageUtils").removeBroadcastListStorage(n)
                // ADAPTED: Cobalt removes from flat ConcurrentHashMap instead of multiple IDB tables
                var lists = new HashMap<>(client.store().businessBroadcastLists()); // ADAPTED: copy from unmodifiable map
                lists.remove(listId); // WAWebBroadcastListStorageUtils.removeBroadcastListStorage
                client.store().setBusinessBroadcastLists(lists); // ADAPTED: set back to store
                return MutationApplicationResult.success(); // WAWebBroadcastListSync.applyMutations: {actionState: Success}
            }

            // WAWebBroadcastListSync.applyMutations: throw Error("Match: No case successfully matched...")
            return MutationApplicationResult.failed(); // WAWebBroadcastListSync.applyMutations: caught by outer try/catch -> {actionState: Failed}
        } catch (Exception e) { // WAWebBroadcastListSync.applyMutations: catch(e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebBroadcastListSync.applyMutations: {actionState: WASyncdConst.SyncActionState.Failed}
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
     *
     * @implNote WAWebBroadcastListSync.applyMutations — batch entry point, malformed counter,
     *           and {@code WALogger.WARN} tagged template literal warning.
     * @param client    the WhatsAppClient instance linked to the mutations
     * @param mutations the batch of mutations to apply
     * @return a list of booleans parallel to the input, {@code true} for {@code SUCCESS}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public List<Boolean> applyMutationBatch(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        var malformedCount = 0; // WAWebBroadcastListSync.applyMutations: var a = 0
        var results = new java.util.ArrayList<Boolean>(mutations.size());
        for (var mutation : mutations) { // ADAPTED: WAWebBroadcastListSync.applyMutations uses yield Promise.all(t.map(...))
            var result = applyMutationResult(client, mutation);
            if (result.actionState() == SyncActionState.MALFORMED) { // WAWebBroadcastListSync.applyMutations: a++ on malformedActionValue
                malformedCount++;
            }
            results.add(result.actionState() == SyncActionState.SUCCESS);
        }
        if (malformedCount > 0) { // WAWebBroadcastListSync.applyMutations: a > 0 && o("WALogger").WARN(...)
            LOGGER.warning("broadcast list sync: " + malformedCount + " malformed mutations"); // WAWebBroadcastListSync.applyMutations: WALogger.WARN("broadcast list sync: N malformed mutations")
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
     *
     * @implNote WAWebBroadcastListSync.getBroadcastListMutation — four-argument convenience
     *           overload; the full WA Web signature {@code (listId, participants, listName,
     *           timestamp, audienceExpression)} is exposed separately.
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
     *
     * @implNote WAWebBroadcastListSync.getBroadcastListMutation — argument order in JS is
     *           {@code (t, n, r, a, i)} = {@code (listId, participants, listName, timestamp,
     *           audienceExpression)}.
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
        // WAWebBroadcastListSync.getBroadcastListMutation: var e = {businessBroadcastListAction: {participants: n, listName: r, labelIds: [], audienceExpression: serialize(i)}}
        var action = new BusinessBroadcastListActionBuilder()
                .participants(participants) // WAWebBroadcastListSync.getBroadcastListMutation: participants: n
                .listName(listName) // WAWebBroadcastListSync.getBroadcastListMutation: listName: r
                .labelIds(List.of()) // WAWebBroadcastListSync.getBroadcastListMutation: labelIds: []
                .audienceExpression(audienceExpression) // WAWebBroadcastListSync.getBroadcastListMutation: audienceExpression: WAWebAudienceExpressionTypes.serializeAudienceExpression(i)
                .build();
        // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp
                .businessBroadcastListAction(action) // WAWebBroadcastListSync.getBroadcastListMutation: businessBroadcastListAction
                .build();
        // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs))
        var index = JSON.toJSONString(List.of(actionName(), listId)); // WAWebSyncdActionUtils.buildPendingMutation: [getAction(), listId]
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, ... }
                index,
                value,
                SyncdOperation.SET, // WAWebBroadcastListSync.getBroadcastListMutation: operation: SET
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }

    /**
     * Builds a pending REMOVE mutation for deleting a business broadcast list.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastListSync.getDeleteBroadcastListMutation}),
     * this method creates a sync action value with an empty payload and builds
     * a REMOVE operation via {@code WAWebSyncdActionUtils.buildPendingMutation}.
     *
     * @implNote WAWebBroadcastListSync.getDeleteBroadcastListMutation
     * @param listId    the broadcast list identifier to remove
     * @param timestamp the mutation timestamp
     * @return a pending mutation ready for outbound sync
     */
    @WhatsAppWebExport(moduleName = "WAWebBroadcastListSync", exports = "getDeleteBroadcastListMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getDeleteBroadcastListMutation(String listId, Instant timestamp) {
        // WAWebBroadcastListSync.getDeleteBroadcastListMutation: buildPendingMutation({...value: {}, operation: REMOVE})
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp
                .build();
        var index = JSON.toJSONString(List.of(actionName(), listId)); // WAWebSyncdActionUtils.buildPendingMutation: [getAction(), listId]
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation
                index,
                value,
                SyncdOperation.REMOVE, // WAWebBroadcastListSync.getDeleteBroadcastListMutation: operation: REMOVE
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
