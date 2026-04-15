package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.device.KeyExpirationAction;
import com.github.auties00.cobalt.model.sync.action.device.KeyExpirationActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.sync.key.SyncKeyUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles sentinel actions for sync key expiration.
 *
 * <p>The sentinel is a special sync action used as a keepalive/liveness check
 * for the app state sync key subsystem. It creates SET mutations with the
 * current key ID and timestamp, used to verify that sync keys are working
 * correctly. The handler's apply method expires sync keys matching the
 * sentinel's expired key epoch. The set method creates sentinel mutations
 * for all collections with the active key's fingerprint data.
 *
 * <p>Per WhatsApp Web, the sentinel handler extends {@code AccountSyncdActionBase}
 * with collection name {@code RegularLow}, version {@code 3}, and action
 * {@code "sentinel"}.
 *
 * @implNote WAWebSentinelMutationSync (default export singleton)
 */
public final class SentinelHandler implements WebAppStateActionHandler {
    /**
     * Logger for sentinel mutation sync operations.
     *
     * @implNote WAWebSentinelMutationSync: WATagsLogger.TAGS(["syncd","SentinelMutationSync"])
     */
    private static final Logger LOGGER = Logger.getLogger(SentinelHandler.class.getName());

    /**
     * Singleton instance of the sentinel handler.
     *
     * <p>Per WhatsApp Web, the module exports a frozen singleton instance
     * of the sentinel handler class.
     *
     * @implNote WAWebSentinelMutationSync: var f = new _; Object.freeze(f); l.default = f
     */
    public static final SentinelHandler INSTANCE = new SentinelHandler();

    /**
     * Constructs the singleton sentinel handler.
     *
     * @implNote WAWebSentinelMutationSync: constructor sets collectionName = CollectionName.RegularLow
     */
    private SentinelHandler() {

    }

    /**
     * Returns the action name for sentinel mutations.
     *
     * <p>Per WhatsApp Web {@code WAWebSentinelMutationSync.getAction()}: returns
     * {@code WASyncdConst.Actions.Sentinel} which resolves to {@code "sentinel"}.
     *
     * @implNote WAWebSentinelMutationSync.getAction
     * @return the sentinel action name {@code "sentinel"}
     */
    @Override
    public String actionName() {
        return KeyExpirationAction.ACTION_NAME; // WAWebSentinelMutationSync.getAction: WASyncdConst.Actions.Sentinel = "sentinel"
    }

    /**
     * Returns the collection name for sentinel mutations.
     *
     * <p>Per WhatsApp Web {@code WAWebSentinelMutationSync}: the constructor sets
     * {@code this.collectionName = CollectionName.RegularLow} which resolves to
     * {@code "regular_low"}.
     *
     * @implNote WAWebSentinelMutationSync: this.collectionName = WASyncdConst.CollectionName.RegularLow
     * @return the sync patch type {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return KeyExpirationAction.COLLECTION_NAME; // WAWebSentinelMutationSync: collectionName = CollectionName.RegularLow = "regular_low"
    }

    /**
     * Returns the mutation format version for sentinel mutations.
     *
     * <p>Per WhatsApp Web {@code WAWebSentinelMutationSync.getVersion()}: returns {@code 3}.
     *
     * @implNote WAWebSentinelMutationSync.getVersion
     * @return the version number {@code 3}
     */
    @Override
    public int version() {
        return KeyExpirationAction.ACTION_VERSION; // WAWebSentinelMutationSync.getVersion: return 3
    }

