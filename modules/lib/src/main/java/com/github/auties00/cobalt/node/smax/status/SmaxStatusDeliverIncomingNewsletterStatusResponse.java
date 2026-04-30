package com.github.auties00.cobalt.node.smax.status;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The inbound projection of the
 * {@code <status from=NEWSLETTER_JID id server_id t
 * is_sender?>...</status>} stanza.
 */
@WhatsAppWebModule(moduleName = "WASmaxInStatusDeliverIncomingNewsletterStatusRequest")
@WhatsAppWebModule(moduleName = "WASmaxInStatusDeliverFromNewsletterMixin")
@WhatsAppWebModule(moduleName = "WASmaxInStatusDeliverStatusNewsletterContentMixin")
@WhatsAppWebModule(moduleName = "WASmaxInStatusDeliverOfflineMixin")
public final class SmaxStatusDeliverIncomingNewsletterStatusResponse implements SmaxOperation.Response {
    /**
     * The relay-assigned stanza id.
     */
    private final String stanzaId;

    /**
     * The newsletter JID the status was published from.
     */
    private final Jid newsletterJid;

    /**
     * The server-assigned status-message id.
     */
    private final long serverId;

    /**
     * The unix-second timestamp of the status post.
     */
    private final long timestamp;

    /**
     * Whether the {@code is_sender="true"} attribute was present.
     * i.e. the connected client authored the status post.
     */
    private final boolean fromSelf;

    /**
     * The newsletter-status content-type variant name. E.g.
     * {@code "newsletter_reaction"},
     * {@code "newsletter_reaction_revoke"}. Derived from the first
     * matching content child.
     */
    private final String contentTypeName;

    /**
     * The optional offline counter from the {@code offline}
     * attribute (0..12).
     */
    private final Integer offline;

    /**
     * The raw underlying {@code <status/>} {@link Node}. Exposed so
     * callers can project the variable-shape content children
     * (reaction emoji, target server-id, etc.) without Cobalt
     * needing to model every fanout variant here.
     */
    private final Node raw;

    /**
     * Constructs a new inbound projection.
     *
     * @param stanzaId        the relay-assigned stanza id; never
     *                        {@code null}
     * @param newsletterJid   the source newsletter JID; never
     *                        {@code null}
     * @param serverId        the server-assigned status-message id
     * @param timestamp       the unix-second timestamp
     * @param fromSelf        whether the connected client authored
     *                        the post
     * @param contentTypeName the content-type variant name; never
     *                        {@code null}
     * @param offline         the optional offline counter; may be
     *                        {@code null}
     * @param raw             the underlying status node; never
     *                        {@code null}
     * @throws NullPointerException if any non-nullable argument is
     *                              {@code null}
     */
    public SmaxStatusDeliverIncomingNewsletterStatusResponse(String stanzaId,
                   Jid newsletterJid,
                   long serverId,
                   long timestamp,
                   boolean fromSelf,
                   String contentTypeName,
                   Integer offline,
                   Node raw) {
        this.stanzaId = Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
        this.newsletterJid = Objects.requireNonNull(newsletterJid, "newsletterJid cannot be null");
        this.serverId = serverId;
        this.timestamp = timestamp;
        this.fromSelf = fromSelf;
        this.contentTypeName = Objects.requireNonNull(contentTypeName, "contentTypeName cannot be null");
        this.offline = offline;
        this.raw = Objects.requireNonNull(raw, "raw cannot be null");
    }

    /**
     * Returns the relay-assigned stanza id.
     *
     * @return the id; never {@code null}
     */
    public String stanzaId() {
        return stanzaId;
    }

    /**
     * Returns the source newsletter JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid newsletterJid() {
        return newsletterJid;
    }

    /**
     * Returns the server-assigned status-message id.
     *
     * @return the server id
     */
    public long serverId() {
        return serverId;
    }

    /**
     * Returns the unix-second timestamp.
     *
     * @return the timestamp
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Returns whether the connected client authored the status
     * post.
     *
     * @return {@code true} when {@code is_sender="true"} was set
     */
    public boolean fromSelf() {
        return fromSelf;
    }

    /**
     * Returns the content-type variant name.
     *
     * @return the variant name; never {@code null}
     */
    public String contentTypeName() {
        return contentTypeName;
    }

