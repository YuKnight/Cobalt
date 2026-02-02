package com.github.auties00.cobalt.device.model;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Hash information for a device list, used in USync queries to enable delta updates.
 *
 * @param hash       SHA-256 hash of the device list
 * @param timestamp  timestamp of the device list
 * @param expectedTs expected timestamp from server (optional)
 */
public record DeviceListHashInfo(String hash, long timestamp, Long expectedTs) {
    /**
     * Creates hash info from a device list.
     *
     * @param deviceList the device list
     * @return hash info
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    public static DeviceListHashInfo of(DeviceList deviceList) throws NoSuchAlgorithmException {
        var hash = calculateDeviceHash(deviceList);
        return new DeviceListHashInfo(
                hash,
                deviceList.timestamp().toEpochMilli(),
                deviceList.expectedTs()
        );
    }

    /**
     * Calculates SHA-256 hash of device IDs in the list.
     *
     * @param deviceList the device list
     * @return Base64-encoded hash
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    // TODO: Optimize me
    private static String calculateDeviceHash(DeviceList deviceList) throws NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance("SHA-256");

        // Hash device IDs in sorted order for consistency
        deviceList.devices()
                .stream()
                .map(DeviceInfo::id)
                .sorted()
                .forEach(deviceId -> digest.update(ByteBuffer.allocate(4).putInt(deviceId).array()));

        return Base64.getEncoder().encodeToString(digest.digest());
    }
}