    /**
     * Applies a single sentinel mutation and returns whether it succeeded.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and checks whether the result state is {@link SyncActionState#SUCCESS}.
     *
     * @implNote ADAPTED: WAWebSentinelMutationSync.applyMutations — WA Web processes
     *           the full batch via {@code Promise.all(r.map(...))}; Cobalt processes
     *           individual mutations via the interface's default batch method
     * @param client   the WhatsApp client instance
     * @param mutation the sentinel mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // WAWebSentinelMutationSync.applyMutations: per-mutation processing
    }

    /**
     * Applies a single sentinel mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebSentinelMutationSync.applyMutations}: for each
     * mutation in the batch:
     * <ul>
     *   <li>If operation is {@code "set"}: extracts {@code value.keyExpiration.expiredKeyEpoch}.
     *       If the epoch is {@code null}, increments a malformed counter and returns
     *       {@code malformedActionValue(collectionName)}. Otherwise, calls
     *       {@code WAWebGetSyncKey.expireSyncKeyInTransaction(epoch)} and returns
     *       {@code {actionState: Success}}.</li>
     *   <li>For any other operation: increments an unsupported counter and returns
     *       {@code {actionState: Unsupported}}.</li>
     *   <li>On exception: returns {@code {actionState: Failed}}.</li>
     * </ul>
     *
     * @implNote WAWebSentinelMutationSync.applyMutations (per-mutation logic within Promise.all map)
     * @param client   the WhatsApp client instance
     * @param mutation the sentinel mutation to apply
     * @return the mutation application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebSentinelMutationSync.applyMutations: if (e.operation === "set") ... else { i++, return {actionState: Unsupported} }
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof KeyExpirationAction action)) { // WAWebSentinelMutationSync.applyMutations: (n = e.value.keyExpiration) == null
            return malformedActionValue(); // WAWebSentinelMutationSync.applyMutations: (a++, malformedActionValue(t.collectionName))
        }

        var expiredEpoch = action.expiredKeyEpoch(); // WAWebSentinelMutationSync.applyMutations: n.expiredKeyEpoch
        if (expiredEpoch.isEmpty()) { // WAWebSentinelMutationSync.applyMutations: r == null ? (a++, malformedActionValue(t.collectionName))
            return malformedActionValue(); // WAWebSentinelMutationSync.applyMutations: malformedActionValue(t.collectionName)
        }

        client.store().expireAppStateKeysByEpoch(expiredEpoch.getAsInt()); // WAWebSentinelMutationSync.applyMutations: yield expireSyncKeyInTransaction(r)
        return MutationApplicationResult.success(); // WAWebSentinelMutationSync.applyMutations: {actionState: Success}
    }

    /**
     * Creates sentinel pending mutations for all sync collection types.
     *
     * <p>Per WhatsApp Web {@code WAWebSentinelMutationSync.getSentinelMutations}:
     * retrieves the newest sync key pair, extracts its key epoch, and creates
     * one pending mutation per collection name. Each mutation is a SET operation
     * with the sentinel action, the handler's version, the current timestamp,
     * and a value containing {@code keyExpiration.expiredKeyEpoch} set to the
     * active key's epoch.
     *
     * <p>The index for each mutation is {@code ["sentinel", collectionName]},
     * matching the WA Web pattern of {@code buildPendingMutation} with
     * {@code action = getAction()} and {@code indexArgs = [collectionName]}.
     *
     * <p>This is called by the sentinel scheduling logic (equivalent to
     * {@code WAWebSentinel.default}) before marking all collections for sync.
     *
     * @implNote WAWebSentinelMutationSync.getSentinelMutations
     * @param client the WhatsApp client instance for accessing the store
     * @return a list of pending mutations, one per collection type, or an empty
     *         list if no sync key pairs exist
     */
    public List<SyncPendingMutation> getSentinelMutations(WhatsAppClient client) {
        LOGGER.fine("preparing mutations..."); // WAWebSentinelMutationSync.getSentinelMutations: p.LOG("preparing mutations...")

        var timestamp = Instant.now(); // WAWebSentinelMutationSync.getSentinelMutations: var t = unixTimeMs()
        var collections = SyncPatchType.values(); // WAWebSentinelMutationSync.getSentinelMutations: var n = Array.from(CollectionName.members())
        var newestKey = SyncKeyUtils.findNewestKey(client.store().appStateKeys()); // WAWebSentinelMutationSync.getSentinelMutations: var r = yield getNewestKeyPair()
        if (newestKey == null) { // WAWebSentinelMutationSync.getSentinelMutations: if (r == null)
            LOGGER.warning("sentinel mutation sync: no key pairs"); // WAWebSentinelMutationSync.getSentinelMutations: WALogger.ERROR("sentinel mutation sync: no key pairs")
            return Collections.emptyList(); // WAWebSentinelMutationSync.getSentinelMutations: return []
        }

        var keyEpoch = SyncKeyUtils.getKeyEpoch(newestKey); // WAWebSentinelMutationSync.getSentinelMutations: var a = r.keyEpoch
        var keyExpirationAction = new KeyExpirationActionBuilder() // WAWebSentinelMutationSync.getSentinelMutations: var i = {keyExpiration: {expiredKeyEpoch: a}}
                .expiredKeyEpoch(keyEpoch)
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp)
                .keyExpirationAction(keyExpirationAction)
                .build();

        var mutations = new ArrayList<SyncPendingMutation>(collections.length); // WAWebSentinelMutationSync.getSentinelMutations: n.map(function(n) { ... })
        for (var collection : collections) { // WAWebSentinelMutationSync.getSentinelMutations: n.map(function(n) { return buildPendingMutation({...}) })
            var index = JSON.toJSONString(List.of(actionName(), collection.toString())); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs)) where indexArgs = [n]
            var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, binarySyncAction, version, operation, timestamp, action }
                    index,
                    value,
                    SyncdOperation.SET, // WAWebSentinelMutationSync.getSentinelMutations: operation = SET
                    timestamp,
                    version() // WAWebSentinelMutationSync.getSentinelMutations: version = this.getVersion()
            );
            mutations.add(new SyncPendingMutation(mutation, 0)); // ADAPTED: WAWebSyncdActionUtils.buildPendingMutation returns raw; WAWebSentinel bulk-creates via bulkCreateSyncPendingMutationsInTransaction
        }
        return mutations; // WAWebSentinelMutationSync.getSentinelMutations: return n.map(...)
    }
}
