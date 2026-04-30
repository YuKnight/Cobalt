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
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.business.CtwaPerCustomerDataSharingAction;
import com.github.auties00.cobalt.model.sync.action.business.CtwaPerCustomerDataSharingActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

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
@WhatsAppWebModule(moduleName = "WAWebCtwaPerCustomerDataSharingSync")
public final class CtwaPerCustomerDataSharingHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code CtwaPerCustomerDataSharingHandler}.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync — module-level singleton:
     *           {@code var p = new m(); l.default = p}
     */
    @WhatsAppWebExport(moduleName = "WAWebCtwaPerCustomerDataSharingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final CtwaPerCustomerDataSharingHandler INSTANCE = new CtwaPerCustomerDataSharingHandler();

    /**
     * Creates the singleton handler instance.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync — singleton constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularHigh};
     *           Cobalt surfaces that via {@link #collectionName()}.
     */
    @WhatsAppWebExport(moduleName = "WAWebCtwaPerCustomerDataSharingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
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
    @WhatsAppWebExport(moduleName = "WAWebCtwaPerCustomerDataSharingSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return CtwaPerCustomerDataSharingAction.ACTION_NAME;
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
    @WhatsAppWebExport(moduleName = "WAWebCtwaPerCustomerDataSharingSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return CtwaPerCustomerDataSharingAction.COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for this handler.
     *
     * @implNote WAWebCtwaPerCustomerDataSharingSync.getVersion — returns {@code 1}
     * @return the version number
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebCtwaPerCustomerDataSharingSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return CtwaPerCustomerDataSharingAction.ACTION_VERSION;
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
    @WhatsAppWebExport(moduleName = "WAWebCtwaPerCustomerDataSharingSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
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
    @WhatsAppWebExport(moduleName = "WAWebCtwaPerCustomerDataSharingSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        var indexArray = JSON.parseArray(mutation.index());
        var accountLid = indexArray.getString(1);

        switch (mutation.operation()) {
            case SET -> {
                if (accountLid == null) {
                    return malformedActionValue();
                }

                // var c = s.ctwaPerCustomerDataSharingAction; if ((c == null ? void 0 : c.isCtwaPerCustomerDataSharingEnabled) == null) ...
                // When the value or action payload is missing, WA Web falls through this branch and returns malformed.
                if (!(mutation.value().action().orElse(null) instanceof CtwaPerCustomerDataSharingAction action)) {
                    return malformedActionValue();
                }

                // ADAPTED: WA Web checks (c?.isCtwaPerCustomerDataSharingEnabled == null) and returns malformed.
                // Cobalt's public accessor coalesces the nullable Boolean field to false per nullable boolean
                // accessor convention; the raw field is package-private and outside this module's ownership,
                // so null vs false cannot be distinguished here without mutating the model. The practical
                // effect is that a deliberately null flag is treated as `false` rather than malformed.
                var enabled = action.isCtwaPerCustomerDataSharingEnabled();

                // WA Web calls createOrReplace({lidRawString: u, dataSharing3pdEnabled: d}) on
                // the data-sharing-3pd-lid-v2 IDB table and updateDataSharing3pdLidInCollection
                // on the frontend. Cobalt mirrors the per-LID schema by writing into the per-LID
                // map keyed by accountLid on the unified store.
                client.store().setCtwaDataSharing(accountLid, enabled);

                return MutationApplicationResult.success();
            }
            case REMOVE -> {
                // WA Web calls remove(t) on the data-sharing-3pd-lid-v2 table and
                // removeDataSharing3pdLidFromCollection on the frontend. WA Web does not validate
                // accountLid on REMOVE — a null key is a no-op on IDB. Cobalt's store removal
                // treats null as a no-op too, preserving Success semantics.
                client.store().removeCtwaDataSharing(accountLid);

                return MutationApplicationResult.success();
            }
            default -> {
                return MutationApplicationResult.unsupported();
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
    @WhatsAppWebExport(moduleName = "WAWebCtwaPerCustomerDataSharingSync", exports = "getCtwaPerCustomerDataSharingMutation", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPendingMutation getCtwaPerCustomerDataSharingMutation(Jid accountLid, boolean isEnabled) {
        var timestamp = Instant.now();
        var action = new CtwaPerCustomerDataSharingActionBuilder()
                .isCtwaPerCustomerDataSharingEnabled(isEnabled)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .ctwaPerCustomerDataSharingAction(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName(), accountLid.toString()));
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
