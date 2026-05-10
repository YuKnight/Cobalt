package com.github.auties00.cobalt.sync.key;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.message.send.id.MessageIdGenerator;
import com.github.auties00.cobalt.message.send.id.MessageIdVersion;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.chat.ChatMessageInfoBuilder;
import com.github.auties00.cobalt.model.device.sync.MissingDeviceSyncKeyBuilder;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainerBuilder;
import com.github.auties00.cobalt.model.message.MessageKeyBuilder;
import com.github.auties00.cobalt.model.message.system.ProtocolMessage;
import com.github.auties00.cobalt.model.message.system.ProtocolMessageBuilder;
import com.github.auties00.cobalt.model.message.system.appstate.AppStateSyncKeyIdBuilder;
import com.github.auties00.cobalt.model.message.system.appstate.AppStateSyncKeyRequest;
import com.github.auties00.cobalt.model.message.system.appstate.AppStateSyncKeyRequestBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.wam.WamService;
import com.github.auties00.cobalt.wam.event.MdBootstrapAppStateCriticalDataProcessingEventBuilder;
import com.github.auties00.cobalt.wam.type.BootstrapAppStateDataStageCode;

import java.time.Instant;
import java.util.*;

/**
 * Service for requesting missing sync keys from companion devices.
 * <p>
 * Per WhatsApp Web {@code WAWebSyncdHandleMissingKeys}: when a sync key is missing
 * during snapshot or patch decryption, the client detects the missing key IDs,
 * filters out already-tracked keys, sends an {@code AppStateSyncKeyRequest}
 * protocol message to all companion devices, and records the missing keys
 * in the store for timeout tracking.
 * <p>
 * In WA Web, {@code handleMissingKeysInSnapshot} and {@code handleMissingKeysInPatches}
 * are separate exported functions that collect missing key IDs from snapshot records
 * or patch mutations respectively, then delegate to the common {@code handleMissingKeys}.
 * In Cobalt, the collection of missing key IDs is performed inline during decryption
 * in {@code WebAppStateService}, which then calls into this service.
 * <p>
 * Per WhatsApp Web {@code WAWebKeyManagementSendKeyRequestApi}: the actual
 * key request is sent as a peer message to all companion devices.
 * Companion devices that have the key respond with {@code AppStateSyncKeyShare}.
 * <p>
 * Per WhatsApp Web {@code WAWebSyncdRequestAllSyncdMissingKeysJob}: a periodic job
 * (every 6 hours via {@code WAWebTasksDefinitions}) wraps the {@code requestAllMissingKeys}
 * call in a NonPersistedJob with BEST_EFFORT priority and 30-second timeout, then
 * schedules {@code setMissingKeyTimeoutInTransaction} after 20 seconds. In Cobalt,
 * the periodic scheduling is in {@link MissingSyncKeyTimeoutScheduler#startPeriodicReRequestJob()}
 * and the core request logic is in {@link #reRequestMissingKeys(Collection)}.
 */
@WhatsAppWebModule(moduleName = "WAWebSyncdHandleMissingKeys")
@WhatsAppWebModule(moduleName = "WAWebSyncdRequestAllSyncdMissingKeysJob")
@WhatsAppWebModule(moduleName = "WAWebKeyManagementSendKeyRequestApi")
@WhatsAppWebModule(moduleName = "WAWebSyncdStoreMissingKeys")
public final class MissingSyncKeyRequestService {
    /**
     * Logger for this service.
     */
    private static final System.Logger LOGGER = System.getLogger(MissingSyncKeyRequestService.class.getName());

    /**
     * The WhatsApp client instance, used for sending peer messages.
     */
    private final WhatsAppClient client;

    /**
     * The WhatsApp store, used for querying and tracking missing keys.
     */
    private final WhatsAppStore store;

