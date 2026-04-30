package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.SubscriptionsSyncV2Action;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles subscriptions sync v2 actions.
 *
 * <p>Per WhatsApp Web ({@code WAWebSubscriptionsSyncV2Sync}), this handler processes
 * mutations for paid subscriptions and feature flags. Each SET mutation carries a
 * {@code SubscriptionsSyncV2Action} value with a list of subscriptions and a list
 * of paid features. The handler applies both lists in {@code rewrite} mode,
 * replacing any previously stored subscriptions and feature flags. REMOVE operations
 * are acknowledged but perform no state changes; a counter is tracked across the
 * batch and a warning is logged if any REMOVE operations were seen.
 *
 * <p>Index format: {@code ["subscriptions_sync_v2"]} (singleton index, no parameters)
 *
 * @implNote WAWebSubscriptionsSyncV2Sync.default — singleton instance of the
 *           {@code AccountSyncdActionBase} subclass with
 *           {@code collectionName = Regular}, {@code getVersion() = 1},
 *           {@code getAction() = "subscriptions_sync_v2"}, and
 *           {@code applyMutations(t)} which processes each mutation via
 *           {@code Promise.all(t.map(...))} and tallies REMOVE operations
 */
@WhatsAppWebModule(moduleName = "WAWebSubscriptionsSyncV2Sync")
public final class SubscriptionHandler implements WebAppStateActionHandler {
    /**
     * Canonical WhatsApp Web action name for this handler.
     *
     * @implNote WAWebSubscriptionsSyncV2Sync.getAction returns
     *           {@code o("WASyncdConst").Actions.SubscriptionsSyncV2}, which is
     *           {@code "subscriptions_sync_v2"}
     */
    private static final String ACTION_NAME = "subscriptions_sync_v2";

    /**
     * Canonical WhatsApp Web mutation format version for this handler.
     *
     * @implNote WAWebSubscriptionsSyncV2Sync.getVersion returns {@code 1}
     */
    private static final int ACTION_VERSION = 1;

    /**
     * Canonical WhatsApp Web collection this handler's mutations belong to.
     *
     * @implNote WAWebSubscriptionsSyncV2Sync constructor sets
     *           {@code this.collectionName = o("WASyncdConst").CollectionName.Regular}
     */
    private static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR;

    /**
     * Logger for subscriptions sync v2 operations.
     *
     * @implNote ADAPTED: WAWebSubscriptionsSyncV2Sync uses WALogger; Cobalt uses {@code java.util.logging}
     */
    private static final Logger LOGGER = Logger.getLogger(SubscriptionHandler.class.getName());

