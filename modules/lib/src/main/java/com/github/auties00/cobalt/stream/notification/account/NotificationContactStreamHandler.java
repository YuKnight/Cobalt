package com.github.auties00.cobalt.stream.notification.account;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.contact.Contact;
import com.github.auties00.cobalt.model.contact.ContactStatus;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.stream.SocketStream;

/**
 * Handles incoming contacts notification stanzas dispatched by
 * {@link NotificationAccountDispatcher} when the notification type is
 * {@code "contacts"}.
 *
 * <p>This handler processes four action types parsed from the notification
 * children: {@code update} (contact profile changed), {@code modify} (phone
 * number change), {@code sync} (full contact re-sync needed), and a
 * default path that simply acknowledges unhandled notification types such as
 * {@code add} or {@code remove}.
 *
 */
@WhatsAppWebModule(moduleName = "WAWebHandleContactNotification")
final class NotificationContactStreamHandler implements SocketStream.Handler {

    /**
     * Logger for diagnostic output related to contacts notification handling.
     */
    private static final System.Logger LOGGER = System.getLogger(NotificationContactStreamHandler.class.getName());

    /**
     * The WhatsApp client used to send acknowledgements and access the store.
     */
    private final WhatsAppClient whatsapp;

    /**
     * Constructs a new handler for contacts notifications.
     *
     * @param whatsapp the non-{@code null} WhatsApp client
     */
    NotificationContactStreamHandler(WhatsAppClient whatsapp) {
        this.whatsapp = whatsapp;
    }

