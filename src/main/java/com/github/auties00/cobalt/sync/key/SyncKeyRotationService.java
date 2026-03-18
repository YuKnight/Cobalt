package com.github.auties00.cobalt.sync.key;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.message.send.id.MessageIdGenerator;
import com.github.auties00.cobalt.message.send.id.MessageIdVersion;
import com.github.auties00.cobalt.model.chat.ChatMessageInfoBuilder;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainerBuilder;
import com.github.auties00.cobalt.model.message.MessageKeyBuilder;
import com.github.auties00.cobalt.model.message.system.ProtocolMessage;
import com.github.auties00.cobalt.model.message.system.ProtocolMessageBuilder;
import com.github.auties00.cobalt.model.message.system.appstate.*;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.util.SchedulerUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Manages sync key rotation based on key age and device removal.
 *
 * <p>Per WhatsApp Web {@code WAWebSyncdKeyManagement}: before pushing mutations,
 * the active key is checked. If the key has expired (age exceeds
 * {@code syncd_key_max_use_days}) or a companion device has been removed
 * (fingerprint mismatch), a new key is generated and shared with companion
 * devices.
 *
 * <p>The rotation flow is:
 * <ol>
 * <li>Check if the current newest key is expired or if devices changed
 * <li>Generate a new key with incremented epoch and fresh random key data
 * <li>Store the new key locally
 * <li>Share the new key with companion devices via {@code AppStateSyncKeyShare}
 * </ol>
 *
 * @implNote WAWebSyncdKeyManagement, WAWebSyncdRotateKey, WAWebSyncdKeyCallbacksApi
 */
public final class SyncKeyRotationService {
    private static final Logger LOGGER = Logger.getLogger(SyncKeyRotationService.class.getName());

    /**
     * Minimum value for the key max use days threshold.
     */
    private static final int MIN_KEY_MAX_USE_DAYS = 1;

    /**
     * Maximum value for the key max use days threshold.
     */
    private static final int MAX_KEY_MAX_USE_DAYS = 90;

    /**
     * Per WhatsApp Web {@code WAWebTasksDefinitions}: the periodic key rotation
     * check runs every 27 days.
     */
    private static final Duration PERIODIC_ROTATION_INTERVAL = Duration.ofDays(27);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final WhatsAppClient whatsapp;
    private final ABPropsService abPropsService;
    private volatile CompletableFuture<?> periodicRotationJob;

    /**
     * Constructs a new sync key rotation service.
     *
     * @param whatsapp       the WhatsApp client instance
     * @param abPropsService the AB props service for threshold configuration
     */
    public SyncKeyRotationService(WhatsAppClient whatsapp, ABPropsService abPropsService) {
        this.whatsapp = whatsapp;
        this.abPropsService = abPropsService;
    }

