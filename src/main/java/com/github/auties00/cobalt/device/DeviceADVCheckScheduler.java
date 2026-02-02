package com.github.auties00.cobalt.device;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.device.info.DeviceExpectedTsUtils;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.util.SchedulerUtils;

import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for periodic ADV (Authenticated Device Verification) device info checks.
 * Refreshes device lists to ensure ADV data doesn't expire.
 * Uses configurable thresholds from AB props and expectedTs-based staleness detection.
 */
public final class DeviceADVCheckScheduler implements Closeable {
    private static final Duration CHECK_INTERVAL = Duration.ofHours(24);

    // Default thresholds (fallback if AB props not available)
    private static final int DEFAULT_EXPIRY_DAYS = 30;
    private static final int DEFAULT_WARNING_DAYS = 7;

    private final WhatsAppClient client;
    private final DeviceService deviceService;
    private final ABPropsService abPropsService;

    private CompletableFuture<Void> scheduledTask;
    private volatile boolean running = false;

    public DeviceADVCheckScheduler(WhatsAppClient client, DeviceService deviceService, ABPropsService abPropsService) {
        this.client = client;
        this.deviceService = deviceService;
        this.abPropsService = abPropsService;
    }

    /**
     * Starts the ADV check scheduler.
     * Schedules checks every 24 hours.
     */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        scheduleNextCheck();
    }

    /**
     * Schedules the next ADV check after the configured interval.
     */
    private void scheduleNextCheck() {
        if (!running) {
            return;
        }

        scheduledTask = SchedulerUtils.scheduleDelayed(CHECK_INTERVAL, this::performCheck)
                .thenRun(this::scheduleNextCheck); // Reschedule after completion
    }

    /**
     * Performs the ADV device info check.
     * Checks all cached device lists for expiration and staleness.
     */
    private void performCheck() {
        try {
            var lastCheck = deviceService.getLastAdvCheckTime();
            if (lastCheck == null) {
                // First check - just record the time
                deviceService.updateAdvCheckTime();
                return;
            }

            // Get configurable thresholds from AB props
            var expiryDays = abPropsService.getInt(ABProp.NUM_DAYS_KEY_INDEX_LIST_EXPIRATION_AB_PROP_CODE)
                    .orElse(DEFAULT_EXPIRY_DAYS);
            var warningDays = abPropsService.getInt(ABProp.NUM_DAYS_BEFORE_DEVICE_EXPIRY_CHECK_AB_PROP_CODE)
                    .orElse(DEFAULT_WARNING_DAYS);

            var expiryThresholdSeconds = TimeUnit.DAYS.toSeconds(expiryDays);
            var warningThresholdSeconds = expiryThresholdSeconds - TimeUnit.DAYS.toSeconds(warningDays);
            var currentTime = System.currentTimeMillis() / 1000; // Convert to seconds

            // Check all device lists
            var allDeviceLists = client.store().deviceLists();
            var hasExpired = false;
            var hasWarning = false;

            for (var deviceList : allDeviceLists) {
                // Check if device list is expired
                if (DeviceExpectedTsUtils.isDeviceListStale(deviceList, currentTime, expiryThresholdSeconds)) {
                    hasExpired = true;
                    // Remove expired device list to force refresh on next access
                    client.store().removeDeviceList(deviceList.userJid());
                    continue;
                }

                // Check if device list is close to expiration
                if (DeviceExpectedTsUtils.isDeviceListCloseToExpiration(deviceList, currentTime, warningThresholdSeconds)) {
                    hasWarning = true;
                }
            }

            // Update check time
            deviceService.updateAdvCheckTime();

            // Notify listeners if any lists expired or approaching expiration
            if (hasExpired || hasWarning) {
                for (var listener : client.store().listeners()) {
                    Thread.startVirtualThread(() ->
                            listener.onDeviceAdvRefreshed(client)
                    );
                }
            }
        } catch (Exception e) {
            System.getLogger(getClass().getName())
                    .log(System.Logger.Level.ERROR, "ADV check failed", e);
        }
    }

    /**
     * Stops the ADV check scheduler and cancels any pending checks.
     */
    @Override
    public void close() {
        running = false;
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
    }
}
