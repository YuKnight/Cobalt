package com.github.auties00.cobalt.sync.key;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.exception.WhatsAppWebAppStateSyncException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.sync.MissingDeviceSyncKey;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Schedules timeout checks for missing sync keys.
 * <p>
 * Per WhatsApp Web WAWebSyncdStoreMissingKeys: when sync keys are missing,
 * a timeout is scheduled. If the keys are still missing after the timeout,
 * a fatal sync error is triggered.
 *
 * @implNote WAWebSyncdStoreMissingKeys, WAWebSyncdRequestAllSyncdMissingKeysJob
 */
@WhatsAppWebModule(moduleName = "WAWebSyncdStoreMissingKeys")
@WhatsAppWebModule(moduleName = "WAWebSyncdRequestAllSyncdMissingKeysJob")
public final class MissingSyncKeyTimeoutScheduler {
    /**
     * Logger for this scheduler.
     */
    private static final System.Logger LOGGER = System.getLogger(MissingSyncKeyTimeoutScheduler.class.getName());

    /**
     * Interval in hours between periodic re-requests of missing sync keys.
     *
     * @implNote WAWebTasksDefinitions: HOUR_SECONDS * 6
     */
    private static final long RE_REQUEST_INTERVAL_HOURS = 6;

    /**
     * The WhatsApp client instance, used for fatal error handling.
     */
    private final WhatsAppClient client;

    /**
     * The WhatsApp store, used for querying missing sync keys and sync key state.
     */
    private final WhatsAppStore store;

    /**
     * The AB props service for reading timeout configuration.
     *
     * @implNote WAWebSyncdGatingUtils.getSyncdWaitForKeyTimeoutDays
     */
    private final ABPropsService abPropsService;

    /**
     * The request service for periodic re-requests of missing sync keys.
     *
     * @implNote WAWebSyncdRequestAllSyncdMissingKeysJob.requestAllSyncdMissingKeysJob
     */
    private final MissingSyncKeyRequestService requestService;

    /**
     * Single-threaded scheduler for all timeout and periodic tasks.
     *
     * @implNote Replaces WA Web's {@code setTimeout}/{@code setInterval}.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * The currently scheduled timeout check future, corresponding to WA Web's
     * module-level variable {@code S} which holds the timeout timer ID.
     *
     * @implNote WAWebSyncdStoreMissingKeys: S (timeout timer)
     */
    private volatile ScheduledFuture<?> scheduledCheck;

    /**
     * The currently scheduled all-devices-responded grace period check future.
     * Independent of {@link #scheduledCheck}.
     *
     * @implNote WAWebSyncdStoreMissingKeys._checkMissingKeyOnAllClients: asyncSleep(5e3)
     */
    private volatile ScheduledFuture<?> allDevicesCheck;

    /**
     * The periodic re-request job future.
     *
     * @implNote WAWebSyncdRequestAllSyncdMissingKeysJob.requestAllSyncdMissingKeysJob
     */
    private volatile ScheduledFuture<?> reRequestJob;