    /**
     * Ensures the active key is valid for use, rotating if necessary.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdKeyManagement.getActiveKey}:
     * gets the newest key pair, checks if it has expired or if a device
     * was removed, and rotates if needed.
     *
     * @param triggerRotation whether to trigger rotation if the key is stale
     *                        (set to {@code false} for read-only checks)
     * @implNote WAWebSyncdKeyManagement.getActiveKey (function m/p/_)
     */
    public void ensureActiveKey(boolean triggerRotation) {
        var newestKey = findNewestKey(); // WAWebSyncdKeyManagement.getActiveKey: yield f() (getNewestKeyPair)
        var currentFingerprint = getCurrentDeviceFingerprint(); // WAWebSyncdKeyManagement.getActiveKey: yield getDeviceFingerprint()
        if (newestKey == null) {
            throw new IllegalStateException("syncd: No sync key available"); // WAWebSyncdKeyManagement.getActiveKey: throw err("syncd: No sync key available")
        }

        var expired = hasKeyExpired(newestKey); // WAWebSyncdKeyManagement.getActiveKey: i = hasKeyExpired(n)
        var deviceRemoved = hasADeviceBeenRemoved(newestKey, currentFingerprint); // WAWebSyncdKeyManagement.getActiveKey: l = hasADeviceBeenRemoved(n, a)

        if (!triggerRotation || (!expired && !deviceRemoved)) {
            return; // WAWebSyncdKeyManagement.getActiveKey: if (!t || !i && !l) return {keyId, keyData}
        }

        var rotatedKey = rotateKey(currentFingerprint, newestKey); // WAWebSyncdKeyManagement.getActiveKey: d = rotateKey(a, n)
        if (rotatedKey == null) {
            return; // ADAPTED: defensive null check, WA Web rotateKey always returns a value
        }

        LOGGER.info("syncd: stored key rotation key id " + SyncKeyUtils.syncKeyIdToHex(rotatedKey)); // WAWebSyncdKeyManagement.getActiveKey: LOG("syncd: stored key rotation key id", syncKeyIdToHex(d.keyId))
        whatsapp.store().addWebAppStateKeys(List.of(rotatedKey)); // WAWebSyncdKeyManagement.getActiveKey: yield setSyncKeyInTransaction(d)
        shareKeyWithCompanionDevices(rotatedKey); // WAWebSyncdKeyManagement.getActiveKey: yield sendSyncdKeyRotation([d])

        // WAWebSyncdKeyManagement.getActiveKey: logging happens AFTER rotation+store+send
        if (expired) {
            LOGGER.info("syncd: key rotation due to key expiry"); // WAWebSyncdKeyManagement.getActiveKey: LOG("syncd: key rotation due to key expiry")
        }
        if (deviceRemoved) {
            LOGGER.info("syncd: key rotation due to device removal"); // WAWebSyncdKeyManagement.getActiveKey: LOG("syncd: key rotation due to device removal")
        }
    }

    /**
     * Finds the newest (highest epoch) sync key.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdKeyManagement.getNewestKeyPair}:
     * among all stored keys, find those with the maximum epoch. Among those,
     * pick the one with the minimum device ID.
     *
     * @return the newest key, or {@code null} if no keys exist
     * @implNote WAWebSyncdKeyManagement.getNewestKeyPair (function f/g)
     */
    private AppStateSyncKey findNewestKey() {
        return SyncKeyUtils.findNewestKey(whatsapp.store().appStateKeys()); // WAWebSyncdKeyManagement.getNewestKeyPair: yield getAllSyncKeysInTransaction() -> max epoch, min deviceId
    }

    /**
     * Checks whether the given key has expired based on its timestamp
     * and the configured maximum use days.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdRotateKey.hasKeyExpired}:
     * compares the key's timestamp against {@code syncd_key_max_use_days}
     * (clamped between 1 and 90).
     *
     * @param key the key to check
     * @return {@code true} if the key has expired
     * @implNote WAWebSyncdRotateKey.hasKeyExpired (function u)
     */
    private boolean hasKeyExpired(AppStateSyncKey key) {
        var timestamp = key.keyData() // WAWebSyncdRotateKey.hasKeyExpired: var n = t.timestamp
                .flatMap(AppStateSyncKeyData::timestamp)
                .orElse(null);
        if (timestamp == null) {
            return false;
        }

        // WAWebSyncdRotateKey.hasKeyExpired: r = Math.min(s, Math.max(e, getSyncdKeyMaxUseDays()))
        var maxDays = Math.min(MAX_KEY_MAX_USE_DAYS,
                Math.max(MIN_KEY_MAX_USE_DAYS,
                        abPropsService.getInt(ABProp.SYNCD_KEY_MAX_USE_DAYS)));
        // WAWebSyncdRotateKey.hasKeyExpired: a = r * DAY_MILLISECONDS, i = unixTimeMs() - n, return i > a
        var maxAge = Duration.ofDays(maxDays);
        var age = Duration.between(timestamp, Instant.now());
        return age.compareTo(maxAge) > 0;
    }

