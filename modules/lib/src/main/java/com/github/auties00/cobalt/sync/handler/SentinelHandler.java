package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
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
 */
@WhatsAppWebModule(moduleName = "WAWebSentinelMutationSync")
public final class SentinelHandler implements WebAppStateActionHandler {
    /**
     * Logger for sentinel mutation sync operations.
     */
    private static final Logger LOGGER = Logger.getLogger(SentinelHandler.class.getName());

    /**
     * Singleton instance of the sentinel handler.
     *
     * <p>Per WhatsApp Web, the module exports a frozen singleton instance
     * of the sentinel handler class.
     */
    @WhatsAppWebExport(moduleName = "WAWebSentinelMutationSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final SentinelHandler INSTANCE = new SentinelHandler();

    /**
     * Constructs the singleton sentinel handler.
     */
    @WhatsAppWebExport(moduleName = "WAWebSentinelMutationSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private SentinelHandler() {

    }

    /**
     * Returns the action name for sentinel mutations.
     *
     * <p>Per WhatsApp Web {@code WAWebSentinelMutationSync.getAction()}: returns
     * {@code WASyncdConst.Actions.Sentinel} which resolves to {@code "sentinel"}.
     * @return the sentinel action name {@code "sentinel"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSentinelMutationSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return KeyExpirationAction.ACTION_NAME;
    }

    /**
     * Returns the collection name for sentinel mutations.
     *
     * <p>Per WhatsApp Web {@code WAWebSentinelMutationSync}: the constructor sets
     * {@code this.collectionName = CollectionName.RegularLow} which resolves to
     * {@code "regular_low"}.
     * @return the sync patch type {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSentinelMutationSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return KeyExpirationAction.COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for sentinel mutations.
     *
     * <p>Per WhatsApp Web {@code WAWebSentinelMutationSync.getVersion()}: returns {@code 3}.
     * @return the version number {@code 3}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSentinelMutationSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return KeyExpirationAction.ACTION_VERSION;
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
     * @param client   the WhatsApp client instance
     * @param mutation the sentinel mutation to apply
     * @return the mutation application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSentinelMutationSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.DIRECT)
    public MutationApplicationResult applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof KeyExpirationAction action)) {
            return SyncdIndexUtils.malformedActionValue(collectionName().name());
        }

        var expiredEpoch = action.expiredKeyEpoch();
        if (expiredEpoch.isEmpty()) {
            return SyncdIndexUtils.malformedActionValue(collectionName().name());
        }

        client.store().expireAppStateKeysByEpoch(expiredEpoch.getAsInt());
        return MutationApplicationResult.success();
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
     * @param client the WhatsApp client instance for accessing the store
     * @return a list of pending mutations, one per collection type, or an empty
     *         list if no sync key pairs exist
     */
    @WhatsAppWebExport(moduleName = "WAWebSentinelMutationSync", exports = "getSentinelMutations", adaptation = WhatsAppAdaptation.DIRECT)
    public List<SyncPendingMutation> getSentinelMutations(WhatsAppClient client) {
        LOGGER.fine("preparing mutations...");

        var timestamp = Instant.now();
        var collections = SyncPatchType.values();
        var newestKey = SyncKeyUtils.findNewestKey(client.store().appStateKeys());
        if (newestKey == null) {
            LOGGER.warning("sentinel mutation sync: no key pairs");
            return Collections.emptyList();
        }

        var keyEpoch = SyncKeyUtils.getKeyEpoch(newestKey);
        var keyExpirationAction = new KeyExpirationActionBuilder()
                .expiredKeyEpoch(keyEpoch)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .keyExpirationAction(keyExpirationAction)
                .build();

        var mutations = new ArrayList<SyncPendingMutation>(collections.length);
        for (var collection : collections) {
            var index = JSON.toJSONString(List.of(actionName(), collection.toString()));
            var mutation = new DecryptedMutation.Trusted(
                    index,
                    value,
                    SyncdOperation.SET,
                    timestamp,
                    version()
            );
            mutations.add(new SyncPendingMutation(mutation, 0)); // ADAPTED: WAWebSyncdActionUtils.buildPendingMutation returns raw; WAWebSentinel bulk-creates via bulkCreateSyncPendingMutationsInTransaction
        }
        return mutations;
    }
}
