package com.github.auties00.cobalt.node.smax.newsletters;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The inbound notification — the relay's "your newsletter has new
 * messages" hint carrying the affected newsletter JID, optional
 * timestamp, and the affected {@code <message>} entries.
 *
 * @implNote {@code WASmaxInNewslettersLiveUpdatesNotificationRequest.parseLiveUpdatesNotificationRequest}
 *           validates the {@code <notification type="newsletter"
 *           from=NEWSLETTER_JID>} envelope, extracts the
 *           {@code <live_updates><messages>} child, and projects every
 *           {@code <message>} grandchild via
 *           {@code parseNewsletterMessageHistoryWithAddOnsMixin}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInNewslettersLiveUpdatesNotificationRequest")
@WhatsAppWebModule(moduleName = "WASmaxInNewslettersCommonNotificationMixin")
@WhatsAppWebModule(moduleName = "WASmaxInNewslettersNewsletterMessageResponsePayloadMixin")
public final class SmaxNewslettersLiveUpdatesNotificationResponse implements SmaxOperation.Response {
    /**
     * The notification stanza id — echoed verbatim into the
     * {@link SmaxNewslettersLiveUpdatesNotificationAcknowledgement}.
     */
    private final String notificationId;

    /**
     * The newsletter JID that produced the notification.
     */
    private final Jid notificationFrom;

    /**
     * The optional newsletter JID echoed on the {@code <messages>}
     * payload.
     */
    private final Jid messagesJid;

    /**
     * The optional unix-second timestamp echoed on the
     * {@code <messages>} payload.
     */
    private final Long messagesTimestamp;

    /**
     * The list of newsletter messages carried in the live-updates
     * delta.
     */
    private final List<NewsletterMessage> messages;