    /**
     * Creates a new timeout scheduler.
     *
     * @implNote WAWebSyncdStoreMissingKeys (module-level state: var S for timeout timer)
     * @param client         the WhatsApp client
     * @param abPropsService the AB props service for timeout configuration
     * @param requestService the request service for periodic re-requests
     */
    public MissingSyncKeyTimeoutScheduler(WhatsAppClient client, ABPropsService abPropsService, MissingSyncKeyRequestService requestService) {
        this.client = client;
        this.store = client.store();
        this.abPropsService = abPropsService;
        this.requestService = requestService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            var thread = new Thread(r, "MissingSyncKeyTimeoutScheduler");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Schedules or reschedules the timeout check for missing sync keys.
     * <p>
     * Per WhatsApp Web: calculates timeout based on earliest missing key timestamp
     * and schedules a check when that timeout expires. Wraps the internal
     * {@code _setMissingKeyTimeout} logic and is the equivalent of calling
     * {@code setMissingKeyTimeoutInTransaction} (exported as {@code k}).
     *
     * @implNote WAWebSyncdStoreMissingKeys.setMissingKeyTimeoutInTransaction, WAWebSyncdStoreMissingKeys._setMissingKeyTimeout
     */
    public synchronized void scheduleTimeoutCheck() {
        if (scheduledCheck != null && !scheduledCheck.isDone()) { // WAWebSyncdStoreMissingKeys._setMissingKeyTimeout: clearTimeout(S)
            scheduledCheck.cancel(false);
        }

        var timeout = getTimeout(); // WAWebSyncdStoreMissingKeys._setMissingKeyTimeout: getSyncdWaitForKeyTimeoutDays() * DAY_MILLISECONDS
        var delay = store.missingSyncKeys() // WAWebSyncdStoreMissingKeys._setMissingKeyTimeout: yield t.getAll()
                .stream()
                .map(MissingDeviceSyncKey::timestamp)
                .min(Instant::compareTo) // WAWebSyncdStoreMissingKeys._setMissingKeyTimeout: n.reduce((e,t) => e.timestamp < t.timestamp ? e : t)
                .map(earliest -> {
                    var elapsed = Duration.between(earliest, Instant.now()); // WAWebSyncdStoreMissingKeys._setMissingKeyTimeout: -r.timestamp + unixTimeMs()
                    var remaining = timeout.minus(elapsed); // WAWebSyncdStoreMissingKeys._setMissingKeyTimeout: timeoutMs - elapsed
                    return remaining.isNegative() ? Duration.ZERO : remaining; // ADAPTED: JS setTimeout with negative fires immediately, Java clamps to 0
                });

        if (delay.isEmpty()) { // WAWebSyncdStoreMissingKeys._setMissingKeyTimeout: if(n.length !== 0) — inverted guard
            LOGGER.log(System.Logger.Level.DEBUG, "No missing sync keys, timeout check not scheduled");
            return;
        }

        var delayMs = delay.get().toMillis();
        LOGGER.log(System.Logger.Level.DEBUG, "Scheduling missing sync key timeout check in {0}ms", delayMs);

        scheduledCheck = scheduler.schedule(this::checkForExpiredKeys, delayMs, TimeUnit.MILLISECONDS); // WAWebSyncdStoreMissingKeys._setMissingKeyTimeout: S = setTimeout(D, a)
    }

    /**
     * Checks for expired missing keys and triggers fatal error if any are found.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdStoreMissingKeys._timeoutWhileWaitingForMissingKey}:
     * re-verifies that keys are still missing before triggering a fatal error,
     * since they may have arrived between scheduling and firing. Triggers a
     * single global fatal error rather than per-key errors.
     *
     * @implNote WAWebSyncdStoreMissingKeys._timeoutWhileWaitingForMissingKey, WAWebSyncdStoreMissingKeys.hasExpiredKeys
     */
    private void checkForExpiredKeys() {
        var currentMissingKeys = store.missingSyncKeys(); // WAWebSyncdStoreMissingKeys.hasExpiredKeys: getAllMissingKeysInTransaction()
        if (currentMissingKeys.isEmpty()) {
            LOGGER.log(System.Logger.Level.DEBUG, "No missing sync keys remain, timeout check skipped");
            return;
        }

        // WAWebSyncdStoreMissingKeys.hasExpiredKeys: cross-reference with getAllSyncKeysInTransaction()
        var actuallyMissing = currentMissingKeys.stream()
                .filter(key -> store.findWebAppStateKeyById(key.keyId()).isEmpty()) // WAWebSyncdStoreMissingKeys.hasExpiredKeys: !allKeys.includes(key.keyHex)
                .toList();
        if (actuallyMissing.isEmpty()) {
            LOGGER.log(System.Logger.Level.DEBUG, "All tracked missing keys have been received");
            return;
        }

        var timeout = getTimeout(); // WAWebSyncdStoreMissingKeys.hasExpiredKeys: getSyncdWaitForKeyTimeoutDays() * DAY_MILLISECONDS
        var now = Instant.now(); // WAWebSyncdStoreMissingKeys.hasExpiredKeys: unixTimeMs()
        var expiredMissingSyncKeys = actuallyMissing
                .stream()
                .filter(key -> Duration.between(key.timestamp(), now).compareTo(timeout) > 0) // WAWebSyncdStoreMissingKeys.hasExpiredKeys: timeoutMs < unixTimeMs() - e.timestamp
                .toList();
        if (expiredMissingSyncKeys.isEmpty()) {
            LOGGER.log(System.Logger.Level.DEBUG, "No expired missing sync keys");
            scheduleTimeoutCheck(); // ADAPTED: defensive reschedule not in WA Web (WA Web relies on next setMissingKeyTimeoutInTransaction call)
            return;
        }

        // WAWebSyncdStoreMissingKeys._timeoutWhileWaitingForMissingKey: reportSyncdFatalError(TIMEOUT_WHILE_WAITING_FOR_MISSING_KEY) + handleSyncdFatal
        LOGGER.log(System.Logger.Level.ERROR, "Fatal sync error: timeout while waiting for {0} missing sync key(s)",
                expiredMissingSyncKeys.size());
        // WA Web reports SyncdFatalErrorType.TIMEOUT_WHILE_WAITING_FOR_MISSING_KEY, distinct from MISSING_KEY_ON_ALL_CLIENTS
        client.handleFailure(new WhatsAppWebAppStateSyncException.TimeoutWhileWaitingForMissingKey(
                expiredMissingSyncKeys.getFirst().keyId()));
    }

    /**
     * Checks whether any tracked missing key has now received explicit negative
     * responses from every device that was successfully asked.
     *
     * <p>This path is intentionally separate from the multi-day wait-for-key timeout:
     * after all devices have replied without the key, WhatsApp Web only applies a
     * short grace period before treating the key as unrecoverable.
     *
     * @implNote WAWebSyncdStoreMissingKeys._checkMissingKeyOnAllClients
     */
    private void checkForAllDevicesRespondedWithoutKey() {
        var currentMissingKeys = store.missingSyncKeys(); // WAWebSyncdStoreMissingKeys._checkMissingKeyOnAllClients: yield t.getAll()
        if (currentMissingKeys.isEmpty()) {
            LOGGER.log(System.Logger.Level.DEBUG, "No missing sync keys remain, all-devices-responded check skipped");
            return;
        }

        var missingOnAllDevices = currentMissingKeys.stream()
                .filter(key -> store.findMissingSyncKey(key.keyId()).isPresent()) // ADAPTED: defensive re-lookup
                .filter(MissingDeviceSyncKey::isMissingOnAllDevices) // WAWebSyncdStoreMissingKeys._checkMissingKeyOnAllClients: all deviceResponses non-null check
                .toList();
        if (missingOnAllDevices.isEmpty()) {
            LOGGER.log(System.Logger.Level.DEBUG, "No missing sync key has exhausted all device responses");
            scheduleTimeoutCheck(); // ADAPTED: defensive reschedule
            return;
        }

        // WAWebSyncdStoreMissingKeys._checkMissingKeyOnAllClients: reportSyncdFatalError(MISSING_KEY_ON_ALL_CLIENTS) + handleSyncdFatal
        LOGGER.log(System.Logger.Level.ERROR, "Fatal sync error: all asked devices responded without the requested sync key");
        client.handleFailure(new WhatsAppWebAppStateSyncException.MissingKeyOnAllDevices(
                missingOnAllDevices.getFirst().keyId()
        ));
    }

    /**
     * Gets the timeout duration from AB props.
     *
     * @implNote WAWebSyncdGatingUtils.getSyncdWaitForKeyTimeoutDays
     * @return the timeout as a {@link Duration}
     */
    private Duration getTimeout() {
        var days = SyncKeyUtils.getSyncdWaitForKeyTimeoutDays(abPropsService); // WAWebSyncdGatingUtils.getSyncdWaitForKeyTimeoutDays — delegated to SyncKeyUtils.getSyncdWaitForKeyTimeoutDays
        return Duration.ofDays(days); // ADAPTED: WA Web multiplies by DAY_MILLISECONDS; Cobalt uses Duration.ofDays
    }

    /**
     * Starts a periodic job that re-requests all missing sync keys from
     * companion devices.
     *
     * <p>Per WhatsApp Web {@code requestAllSyncdMissingKeysJob}: periodically
     * re-sends key requests for all tracked missing keys to handle cases
     * where the original request was lost or a new companion device joined.
     * The job wraps {@code WAWebSyncdHandleMissingKeys.requestAllMissingKeys}
     * in a NonPersistedJob with {@code BEST_EFFORT} priority and 30-second
     * timeout, then schedules {@code setMissingKeyTimeoutInTransaction} after
     * 20 seconds. {@code WAWebTasksDefinitions} drives the job every
     * {@code HOUR_SECONDS * 6} (6 hours).
     *
     * @implNote WAWebSyncdRequestAllSyncdMissingKeysJob.requestAllSyncdMissingKeysJob,
     *     WAWebSyncdHandleMissingKeys.requestAllMissingKeys,
     *     WAWebTasksDefinitions (6-hour cadence).
     *     ADAPTED: Cobalt uses {@link ScheduledExecutorService#scheduleAtFixedRate}
     *     instead of {@code NonPersistedJob} + {@code WAWebTasksDefinitions}.
     *     The {@code BEST_EFFORT} priority and {@code maxTimeoutMs: 30_000}
     *     wrapper have no Cobalt equivalent — the job is executed directly
     *     on a virtual thread.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdRequestAllSyncdMissingKeysJob",
            exports = "requestAllSyncdMissingKeysJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebSyncdHandleMissingKeys",
            exports = "requestAllMissingKeys",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public synchronized void startPeriodicReRequestJob() {
        if (reRequestJob != null && !reRequestJob.isDone()) { // ADAPTED: idempotent start guard (WA Web relies on WAWebTasksDefinitions single-registration)
            return;
        }

        reRequestJob = scheduler.scheduleAtFixedRate(() -> { // WAWebTasksDefinitions: HOUR_SECONDS * 6 interval
            // WAWebSyncdHandleMissingKeys.requestAllMissingKeys: var e = yield getAllMissingKeysInTransaction()
            var missingKeys = store.missingSyncKeys();
            var keyIds = missingKeys.stream() // WAWebSyncdHandleMissingKeys.requestAllMissingKeys: e.map(e => e.keyId)
                    .map(MissingDeviceSyncKey::keyId)
                    .toList();
            // WAWebSyncdHandleMissingKeys.requestAllMissingKeys: WALogger.LOG("syncd: requestAllMissingKeys: missing keys: [", keyHexes, "]")
            LOGGER.log(System.Logger.Level.INFO, "syncd: requestAllMissingKeys: missing keys: [{0}]",
                    missingKeys.stream().map(MissingDeviceSyncKey::keyId).map(SyncKeyUtils::syncKeyIdToHex).toList());
            if (keyIds.isEmpty()) { // WAWebSyncdHandleMissingKeys.requestAllMissingKeys: e.length !== 0 && (yield sendSyncdKeyRequest(...))
                return;
            }
            Thread.startVirtualThread(() -> { // ADAPTED: virtual thread replaces async generator + NonPersistedJob 30s timeout
                requestService.reRequestMissingKeys(keyIds); // WAWebSyncdRequestAllSyncdMissingKeysJob: yield requestAllMissingKeys()
                scheduler.schedule(this::scheduleTimeoutCheck, 20, TimeUnit.SECONDS); // WAWebSyncdRequestAllSyncdMissingKeysJob: self.setTimeout(setMissingKeyTimeoutInTransaction, 1e3*20)
            });
        }, RE_REQUEST_INTERVAL_HOURS, RE_REQUEST_INTERVAL_HOURS, TimeUnit.HOURS); // WAWebTasksDefinitions: HOUR_SECONDS*6
    }

    /**
     * Schedules a short-delay check before triggering fatal error when all
     * devices have responded without the key.
     *
     * <p>Per WhatsApp Web behavior, a 5-second grace period is added before
     * declaring a key as missing on all devices. This allows for late-arriving
     * key share responses that may resolve the missing key.
     *
     * @implNote WAWebSyncdStoreMissingKeys._checkMissingKeyOnAllClients
     */
    public synchronized void scheduleAllDevicesRespondedCheck() {
        if (allDevicesCheck != null && !allDevicesCheck.isDone()) { // ADAPTED: cancel previous grace-period check if already pending
            allDevicesCheck.cancel(false);
        }

        LOGGER.log(System.Logger.Level.DEBUG, "Scheduling 5-second grace period before missing key fatal");
        allDevicesCheck = scheduler.schedule(this::checkForAllDevicesRespondedWithoutKey, 5, TimeUnit.SECONDS); // ADAPTED: WAWebSyncdStoreMissingKeys._checkMissingKeyOnAllClients: asyncSleep(5e3) moved to schedule delay
    }

    /**
     * Cancels any pending timeout check.
     * <p>
     * Per WhatsApp Web {@code WAWebSyncdStoreMissingKeys._setMissingKeyTimeout}:
     * corresponds to the {@code clearTimeout(S); S = null} preamble when the
     * caller wants to cancel without rescheduling.
     *
     * @implNote WAWebSyncdStoreMissingKeys._setMissingKeyTimeout (clearTimeout(S); S = null)
     */
    public synchronized void cancel() {
        if (scheduledCheck != null && !scheduledCheck.isDone()) {
            scheduledCheck.cancel(false);
        }
    }

    /**
     * Shuts down the scheduler, cancelling all pending timeout checks,
     * all-devices-responded grace period checks, and the periodic re-request job.
     *
     * @implNote NO_WA_BASIS: Java lifecycle management, WA Web relies on page unload
     */
    public void shutdown() {
        cancel();
        if (allDevicesCheck != null && !allDevicesCheck.isDone()) {
            allDevicesCheck.cancel(false);
        }
        if (reRequestJob != null && !reRequestJob.isDone()) {
            reRequestJob.cancel(false);
        }
        scheduler.shutdown();
    }
}
