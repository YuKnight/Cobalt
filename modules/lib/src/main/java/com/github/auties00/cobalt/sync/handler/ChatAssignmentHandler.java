package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.ChatAssignmentAction;
import com.github.auties00.cobalt.model.sync.action.chat.ChatAssignmentActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

/**
 * Handles chat assignment sync actions.
 *
 * <p>This handler processes mutations that assign chats to agents
 * in business accounts. It corresponds to the {@code "agentChatAssignment"}
 * action in the {@code Regular} collection.
 *
 * <p>Per WhatsApp Web, this handler extends {@code ChatSyncdActionBase}
 * with {@code chatJidIndex = 1} and collection {@code Regular}.
 *
 * <p>Index format: {@code ["agentChatAssignment", "chatJid"]}
 *
 * @implNote WAWebChatAssignmentSync.default
 */
@WhatsAppWebModule(moduleName = "WAWebChatAssignmentSync")
public final class ChatAssignmentHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code ChatAssignmentHandler}.
     *
     * @implNote WAWebChatAssignmentSync.default, the module exports
     *           a singleton {@code new s()} where {@code s} is the handler class
     */
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final ChatAssignmentHandler INSTANCE = new ChatAssignmentHandler();

    /**
     * Creates a new {@code ChatAssignmentHandler}.
     *
     * @implNote WAWebChatAssignmentSync.default, constructor sets
     *           {@code chatJidIndex = 1} and
     *           {@code collectionName = WASyncdConst.CollectionName.Regular}
     */
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private ChatAssignmentHandler() {

    }

    /**
     * Returns the action name for chat assignment sync.
     *
     * @implNote WAWebChatAssignmentSync.getAction --
     *           returns {@code WASyncdConst.Actions.ChatAssignment}
     *           which is {@code "agentChatAssignment"}
     * @return the action name string
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return ChatAssignmentAction.ACTION_NAME; // WAWebChatAssignmentSync.getAction
    }

    /**
     * Returns the sync collection for this handler.
     *
     * @implNote WAWebChatAssignmentSync constructor --
     *           sets {@code collectionName = WASyncdConst.CollectionName.Regular}
     * @return the {@link SyncPatchType#REGULAR} collection
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return ChatAssignmentAction.COLLECTION_NAME; // WAWebChatAssignmentSync.collectionName
    }

    /**
     * Returns the mutation format version for chat assignment.
     *
     * @implNote WAWebChatAssignmentSync.getVersion --
     *           returns {@code WASyncdConst.CHAT_ASSIGNMENT_SYNC_VERSION} which is {@code 7}
     * @return the version number
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return ChatAssignmentAction.ACTION_VERSION; // WAWebChatAssignmentSync.getVersion
    }

    /**
     * Applies a single chat assignment mutation.
     *
     * @implNote WAWebChatAssignmentSync.applyMutations, delegates to
     *           {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     *           and checks for success
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS; // WAWebChatAssignmentSync.applyMutations
    }

    /**
     * Applies a single chat assignment mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code applyMutations}: for each mutation in the batch,
     * extracts {@code indexParts[1]} as the chat JID. If absent, returns malformed.
     * For {@code SET} operations, extracts the {@code chatAssignment.deviceAgentID},
     * verifies the agent exists in the agent collection (when non-empty), resolves
     * the chat, removes existing assignments for that chat, and adds the new
     * assignment. Non-SET operations return unsupported.
     *
     * <p>After all mutations are processed, WA Web calls batch store operations
     * ({@code bulkCreateOrMerge}, {@code bulkRemove}), collection updates
     * ({@code processChatAssignments}, {@code remove}), system message creation,
     * notification triggering, and orphan checking. In Cobalt, the store update
     * is applied inline per mutation via the {@code chatAssignmentStates} map.
     *
     * @implNote WAWebChatAssignmentSync.applyMutations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        try { // WAWebChatAssignmentSync.applyMutations: try { ... } catch(e) { return {actionState: Failed} }
            var indexArray = JSON.parseArray(mutation.index()); // WAWebChatAssignmentSync.applyMutations: var t = e.indexParts
            var chatJidString = indexArray.getString(1); // WAWebChatAssignmentSync.applyMutations: n = t[1]
            if (chatJidString == null || chatJidString.isEmpty()) { // WAWebChatAssignmentSync.applyMutations: if (!n) return a.malformedActionIndex()
                return malformedActionIndex(); // WAWebChatAssignmentSync.applyMutations: return a.malformedActionIndex()
            }

            if (mutation.operation() != SyncdOperation.SET) { // WAWebChatAssignmentSync.applyMutations: if (e.operation === "set") { ... } return {actionState: Unsupported}
                return MutationApplicationResult.unsupported(); // WAWebChatAssignmentSync.applyMutations: return {actionState: Unsupported}
            }

            if (!(mutation.value().action().orElse(null) instanceof ChatAssignmentAction action)) { // WAWebChatAssignmentSync.applyMutations: var c = e.value.chatAssignment; if (!c) return malformedActionValue
                return malformedActionValue(); // WAWebChatAssignmentSync.applyMutations: return o("WAWebSyncdIndexUtils").malformedActionValue(a.collectionName)
            }

            var agentId = action.deviceAgentID().orElse(""); // WAWebChatAssignmentSync.applyMutations: var d = (u = c.deviceAgentID) != null ? u : ""
            if (!agentId.isEmpty() && !client.store().agentStates().containsKey(agentId)) { // WAWebChatAssignmentSync.applyMutations: var m = o("WAWebAgentCollection").AgentCollection.get(d); if (d !== "" && m == null)
                return MutationApplicationResult.orphan(agentId, "Agent"); // WAWebChatAssignmentSync.applyMutations: return {actionState: Orphan, orphanModel: {modelId: d, modelType: Agent}}
            }

            var chatJid = Jid.of(chatJidString); // WAWebChatAssignmentSync.applyMutations: yield o("WAWebSyncdGetChat").resolveChatForMutationIndex(o("WAWebWidFactory").createWid(n))
            var chat = client.store().findChatByJid(chatJid); // ADAPTED: WAWebChatAssignmentSync.applyMutations: resolveChatForMutationIndex, Cobalt uses findChatByJid
            if (chat.isEmpty()) { // WAWebChatAssignmentSync.applyMutations: if (!p.success) return {actionState: Orphan, orphanModel: p.orphanModel}
                return MutationApplicationResult.orphan(chatJidString, "Chat"); // WAWebChatAssignmentSync.applyMutations: return {actionState: Orphan, orphanModel: p.orphanModel}
            }

            var resolvedChatJid = chat.get().toJid().toString(); // WAWebChatAssignmentSync.applyMutations: var _ = o("WAWebWidFactory").createWid(p.chat.id); _.toJid()
            var states = new HashMap<>(client.store().chatAssignmentStates()); // ADAPTED: WAWebChatAssignmentSync.applyMutations, batch bulkCreateOrMerge/bulkRemove/processChatAssignments simplified to inline map update
            if (agentId.isEmpty()) { // WAWebChatAssignmentSync.applyMutations: d !== "" && i.push({...}), empty agentId means unassign (no add), only remove existing
                states.remove(resolvedChatJid); // ADAPTED: WAWebChatAssignmentSync.applyMutations: getAgentCollectionForChatId(_).filter(e => e.id !== d).forEach(e => l.push(...)) + bulkRemove(l), removes all existing assignments for this chat
            } else {
                states.put(resolvedChatJid, agentId); // ADAPTED: WAWebChatAssignmentSync.applyMutations: i.push({id: _.toJid()+"_"+d, chatId: _.toJid(), agentId: d, chatOpenedByAgent: false}) + bulkCreateOrMerge(i)
            }
            client.store().setChatAssignmentStates(states); // ADAPTED: WAWebChatAssignmentSync.applyMutations: yield getChatAssignmentTable().bulkCreateOrMerge(i) + processChatAssignments(i) + bulkRemove(l) + remove(l)
            // WAWebChatAssignmentSync.applyMutations: createChatAssignmentSystemMsgs(s), system messages skipped (business UI feature)
            // WAWebChatAssignmentSync.applyMutations: triggerChatAssignmentNotification(i, ...), notifications skipped (business UI feature)
            // WAWebChatAssignmentSync.applyMutations: checkOrphanChatAssignments(c), orphan re-check skipped (handled at higher level in Cobalt)
            return MutationApplicationResult.success(); // WAWebChatAssignmentSync.applyMutations: return {actionState: Success}
        } catch (Exception e) { // WAWebChatAssignmentSync.applyMutations: catch(e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebChatAssignmentSync.applyMutations: return {actionState: Failed}
        }
    }

    /**
     * Builds a pending SET mutation that assigns the given chat to the given
     * agent.
     *
     * <p>Per WhatsApp Web
     * {@code WAWebChatAssignmentSync.default.createChatAssignmentMutations},
     * the mutation carries a {@link ChatAssignmentAction} whose
     * {@code deviceAgentID} is the target agent id (an empty string
     * unassigns the chat). The mutation index is
     * {@code ["agentChatAssignment", chatJid]}.
     *
     * @implNote WAWebChatAssignmentSync.default.createChatAssignmentMutations
     * @param chatJid   the JID of the chat being assigned
     * @param agentId   the target agent id, or {@code ""} to unassign
     * @param timestamp the mutation timestamp
     * @return the pending mutation for the chat assignment
     */
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentSync", exports = "createChatAssignmentMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation createChatAssignmentMutation(
            Jid chatJid,
            String agentId,
            Instant timestamp
    ) {
        var action = new ChatAssignmentActionBuilder() // WAWebChatAssignmentSync.createChatAssignmentMutations: {chatAssignment: {deviceAgentID: t}}
                .deviceAgentID(agentId) // WAWebChatAssignmentSync.createChatAssignmentMutations: deviceAgentID: t
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp
                .chatAssignment(action) // WAWebChatAssignmentSync.createChatAssignmentMutations: value: {chatAssignment: ...}
                .build();
        var index = JSON.toJSONString(List.of(actionName(), chatJid.toString())); // WAWebChatAssignmentSync.createChatAssignmentMutations: indexArgs: [getChatJidMutationIndexForChat(chatWid, ChatAssignment)]
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET, // WAWebChatAssignmentSync.createChatAssignmentMutations: SyncdMutation$SyncdOperation.SET
                timestamp,
                version() // WAWebChatAssignmentSync.createChatAssignmentMutations: version: CHAT_ASSIGNMENT_SYNC_VERSION
        );
        return new SyncPendingMutation(mutation, 0);
    }
}
