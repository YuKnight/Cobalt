package com.github.auties00.cobalt.stream.control;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.wam.WamService;
import com.github.auties00.cobalt.wam.event.MdAppStateOfflineNotificationsEventBuilder;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks per-collection counts of offline {@code server_sync} notifications
 * received during the offline backlog window and emits a single summary
 * telemetry event when the backlog window ends.
 *
 * <p>WhatsApp Web keeps a module-level {@code Map} of collection-name to
 * observation-count inside {@code WAWebHandleReportServerSyncNotification}.
 * Every offline server-sync notification bumps the counter for each changed
 * collection; when the server signals that the offline backlog is drained
 * via the {@code <ib><offline/></ib>} bulletin, the module flushes the map
 * into a {@code MdAppStateOfflineNotificationsWamEvent} whose
 * {@code redundantCount} equals the number of notifications that exceeded
 * one per unique collection (i.e. the duplicates that did not add new
 * information for the local syncd state).
 *
 * <p>Cobalt splits the producer and consumer of this map across two socket
 * stream handlers: {@link com.github.auties00.cobalt.stream.notification.device.NotificationSyncStreamHandler}
 * feeds the map and {@link InfoBulletinStreamHandler} drains it when the
 * offline bulletin arrives. This shared reporter acts as the
 * module-equivalent state holder so that both handlers observe the same
 * map without needing to expose private state on the WhatsApp client.
 *
 * @implNote WAWebHandleReportServerSyncNotification: the module defines a
 * module-scoped {@code var e = new Map} and exports both the map itself as
 * {@code offlineNotificationsCount} and the flush function
 * {@code reportOfflineNotifications} that reads the map, logs the WAM
 * event with {@code redundantCount: sum(count - 1)}, and clears the map.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleReportServerSyncNotification")
public final class OfflineNotificationsReporter {
    /**
     * The WhatsApp client retained for parity with sibling reporters; the
     * actual WAM commit is routed through {@link #wamService}.
     *
     * @implNote WAWebHandleReportServerSyncNotification: the module closes
     * over the return value of {@code o("WAWebMdAppStateOfflineNotificationsWamEvent")}
     * to reach the WAM codegen class; Cobalt routes the commit through the
     * client-scoped {@code WamService} instead.
     */
    private final WhatsAppClient whatsapp;

    /**
     * The WAM telemetry service used to commit the offline notifications event.
     */
    private final WamService wamService;

    /**
     * Per-collection observation count for offline server-sync notifications
     * received since the last flush. Writes come from the server-sync
     * handler; reads and the atomic clear come from the info bulletin
     * handler when the offline backlog window ends.
     *
     * @implNote WAWebHandleReportServerSyncNotification: {@code var e = new Map}
     * keyed by {@code CollectionName} string; Cobalt keys on the already-cast
     * {@link SyncPatchType} enum to avoid a second string match at flush time.
     */
    private final ConcurrentMap<SyncPatchType, Integer> offlineNotificationsCount;

    /**
     * Constructs a new reporter bound to the given client.
     *
     * @param whatsapp   the WhatsApp client retained for parity with sibling reporters,
     *                   must not be {@code null}
     * @param wamService the WAM telemetry service used to commit the offline notifications event,
     *                   must not be {@code null}
     * @implNote WAWebHandleReportServerSyncNotification: module
     * initialisation creates the empty map; Cobalt does the same inside
     * the constructor.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleReportServerSyncNotification",
            exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public OfflineNotificationsReporter(WhatsAppClient whatsapp, WamService wamService) {
        this.whatsapp = Objects.requireNonNull(whatsapp, "whatsapp cannot be null");
        this.wamService = Objects.requireNonNull(wamService, "wamService cannot be null");
        this.offlineNotificationsCount = new ConcurrentHashMap<>();
    }

    /**
     * Increments the observation count for the given collection by one,
     * initialising to {@code 1} when the collection is seen for the first
     * time in the current offline backlog window.
     *
     * <p>This method is called once per changed collection for every
     * offline server-sync notification that arrives while the server is
     * still draining the offline queue.
     *
     * @param collection the collection whose offline notification count
     *                   should be bumped, must not be {@code null}
     * @implNote WAWebHandleServerSyncNotification._: the offline branch
     * runs {@code e.offline && _.forEach(function(e){ var t =
     * offlineNotificationsCount.get(e); t != null ?
     * offlineNotificationsCount.set(e, t + 1) :
     * offlineNotificationsCount.set(e, 1) })}. Cobalt collapses this to an
     * atomic {@link ConcurrentMap#merge merge} to preserve the
     * read-modify-write invariant under concurrent notification delivery.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleReportServerSyncNotification",
            exports = "offlineNotificationsCount", adaptation = WhatsAppAdaptation.ADAPTED)
    public void increment(SyncPatchType collection) {
        Objects.requireNonNull(collection, "collection cannot be null");
        offlineNotificationsCount.merge(collection, 1, Integer::sum);
    }

    /**
     * Flushes the accumulated offline notification counts by emitting a
     * {@code MdAppStateOfflineNotifications} WAM event carrying the total
     * redundant notification count and clearing the map.
     *
     * <p>The redundant count is computed as the sum of {@code (count - 1)}
     * for each collection that appears in the map. Because a single
     * well-behaved server delivery only needs one notification per dirty
     * collection to trigger a pull, every additional notification for the
     * same collection is redundant.
     *
     * <p>When the map is empty the method is a no-op, matching WA Web's
     * {@code if (!(e.size < 1))} guard.
     *
     * @implNote WAWebHandleReportServerSyncNotification.reportOfflineNotifications:
     * the function iterates the map entries, accumulates {@code t += n - 1},
     * emits {@code new MdAppStateOfflineNotificationsWamEvent({redundantCount: t}).commit()},
     * then calls {@code e.clear()}. Cobalt mirrors the same flow with
     * enum-keyed entries and routes the commit through the client-scoped
     * WAM service.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleReportServerSyncNotification",
            exports = "reportOfflineNotifications", adaptation = WhatsAppAdaptation.DIRECT)
    public void report() {
        if (offlineNotificationsCount.isEmpty()) {
            return;
        }

        var redundantCount = 0;
        for (var count : offlineNotificationsCount.values()) {
            redundantCount += count - 1;
        }

        wamService.commit(new MdAppStateOfflineNotificationsEventBuilder()
                .redundantCount(redundantCount)
                .build());

        offlineNotificationsCount.clear();
    }
}
