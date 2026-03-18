package com.github.auties00.cobalt.sync.key;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.message.send.id.MessageIdGenerator;
import com.github.auties00.cobalt.message.send.id.MessageIdVersion;
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
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.time.Instant;
import java.util.*;

/**
 * Service for requesting missing sync keys from companion devices.
 * <p>
 * Per WhatsApp Web WAWebKeyManagementSendKeyRequestApi: when a sync key is missing,
 * the client sends an AppStateSyncKeyRequest protocol message to all companion devices.
 * Companion devices that have the key respond with AppStateSyncKeyShare.
 *
 * @implNote WAWebSyncdHandleMissingKeys.handleMissingKeys, WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest
 */
public final class MissingSyncKeyRequestService {
    private static final System.Logger LOGGER = System.getLogger(MissingSyncKeyRequestService.class.getName());

    private final WhatsAppClient client;
    private final WhatsAppStore store;

    public MissingSyncKeyRequestService(WhatsAppClient client) {
        this.client = client;
        this.store = client.store();
    }

    /**
     * Requests missing sync keys from companion devices.
     *
     * @implNote WAWebSyncdHandleMissingKeys.handleMissingKeys
     * @param keyIds the IDs of the missing keys
     */
    public void requestMissingKeys(Collection<byte[]> keyIds) {
        handleMissingKeys(keyIds);
    }

    /**
     * Requests a single missing sync key from companion devices.
     *
     * @implNote WAWebSyncdHandleMissingKeys.handleMissingKeys
     * @param keyId the ID of the missing key
     */
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
     * @implNote WAWebSyncdHandleMissingKeys.requestAllMissingKeys
     * @param keyIds the IDs of the missing keys to re-request
     */
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
     *
     * @implNote WAWebKeyManagementUtils.getPeerDevices
     */
    private List<Jid> getCompanionDevices() {
        var myJid = store.jid() // WAWebKeyManagementUtils.getPeerDevices
                .orElse(null);
        if (myJid == null) {
            return List.of();
        }

        try {
            var myDeviceList = store.findDeviceList(myJid.toUserJid()); // WAWebApiDeviceList.getMyDeviceList
            if (myDeviceList.isEmpty()) {
                // WAWebKeyManagementUtils.getPeerDevices: fallback to primary device on error
                LOGGER.log(System.Logger.Level.WARNING,
                        "[syncd] getPeerDevices: no device list. Key reqs->primary only");
                return List.of(Jid.of(myJid.user(), myJid.server(), 0, 0));
            }

            return myDeviceList.get()
                    .devices()
                    .stream()
                    .filter(device -> device.id() != myJid.device()) // WAWebKeyManagementUtils.getPeerDevices: exclude own device
                    .map(device -> device.toDeviceJid(myJid.user(), myJid.server()))
                    .toList();
        } catch (Exception e) {
            // WAWebKeyManagementUtils.getPeerDevices: catch block falls back to primary device
            LOGGER.log(System.Logger.Level.WARNING,
                    "[syncd] getPeerDevices: {0}. Key reqs->primary only", e.getMessage());
            return List.of(Jid.of(myJid.user(), myJid.server(), 0, 0));
        }
    }

    /**
     * Handles missing keys by filtering already-tracked keys, sending a key request to all
     * companion devices, and tracking the newly-missing keys.
     *
     * @implNote WAWebSyncdHandleMissingKeys.handleMissingKeys
     * @param keyIds the IDs of the missing keys (as raw bytes)
     */
    private void handleMissingKeys(Collection<byte[]> keyIds) {
        if (keyIds.isEmpty()) { // WAWebSyncdHandleMissingKeys.handleMissingKeys
            return;
        }

        // WAWebSyncdHandleMissingKeys.handleMissingKeys: skip if offline resume is still in progress
        if (!store.isResumeFromRestartComplete()) { // WAWebOfflineHandler.OfflineMessageHandler.isResumeFromRestartComplete
            LOGGER.log(System.Logger.Level.DEBUG, "syncd: _handleMissingKeys: skip, resume in progress");
            return;
        }

        // WAWebSyncdHandleMissingKeys.handleMissingKeys: filter out already-tracked keys
        var requestedKeyIds = keyIds.stream() // WAWebSyncdHandleMissingKeys.handleMissingKeys
                .filter(Objects::nonNull)
                .map(id -> Arrays.copyOf(id, id.length))
                .filter(id -> store.findMissingSyncKey(id).isEmpty()) // WAWebGetMissingKey.bulkGetMissingKeysInTransaction
                .toList();
        if (requestedKeyIds.isEmpty()) { // WAWebSyncdHandleMissingKeys.handleMissingKeys: i.length === 0
            return;
        }

        var keyIdList = requestedKeyIds.stream() // WAWebSyncdHandleMissingKeys.handleMissingKeys: toSyncKeyId conversion
                .map(id -> new AppStateSyncKeyIdBuilder()
                        .keyId(id)
                        .build())
                .toList();
        var keyRequest = new AppStateSyncKeyRequestBuilder() // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest
                .keyIds(keyIdList)
                .build();

        // WAWebSyncdKeyCallbacksApi.sendSyncdKeyRequest -> WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest
        var successfulDeviceIds = sendKeyRequestToAllDevices(keyRequest);

        // WAWebSyncdStoreMissingKeys.addMissingKeys
        trackMissingKeys(requestedKeyIds, successfulDeviceIds);
    }

