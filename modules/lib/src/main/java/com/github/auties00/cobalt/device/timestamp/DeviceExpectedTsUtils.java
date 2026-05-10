package com.github.auties00.cobalt.device.timestamp;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.info.DeviceList;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Utility methods that implement WhatsApp's expected-timestamp staleness logic for
 * device lists.
 *
 * <p>A device list whose dhash matches the server's is not necessarily up-to-date:
 * the server may know a newer list exists but choose not to send it, instead
 * indicating the expected next timestamp. Cobalt mirrors WA Web by tracking three
 * timestamps per device record (the expected timestamp, when it was last updated,
 * and which ADV job run observed it) and using them to refresh, clear, or expire
 * records.
 *
 * <p>Consumed by {@link com.github.auties00.cobalt.device.DeviceService} during
 * USync response handling and by
 * {@link com.github.auties00.cobalt.device.adv.DeviceADVChecker} during the
 * periodic expiration check.
 */
@WhatsAppWebModule(moduleName = "WAWebAdvExpectedTsApi")
@WhatsAppWebModule(moduleName = "WAWebAdvDeviceInfoCheckJob")
public final class DeviceExpectedTsUtils {

    /**
     * Staleness threshold (25 hours) used by the ADV scheduler when comparing the
     * expected-timestamp update instant.
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvDeviceInfoCheckJob",
            exports = "runAdvDeviceInfoCheck",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final Duration EXPECTED_TIMESTAMP_UPDATE_THRESHOLD = Duration.ofHours(25);

    /**
     * Prevents instantiation.
     *
     * @throws UnsupportedOperationException always
     */
    private DeviceExpectedTsUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Determines whether the expected timestamp should be cleared for a device list.
     *
     * <p>Returns {@code true} when either the server timestamp has caught up to the
     * cached expected timestamp, or when the incoming expected timestamp matches the
     * cached value and a staleness condition is met based on the last ADV job check.
     * @param incomingTimestamp         the timestamp from server response
     * @param incomingExpectedTimestamp the expected timestamp from server response, or {@code null}
     * @param cachedList                the cached device list, or {@code null}
     * @param lastADVCheckTime          the last ADV device check time, or {@code null}
     * @return {@code true} if expected timestamp should be cleared
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "shouldClearExpectedTs",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static boolean shouldClearExpectedTimestamp(
            Instant incomingTimestamp,
            Instant incomingExpectedTimestamp,
            DeviceList cachedList,
            Instant lastADVCheckTime
    ) {
        if (cachedList == null || cachedList.deleted() || cachedList.expectedTimestamp() == null) {
            return false;
        }

        var cachedExpectedTimestamp = cachedList.expectedTimestamp();

        // The server has caught up, so the expectation is no longer interesting.
        if (!incomingTimestamp.isBefore(cachedExpectedTimestamp)) {
            return true;
        }

        // The expectation is unchanged but a fresh ADV job ran after the cached one.
        if (incomingExpectedTimestamp == null || !incomingExpectedTimestamp.equals(cachedExpectedTimestamp) || lastADVCheckTime == null) {
            return false;
        }

        var cachedLastJobTimestamp = cachedList.expectedTimestampLastDeviceJobTimestamp();
        return cachedLastJobTimestamp == null || lastADVCheckTime.isAfter(cachedLastJobTimestamp);
    }

