package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.PrimaryFeatureAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles primary device feature flag actions.
 *
 * <p>This handler processes mutations that communicate the set of primary
 * device feature flags. Only {@code SET} operations are supported; other
 * operations are acknowledged with an {@code UNSUPPORTED} state. When
 * processing a batch, only the {@link PrimaryFeatureAction} mutation with the
 * highest timestamp is actually applied to the store, mirroring WhatsApp Web's
 * "latest wins" semantics for this collection.
 *
 * <p>Index format: {@code ["primary_feature"]}
 *
 * @implNote WAWebPrimaryFeatureSync — extends {@code AccountSyncdActionBase}
 *           with {@code collectionName = Regular}, {@code getAction()} returning
 *           {@code "primary_feature"}, and {@code getVersion()} returning {@code 7}
 */
public final class PrimaryFeatureHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code PrimaryFeatureHandler}.
     *
     * @implNote WAWebPrimaryFeatureSync.default — WA Web exports a single
     *           pre-instantiated handler ({@code c = new u; l.default = c})
     */
    public static final PrimaryFeatureHandler INSTANCE = new PrimaryFeatureHandler();

    /**
     * Constructs the singleton instance.
     *
     * @implNote WAWebPrimaryFeatureSync — WA Web instantiates the handler once
     *           via {@code new u()} and exports it as the module default
     */
    private PrimaryFeatureHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPrimaryFeatureSync.getAction — returns
     *           {@code WASyncdConst.Actions.PrimaryFeature} which equals
     *           {@code "primary_feature"}
     */
    @Override
    public String actionName() {
        return PrimaryFeatureAction.ACTION_NAME; // WAWebPrimaryFeatureSync.getAction
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPrimaryFeatureSync — sets {@code this.collectionName = WASyncdConst.CollectionName.Regular}
     *           in the constructor
     */
    @Override
    public SyncPatchType collectionName() {
        return PrimaryFeatureAction.COLLECTION_NAME; // WAWebPrimaryFeatureSync constructor: this.collectionName = Regular
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebPrimaryFeatureSync.getVersion — returns the literal {@code 7}
     */
    @Override
    public int version() {
        return PrimaryFeatureAction.ACTION_VERSION; // WAWebPrimaryFeatureSync.getVersion: return 7
    }

    /**
     * {@inheritDoc}
     *
     * <p>WhatsApp Web exposes only a batch entry point ({@code applyMutations});
     * Cobalt's interface contract additionally requires a single-mutation path,
     * which is implemented here by delegating to {@link #applyMutationResult}.
     *
     * @implNote ADAPTED: WAWebPrimaryFeatureSync.applyMutations — WA Web only
     *           processes mutations as a batch; the single-mutation entry point
     *           is a Cobalt interface adaptation
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: single-path adapter for batch-only WA Web entry
    }

    /**
     * {@inheritDoc}
     *
     * <p>Per WhatsApp Web {@code WAWebPrimaryFeatureSync.applyMutations}: maps
     * each mutation to a per-mutation {@link MutationApplicationResult}, while
     * tracking the mutation with the highest timestamp among the valid {@code SET}
     * mutations. After the mapping, the flags from the latest valid mutation are
     * persisted via {@link com.github.auties00.cobalt.store.WhatsAppStore#setPrimaryFeatures(List)}.
     *
     * <p>Non-{@code SET} mutations are acknowledged as {@code UNSUPPORTED} and
     * mutations whose decoded value is not a {@link PrimaryFeatureAction} are
     * acknowledged as {@code MALFORMED}; neither participates in the timestamp
     * comparison. WhatsApp Web also accepts an empty {@code flags} list as a
     * valid value (its only check is {@code flags == null}), so an empty list
     * is treated as {@code SUCCESS} here as well.
     *
     * @implNote WAWebPrimaryFeatureSync.applyMutations
     */
    @Override
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        DecryptedMutation.Trusted latest = null; // WAWebPrimaryFeatureSync.applyMutations: var a
        var results = new ArrayList<MutationApplicationResult>(mutations.size()); // WAWebPrimaryFeatureSync.applyMutations: var u = t.map(...)
        for (var mutation : mutations) { // WAWebPrimaryFeatureSync.applyMutations: t.map(function(e) {...})
            if (mutation.operation() != SyncdOperation.SET) { // WAWebPrimaryFeatureSync.applyMutations: if (e.operation !== "set")
                results.add(MutationApplicationResult.unsupported()); // WAWebPrimaryFeatureSync.applyMutations: return {actionState: Unsupported}
                continue;
            }

            var action = mutation.value().action().orElse(null); // WAWebPrimaryFeatureSync.applyMutations: var r = (t = e.value.primaryFeature) == null ? void 0 : t.flags
            if (!(action instanceof PrimaryFeatureAction)) { // WAWebPrimaryFeatureSync.applyMutations: if (r == null) -- in protobuf, an absent primaryFeature sub-action is the only way r becomes null
                results.add(malformedActionValue()); // WAWebPrimaryFeatureSync.applyMutations: return WAWebSyncdIndexUtils.malformedActionValue(n.collectionName)
                continue;
            }

            if (latest == null || mutation.timestamp().compareTo(latest.timestamp()) > 0) { // WAWebPrimaryFeatureSync.applyMutations: if (a == null || e.timestamp > a.timestamp)
                latest = mutation; // WAWebPrimaryFeatureSync.applyMutations: a = e
            }
            results.add(MutationApplicationResult.success()); // WAWebPrimaryFeatureSync.applyMutations: return {actionState: Success}
        }
        // WAWebPrimaryFeatureSync.applyMutations: WARN("primary feature sync: i operations not supported") and WARN("primary feature sync: l malformed mutations") -- skipped, telemetry/logging

        if (latest != null) { // WAWebPrimaryFeatureSync.applyMutations: if (a != null)
            // WAWebPrimaryFeatureSync.applyMutations: var d = WANullthrows((c = a.value.primaryFeature) == null ? void 0 : c.flags)
            var pfa = (PrimaryFeatureAction) latest.value().action().orElseThrow();
            client.store().setPrimaryFeatures(pfa.flags()); // WAWebPrimaryFeatureSync.applyMutations: yield WAWebPrimaryFeatures.setPrimaryFeatures(d)
        }

        return results; // WAWebPrimaryFeatureSync.applyMutations: return u
    }

    /**
     * {@inheritDoc}
     *
     * <p>Single-mutation adapter that mirrors the WhatsApp Web batch logic for
     * a list of size one: a non-{@code SET} mutation yields {@code UNSUPPORTED};
     * a mutation whose decoded value is not a {@link PrimaryFeatureAction}
     * yields {@code MALFORMED}; otherwise the flags are persisted to the store
     * and {@code SUCCESS} is returned. As in WhatsApp Web, an empty {@code flags}
     * list is considered valid.
     *
     * @implNote ADAPTED: WAWebPrimaryFeatureSync.applyMutations — WA Web only
     *           defines a batch entry point; this single-mutation path applies
     *           the same per-mutation logic with no batch-level latest selection
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebPrimaryFeatureSync.applyMutations: if (e.operation !== "set")
            return MutationApplicationResult.unsupported(); // WAWebPrimaryFeatureSync.applyMutations: return {actionState: Unsupported}
        }

        if (!(mutation.value().action().orElse(null) instanceof PrimaryFeatureAction action)) { // WAWebPrimaryFeatureSync.applyMutations: if (r == null) where r = e.value.primaryFeature?.flags
            return malformedActionValue(); // WAWebPrimaryFeatureSync.applyMutations: return WAWebSyncdIndexUtils.malformedActionValue(n.collectionName)
        }

        client.store().setPrimaryFeatures(action.flags()); // WAWebPrimaryFeatureSync.applyMutations: yield WAWebPrimaryFeatures.setPrimaryFeatures(d)
        return MutationApplicationResult.success(); // WAWebPrimaryFeatureSync.applyMutations: return {actionState: Success}
    }
}
