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
        return ChatAssignmentAction.ACTION_NAME;
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
        return ChatAssignmentAction.COLLECTION_NAME;
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
        return ChatAssignmentAction.ACTION_VERSION;
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
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
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
        try {
            var indexArray = JSON.parseArray(mutation.index());
            var chatJidString = indexArray.getString(1);
            if (chatJidString == null || chatJidString.isEmpty()) {
                return malformedActionIndex();
            }

            if (mutation.operation() != SyncdOperation.SET) {
                return MutationApplicationResult.unsupported();
            }

            if (!(mutation.value().action().orElse(null) instanceof ChatAssignmentAction action)) {
                return malformedActionValue();
            }

            var agentId = action.deviceAgentID().orElse("");
            if (!agentId.isEmpty() && !client.store().agentStates().containsKey(agentId)) {
                return MutationApplicationResult.orphan(agentId, "Agent");
            }

            var chatJid = Jid.of(chatJidString);
            var chat = client.store().findChatByJid(chatJid); // ADAPTED: WAWebChatAssignmentSync.applyMutations: resolveChatForMutationIndex, Cobalt uses findChatByJid
            if (chat.isEmpty()) {
                return MutationApplicationResult.orphan(chatJidString, "Chat");
            }

            var resolvedChatJid = chat.get().toJid().toString();
            var states = new HashMap<>(client.store().chatAssignmentStates()); // ADAPTED: WAWebChatAssignmentSync.applyMutations, batch bulkCreateOrMerge/bulkRemove/processChatAssignments simplified to inline map update
            if (agentId.isEmpty()) {
                states.remove(resolvedChatJid); // ADAPTED: WAWebChatAssignmentSync.applyMutations: getAgentCollectionForChatId(_).filter(e => e.id !== d).forEach(e => l.push(...)) + bulkRemove(l), removes all existing assignments for this chat
            } else {
                states.put(resolvedChatJid, agentId); // ADAPTED: WAWebChatAssignmentSync.applyMutations: i.push({id: _.toJid()+"_"+d, chatId: _.toJid(), agentId: d, chatOpenedByAgent: false}) + bulkCreateOrMerge(i)
            }
            client.store().setChatAssignmentStates(states); // ADAPTED: WAWebChatAssignmentSync.applyMutations: yield getChatAssignmentTable().bulkCreateOrMerge(i) + processChatAssignments(i) + bulkRemove(l) + remove(l)
            return MutationApplicationResult.success();
        } catch (Exception e) {
            return MutationApplicationResult.failed();
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
        var action = new ChatAssignmentActionBuilder()
                .deviceAgentID(agentId)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .chatAssignment(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName(), chatJid.toString()));
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0);
    }
}
