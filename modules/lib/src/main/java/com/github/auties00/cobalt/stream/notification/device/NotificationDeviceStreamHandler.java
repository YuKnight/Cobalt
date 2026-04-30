package com.github.auties00.cobalt.stream.notification.device;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.device.DeviceService;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.stream.SocketStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles incoming device notification stanzas (type="devices").
 * <p>
 * Parses add, remove, and update device notifications, registers LID-PN mappings,
 * and dispatches to {@link DeviceService} for add/remove or device list sync for updates.
 * Processes notifications for both primary and secondary (LID/PN) user identities,
 * mirroring WA Web's dual-wid processing pattern.
 *
 * @implNote Adapts the following WA Web modules:
 * <ul>
 *   <li>{@code WAWebHandleDeviceNotification.handleDevicesNotification}: main entry point
 *       for device notification processing. Parses the notification stanza, builds an ack,
 *       registers LID-PN mappings, and dispatches each dual-wid entry to the ADV handler
 *       or device sync job.</li>
 *   <li>{@code WAWebAdvHandlerApi.handleADVDeviceNotification}: invoked indirectly through
 *       {@link DeviceService#handleDeviceNotification} for add/remove actions.</li>
 *   <li>{@code WAWebSyncDeviceAdvDeviceListJob.syncDeviceListJob}: invoked indirectly
 *       through {@link DeviceService#getDeviceLists} with context {@code "notification"}
 *       for update actions.</li>
 * </ul>
 * The WA Web {@code isResumeFromRestartComplete}/{@code isResumeOnSocketDisconnectInProgress}
 * offline-mode gating and the "NO_ACK" deferred-ack path are intentionally not replicated:
 * Cobalt sends the ack unconditionally and does not maintain an offline pending-device cache.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleDeviceNotification")
public final class NotificationDeviceStreamHandler implements SocketStream.Handler {

    /**
     * Logger for device notification operations.
     */
    private static final System.Logger LOGGER =
            System.getLogger(NotificationDeviceStreamHandler.class.getName());

    /**
     * The WhatsApp client used for sending ack stanzas and accessing the store.
     */
    private final WhatsAppClient whatsapp;

    /**
     * The device service used for handling add and remove notifications and for syncing device lists.
     */
    private final DeviceService deviceService;

    /**
     * Constructs a new device notification stream handler.
     *
     * @param whatsapp      the WhatsApp client
     * @param deviceService the device service for device list operations
     */
    public NotificationDeviceStreamHandler(WhatsAppClient whatsapp, DeviceService deviceService) {
        this.whatsapp = whatsapp;
        this.deviceService = deviceService;
    }

    /**
     * Handles an incoming device notification stanza.
     * <p>
     * Validates the stanza is a notification with type="devices", extracts the user JID from the
     * {@code from} attribute and the optional LID from the {@code lid} attribute, registers
     * LID-PN mappings, and dispatches to the appropriate handler based on the action type
     * (add, remove, or update). Both the primary user and secondary (LID/PN) identity are
     * processed when available. An ack stanza is always sent to the server.
     *
     * @param node the incoming notification stanza node
     */
    @Override
    public void handle(Node node) {
        if (!node.hasDescription("notification") || !node.hasAttribute("type", "devices")) {
            return;
        }

        var userJid = node.getAttributeAsJid("from")
                .map(Jid::toUserJid)
                .orElse(null);
        if (userJid == null) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Skipping devices notification without from attribute");
            return;
        }

        var lidUser = node.getAttributeAsJid("lid")
                .map(Jid::toUserJid)
                .orElse(null);

        var actionNode = node.getChild("remove")
                .or(() -> node.getChild("add"))
                .or(() -> node.getChild("update"))
                .orElse(null);
        if (actionNode == null) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "[devices] notif missing \"remove\" or \"add\" node");
            return;
        }
        var actionType = actionNode.description();

        var hash = actionType.equals("update")
                ? actionNode.getAttributeAsString("hash", null)
                : null;

        var secondaryJid = userJid.hasLidServer()
                ? whatsapp.store().getPhoneNumberByLid(userJid).orElse(null)
                : lidUser;

        if (lidUser != null) {
            whatsapp.store().registerLidMapping(userJid, lidUser);
        }

        var entries = new ArrayList<Jid>();
        entries.add(userJid);
        if (secondaryJid != null) {
            entries.add(secondaryJid);
        }

        for (var entryJid : entries) {
            try {
                processDeviceEntry(entryJid, actionType, actionNode, hash);
            } catch (Throwable throwable) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "handleDevicesNotification - {0} error: {1}",
                        actionType, throwable.getMessage());
            }
        }

        sendNotificationAck(node, userJid);
    }

    /**
     * Processes a single device notification entry for a given user JID.
     * <p>
     * For add/remove actions, delegates to {@link DeviceService#handleDeviceNotification}.
     * For update actions, triggers a device list sync via {@link DeviceService#getDeviceLists}.
     *
     * @param entryJid   the user JID to process the notification for
     * @param actionType the action type ("add", "remove", or "update")
     * @param actionNode the action child node from the notification stanza
     * @param hash       the hash attribute value for update notifications, or {@code null}
     * @implNote Cobalt does not maintain a contact-by-hash index. WA Web's update branch resolves the opaque side-contact hash to a wid through getContactRecordByHash; Cobalt syncs the device list for the entry JID directly because the entry JID already identifies the user.
     */
    private void processDeviceEntry(Jid entryJid, String actionType, Node actionNode, String hash) {
        switch (actionType) {
            case "add", "remove" -> {
                deviceService.handleDeviceNotification(
                        normalizeDeviceActionNode(actionNode),
                        actionType,
                        entryJid
                );
            }
            case "update" -> {
                if (hash == null) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "[devices] update notification missing hash for {0}", entryJid);
                    return;
                }
                deviceService.getDeviceLists(List.of(entryJid), "notification", null, false);
            }
            default -> LOGGER.log(System.Logger.Level.WARNING,
                    "handleDevicesNotification - unknown notification type: {0}", actionType);
        }
    }

    /**
     * Normalizes an action node for consumption by {@link DeviceService#handleDeviceNotification}.
     * <p>
     * If the action node already has a {@code device-list} child or is missing a
     * {@code key-index-list} child, it is returned unchanged. Otherwise, wraps the device
     * children in a {@code device-list} node and rebuilds the action node structure.
     *
     * @param actionNode the original action node from the notification stanza
     * @return the normalized action node
     * @implNote ADAPTED: WAWebHandleDeviceNotification.h: the WA Web parser extracts device and
     * key-index-list as separate fields. DeviceService.handleDeviceNotification expects them as
     * child nodes of the action node, so this method adapts the stanza structure.
     */
    private Node normalizeDeviceActionNode(Node actionNode) {
        if (actionNode.getChild("device-list").isPresent()
                || actionNode.getChild("key-index-list").isEmpty()) {
            return actionNode;
        }

        var deviceChildren = actionNode.getChildren("device");
        if (deviceChildren.isEmpty()) {
            return actionNode;
        }

        var deviceListNode = new NodeBuilder()
                .description("device-list")
                .content(deviceChildren)
                .build();

        var rebuiltChildren = new ArrayList<Node>();
        rebuiltChildren.add(deviceListNode);
        actionNode.getChild("key-index-list").ifPresent(rebuiltChildren::add);

        return new NodeBuilder()
                .description(actionNode.description())
                .content(rebuiltChildren)
                .build();
    }

    /**
     * Sends an ack stanza for a device notification.
     * <p>
     * Builds and sends an ack with the user JID (not the raw device JID from the
     * notification), the stanza id, and class="notification".
     *
     * @param node    the original notification stanza node
     * @param userJid the user-level JID extracted from the {@code from} attribute
     * @implNote The ack only carries to, id, and class. There is no type or participant attribute.
     */
    private void sendNotificationAck(Node node, Jid userJid) {
        var stanzaId = node.getAttributeAsString("id", null);
        if (stanzaId == null) {
            return;
        }

        whatsapp.sendNodeWithNoResponse(new NodeBuilder()
                .description("ack")
                .attribute("id", stanzaId)
                .attribute("class", "notification")
                .attribute("to", userJid)
                .build());
    }
}
