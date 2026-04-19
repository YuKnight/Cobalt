package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.AndroidUnsupportedActions;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.logging.Logger;

/**
 * Handles Android unsupported actions sync mutations.
 *
 * <p>This handler processes mutations that declare which actions are unsupported
 * on Android devices. On SET, reads the {@code androidUnsupportedActions} field from the
 * mutation value. If the {@code allowed} flag is {@code true}, updates the primary
 * allows-all-mutations flag in the store (only if not already set). Other operations
 * are acknowledged as unsupported.
 *
 * <p>Index format: ["android_unsupported_actions"]
 *
 * @implNote WAWebAndroidUnsupportedActionsSync.default — singleton instance of the
 *           class extending {@code AccountSyncdActionBase} with
 *           {@code collectionName = RegularLow}, {@code getVersion() = 4},
 *           {@code getAction() = "android_unsupported_actions"}
 */
@WhatsAppWebModule(moduleName = "WAWebAndroidUnsupportedActionsSync")
public final class AndroidUnsupportedActionsHandler implements WebAppStateActionHandler {
    /**
     * Logger for this handler.
     *
     * @implNote ADAPTED: WAWebAndroidUnsupportedActionsSync uses WALogger; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(AndroidUnsupportedActionsHandler.class.getName()); // ADAPTED: WALogger

    /**
     * The singleton instance of {@code AndroidUnsupportedActionsHandler}.
     *
     * @implNote WAWebAndroidUnsupportedActionsSync — {@code m = new d} at module level
     */
    @WhatsAppWebExport(moduleName = "WAWebAndroidUnsupportedActionsSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final AndroidUnsupportedActionsHandler INSTANCE = new AndroidUnsupportedActionsHandler();

    /**
     * Constructs the singleton handler.
     *
     * @implNote WAWebAndroidUnsupportedActionsSync — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     */
    @WhatsAppWebExport(moduleName = "WAWebAndroidUnsupportedActionsSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private AndroidUnsupportedActionsHandler() {

    }

    /**
     * Returns the action name for this handler.
     *
     * @implNote WAWebAndroidUnsupportedActionsSync.getAction — returns
     *           {@code WASyncdConst.Actions.AndroidUnsupportedActions} which is
     *           {@code "android_unsupported_actions"}
     * @return the action name string
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAndroidUnsupportedActionsSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return AndroidUnsupportedActions.ACTION_NAME; // WAWebAndroidUnsupportedActionsSync.getAction -> WASyncdConst.Actions.AndroidUnsupportedActions
    }

    /**
     * Returns the sync collection this handler's action belongs to.
     *
     * @implNote WAWebAndroidUnsupportedActionsSync — constructor sets
     *           {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}
     *           which maps to {@code "regular_low"}
     * @return the sync patch type
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAndroidUnsupportedActionsSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return AndroidUnsupportedActions.COLLECTION_NAME; // WAWebAndroidUnsupportedActionsSync: this.collectionName = WASyncdConst.CollectionName.RegularLow
    }

    /**
     * Returns the mutation format version for this handler.
     *
     * @implNote WAWebAndroidUnsupportedActionsSync.getVersion — returns {@code 4}
     * @return the version number
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAndroidUnsupportedActionsSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return AndroidUnsupportedActions.ACTION_VERSION; // WAWebAndroidUnsupportedActionsSync.getVersion: return 4
    }

    /**
     * Applies a single mutation and returns whether it was successful.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and checks whether the outcome is {@code SUCCESS}.
     *
     * @implNote ADAPTED: WAWebAndroidUnsupportedActionsSync.applyMutations — WA Web uses
     *           batch {@code applyMutations} with {@code Promise.all(r.map(...))}; Cobalt
     *           exposes per-mutation entry point for the framework's batch orchestration
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAndroidUnsupportedActionsSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS; // WAWebAndroidUnsupportedActionsSync.applyMutations: actionState === Success
    }

    /**
     * Applies a single mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web, the per-mutation logic within {@code applyMutations}:
     * <ul>
     *   <li>If operation is not SET: returns {@code {actionState: Unsupported}}</li>
     *   <li>If operation is SET but {@code androidUnsupportedActions} is falsy: returns
     *       malformed via {@code WAWebSyncdIndexUtils.malformedActionValue(collectionName)}</li>
     *   <li>If {@code allowed === true}: calls {@code updatePrimaryAllowsAllMutationsFlag}</li>
     *   <li>Returns {@code {actionState: Success}}</li>
     *   <li>On exception: returns {@code {actionState: Failed}}</li>
     * </ul>
     *
     * @implNote WAWebAndroidUnsupportedActionsSync.applyMutations — per-mutation lambda
     *           within the batch map
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAndroidUnsupportedActionsSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        try { // WAWebAndroidUnsupportedActionsSync.applyMutations: try { ... } catch(e) { return {actionState: Failed} }
            if (mutation.operation() != SyncdOperation.SET) { // WAWebAndroidUnsupportedActionsSync.applyMutations: if (e.operation==="set") ... else: return Unsupported
                return MutationApplicationResult.unsupported(); // WAWebAndroidUnsupportedActionsSync.applyMutations: {actionState: SyncActionState.Unsupported}
            }

            if (!(mutation.value().action().orElse(null) instanceof AndroidUnsupportedActions action)) { // WAWebAndroidUnsupportedActionsSync.applyMutations: var r = n.androidUnsupportedActions; if (!r) return malformedActionValue
                return malformedActionValue(); // WAWebAndroidUnsupportedActionsSync.applyMutations: o("WAWebSyncdIndexUtils").malformedActionValue(t.collectionName)
            }

            if (action.allowed()) { // WAWebAndroidUnsupportedActionsSync.applyMutations: l === true
                updatePrimaryAllowsAllMutationsFlag(client); // WAWebAndroidUnsupportedActionsSync.applyMutations: t.updatePrimaryAllowsAllMutationsFlag("allow_unsupported_mutation")
            }

            return MutationApplicationResult.success(); // WAWebAndroidUnsupportedActionsSync.applyMutations: {actionState: SyncActionState.Success}
        } catch (Exception e) { // WAWebAndroidUnsupportedActionsSync.applyMutations: catch(e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebAndroidUnsupportedActionsSync.applyMutations: {actionState: SyncActionState.Failed}
        }
    }

    /**
     * Updates the primary allows-all-mutations flag in the store if it is not already set.
     *
     * <p>Per WhatsApp Web {@code updatePrimaryAllowsAllMutationsFlag}: first checks
     * {@code getPrimaryAllowsAllMutations()} and only sets the flag (and logs) when it
     * is currently falsy. This avoids redundant updates and provides a log entry when
     * the flag transitions from unset to set.
     *
     * @implNote WAWebAndroidUnsupportedActionsSync.updatePrimaryAllowsAllMutationsFlag
     * @param client the WhatsApp client instance providing store access
     */
    @WhatsAppWebExport(moduleName = "WAWebAndroidUnsupportedActionsSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    private void updatePrimaryAllowsAllMutationsFlag(WhatsAppClient client) {
        if (!client.store().primaryAllowsAllMutations()) { // WAWebAndroidUnsupportedActionsSync.updatePrimaryAllowsAllMutationsFlag: o("WAWebUserPrefsAppStateSync").getPrimaryAllowsAllMutations() || ...
            LOGGER.info("[syncd] primary allows all mutations flag set: allow_unsupported_mutation"); // WAWebAndroidUnsupportedActionsSync.updatePrimaryAllowsAllMutationsFlag: WALogger.LOG("[syncd] primary allows all mutations flag set: ", t)
            client.store().setPrimaryAllowsAllMutations(true); // WAWebAndroidUnsupportedActionsSync.updatePrimaryAllowsAllMutationsFlag: o("WAWebUserPrefsAppStateSync").setPrimaryAllowsAllMutations()
        }
    }
}
