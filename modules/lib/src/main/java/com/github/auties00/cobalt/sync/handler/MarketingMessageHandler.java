package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.business.MarketingMessageAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.util.HashMap;

/**
 * Handles marketing (a.k.a. premium) message template actions.
 *
 * <p>This handler processes mutations related to premium/marketing message
 * templates. Each mutation persists or updates a {@link MarketingMessageAction}
 * keyed by a stable {@code messageId} parsed from the mutation index.
 *
 * <p>Index format: {@code ["marketingMessage", messageId]}
 *
 * <p>Per WhatsApp Web {@code WAWebPremiumMessageSync.applyMutations}, on a
 * {@code "set"} operation the web client validates that
 * {@code value.marketingMessageAction} is present and that {@code type} is
 * non-{@code null}, then accumulates a row to be persisted via
 * {@code WAWebPremiumMessageSchema.getPremiumMessageTable().bulkCreateOrMerge}
 * and added to the in-memory {@code PremiumMessageCollection}. Any other
 * operation maps to {@code SyncActionState.Unsupported} and a missing index
 * maps to {@code malformedActionIndex()}.
 *
 * <p>Cobalt collapses both the IDB table and the in-memory collection into
 * {@code AbstractWhatsAppStore.marketingMessages}, a flat
 * {@code Map<messageId, MarketingMessageAction>}. The map is updated eagerly
 * per mutation, mirroring how the other Cobalt sync handlers update their
 * store maps without holding a per-entity lock.
 *
 * @implNote WAWebPremiumMessageSync — concrete handler extending
 *           {@code AccountSyncdActionBase} with
 *           {@code collectionName = WASyncdConst.CollectionName.Regular},
 *           {@code getVersion() = 7} and
 *           {@code getAction() = WASyncdConst.Actions.MarketingMessage}
 */
