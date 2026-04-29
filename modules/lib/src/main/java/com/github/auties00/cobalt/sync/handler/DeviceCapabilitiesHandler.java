package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.DeviceCapabilities;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.event.Lid11MigrationLifecycleEventBuilder;
import com.github.auties00.cobalt.wam.type.MigrationStageEnum;
import com.github.auties00.cobalt.wam.WamService;

import java.util.HashMap;
import java.util.Objects;

/**
 * Handles the {@code device_capabilities} app-state sync action, which
 * synchronizes device capability information (chat lock support, AI thread
 * support, LID migration status, avatar presence, business broadcast support,
 * and member name tag support) from the primary device to companion devices.
 *
 * <p>Per WhatsApp Web {@code WAWebDeviceCapabilitiesSync.applyMutations}, for
 * every mutation whose operation is {@code SET}, the index is parsed as a JID
 * and the device component is extracted. When the device identifier is
 * {@code 0} (the primary device) the payload is applied to the local store
 * via {@code mergeDeviceCapabilitiesToStorage(capabilities, "primary")}. Any
 * other device is accepted with no side effect.
 *
 * <p>WhatsApp Web additionally:
 * <ul>
 *   <li>registers an event-bus listener ({@code checkLidTimeout}) that
 *       schedules a delayed logout when the LID 1-to-1 migration deadline
 *       elapses &mdash; skipped in Cobalt (timeout/lifecycle telemetry);</li>
 *   <li>commits a {@code Lid11MigrationLifecycleWamEvent} WAM telemetry
 *       event when a new {@code chatDbMigrationTimestamp} arrives &mdash;
 *       mirrored in Cobalt via
 *       {@link com.github.auties00.cobalt.wam.event.Lid11MigrationLifecycleEvent}
 *       with {@code migrationStage = COMPANION_RECEIVED_DEVICE_CAPABILITY};</li>
 *   <li>triggers {@code frontendFireAndForget("initializeMetaAiBotAiThreads")}
 *       when {@code aiThread.supportLevel} is {@code INFRA} or {@code FULL}
 *       &mdash; skipped in Cobalt (frontend-only RPC);</li>
 *   <li>when running as the SMB client, diffs
 *       {@code businessBroadcast.companionSupportEnabled &&
 *       businessBroadcast.campaignSyncEnabled} against the stored primary
 *       support flag and on change calls
 *       {@code saveBizBroadcastCapabilityToStorage} and fires
 *       {@code loadQuickPromotions} &mdash; skipped in Cobalt because the
 *       {@link DeviceCapabilities.BusinessBroadcast} protobuf message
 *       intentionally omits those sub-fields;</li>
 *   <li>logs {@code "[DeviceCapabilitiesSync] primary caps updated Nx"} via
 *       {@code WALogger} &mdash; omitted in Cobalt (logging).</li>
 * </ul>
 *
 * @implNote WAWebDeviceCapabilitiesSync &mdash; singleton instance of a class
 *           extending {@code AccountSyncdActionBase} from
 *           {@code WAWebSyncdAction}. Declares
 *           {@code collectionName = RegularLow}, {@code getVersion() = 7} and
 *           {@code getAction() = "device_capabilities"}. Outbound
 *           {@code getMutation}/{@code sendMutation} are not mirrored
 *           per-handler in Cobalt: outbound mutation construction is handled
 *           centrally by {@code WebAppStateService}.
 */
