package com.github.auties00.cobalt.sync.key;

import com.github.auties00.cobalt.model.message.system.appstate.AppStateSyncKey;
import com.github.auties00.cobalt.model.message.system.appstate.AppStateSyncKeyId;

import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * Utility methods for extracting metadata from sync key identifiers.
 *
 * <p>Per WhatsApp Web {@code WASyncdKeyManagementUtils}: a sync key ID
 * is a 6-byte buffer structured as:
 * <ul>
 * <li>Bytes 0–1 (big-endian {@code uint16}): device ID of the key creator
 * <li>Bytes 2–5 (big-endian {@code uint32}): key epoch (monotonically increasing)
 * </ul>
 */
public final class SyncKeyUtils {
    /**
     * The expected length of a sync key ID in bytes.
     */
    private static final int KEY_ID_LENGTH = 6;

    private SyncKeyUtils() {
    }

    /**
     * Extracts the device ID (creator device) from a sync key ID.
     *
     * <p>Per WhatsApp Web {@code WASyncdKeyManagementUtils.getKeyDeviceId}:
     * reads the first two bytes as a big-endian unsigned 16-bit integer.
     *
     * @param keyId the raw key ID bytes (must be at least 6 bytes)
     * @return the device ID, or {@code -1} if the key ID is malformed
     * @implNote WASyncdKeyManagementUtils.getKeyDeviceId
     */
    public static int getKeyDeviceId(byte[] keyId) {
        if (keyId == null || keyId.length < KEY_ID_LENGTH) { // ADAPTED: Java null-safety guard
            return -1;
        }
        return ByteBuffer.wrap(keyId).getShort(0) & 0xFFFF; // WASyncdKeyManagementUtils.getKeyDeviceId: DataView.getUint16(0)
    }

    /**
     * Extracts the key epoch from a sync key ID.
     *
     * <p>Per WhatsApp Web {@code WASyncdKeyManagementUtils.getKeyEpoch}:
     * reads bytes 2–5 as a big-endian unsigned 32-bit integer.
     *
     * @param keyId the raw key ID bytes (must be at least 6 bytes)
     * @return the key epoch, or {@code -1} if the key ID is malformed
     * @implNote WASyncdKeyManagementUtils.getKeyEpoch
     */
    public static int getKeyEpoch(byte[] keyId) {
        if (keyId == null || keyId.length < KEY_ID_LENGTH) { // ADAPTED: Java null-safety guard
            return -1;
        }
        return ByteBuffer.wrap(keyId).getInt(2); // WASyncdKeyManagementUtils.getKeyEpoch: DataView.getUint32(2)
    }

    /**
     * Extracts the key epoch from an {@link AppStateSyncKey}.
     *
     * @param key the sync key
     * @return the key epoch, or {@code -1} if the key or key ID is absent/malformed
     * @implNote ADAPTED: convenience overload wrapping WASyncdKeyManagementUtils.getKeyEpoch with Optional unwrapping
     */
    public static int getKeyEpoch(AppStateSyncKey key) {
        return key.keyId() // ADAPTED: WASyncdKeyManagementUtils.getKeyEpoch — Optional unwrapping
                .flatMap(AppStateSyncKeyId::keyId)
                .map(SyncKeyUtils::getKeyEpoch)
                .orElse(-1);
    }

    /**
     * Generates the next key epoch value, incrementing the given key's epoch by one.
     *
     * <p>Per WhatsApp Web {@code WASyncdKeyManagementUtils.generateNewKeyEpoch}.
     *
     * @param keyId the current key ID bytes
     * @return the next epoch value
     * @implNote WASyncdKeyManagementUtils.generateNewKeyEpoch
     */
    public static int generateNewKeyEpoch(byte[] keyId) {
        return getKeyEpoch(keyId) + 1; // WASyncdKeyManagementUtils.generateNewKeyEpoch: getKeyEpoch(e) + 1
    }

