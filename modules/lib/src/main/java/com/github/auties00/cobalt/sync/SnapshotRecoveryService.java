package com.github.auties00.cobalt.sync;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.exception.WhatsAppWebAppStateSyncException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.message.send.id.MessageIdGenerator;
import com.github.auties00.cobalt.message.send.id.MessageIdVersion;
import com.github.auties00.cobalt.model.chat.ChatMessageInfoBuilder;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainerBuilder;
import com.github.auties00.cobalt.model.message.MessageKeyBuilder;
import com.github.auties00.cobalt.model.message.system.ProtocolMessage;
import com.github.auties00.cobalt.model.message.system.ProtocolMessageBuilder;
import com.github.auties00.cobalt.model.message.system.peer.PeerDataOperationRequestMessageBuilder;
import com.github.auties00.cobalt.model.message.system.peer.PeerDataOperationRequestMessageSyncDCollectionFatalRecoveryRequestBuilder;
import com.github.auties00.cobalt.model.message.system.peer.PeerDataOperationRequestResponseMessage;
import com.github.auties00.cobalt.model.message.system.peer.PeerDataOperationRequestType;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.data.SyncdSnapshotRecovery;
import com.github.auties00.cobalt.model.sync.data.SyncdSnapshotRecoverySpec;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.wam.WamService;
import com.github.auties00.cobalt.wam.event.NonMessagePeerDataRequestEventBuilder;
import com.github.auties00.cobalt.wam.type.PeerDataRequestType;
import it.auties.protobuf.stream.ProtobufInputStream;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Service for handling snapshot recovery when a snapshot MAC validation fails.
 *
 * <p>Per WhatsApp Web {@code WAWebRequestSyncdSnapshotRecovery}: when a snapshot
 * MAC mismatch occurs, the client can request a corrected snapshot from the
 * primary device. This allows recovery from data corruption without requiring
 * a full re-link.
 *
 * <p>The recovery flow is:
 * <ol>
 *   <li>Check if recovery should be attempted (AB prop gating)</li>
 *   <li>Send a {@code COMPANION_SYNCD_SNAPSHOT_FATAL_RECOVERY} peer data
 *       operation request to the primary device</li>
 *   <li>Wait for the response (with timeout)</li>
 *   <li>Process the recovered snapshot data</li>
 * </ol>
 *
 * <p>Recovery is gated by:
 * <ul>
 *   <li>AB prop {@code enable_peer_snapshot_recovery} must be enabled</li>
 *   <li>Collection must not be {@code CRITICAL_BLOCK}</li>
 * </ul>
 *
 * @implNote WAWebRequestSyncdSnapshotRecovery.SyncdSnapshotRecoveryModule,
 *           WAWebSyncdSnapshotRecoveryGatingUtils.shouldPreformSnapshotRecovery,
 *           WAWebSyncdSnapshotRecoveryGatingUtils.syncdSnapshotRecoveryEnabled,
 *           WAWebSyncdSnapshotRecoveryGatingUtils.updatePrimaryDeviceSupportsSyncdRecovery,
 *           WAWebSendNonMessageDataRequest.sendPeerDataOperationRequest (snapshot recovery path)
 */
@WhatsAppWebModule(moduleName = "WAWebRequestSyncdSnapshotRecovery")
@WhatsAppWebModule(moduleName = "WAWebSyncdSnapshotRecoveryGatingUtils")
@WhatsAppWebModule(moduleName = "WAWebSendNonMessageDataRequest")
public final class SnapshotRecoveryService {
    private static final Logger LOGGER = Logger.getLogger(SnapshotRecoveryService.class.getName());
    private static final long RECOVERY_TIMEOUT_MS = 60_000; // WAWebRequestSyncdSnapshotRecovery: var p = 6e4

