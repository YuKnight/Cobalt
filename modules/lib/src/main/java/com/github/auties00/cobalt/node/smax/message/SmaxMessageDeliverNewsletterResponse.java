package com.github.auties00.cobalt.node.smax.message;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The inbound projection of the
 * {@code <message type="media" from=NEWSLETTER_JID id server_id t
 * is_sender?>...</message>} stanza.
 *
 * @implNote {@code WASmaxInMessageDeliverNewsletterRequest.parseNewsletterRequest}
 *           composes
 *           {@code WASmaxInMessageDeliverNewsletterMessageWithJIDMixin}
 *           (envelope: {@code from=newsletterJid}, fanout id /
 *           server_id / t / is_sender, optional original-msg-t /
 *           msg-edit-t / admin-profile / paid-partnership-content
 *           markers, the disjunctive newsletter-fanout-content
 *           projection) with
 *           {@code WASmaxInMessageDeliverReceiverContentTypeMediaRCATMixin}
 *           (mandatory {@code type="media"}, mandatory
 *           {@code <plaintext mediatype="url"/>} child, mandatory
 *           {@code <rcat/>} child carrying raw bytes). Cobalt keeps
 *           the variable-shape fanout-content payload accessible as
 *           the raw {@link Node} since each of the 14 disjunctive
 *           variants ships a distinct child schema.
 */
@WhatsAppWebModule(moduleName = "WASmaxInMessageDeliverNewsletterRequest")
@WhatsAppWebModule(moduleName = "WASmaxInMessageDeliverNewsletterMessageWithJIDMixin")
@WhatsAppWebModule(moduleName = "WASmaxInMessageDeliverNewsletterMessageFanoutMixin")
@WhatsAppWebModule(moduleName = "WASmaxInMessageDeliverReceiverContentTypeMediaRCATMixin")
public final class SmaxMessageDeliverNewsletterResponse implements SmaxOperation.Response {
    /**
     * The newsletter JID the message was published to.
     */
    private final Jid newsletterJid;

    /**
     * The stanza id assigned by the relay.
     */
    private final String stanzaId;

    /**
     * The server-assigned message id.
     */
    private final long serverId;

    /**
     * The unix-second timestamp of the message.
     */
    private final long timestamp;

    /**
     * Whether the {@code is_sender="true"} attribute was present.
     * i.e. the connected client authored the post.
     */
    private final boolean fromSelf;

    /**
     * The optional original-message timestamp from the
     * {@code <meta original_msg_t/>} child, when the post is an edit.
     */
    private final Long metaOriginalMsgT;

    /**
     * The optional last-edit timestamp from the
     * {@code <meta msg_edit_t/>} child.
     */
    private final Long metaMsgEditT;

    /**
     * The optional admin-profile metadata projection from the
     * {@code <meta><admin_profile/></meta>} child.
     */
    private final Node adminProfileMeta;

    /**
     * Whether the {@code <meta><paid_partnership/></meta>} marker
     * child was present.
     */
    private final boolean hasPaidPartnership;

    /**
     * The optional offline counter from the {@code offline} attribute.
     */
    private final Integer offline;

    /**
     * The fanout-content variant name. One of {@code "NewsletterText"},
     * {@code "NewsletterMedia"}, {@code "NewsletterReaction"},
     * {@code "NewsletterReactionRevoke"}, {@code "NewsletterEdit"},
     * {@code "NewsletterRevoke"}, {@code "NewsletterPollCreation"},
     * {@code "NewsletterQuizCreation"}, {@code "NewsletterPollVote"},
     * {@code "NewsletterPollResultSnapshot"},
     * {@code "NewsletterQuestion"},
     * {@code "NewsletterQuestionResponse"},
     * {@code "NewsletterQuestionReply"},
     * {@code "NewsletterWAMOEmpty"}.
     */
    private final String fanoutContentName;

    /**
     * The {@code mediatype} on the {@code <plaintext/>} child.
     * always {@code "url"} per the relay schema.
     */
    private final String plaintextMediatype;

    /**
     * The raw bytes carried by the {@code <rcat/>} child.
     */
    private final byte[] rcatBytes;

    /**
     * The raw underlying {@code <message/>} {@link Node}. Exposed so
     * callers can project the variable-shape fanout-content children
     * (text body, media descriptor, poll metadata, etc.) without
     * Cobalt having to model 14 distinct payload variants here.
     */
    private final Node raw;

