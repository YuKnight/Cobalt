package com.github.auties00.cobalt.device.model;

import com.github.auties00.cobalt.device.util.DeviceConstants;
import com.github.auties00.cobalt.model.jid.Jid;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Represents a cached device list for a user.
 *
 * @param userJid                   the user JID (without device component)
 * @param devices                   the list of devices for this user
 * @param timestamp                 when this list was fetched from the server
 * @param expiresAt                 when this cache entry expires
 * @param rawId                     the raw identity ID from the server
 * @param deleted                   whether this device list has been deleted
 * @param deletedChangedToHost      whether deletion was due to change to hosted account
 * @param advAccountType            the ADV account type (E2EE or HOSTED)
 * @param expectedTs                expected timestamp from server
 * @param expectedTsLastDeviceJobTs last device job timestamp
 * @param expectedTsUpdateTs        timestamp when expectedTs was updated
 */
public record DeviceList(
        Jid userJid,
        List<DeviceInfo> devices,
        Instant timestamp,
        Instant expiresAt,
        String rawId,
        boolean deleted,
        boolean deletedChangedToHost,
        DeviceInfo.Type advAccountType,
        Long expectedTs,
        Long expectedTsLastDeviceJobTs,
        Long expectedTsUpdateTs,
        int currentIndex,
        List<Integer> validIndexes
) {
    private static final Duration DEFAULT_TTL = Duration.ofDays(1);

    /**
     * Creates a new DeviceList with default TTL.
     */
    public static DeviceList of(Jid userJid, List<DeviceInfo> devices) {
        return of(userJid, devices, DEFAULT_TTL);
    }

    /**
     * Creates a new DeviceList with custom TTL.
     */
    public static DeviceList of(Jid userJid, List<DeviceInfo> devices, Duration ttl) {
        var now = Instant.now();
        return new DeviceList(
                userJid,
                List.copyOf(devices),
                now,
                now.plus(ttl),
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                0,
                List.of()
        );
    }

    /**
     * Creates a new DeviceList with full parameters.
     */
    public static DeviceList of(
            Jid userJid,
            List<DeviceInfo> devices,
            Duration ttl,
            String rawId,
            DeviceInfo.Type advAccountType,
            Long expectedTs
    ) {
        var now = Instant.now();
        return new DeviceList(
                userJid,
                List.copyOf(devices),
                now,
                now.plus(ttl),
                rawId,
                false,
                false,
                advAccountType,
                expectedTs,
                null,
                null,
                0,
                List.of()
        );
    }

    /**
     * Creates a new DeviceList with full parameters including key index validation data.
     */
    public static DeviceList of(
            Jid userJid,
            List<DeviceInfo> devices,
            Duration ttl,
            String rawId,
            DeviceInfo.Type advAccountType,
            Long expectedTs,
            int currentIndex,
            List<Integer> validIndexes
    ) {
        var now = Instant.now();
        return new DeviceList(
                userJid,
                List.copyOf(devices),
                now,
                now.plus(ttl),
                rawId,
                false,
                false,
                advAccountType,
                expectedTs,
                null,
                null,
                currentIndex,
                validIndexes != null ? List.copyOf(validIndexes) : List.of()
        );
    }

    /**
     * Creates a deleted device list marker.
     */
    public static DeviceList deleted(Jid userJid, boolean changedToHost) {
        var now = Instant.now();
        return new DeviceList(
                userJid,
                List.of(),
                now,
                now,
                null,
                true,
                changedToHost,
                null,
                null,
                null,
                null,
                0,
                List.of()
        );
    }

    /**
     * Creates a minimal device list containing only the primary device.
     * Used as fallback when device list is not found on the server.
     */
    public static DeviceList primaryOnly(Jid userJid) {
        var now = Instant.now();
        return new DeviceList(
                userJid,
                List.of(DeviceInfo.ofE2EE(DeviceConstants.PRIMARY_DEVICE_ID, 0)),
                now,
                now.plus(DEFAULT_TTL),
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                0,
                List.of()
        );
    }

    /**
     * Returns true if this cache entry has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Returns the primary device if present.
     */
    public Optional<DeviceInfo> primaryDevice() {
        return devices.stream()
                .filter(DeviceInfo::isPrimary)
                .findFirst();
    }

    /**
     * Returns all non-hosted devices.
     */
    public List<DeviceInfo> e2eeDevices() {
        return devices.stream()
                .filter(d -> !d.isHosted())
                .toList();
    }

    /**
     * Returns the hosted device if present.
     */
    public Optional<DeviceInfo> hostedDevices() {
        return devices.stream()
                .filter(DeviceInfo::isHosted)
                .findFirst();
    }

    /**
     * Converts all devices to their full JIDs.
     */
    public List<Jid> deviceJids() {
        return devices.stream()
                .map(d -> d.toDeviceJid(userJid.user(), userJid.server()))
                .toList();
    }

    /**
     * Returns the number of devices.
     */
    public int size() {
        return devices.size();
    }

    /**
     * Returns true if there are no devices.
     */
    public boolean isEmpty() {
        return devices.isEmpty();
    }

    /**
     * Merges this device list with another, deduplicating by device ID.
     * This device list's devices take precedence over the other's.
     *
     * @param other the other device list to merge with
     * @return a new merged device list
     */
    public DeviceList merge(DeviceList other) {
        if (other == null || other.devices.isEmpty()) {
            return this;
        }

        var mergedDevices = new LinkedHashMap<Integer, DeviceInfo>();
        // Add other's devices first (lower precedence)
        for (var device : other.devices) {
            mergedDevices.put(device.id(), device);
        }
        // Overwrite with this list's devices (higher precedence)
        for (var device : devices) {
            mergedDevices.put(device.id(), device);
        }

        return new DeviceList(
                userJid,
                List.copyOf(mergedDevices.values()),
                timestamp.isAfter(other.timestamp) ? timestamp : other.timestamp,
                timestamp.isAfter(other.timestamp) ? expiresAt : other.expiresAt,
                rawId != null ? rawId : other.rawId,
                deleted,
                deletedChangedToHost,
                advAccountType != null ? advAccountType : other.advAccountType,
                expectedTs != null ? expectedTs : other.expectedTs,
                expectedTsLastDeviceJobTs,
                expectedTsUpdateTs,
                currentIndex != 0 ? currentIndex : other.currentIndex,
                !validIndexes.isEmpty() ? validIndexes : other.validIndexes
        );
    }

    /**
     * Returns true if the account type has changed compared to another device list.
     *
     * @param other the other device list to compare
     * @return true if account types differ and both are non-null
     */
    public boolean hasAccountTypeChanged(DeviceList other) {
        return other != null
                && advAccountType != null
                && other.advAccountType != null
                && advAccountType != other.advAccountType;
    }

    /**
     * Compares this device list to another and returns a detailed change report.
     *
     * @param other the previous device list
     * @return change report
     */
    public DeviceChanges mismatch(DeviceList other) {
        if (other == null) {
            return new DeviceChanges(deviceJids(), List.of(), List.of());
        }

        var otherDevices = new HashMap<Integer, DeviceInfo>();
        for (var device : other.devices) {
            otherDevices.put(device.id(), device);
        }

        var added = new ArrayList<Jid>();
        var identityChanged = new ArrayList<Jid>();

        for (var device : devices) {
            var otherDevice = otherDevices.remove(device.id());
            var deviceJid = device.toDeviceJid(userJid.user(), userJid.server());

            if (otherDevice == null) {
                added.add(deviceJid);
            } else if (otherDevice.keyIndex() >= 0 && otherDevice.keyIndex() != device.keyIndex()) {
                identityChanged.add(deviceJid);
            }
        }

        var removed = otherDevices.values().stream()
                .map(d -> d.toDeviceJid(userJid.user(), userJid.server()))
                .toList();

        return new DeviceChanges(added, removed, identityChanged);
    }
}