    /**
     * Sends a key request to all companion devices and returns the set of successful device IDs.
     *
     * @implNote WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest
     * @param keyRequest the key request to send
     * @return the set of device IDs that were successfully sent to
     */
    private Set<Integer> sendKeyRequestToAllDevices(AppStateSyncKeyRequest keyRequest) {
        var companionDevices = getCompanionDevices(); // WAWebKeyManagementUtils.getPeerDevices

        // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: throw if no peers available
        if (companionDevices.isEmpty()) {
            throw new IllegalStateException(
                    "syncd: sendAppStateSyncKeyRequest: no peer devices available to request key from");
        }

        var keyIdHexes = keyRequest.keyIds().stream() // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: logging
                .map(id -> id.keyId().map(k -> HexFormat.of().formatHex(k)).orElse("?"))
                .toList();
        var deviceIds = companionDevices.stream()
                .map(Jid::device)
                .toList();
        LOGGER.log(System.Logger.Level.INFO,
                "syncd: send key request key id {0} to peer deviceIds {1}", keyIdHexes, deviceIds);

        var successfulDeviceIds = new LinkedHashSet<Integer>(); // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest
        var failureCount = 0;
        for (var device : companionDevices) { // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest: Promise.allSettled
            if (sendKeyRequest(device, keyRequest)) {
                successfulDeviceIds.add(device.device());
            } else {
                failureCount++;
            }
        }

        // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest:
        // partial failures are warned; total failure throws
        if (failureCount > 0 && failureCount < companionDevices.size()) { // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest
            LOGGER.log(System.Logger.Level.WARNING,
                    "syncd: sendAppStateSyncKeyRequest: {0}/{1} peer device(s) failed",
                    failureCount, companionDevices.size());
        } else if (failureCount == companionDevices.size()) { // WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest
            LOGGER.log(System.Logger.Level.ERROR,
                    "[syncd] sendAppStateSyncKeyRequest: all {0} peers failed", companionDevices.size());
            throw new IllegalStateException(
                    "syncd: sendAppStateSyncKeyRequest failed for all " + companionDevices.size() + " peer device(s)");
        }

        return successfulDeviceIds;
    }

    /**
     * Tracks missing keys in the store, creating new entries or updating existing ones.
     *
     * @implNote WAWebSyncdStoreMissingKeys.addMissingKeys
     * @param keyIds the IDs of the missing keys
     * @param successfulDeviceIds the set of device IDs that were successfully asked
     */
    private void trackMissingKeys(Collection<byte[]> keyIds, Set<Integer> successfulDeviceIds) {
        for (var keyId : keyIds) { // WAWebSyncdStoreMissingKeys.addMissingKeys
            var missingKey = store.findMissingSyncKey(keyId)
                    .orElseGet(() -> new MissingDeviceSyncKeyBuilder()
                            .keyId(keyId)
                            .timestamp(Instant.now()) // WAWebSyncdStoreMissingKeys.addMissingKeys: timestamp: unixTimeMs()
                            .askedDevices(Set.of())
                            .build());
            successfulDeviceIds.forEach(missingKey::markDeviceAsked); // WAWebSyncdStoreMissingKeys.addMissingKeys: deviceResponses
            store.addMissingSyncKey(missingKey); // WAWebGetMissingKey.bulkUpdateMissingKeysInTransaction
        }
    }

    /**
     * Sends a key request protocol message to a single companion device.
     *
     * @implNote WAWebKeyManagementSendKeyRequestApi.sendAppStateSyncKeyRequest
     * @param device the target companion device JID
     * @param keyRequest the key request to send
     * @return {@code true} if the message was sent successfully, {@code false} otherwise
     */
    private boolean sendKeyRequest(Jid device, AppStateSyncKeyRequest keyRequest) {
        try {
            var self = store.jid().orElse(null);
            if (self == null) {
                return false;
            }

            var protocolMessage = new ProtocolMessageBuilder()
                    .type(ProtocolMessage.Type.APP_STATE_SYNC_KEY_REQUEST)
                    .appStateSyncKeyRequest(keyRequest)
                    .build();
            var messageContainer = new MessageContainerBuilder()
                    .protocolMessage(protocolMessage)
                    .build();
            var messageKey = new MessageKeyBuilder()
                    .id(MessageIdGenerator.generate(MessageIdVersion.V1, self)) // WAWebMsgKey.newId_DEPRECATED
                    .parentJid(self)
                    .fromMe(true)
                    .senderJid(self)
                    .build();
            var messageInfo = new ChatMessageInfoBuilder()
                    .key(messageKey)
                    .message(messageContainer)
                    .timestamp(Instant.now())
                    .senderJid(self)
                    .build();
            client.sendPeerMessage(device, messageInfo);
            return true;
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to send key request to device {0}: {1}",
                    device, e.getMessage());
            return false;
        }
    }
}
