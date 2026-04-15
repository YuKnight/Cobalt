package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.business.CtwaPerCustomerDataSharingAction;
import com.github.auties00.cobalt.model.sync.action.business.CtwaPerCustomerDataSharingActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.List;

/**
 * Handles CTWA per-customer data sharing sync actions.
 *
 * <p>Per WhatsApp Web {@code WAWebCtwaPerCustomerDataSharingSync}, this handler
 * processes mutations for the {@code "ctwa_per_customer_data_sharing"} sync action.
 * SET and REMOVE operations are supported. On SET, validates that
 * {@code indexParts[1]} (accountLid) is non-{@code null} and that the
 * {@code ctwaPerCustomerDataSharingAction} is present with a non-{@code null}
 * {@code isCtwaPerCustomerDataSharingEnabled} field. On REMOVE, the stored
 * data sharing preference is cleared.
 *
 * <p>Index format: {@code ["ctwaPerCustomerDataSharing", accountLid]}
 *
 * <p>WA Web uses a per-accountLid IDB table ({@code data-sharing-3pd-lid-v2})
 * and an in-memory collection to store per-customer data sharing preferences.
 * Cobalt mirrors that schema by storing one entry per account LID raw string
 * on the store via {@link com.github.auties00.cobalt.store.WhatsAppStore#setCtwaDataSharing(String, Boolean)}
 * and {@link com.github.auties00.cobalt.store.WhatsAppStore#removeCtwaDataSharing(String)}.
 *
 * @implNote WAWebCtwaPerCustomerDataSharingSync
 */
public final class CtwaPerCustomerDataSharingHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code CtwaPerCustomerDataSharingHandler}.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync — module-level singleton:
     *           {@code var p = new m(); l.default = p}
     */
    public static final CtwaPerCustomerDataSharingHandler INSTANCE = new CtwaPerCustomerDataSharingHandler();

    /**
     * Creates the singleton handler instance.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync — singleton constructor
     */
    private CtwaPerCustomerDataSharingHandler() {
    }

    /**
     * Returns the action name for CTWA per-customer data sharing.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync.getAction —
     *           returns {@code WASyncdConst.Actions.AdsCtwaPerCustomerDataSharing}
     *           which resolves to {@code "ctwaPerCustomerDataSharing"}
     * @return the action name string
     */
    @Override
    public String actionName() {
        return CtwaPerCustomerDataSharingAction.ACTION_NAME; // WAWebCtwaPerCustomerDataSharingSync.getAction
    }

    /**
     * Returns the collection name for CTWA per-customer data sharing.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync.collectionName —
     *           set in constructor to {@code WASyncdConst.CollectionName.RegularHigh}
     *           which resolves to {@code "regular_high"}
     * @return the sync patch type
     */
    @Override
    public SyncPatchType collectionName() {
        return CtwaPerCustomerDataSharingAction.COLLECTION_NAME; // WAWebCtwaPerCustomerDataSharingSync.collectionName
    }

    /**
     * Returns the mutation format version for this handler.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync.getVersion — returns {@code 1}
     * @return the version number
     */
    @Override
    public int version() {
        return CtwaPerCustomerDataSharingAction.ACTION_VERSION; // WAWebCtwaPerCustomerDataSharingSync.getVersion
    }

    /**
     * Applies a CTWA per-customer data sharing mutation.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result state is {@code SUCCESS}.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync.applyMutations — per-mutation
     *           application within the batch handler
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS; // WAWebCtwaPerCustomerDataSharingSync.applyMutations
    }

    /**
     * Applies a CTWA per-customer data sharing mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebCtwaPerCustomerDataSharingSync.applyMutations}:
     * <ul>
     *   <li><b>SET</b>: validates that {@code indexParts[1]} (accountLid) is present;
     *       extracts the {@code ctwaPerCustomerDataSharingAction} from the value and
     *       validates that {@code isCtwaPerCustomerDataSharingEnabled} is non-{@code null};
     *       stores the enabled flag via {@code $CtwaPerCustomerDataSharingSync$p_1}
     *       (which calls {@code createOrReplace} on the data-sharing-3pd-lid-v2 table
     *       and {@code updateDataSharing3pdLidInCollection} on the frontend), then
     *       fires {@code maybeGeneratePerCustomerDataSharingSystemMessage}.</li>
     *   <li><b>REMOVE</b>: removes the entry via {@code $CtwaPerCustomerDataSharingSync$p_2}
     *       (which calls {@code remove} on the table and
     *       {@code removeDataSharing3pdLidFromCollection} on the frontend).</li>
     *   <li><b>default</b>: returns unsupported.</li>
     * </ul>
     *
     * <p>WA Web wraps each mutation in a try/catch that returns
     * {@code SyncActionState.Failed} on error. Per Cobalt's error model,
     * exceptions propagate instead of being caught inline.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync.applyMutations
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index()); // WAWebCtwaPerCustomerDataSharingSync.applyMutations — indexParts

        switch (mutation.operation()) {
            case SET -> { // WAWebCtwaPerCustomerDataSharingSync.applyMutations — operation === "set"
                var accountLid = indexArray.getString(1); // WAWebCtwaPerCustomerDataSharingSync.applyMutations — u = n[1]
                if (accountLid == null) { // WAWebCtwaPerCustomerDataSharingSync.applyMutations — if (!u)
                    return malformedActionValue(); // WAWebCtwaPerCustomerDataSharingSync.applyMutations — malformedActionValue(r.collectionName)
                }

                if (!(mutation.value().action().orElse(null) instanceof CtwaPerCustomerDataSharingAction action)) { // WAWebCtwaPerCustomerDataSharingSync.applyMutations — var c = s.ctwaPerCustomerDataSharingAction; if (c == null)
                    return malformedActionValue(); // WAWebCtwaPerCustomerDataSharingSync.applyMutations — malformedActionValue(r.collectionName)
                }

                // ADAPTED: WA Web checks (c?.isCtwaPerCustomerDataSharingEnabled == null) and returns malformed.
                // Cobalt's accessor coalesces null to false per nullable boolean accessor convention.
                // The protobuf field is Boolean (nullable), but the accessor returns boolean.
                var enabled = action.isCtwaPerCustomerDataSharingEnabled(); // WAWebCtwaPerCustomerDataSharingSync.applyMutations — var d = c.isCtwaPerCustomerDataSharingEnabled

                // WAWebCtwaPerCustomerDataSharingSync.$CtwaPerCustomerDataSharingSync$p_1 —
                // WA Web calls createOrReplace({lidRawString: u, dataSharing3pdEnabled: d}) on
                // the data-sharing-3pd-lid-v2 IDB table and updateDataSharing3pdLidInCollection
                // on the frontend. Cobalt mirrors the per-LID schema by writing into the per-LID
                // map keyed by accountLid.
                client.store().setCtwaDataSharing(accountLid, enabled); // WAWebCtwaPerCustomerDataSharingSync.$CtwaPerCustomerDataSharingSync$p_1

                // WAWebCtwaPerCustomerDataSharingSync.applyMutations — frontendFireAndForget("maybeGeneratePerCustomerDataSharingSystemMessage", ...) — skipped, frontend IPC
                return MutationApplicationResult.success(); // WAWebCtwaPerCustomerDataSharingSync.applyMutations — {actionState: Success}
            }
            case REMOVE -> { // WAWebCtwaPerCustomerDataSharingSync.applyMutations — operation === "remove"
                var accountLid = indexArray.getString(1); // WAWebCtwaPerCustomerDataSharingSync.applyMutations — u = n[1]
                if (accountLid == null) { // WAWebCtwaPerCustomerDataSharingSync.applyMutations — if (!u)
                    return malformedActionValue(); // WAWebCtwaPerCustomerDataSharingSync.applyMutations — malformedActionValue(r.collectionName)
                }

                // WAWebCtwaPerCustomerDataSharingSync.$CtwaPerCustomerDataSharingSync$p_2 —
                // WA Web calls remove(t) on the data-sharing-3pd-lid-v2 table and
                // removeDataSharing3pdLidFromCollection on the frontend. Cobalt mirrors the
                // per-LID schema by deleting the entry keyed by accountLid.
                client.store().removeCtwaDataSharing(accountLid); // WAWebCtwaPerCustomerDataSharingSync.$CtwaPerCustomerDataSharingSync$p_2

                return MutationApplicationResult.success(); // WAWebCtwaPerCustomerDataSharingSync.applyMutations — {actionState: Success}
            }
            default -> { // WAWebCtwaPerCustomerDataSharingSync.applyMutations — unsupported operation
                return MutationApplicationResult.unsupported(); // WAWebCtwaPerCustomerDataSharingSync.applyMutations — {actionState: Unsupported}
            }
        }
    }

    /**
     * Builds a pending mutation for setting or clearing the CTWA per-customer
     * data sharing preference for an account.
     *
     * <p>Per WhatsApp Web {@code WAWebCtwaPerCustomerDataSharingSync.getCtwaPerCustomerDataSharingMutation}:
     * constructs a {@code SyncActionValue} with a {@code ctwaPerCustomerDataSharingAction}
     * containing the enabled flag, then delegates to
     * {@code WAWebSyncdActionUtils.buildPendingMutation} with the handler's collection,
     * index args, version, and a SET operation.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync.getCtwaPerCustomerDataSharingMutation
     * @param accountLid the account LID identifying the customer
     * @param isEnabled  whether per-customer data sharing is enabled
     * @return the pending mutation ready to be queued for sync
     */
    public SyncPendingMutation getCtwaPerCustomerDataSharingMutation(Jid accountLid, boolean isEnabled) {
        var timestamp = Instant.now(); // WAWebCtwaPerCustomerDataSharingSync.getCtwaPerCustomerDataSharingMutation — var r = o("WATimeUtils").unixTimeMs()
        var action = new CtwaPerCustomerDataSharingActionBuilder() // WAWebCtwaPerCustomerDataSharingSync.getCtwaPerCustomerDataSharingMutation — {ctwaPerCustomerDataSharingAction: {isCtwaPerCustomerDataSharingEnabled: n}}
                .isCtwaPerCustomerDataSharingEnabled(isEnabled) // WAWebCtwaPerCustomerDataSharingSync.getCtwaPerCustomerDataSharingMutation — isCtwaPerCustomerDataSharingEnabled: n
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation — encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation — timestamp: r
                .ctwaPerCustomerDataSharingAction(action) // WAWebCtwaPerCustomerDataSharingSync.getCtwaPerCustomerDataSharingMutation — {ctwaPerCustomerDataSharingAction: ...}
                .build();
        var index = JSON.toJSONString(List.of(actionName(), accountLid.toString())); // WAWebSyncdActionUtils.buildPendingMutation — index = JSON.stringify([action].concat(indexArgs)) where indexArgs = [e.toString()]
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation — return { collection, index, binarySyncAction, version, operation, timestamp, action }
                index,
                value,
                SyncdOperation.SET, // WAWebCtwaPerCustomerDataSharingSync.getCtwaPerCustomerDataSharingMutation — operation: SyncdMutation$SyncdOperation.SET
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
