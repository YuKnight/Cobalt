package com.github.auties00.cobalt.device.util;

import com.github.auties00.cobalt.device.model.DeviceList;

import java.time.Duration;

/**
 * Utility class for managing expected timestamp logic in device lists.
 * <p>
 * The expected timestamp (expectedTs) system helps detect stale device lists
 * that haven't been refreshed in a while, even if the dhash matches.
 */
public final class DeviceExpectedTsUtils {
    /**
     * Threshold after which expectedTs staleness check is performed in the ADV scheduler.
     * If expectedTsUpdateTs is older than this during ADV check, staleness is checked.
     */
    private static final Duration EXPECTED_TS_UPDATE_THRESHOLD = Duration.ofHours(25);

    private DeviceExpectedTsUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Determines whether expectedTs should be cleared for a device list.
     * <p>
     * This matches WhatsApp Web's shouldClearExpectedTs logic exactly.
     * ExpectedTs should be cleared when:
     * <ol>
     *   <li>incomingTs >= cachedList.expectedTs (server timestamp caught up), OR</li>
     *   <li>incomingTs < cachedList.expectedTs AND incomingExpectedTs == cachedList.expectedTs
     *       AND lastADVCheckTime is set AND (cachedList.expectedTsLastDeviceJobTs is null
     *       OR lastADVCheckTime > cachedList.expectedTsLastDeviceJobTs)</li>
     * </ol>
     *
     * @param incomingTs         the timestamp from server response (epoch seconds)
     * @param incomingExpectedTs the expectedTs from server response (epoch seconds, may be null)
     * @param cachedList         the cached device list (may be null)
     * @param lastADVCheckTime   the last ADV device check time (epoch millis, may be null)
     * @return true if expectedTs should be cleared
     */
    public static boolean shouldClearExpectedTs(
            long incomingTs,
            Long incomingExpectedTs,
            DeviceList cachedList,
            Long lastADVCheckTime
    ) {
        // If no cached list, deleted, or no expectedTs set, don't clear
        if (cachedList == null || cachedList.deleted() || cachedList.expectedTs() == null) {
            return false;
        }

        var cachedExpectedTs = cachedList.expectedTs();

        // Condition 1: Server timestamp has caught up to or exceeded the expected timestamp
        if (incomingTs >= cachedExpectedTs) {
            return true;
        }

        // Condition 2: Complex staleness check
        // - incomingTs < cachedExpectedTs (server timestamp hasn't caught up yet)
        // - incomingExpectedTs == cachedExpectedTs (server agrees on the expected timestamp)
        // - lastADVCheckTime is set
        // - cachedList.expectedTsLastDeviceJobTs is null OR lastADVCheckTime > cachedList.expectedTsLastDeviceJobTs
        if (incomingExpectedTs != null
                && incomingExpectedTs.equals(cachedExpectedTs)
                && lastADVCheckTime != null) {
            var cachedLastJobTs = cachedList.expectedTsLastDeviceJobTs();
            if (cachedLastJobTs == null || lastADVCheckTime > cachedLastJobTs) {
                return true;
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
     * Result of computing expectedTs fields for a device record.
     *
     * @param expectedTs              the new expectedTs value (may be null)
     * @param expectedTsLastDeviceJobTs the new expectedTsLastDeviceJobTs value (may be null)
     * @param expectedTsUpdateTs      the new expectedTsUpdateTs value (may be null)
     */
    public record ComputedExpectedTs(
            Long expectedTs,
            Long expectedTsLastDeviceJobTs,
            Long expectedTsUpdateTs
    ) {}

    /**
     * Computes the new expectedTs tracking fields when processing a device list update.
     * <p>
     * This matches WhatsApp Web's computeExpectedTsForDeviceRecord logic exactly.
     *
     * @param incomingTs       the timestamp from the server response (epoch seconds)
     * @param cachedList       the cached device list (may be null)
     * @param lastADVCheckTime the last ADV device check time (epoch millis, may be null)
     * @return the computed expectedTs fields
     */
    public static ComputedExpectedTs computeExpectedTsForDeviceRecord(
            long incomingTs,
            DeviceList cachedList,
            Long lastADVCheckTime
    ) {
        // If no cached list or null timestamp, return empty result
        if (cachedList == null) {
            return new ComputedExpectedTs(null, null, null);
        }

        var currentTs = cachedList.timestamp().getEpochSecond();

        // Get current expectedTs values (null if deleted)
        Long currentExpectedTs = null;
        Long currentExpectedTsLastDeviceJobTs = null;
        Long currentExpectedTsUpdateTs = null;

        if (!cachedList.deleted()) {
            currentExpectedTs = cachedList.expectedTs();
            currentExpectedTsLastDeviceJobTs = cachedList.expectedTsLastDeviceJobTs();
            currentExpectedTsUpdateTs = cachedList.expectedTsUpdateTs();
        }

        return computeNewExpectedTs(
                incomingTs,
                currentTs,
                lastADVCheckTime,
                currentExpectedTs,
                currentExpectedTsLastDeviceJobTs,
                currentExpectedTsUpdateTs
        );
    }

    /**
     * Computes new expectedTs tracking fields based on incoming and current values.
     * <p>
     * This matches WhatsApp Web's computeNewExpectedTs logic exactly.
     *
     * @param incomingTs                     the timestamp from the server response (epoch seconds)
     * @param currentTs                      the current cached timestamp (epoch seconds)
     * @param lastADVCheckTime               the last ADV device check time (epoch millis, may be null)
     * @param currentExpectedTs              the current expectedTs (may be null)
     * @param currentExpectedTsLastDeviceJobTs the current expectedTsLastDeviceJobTs (may be null)
     * @param currentExpectedTsUpdateTs      the current expectedTsUpdateTs (may be null)
     * @return the computed expectedTs fields
     */
    public static ComputedExpectedTs computeNewExpectedTs(
            long incomingTs,
            long currentTs,
            Long lastADVCheckTime,
            Long currentExpectedTs,
            Long currentExpectedTsLastDeviceJobTs,
            Long currentExpectedTsUpdateTs
    ) {
        // Start with current values
        var result = new ComputedExpectedTs(
                currentExpectedTs,
                currentExpectedTsLastDeviceJobTs,
                currentExpectedTsUpdateTs
        );

        // If currentTs >= incomingTs, or if currentExpectedTs >= incomingTs,
        // keep the existing values (no update needed)
        if (currentTs >= incomingTs) {
            return result;
        }
        if (currentExpectedTs != null && currentExpectedTs >= incomingTs) {
            return result;
        }

        // Update expectedTs to the incoming timestamp
        var newExpectedTs = incomingTs;
        var newExpectedTsLastDeviceJobTs = lastADVCheckTime;

        // Update expectedTsUpdateTs only if currentExpectedTs is null or currentTs >= currentExpectedTs
        Long newExpectedTsUpdateTs = currentExpectedTsUpdateTs;
        if (currentExpectedTs == null || currentTs >= currentExpectedTs) {
            newExpectedTsUpdateTs = System.currentTimeMillis();
        }

        return new ComputedExpectedTs(newExpectedTs, newExpectedTsLastDeviceJobTs, newExpectedTsUpdateTs);
    }

    /**
     * Checks if a device list is stale (expired) based on timestamp and expectedTs.
     * <p>
     * This matches WhatsApp Web's expiration logic exactly.
     * A device list is stale when:
     * <ol>
     *   <li>Regular expiration: currentTime - timestamp >= expiryThreshold, OR</li>
     *   <li>ExpectedTs-based staleness: expectedTsUpdateTs is older than 25 hours AND
     *       expectedTsLastDeviceJobTs != lastADVCheckTime</li>
     * </ol>
     *
     * @param deviceList               the device list to check
     * @param currentTime              current time in epoch seconds
     * @param expiryThresholdSeconds   threshold in seconds for regular expiration
     * @param lastADVCheckTime         the last ADV device check time (epoch millis, may be null)
     * @return true if the device list is stale
     */
    public static boolean isDeviceListStale(
            DeviceList deviceList,
            long currentTime,
            long expiryThresholdSeconds,
            Long lastADVCheckTime
    ) {
        var timestamp = deviceList.timestamp().getEpochSecond(); // Convert to seconds

        // Condition 1: Regular expiration based on timestamp
        if (currentTime - timestamp >= expiryThresholdSeconds) {
            return true;
        }

        // Condition 2: ExpectedTs-based staleness
        // Check if expectedTsUpdateTs is older than 25 hours AND expectedTsLastDeviceJobTs doesn't match
        var expectedTsUpdateTs = deviceList.expectedTsUpdateTs();
        if (expectedTsUpdateTs != null) {
            var currentTimeMillis = currentTime * 1000; // Convert to millis for comparison
            var elapsedSinceUpdate = Duration.ofMillis(currentTimeMillis - expectedTsUpdateTs);

            if (elapsedSinceUpdate.compareTo(EXPECTED_TS_UPDATE_THRESHOLD) >= 0) {
                var expectedTsLastDeviceJobTs = deviceList.expectedTsLastDeviceJobTs();
                // Stale if job timestamps don't match
                if (expectedTsLastDeviceJobTs == null || !expectedTsLastDeviceJobTs.equals(lastADVCheckTime)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a device list is stale (expired) based on timestamp and expectedTs.
     * <p>
     * Overload without lastADVCheckTime for backward compatibility.
     *
     * @param deviceList             the device list to check
     * @param currentTime            current time in epoch seconds
     * @param expiryThresholdSeconds threshold in seconds for regular expiration
     * @return true if the device list is stale
     */
    public static boolean isDeviceListStale(
            DeviceList deviceList,
            long currentTime,
            long expiryThresholdSeconds
    ) {
        return isDeviceListStale(deviceList, currentTime, expiryThresholdSeconds, null);
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
