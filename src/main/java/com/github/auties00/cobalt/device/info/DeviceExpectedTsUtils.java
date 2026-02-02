package com.github.auties00.cobalt.device.info;

import java.time.Duration;

/**
 * Utility class for managing expected timestamp logic in device lists.
 * <p>
 * The expected timestamp (expectedTs) system helps detect stale device lists
 * that haven't been refreshed in a while, even if the dhash matches.
 */
public final class DeviceExpectedTsUtils {
    /**
     * Threshold after which expectedTs staleness check is performed.
     * If expectedTsUpdateTs is older than this, the device list is considered potentially stale.
     */
    private static final Duration EXPECTED_TS_UPDATE_THRESHOLD = Duration.ofHours(25);

    private DeviceExpectedTsUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Determines whether expectedTs should be cleared for a device list.
     * <p>
     * ExpectedTs should be cleared when:
     * 1. The new timestamp is older than the cached timestamp (invalid), OR
     * 2. ExpectedTsUpdateTs exists and is older than 25 hours, AND
     *    expectedTsLastDeviceJobTs doesn't match the current lastADVCheckTime
     *
     * @param newTimestamp       the new timestamp from server (epoch seconds)
     * @param newExpectedTs      the new expectedTs from server (epoch seconds, may be null)
     * @param cachedList         the cached device list (may be null)
     * @param lastADVCheckTime   the last ADV device check time (epoch millis, may be null)
     * @return true if expectedTs should be cleared
     */
    public static boolean shouldClearExpectedTs(
            long newTimestamp,
            Long newExpectedTs,
            DeviceList cachedList,
            Long lastADVCheckTime
    ) {
        if (cachedList == null || cachedList.deleted()) {
            return false;
        }

        // Check if new timestamp is older than cached (invalid response)
        var cachedTimestamp = cachedList.timestamp().getEpochSecond(); // Convert to seconds
        if (newTimestamp < cachedTimestamp) {
            return true;
        }

        // Check expectedTsUpdateTs staleness (25-hour threshold)
        var expectedTsUpdateTs = cachedList.expectedTsUpdateTs();
        if (expectedTsUpdateTs != null) {
            var now = System.currentTimeMillis();
            var elapsedSinceUpdate = Duration.ofMillis(now - expectedTsUpdateTs);

            if (elapsedSinceUpdate.compareTo(EXPECTED_TS_UPDATE_THRESHOLD) >= 0) {
                // Check if expectedTsLastDeviceJobTs matches current lastADVCheckTime
                var expectedTsLastDeviceJobTs = cachedList.expectedTsLastDeviceJobTs();
                return expectedTsLastDeviceJobTs == null || !expectedTsLastDeviceJobTs.equals(lastADVCheckTime);
            }
        }

        return false;
    }

    /**
     * Determines if expectedTs has changed between two values.
     *
     * @param oldExpectedTs the old expectedTs value (may be null)
     * @param newExpectedTs the new expectedTs value (may be null)
     * @return true if expectedTs has changed
     */
    public static boolean hasExpectedTsChanged(Long oldExpectedTs, Long newExpectedTs) {
        if (oldExpectedTs == null && newExpectedTs == null) {
            return false;
        }
        if (oldExpectedTs == null || newExpectedTs == null) {
            return true;
        }
        return !oldExpectedTs.equals(newExpectedTs);
    }

    /**
     * Checks if a device list is stale based on expectedTs.
     * <p>
     * A device list is stale when:
     * 1. Regular expiration (timestamp + expiry period has passed), OR
     * 2. ExpectedTs exists and indicates the list should have been updated
     *
     * @param deviceList the device list to check
     * @param currentTime current time in epoch seconds
     * @param expiryThresholdSeconds threshold in seconds for regular expiration
     * @return true if the device list is stale
     */
    public static boolean isDeviceListStale(
            DeviceList deviceList,
            long currentTime,
            long expiryThresholdSeconds
    ) {
        var timestamp = deviceList.timestamp().getEpochSecond(); // Convert to seconds

        // Check regular expiration
        if (currentTime - timestamp >= expiryThresholdSeconds) {
            return true;
        }

        // Check expectedTs-based staleness
        var expectedTs = deviceList.expectedTs();
        if (expectedTs != null && expectedTs > timestamp) {
            // Server indicated list should have been updated by now
            return true;
        }

        return false;
    }

    /**
     * Checks if a device list is close to expiration.
     * <p>
     * Used for pre-expiration warnings and proactive refreshes.
     *
     * @param deviceList the device list to check
     * @param currentTime current time in epoch seconds
     * @param warningThresholdSeconds threshold in seconds for warning
     * @return true if the device list is close to expiration
     */
    public static boolean isDeviceListCloseToExpiration(
            DeviceList deviceList,
            long currentTime,
            long warningThresholdSeconds
    ) {
        var timestamp = deviceList.timestamp().getEpochSecond(); // Convert to seconds

        // Check if approaching regular expiration
        if (currentTime - timestamp >= warningThresholdSeconds) {
            return true;
        }

        // Check if expectedTs indicates imminent expiration
        var expectedTs = deviceList.expectedTs();
        if (expectedTs != null && expectedTs > timestamp) {
            return true;
        }

        return false;
    }
}