    /**
     * Constructs a new inbound projection.
     *
     * @param notificationId    the notification id; never {@code null}
     * @param notificationFrom  the notification sender JID; never
     *                          {@code null}
     * @param messagesJid       the optional echoed JID; may be
     *                          {@code null}
     * @param messagesTimestamp the optional echoed timestamp; may be
     *                          {@code null}
     * @param messages          the message entries; never {@code null}
     * @throws NullPointerException if any non-optional argument is
     *                              {@code null}
     */
    public SmaxNewslettersLiveUpdatesNotificationResponse(String notificationId, Jid notificationFrom,
                   Jid messagesJid, Long messagesTimestamp,
                   List<NewsletterMessage> messages) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
        this.messagesJid = messagesJid;
        this.messagesTimestamp = messagesTimestamp;
        this.messages = List.copyOf(Objects.requireNonNullElse(messages, List.of()));
    }

    /**
     * Returns the notification id.
     *
     * @return the id; never {@code null}
     */
    public String notificationId() {
        return notificationId;
    }

    /**
     * Returns the notification sender JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid notificationFrom() {
        return notificationFrom;
    }

    /**
     * Returns the optional echoed JID on the {@code <messages>}
     * payload.
     *
     * @return an {@link Optional} carrying the JID, or empty when
     *         omitted
     */
    public Optional<Jid> messagesJid() {
        return Optional.ofNullable(messagesJid);
    }

    /**
     * Returns the optional echoed timestamp.
     *
     * @return an {@link Optional} carrying the timestamp, or empty
     *         when omitted
     */
    public Optional<Long> messagesTimestamp() {
        return Optional.ofNullable(messagesTimestamp);
    }

    /**
     * Returns the message entries.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<NewsletterMessage> messages() {
        return messages;
    }

    /**
     * Tries to parse an {@link SmaxNewslettersLiveUpdatesNotificationResponse} projection from the given
     * {@code <notification/>} stanza.
     *
     * @param node the inbound notification stanza; never {@code null}
     * @return an {@link Optional} carrying the projection, or empty
     *         when the stanza does not match the expected shape
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInNewslettersLiveUpdatesNotificationRequest",
            exports = "parseLiveUpdatesNotificationRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxNewslettersLiveUpdatesNotificationResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("notification")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("type", "newsletter")) {
            return Optional.empty();
        }
        var notificationId = node.getAttributeAsString("id").orElse(null);
        if (notificationId == null) {
            return Optional.empty();
        }
        var from = node.getAttributeAsJid("from").orElse(null);
        if (from == null) {
            return Optional.empty();
        }
        var liveUpdates = node.getChild("live_updates").orElse(null);
        if (liveUpdates == null) {
            return Optional.empty();
        }
        var messagesNode = liveUpdates.getChild("messages").orElse(null);
        if (messagesNode == null) {
            return Optional.empty();
        }
        var messagesJid = messagesNode.getAttributeAsJid("jid").orElse(null);
        Long timestamp = null;
        var tOpt = messagesNode.getAttributeAsLong("t");
        if (tOpt.isPresent()) {
            var tv = tOpt.getAsLong();
            if (tv < 0) {
                return Optional.empty();
            }
            timestamp = tv;
        }
        var entries = new ArrayList<NewsletterMessage>();
        for (var messageNode : messagesNode.getChildren("message")) {
            var entry = NewsletterMessage.of(messageNode).orElse(null);
            if (entry == null) {
                return Optional.empty();
            }
            entries.add(entry);
        }
        return Optional.of(new SmaxNewslettersLiveUpdatesNotificationResponse(notificationId, from, messagesJid, timestamp, entries));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxNewslettersLiveUpdatesNotificationResponse) obj;
        return Objects.equals(this.notificationId, that.notificationId)
                && Objects.equals(this.notificationFrom, that.notificationFrom)
                && Objects.equals(this.messagesJid, that.messagesJid)
                && Objects.equals(this.messagesTimestamp, that.messagesTimestamp)
                && Objects.equals(this.messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, notificationFrom, messagesJid, messagesTimestamp, messages);
    }

    @Override
    public String toString() {
        return "SmaxNewslettersLiveUpdatesNotificationResponse[notificationId=" + notificationId
                + ", notificationFrom=" + notificationFrom
                + ", messagesJid=" + messagesJid
                + ", messagesTimestamp=" + messagesTimestamp
                + ", messages=" + messages + ']';
    }

    /**
     * One newsletter message entry carried in the live-updates
     * delta.
     *
     * @implNote {@code WASmaxInNewslettersNewsletterMessageHistoryWithAddOnsMixin.parseNewsletterMessageHistoryWithAddOnsMixin}
     *           composes
     *           {@code parseNewsletterMessageHistoryMixin} (id /
     *           server_id / t / is_sender / optional history-content)
     *           with the optional add-on mixins (reactions,
     *           poll-votes, responses count, forwards count, views).
     *           Cobalt projects the top-level scalars and exposes the
     *           raw {@link Node} for downstream add-on parsing.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersNewsletterMessageHistoryWithAddOnsMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersNewsletterMessageHistoryMixin")
    public static final class NewsletterMessage {
        /**
         * The optional client-supplied stanza id of the original
         * message.
         */
        private final String stanzaId;

        /**
         * The server-assigned monotonic message id within the
         * newsletter.
         */
        private final long serverId;

        /**
         * The optional unix-second timestamp of the message.
         */
        private final Long timestamp;

        /**
         * Whether the message was authored by the connected client.
         */
        private final boolean fromSelf;

        /**
         * The raw underlying {@link Node} — exposed so callers can
         * project the variable-shape add-on children.
         */
        private final Node raw;

        /**
         * Constructs a new newsletter message entry.
         *
         * @param stanzaId  the optional stanza id; may be {@code null}
         * @param serverId  the server-assigned id
         * @param timestamp the optional unix-second timestamp; may be
         *                  {@code null}
         * @param fromSelf  whether the message was authored by self
         * @param raw       the underlying {@link Node}; never
         *                  {@code null}
         * @throws NullPointerException if {@code raw} is {@code null}
         */
        public NewsletterMessage(String stanzaId, long serverId, Long timestamp, boolean fromSelf, Node raw) {
            this.stanzaId = stanzaId;
            this.serverId = serverId;
            this.timestamp = timestamp;
            this.fromSelf = fromSelf;
            this.raw = Objects.requireNonNull(raw, "raw cannot be null");
        }

        /**
         * Returns the optional stanza id.
         *
         * @return an {@link Optional} carrying the stanza id, or
         *         empty when omitted
         */
        public Optional<String> stanzaId() {
            return Optional.ofNullable(stanzaId);
        }

        /**
         * Returns the server-assigned message id.
         *
         * @return the server id
         */
        public long serverId() {
            return serverId;
        }

        /**
         * Returns the optional unix-second timestamp.
         *
         * @return an {@link Optional} carrying the timestamp, or
         *         empty when omitted
         */
        public Optional<Long> timestamp() {
            return Optional.ofNullable(timestamp);
        }

        /**
         * Returns whether the message was authored by self.
         *
         * @return {@code true} when {@code is_sender="true"} was
         *         present
         */
        public boolean fromSelf() {
            return fromSelf;
        }

        /**
         * Returns the underlying {@link Node} for downstream
         * inspection of the add-on children.
         *
         * @return the raw node; never {@code null}
         */
        public Node raw() {
            return raw;
        }

        /**
         * Tries to parse a {@link NewsletterMessage} from a
         * {@code <message>} node.
         *
         * @param messageNode the source node; never {@code null}
         * @return an {@link Optional} carrying the parsed entry, or
         *         empty when the node does not match the schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersNewsletterMessageHistoryWithAddOnsMixin",
                exports = "parseNewsletterMessageHistoryWithAddOnsMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<NewsletterMessage> of(Node messageNode) {
            Objects.requireNonNull(messageNode, "messageNode cannot be null");
            if (!messageNode.hasDescription("message")) {
                return Optional.empty();
            }
            var stanzaId = messageNode.getAttributeAsString("id").orElse(null);
            var serverIdOpt = messageNode.getAttributeAsLong("server_id");
            if (serverIdOpt.isEmpty()) {
                return Optional.empty();
            }
            var serverId = serverIdOpt.getAsLong();
            if (serverId < 99 || serverId > 2147476647L) {
                return Optional.empty();
            }
            Long timestamp = null;
            var tOpt = messageNode.getAttributeAsLong("t");
            if (tOpt.isPresent()) {
                var tv = tOpt.getAsLong();
                if (tv < 0) {
                    return Optional.empty();
                }
                timestamp = tv;
            }
            var fromSelf = messageNode.hasAttribute("is_sender", "true");
            return Optional.of(new NewsletterMessage(stanzaId, serverId, timestamp, fromSelf, messageNode));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (NewsletterMessage) obj;
            return this.serverId == that.serverId
                    && this.fromSelf == that.fromSelf
                    && Objects.equals(this.stanzaId, that.stanzaId)
                    && Objects.equals(this.timestamp, that.timestamp)
                    && Objects.equals(this.raw, that.raw);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stanzaId, serverId, timestamp, fromSelf, raw);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersLiveUpdatesNotificationResponse.NewsletterMessage[stanzaId=" + stanzaId
                    + ", serverId=" + serverId
                    + ", timestamp=" + timestamp
                    + ", fromSelf=" + fromSelf + ']';
        }
    }
}
