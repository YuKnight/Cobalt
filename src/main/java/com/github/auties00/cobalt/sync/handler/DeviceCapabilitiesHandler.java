package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.HashMap;

/**
 * Handles the "deviceCapabilities" sync action, which synchronizes device capability
 * information (such as chat lock support, AI thread support, LID migration status,
 * and avatar presence) across companion devices.
 *
 * <p>When the incoming mutation targets the primary device (device {@code 0}), this
 * handler stores the capabilities as the primary device capabilities and extracts
 * relevant fields (e.g., avatar status, LID migration timestamp) into dedicated
 * store properties.
 *
 * @implNote WAWebDeviceCapabilitiesSync — singleton instance created via
 *           {@code new _()} extending {@code AccountSyncdActionBase} from
 *           {@code WAWebSyncdAction}. Declares {@code collectionName = RegularLow},
 *           {@code getVersion() = 7}, {@code getAction() = "device_capabilities"}.
 */
public final class DeviceCapabilitiesHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of this handler.
     *
     * @implNote WAWebDeviceCapabilitiesSync.default — module exports a single instance
     *           ({@code new _()}) as the default export
     */
    public static final DeviceCapabilitiesHandler INSTANCE = new DeviceCapabilitiesHandler();

    /**
     * Constructs the singleton device capabilities handler.
     *
     * @implNote WAWebDeviceCapabilitiesSync.default — instantiated once at module load
     */
    private DeviceCapabilitiesHandler() {

    }

    /**
     * Returns the action name for device capabilities sync.
     *
     * @implNote WAWebDeviceCapabilitiesSync.getAction — returns
     *           {@code WASyncdConst.Actions.DeviceCapabilities} which is
     *           {@code "device_capabilities"}
     * @return the action name {@code "device_capabilities"}
     */
    @Override
    public String actionName() {
        return com.github.auties00.cobalt.model.device.DeviceCapabilities.ACTION_NAME;
    }

    /**
     * Returns the sync collection for device capabilities mutations.
     *
     * @implNote WAWebDeviceCapabilitiesSync.collectionName — set in constructor to
     *           {@code WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return com.github.auties00.cobalt.model.device.DeviceCapabilities.COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for device capabilities.
     *
     * @implNote WAWebDeviceCapabilitiesSync.getVersion — returns {@code 7}
     * @return the version number {@code 7}
     */
    @Override
    public int version() {
        return com.github.auties00.cobalt.model.device.DeviceCapabilities.ACTION_VERSION;
    }

    /**
     * Applies a single device capabilities mutation by delegating to
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}.
     *
     * @implNote ADAPTED: WAWebDeviceCapabilitiesSync.applyMutations — WA Web processes
     *           mutations in a batch loop returning per-mutation
     *           {@code {actionState: SyncActionState.Success}}; Cobalt delegates to the
     *           result-returning variant for consistency with the handler interface
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    /**
     * Applies a single device capabilities mutation and returns a detailed result.
     *
     * <p>Per WA Web, for each mutation with {@code operation === "set"}:
     * <ol>
     *   <li>Extracts the JID from {@code indexParts[1]}</li>
     *   <li>Extracts the device number from the JID string</li>
     *   <li>If the device is the primary device ({@code 0}):
     *     <ul>
     *       <li>Stores the capabilities as primary device capabilities</li>
     *       <li>Extracts avatar status from {@code userHasAvatar}</li>
     *       <li>Observes LID migration timestamp if present</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p>Non-SET operations and mutations without valid capabilities or JID are
     * silently accepted as successful.
     *
     * @implNote WAWebDeviceCapabilitiesSync.applyMutations — per-mutation logic within
     *           the batch handler. WA Web extracts device via helper function
     *           {@code m(indexParts[1])} and compares with {@code "0"} for primary;
     *           Cobalt uses {@code Jid.of(jidString).device() == 0}.
     *           WA Web calls {@code mapProtobufToAllDeviceCapabilities} and
     *           {@code mergeDeviceCapabilitiesToStorage} for primary device;
     *           Cobalt stores the full protobuf object directly.
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return a {@link MutationApplicationResult} with {@code SUCCESS} state
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != com.github.auties00.cobalt.model.sync.data.SyncdOperation.SET) { // WAWebDeviceCapabilitiesSync.applyMutations: e.operation === "set"
            return MutationApplicationResult.success();
        }

        var indexArray = JSON.parseArray(mutation.index()); // WAWebDeviceCapabilitiesSync.applyMutations: e.indexParts
        var deviceJidString = indexArray.size() > 1 ? indexArray.getString(1) : null; // WAWebDeviceCapabilitiesSync.applyMutations: e.indexParts[d] where d=1
        var capabilities = mutation.value().action().orElse(null) instanceof com.github.auties00.cobalt.model.device.DeviceCapabilities entry // ADAPTED: WAWebDeviceCapabilitiesSync.applyMutations: e.value.deviceCapabilities — WA Web accesses field directly; Cobalt uses polymorphic action() + instanceof
                ? entry
                : null;
        if (capabilities == null || deviceJidString == null || deviceJidString.isBlank()) { // WAWebDeviceCapabilitiesSync.applyMutations: if (a != null) and indexParts null check
            return MutationApplicationResult.success();
        }

        var states = new HashMap<>(client.store().deviceCapabilitiesStates()); // ADAPTED: NO_WA_BASIS — Cobalt tracks all device capabilities in a map; WA Web only stores primary
        var previous = states.put(deviceJidString, capabilities); // ADAPTED: NO_WA_BASIS — used for change detection
        client.store().setDeviceCapabilitiesStates(states); // ADAPTED: NO_WA_BASIS — persists all device capabilities
        if (java.util.Objects.equals(previous, capabilities)) { // ADAPTED: NO_WA_BASIS — skip redundant primary update
            return MutationApplicationResult.success();
        }

        var deviceJid = Jid.of(deviceJidString); // WAWebDeviceCapabilitiesSync.applyMutations: m(i) extracts device from JID
        if (deviceJid.device() == 0) { // WAWebDeviceCapabilitiesSync.applyMutations: l === c where c="0"
            client.store().setPrimaryDeviceCapabilities(capabilities); // WAWebDeviceCapabilitiesSync.applyMutations: mergeDeviceCapabilitiesToStorage(u, "primary")
            capabilities.userHasAvatar().ifPresent(avatar -> client.store().setHasAvatar(avatar.userHasAvatar())); // ADAPTED: WAWebHasAvatarDeviceCapability — extracted proactively during sync instead of reading from DB on demand
            capabilities.lidMigration() // WAWebDeviceCapabilitiesSync.applyMutations: e.value.deviceCapabilities.lidMigration.chatDbMigrationTimestamp
                    .flatMap(com.github.auties00.cobalt.model.device.DeviceCapabilities.LIDMigration::chatDbMigrationTimestamp)
                    .ifPresent(client.lidMigrationService()::observeChatDbMigrationTimestamp); // ADAPTED: WAWebDeviceCapabilitiesSync.applyMutations — WA Web fires WAM event; Cobalt triggers migration observation
        }
        return MutationApplicationResult.success(); // WAWebDeviceCapabilitiesSync.applyMutations: {actionState: SyncActionState.Success}
    }
}