    /**
     * The singleton instance of {@code SubscriptionHandler}.
     *
     * @implNote WAWebSubscriptionsSyncV2Sync — module-level {@code c = new u()} singleton,
     *           exported as {@code l.default = c}
     */
    @WhatsAppWebExport(moduleName = "WAWebSubscriptionsSyncV2Sync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final SubscriptionHandler INSTANCE = new SubscriptionHandler();

    /**
     * Private constructor to enforce the singleton pattern.
     *
     * @implNote WAWebSubscriptionsSyncV2Sync — class {@code u} constructor sets
     *           {@code e.collectionName = o("WASyncdConst").CollectionName.Regular}
     */
    @WhatsAppWebExport(moduleName = "WAWebSubscriptionsSyncV2Sync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private SubscriptionHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebSubscriptionsSyncV2Sync.getAction — returns
     *           {@code o("WASyncdConst").Actions.SubscriptionsSyncV2}, the constant
     *           {@code "subscriptions_sync_v2"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSubscriptionsSyncV2Sync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebSubscriptionsSyncV2Sync — constructor sets
     *           {@code this.collectionName = o("WASyncdConst").CollectionName.Regular}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSubscriptionsSyncV2Sync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return COLLECTION_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSubscriptionsSyncV2Sync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return ACTION_VERSION;
    }

    /**
     * Applies a subscriptions sync v2 mutation.
     *
     * <p>Per WhatsApp Web ({@code WAWebSubscriptionsSyncV2Sync.applyMutations}),
     * delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} when the result is {@code SUCCESS}.
     *
     * @implNote WAWebSubscriptionsSyncV2Sync.applyMutations — per-mutation logic
     *           within the batch handler's {@code Promise.all(t.map(...))}
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSubscriptionsSyncV2Sync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies a subscriptions sync v2 mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web ({@code WAWebSubscriptionsSyncV2Sync.applyMutations}),
     * the per-mutation logic wraps the entire body in a {@code try/catch} that
     * returns {@code Failed} on any error. The inner body switches on
     * {@code operation}:
     * <ul>
     *   <li>{@code "set"}: extracts {@code subscriptionsSyncV2Action} from the
     *       mutation value. If missing, increments the {@code malformed} ODS
     *       counter and returns {@code malformedActionValue(collectionName)}.
     *       Otherwise maps the subscriptions list and paid features list and
     *       calls {@code WAWebSubscriptions.applySubscriptionsAndFeatureFlags(
     *       subs, features, "rewrite")}, which rewrites both the subscription
     *       table and the feature flag table. On success, increments the
     *       {@code success} ODS counter and returns {@code Success}.</li>
     *   <li>{@code "remove"}: increments a REMOVE counter on the batch handler
     *       and returns {@code Success} with no state change.</li>
     *   <li>Any other operation: throws a "Match: No case successfully matched"
     *       error, caught by the outer {@code try/catch} returning {@code Failed}.</li>
     * </ul>
     *
     * <p>In Cobalt, the WA Web ODS telemetry increments
     * ({@code web.app.subscription_sync.syncd.malformed},
     * {@code web.app.subscription_sync.syncd.success}, and
     * {@code web.app.subscription_sync.syncd.error}) are intentionally omitted,
     * since WAM/telemetry is not replicated.
     *
     * <p>The "rewrite" semantics of {@code applySubscriptionsAndFeatureFlags}
     * are mapped to a wholesale replacement of the four flat
     * {@code ConcurrentHashMap}s on {@link com.github.auties00.cobalt.store.WhatsAppStore}
     * that back business feature flags and per-subscription metadata
     * (status, expiration, creation time). Each SET therefore overwrites the
     * previous snapshot rather than merging with it.
     *
     * <p>The REMOVE counter is tracked at the batch level in
     * {@link #applyMutationBatchResults(WhatsAppClient, List)} rather than as a
     * mutable field on this singleton; a single REMOVE mutation simply returns
     * {@code Success} here.
     *
     * @implNote WAWebSubscriptionsSyncV2Sync.applyMutations — per-mutation body
     *           of the batch handler, structured as a labelled block with SET,
     *           REMOVE, and fall-through throw cases, all wrapped in try/catch
     * @param client   the WhatsApp client
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSubscriptionsSyncV2Sync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        try {
            if (mutation.operation() == SyncdOperation.SET) {
                if (!(mutation.value().action().orElse(null) instanceof SubscriptionsSyncV2Action action)) {
                    return malformedActionValue();
                }

                // ADAPTED: WA Web rewrites the subscription table and feature flag table via WAWebSubscriptions;
                // Cobalt rewrites four flat ConcurrentHashMaps held on WhatsAppStore directly.
                var featureFlags = new HashMap<String, Boolean>();
                for (var feature : action.paidFeatures()) {
                    feature.name().ifPresent(name -> featureFlags.put(name, feature.enabled()));
                }
                client.store().setBusinessFeatureFlags(featureFlags);

                var statuses = new HashMap<String, String>();
                var expirations = new HashMap<String, Long>();
                var creationTimes = new HashMap<String, Long>();
                for (var subscription : action.subscriptions()) {
                    var idOpt = subscription.id();
                    if (idOpt.isEmpty()) {
                        continue;
                    }
                    var id = idOpt.get();
                    subscription.status().ifPresent(status -> statuses.put(id, status));
                    var endTime = subscription.endTime();
                    if (endTime.isPresent()) {
                        expirations.put(id, endTime.getAsLong());
                    }
                    var creationTime = subscription.creationTime();
                    if (creationTime.isPresent()) {
                        creationTimes.put(id, creationTime.getAsLong());
                    }
                }
                client.store().setBusinessSubscriptionStatuses(statuses);
                client.store().setBusinessSubscriptionExpirations(expirations);
                client.store().setBusinessSubscriptionCreationTimes(creationTimes);

                return MutationApplicationResult.success();
            }

            if (mutation.operation() == SyncdOperation.REMOVE) {
                return MutationApplicationResult.success();
            }

            return MutationApplicationResult.failed();
        } catch (Exception e) {
            return MutationApplicationResult.failed();
        }
    }

    /**
     * Applies a batch of subscriptions sync v2 mutations and returns detailed
     * results.
     *
     * <p>Per WhatsApp Web ({@code WAWebSubscriptionsSyncV2Sync.applyMutations}),
     * the batch handler:
     * <ol>
     *   <li>Initializes a REMOVE counter {@code i} to 0.</li>
     *   <li>Processes each mutation in parallel via {@code Promise.all(t.map(...))},
     *       collecting results into list {@code l}.</li>
     *   <li>After processing, if {@code i > 0}, logs a warning
     *       {@code "[SubscriptionsSyncV2Sync] N REMOVE ops (singleton)"}.</li>
     *   <li>Returns {@code l}.</li>
     * </ol>
     *
     * <p>In Cobalt, the parallel {@code Promise.all} is mapped to a sequential
     * loop on the caller's virtual thread, preserving semantics. The REMOVE
     * counter is maintained locally in this method (rather than as a field on
     * the singleton) since Cobalt treats the handler as a stateless service.
     *
     * @implNote WAWebSubscriptionsSyncV2Sync.applyMutations — batch entry point
     *           with {@code Promise.all(t.map(...))} and the post-batch warning
     *           log for REMOVE operations
     * @param client    the WhatsAppClient instance linked to the mutations
     * @param mutations the batch of mutations to apply
     * @return a list of results parallel to the input
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSubscriptionsSyncV2Sync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, WamService wamService, List<DecryptedMutation.Trusted> mutations) {
        var removeCount = 0;
        var results = new ArrayList<MutationApplicationResult>(mutations.size());
        for (var mutation : mutations) { // ADAPTED: WAWebSubscriptionsSyncV2Sync.applyMutations uses Promise.all(t.map(...)) — virtual-thread blocking loop
            var result = applyMutationResult(client, wamService, mutation);
            if (result.actionState() == SyncActionState.SUCCESS && mutation.operation() == SyncdOperation.REMOVE) {
                removeCount++;
            }
            results.add(result);
        }
        if (removeCount > 0) {
            LOGGER.warning("[SubscriptionsSyncV2Sync] " + removeCount + " REMOVE ops (singleton)");
        }
        return results;
    }

    /**
     * Applies a batch of subscriptions sync v2 mutations and returns the legacy
     * boolean results.
     *
     * <p>Delegates to {@link #applyMutationBatchResults(WhatsAppClient, List)}
     * to preserve the REMOVE counter logging, then converts each result to
     * {@code true} if {@code SUCCESS} and {@code false} otherwise.
     *
     * @implNote ADAPTED: WAWebSubscriptionsSyncV2Sync.applyMutations — WA Web returns
     *           per-mutation {@code SyncActionState} values directly; Cobalt wraps
     *           them in {@link MutationApplicationResult} and then converts to
     *           boolean for the legacy batch interface
     * @param client    the WhatsAppClient instance linked to the mutations
     * @param mutations the batch of mutations to apply
     * @return a list of booleans parallel to the input
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSubscriptionsSyncV2Sync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public List<Boolean> applyMutationBatch(WhatsAppClient client, WamService wamService, List<DecryptedMutation.Trusted> mutations) {
        var detailed = applyMutationBatchResults(client, wamService, mutations);
        var results = new ArrayList<Boolean>(detailed.size());
        for (var result : detailed) {
            results.add(result.actionState() == SyncActionState.SUCCESS);
        }
        return results;
    }
}