@WhatsAppWebModule(moduleName = "WAWebPremiumMessageSync")
public final class MarketingMessageHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code MarketingMessageHandler}.
     *
     * @implNote WAWebPremiumMessageSync — module-level
     *           {@code s = new e} singleton exported as {@code default}
     */
    @WhatsAppWebExport(moduleName = "WAWebPremiumMessageSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final MarketingMessageHandler INSTANCE = new MarketingMessageHandler();

    /**
     * Constructs the singleton handler.
     *
     * @implNote WAWebPremiumMessageSync — class constructor that initializes
     *           {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     */
    @WhatsAppWebExport(moduleName = "WAWebPremiumMessageSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private MarketingMessageHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPremiumMessageSync.getAction — returns
     *           {@code WASyncdConst.Actions.MarketingMessage}, which resolves
     *           to the literal {@code "marketingMessage"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPremiumMessageSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return MarketingMessageAction.ACTION_NAME; // WAWebPremiumMessageSync.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPremiumMessageSync — set in constructor as
     *           {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPremiumMessageSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return MarketingMessageAction.COLLECTION_NAME; // WAWebPremiumMessageSync constructor: collectionName = Regular
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPremiumMessageSync.getVersion — returns the literal
     *           {@code 7}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPremiumMessageSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return MarketingMessageAction.ACTION_VERSION; // WAWebPremiumMessageSync.getVersion: return 7
    }

    /**
     * Applies a marketing message mutation.
     *
     * <p>Boolean adapter on top of {@link #applyMutationResult}: returns
     * {@code true} only when the underlying result is {@code SUCCESS}.
     * {@code MALFORMED} and {@code UNSUPPORTED} both map to {@code false},
     * mirroring WhatsApp Web's batch loop where any non-Success outcome is
     * treated as not-applied.
     *
     * @implNote ADAPTED: WAWebPremiumMessageSync.applyMutations — WhatsApp Web
     *           stores per-mutation states in an array; Cobalt collapses the
     *           {@code SUCCESS} case to {@code true} and every other state to
     *           {@code false}
     * @param client   the {@link WhatsAppClient} instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was successfully applied,
     *         {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPremiumMessageSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebPremiumMessageSync.applyMutations
    }

    /**
     * Applies a marketing message mutation and returns the detailed outcome.
     *
     * <p>Per WhatsApp Web {@code WAWebPremiumMessageSync.applyMutations} the
     * per-mutation logic is:
     * <ol>
     *   <li>Read {@code indexParts[1]} as {@code messageId}. If absent, return
     *       {@code malformedActionIndex()}.</li>
     *   <li>If the operation is {@code "set"}:
     *     <ol type="a">
     *       <li>If {@code value.marketingMessageAction} is missing, increment
     *           the {@code r} counter and return
     *           {@code malformedActionValue(collectionName)}.</li>
     *       <li>If {@code marketingMessageAction.type} is {@code null},
     *           increment the {@code a} counter and return
     *           {@code malformedActionValue(collectionName)}.</li>
     *       <li>Otherwise, queue
     *           {@code {id, name, type, isDeleted, message, mediaId, sentMessageIds: new Set}}
     *           for the batched persist and return
     *           {@code {actionState: Success}}. The full action (including
     *           {@code isDeleted}) is stored as-is; readers later filter on
     *           {@code isDeleted} when listing live templates.</li>
     *     </ol>
     *   </li>
     *   <li>For any other operation, increment the {@code i} counter and
     *       return {@code {actionState: Unsupported}}.</li>
     * </ol>
     *
     * <p>After the loop WhatsApp Web persists via
     * {@code WAWebPremiumMessageSchema.getPremiumMessageTable().bulkCreateOrMerge(n)}
     * and adds the rows to the in-memory
     * {@code WAWebPremiumMessageCollection.PremiumMessageCollection}. Cobalt
     * collapses both into a single
     * {@code Map<messageId, MarketingMessageAction>} on the store and updates
     * it eagerly per mutation.
     *
     * <p>Cobalt diverges from WhatsApp Web in two intentional ways:
     * <ul>
     *   <li>The persist is per-mutation rather than batched. WhatsApp Web
     *       collects all queued rows in an {@code n} array and calls
     *       {@code bulkCreateOrMerge(n)} once at the end of the batch.
     *       Cobalt updates the {@code marketingMessages} map eagerly because
     *       the underlying storage is a flat key/value map and there is no
     *       per-entity batch buffer.</li>
     *   <li>WhatsApp Web's per-mutation {@code try/catch} that produces a
     *       {@code Failed} state is not replicated. Per Cobalt's error model,
     *       unexpected exceptions propagate to the orchestration layer instead
     *       of being mapped to a {@code Failed} state.</li>
     * </ul>
     *
     * <p>The {@code r > 0}, {@code a > 0} and {@code i > 0} expressions in
     * WhatsApp Web are dead-code reads of the per-batch counters and are
     * intentionally not replicated.
     *
     * @implNote WAWebPremiumMessageSync.applyMutations — single mutation slice
     *           of the WhatsApp Web batch loop
     * @param client   the {@link WhatsAppClient} instance linked to the mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebPremiumMessageSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index()); // WAWebPremiumMessageSync.applyMutations: var l = e.indexParts
        var messageId = indexArray.getString(1); // WAWebPremiumMessageSync.applyMutations: s = l[1]
        if (messageId == null || messageId.isEmpty()) {
            return malformedActionIndex(); // WAWebPremiumMessageSync.applyMutations: if (!s) return t.malformedActionIndex()
        }

        if (mutation.operation() != SyncdOperation.SET) {
            // WAWebPremiumMessageSync.applyMutations: (i++, {actionState: Unsupported})
            // The `i++` counter is dead-code in WhatsApp Web (only read by `i > 0` which is itself a no-op expression).
            return MutationApplicationResult.unsupported();
        }

        // WAWebPremiumMessageSync.applyMutations: u = e.value.marketingMessageAction; if (!u) ...
        if (!(mutation.value().action().orElse(null) instanceof MarketingMessageAction action)) {
            // WAWebPremiumMessageSync.applyMutations: r++, return WAWebSyncdIndexUtils.malformedActionValue(collectionName)
            return malformedActionValue();
        }

        // WAWebPremiumMessageSync.applyMutations: var _ = u.type; if (_ == null) ...
        if (action.type().isEmpty()) {
            // WAWebPremiumMessageSync.applyMutations: a++, return WAWebSyncdIndexUtils.malformedActionValue(collectionName)
            return malformedActionValue();
        }

        // WAWebPremiumMessageSync.applyMutations:
        //   n.push({id: s, name: p, type: _, isDeleted: c, message: m, mediaId: d, sentMessageIds: new Set})
        // followed (after the loop) by:
        //   yield WAWebPremiumMessageSchema.getPremiumMessageTable().bulkCreateOrMerge(n)
        //   PremiumMessageCollection.add(n.map(e => babelHelpers.extends({}, e)))
        // ADAPTED: Cobalt's marketingMessages map plays the role of both the IDB table and the
        // PremiumMessageCollection. The action is stored as-is regardless of the isDeleted flag,
        // matching WhatsApp Web (which never branches on isDeleted in this handler — readers
        // filter on it when listing live templates). The eager copy-then-replace pattern
        // mirrors how Cobalt's other handlers update store maps without holding the
        // collection lock.
        var messages = new HashMap<>(client.store().marketingMessages()); // ADAPTED: WAWebPremiumMessageSync.applyMutations: n = []
        messages.put(messageId, action); // ADAPTED: WAWebPremiumMessageSync.applyMutations: n.push({id: s, ...}) + bulkCreateOrMerge + PremiumMessageCollection.add
        client.store().setMarketingMessages(messages); // ADAPTED: WAWebPremiumMessageSync.applyMutations: bulkCreateOrMerge
        return MutationApplicationResult.success(); // WAWebPremiumMessageSync.applyMutations: {actionState: Success}
    }
}