    /**
     * Checks whether a device has been removed by comparing the key's
     * stored fingerprint with the current device list.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdRotateKey.hasADeviceBeenRemoved}:
     * expands the key's fingerprint device indexes up to the current device
     * list's {@code currentIndex} and compares with the current device indexes.
     * A mismatch (rawId change or device set difference) indicates removal.
     *
     * @param key                the key whose fingerprint to check
     * @param currentFingerprint the current device fingerprint snapshot
     * @return {@code true} if a device has been removed since the key was created
     * @implNote WAWebSyncdRotateKey.hasADeviceBeenRemoved (function c)
     */
    private boolean hasADeviceBeenRemoved(AppStateSyncKey key, DeviceFingerprint currentFingerprint) {
        var fingerprint = key.keyData() // WAWebSyncdRotateKey.hasADeviceBeenRemoved: var n = e.fingerprint
                .flatMap(AppStateSyncKeyData::fingerprint)
                .orElse(null);
        if (fingerprint == null) {
            return false;
        }

        if (currentFingerprint == null) {
            return false;
        }

        // WAWebSyncdRotateKey.hasADeviceBeenRemoved: n.rawId !== i
        var keyRawId = fingerprint.rawId().orElse(-1);
        if (keyRawId != currentFingerprint.rawId) {
            return true;
        }

        // WAWebSyncdRotateKey.hasADeviceBeenRemoved: new Set(n.deviceIndexes), expand from n.currentIndex+1 to o
        var keyDeviceIndexes = new HashSet<>(fingerprint.deviceIndexes());
        var keyCurrentIndex = fingerprint.currentIndex().orElse(0);
        for (int i = keyCurrentIndex + 1; i <= currentFingerprint.currentIndex; i++) {
            keyDeviceIndexes.add(i);
        }

        // WAWebSyncdRotateKey.hasADeviceBeenRemoved: !equalsSet(l, new Set(a))
        return !keyDeviceIndexes.equals(currentFingerprint.deviceIndexes);
    }

    /**
     * Gets the current device fingerprint from the own device list.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdKeyCallbacksApi.getDeviceFingerprint}:
     * reads the own device list and extracts currentIndex, deviceIndexes (keyIndex
     * values), and rawId.
     *
     * @return the current fingerprint, or {@code null} if unavailable
     * @implNote WAWebSyncdKeyCallbacksApi.getDeviceFingerprint (function s)
     */
    private DeviceFingerprint getCurrentDeviceFingerprint() {
        // ADAPTED: WAWebSyncdKeyCallbacksApi.getDeviceFingerprint: yield getMyDeviceList()
        var myJid = whatsapp.store().jid().orElse(null);
        if (myJid == null) {
            return null;
        }

        var deviceListOpt = whatsapp.store().findDeviceList(myJid.toUserJid());
        if (deviceListOpt.isEmpty()) {
            return null;
        }

        var deviceList = deviceListOpt.get();
        var currentIndex = deviceList.currentIndex(); // WAWebSyncdKeyCallbacksApi.getDeviceFingerprint: n = t.currentIndex
        var rawId = deviceList.rawId(); // WAWebSyncdKeyCallbacksApi.getDeviceFingerprint: i = t.rawId
        var rawIdInt = -1;
        if (rawId != null) {
            try {
                rawIdInt = Integer.parseInt(rawId);
            } catch (NumberFormatException ignored) {
            }
        }

        // WAWebSyncdKeyCallbacksApi.getDeviceFingerprint: a = t.devices.map(e => e.keyIndex)
        var deviceIndexes = new HashSet<Integer>();
        for (var device : deviceList.devices()) {
            deviceIndexes.add(device.keyIndex());
        }

        // WAWebSyncdKeyCallbacksApi.getDeviceFingerprint: return {currentIndex: n, deviceIndexes: a, rawId: i}
        return new DeviceFingerprint(currentIndex, deviceIndexes, rawIdInt);
    }