    private final WhatsAppClient client; // ADAPTED: WAWebRequestSyncdSnapshotRecovery constructor DI
    private final ABPropsService abPropsService; // ADAPTED: WAWebSyncdSnapshotRecoveryGatingUtils uses WAWebABProps directly
    /**
     * The WAM telemetry service used to commit recovery-request events.
     */
    private final WamService wamService;
    private final Map<SyncPatchType, CompletableFuture<SyncdSnapshotRecovery>> pendingRecoveries; // ADAPTED: WAWebRequestSyncdSnapshotRecovery: this.recoveryPromise = new Map — holds the decoded snapshot to avoid double-decoding
    private final Semaphore recoverySemaphore; // ADAPTED: WAWebRequestSyncdSnapshotRecovery: this.recoveryInflight (Resolvable)

    /**
     * Creates a new snapshot recovery service.
     *
     * @param client         the WhatsApp client for sending messages
     * @param abPropsService the AB props service for gating checks
     * @param wamService     the WAM telemetry service for committing events
     * @implNote WAWebRequestSyncdSnapshotRecovery: constructor of class _
     */
    @WhatsAppWebExport(moduleName = "WAWebRequestSyncdSnapshotRecovery", exports = "SyncdSnapshotRecoveryModule", adaptation = WhatsAppAdaptation.ADAPTED)
    public SnapshotRecoveryService(WhatsAppClient client, ABPropsService abPropsService, WamService wamService) {
        this.client = client; // ADAPTED: WAWebRequestSyncdSnapshotRecovery constructor DI
        this.abPropsService = abPropsService; // ADAPTED: WAWebSyncdSnapshotRecoveryGatingUtils uses WAWebABProps directly
        this.wamService = wamService;
        this.pendingRecoveries = new ConcurrentHashMap<>(); // WAWebRequestSyncdSnapshotRecovery: this.recoveryPromise = new Map
        this.recoverySemaphore = new Semaphore(1); // ADAPTED: WAWebRequestSyncdSnapshotRecovery: this.recoveryInflight = null
    }