    /**
     * Constructs a new inbound projection.
     *
     * @param newsletterJid      the source newsletter JID; never
     *                           {@code null}
     * @param stanzaId           the relay-assigned stanza id; never
     *                           {@code null}
     * @param serverId           the server-assigned message id
     * @param timestamp          the unix-second timestamp
     * @param fromSelf           whether the connected client authored
     *                           the post
     * @param metaOriginalMsgT   the optional original timestamp; may
     *                           be {@code null}
     * @param metaMsgEditT       the optional edit timestamp; may be
     *                           {@code null}
     * @param adminProfileMeta   the optional admin-profile child node;
     *                           may be {@code null}
     * @param hasPaidPartnership whether the paid-partnership marker
     *                           was present
     * @param offline            the optional offline counter; may be
     *                           {@code null}
     * @param fanoutContentName  the fanout-content variant name; never
     *                           {@code null}
     * @param plaintextMediatype the {@code <plaintext/>} mediatype;
     *                           never {@code null}
     * @param rcatBytes          the raw {@code <rcat/>} content bytes;
     *                           never {@code null}
     * @param raw                the underlying {@code <message/>}
     *                           node; never {@code null}
     * @throws NullPointerException if any non-nullable argument is
     *                              {@code null}
     */
    public SmaxMessageDeliverNewsletterResponse(Jid newsletterJid,
                   String stanzaId,
                   long serverId,
                   long timestamp,
                   boolean fromSelf,
                   Long metaOriginalMsgT,
                   Long metaMsgEditT,
                   Node adminProfileMeta,
                   boolean hasPaidPartnership,
                   Integer offline,
                   String fanoutContentName,
                   String plaintextMediatype,
                   byte[] rcatBytes,
                   Node raw) {
        this.newsletterJid = Objects.requireNonNull(newsletterJid, "newsletterJid cannot be null");
        this.stanzaId = Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
        this.serverId = serverId;
        this.timestamp = timestamp;
        this.fromSelf = fromSelf;
        this.metaOriginalMsgT = metaOriginalMsgT;
        this.metaMsgEditT = metaMsgEditT;
        this.adminProfileMeta = adminProfileMeta;
        this.hasPaidPartnership = hasPaidPartnership;
        this.offline = offline;
        this.fanoutContentName = Objects.requireNonNull(fanoutContentName, "fanoutContentName cannot be null");
        this.plaintextMediatype = Objects.requireNonNull(plaintextMediatype, "plaintextMediatype cannot be null");
        this.rcatBytes = Objects.requireNonNull(rcatBytes, "rcatBytes cannot be null");
        this.raw = Objects.requireNonNull(raw, "raw cannot be null");
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
     * Returns the relay-assigned stanza id.
     *
     * @return the id; never {@code null}
     */
    public String stanzaId() {
        return stanzaId;
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
     * Returns the unix-second timestamp.
     *
     * @return the timestamp
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Returns whether the connected client authored the post.
     *
     * @return {@code true} when {@code is_sender="true"} was set
     */
    public boolean fromSelf() {
        return fromSelf;
    }

    /**
     * Returns the optional original-message timestamp from the
     * {@code <meta original_msg_t/>} child.
     *
     * @return an {@link Optional} carrying the timestamp, or empty
     *         when the message is not an edit
     */
    public Optional<Long> metaOriginalMsgT() {
        return Optional.ofNullable(metaOriginalMsgT);
    }

    /**
     * Returns the optional last-edit timestamp from the
     * {@code <meta msg_edit_t/>} child.
     *
     * @return an {@link Optional} carrying the timestamp, or empty
     *         when omitted
     */
    public Optional<Long> metaMsgEditT() {
        return Optional.ofNullable(metaMsgEditT);
    }

    /**
     * Returns the optional admin-profile metadata projection.
     *
     * @return an {@link Optional} carrying the
     *         {@code <admin_profile/>} node, or empty when omitted
     */
    public Optional<Node> adminProfileMeta() {
        return Optional.ofNullable(adminProfileMeta);
    }

    /**
     * Returns whether the paid-partnership marker child was present.
     *
     * @return {@code true} when the marker was present
     */
    public boolean hasPaidPartnership() {
        return hasPaidPartnership;
    }

    /**
     * Returns the optional offline counter.
     *
     * @return an {@link Optional} carrying the counter, or empty when
     *         the {@code offline} attribute was absent
     */
    public Optional<Integer> offline() {
        return Optional.ofNullable(offline);
    }

    /**
     * Returns the fanout-content variant name.
     *
     * @return the variant name; never {@code null}
     */
    public String fanoutContentName() {
        return fanoutContentName;
    }

    /**
     * Returns the {@code <plaintext/>} mediatype.
     *
     * @return the mediatype; never {@code null}
     */
    public String plaintextMediatype() {
        return plaintextMediatype;
    }

    /**
     * Returns the raw {@code <rcat/>} content bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] rcatBytes() {
        return rcatBytes;
    }

    /**
     * Returns the underlying {@code <message/>} node for downstream
     * inspection of the variable-shape fanout-content payload.
     *
     * @return the raw node; never {@code null}
     */
    public Node raw() {
        return raw;
    }

    /**
     * Tries to parse an {@link SmaxMessageDeliverNewsletterResponse} projection from the given
     * {@code <message/>} stanza.
     *
     * @param node the inbound message stanza; never {@code null}
     * @return an {@link Optional} carrying the projection, or empty
     *         when the stanza does not match the expected shape
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInMessageDeliverNewsletterRequest",
            exports = "parseNewsletterRequest", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxMessageDeliverNewsletterResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("message")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("type", "media")) {
            return Optional.empty();
        }
        var from = node.getAttributeAsJid("from").orElse(null);
        if (from == null || !from.hasNewsletterServer()) {
            return Optional.empty();
        }
        var stanzaId = node.getAttributeAsString("id").orElse(null);
        if (stanzaId == null) {
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
        if (timestamp < 0) {
            return Optional.empty();
        }
        var fromSelf = node.hasAttribute("is_sender", "true");
        // <meta original_msg_t> / <meta msg_edit_t> / <meta><admin_profile/> / <meta><paid_partnership/>
        var meta = node.getChild("meta").orElse(null);
        Long metaOriginalMsgT = null;
        Long metaMsgEditT = null;
        Node adminProfileMeta = null;
        var hasPaidPartnership = false;
        if (meta != null) {
            var origOpt = meta.getAttributeAsLong("original_msg_t");
            if (origOpt.isPresent()) {
                var ov = origOpt.getAsLong();
                if (ov < 1577865600L || ov > 4102473600L) {
                    return Optional.empty();
                }
                metaOriginalMsgT = ov;
            }
            var editOpt = meta.getAttributeAsLong("msg_edit_t");
            if (editOpt.isPresent()) {
                var ev = editOpt.getAsLong();
                if (ev < 1577865600000L || ev > 4102473600000L) {
                    return Optional.empty();
                }
                metaMsgEditT = ev;
            }
            adminProfileMeta = meta.getChild("admin_profile").orElse(null);
            hasPaidPartnership = meta.hasChild("paid_partnership");
        }
        // offline mixin (optional 0..12)
        Integer offline = null;
        var offlineOpt = node.getAttributeAsInt("offline");
        if (offlineOpt.isPresent()) {
            var ov = offlineOpt.getAsInt();
            if (ov < 0 || ov > 12) {
                return Optional.empty();
            }
            offline = ov;
        }
        // receiver content-type media RCAT mixin
        var plaintext = node.getChild("plaintext").orElse(null);
        if (plaintext == null) {
            return Optional.empty();
        }
        if (!plaintext.hasAttribute("mediatype", "url")) {
            return Optional.empty();
        }
        var rcat = node.getChild("rcat").orElse(null);
        if (rcat == null) {
            return Optional.empty();
        }
        var rcatBytes = rcat.toContentBytes().orElse(null);
        if (rcatBytes == null) {
            return Optional.empty();
        }
        // fanout-content disjunction. Pick the first matching content child as a label.
        var fanoutContentName = classifyFanoutContent(node);
        if (fanoutContentName == null) {
            return Optional.empty();
        }
        return Optional.of(new SmaxMessageDeliverNewsletterResponse(
                from,
                stanzaId,
                serverId,
                timestamp,
                fromSelf,
                metaOriginalMsgT,
                metaMsgEditT,
                adminProfileMeta,
                hasPaidPartnership,
                offline,
                fanoutContentName,
                "url",
                rcatBytes,
                node));
    }

    /**
     * Classifies the fanout-content variant by inspecting the child
     * structure of the message stanza.
     *
     * <p>Mirrors
     * {@code WASmaxInMessageDeliverNewsletterMessageFanoutContent.parseNewsletterMessageFanoutContent}'s
     * disjunctive try-cascade. We walk the same priority order
     * (question → question-response → edit → question-reply → revoke
     * → text → media → reaction → reaction-revoke → poll-creation →
     * quiz-creation → poll-vote → poll-result-snapshot → wamo-empty)
     * and return the matching variant label.
     *
     * @param node the message stanza; never {@code null}
     * @return the variant name, or {@code null} when no variant
     *         matched
     */
    @WhatsAppWebExport(moduleName = "WASmaxInMessageDeliverNewsletterMessageFanoutContent",
            exports = "parseNewsletterMessageFanoutContent",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static String classifyFanoutContent(Node node) {
        // Disjunction is structural. Cobalt collapses to a label-only classifier
        // since downstream code re-walks the raw node for content extraction.
        if (node.hasChild("question")) {
            return "NewsletterQuestion";
        }
        if (node.hasChild("question_response")) {
            return "NewsletterQuestionResponse";
        }
        if (node.hasChild("edit")) {
            return "NewsletterEdit";
        }
        if (node.hasChild("question_reply")) {
            return "NewsletterQuestionReply";
        }
        if (node.hasChild("revoke")) {
            return "NewsletterRevoke";
        }
        if (node.hasChild("reaction")) {
            if (node.getChild("reaction")
                    .map(c -> c.hasAttribute("operation", "revoke"))
                    .orElse(false)) {
                return "NewsletterReactionRevoke";
            }
            return "NewsletterReaction";
        }
        if (node.hasChild("poll_creation")) {
            return "NewsletterPollCreation";
        }
        if (node.hasChild("quiz_creation")) {
            return "NewsletterQuizCreation";
        }
        if (node.hasChild("poll_vote")) {
            return "NewsletterPollVote";
        }
        if (node.hasChild("poll_result_snapshot")) {
            return "NewsletterPollResultSnapshot";
        }
        if (node.hasChild("wamo")) {
            return "NewsletterWAMOEmpty";
        }
        // Plaintext + rcat with no other child: text/media variant. Distinguished
        // by the inner protobuf shape that downstream code parses from <plaintext/>.
        return "NewsletterText";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMessageDeliverNewsletterResponse) obj;
        return this.serverId == that.serverId
                && this.timestamp == that.timestamp
                && this.fromSelf == that.fromSelf
                && this.hasPaidPartnership == that.hasPaidPartnership
                && Objects.equals(this.newsletterJid, that.newsletterJid)
                && Objects.equals(this.stanzaId, that.stanzaId)
                && Objects.equals(this.metaOriginalMsgT, that.metaOriginalMsgT)
                && Objects.equals(this.metaMsgEditT, that.metaMsgEditT)
                && Objects.equals(this.adminProfileMeta, that.adminProfileMeta)
                && Objects.equals(this.offline, that.offline)
                && Objects.equals(this.fanoutContentName, that.fanoutContentName)
                && Objects.equals(this.plaintextMediatype, that.plaintextMediatype)
                && Arrays.equals(this.rcatBytes, that.rcatBytes)
                && Objects.equals(this.raw, that.raw);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(newsletterJid, stanzaId, serverId, timestamp, fromSelf,
                metaOriginalMsgT, metaMsgEditT, adminProfileMeta, hasPaidPartnership, offline,
                fanoutContentName, plaintextMediatype, raw);
        result = 31 * result + Arrays.hashCode(rcatBytes);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxMessageDeliverNewsletterResponse[newsletterJid=" + newsletterJid
                + ", stanzaId=" + stanzaId
                + ", serverId=" + serverId
                + ", timestamp=" + timestamp
                + ", fromSelf=" + fromSelf
                + ", metaOriginalMsgT=" + metaOriginalMsgT
                + ", metaMsgEditT=" + metaMsgEditT
                + ", hasPaidPartnership=" + hasPaidPartnership
                + ", offline=" + offline
                + ", fanoutContentName=" + fanoutContentName
                + ", plaintextMediatype=" + plaintextMediatype + ']';
    }
}