    /**
     * Generates a new rotated key from the given previous key.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdRotateKey.rotateKey}:
     * creates a new key with an incremented epoch, the current device's ID,
     * fresh random 32-byte key data, the current device fingerprint, and
     * the current timestamp.
     *
     * @param currentFingerprint the current device fingerprint snapshot
     * @param previousKey        the key being rotated out
     * @return the new key, or {@code null} if rotation cannot proceed
     * @implNote WAWebSyncdRotateKey.rotateKey (function d), WAWebSyncdRotateKey (function m, function p)
     */
    private AppStateSyncKey rotateKey(DeviceFingerprint currentFingerprint, AppStateSyncKey previousKey) {
        var previousKeyIdBytes = previousKey.keyId() // WAWebSyncdRotateKey.m: e.keyId
                .flatMap(AppStateSyncKeyId::keyId)
                .orElse(null);
        if (previousKeyIdBytes == null) {
            LOGGER.warning("Cannot rotate key: previous key has no ID");
            return null;
        }

        var myJid = whatsapp.store().jid().orElse(null);
        if (myJid == null) {
            LOGGER.warning("Cannot rotate key: own JID not available");
            return null;
        }

        if (currentFingerprint == null) {
            LOGGER.warning("Cannot rotate key: device fingerprint not available");
            return null;
        }

        // WAWebSyncdRotateKey.m: t = generateNewKeyEpoch(e.keyId)
        var newEpoch = SyncKeyUtils.generateNewKeyEpoch(previousKeyIdBytes);
        // WAWebSyncdRotateKey.m: r = interpretAsNumber(extractDeviceId(getMyDeviceJid()))
        var myDeviceId = myJid.device();
        // WAWebSyncdRotateKey.m: keyId = toSyncKeyId(concat(intToBytes(2, r), intToBytes(4, t)))
        var newKeyId = SyncKeyUtils.buildKeyId(myDeviceId, newEpoch);

        // WAWebSyncdRotateKey.p: getRandomValues(new Uint8Array(32))
        var newKeyData = new byte[32];
        SECURE_RANDOM.nextBytes(newKeyData);

        // WAWebSyncdRotateKey.d: fingerprint: e (the passed-in fingerprint)
        var fingerprint = new AppStateSyncKeyFingerprintBuilder()
                .rawId(currentFingerprint.rawId)
                .currentIndex(currentFingerprint.currentIndex)
                .deviceIndexes(new ArrayList<>(currentFingerprint.deviceIndexes))
                .build();

        // WAWebSyncdRotateKey.d: timestamp: unixTimeMs()
        var keyData = new AppStateSyncKeyDataBuilder()
                .keyData(newKeyData)
                .fingerprint(fingerprint)
                .timestamp(Instant.now())
                .build();

        var keyIdProto = new AppStateSyncKeyIdBuilder()
                .keyId(newKeyId)
                .build();

        // WAWebSyncdRotateKey.d: return {keyId: a, keyEpoch: r, keyData: i, fingerprint: e, timestamp: l}
        return new AppStateSyncKeyBuilder()
                .keyId(keyIdProto)
                .keyData(keyData)
                .build();
    }

    /**
     * Shares the rotated key with all companion devices.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdKeyCallbacksApi.sendSyncdKeyRotation}:
     * wraps the key in an {@code AppStateSyncKeyShare} protocol message with type
     * {@code key_rotation} and sends it to all peer devices.
     *
     * @param key the new key to share
     * @implNote WAWebSyncdKeyCallbacksApi.sendSyncdKeyRotation (function u), WAWebKeyManagementSendKeyShareApi.sendAppStateSyncKeyShare
     */
    private void shareKeyWithCompanionDevices(AppStateSyncKey key) {
        var companionDevices = getCompanionDevices();
        if (companionDevices.isEmpty()) {
            LOGGER.fine("No companion devices to share rotated key with");
            return;
        }

        var myJid = whatsapp.store().jid().orElse(null);
        if (myJid == null) {
            LOGGER.warning("Cannot share rotated key: own JID not available");
            return;
        }

        var keyShare = new AppStateSyncKeyShareBuilder()
                .keys(List.of(key))
                .build();

        var protocolMessage = new ProtocolMessageBuilder()
                .type(ProtocolMessage.Type.APP_STATE_SYNC_KEY_SHARE)
                .appStateSyncKeyShare(keyShare)
                .build();

        var messageContainer = new MessageContainerBuilder()
                .protocolMessage(protocolMessage)
                .build();

        // Per WA Web WAWebKeyManagementSendKeyShareApi: key rotation shares
        // are sent as peer messages (category="peer", push_priority="high")
        // with subtype "app_state_sync_key_share"
        for (var device : companionDevices) {
            try {
                var messageKey = new MessageKeyBuilder()
                        .id(MessageIdGenerator.generate(MessageIdVersion.V1, myJid)) // WAWebMsgKey.newId_DEPRECATED
                        .parentJid(myJid)
                        .fromMe(true)
                        .senderJid(myJid)
                        .build();
                var messageInfo = new ChatMessageInfoBuilder()
                        .key(messageKey)
                        .message(messageContainer)
                        .build();
                whatsapp.sendPeerMessage(device, messageInfo);
            } catch (Exception e) {
                LOGGER.warning("Failed to send rotated key to device " + device + ": " + e.getMessage());
            }
        }

        LOGGER.info("Shared rotated key with " + companionDevices.size() + " companion devices");
    }

