package com.github.auties00.cobalt.node.smax.support;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One {@code <message>} child of the {@code <spam_list>} payload —
 * the wire-shape descriptor of a single offending message harvested
 * from the local cache.
 *
 * @implNote {@code WASmaxOutSpamMessageMixin.mergeMessageMixin}
 *           composes the canonical
 *           {@code <message t id from? selected? unsent?
 *           deleted_reason?>} envelope with a 17-deep stack of
 *           optional mixins (revoke, admin-revoke, edit, multicast,
 *           pre-filled-text/number, automated, hsm-template,
 *           extension-screen, participant, franking,
 *           wa-message-reporting, smb-broadcast, iab-report-link,
 *           question-response, message-placeholder,
 *           reported-push-name, payload-types). Cobalt exposes a flat
 *           {@code (from, t, id, raw)} record and lets callers build
 *           ad-hoc descriptors using the {@link Builder} for the
 *           common attributes; advanced callers can attach a
 *           pre-built {@link Node} via {@link #raw()} which the
 *           {@link SmaxNewsletterReportRequest} embeds verbatim.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutSpamMessageMixin")
public final class SmaxNewsletterReportMessageEntry {
    /**
     * The sender JID of the offending message — routed verbatim into
     * the message's {@code from} attribute.
     */
    private final Jid messageFrom;

    /**
     * The message timestamp (unix seconds).
     */
    private final long messageTimestamp;

    /**
     * The message stanza id.
     */
    private final String messageId;

    /**
     * The optional pre-built node — when supplied, the request
     * embeds the supplied node verbatim instead of reconstructing
     * the {@code <message>} envelope from the scalar fields.
     */
    private final Node raw;

    /**
     * Constructs a new entry from scalar fields (no add-ons).
     *
     * @param messageFrom      the sender JID; never {@code null}
     * @param messageTimestamp the timestamp
     * @param messageId        the stanza id; never {@code null}
     * @throws NullPointerException if any non-numeric argument is
     *                              {@code null}
     */
    public SmaxNewsletterReportMessageEntry(Jid messageFrom, long messageTimestamp, String messageId) {
        this(messageFrom, messageTimestamp, messageId, null);
    }

    /**
     * Constructs a new entry from a pre-built node.
     *
     * @param messageFrom      the sender JID; never {@code null}
     * @param messageTimestamp the timestamp
     * @param messageId        the stanza id; never {@code null}
     * @param raw              the optional pre-built node; may be
     *                         {@code null}
     * @throws NullPointerException if any non-optional argument is
     *                              {@code null}
     */
    public SmaxNewsletterReportMessageEntry(Jid messageFrom, long messageTimestamp, String messageId, Node raw) {
        this.messageFrom = Objects.requireNonNull(messageFrom, "messageFrom cannot be null");
        this.messageTimestamp = messageTimestamp;
        this.messageId = Objects.requireNonNull(messageId, "messageId cannot be null");
        this.raw = raw;
    }

    /**
     * Returns the sender JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid messageFrom() {
        return messageFrom;
    }

    /**
     * Returns the message timestamp.
     *
     * @return the timestamp
     */
    public long messageTimestamp() {
        return messageTimestamp;
    }

    /**
     * Returns the message stanza id.
     *
     * @return the id; never {@code null}
     */
    public String messageId() {
        return messageId;
    }

    /**
     * Returns the optional pre-built node.
     *
     * @return an {@link Optional} carrying the node, or empty when
     *         the request should reconstruct the envelope from the
     *         scalar fields
     */
    public Optional<Node> raw() {
        return Optional.ofNullable(raw);
    }

    /**
     * Builds the {@code <message>} child for embedding into the
     * {@code <spam_list>} payload.
     *
     * @return the built node; never {@code null}
     */
    public Node toNode() {
        if (raw != null) {
            return raw;
        }
        return new NodeBuilder()
                .description("message")
                .attribute("from", messageFrom)
                .attribute("t", messageTimestamp)
                .attribute("id", messageId)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxNewsletterReportMessageEntry) obj;
        return this.messageTimestamp == that.messageTimestamp
                && Objects.equals(this.messageFrom, that.messageFrom)
                && Objects.equals(this.messageId, that.messageId)
                && Objects.equals(this.raw, that.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageFrom, messageTimestamp, messageId, raw);
    }

    @Override
    public String toString() {
        return "SmaxNewsletterReportMessageEntry[messageFrom=" + messageFrom
                + ", messageTimestamp=" + messageTimestamp
                + ", messageId=" + messageId + ']';
    }
}