    /**
     * The timeout scheduler used to (re)schedule the missing-key timeout check
     * inline at the end of {@link #trackMissingKeys(Collection, Set)},
     * mirroring WA Web's {@code addMissingKeys} which calls
     * {@code setMissingKeyTimeoutInTransaction} as its final step.
     *
     * <p>This dependency is wired via {@link #setTimeoutScheduler(MissingSyncKeyTimeoutScheduler)}
     * after construction because {@link MissingSyncKeyTimeoutScheduler} also depends on
     * this service, creating a circular construction dependency.
     */
    private MissingSyncKeyTimeoutScheduler timeoutScheduler;

    /**
     * The WAM telemetry service used to commit critical bootstrap stage events.
     */
    private final WamService wamService;

    /**
     * Creates a new missing sync key request service.
     *
     * @param client     the WhatsApp client instance
     * @param wamService the WAM telemetry service for committing critical bootstrap events
     */
    public MissingSyncKeyRequestService(WhatsAppClient client, WamService wamService) {
        this.client = client;
        this.store = client.store();
        this.wamService = wamService;
    }

    /**
     * Wires the timeout scheduler dependency after construction.
     *
     * <p>{@link MissingSyncKeyTimeoutScheduler} cannot be supplied via the constructor
     * because the scheduler also depends on this service for its periodic re-request
     * job, producing a circular dependency. The owning {@link com.github.auties00.cobalt.sync.WebAppStateService}
     * constructs both collaborators and then invokes this method to complete the wiring.
     *
     * @param timeoutScheduler the timeout scheduler instance
     */
    public void setTimeoutScheduler(MissingSyncKeyTimeoutScheduler timeoutScheduler) {
        this.timeoutScheduler = timeoutScheduler;
    }

