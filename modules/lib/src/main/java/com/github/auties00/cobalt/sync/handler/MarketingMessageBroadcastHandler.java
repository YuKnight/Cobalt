package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.MarketingMessageBroadcastAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.HashMap;

/**
 * Handles marketing message broadcast actions.
 *
 * <p>This handler processes mutations that associate sent message ids with
 * premium/marketing messages. Per WhatsApp Web {@code WAWebPremiumMessageBroadcastSync},
 * each successful mutation marks the {@code messageId} (the id of an actual
 * message that was sent) as belonging to the premium message template
 * identified by {@code premiumMessageId}.
 *
 * <p>Index format: {@code ["marketingMessageBroadcast", premiumMessageId, messageId]}
 *
 * <p>Cobalt stores the association in {@code AbstractWhatsAppStore.marketingMessageBroadcasts}
 * as a {@code Map<messageId, premiumMessageId>}. This is an architectural adaptation:
 * WhatsApp Web mutates the {@code sentMessageIds} {@code Set} stored on the premium
 * message model itself (and persists via {@code WAWebPremiumMessageAddSendAction}).
 * Cobalt's {@link MarketingMessageBroadcastAction} protobuf does not carry a
 * {@code sentMessageIds} field, so a side map is used instead.
 *
 * @implNote WAWebPremiumMessageBroadcastSync — concrete handler extending
 *           {@code AccountSyncdActionBase} with {@code collectionName = Regular},
 *           {@code getVersion() = 7} and
 *           {@code getAction() = WASyncdConst.Actions.MarketingMessageBroadcast}
 */
public final class MarketingMessageBroadcastHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code MarketingMessageBroadcastHandler}.
     *
     * @implNote WAWebPremiumMessageBroadcastSync — module-level
     *           {@code s = new e} singleton exported as {@code default}
     */
    public static final MarketingMessageBroadcastHandler INSTANCE = new MarketingMessageBroadcastHandler();

    /**
     * Constructs the singleton handler.
     *
     * @implNote WAWebPremiumMessageBroadcastSync — class constructor that
     *           initializes {@code collectionName = WASyncdConst.CollectionName.Regular}
     */
    private MarketingMessageBroadcastHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPremiumMessageBroadcastSync.getAction — returns
     *           {@code WASyncdConst.Actions.MarketingMessageBroadcast},
     *           which resolves to the literal {@code "marketingMessageBroadcast"}
     */
    @Override
    public String actionName() {
        return MarketingMessageBroadcastAction.ACTION_NAME; // WAWebPremiumMessageBroadcastSync.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPremiumMessageBroadcastSync — set in constructor as
     *           {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     */
    @Override
    public SyncPatchType collectionName() {
        return MarketingMessageBroadcastAction.COLLECTION_NAME; // WAWebPremiumMessageBroadcastSync constructor: collectionName = Regular
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPremiumMessageBroadcastSync.getVersion — returns the
     *           literal {@code 7}
     */
    @Override
    public int version() {
        return MarketingMessageBroadcastAction.ACTION_VERSION; // WAWebPremiumMessageBroadcastSync.getVersion: return 7
    }

    /**
     * Applies a marketing message broadcast mutation.
     *
     * <p>Boolean adapter on top of {@link #applyMutationResult}: returns
     * {@code true} only when the underlying result is {@code SUCCESS}.
     * {@code MALFORMED}, {@code UNSUPPORTED} and {@code ORPHAN} all map to
     * {@code false}, mirroring WhatsApp Web's batch loop where any non-Success
     * outcome is treated as not-applied.
     *
     * @implNote ADAPTED: WAWebPremiumMessageBroadcastSync.applyMutations —
     *           WhatsApp Web stores per-mutation states in an array; Cobalt
     *           collapses the {@code SUCCESS} case to {@code true} and every
     *           other state to {@code false}
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was successfully applied,
     *         {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebPremiumMessageBroadcastSync.applyMutations
    }

    /**
     * Applies a marketing message broadcast mutation and returns the detailed
     * outcome.
     *
     * <p>Per WhatsApp Web {@code WAWebPremiumMessageBroadcastSync.applyMutations}
     * the per-mutation logic is:
     * <ol>
     *   <li>Read {@code indexParts[1]} as {@code premiumMessageId} and
     *       {@code indexParts[2]} as {@code messageId}.</li>
     *   <li>If either is missing or empty, return
     *       {@code malformedActionIndex()}.</li>
     *   <li>If the operation is {@code "set"}, look up the premium message
     *       template via {@code PremiumMessageCollection.find(premiumMessageId)}.
     *       If it does not exist, return {@code {actionState: Orphan}}.
     *       Otherwise, queue the {@code (premiumMessageId, messageId)} pair
     *       for the batched persist (in WhatsApp Web this is
     *       {@code WAWebPremiumMessageAddSendAction}, which adds
     *       {@code messageId} to the premium message's
     *       {@code sentMessageIds} set and writes it back to the
     *       PremiumMessageCollection / IDB table) and return
     *       {@code {actionState: Success}}.</li>
     *   <li>For any other operation (e.g., {@code "remove"}) increment the
     *       unsupported counter (a no-op in WhatsApp Web because the value is
     *       discarded) and return {@code {actionState: Unsupported}}.</li>
     * </ol>
     *
     * <p>Cobalt diverges from WhatsApp Web in two ways, both intentional:
     * <ul>
     *   <li>The persist is per-mutation rather than batched. WhatsApp Web
     *       collects all queued pairs in a {@code n} array and calls
     *       {@code WAWebPremiumMessageAddSendAction(n)} once at the end of the
     *       batch. Cobalt updates the {@code marketingMessageBroadcasts} map
     *       eagerly because the underlying storage is a flat key/value map and
     *       there is no per-entity Set to mutate.</li>
     *   <li>WhatsApp Web's per-mutation {@code try/catch} that produces a
     *       {@code Failed} state is not replicated. Per Cobalt's error model,
     *       unexpected exceptions propagate to the orchestration layer instead
     *       of being mapped to a Failed state.</li>
     * </ul>
     *
     * @implNote WAWebPremiumMessageBroadcastSync.applyMutations — single
     *           mutation slice of the WhatsApp Web batch loop
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index()); // WAWebPremiumMessageBroadcastSync.applyMutations: var r = e.indexParts
        var premiumMessageId = indexArray.getString(1); // WAWebPremiumMessageBroadcastSync.applyMutations: i = r[1]
        var messageId = indexArray.getString(2); // WAWebPremiumMessageBroadcastSync.applyMutations: l = r[2]
        if (premiumMessageId == null || premiumMessageId.isEmpty()
                || messageId == null || messageId.isEmpty()) {
            return malformedActionIndex(); // WAWebPremiumMessageBroadcastSync.applyMutations: !i || !l ? t.malformedActionIndex()
        }

        if (mutation.operation() != SyncdOperation.SET) {
            // WAWebPremiumMessageBroadcastSync.applyMutations: (a++, {actionState: Unsupported})
            // The `a++` counter in WhatsApp Web is dead code (only used in `a > 0` which is itself a no-op expression).
            return MutationApplicationResult.unsupported();
        }

        // WAWebPremiumMessageBroadcastSync.applyMutations:
        // PremiumMessageCollection.find(i) == null ? {actionState: Orphan}
        // ADAPTED: Cobalt's marketingMessages map plays the role of PremiumMessageCollection;
        // containsKey() is the equivalent of WhatsApp Web's BaseCollection.find(id) == null check.
        if (!client.store().marketingMessages().containsKey(premiumMessageId)) {
            return MutationApplicationResult.orphan(); // WAWebPremiumMessageBroadcastSync.applyMutations: {actionState: Orphan}
        }

        // WAWebPremiumMessageBroadcastSync.applyMutations: n.push({messageId: l, premiumMessageId: i})
        // followed by `yield WAWebPremiumMessageAddSendAction(n)`, which (per
        // WAWebPremiumMessageAddSendAction) does:
        //     var r = PremiumMessageCollection.get(premiumMessageId);
        //     if (r) { var a = new Set(r.sentMessageIds); a.add(messageId); r.set("sentMessageIds", a); }
        //     ...bulkCreateOrMerge to persist...
        // ADAPTED: MarketingMessageAction has no `sentMessageIds` field, so the association is
        // tracked in a side map keyed by messageId. The eager copy-then-replace pattern
        // mirrors how Cobalt's other handlers update store maps without holding the
        // collection lock.
        var broadcasts = new HashMap<>(client.store().marketingMessageBroadcasts()); // ADAPTED: WAWebPremiumMessageAddSendAction
        broadcasts.put(messageId, premiumMessageId); // ADAPTED: WAWebPremiumMessageAddSendAction: a.add(n.messageId)
        client.store().setMarketingMessageBroadcasts(broadcasts); // ADAPTED: WAWebPremiumMessageAddSendAction: bulkCreateOrMerge
        return MutationApplicationResult.success(); // WAWebPremiumMessageBroadcastSync.applyMutations: {actionState: Success}
    }
}
