package com.github.auties00.cobalt.stream.notification.account;

import com.github.auties00.cobalt.client.WhatsAppClient;
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
 * @implNote WAWebHandlePrivacyTokensNotification.default
 */
final class NotificationPrivacyStreamHandler implements SocketStream.Handler {

    /**
     * Logger for this handler.
     *
     * @implNote WAWebHandlePrivacyTokensNotification (WALogger usage)
     */
    private static final System.Logger LOGGER = System.getLogger(NotificationPrivacyStreamHandler.class.getName());

    /**
     * The WhatsApp client instance used for store access and node sending.
     *
     * @implNote WAWebHandlePrivacyTokensNotification (module-level dependency injection)
     */
    private final WhatsAppClient whatsapp;

    /**
     * Constructs a new privacy stream handler.
     *
     * @param whatsapp the WhatsApp client instance
     * @implNote WAWebHandlePrivacyTokensNotification (constructor DI)
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
     * @implNote WAWebHandlePrivacyTokensNotification.default: in WA Web the
     *           parser function {@code f/g} throws on parse errors and returns
     *           the constructed ACK stanza {@code i} on success. ADAPTED:
     *           Cobalt logs the parse failure and still emits the ACK in a
     *           {@code finally} block, matching Cobalt's project-wide
     *           "always ACK" stanza-handler convention so the server does not
     *           retransmit the notification on transient client errors.
     */
    @Override
    public void handle(Node node) {
        if (!node.hasDescription("notification") || !node.hasAttribute("type", "privacy_token")) {
            return;
        }

        try {
            handleNotification(node);
        } catch (Throwable throwable) {
            // WAWebHandlePrivacyTokensNotification.g: LOG("error while parsing: ...", e.toString());
            //                                         ERROR("Parsing Error: ...", t.error.toString())
            LOGGER.log(System.Logger.Level.WARNING,
                    "Cannot handle privacy_token notification {0}: {1}",
                    node.getAttributeAsString("id", "<missing>"),
                    throwable.getMessage());
        } finally {
            // ADAPTED: WAWebHandlePrivacyTokensNotification.g returns the ACK only on success;
            // Cobalt ACKs unconditionally to avoid server-side retransmits after client crashes.
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
     * @implNote WAWebHandlePrivacyTokensNotification.default (parser m + function g body):
     *           {@code yield waitForOfflineDeliveryEnd(); yield Promise.all(privacyTokens.map(...))}
     */
    private void handleNotification(Node node) {
        // WAWebHandlePrivacyTokensNotification: m.parse - attrUserJid("from")
        var senderPn = getUserJid(node, "from");
        // WAWebHandlePrivacyTokensNotification: m.parse - maybeAttrLidUserJid("sender_lid")
        var senderLid = node.getAttributeAsJid("sender_lid")
                .map(Jid::toUserJid)
                .orElse(null);
        if (senderPn == null) {
            return;
        }

        // WAWebHandlePrivacyTokensNotification: m.parse - t.child("tokens")
        var tokensNode = node.getChild("tokens").orElse(null);
        if (tokensNode == null) {
            return;
        }

        // WAWebHandlePrivacyTokensNotification.g: yield waitForOfflineDeliveryEnd()
        // (guarded by !WAWebEnvironment.isGuest upstream; Cobalt is never a guest client)
        whatsapp.store().waitForOfflineDeliveryEnd();

        // WAWebHandlePrivacyTokensNotification: m.parse - i.forEachChildWithTag("token", ...)
        // ADAPTED: WAWebHandlePrivacyTokensNotification.g maps each token via Promise.all;
        // on a virtual thread we just iterate and block inline.
        for (var tokenNode : tokensNode.getChildren("token")) {
            // WAWebHandlePrivacyTokensNotification: m.parse - t.attrString("type")
            var type = tokenNode.getAttributeAsString("type", "");
            switch (type) {
                case "trusted_contact" -> handleTrustedContactToken(senderPn, senderLid, tokenNode);
                // WAWebHandlePrivacyTokensNotification.m: LOG("receiving an unknown type: ...", n)
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
     * @implNote WAWebHandlePrivacyTokensNotification._ (function p/_ body) +
     *           WAWebSetTcTokenChatAction.handleIncomingTcToken
     */
    private void handleTrustedContactToken(Jid senderPn, Jid senderLid, Node tokenNode) {
        // WAWebSetTcTokenChatAction.handleIncomingTcToken: if (t.isRegularUser()) { ... }
        // Tokens for PSAs / bots / non-user servers are silently dropped by WA Web.
        if (!LidMigrationService.isRegularUser(senderPn)) { // WAWebWid.isRegularUser - shared helper
            return;
        }

        // WAWebHandlePrivacyTokensNotification: d(t) - t.contentBytes()
        var content = tokenNode.toContentBytes().orElse(null);
        // WAWebSetTcTokenChatAction.handleIncomingTcToken: if (... && a != null)
        if (content == null || content.length == 0) {
            return;
        }

        // WAWebHandlePrivacyTokensNotification: d(t) - t.attrTime("t")
        var tokenTimestamp = getInstantAttribute(tokenNode, "t");

        // WAWebSetTcTokenChatAction.handleIncomingTcToken: update chat tc token
        updateChatTcToken(senderPn, senderLid, tokenTimestamp, content);

        // WAWebHandlePrivacyTokensNotification._: yield reSubscribeWhenActive(r)
        // where r = userJidToUserWid(e.from), i.e. the sender's PN.
        try {
            whatsapp.subscribeToPresence(senderPn); // WAWebPresenceCollection.reSubscribeWhenActive -> WAWebContactPresenceBridge.subscribePresence
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
     * @implNote WAWebSetTcTokenChatAction.handleIncomingTcToken: in WA Web the
     *           lookup is {@code getChatByAccountLid(n ?? toLid(t))} followed
     *           by {@code getExisting(t)}, and a missing chat defers to
     *           {@code WAWebApiOrphanTcToken.createOrUpdateOrphanTcToken}.
     *           Cobalt has no orphan-tc-token store, so the missing-chat branch
     *           is ADAPTED to {@code addNewChat(senderPn)}. The skip condition
     *           ({@code tcToken equals a || tcTokenTimestamp > r}) is preserved
     *           via {@link Instant#isAfter(Instant)}, which models the JS
     *           {@code >} operator on numeric timestamps.
     */
    private void updateChatTcToken(Jid senderPn, Jid senderLid, Instant tokenTimestamp, byte[] tcTokenContent) {
        // WAWebSetTcTokenChatAction.handleIncomingTcToken: getChatByAccountLid(n ?? toLid(t))
        // ADAPTED: when senderLid is absent WA Web computes it from senderPn via toLid;
        // Cobalt cannot reach LidMigrationService from this handler, so we fall back to PN
        // (the most common real-world shape, since sender_lid is typically present).
        var chat = (senderLid != null
                ? whatsapp.store().findChatByJid(senderLid).orElse(null)
                : null);
        if (chat == null) {
            // WAWebSetTcTokenChatAction.handleIncomingTcToken: fall back through getExisting(t)
            // ADAPTED: getExisting is a bridge helper; Cobalt collapses it to findChatByJid(pn).
            chat = whatsapp.store().findChatByJid(senderPn)
                    .orElseGet(() -> whatsapp.store().addNewChat(senderPn));
        }

        // WAWebSetTcTokenChatAction.handleIncomingTcToken:
        // if (!(l.tcToken != null && arrayBuffersEqualUNSAFE(l.tcToken, a)
        //      || l.tcTokenTimestamp != null && l.tcTokenTimestamp > r))
        var existingToken = chat.tcToken().orElse(null);
        var existingTimestamp = chat.tcTokenTimestamp().orElse(null);
        if (existingToken != null && Arrays.equals(existingToken, tcTokenContent)
                || existingTimestamp != null && tokenTimestamp != null && existingTimestamp.isAfter(tokenTimestamp)) {
            return;
        }

        // WAWebSetTcTokenChatAction.handleIncomingTcToken: l.set({tcToken: a, tcTokenTimestamp: r})
        chat.setTcToken(tcTokenContent);
        chat.setTcTokenTimestamp(tokenTimestamp);
        // NO_WA_BASIS: WA Web also merges into WAWebSchemaChat.getChatTable() and optionally
        // kicks ProfilePicThumbCollection.find when the profile-pic IQ privacy-token AB prop is on.
        // Cobalt has neither the chat-table IDB write nor the profile-pic-thumb collection.
    }

    /**
     * Extracts a user JID from a node attribute.
     *
     * @param node the node to extract from
     * @param key  the attribute key
     * @return the user JID, or {@code null} if not present
     * @implNote WAWebHandlePrivacyTokensNotification (parser m: attrUserJid)
     */
    private Jid getUserJid(Node node, String key) {
        return node.getAttributeAsJid(key)
                .map(Jid::toUserJid)
                .orElse(null);
    }

    /**
     * Extracts an {@link Instant} from a node attribute representing epoch
     * seconds.
     *
     * @param node the node to extract from
     * @param key  the attribute key
     * @return the parsed instant, or {@code null} if absent or non-positive
     * @implNote WAWebHandlePrivacyTokensNotification.d (attrTime("t"))
     */
    private Instant getInstantAttribute(Node node, String key) {
        var seconds = node.getAttributeAsLong(key, (Long) null);
        return seconds == null || seconds <= 0 ? null : Instant.ofEpochSecond(seconds);
    }

    /**
     * Sends an acknowledgement stanza for the processed notification.
     *
     * <p>The ACK uses the notification's ID and sender JID, with
     * {@code class="notification"} and {@code type="privacy_token"}.
     *
     * @param node the original notification node
     * @implNote WAWebHandlePrivacyTokensNotification.default (ACK stanza construction in g)
     */
    private void sendNotificationAck(Node node) {
        // WAWebHandlePrivacyTokensNotification.default: wap("ack", {id, class, to, type})
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