    /**
     * Gets companion device JIDs (all devices except our own).
     *
     * <p>Per WhatsApp Web {@code WAWebKeyManagementUtils.getPeerDevices}:
     * returns all peer devices from the device list, falling back to
     * the primary device (device 0) if the device list cannot be retrieved.
     *
     * @return the list of companion device JIDs
     * @implNote WAWebKeyManagementUtils.getPeerDevices
     */
    private List<Jid> getCompanionDevices() {
        var myJid = whatsapp.store().jid().orElse(null); // WAWebKeyManagementUtils.getPeerDevices: getMeDevicePnOrThrow()
        if (myJid == null) {
            return List.of();
        }

        try {
            var myDeviceList = whatsapp.store().findDeviceList(myJid.toUserJid()); // WAWebKeyManagementUtils.getPeerDevices: yield getMyDeviceList()
            if (myDeviceList.isEmpty()) {
                return List.of();
            }

            return myDeviceList.get()
                    .devices()
                    .stream()
                    .filter(device -> device.id() != myJid.device()) // WAWebKeyManagementUtils.getPeerDevices: e.id !== n.getDeviceId()
                    .map(device -> device.toDeviceJid(myJid.user(), myJid.server())) // WAWebKeyManagementUtils.getPeerDevices: createDeviceWidFromUserAndDevice
                    .toList();
        } catch (Exception e) {
            // WAWebKeyManagementUtils.getPeerDevices: catch -> fallback to primary device
            LOGGER.warning("[syncd] getPeerDevices: " + e.getMessage() + ". Key reqs->primary only");
            return List.of(Jid.of(myJid.user(), myJid.server(), 0, 0));
        }
    }

    /**
     * Starts a periodic background job that checks key rotation every 27 days.
     *
     * <p>Per WhatsApp Web {@code WAWebTasksDefinitions}: a persisted
     * {@code RotateKeyTask} runs every 27 days as a background check
     * independent of mutation push. This ensures expired keys are rotated
     * even if no mutations are pushed for extended periods.
     */
    public void startPeriodicRotationJob() {
        stopPeriodicRotationJob();
        scheduleNextPeriodicRotation();
    }

    private void scheduleNextPeriodicRotation() {
        periodicRotationJob = SchedulerUtils.scheduleDelayed(
                PERIODIC_ROTATION_INTERVAL,
                () -> {
                    try {
                        ensureActiveKey(true);
                    } catch (Exception e) {
                        LOGGER.warning("Periodic key rotation check failed: " + e.getMessage());
                    } finally {
                        scheduleNextPeriodicRotation();
                    }
                }
        );
    }

    /**
     * Stops the periodic key rotation background job.
     */
    public void stopPeriodicRotationJob() {
        var job = periodicRotationJob;
        if (job != null) {
            job.cancel(false);
            periodicRotationJob = null;
        }
    }

    /**
     * Internal representation of a device fingerprint snapshot.
     *
     * @param currentIndex  the current device index counter
     * @param deviceIndexes the set of active device key indexes
     * @param rawId         the raw fingerprint ID
     */
    private record DeviceFingerprint(int currentIndex, Set<Integer> deviceIndexes, int rawId) {
    }
}