    /**
     * Requests missing sync keys from companion devices.
     * <p>
     * Per WA Web, this is the entry point called after {@code handleMissingKeysInSnapshot}
     * or {@code handleMissingKeysInPatches} has collected the set of missing key IDs.
     * In Cobalt, the collection is done inline during decryption and this method is called
     * directly with the detected missing key IDs.
     * @param keyIds the IDs of the missing keys
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdHandleMissingKeys",
            exports = {"handleMissingKeysInSnapshot", "handleMissingKeysInPatches"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void requestMissingKeys(Collection<byte[]> keyIds) {
        handleMissingKeys(keyIds);
    }

    /**
     * Requests a single missing sync key from companion devices.
     * @param keyId the ID of the missing key
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdHandleMissingKeys",
            exports = "handleMissingKeys",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void requestMissingKey(byte[] keyId) {
        requestMissingKeys(List.of(keyId));
    }

    /**
     * Re-requests all currently-tracked missing sync keys from companion devices.
     *
     * <p>Per WhatsApp Web {@code requestAllMissingKeys}: fetches all tracked missing keys,
     * then sends a key request without checking resume state or re-tracking the keys
     * (they are already stored in the missing key store).
     *
     * <p>This method provides the core logic for the
     * {@code WAWebSyncdRequestAllSyncdMissingKeysJob.requestAllSyncdMissingKeysJob}
     * periodic job. The job framework wrapper (NonPersistedJob with BEST_EFFORT priority
     * and 30-second timeout) is handled by
     * {@link MissingSyncKeyTimeoutScheduler#startPeriodicReRequestJob()}.
     * @param keyIds the IDs of the missing keys to re-request
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdHandleMissingKeys",
            exports = "requestAllMissingKeys",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebSyncdRequestAllSyncdMissingKeysJob",
            exports = "requestAllSyncdMissingKeysJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void reRequestMissingKeys(Collection<byte[]> keyIds) {
        if (keyIds.isEmpty()) { // WAWebSyncdHandleMissingKeys.requestAllMissingKeys: e.length !== 0
            return;
        }

        // WAWebSyncdHandleMissingKeys.requestAllMissingKeys: directly calls sendSyncdKeyRequest
        // without checking resume state or re-tracking keys
        var keyIdList = keyIds.stream() // WAWebSyncdHandleMissingKeys.requestAllMissingKeys
                .filter(Objects::nonNull)
                .map(id -> new AppStateSyncKeyIdBuilder()
                        .keyId(Arrays.copyOf(id, id.length))
                        .build())
                .toList();
        var keyRequest = new AppStateSyncKeyRequestBuilder() // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest
                .keyIds(keyIdList)
                .build();

        sendKeyRequestToAllDevices(keyRequest); // WAWebSyncdKeyCallbacksApi.sendSyncdKeyRequest
    }

    /**
     * Gets the list of companion devices that can be asked for missing keys.
     * <p>
     * Per WhatsApp Web WAWebKeyManagementUtils.getPeerDevices:
     * Returns all devices from our own device list except the current device.
     * On failure, falls back to the primary device (device 0) only.
     */
    @WhatsAppWebExport(moduleName = "WAWebKeyManagementUtils",
            exports = "getPeerDevices",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private List<Jid> getCompanionDevices() {
        var myJid = store.jid() // ADAPTED: WAWebUserPrefsMeUser.getMeDevicePnOrThrow (store returns Optional)
                .orElse(null);
        if (myJid == null) { // ADAPTED: WAWebUserPrefsMeUser.getMeDevicePnOrThrow (null-safe vs OrThrow)
            return List.of();
        }

        try {
            var myDeviceList = store.findDeviceList(myJid.toUserJid()); // ADAPTED: WAWebApiDeviceList.getMyDeviceList (store lookup returns Optional)
            if (myDeviceList.isEmpty()) { // ADAPTED: WAWebKeyManagementUtils.getPeerDevices catch block (empty store = no data = equivalent to API failure)
                LOGGER.log(System.Logger.Level.WARNING,
                        "[syncd] getPeerDevices: no device list. Key reqs->primary only");
                return List.of(Jid.of(myJid.user(), myJid.server(), 0, 0)); // WAWebWidFactory.createDeviceWidFromUserAndDevice(n.user, n.server, 0)
            }

            return myDeviceList.get()
                    .devices() // WAWebKeyManagementUtils.getPeerDevices: r.devices
                    .stream()
                    .filter(device -> device.id() != myJid.device()) // WAWebKeyManagementUtils.getPeerDevices: e.id !== n.getDeviceId()
                    .map(device -> device.toDeviceJid(myJid.user(), myJid.server())) // WAWebWidFactory.createDeviceWidFromUserAndDevice(n.user, n.server, e.id)
                    .toList(); // WAWebKeyManagementUtils.getPeerDevices: return t
        } catch (Exception e) { // WAWebKeyManagementUtils.getPeerDevices: catch(t)
            LOGGER.log(System.Logger.Level.WARNING, // WAWebKeyManagementUtils.getPeerDevices: WALogger.WARN
                    "[syncd] getPeerDevices: {0}. Key reqs->primary only", e.getMessage());
            return List.of(Jid.of(myJid.user(), myJid.server(), 0, 0)); // WAWebWidFactory.createDeviceWidFromUserAndDevice(n.user, n.server, 0)
        }
    }

    /**
     * Handles missing keys by filtering already-tracked keys, sending a key request to all
     * companion devices, and tracking the newly-missing keys.
     * @param keyIds the IDs of the missing keys (as raw bytes)
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdHandleMissingKeys",
            exports = "handleMissingKeys",
            adaptation = WhatsAppAdaptation.DIRECT)
    private void handleMissingKeys(Collection<byte[]> keyIds) {
        if (keyIds.isEmpty()) { // ADAPTED: defensive early return, no direct WA Web equivalent
            return;
        }

        // WAWebSyncdHandleMissingKeys.handleMissingKeys: var t = isResumeFromRestartComplete() ? "idle" : "processing"
        if (!store.isResumeFromRestartComplete()) { // WAWebSyncdHandleMissingKeys.handleMissingKeys: if (t !== "idle") { return }
            LOGGER.log(System.Logger.Level.DEBUG, "syncd: _handleMissingKeys: skip, resume in progress"); // WAWebSyncdHandleMissingKeys.handleMissingKeys
            return;
        }

        // WAWebSyncdHandleMissingKeys.handleMissingKeys: var a = new Set(bulkGetMissingKeysInTransaction(r)...); var i = r.filter(e => !a.has(e))
        var requestedKeyIds = keyIds.stream()
                .filter(Objects::nonNull) // ADAPTED: defensive null filter (WA Web applies .filter(Boolean) on query results, not input)
                .map(id -> Arrays.copyOf(id, id.length)) // ADAPTED: defensive copy for Java byte[] mutability
                .filter(id -> store.findMissingSyncKey(id).isEmpty()) // ADAPTED: WAWebGetMissingKey.bulkGetMissingKeysInTransaction — individual lookups instead of bulk
                .toList();
        if (requestedKeyIds.isEmpty()) { // WAWebSyncdHandleMissingKeys.handleMissingKeys: if (i.length === 0) return
            return;
        }

        var keyIdList = requestedKeyIds.stream() // ADAPTED: WAWebSyncdHandleMissingKeys.handleMissingKeys: i.map(e => toSyncKeyId(hexToUint8Array(e).buffer))
                .map(id -> new AppStateSyncKeyIdBuilder()
                        .keyId(id)
                        .build())
                .toList();
        var keyRequest = new AppStateSyncKeyRequestBuilder() // ADAPTED: WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest builds request internally
                .keyIds(keyIdList)
                .build();

        var successfulDeviceIds = sendKeyRequestToAllDevices(keyRequest); // WAWebSyncdKeyCallbacksApi.sendSyncdKeyRequest

        trackMissingKeys(requestedKeyIds, successfulDeviceIds); // WAWebSyncdStoreMissingKeys.addMissingKeys(l, s)
    }

    /**
     * Sends a key request to all companion devices and returns the set of successful device IDs.
     * @param keyRequest the key request to send
     * @return the set of device IDs that were successfully sent to
     */
    @WhatsAppWebExport(moduleName = "WAWebKeyManagementSendKeyRequestApi",
            exports = "sendAppStateSyncKeyRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private Set<Integer> sendKeyRequestToAllDevices(AppStateSyncKeyRequest keyRequest) {
        var companionDevices = getCompanionDevices(); // WAWebKeyManagementUtils.getPeerDevices

        // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: a.length === 0 throw
        if (companionDevices.isEmpty()) {
            throw new IllegalStateException(
                    "syncd: sendAppStateSyncKeyRequest: no peer devices available to request key from");
        }

        // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: build messages for all devices upfront
        var messages = buildKeyRequestMessages(companionDevices, keyRequest);

        var keyIdHexes = keyRequest.keyIds().stream() // WAWebSyncdCryptoUtils.syncKeyIdToHex
                .map(id -> id.keyId().map(SyncKeyUtils::syncKeyIdToHex).orElse("?"))
                .toList();
        var deviceIds = companionDevices.stream() // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: a.map(e => e.getDeviceId())
                .map(Jid::device)
                .toList();
        LOGGER.log(System.Logger.Level.INFO, // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: WALogger.LOG
                "syncd: send key request key id {0} to peer deviceIds {1}", keyIdHexes, deviceIds);

        // WAWebApiPeerMessageStore.storePeerMessages: persist before sending for offline retry
        for (var entry : messages.entrySet()) {
            var msgId = entry.getValue().key().id().orElse(null);
            if (msgId != null) {
                store.addPeerMessage(msgId, entry.getValue());
            }
        }

        // ADAPTED: WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: Promise.allSettled
        // Cobalt uses sequential loop instead of parallel sends
        var successfulDeviceIds = new LinkedHashSet<Integer>();
        var failureMessages = new ArrayList<String>();
        for (var entry : messages.entrySet()) { // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: d.map(e => encryptAndSendKeyMsg({msg: e}))
            var device = entry.getKey();
            var messageInfo = entry.getValue();
            try {
                client.sendPeerMessage(device, messageInfo); // WAWebSendAppStateSyncMsgJob.encryptAndSendKeyMsg
                successfulDeviceIds.add(device.device());
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Failed to send key request to device {0}: {1}",
                        device, e.getMessage());
                failureMessages.add(e.getMessage()); // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: getErrorSafe(e.reason).message
            }
        }

        var failureCount = failureMessages.size();
        // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: partial warn, total throw
        if (failureCount > 0 && failureCount < companionDevices.size()) { // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: f.length > 0 && f.length < _.length
            LOGGER.log(System.Logger.Level.WARNING, // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: WALogger.WARN
                    "syncd: sendAppStateSyncKeyRequest: {0}/{1} peer device(s) failed",
                    failureCount, companionDevices.size());
        } else if (failureCount == companionDevices.size()) { // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: f.length === _.length
            var errorDetails = String.join(", ", failureMessages); // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: f.map(e => getErrorSafe(e.reason).message).join(", ")
            LOGGER.log(System.Logger.Level.ERROR, // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: WALogger.ERROR
                    "[syncd] sendAppStateSyncKeyRequest: all {0} peers failed: {1}", companionDevices.size(), errorDetails);
            throw new IllegalStateException( // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: throw err(...)
                    "syncd: sendAppStateSyncKeyRequest failed for all " + companionDevices.size() + " peer device(s): " + errorDetails);
        }

        // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest:
        //   return logCriticalBootstrapStageIfNecessary(MISSING_KEYS_REQUESTED), h;
        // Emission happens AFTER the key request has been dispatched to peers, right
        // before returning the set of fulfilled device IDs.
        logCriticalBootstrapStageIfNecessary(BootstrapAppStateDataStageCode.MISSING_KEYS_REQUESTED);

        return successfulDeviceIds; // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: h (fulfilled device IDs)
    }