    /**
     * Checks whether snapshot recovery is enabled at all, combining the primary
     * device support flag with the AB prop gating.
     *
     * @return {@code true} if the primary device supports syncd recovery AND the
     *         {@code enable_peer_snapshot_recovery} AB prop is enabled
     * @implNote WAWebSyncdSnapshotRecoveryGatingUtils.syncdSnapshotRecoveryEnabled
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdSnapshotRecoveryGatingUtils", exports = "syncdSnapshotRecoveryEnabled", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean isRecoveryEnabled() {
        if (!client.store().primaryDeviceSupportsSyncdRecovery()) { // WAWebSyncdSnapshotRecoveryGatingUtils.e: userPrefsIdb.get("WAPrimaryDeviceSupportsSyncdRecovery") === true
            return false;
        }

        return abPropsService.getBool(ABProp.ENABLE_PEER_SNAPSHOT_RECOVERY); // WAWebSyncdSnapshotRecoveryGatingUtils.s: getABPropConfigValue("enable_peer_snapshot_recovery")
    }

    /**
     * Updates the stored flag indicating whether the primary device supports
     * syncd snapshot recovery.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdSnapshotRecoveryGatingUtils.updatePrimaryDeviceSupportsSyncdRecovery}:
     * this persists the {@code WAPrimaryDeviceSupportsSyncdRecovery} user preference,
     * which is checked by {@link #isRecoveryEnabled()} to gate snapshot recovery.
     *
     * @param supported {@code true} if the primary device supports syncd recovery
     * @implNote WAWebSyncdSnapshotRecoveryGatingUtils.updatePrimaryDeviceSupportsSyncdRecovery
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdSnapshotRecoveryGatingUtils", exports = "updatePrimaryDeviceSupportsSyncdRecovery", adaptation = WhatsAppAdaptation.ADAPTED)
    public void updatePrimaryDeviceSupportsSyncdRecovery(boolean supported) {
        client.store().setPrimaryDeviceSupportsSyncdRecovery(supported); // WAWebSyncdSnapshotRecoveryGatingUtils.c: userPrefsIdb.set("WAPrimaryDeviceSupportsSyncdRecovery", e)
    }

    /**
     * Checks whether snapshot recovery should be attempted for the given collection.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdSnapshotRecoveryGatingUtils.shouldPreformSnapshotRecovery}:
     * recovery is only attempted when all gating conditions are met:
     * <ol>
     *   <li>Recovery is enabled ({@link #isRecoveryEnabled()})</li>
     *   <li>Collection is not {@code CRITICAL_BLOCK}</li>
     *   <li>Mutation count does not exceed
     *       {@code snapshot_recovery_max_mutations_count_allowed}</li>
     * </ol>
     *
     * @param collectionName the collection that failed snapshot MAC validation
     * @param mutationCount  the number of mutations in the snapshot
     * @return {@code true} if recovery should be attempted
     * @implNote WAWebSyncdSnapshotRecoveryGatingUtils.shouldPreformSnapshotRecovery
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdSnapshotRecoveryGatingUtils", exports = "shouldPreformSnapshotRecovery", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean shouldAttemptRecovery(SyncPatchType collectionName, int mutationCount) {
        // ADAPTED: WA Web checks `r instanceof SyncdFatalError` here; Cobalt checks e.isFatal() at call-site
        if (!isRecoveryEnabled()) { // WAWebSyncdSnapshotRecoveryGatingUtils.d -> s(): syncdSnapshotRecoveryEnabled()
            return false;
        }

        if (collectionName == SyncPatchType.CRITICAL_BLOCK) { // WAWebSyncdSnapshotRecoveryGatingUtils.d: t === CollectionName.CriticalBlock
            return false;
        }

        var maxMutations = abPropsService.getInt(ABProp.SNAPSHOT_RECOVERY_MAX_MUTATIONS_COUNT_ALLOWED); // WAWebSyncdSnapshotRecoveryGatingUtils.d: getABPropConfigValue("snapshot_recovery_max_mutations_count_allowed")
        return mutationCount <= maxMutations; // WAWebSyncdSnapshotRecoveryGatingUtils.d: n > a ? ... : {shouldPerformRecovery: true}
    }

    /**
     * Requests a snapshot recovery from the primary device.
     *
     * <p>Sends a {@code COMPANION_SYNCD_SNAPSHOT_FATAL_RECOVERY} peer data
     * operation request and waits for the response with a timeout.
     *
     * <p>Per WhatsApp Web behavior, recovery requests are serialized globally
     * to prevent multiple concurrent recoveries across different collections.
     * The total time for semaphore acquisition plus waiting for the response
     * is bounded by a single {@link #RECOVERY_TIMEOUT_MS} window, matching
     * the WA Web {@code promiseTimeout} that wraps the entire recovery call.
     *
     * @param collectionName the collection to recover
     * @return the decoded recovery snapshot, or {@code null} if recovery failed or timed out
     * @implNote WAWebRequestSyncdSnapshotRecovery.requestRecoveryWithTimeout,
     *           WAWebRequestSyncdSnapshotRecovery.requestRecoveryFromPrimary
     */
    @WhatsAppWebExport(moduleName = "WAWebRequestSyncdSnapshotRecovery", exports = "SyncdSnapshotRecoveryModule", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncdSnapshotRecovery requestRecovery(SyncPatchType collectionName) {
        var startTime = System.currentTimeMillis(); // ADAPTED: WAWebRequestSyncdSnapshotRecovery.requestRecoveryWithTimeout: promiseTimeout wraps entire call
        try {
            // ADAPTED: WAWebRequestSyncdSnapshotRecovery.requestRecoveryFromPrimary: this.recoveryInflight != null && (yield this.recoveryInflight.promise)
            if (!recoverySemaphore.tryAcquire(RECOVERY_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                LOGGER.warning("Snapshot recovery timed out waiting for concurrent recovery to complete");
                return null;
            }
        } catch (InterruptedException e) { // NO_WA_BASIS: Java threading adaptation
            Thread.currentThread().interrupt();
            return null;
        }

        try {
            var responseFuture = pendingRecoveries.computeIfAbsent(collectionName, // WAWebRequestSyncdSnapshotRecovery.requestRecoveryFromPrimary: this.recoveryPromise.has(e) || this.recoveryPromise.set(e, new Resolvable)
                    _ -> new CompletableFuture<>());

            sendRecoveryRequest(collectionName); // WAWebRequestSyncdSnapshotRecovery.requestRecoveryFromPrimary: sendPeerDataOperationRequest(COMPANION_SYNCD_SNAPSHOT_FATAL_RECOVERY, ...)

            // ADAPTED: WAWebRequestSyncdSnapshotRecovery.requestRecoveryWithTimeout: promiseTimeout(this.requestRecoveryFromPrimary(t), p)
            var elapsed = System.currentTimeMillis() - startTime; // WAWebRequestSyncdSnapshotRecovery.requestRecoveryWithTimeout: single timeout wraps entire operation
            var remainingTimeout = RECOVERY_TIMEOUT_MS - elapsed; // WAWebRequestSyncdSnapshotRecovery.requestRecoveryWithTimeout: promiseTimeout(p = 6e4)
            if (remainingTimeout <= 0) {
                LOGGER.warning("Snapshot recovery timed out after semaphore acquisition for collection " + collectionName);
                return null;
            }
            return responseFuture.get(remainingTimeout, TimeUnit.MILLISECONDS); // WAWebRequestSyncdSnapshotRecovery.requestRecoveryWithTimeout: promiseTimeout
        } catch (Exception e) {
            LOGGER.warning("Snapshot recovery failed for collection " + collectionName + ": " + e.getMessage()); // WAWebRequestSyncdSnapshotRecovery.requestRecoveryWithTimeout: catch(t) { ERROR(...) }
            return null; // WAWebRequestSyncdSnapshotRecovery.requestRecoveryWithTimeout: return null
        } finally {
            pendingRecoveries.remove(collectionName); // ADAPTED: WAWebRequestSyncdSnapshotRecovery.requestRecoveryFromPrimary: this.recoveryPromise.set(e, new Resolvable)
            recoverySemaphore.release(); // ADAPTED: WAWebRequestSyncdSnapshotRecovery.requestRecoveryFromPrimary: this.recoveryInflight.resolve()
        }
    }

    /**
     * Resolves a pending recovery request with the decoded snapshot from the primary device.
     *
     * <p>This method should be called by the protocol message handler when a
     * {@code PeerDataOperationRequestResponseMessage} with type
     * {@code COMPANION_SYNCD_SNAPSHOT_FATAL_RECOVERY} is received. The handler is
     * responsible for decoding the {@code SyncDSnapshotFatalRecoveryResponse} bytes
     * into a {@link SyncdSnapshotRecovery} via {@link #decodeRecoverySnapshot} and
     * passing the decoded object here, mirroring WA Web's
     * {@code WAWebNonMessageDataRequestHandler.m} which decodes once before
     * resolving the recovery promise.
     *
     * @param collectionName the collection name extracted from the decoded snapshot
     * @param recoveredSnapshot the decoded recovery snapshot
     * @implNote WAWebRequestSyncdSnapshotRecovery.resolveRecoveryPromise
     */
    @WhatsAppWebExport(moduleName = "WAWebRequestSyncdSnapshotRecovery", exports = "SyncdSnapshotRecoveryModule", adaptation = WhatsAppAdaptation.ADAPTED)
    public void resolveRecovery(
            SyncPatchType collectionName,
            SyncdSnapshotRecovery recoveredSnapshot
    ) {
        var future = pendingRecoveries.get(collectionName); // WAWebRequestSyncdSnapshotRecovery.resolveRecoveryPromise: var e = this.recoveryPromise.get(t)
        if (future != null) {
            future.complete(recoveredSnapshot); // WAWebRequestSyncdSnapshotRecovery.resolveRecoveryPromise: e.resolve(n)
        } else {
            LOGGER.fine("Received snapshot recovery response for " + collectionName + " but no pending request found");
        }
    }

    /**
     * Decodes the snapshot recovery from a recovery response, transparently
     * handling gzip-compressed responses by streaming through a
     * {@link GZIPInputStream}.
     *
     * <p>Per WhatsApp Web behavior, the {@code collectionSnapshot} bytes
     * encode a {@link SyncdSnapshotRecovery} protobuf containing plaintext
     * mutation records, the primary device's LT-Hash, and the version.
     *
     * @param response the recovery response
     * @return the decoded snapshot recovery
     * @implNote WAWebNonMessageDataRequestHandler.m (decode step; WA Web does this in the handler, not in the recovery module)
     */
    @WhatsAppWebExport(moduleName = "WAWebNonMessageDataRequestHandler", exports = "m", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncdSnapshotRecovery decodeRecoverySnapshot(
            PeerDataOperationRequestResponseMessage.PeerDataOperationResult.SyncDSnapshotFatalRecoveryResponse response
    ) {
        var snapshotBytes = response.collectionSnapshot() // WAWebNonMessageDataRequestHandler.m: e.syncdSnapshotFatalRecoveryResponse.collectionSnapshot
                .orElseThrow(() -> new NoSuchElementException("Missing snapshot"));
        try {
            if (response.isCompressed()) { // WAWebNonMessageDataRequestHandler.m: i === true && (s = yield inflate(l.readByteArrayView()))
                try (var protobufStream = ProtobufInputStream.fromStream(new GZIPInputStream(new ByteArrayInputStream(snapshotBytes)))) {
                    return SyncdSnapshotRecoverySpec.decode(protobufStream); // WAWebNonMessageDataRequestHandler.m: decodeProtobuf(SyncdSnapshotRecoverySpec, s)
                }
            } else {
                return SyncdSnapshotRecoverySpec.decode(snapshotBytes); // WAWebNonMessageDataRequestHandler.m: decodeProtobuf(SyncdSnapshotRecoverySpec, s)
            }
        } catch (Exception e) {
            throw new WhatsAppWebAppStateSyncException.ExternalDecodeFailed(e);
        }
    }

    /**
     * Sends the recovery request to the primary device.
     *
     * <p>Constructs a {@code COMPANION_SYNCD_SNAPSHOT_FATAL_RECOVERY}
     * peer data operation request message and sends it to device 0
     * (the primary device). The request includes the collection name
     * and current timestamp.
     *
     * @implNote WAWebRequestSyncdSnapshotRecovery.requestRecoveryFromPrimary (send portion),
     *           WAWebSendNonMessageDataRequest.sendPeerDataOperationRequest (orchestration),
     *           WAWebSendNonMessageDataRequest builds syncdCollectionFatalRecoveryRequest via local helper,
     *           WAWebSendNonMessageDataRequest builds single peer message to device 0 via non-fanout path
     */
    @WhatsAppWebExport(moduleName = "WAWebSendNonMessageDataRequest", exports = "sendPeerDataOperationRequest", adaptation = WhatsAppAdaptation.ADAPTED)
    private void sendRecoveryRequest(SyncPatchType collectionName) {
        var primaryDevice = getPrimaryDevice(); // WAWebSendNonMessageDataRequest.D: createDeviceWidFromUserAndDevice(getMeDevicePnOrThrow().user, getMeDevicePnOrThrow().server, 0)
        if (primaryDevice == null) {
            throw new IllegalStateException("No primary device available for snapshot recovery");
        }

        var recoveryRequest = new PeerDataOperationRequestMessageSyncDCollectionFatalRecoveryRequestBuilder()
                .collectionName(collectionName.toString()) // WAWebSendNonMessageDataRequest.P: {collectionName: e.collectionName, timestamp: e.timestamp}
                .timestamp(Instant.now()) // WAWebRequestSyncdSnapshotRecovery.requestRecoveryFromPrimary: {collectionName: e, timestamp: unixTime()}
                .build();

        var requestMessage = new PeerDataOperationRequestMessageBuilder()
                .peerDataOperationRequestType(PeerDataOperationRequestType.COMPANION_SYNCD_SNAPSHOT_FATAL_RECOVERY) // WAWebSendNonMessageDataRequest.k: peerDataOperationRequestType: e
                .syncdCollectionFatalRecoveryRequest(recoveryRequest) // WAWebSendNonMessageDataRequest.k: n.syncdCollectionFatalRecoveryRequest = s
                .build();

        var protocolMessage = new ProtocolMessageBuilder() // ADAPTED: WAWebSendNonMessageDataRequest wraps in protocol msg internally
                .type(ProtocolMessage.Type.PEER_DATA_OPERATION_REQUEST_MESSAGE)
                .peerDataOperationRequestMessage(requestMessage)
                .build();

        var messageContainer = new MessageContainerBuilder() // ADAPTED: WAWebSendNonMessageDataRequest wraps in message container
                .protocolMessage(protocolMessage)
                .build();

        LOGGER.info("Sending snapshot recovery request for collection " + collectionName + " to primary device " + primaryDevice);
        var self = client.store().jid().orElse(null); // ADAPTED: WAWebSendNonMessageDataRequest.D: getMePnUserOrThrow()
        if (self == null) {
            throw new IllegalStateException("Own JID not available for snapshot recovery request");
        }

        var peerMessageId = MessageIdGenerator.generate(MessageIdVersion.V2, self); // WAWebSendNonMessageDataRequest.D: yield WAWebMsgKey.newId()
        var messageKey = new MessageKeyBuilder() // WAWebSendNonMessageDataRequest.D: new WAWebMsgKey({fromMe: true, remote: getMePnUserOrThrow(), id: ...})
                .id(peerMessageId)
                .parentJid(self) // WAWebSendNonMessageDataRequest.D: remote: getMePnUserOrThrow()
                .fromMe(true) // WAWebSendNonMessageDataRequest.D: fromMe: true
                .senderJid(self)
                .build();
        var messageInfo = new ChatMessageInfoBuilder() // ADAPTED: WAWebSendNonMessageDataRequest.D: {id, to, type: "protocol", subtype: "peer_data_operation_request_message", ...}
                .key(messageKey)
                .message(messageContainer)
                .build();
        // WAWebNonMessageDataRequestLoggingUtils.logNonMessagePeerDataRequest: emitted for every
        // fanout message in WAWebSendNonMessageDataRequest.sendPeerDataOperationRequest. For
        // COMPANION_SYNCD_SNAPSHOT_FATAL_RECOVERY WAWebNonMessageDataRequestLoggingUtils.d returns 1,
        // WAWebNonMessageDataRequestLoggingUtils.m maps to PEER_DATA_REQUEST_TYPE.SYNCD_SNAPSHOT_RECOVERY,
        // and peerDataRequestSessionId is the outbound peer message key id (t.id.id).
        wamService.commit(new NonMessagePeerDataRequestEventBuilder()
                .peerDataRequestCount(1)
                .peerDataRequestType(PeerDataRequestType.SYNCD_SNAPSHOT_RECOVERY)
                .peerDataRequestSessionId(peerMessageId)
                .build());
        client.sendPeerMessage(primaryDevice, messageInfo); // WAWebSendNonMessageDataRequest.sendPeerDataOperationRequest -> WAWebSendAppStateSyncMsgJob.encryptAndSendKeyMsg({msg: t, pushPriority: null, privacySensitive: undefined})
    }

    /**
     * Gets the primary device JID (device 0).
     *
     * @return the primary device JID, or {@code null} if own JID is not set
     * @implNote WAWebSendNonMessageDataRequest non-fanout path: createDeviceWidFromUserAndDevice(getMeDevicePnOrThrow().user, getMeDevicePnOrThrow().server, 0)
     */
    @WhatsAppWebExport(moduleName = "WAWebSendNonMessageDataRequest", exports = "D", adaptation = WhatsAppAdaptation.ADAPTED)
    private Jid getPrimaryDevice() {
        var myJid = client.store().jid().orElse(null); // ADAPTED: WAWebSendNonMessageDataRequest.D: getMeDevicePnOrThrow()
        if (myJid == null) {
            return null;
        }

        return Jid.of(myJid.user(), myJid.server(), 0, 0); // WAWebSendNonMessageDataRequest.D: createDeviceWidFromUserAndDevice(..., 0)
    }
}