    /**
     * Builds a new sync key ID from the given device ID and key epoch.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdRotateKey}: the key ID is constructed
     * by concatenating the device ID (2 bytes, big-endian) with the key epoch
     * (4 bytes, big-endian).
     *
     * @param deviceId the device ID of the creator
     * @param keyEpoch the key epoch
     * @return the 6-byte key ID
     * @implNote WAWebSyncdRotateKey.rotateKey (key ID construction)
     */
    public static byte[] buildKeyId(int deviceId, int keyEpoch) {
        var buffer = ByteBuffer.allocate(KEY_ID_LENGTH); // WAWebSyncdRotateKey.rotateKey: new Uint8Array(6)
        buffer.putShort((short) deviceId); // WAWebSyncdRotateKey.rotateKey: DataView.setUint16(0, deviceId)
        buffer.putInt(keyEpoch); // WAWebSyncdRotateKey.rotateKey: DataView.setUint32(2, epoch)
        return buffer.array();
    }

    /**
     * Converts a sync key's key ID bytes to a space-separated hex string.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdCryptoUtils.syncKeyIdToHex}:
     * converts each byte to its hexadecimal representation (without zero-padding)
     * and joins with spaces.
     *
     * @param key the sync key
     * @return the hex string, or {@code "unknown"} if the key ID is absent
     * @implNote WAWebSyncdCryptoUtils.syncKeyIdToHex (function c)
     */
    public static String syncKeyIdToHex(AppStateSyncKey key) {
        var keyIdBytes = key.keyId()
                .flatMap(AppStateSyncKeyId::keyId)
                .orElse(null);
        if (keyIdBytes == null) {
            return "unknown";
        }

        var sb = new StringBuilder();
        for (int i = 0; i < keyIdBytes.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(Integer.toHexString(keyIdBytes[i] & 0xFF)); // WAWebSyncdCryptoUtils.syncKeyIdToHex: e.toString(16) — no padding
        }
        return sb.toString();
    }

    /**
     * Finds the newest sync key using the same ordering as WA Web:
     * highest epoch first, then lowest device id among ties.
     *
     * @param keys the available sync keys
     * @return the newest key, or {@code null} if none exist
     * @implNote WAWebSyncdKeyManagement.getNewestKeyPair
     */
    public static AppStateSyncKey findNewestKey(Collection<AppStateSyncKey> keys) {
        if (keys == null || keys.isEmpty()) { // ADAPTED: Java null-safety guard
            return null; // WAWebSyncdKeyManagement.getNewestKeyPair: if (e.length === 0) return null
        }

        // WAWebSyncdKeyManagement.getNewestKeyPair: Math.max(...e.map(getKeyEpoch))
        var maxEpoch = Integer.MIN_VALUE;
        for (var key : keys) {
            var epoch = getKeyEpoch(key); // WAWebSyncdKeyManagement.getNewestKeyPair: getKeyEpoch(e.keyId)
            if (epoch > maxEpoch) {
                maxEpoch = epoch;
            }
        }

        // WAWebSyncdKeyManagement.getNewestKeyPair: filter by maxEpoch, then Math.min(...getKeyDeviceId)
        AppStateSyncKey bestKey = null;
        var bestDeviceId = Integer.MAX_VALUE;
        for (var key : keys) {
            if (getKeyEpoch(key) != maxEpoch) { // WAWebSyncdKeyManagement.getNewestKeyPair: e.filter(epoch === n)
                continue;
            }

            var keyIdBytes = key.keyId()
                    .flatMap(AppStateSyncKeyId::keyId)
                    .orElse(null);
            if (keyIdBytes == null) { // ADAPTED: Java null-safety guard
                continue;
            }

            var deviceId = getKeyDeviceId(keyIdBytes); // WAWebSyncdKeyManagement.getNewestKeyPair: getKeyDeviceId(e.keyId)
            if (deviceId < bestDeviceId) { // WAWebSyncdKeyManagement.getNewestKeyPair: Math.min + indexOf
                bestDeviceId = deviceId;
                bestKey = key;
            }
        }

        return bestKey; // WAWebSyncdKeyManagement.getNewestKeyPair: return r[l]
    }
}