    /**
     * Emits a {@code MdBootstrapAppStateCriticalDataProcessingEvent} for the
     * supplied bootstrap stage when the critical data sync is still in progress.
     *
     * <p>Per WhatsApp Web
     * {@code WAWebSyncdCriticalBootstrapProcessingApi.logCriticalBootstrapStageIfNecessary}:
     * the event is gated on
     * {@code WAWebSyncBootstrap.isSyncDCriticalDataSyncInProcess()}. In Cobalt that
     * global state machine is approximated by checking whether the
     * {@link SyncPatchType#CRITICAL_BLOCK} collection has been bootstrapped yet,
     * mirroring {@link com.github.auties00.cobalt.sync.WebAppStateService}.
     * @param stage the bootstrap stage reached; never {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdCriticalBootstrapProcessingApi", exports = "logCriticalBootstrapStageIfNecessary", adaptation = WhatsAppAdaptation.ADAPTED)
    private void logCriticalBootstrapStageIfNecessary(BootstrapAppStateDataStageCode stage) {
        if (store.findWebAppState(SyncPatchType.CRITICAL_BLOCK).bootstrapped()) {
            return;
        }
        wamService.commit(new MdBootstrapAppStateCriticalDataProcessingEventBuilder()
                .bootstrapAppStateDataStage(stage) // WAWebSyncdCriticalBootstrapProcessingApi: bootstrapAppStateDataStage: e
                .mdTimestamp((int) System.currentTimeMillis()) // WAWebSyncdCriticalBootstrapProcessingApi: mdTimestamp: unixTimeMs()
                .build());
    }

    /**
     * Builds key request messages for all companion devices.
     *
     * <p>Per WA Web, all messages are built upfront before storing/sending.
     * Each message uses the user JID (not device JID) as the message key's remote.
     * @param companionDevices the list of companion device JIDs
     * @param keyRequest the key request payload
     * @return a map from device JID to the built message info
     */
    @WhatsAppWebExport(moduleName = "WAWebKeyManagementSendKeyRequestApi",
            exports = "sendAppStateSyncKeyRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private LinkedHashMap<Jid, ChatMessageInfo> buildKeyRequestMessages(List<Jid> companionDevices, AppStateSyncKeyRequest keyRequest) {
        var self = store.jid().orElseThrow(() -> // WAWebUserPrefsMeUser.getMePnUserOrThrow
                new IllegalStateException("syncd: sendAppStateSyncKeyRequest: no JID available"));

        var protocolMessage = new ProtocolMessageBuilder() // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: appStateSyncKeyRequest: l
                .type(ProtocolMessage.Type.APP_STATE_SYNC_KEY_REQUEST)
                .appStateSyncKeyRequest(keyRequest)
                .build();
        var messageContainer = new MessageContainerBuilder() // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest
                .protocolMessage(protocolMessage)
                .build();

        var userJid = self.toUserJid(); // WAWebUserPrefsMeUser.getMePnUserOrThrow: returns user-level JID (no device)
        var messages = new LinkedHashMap<Jid, ChatMessageInfo>(); // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: d = a.map(...)
        for (var device : companionDevices) { // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: a.map(function(e){...})
            var messageKey = new MessageKeyBuilder()
                    .id(MessageIdGenerator.generate(MessageIdVersion.V1, self)) // WAWebMsgKey.newId_DEPRECATED()
                    .parentJid(userJid) // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: remote: getMePnUserOrThrow()
                    .fromMe(true) // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: fromMe: true
                    .senderJid(self) // ADAPTED: Cobalt message infrastructure requires senderJid
                    .build();
            var messageInfo = new ChatMessageInfoBuilder()
                    .key(messageKey)
                    .message(messageContainer) // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: appStateSyncKeyRequest: l (shared across all messages)
                    .timestamp(Instant.now()) // ADAPTED: Cobalt message infrastructure requires timestamp
                    .senderJid(self) // ADAPTED: Cobalt message infrastructure requires senderJid
                    .build();
            messages.put(device, messageInfo); // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: {id: t, to: e, ...}
        }
        return messages;
    }