    /**
     * Returns the optional offline counter.
     *
     * @return an {@link Optional} carrying the counter, or empty
     *         when omitted
     */
    public Optional<Integer> offline() {
        return Optional.ofNullable(offline);
    }

    /**
     * Returns the underlying {@code <status/>} node for downstream
     * inspection of the variable-shape content payload.
     *
     * @return the raw node; never {@code null}
     */
    public Node raw() {
        return raw;
    }

    /**
     * Tries to parse an {@link SmaxStatusDeliverIncomingNewsletterStatusResponse} projection from the given
     * {@code <status/>} stanza.
     *
     * @param node the inbound status stanza; never {@code null}
     * @return an {@link Optional} carrying the projection, or empty
     *         when the stanza does not match the expected shape
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInStatusDeliverIncomingNewsletterStatusRequest",
            exports = "parseIncomingNewsletterStatusRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxStatusDeliverIncomingNewsletterStatusResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("status")) {
            return Optional.empty();
        }
        var stanzaId = node.getAttributeAsString("id").orElse(null);
        if (stanzaId == null) {
            return Optional.empty();
        }
        var from = node.getAttributeAsJid("from").orElse(null);
        if (from == null || !from.hasNewsletterServer()) {
            return Optional.empty();
        }
        var serverIdOpt = node.getAttributeAsLong("server_id");
        if (serverIdOpt.isEmpty()) {
            return Optional.empty();
        }
        var serverId = serverIdOpt.getAsLong();
        if (serverId < 99 || serverId > 2147476647L) {
            return Optional.empty();
        }
        var timestampOpt = node.getAttributeAsLong("t");
        if (timestampOpt.isEmpty()) {
            return Optional.empty();
        }
        var timestamp = timestampOpt.getAsLong();
        if (timestamp < 1577865600L || timestamp > 4102473600L) {
            return Optional.empty();
        }
        var fromSelf = node.hasAttribute("is_sender", "true");
        var contentTypeName = classifyContent(node);
        if (contentTypeName == null) {
            return Optional.empty();
        }
        Integer offline = null;
        var offlineOpt = node.getAttributeAsInt("offline");
        if (offlineOpt.isPresent()) {
            var ov = offlineOpt.getAsInt();
            if (ov < 0 || ov > 12) {
                return Optional.empty();
            }
            offline = ov;
        }
        return Optional.of(new SmaxStatusDeliverIncomingNewsletterStatusResponse(stanzaId, from, serverId, timestamp, fromSelf,
                contentTypeName, offline, node));
    }

    /**
     * Classifies the content-type variant by inspecting the
     * {@code <status/>} child structure.
     *
     * @param node the status stanza; never {@code null}
     * @return the variant name, or {@code null} when no variant
     *         matched
     */
    @WhatsAppWebExport(moduleName = "WASmaxInStatusDeliverNewsletterStatusContentTypeMixins",
            exports = "parseNewsletterStatusContentTypeMixins",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static String classifyContent(Node node) {
        if (node.hasChild("reaction")) {
            if (node.getChild("reaction")
                    .map(c -> c.hasAttribute("operation", "revoke"))
                    .orElse(false)) {
                return "newsletter_reaction_revoke";
            }
            return "newsletter_reaction";
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxStatusDeliverIncomingNewsletterStatusResponse) obj;
        return this.serverId == that.serverId
                && this.timestamp == that.timestamp
                && this.fromSelf == that.fromSelf
                && Objects.equals(this.stanzaId, that.stanzaId)
                && Objects.equals(this.newsletterJid, that.newsletterJid)
                && Objects.equals(this.contentTypeName, that.contentTypeName)
                && Objects.equals(this.offline, that.offline)
                && Objects.equals(this.raw, that.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stanzaId, newsletterJid, serverId, timestamp, fromSelf,
                contentTypeName, offline, raw);
    }

    @Override
    public String toString() {
        return "SmaxStatusDeliverIncomingNewsletterStatusResponse[stanzaId=" + stanzaId
                + ", newsletterJid=" + newsletterJid
                + ", serverId=" + serverId
                + ", timestamp=" + timestamp
                + ", fromSelf=" + fromSelf
                + ", contentTypeName=" + contentTypeName
                + ", offline=" + offline + ']';
    }
}