@WhatsAppWebModule(moduleName = "WAWebDeviceCapabilitiesSync")
public final class DeviceCapabilitiesHandler implements WebAppStateActionHandler {
    /**
     * Primary device identifier constant.
     *
     * @implNote WAWebDeviceCapabilitiesSync: {@code var c = "0"}. A value of
     *           {@code 0} in the {@code device} component of a JID identifies
     *           the primary device of the account.
     */
    @WhatsAppWebExport(moduleName = "WAWebDeviceCapabilitiesSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    private static final int PRIMARY_DEVICE = 0; // WAWebDeviceCapabilitiesSync: c = "0"

    /**
     * Index of the JID element inside the mutation's {@code indexParts}
     * array.
     *
     * @implNote WAWebDeviceCapabilitiesSync: {@code var d = 1}. The mutation
     *           index is {@code [hash, jidString]}; element {@code 1} holds
     *           the target device JID in legacy format.
     */
    @WhatsAppWebExport(moduleName = "WAWebDeviceCapabilitiesSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    private static final int JID_INDEX = 1; // WAWebDeviceCapabilitiesSync: d = 1

    /**
     * Singleton instance of this handler.
     *
     * @implNote WAWebDeviceCapabilitiesSync.default &mdash; the module exports
     *           a single instance ({@code var f = new _()}) as its default
     *           export.
     */
    @WhatsAppWebExport(moduleName = "WAWebDeviceCapabilitiesSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final DeviceCapabilitiesHandler INSTANCE = new DeviceCapabilitiesHandler();

    /**
     * Constructs the singleton device capabilities handler.
     *
     * <p>Per WhatsApp Web, the constructor of class {@code _} inherits from
     * {@code AccountSyncdActionBase} and sets
     * {@code this.collectionName = WASyncdConst.CollectionName.RegularLow}.
     * The {@code collectionName} assignment is surfaced in Cobalt via
     * {@link #collectionName()} rather than as an instance field.
     *
     * @implNote WAWebDeviceCapabilitiesSync &mdash; constructor of class
     *           {@code _} extending {@code AccountSyncdActionBase}.
     */
    @WhatsAppWebExport(moduleName = "WAWebDeviceCapabilitiesSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private DeviceCapabilitiesHandler() {
        // WAWebDeviceCapabilitiesSync constructor: this.collectionName = RegularLow
    }

    /**
     * Returns the action name for device capabilities sync.
     *
     * @implNote WAWebDeviceCapabilitiesSync.getAction &mdash; returns
     *           {@code WASyncdConst.Actions.DeviceCapabilities} which
     *           resolves to {@code "device_capabilities"}.
     * @return the action name {@code "device_capabilities"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeviceCapabilitiesSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return DeviceCapabilities.ACTION_NAME; // WAWebDeviceCapabilitiesSync.getAction
    }

    /**
     * Returns the sync collection for device capabilities mutations.
     *
     * @implNote WAWebDeviceCapabilitiesSync.collectionName &mdash; set in the
     *           constructor to {@code WASyncdConst.CollectionName.RegularLow}.
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeviceCapabilitiesSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return DeviceCapabilities.COLLECTION_NAME; // WAWebDeviceCapabilitiesSync: this.collectionName = RegularLow
    }

    /**
     * Returns the mutation format version for device capabilities.
     *
     * @implNote WAWebDeviceCapabilitiesSync.getVersion &mdash; returns
     *           {@code 7}.
     * @return the version number {@code 7}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeviceCapabilitiesSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return DeviceCapabilities.ACTION_VERSION; // WAWebDeviceCapabilitiesSync.getVersion: return 7
    }

    /**
     * Applies a single device capabilities mutation by delegating to
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}.
     *
     * @implNote ADAPTED: WAWebDeviceCapabilitiesSync.applyMutations &mdash;
     *           WA Web processes mutations in a batch loop returning per
     *           mutation {@code {actionState: SyncActionState.Success}};
     *           Cobalt delegates to the result-returning variant for
     *           consistency with the handler interface.
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied with state
     *         {@link SyncActionState#SUCCESS}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeviceCapabilitiesSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies a single device capabilities mutation and returns a detailed
     * result.
     *
     * <p>Per WhatsApp Web, for each mutation with
     * {@code e.operation === "set"}:
     * <ol>
     *   <li>extracts the target JID from {@code e.indexParts[1]};</li>
     *   <li>extracts the device component via {@code m(jidString)} which
     *       returns the substring between {@code ':'} and {@code '@'};</li>
     *   <li>when the device identifier equals {@code "0"} (primary), maps
     *       the protobuf payload through
     *       {@code mapProtobufToAllDeviceCapabilities} and merges the
     *       result into storage via
     *       {@code mergeDeviceCapabilitiesToStorage(caps, "primary")}.</li>
     * </ol>
     *
     * <p>Non-{@code SET} operations, mutations with an empty or missing JID
     * part and mutations carrying a non {@code deviceCapabilities} action are
     * silently accepted as {@link SyncActionState#SUCCESS}, matching WA Web
     * which always returns {@code {actionState: Success}} from the loop body.
     *
     * @implNote WAWebDeviceCapabilitiesSync.applyMutations &mdash; per
     *           mutation logic inside the batch handler. Differences vs. WA
     *           Web are called out inline via {@code ADAPTED} comments.
     *           WA Web only persists the primary device payload; Cobalt also
     *           keeps a map keyed by JID (see
     *           {@code WhatsAppStore.deviceCapabilitiesStates}) which is
     *           used to skip redundant primary updates when the payload is
     *           identical to the previous one.
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return a {@link MutationApplicationResult} with
     *         {@link SyncActionState#SUCCESS} state
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDeviceCapabilitiesSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebDeviceCapabilitiesSync.applyMutations: if (e.operation === "set")
            return MutationApplicationResult.success(); // WAWebDeviceCapabilitiesSync.applyMutations: return {actionState: Success}
        }

        // WAWebDeviceCapabilitiesSync.applyMutations: var i = e.indexParts[d]
        var indexArray = JSON.parseArray(mutation.index());
        var deviceJidString = indexArray.size() > JID_INDEX ? indexArray.getString(JID_INDEX) : null;
        // WAWebDeviceCapabilitiesSync.applyMutations: var a = e.value?.deviceCapabilities
        // ADAPTED: WA Web accesses the field directly on the decoded protobuf value; Cobalt
        // resolves the polymorphic SyncAction and narrows it via instanceof.
        var capabilities = mutation.value().action().orElse(null) instanceof DeviceCapabilities entry
                ? entry
                : null;
        // WAWebDeviceCapabilitiesSync.applyMutations: if (a != null) ... and implicit null
        // check on indexParts[d] before m(i)
        if (capabilities == null || deviceJidString == null || deviceJidString.isBlank()) {
            return MutationApplicationResult.success(); // WAWebDeviceCapabilitiesSync.applyMutations: fall through to return Success
        }

        // ADAPTED: NO_WA_BASIS - Cobalt tracks a per-JID map so repeated primary mutations with
        // an identical payload can be fast-pathed. WA Web always re-merges the storage entry.
        var states = new HashMap<>(client.store().deviceCapabilitiesStates());
        var previous = states.put(deviceJidString, capabilities);
        client.store().setDeviceCapabilitiesStates(states);
        if (Objects.equals(previous, capabilities)) {
            return MutationApplicationResult.success();
        }

        // WAWebDeviceCapabilitiesSync.applyMutations: var l = i != null ? m(i) : null; if (l === c)
        // where m() extracts the substring between ':' and '@' from the legacy JID string.
        // Jid.of(...).device() returns the same integer decoded from the ":<device>@" section.
        var deviceJid = Jid.of(deviceJidString);
        if (deviceJid.device() == PRIMARY_DEVICE) { // WAWebDeviceCapabilitiesSync.applyMutations: if (l === c)
            // WAWebDeviceCapabilitiesSync.applyMutations:
            //   u = mapProtobufToAllDeviceCapabilities(a);
            //   mergeDeviceCapabilitiesToStorage(u, "primary");
            // ADAPTED: WA Web flattens the payload into {chatLockSupportLevel, aiThread.supportLevel}
            // with NONE defaults before merging. Cobalt persists the full protobuf object: the
            // generated getters already coalesce null into default values where relevant.
            client.store().setPrimaryDeviceCapabilities(capabilities);
            // ADAPTED: WAWebHasAvatarDeviceCapability - Cobalt eagerly publishes the
            // userHasAvatar flag to the store during sync; WA Web reads it on demand.
            capabilities.userHasAvatar()
                    .ifPresent(avatar -> client.store().setHasAvatar(avatar.userHasAvatar()));
            // WAWebDeviceCapabilitiesSync.applyMutations: e.value.deviceCapabilities.lidMigration.chatDbMigrationTimestamp != null
            // Forwards the timestamp to the LidMigrationService so it can progress the local
            // migration state machine, and emits the companion-received-device-capability WAM
            // event when the timestamp is present and the account is not yet LID-migrated,
            // exactly matching the guarded WA Web emission.
            capabilities.lidMigration()
                    .flatMap(DeviceCapabilities.LIDMigration::chatDbMigrationTimestamp)
                    .ifPresent(timestamp -> {
                        client.lidMigrationService().observeChatDbMigrationTimestamp(timestamp);
                        // WAWebDeviceCapabilitiesSync.applyMutations:
                        //   chatDbMigrationTimestamp != null && !Lid1X1MigrationUtils.isLidMigrated()
                        //     && new Lid11MigrationLifecycleWamEvent({
                        //          migrationStage: COMPANION_RECEIVED_DEVICE_CAPABILITY,
                        //          isLocally1x1MigratedFromDb: Lid1X1MigrationUtils.isLidMigrated()
                        //        }).commit()
                        // WA Web always passes the post-check value which is false at this point;
                        // Cobalt keeps the same read so the wire payload stays byte-identical.
                        if (!client.lidMigrationService().isLidMigrated()) {
                            wamService.commit(new Lid11MigrationLifecycleEventBuilder()
                                    .migrationStage(MigrationStageEnum.COMPANION_RECEIVED_DEVICE_CAPABILITY)
                                    .isLocally1x1MigratedFromDb(client.lidMigrationService().isLidMigrated())
                                    .build());
                        }
                    });
            // NO_WA_BASIS for the following WA Web behaviours, deliberately omitted:
            //   - frontendFireAndForget("initializeMetaAiBotAiThreads", {}) when aiThread.supportLevel
            //     is INFRA or FULL (frontend-only RPC);
            //   - isSMB()-gated business broadcast flag diff and workerSafeFireAndForget
            //     ("loadQuickPromotions", {trigger:"prefetch"}) (Cobalt's
            //     DeviceCapabilities.BusinessBroadcast does not parse companionSupportEnabled /
            //     campaignSyncEnabled);
            //   - WALogger.LOG("[DeviceCapabilitiesSync] primary caps updated Nx", counter).
        }
        return MutationApplicationResult.success(); // WAWebDeviceCapabilitiesSync.applyMutations: return {actionState: Success}
    }
}
