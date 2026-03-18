package com.github.auties00.cobalt.stream.notification.device;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.stream.SocketStream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Handles server sync notifications by parsing the changed collection names,
 * triggering a pull sync for recognized collections, and acknowledging the
 * notification back to the server.
 *
 * @implNote WAWebHandleServerSyncNotification.handleServerSyncNotification,
 *           WAWebHandleServerSyncNotification._ (helper)
 */
public final class NotificationSyncStreamHandler implements SocketStream.Handler {
    /**
     * Logger for this handler.
     */
    private static final System.Logger LOGGER =
            System.getLogger(NotificationSyncStreamHandler.class.getName());

    /**
     * The WhatsApp client instance used to access the store and send nodes.
     */
    private final WhatsAppClient whatsapp;

    /**
     * Constructs a new notification sync stream handler.
     *
     * @param whatsapp the WhatsApp client instance
     * @implNote WAWebHandleServerSyncNotification (module-level dependency injection)
     */
    public NotificationSyncStreamHandler(WhatsAppClient whatsapp) {
        this.whatsapp = whatsapp;
    }

    /**
     * Handles an incoming server sync notification by extracting the changed
     * collection names, casting them to {@link SyncPatchType} values, and
     * triggering a pull sync for recognized collections. Unknown collection
     * names are logged and skipped. An acknowledgement stanza is always sent
     * back to the server regardless of success or failure.
     *
     * <p>Per WhatsApp Web, during bootstrap (critical data sync in process),
     * only critical collections ({@code critical_block}, {@code critical_unblock_low})
     * are processed from server sync notifications; regular collections are
     * filtered out.
     *
     * @param node the incoming notification node
     * @implNote WAWebHandleServerSyncNotification.handleServerSyncNotification (parser + dispatch),
     *           WAWebHandleServerSyncNotification._ (collection filtering + markCollectionsForSync)
     */
    @Override
    public void handle(Node node) {
        if (!node.hasDescription("notification") || !node.hasAttribute("type", "server_sync")) { // WAWebHandleServerSyncNotification.handleServerSyncNotification (parser assertTag + type check)
            return;
        }

        try {
            var from = node.getAttributeAsString("from", null); // WAWebHandleServerSyncNotification.handleServerSyncNotification (from attr)
            if (from != null && !from.equals(JidServer.user().toString())) { // WAWebHandleServerSyncNotification.handleServerSyncNotification (from !== S_WHATSAPP_NET check)
                LOGGER.log(System.Logger.Level.ERROR,
                        "handleServerSyncNotification: \"from\" is not domain jid \"s.whatsapp.net\"");
            }

            var changedCollections = new LinkedHashMap<SyncPatchType, Integer>(); // WAWebHandleServerSyncNotification._ (t = new Map)
            var unknownNames = new ArrayList<String>(); // WAWebHandleServerSyncNotification._ (a = [])
            for (var collectionNode : node.getChildren("collection")) { // WAWebHandleServerSyncNotification._ (for...of e.changedCollections)
                var collectionName = collectionNode.getAttributeAsString("name", null); // WAWebHandleServerSyncNotification._ (l = i[0])
                var collectionVersion = collectionNode.getAttributeAsInt("version", 0); // WAWebHandleServerSyncNotification._ (d = i[1])
                var collectionType = SyncPatchType.of(collectionName).orElse(null); // WAWebHandleServerSyncNotification._ (CollectionName.cast(l))
                if (collectionType != null) { // WAWebHandleServerSyncNotification._ (p != null)
                    changedCollections.put(collectionType, collectionVersion); // WAWebHandleServerSyncNotification._ (t.set(p, d))
                } else if (unknownNames.size() < 3) { // WAWebHandleServerSyncNotification._ (a.length < 3 && a.push(l))
                    unknownNames.add(collectionName);
                }
            }

            if (!unknownNames.isEmpty()) { // WAWebHandleServerSyncNotification._ (a.length > 0 warning)
                LOGGER.log(System.Logger.Level.WARNING,
                        "syncd: {0} unknown collection names in notification => {1}",
                        unknownNames.size(), unknownNames);
            }

            var collectionsToSync = List.copyOf(changedCollections.keySet()); // WAWebHandleServerSyncNotification._ (_ = Array.from(t.keys()))
            // WAWebHandleServerSyncNotification._ (e.offline → offlineNotificationsCount): skipped (WAM telemetry)

            if (!collectionsToSync.isEmpty()) { // WAWebHandleServerSyncNotification._ (markCollectionsForSync guard)
                var filteredCollections = collectionsToSync; // WAWebHandleServerSyncNotification._ (collections before filter)
                if (isCriticalDataSyncInProcess()) { // WAWebHandleServerSyncNotification._ (isSyncDCriticalDataSyncInProcess check)
                    filteredCollections = collectionsToSync.stream()
                            .filter(SyncPatchType::isCritical) // WAWebHandleServerSyncNotification._ (isCriticalCollection filter)
                            .toList();
                    LOGGER.log(System.Logger.Level.DEBUG,
                            "syncd: filtered non critical collections during bootstrap. new collections: {0}",
                            filteredCollections);
                }

                if (!filteredCollections.isEmpty()) {
                    whatsapp.store().setSyncedWebAppState(false);
                    whatsapp.pullWebAppState(filteredCollections.toArray(SyncPatchType[]::new)); // WAWebHandleServerSyncNotification._ -> WAWebSyncd.markCollectionsForSync
                }
            }
        } catch (Throwable throwable) { // ADAPTED: defensive error handling (Java practice)
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to handle server_sync notification {0}: {1}",
                    node.getAttributeAsString("id", "[missing-id]"), throwable.getMessage());
        } finally {
            sendNotificationAck(node); // WAWebHandleServerSyncNotification._ (return ack promise)
        }
    }

    /**
     * Checks whether a critical data sync (bootstrap) is currently in process.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncBootstrap.isSyncDCriticalDataSyncInProcess}:
     * returns {@code true} when the initial critical data sync phase has not yet
     * completed. In Cobalt, this is approximated by checking whether the
     * {@code critical_block} collection has not yet been bootstrapped.
     *
     * @return {@code true} if critical data sync is still in process
     * @implNote WAWebSyncBootstrap.isSyncDCriticalDataSyncInProcess
     */
    private boolean isCriticalDataSyncInProcess() {
        return !whatsapp.store() // ADAPTED: WAWebSyncBootstrap.isSyncDCriticalDataSyncInProcess (global state → per-collection check)
                .findWebAppState(SyncPatchType.CRITICAL_BLOCK)
                .bootstrapped();
    }

    /**
     * Sends an acknowledgement for the server sync notification.
     *
     * <p>Per WhatsApp Web {@code WAWebHandleServerSyncNotification}: the ack stanza
     * is always sent to {@code s.whatsapp.net} (the domain JID), with
     * {@code class="notification"} and {@code type="server_sync"}.
     *
     * @param node the notification node to acknowledge
     * @implNote WAWebHandleServerSyncNotification._ (ack stanza construction)
     */
    private void sendNotificationAck(Node node) {
        var stanzaId = node.getAttributeAsString("id", null); // WAWebHandleServerSyncNotification._ (e.stanzaId)
        if (stanzaId == null) { // ADAPTED: defensive null check (Java practice)
            return;
        }

        whatsapp.sendNodeWithNoResponse(new NodeBuilder() // WAWebHandleServerSyncNotification._ (wap("ack", {...}))
                .description("ack") // WAWebHandleServerSyncNotification._
                .attribute("id", stanzaId) // WAWebHandleServerSyncNotification._ (id: CUSTOM_STRING(e.stanzaId))
                .attribute("class", "notification") // WAWebHandleServerSyncNotification._ (class: "notification")
                .attribute("type", "server_sync") // WAWebHandleServerSyncNotification._ (type: "server_sync")
                .attribute("to", JidServer.user()) // WAWebHandleServerSyncNotification._ (to: S_WHATSAPP_NET)
                .build());
    }
}
