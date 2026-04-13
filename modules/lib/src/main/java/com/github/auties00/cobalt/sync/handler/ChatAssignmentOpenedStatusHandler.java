package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.ChatAssignmentOpenedStatusAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

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
public final class ChatAssignmentOpenedStatusHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code ChatAssignmentOpenedStatusHandler}.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync.default -- the module exports
     *           a singleton {@code new s()} where {@code s} is the handler class
     */
    public static final ChatAssignmentOpenedStatusHandler INSTANCE = new ChatAssignmentOpenedStatusHandler();

    /**
     * Creates a new {@code ChatAssignmentOpenedStatusHandler}.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync.default -- constructor sets
     *           {@code chatJidIndex = 1} and {@code collectionName = Regular}
     */
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
    public int version() {
        return ChatAssignmentOpenedStatusAction.ACTION_VERSION; // WAWebChatAssignmentOpenedStatusSync.getVersion
    }

    /**
     * Applies a single chat assignment opened status mutation.
     *
     * @implNote WAWebChatAssignmentOpenedStatusSync.applyMutations -- delegates to
     *           {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)} and checks for success
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
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

            var chatOpened = action.chatOpened(); // ADAPTED: WAWebChatAssignmentOpenedStatusSync.applyMutations: var c = u.chatOpened; if (c == null) return malformedActionValue -- Cobalt coalesces null to false per project convention
            var states = new java.util.HashMap<>(client.store().chatAssignmentOpenedStates()); // ADAPTED: WAWebChatAssignmentOpenedStatusSync.applyMutations: a.push({...}); updateLocalOpenedState(a) -- inline update instead of batch
            states.put(assignmentKey, chatOpened); // WAWebBizChatAssignmentOpenedAction.updateLocalOpenedState: r.set("chatOpenedByAgent", t)
            client.store().setChatAssignmentOpenedStates(states); // WAWebBizChatAssignmentOpenedAction.updateLocalOpenedState: t.bulkCreateOrMerge(e)
            return MutationApplicationResult.success(); // WAWebChatAssignmentOpenedStatusSync.applyMutations: return {actionState: Success}
        } catch (Exception e) { // WAWebChatAssignmentOpenedStatusSync.applyMutations: catch(e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebChatAssignmentOpenedStatusSync.applyMutations: return {actionState: Failed}
        }
    }
}
