package com.github.auties00.cobalt.stream.notification.account;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.migration.LidMigrationService;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.stream.SocketStream;

import java.time.Instant;
import java.util.Arrays;

/**
 * Handles incoming privacy token notifications from the WhatsApp server.
 *
 * <p>When a privacy token notification is received, this handler parses the
 * token data, updates the relevant chat's trusted-contact token in the store,
 * re-subscribes to presence for the sender, and sends an acknowledgement
 * stanza back to the server.
 *
 */
@WhatsAppWebModule(moduleName = "WAWebHandlePrivacyTokensNotification")
final class NotificationPrivacyStreamHandler implements SocketStream.Handler {

    /**
     * Logger for diagnostic output from this handler.
     */
    private static final System.Logger LOGGER = System.getLogger(NotificationPrivacyStreamHandler.class.getName());

    /**
     * The WhatsApp client used for store access and node sending.
     */
    private final WhatsAppClient whatsapp;

    /**
     * Constructs a new privacy stream handler.
     *
     * @param whatsapp the WhatsApp client instance
     */
    NotificationPrivacyStreamHandler(WhatsAppClient whatsapp) {
        this.whatsapp = whatsapp;
    }

    /**
     * Handles a privacy token notification node.
     *
     * <p>Parses the notification, processes each trusted-contact token by
     * updating the chat store and re-subscribing to presence, then sends
     * an acknowledgement stanza.
     *
     * @param node the incoming notification node
     */
    @Override
    public void handle(Node node) {
        if (!node.hasDescription("notification") || !node.hasAttribute("type", "privacy_token")) {
            return;
        }

        try {
            handleNotification(node);
        } catch (Throwable throwable) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Cannot handle privacy_token notification {0}: {1}",
                    node.getAttributeAsString("id", "<missing>"),
                    throwable.getMessage());
        } finally {
            sendNotificationAck(node);
        }
    }

    /**
     * Parses the notification and processes each privacy token.
     *
     * <p>Extracts the sender's phone number JID and optional LID from the
     * notification attributes, waits for offline delivery to end, then
     * iterates over the token children dispatching each by type.
     *
     * @param node the notification node
     */
    private void handleNotification(Node node) {
        var senderPn = getUserJid(node, "from");
        var senderLid = node.getAttributeAsJid("sender_lid")
                .map(Jid::toUserJid)
                .orElse(null);
        if (senderPn == null) {
            return;
        }

        var tokensNode = node.getChild("tokens").orElse(null);
        if (tokensNode == null) {
            return;
        }

        whatsapp.store().waitForOfflineDeliveryEnd();

        for (var tokenNode : tokensNode.getChildren("token")) {
            var type = tokenNode.getAttributeAsString("type", "");
            switch (type) {
                case "trusted_contact" -> handleTrustedContactToken(senderPn, senderLid, tokenNode);
                default -> LOGGER.log(System.Logger.Level.DEBUG,
                        "incomingPrivacyTokensParser - receiving an unknown type: {0}", type);
            }
        }
    }

    /**
     * Handles a single trusted-contact token by updating the chat store
     * and re-subscribing to the sender's presence.
     *
     * <p>The token content and timestamp are extracted from the token node.
     * The chat is located by JID (phone number first, then LID if available),
     * and its trusted-contact token fields are updated. Finally, presence is
     * re-subscribed for the sender's phone number JID.
     *
     * @param senderPn  the sender's phone number JID
     * @param senderLid the sender's LID JID, or {@code null} if absent
     * @param tokenNode the token node containing content and timestamp
     */
    private void handleTrustedContactToken(Jid senderPn, Jid senderLid, Node tokenNode) {
        // Tokens for PSAs, bots, and non-user servers are silently dropped.
        if (!LidMigrationService.isRegularUser(senderPn)) {
            return;
        }

        var content = tokenNode.toContentBytes().orElse(null);
        if (content == null || content.length == 0) {
            return;
        }

        var tokenTimestamp = getInstantAttribute(tokenNode, "t");

        updateChatTcToken(senderPn, senderLid, tokenTimestamp, content);

        try {
            whatsapp.subscribeToPresence(senderPn);
        } catch (Throwable throwable) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Cannot resubscribe to presence for tc token sender {0}: {1}",
                    senderPn,
                    throwable.getMessage());
        }
    }

    /**
     * Updates the trusted-contact token on the chat corresponding to the
     * sender.
     *
     * <p>Looks up the chat first by the sender's LID JID (when provided),
     * then by the sender's phone number JID. If no chat is found, a new
     * chat entry is created on the phone number JID. Skips the update if
     * the existing token equals the new content or if the existing
     * timestamp is strictly newer than the incoming one.
     *
     * @param senderPn       the sender's phone number JID
     * @param senderLid      the sender's LID JID, or {@code null}
     * @param tokenTimestamp the timestamp from the token, or {@code null}
     * @param tcTokenContent the raw trusted-contact token bytes
     */
    private void updateChatTcToken(Jid senderPn, Jid senderLid, Instant tokenTimestamp, byte[] tcTokenContent) {
        var chat = (senderLid != null
                ? whatsapp.store().findChatByJid(senderLid).orElse(null)
                : null);
        if (chat == null) {
            chat = whatsapp.store().findChatByJid(senderPn)
                    .orElseGet(() -> whatsapp.store().addNewChat(senderPn));
        }

        var existingToken = chat.tcToken().orElse(null);
        var existingTimestamp = chat.tcTokenTimestamp().orElse(null);
        if (existingToken != null && Arrays.equals(existingToken, tcTokenContent)
                || existingTimestamp != null && tokenTimestamp != null && existingTimestamp.isAfter(tokenTimestamp)) {
            return;
        }

        chat.setTcToken(tcTokenContent);
        chat.setTcTokenTimestamp(tokenTimestamp);
    }

    /**
     * Extracts a user JID from a node attribute.
     *
     * @param node the node to extract from
     * @param key  the attribute key
     * @return the user JID, or {@code null} if not present
     */
    private Jid getUserJid(Node node, String key) {
        return node.getAttributeAsJid(key)
                .map(Jid::toUserJid)
                .orElse(null);
    }

    /**
     * Extracts an {@link Instant} from a node attribute representing epoch seconds.
     *
     * @param node the node to extract from
     * @param key  the attribute key
     * @return the parsed instant, or {@code null} if absent or non-positive
     */
    private Instant getInstantAttribute(Node node, String key) {
        var seconds = node.getAttributeAsLong(key, (Long) null);
        return seconds == null || seconds <= 0 ? null : Instant.ofEpochSecond(seconds);
    }

    /**
     * Sends an acknowledgement stanza for the processed notification.
     *
     * <p>The ACK uses the notification's ID and sender JID, with {@code class="notification"} and {@code type="privacy_token"}.
     *
     * @param node the original notification node
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
                .attribute("class", "notification")
                .attribute("to", stanzaFrom)
                .attribute("type", "privacy_token")
                .build());
    }
}