    /**
     * Tracks missing keys in the store, always creating fresh entries for each key,
     * then (re)schedules the missing-key timeout check.
     *
     * <p>Per WhatsApp Web: each entry is created with the current timestamp and a
     * {@code deviceResponses} map of {@code deviceId -> null} for every successfully
     * contacted device. Existing entries for the same key are overwritten (upsert
     * via {@code bulkUpdateMissingKeysInTransaction}). After the bulk update,
     * {@code addMissingKeys} calls {@code setMissingKeyTimeoutInTransaction} inline
     * as its final step so that the tracker always reschedules its expiry check
     * whenever new missing keys are recorded.
     * @param keyIds the IDs of the missing keys
     * @param successfulDeviceIds the set of device IDs that were successfully asked
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdStoreMissingKeys",
            exports = "addMissingKeys",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private void trackMissingKeys(Collection<byte[]> keyIds, Set<Integer> successfulDeviceIds) {
        for (var keyId : keyIds) { // WAWebSyncdStoreMissingKeys.addMissingKeys: r = e.map(function(e){return{...}})
            var missingKey = new MissingDeviceSyncKeyBuilder() // WAWebSyncdStoreMissingKeys.addMissingKeys: { keyHex, keyId, timestamp, deviceResponses }
                    .keyId(keyId) // WAWebSyncdStoreMissingKeys.addMissingKeys: keyId: e
                    .timestamp(Instant.now()) // WAWebSyncdStoreMissingKeys.addMissingKeys: timestamp: o("WATimeUtils").unixTimeMs()
                    .askedDevices(Set.copyOf(successfulDeviceIds)) // ADAPTED: WAWebSyncdStoreMissingKeys.addMissingKeys: deviceResponses: n() — Map<deviceId, null> mapped to Set<Integer>
                    .build();
            store.addMissingSyncKey(missingKey); // ADAPTED: WAWebGetMissingKey.bulkUpdateMissingKeysInTransaction — per-entry upsert instead of bulk
        }

        // WAWebSyncdStoreMissingKeys.addMissingKeys — invokes setMissingKeyTimeoutInTransaction inline at end
        if (timeoutScheduler != null) {
            timeoutScheduler.scheduleTimeoutCheck(); // WAWebSyncdStoreMissingKeys.addMissingKeys: yield k() (setMissingKeyTimeoutInTransaction)
        }
    }
}
