package com.github.auties00.cobalt.node.smax.pings;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;

import java.util.Objects;
import java.util.Optional;

/**
 * Inbound {@code <iq type="result">} reply produced by the relay in response
 * to a SMAX client ping ({@code WASmaxOutPingsClientRequest.makeClientRequest},
 * an {@code <iq xmlns="w:p" type="get">} stanza emitted by the keep-alive
 * pump).
 *
 * <p>The reply carries no body — only the standard envelope attributes:
 * {@code from}, {@code type="result"}, an {@code id} that echoes the request,
 * and a server-stamped {@code t} timestamp. Cobalt models it as a single
 * record-style projection because WA Web's parser produces only one shape
 * (success-only, no error variants).
 */
@WhatsAppWebModule(moduleName = "WASmaxInPingsClientResponseServerResponse")
@WhatsAppWebModule(moduleName = "WASmaxInPingsEnums")
public final class SmaxPingsClientResponseServerResponse implements SmaxOperation.Response {
    /**
     * The relay JID that produced the reply. WA Web validates this against
     * {@code DOMAINJID_USERJID} — either a server-only domain JID
     * ({@code s.whatsapp.net} / {@code g.us} / {@code call}) or any standard
     * user-server JID ({@code s.whatsapp.net} / {@code c.us}).
     */
    private final Jid from;

    /**
     * The literal {@code "result"} type tag. Always equal to {@code "result"}
     * when the parser succeeded.
     */
    private final String type;

    /**
     * The relay-stamped server timestamp, in seconds since the Unix epoch.
     */
    private final long timestamp;

    /**
     * Constructs a new server-response projection.
     *
     * @param from      the relay JID. Never {@code null}
     * @param type      the literal {@code "result"} type tag. Never
     *                  {@code null}
     * @param timestamp the relay-stamped server timestamp, in seconds
     * @throws NullPointerException if either {@code from} or {@code type}
     *                              is {@code null}
     */
    public SmaxPingsClientResponseServerResponse(Jid from, String type, long timestamp) {
        this.from = Objects.requireNonNull(from, "from cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.timestamp = timestamp;
    }

    /**
     * Returns the relay JID.
     *
     * @return the relay JID. Never {@code null}
     */
    public Jid from() {
        return from;
    }

    /**
     * Returns the literal {@code "result"} type tag.
     *
     * @return the type tag. Never {@code null}
     */
    public String type() {
        return type;
    }

    /**
     * Returns the relay-stamped server timestamp.
     *
     * @return the timestamp, in seconds since the Unix epoch
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Tries to parse a {@link SmaxPingsClientResponseServerResponse} from
     * the given inbound stanza, cross-checked against the original
     * outbound request.
     *
     * <p>Returns {@link Optional#empty()} when any of the WA Web parser's
     * preconditions fail: the reply must be an {@code <iq>}, must carry a
     * {@code from} JID that satisfies the {@code DOMAINJID_USERJID}
     * predicate, must have {@code type="result"}, must echo the request's
     * {@code id} attribute, and must carry a non-negative integer
     * {@code t} attribute.
     *
     * @param node    the inbound IQ stanza. Never {@code null}
     * @param request the original outbound request. Used to cross-check
     *                the echoed {@code id}. Never {@code null}
     * @return an {@link Optional} carrying the parsed projection, or
     *         {@link Optional#empty()} when the stanza did not satisfy
     *         the parser's preconditions
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInPingsClientResponseServerResponse",
            exports = "parseClientResponseServerResponse",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxPingsClientResponseServerResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        // WASmaxParseUtils.assertTag(reply, "iq")
        if (!node.hasDescription("iq")) {
            return Optional.empty();
        }
        // WASmaxParseJid.attrJidEnum(reply, "from", DOMAINJID_USERJID)
        var from = node.getAttributeAsJid("from").orElse(null);
        if (from == null) {
            return Optional.empty();
        }
        if (!isDomainOrUserJid(from)) {
            return Optional.empty();
        }
        // WASmaxParseUtils.literal(attrString, reply, "type", "result")
        if (!node.hasAttribute("type", "result")) {
            return Optional.empty();
        }
        // WASmaxParseReference.attrStringFromReference(request, ["id"])
        var requestId = request.getAttributeAsString("id").orElse(null);
        if (requestId == null) {
            return Optional.empty();
        }
        // WASmaxParseUtils.literal(attrString, reply, "id", requestId)
        if (!node.hasAttribute("id", requestId)) {
            return Optional.empty();
        }
        // WASmaxParseUtils.attrInt(reply, "t")
        var timestamp = node.getAttributeAsLong("t");
        if (timestamp.isEmpty()) {
            return Optional.empty();
        }
        // WAResultOrError.makeResult({from, type, t})
        return Optional.of(new SmaxPingsClientResponseServerResponse(from, "result", timestamp.getAsLong()));
    }

    /**
     * Returns whether the given JID matches the
     * {@code WASmaxInPingsEnums.DOMAINJID_USERJID} descriptor — accepted
     * when it is either a server-only JID for the bare WhatsApp / group /
     * call domains or a regular user-server JID.
     *
     * @param jid the JID to validate. Never {@code null}
     * @return {@code true} when the JID satisfies the
     *         {@code DOMAINJID_USERJID} predicate; {@code false}
     *         otherwise
     */
    @WhatsAppWebExport(moduleName = "WASmaxInPingsEnums",
            exports = "DOMAINJID_USERJID",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean isDomainOrUserJid(Jid jid) {
        // WAJids.validateDomainJid: accepts s.whatsapp.net | g.us | call as bare server JIDs
        if (jid.isServerJid(JidServer.user())
                || jid.isServerJid(JidServer.groupOrCommunity())
                || jid.isServerJid(JidServer.call())) {
            return true;
        }
        // WAJids.validateUserJid: accepts any standard user-domain JID (s.whatsapp.net | c.us)
        return jid.hasUserServer();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxPingsClientResponseServerResponse) obj;
        return this.timestamp == that.timestamp
                && Objects.equals(this.from, that.from)
                && Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, type, timestamp);
    }

    @Override
    public String toString() {
        return "SmaxPingsClientResponseServerResponse[from=" + from
                + ", type=" + type
                + ", timestamp=" + timestamp + ']';
    }
}
