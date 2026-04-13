package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.AgentAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles agent sync actions for managing business account agents (device agents).
 *
 * <p>This handler processes mutations that manage agent/bot assignments.
 * Per WhatsApp Web, the handler belongs to the {@code Regular} collection,
 * uses version {@code 7}, and routes on action name {@code "deviceAgent"}.
 *
 * <p>Index format: {@code ["deviceAgent", agentId]}
 *
 * <p>On {@code SET}, the handler validates that {@code indexParts[1]} (the agentId)
 * is present and that the protobuf {@code agentAction} field is non-null, then
 * merges the agent into the store (even when {@code isDeleted} is {@code true}).
 * On {@code REMOVE}, only the agentId is validated, and the agent is removed
 * from the store.
 *
 * @implNote WAWebAgentSync.default — singleton instance of the agent sync handler
 *           extending {@code WAWebSyncdAction.AccountSyncdActionBase}
 */
public final class AgentActionHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code AgentActionHandler}.
     *
     * @implNote WAWebAgentSync.default — {@code var c = new u(); l.default = c}
     */
    public static final AgentActionHandler INSTANCE = new AgentActionHandler();

    /**
     * Creates the singleton agent action handler.
     *
     * @implNote WAWebAgentSync — constructor of class {@code u} extending
     *           {@code AccountSyncdActionBase}
     */
    private AgentActionHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebAgentSync.getAction — returns {@code WASyncdConst.Actions.Agent}
     *           (value: {@code "deviceAgent"})
     */
    @Override
    public String actionName() {
        return AgentAction.ACTION_NAME; // WAWebAgentSync.getAction -> WASyncdConst.Actions.Agent = "deviceAgent"
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebAgentSync — {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     *           (value: {@code "regular"})
     */
    @Override
    public SyncPatchType collectionName() {
        return AgentAction.COLLECTION_NAME; // WAWebAgentSync.collectionName = WASyncdConst.CollectionName.Regular = "regular"
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebAgentSync.getVersion — returns {@code 7}
     */
    @Override
    public int version() {
        return AgentAction.ACTION_VERSION; // WAWebAgentSync.getVersion -> 7
    }

    /**
     * Applies an agent mutation and returns whether it succeeded.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and checks if the result state is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebAgentSync.applyMutations — WA Web returns
     *           {@code WASyncdConst.SyncActionState.Success} directly; Cobalt wraps
     *           in {@link MutationApplicationResult} and extracts the boolean here
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS; // WAWebAgentSync.applyMutations -> SyncActionState.Success
    }

    /**
     * Applies an agent mutation and returns the detailed result.
     *
     * <p>Per WhatsApp Web, the handler validates the mutation content via
     * {@code getValidatedContentSet} / {@code getValidatedContentRemove}, then:
     * <ul>
     *   <li>For {@code REMOVE}: removes the agent from the store by agentId</li>
     *   <li>For {@code SET}: merges the agent into the store with all its fields
     *       (including {@code isDeleted}), regardless of the deleted flag</li>
     * </ul>
     *
     * <p>After all mutations in a batch, WA Web also reconciles unattributed messages
     * with the agent collection via a post-processing step. That reconciliation is
     * intentionally omitted in Cobalt because unattributed message tracking is not
     * implemented.
     *
     * @implNote WAWebAgentSync.applyMutations — per-mutation logic within
     *           {@code withValidatedContent} callback, using
     *           {@code getValidatedContentSet} and {@code getValidatedContentRemove}
     *           for validation, and {@code WAWebSchemaAgent.getAgentTable().bulkCreateOrMerge} /
     *           {@code bulkRemove} plus {@code WAWebAgentCollection.AgentCollection.add/remove}
     *           for persistence
     * @param client   the WhatsApp client
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index()); // WAWebAgentSync.getValidatedContentSet/getValidatedContentRemove — indexParts
        var agentId = indexArray.getString(1); // WAWebAgentSync — var n = t[1]
        if (agentId == null || agentId.isEmpty()) { // WAWebAgentSync — if (!n) return {result: "malformed_index"}
            return malformedActionIndex(); // WAWebAgentSync.getValidatedContentSet/getValidatedContentRemove -> {result: "malformed_index"}
        }

        var states = new java.util.HashMap<>(client.store().agentStates()); // ADAPTED: WAWebAgentCollection/WAWebSchemaAgent — Cobalt uses ConcurrentHashMap store
        if (mutation.operation() == SyncdOperation.REMOVE) { // WAWebAgentSync.applyMutations — n.operation === "remove"
            states.remove(agentId); // WAWebAgentSync.applyMutations — i.push(t) -> AgentCollection.remove(i)
            client.store().setAgentStates(states);
            return MutationApplicationResult.success(); // WAWebAgentSync.applyMutations — {actionState: SyncActionState.Success}
        }

        if (mutation.operation() != SyncdOperation.SET) { // NO_WA_BASIS — defensive guard for unknown operation types
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof AgentAction action)) { // WAWebAgentSync.getValidatedContentSet — var r = e.value.agentAction; return r ? ... : {result: "malformed_value"}
            return malformedActionValue(); // WAWebAgentSync.getValidatedContentSet -> {result: "malformed_value"}
        }

        states.put(agentId, action); // WAWebAgentSync.applyMutations — a.push({id:t, name:c, deviceId:..., isDeleted:!!u.isDeleted}) -> AgentCollection.add(a, {merge: true})
        client.store().setAgentStates(states);
        return MutationApplicationResult.success(); // WAWebAgentSync.applyMutations — {actionState: SyncActionState.Success}
    }
}
