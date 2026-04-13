package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.business.BroadcastListParticipantAction;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastListAction;
import com.github.auties00.cobalt.model.sync.action.business.BusinessBroadcastListActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

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
public final class BusinessBroadcastListHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code BusinessBroadcastListHandler}.
     *
     * @implNote WAWebBroadcastListSync — module-level singleton: {@code var c = new u; l.default = c}
     */
    public static final BusinessBroadcastListHandler INSTANCE = new BusinessBroadcastListHandler();

    /**
     * Creates a new {@code BusinessBroadcastListHandler}.
     *
     * @implNote WAWebBroadcastListSync — singleton constructor via
     *           {@code AccountSyncdActionBase} inheritance
     */
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
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS; // ADAPTED: WAWebBroadcastListSync.applyMutations
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
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index()); // WAWebBroadcastListSync.applyMutations: var t = e.indexParts
        var listId = indexArray.getString(1); // WAWebBroadcastListSync.applyMutations: n = t[1]
        if (listId == null || listId.isEmpty()) { // WAWebBroadcastListSync.applyMutations: if (!n) return r.malformedActionIndex()
            return malformedActionIndex(); // WAWebBroadcastListSync.applyMutations: return r.malformedActionIndex()
        }

        // WAWebBroadcastListSync.applyMutations: if (!o("WAWebBizGatingUtils").isBizBroadcastSendWebEnabledNoExposure()) return {actionState: Unsupported}
        // ADAPTED: AB prop gating not replicated in Cobalt

        if (mutation.operation() == SyncdOperation.SET) { // WAWebBroadcastListSync.applyMutations: i.operation === "set" && "value" in i
            if (!(mutation.value().action().orElse(null) instanceof BusinessBroadcastListAction action)) { // WAWebBroadcastListSync.applyMutations: var u = s.businessBroadcastListAction; if (!u) return malformedActionValue
                return malformedActionValue(); // WAWebBroadcastListSync.applyMutations: return a++, o("WAWebSyncdIndexUtils").malformedActionValue(r.collectionName)
            }

            // WAWebBroadcastListSync.applyMutations: yield o("WAWebBroadcastListStorageUtils").updateBroadcastListStorage({audienceExpression: h, id: n, listName: m != null ? m : ""})
            // ADAPTED: Cobalt stores the protobuf action directly in a flat ConcurrentHashMap instead of
            // resolving audience expressions and writing to multiple IDB tables via WAWebBroadcastListStorageUtils
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
        // ADAPTED: Cobalt returns Failed instead of throwing (caught by outer try/catch in WA Web)
        return MutationApplicationResult.failed(); // WAWebBroadcastListSync.applyMutations: catch(e) { return {actionState: Failed} }
    }

    /**
     * Builds a pending SET mutation for creating or updating a business broadcast list.
     *
     * <p>Per WhatsApp Web ({@code WAWebBroadcastListSync.getBroadcastListMutation}),
     * this method creates a sync action value containing the business broadcast list
     * action with the specified participants, list name, empty label IDs, and
     * a serialized audience expression. The mutation is built as a SET operation
     * via {@code WAWebSyncdActionUtils.buildPendingMutation}.
     *
     * @implNote WAWebBroadcastListSync.getBroadcastListMutation
     * @param listId       the broadcast list identifier (index arg)
     * @param participants the list of broadcast list participants
     * @param listName     the name of the broadcast list
     * @param timestamp    the mutation timestamp
     * @return a pending mutation ready for outbound sync
     */
    public SyncPendingMutation getBroadcastListMutation(
            String listId,
            List<BroadcastListParticipantAction> participants,
            String listName,
            Instant timestamp
    ) {
        // WAWebBroadcastListSync.getBroadcastListMutation: var e = {businessBroadcastListAction: {participants: n, listName: r, labelIds: [], audienceExpression: serialize(i)}}
        var action = new BusinessBroadcastListActionBuilder()
                .participants(participants) // WAWebBroadcastListSync.getBroadcastListMutation: participants: n
                .listName(listName) // WAWebBroadcastListSync.getBroadcastListMutation: listName: r
                .labelIds(List.of()) // WAWebBroadcastListSync.getBroadcastListMutation: labelIds: []
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
