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
import com.github.auties00.cobalt.model.sync.action.chat.ChatAssignmentOpenedStatusAction;
import com.github.auties00.cobalt.model.sync.action.chat.ChatAssignmentOpenedStatusActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

/**
 * Handles chat assignment opened status sync actions.
 *
 * <p>This handler processes mutations that track whether an assigned chat
 * has been opened by the agent. It corresponds to the
 * {@code "agentChatAssignmentOpenedStatus"} action in the {@code Regular}
 * collection.
 *
 * <p>Index format: {@code ["agentChatAssignmentOpenedStatus", "chatJid", "agentId"]}
 *
 * <p>Per WhatsApp Web, this handler extends {@code ChatSyncdActionBase}
 * with {@code chatJidIndex = 1} and collection {@code Regular}.
 *
 * @implNote WAWebChatAssignmentOpenedStatusSync.default
 */
@WhatsAppWebModule(moduleName = "WAWebChatAssignmentOpenedStatusSync")
public final class ChatAssignmentOpenedStatusHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code ChatAssignmentOpenedStatusHandler}.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync.default, the module exports
     *           a singleton {@code new s()} where {@code s} is the handler class
     */
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentOpenedStatusSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final ChatAssignmentOpenedStatusHandler INSTANCE = new ChatAssignmentOpenedStatusHandler();

    /**
     * Creates a new {@code ChatAssignmentOpenedStatusHandler}.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync.default, constructor sets
     *           {@code chatJidIndex = 1} and {@code collectionName = Regular}
     */
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentOpenedStatusSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private ChatAssignmentOpenedStatusHandler() {

    }

    /**
     * Returns the action name for chat assignment opened status sync.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync.getAction --
     *           returns {@code WASyncdConst.Actions.ChatAssignmentOpenedStatus}
     *           which is {@code "agentChatAssignmentOpenedStatus"}
     * @return the action name string
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentOpenedStatusSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return ChatAssignmentOpenedStatusAction.ACTION_NAME; // WAWebChatAssignmentOpenedStatusSync.getAction
    }

    /**
     * Returns the sync collection for this handler.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync constructor --
     *           sets {@code collectionName = WASyncdConst.CollectionName.Regular}
     * @return the {@link SyncPatchType#REGULAR} collection
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentOpenedStatusSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return ChatAssignmentOpenedStatusAction.COLLECTION_NAME; // WAWebChatAssignmentOpenedStatusSync.collectionName
    }

    /**
     * Returns the mutation format version for chat assignment opened status.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync.getVersion --
     *           returns {@code WASyncdConst.CHAT_ASSIGNMENT_SYNC_VERSION} which is {@code 7}
     * @return the version number
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentOpenedStatusSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return ChatAssignmentOpenedStatusAction.ACTION_VERSION; // WAWebChatAssignmentOpenedStatusSync.getVersion
    }

    /**
     * Applies a single chat assignment opened status mutation.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync.applyMutations, delegates to
     *           {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)} and checks for success
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentOpenedStatusSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // WAWebChatAssignmentOpenedStatusSync.applyMutations
    }

    /**
     * Applies a single chat assignment opened status mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code applyMutations}: for each mutation in the batch,
     * extracts {@code indexParts[1]} as the chat JID and {@code indexParts[2]} as
     * the agent ID. If either is absent, returns malformed. For {@code SET} operations,
     * resolves the chat, extracts the {@code chatAssignmentOpenedStatus.chatOpened}
     * value, verifies the assignment exists in the ChatAssignment collection, and
     * accumulates the update. Non-SET operations return unsupported.
     *
     * <p>After all mutations are processed, WA Web calls
     * {@code WAWebBizChatAssignmentOpenedAction.updateLocalOpenedState(accumulator)}
     * which updates the ChatAssignment collection models and bulk-merges to IDB.
     * In Cobalt, the store update is applied inline per mutation via the
     * {@code chatAssignmentOpenedStates} map.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync.applyMutations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentOpenedStatusSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        try { // WAWebChatAssignmentOpenedStatusSync.applyMutations: try { ... } catch(e) { return {actionState: Failed} }
            var indexArray = JSON.parseArray(mutation.index()); // WAWebChatAssignmentOpenedStatusSync.applyMutations: var t = e.indexParts
            var chatJidString = indexArray.getString(1); // WAWebChatAssignmentOpenedStatusSync.applyMutations: n = t[1]
            var agentId = indexArray.getString(2); // WAWebChatAssignmentOpenedStatusSync.applyMutations: i = t[2]
            if (chatJidString == null || agentId == null) { // WAWebChatAssignmentOpenedStatusSync.applyMutations: if (n == null || i == null)
                return malformedActionIndex(); // WAWebChatAssignmentOpenedStatusSync.applyMutations: return r.malformedActionIndex()
            }

            if (mutation.operation() != SyncdOperation.SET) { // WAWebChatAssignmentOpenedStatusSync.applyMutations: if (e.operation === "set")
                return MutationApplicationResult.unsupported(); // WAWebChatAssignmentOpenedStatusSync.applyMutations: return {actionState: Unsupported}
            }

            var chatJid = Jid.of(chatJidString); // WAWebChatAssignmentOpenedStatusSync.applyMutations: o("WAWebWidFactory").createWid(n)
            var chat = client.store().findChatByJid(chatJid); // ADAPTED: WAWebChatAssignmentOpenedStatusSync.applyMutations: resolveChatForMutationIndex(createWid(n))
            if (chat.isEmpty()) { // WAWebChatAssignmentOpenedStatusSync.applyMutations: if (!l.success) return {actionState: Orphan, orphanModel: l.orphanModel}
                return MutationApplicationResult.orphan(chatJidString, "Chat");
            }

            if (!(mutation.value().action().orElse(null) instanceof ChatAssignmentOpenedStatusAction action)) { // WAWebChatAssignmentOpenedStatusSync.applyMutations: var u = e.value.chatAssignmentOpenedStatus; if (!u) return malformedActionValue
                return malformedActionValue(); // WAWebChatAssignmentOpenedStatusSync.applyMutations: return malformedActionValue(r.collectionName)
            }

            var resolvedChatJid = chat.get().toJid().toString(); // WAWebChatAssignmentOpenedStatusSync.applyMutations: var s = createWid(l.chat.id); s.toJid()
            var assignmentKey = resolvedChatJid + "_" + agentId; // WAWebChatAssignmentOpenedStatusSync.applyMutations: var d = s.toJid() + "_" + i

            if (!client.store().chatAssignmentOpenedStates().containsKey(assignmentKey) // ADAPTED: WAWebChatAssignmentOpenedStatusSync.applyMutations: var m = ChatAssignmentCollection.get(d); if (m == null)
                    && !agentId.equals(client.store().chatAssignmentStates().get(resolvedChatJid))) { // ADAPTED: fallback check via chatAssignmentStates
                return MutationApplicationResult.orphan(assignmentKey, "ChatAssignment"); // WAWebChatAssignmentOpenedStatusSync.applyMutations: return {actionState: Orphan, orphanModel: {modelId: d, modelType: ChatAssignment}}
            }

            var chatOpened = action.chatOpened(); // ADAPTED: WAWebChatAssignmentOpenedStatusSync.applyMutations: var c = u.chatOpened; if (c == null) return malformedActionValue, Cobalt coalesces null to false per project convention
            var states = new HashMap<>(client.store().chatAssignmentOpenedStates()); // ADAPTED: WAWebChatAssignmentOpenedStatusSync.applyMutations: a.push({...}); updateLocalOpenedState(a), inline update instead of batch
            states.put(assignmentKey, chatOpened); // WAWebBizChatAssignmentOpenedAction.updateLocalOpenedState: r.set("chatOpenedByAgent", t)
            client.store().setChatAssignmentOpenedStates(states); // WAWebBizChatAssignmentOpenedAction.updateLocalOpenedState: t.bulkCreateOrMerge(e)
            return MutationApplicationResult.success(); // WAWebChatAssignmentOpenedStatusSync.applyMutations: return {actionState: Success}
        } catch (Exception e) { // WAWebChatAssignmentOpenedStatusSync.applyMutations: catch(e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebChatAssignmentOpenedStatusSync.applyMutations: return {actionState: Failed}
        }
    }

    /**
     * Builds a pending SET mutation that records whether the given agent has
     * opened the given chat.
     *
     * <p>Per WhatsApp Web
     * {@code WAWebChatAssignmentOpenedStatusSync.default.createChatOpenedMutations},
     * the mutation carries a {@link ChatAssignmentOpenedStatusAction} whose
     * {@code chatOpened} flag records the agent's open state. The index is
     * {@code ["agentChatAssignmentOpenedStatus", chatJid, agentId]}.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync.default.createChatOpenedMutations
     * @param chatJid     the JID of the chat
     * @param agentId     the agent identifier
     * @param chatOpened  {@code true} when the agent has opened the chat
     * @param timestamp   the mutation timestamp
     * @return the pending mutation for the opened-state change
     */
    @WhatsAppWebExport(moduleName = "WAWebChatAssignmentOpenedStatusSync", exports = "createChatOpenedMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation createChatOpenedMutation(
            Jid chatJid,
            String agentId,
            boolean chatOpened,
            Instant timestamp
    ) {
        var action = new ChatAssignmentOpenedStatusActionBuilder() // WAWebChatAssignmentOpenedStatusSync.createChatOpenedMutations: {chatAssignmentOpenedStatus: {chatOpened: i}}
                .chatOpened(chatOpened) // WAWebChatAssignmentOpenedStatusSync.createChatOpenedMutations: chatOpened: i
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp
                .chatAssignmentOpenedStatus(action) // WAWebChatAssignmentOpenedStatusSync.createChatOpenedMutations: value: {chatAssignmentOpenedStatus: ...}
                .build();
        var index = JSON.toJSONString(List.of(actionName(), chatJid.toString(), agentId)); // WAWebChatAssignmentOpenedStatusSync.createChatOpenedMutations: indexArgs: [getChatJidMutationIndexForChat(chatWid, ...), t]
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version() // WAWebChatAssignmentOpenedStatusSync.createChatOpenedMutations: version: CHAT_ASSIGNMENT_SYNC_VERSION
        );
        return new SyncPendingMutation(mutation, 0);
    }
}