    /**
     * Determines if expected timestamp has changed between two values.
     *
     * <p>Handles {@code null} values safely: two {@code null} values are considered
     * equal, and a {@code null} compared to a non-{@code null} value is considered changed.
     * @param oldExpectedTimestamp the old expected timestamp, or {@code null}
     * @param newExpectedTimestamp the new expected timestamp, or {@code null}
     * @return {@code true} if the timestamps differ
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "computeNewExpectedTs",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static boolean hasExpectedTimestampChanged(Instant oldExpectedTimestamp, Instant newExpectedTimestamp) {
        if (oldExpectedTimestamp == null && newExpectedTimestamp == null) {
            return false;
        }
        if (oldExpectedTimestamp == null || newExpectedTimestamp == null) {
            return true;
        }
        return !oldExpectedTimestamp.equals(newExpectedTimestamp);
    }

    /**
     * Computes expected timestamp tracking fields when processing a device list update.
     *
     * <p>Extracts current expected timestamp values from the cached device list (if
     * non-deleted) and delegates to {@link #computeNewExpectedTimestamp} for the
     * actual computation.
     * @param incomingTimestamp the timestamp from the server response
     * @param cachedList        the cached device list, or {@code null}
     * @param lastADVCheckTime  the last ADV device check time, or {@code null}
     * @return the computed expected timestamp fields
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "computeExpectedTsForDeviceRecord",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static ExpectedTimestampResult computeExpectedTimestampForDeviceRecord(
            Instant incomingTimestamp,
            DeviceList cachedList,
            Instant lastADVCheckTime
    ) {
        if (cachedList == null) {
            return new ExpectedTimestampResult(null, null, null);
        }

        var currentTimestamp = cachedList.timestamp();
        if (currentTimestamp == null) {
            return new ExpectedTimestampResult(null, null, null);
        }
        Instant currentExpectedTimestamp = null;
        Instant currentExpectedTimestampLastDeviceJobTimestamp = null;
        Instant currentExpectedTimestampUpdateTimestamp = null;

        // Existing values are only carried forward from non-deleted records.
        if (!cachedList.deleted()) {
            currentExpectedTimestamp = cachedList.expectedTimestamp();
            currentExpectedTimestampLastDeviceJobTimestamp = cachedList.expectedTimestampLastDeviceJobTimestamp();
            currentExpectedTimestampUpdateTimestamp = cachedList.expectedTimestampUpdateTimestamp();
        }

        return computeNewExpectedTimestamp(
                incomingTimestamp,
                currentTimestamp,
                lastADVCheckTime,
                currentExpectedTimestamp,
                currentExpectedTimestampLastDeviceJobTimestamp,
                currentExpectedTimestampUpdateTimestamp
        );
    }

    /**
     * Computes new expected timestamp tracking fields based on incoming and current values.
     *
     * <p>If the current timestamp or current expected timestamp already meets or
     * exceeds the incoming timestamp, the existing values are preserved. Otherwise,
     * the expected timestamp is set to the incoming timestamp, and the update
     * timestamp is refreshed when the expected timestamp is newly set or the current
     * timestamp has caught up to the previous expected timestamp.
     * @param incomingTimestamp                              the incoming timestamp
     * @param currentTimestamp                               the current timestamp
     * @param lastADVCheckTime                               the last ADV check time, or {@code null}
     * @param currentExpectedTimestamp                       the current expected timestamp, or {@code null}
     * @param currentExpectedTimestampLastDeviceJobTimestamp the current last job timestamp, or {@code null}
     * @param currentExpectedTimestampUpdateTimestamp        the current update timestamp, or {@code null}
     * @return the computed expected timestamp fields
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvExpectedTsApi",
            exports = "computeNewExpectedTs",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static ExpectedTimestampResult computeNewExpectedTimestamp(
            Instant incomingTimestamp,
            Instant currentTimestamp,
            Instant lastADVCheckTime,
            Instant currentExpectedTimestamp,
            Instant currentExpectedTimestampLastDeviceJobTimestamp,
            Instant currentExpectedTimestampUpdateTimestamp
    ) {
        var result = new ExpectedTimestampResult(
                currentExpectedTimestamp,
                currentExpectedTimestampLastDeviceJobTimestamp,
                currentExpectedTimestampUpdateTimestamp
        );

        if (!currentTimestamp.isBefore(incomingTimestamp)) {
            return result;
        }

        if (currentExpectedTimestamp != null && !currentExpectedTimestamp.isBefore(incomingTimestamp)) {
            return result;
        }

        // The update instant is refreshed only when a new expectation target is set,
        // not when the existing one is reaffirmed.
        var newExpectedTimestampUpdateTimestamp = currentExpectedTimestampUpdateTimestamp;
        if (currentExpectedTimestamp == null || !currentTimestamp.isBefore(currentExpectedTimestamp)) {
            newExpectedTimestampUpdateTimestamp = Instant.now();
        }

        return new ExpectedTimestampResult(incomingTimestamp, lastADVCheckTime, newExpectedTimestampUpdateTimestamp);
    }

    /**
     * Checks if a device list is stale (expired) based on timestamp and expected timestamp.
     *
     * <p>A device list is considered stale if its timestamp exceeds the expiry
     * threshold, or if its expected timestamp update timestamp was set more than
     * 25 hours ago and the last device job timestamp does not match the last ADV
     * check time.
     * @param deviceList       the device list to check
     * @param currentTime      current time
     * @param expiryThreshold  threshold for regular expiration
     * @param lastADVCheckTime the last ADV device check time, or {@code null}
     * @return {@code true} if the device list is stale
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvDeviceInfoCheckJob",
            exports = "runAdvDeviceInfoCheck",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static boolean isDeviceListStale(
            DeviceList deviceList,
            Instant currentTime,
            Duration expiryThreshold,
            Instant lastADVCheckTime
    ) {
        var timestamp = deviceList.timestamp();

        if (Duration.between(timestamp, currentTime).compareTo(expiryThreshold) >= 0) {
            return true;
        }

        var expectedTimestampUpdateTimestamp = deviceList.expectedTimestampUpdateTimestamp();
        if (expectedTimestampUpdateTimestamp == null) {
            return false;
        }

        var elapsedSinceUpdate = Duration.between(expectedTimestampUpdateTimestamp, currentTime);
        if (elapsedSinceUpdate.compareTo(EXPECTED_TIMESTAMP_UPDATE_THRESHOLD) < 0) {
            return false;
        }

        var expectedTimestampLastDeviceJobTimestamp = deviceList.expectedTimestampLastDeviceJobTimestamp();
        return !Objects.equals(expectedTimestampLastDeviceJobTimestamp, lastADVCheckTime);
    }

    /**
     * Checks if a device list is close to expiration.
     *
     * <p>Returns {@code true} if the device list's timestamp exceeds the warning
     * threshold, or if the expected timestamp is set and ahead of the current
     * timestamp (indicating a newer device list version exists on the server).
     * @param deviceList       the device list to check
     * @param currentTime      current time
     * @param warningThreshold threshold for warning
     * @return {@code true} if the device list is close to expiration
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvDeviceInfoCheckJob",
            exports = "runAdvDeviceInfoCheck",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static boolean isDeviceListCloseToExpiration(
            DeviceList deviceList,
            Instant currentTime,
            Duration warningThreshold
    ) {
        var timestamp = deviceList.timestamp();

        if (Duration.between(timestamp, currentTime).compareTo(warningThreshold) >= 0) {
            return true;
        }

        var expectedTimestamp = deviceList.expectedTimestamp();
        return expectedTimestamp != null && expectedTimestamp.isAfter(timestamp);
    }
}