    /**
     * Handles an incoming contacts notification node.
     *
     * <p>Validates the node, dispatches to the appropriate action handler, and
     * sends an acknowledgement stanza back to the server. Errors during
     * handling are logged; the acknowledgement is always sent.
     *
     * @param node the incoming notification node
     */
    @Override
    public void handle(Node node) {
        if (!node.hasDescription("notification") || !node.hasAttribute("type", "contacts")) {
            return;
        }

        try {
            handleNotification(node);
        } catch (Throwable throwable) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to handle contacts notification {0}: {1}",
                    node.getAttributeAsString("id", "[missing-id]"),
                    throwable.getMessage());
        } finally {
            sendNotificationAck(node);
        }
    }

    /**
     * Parses the notification node and dispatches to the appropriate case handler.
     *
     * <p>The notification node is expected to contain one of the following children: {@code update}, {@code modify}, or {@code sync}. Any other child (such as {@code add} or {@code remove}) is logged and acknowledged without further processing.
     *
     * @param node the notification node
     */
    private void handleNotification(Node node) {
        Node actionNode = null;
        for (var child : node.children()) {
            var desc = child.description();
            if ("update".equals(desc) || "add".equals(desc) || "remove".equals(desc)
                    || "modify".equals(desc) || "sync".equals(desc)) {
                actionNode = child;
                break;
            }
        }
        if (actionNode == null) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Contacts notification {0} has no supported action child",
                    node.getAttributeAsString("id", "[missing-id]"));
            return;
        }

        switch (actionNode.description()) {
            case "update" -> handleUpdate(node, actionNode);
            case "modify" -> handleModify(node, actionNode);
            case "sync" -> {
                LOGGER.log(System.Logger.Level.DEBUG, "Received contact sync notification");
                whatsapp.store().setSyncedContacts(false);
            }
            default ->
                    LOGGER.log(System.Logger.Level.DEBUG,
                            "Ignoring unhandled contacts notification type {0} for notification {1}",
                            actionNode.description(),
                            node.getAttributeAsString("id", "[missing-id]"));
        }
    }

    /**
     * Handles the {@code "update"} contact notification case.
     *
     * <p>When the update child has a {@code jid} attribute, this method resolves the
     * target contact, resets its presence to {@link ContactStatus#UNAVAILABLE},
     * and refreshes the contact's push name. When the update child has only a
     * {@code hash} attribute (with no {@code jid}), the handler logs and returns
     * since hash-based contact lookup is not supported in Cobalt. When neither
     * attribute is present, the notification is treated as empty.
     *
     * @param notificationNode the parent notification node
     * @param updateNode       the {@code "update"} child node
     */
    private void handleUpdate(Node notificationNode, Node updateNode) {
        if (updateNode.hasAttribute("jid")) {
            var targetJid = updateNode.getAttributeAsJid("jid")
                    .map(Jid::toUserJid)
                    .orElse(null);
            if (targetJid == null) {
                LOGGER.log(System.Logger.Level.DEBUG,
                        "handleContactsNotification: update cmd missing jid");
                return;
            }

            var contact = whatsapp.store()
                    .findContactByJid(targetJid)
                    .orElseGet(() -> whatsapp.store().addNewContact(targetJid));

            contact.setLastKnownPresence(ContactStatus.UNAVAILABLE);

            // Cobalt refreshes the push name as the adapted equivalent of WA Web's changeProfilePicThumb plus status refresh.
            refreshContact(targetJid, contact);
            return;
        }

        if (updateNode.hasAttribute("hash")) {
            // Cobalt does not maintain userhash, so hash-only updates fall through to the empty case.
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Ignoring hash-only contacts update notification {0}",
                    notificationNode.getAttributeAsString("id", "[missing-id]"));
            return;
        }

        LOGGER.log(System.Logger.Level.DEBUG,
                "Contacts update notification {0} has neither jid nor hash",
                notificationNode.getAttributeAsString("id", "[missing-id]"));
    }

    /**
     * Handles the {@code "modify"} contact notification case (phone number change).
     *
     * <p>Processes the phone number change by updating both the old and new chat
     * records with the appropriate {@code changeNumberNewJid} and
     * {@code changeNumberOldJid} fields. If LID attributes are present, LID-to-phone
     * mappings are registered for both old and new identifiers.
     *
     * @param notificationNode the parent notification node (unused but available for context)
     * @param modifyNode       the {@code "modify"} child node containing {@code old} and {@code new} jid attributes
     */
    private void handleModify(Node notificationNode, Node modifyNode) {
        var oldJid = modifyNode.getAttributeAsJid("old")
                .map(Jid::toUserJid)
                .orElse(null);
        var newJid = modifyNode.getAttributeAsJid("new")
                .map(Jid::toUserJid)
                .orElse(null);
        if (oldJid == null || newJid == null) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "modify notification missing old or new jid");
            return;
        }

        var newLid = modifyNode.getAttributeAsJid("new_lid")
                .map(Jid::toUserJid)
                .orElse(null);
        var oldLid = modifyNode.getAttributeAsJid("old_lid")
                .map(Jid::toUserJid)
                .orElse(null);

        whatsapp.store().findChatByJid(oldJid).ifPresent(chat -> {
            chat.setNewJid(newJid);
        });

        whatsapp.store().findChatByJid(newJid).ifPresent(chat -> {
            chat.setOldJid(oldJid);
        });

        if (oldLid != null && newLid != null) {
            whatsapp.store().registerLidMapping(oldJid, oldLid);
            whatsapp.store().registerLidMapping(newJid, newLid);
        }

        var updated = whatsapp.store()
                .findContactByJid(newJid)
                .orElseGet(() -> whatsapp.store().addNewContact(newJid));
        if (newLid != null) {
            updated.setLid(newLid);
        }
        whatsapp.store().addContact(updated);
    }

    /**
     * Refreshes a contact's push name by querying the server.
     *
     * @param targetJid the JID of the contact to refresh
     * @param contact   the contact model to update
     */
    private void refreshContact(Jid targetJid, Contact contact) {
        try {
            whatsapp.queryName(targetJid).ifPresent(contact::setChosenName);
        } catch (Throwable throwable) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Cannot refresh contact name for {0}: {1}",
                    targetJid,
                    throwable.getMessage());
        }

        whatsapp.store().addContact(contact);
    }

    /**
     * Sends an acknowledgement stanza for a contacts notification.
     *
     * <p>The ack stanza uses the notification's {@code id} and {@code from}
     * attributes, with {@code class} set to {@code "notification"} and
     * {@code type} set to {@code "contacts"}.
     *
     * @param node the notification node to acknowledge
     */
    private void sendNotificationAck(Node node) {
        var stanzaId = node.getAttributeAsString("id", null);
        var stanzaFrom = node.getAttributeAsJid("from", null);
        if (stanzaId == null || stanzaFrom == null) {
            return;
        }

        whatsapp.sendNodeWithNoResponse(new NodeBuilder()
                .description("ack")
                .attribute("id", stanzaId)
                .attribute("to", stanzaFrom)
                .attribute("class", "notification")
                .attribute("type", "contacts")
                .build());
    }
}
