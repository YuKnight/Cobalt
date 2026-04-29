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
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutSpamStatusReportV2Request")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBaseReportMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamReportableNewsletterStatusMixin")
public final class SmaxStatusReportV2Request implements SmaxOperation.Request {
    /**
     * The newsletter JID being reported.
     */
    private final Jid spamListJid;

    /**
     * The spam-flow string surfacing the user-visible report flow.
     */
    private final String spamListSpamFlow;

    /**
     * The server-id of the offending status post.
     */
    private final long statusServerId;

    /**
     * The unix-second timestamp of the offending status post.
     */
    private final long statusTimestamp;

    /**
     * The optional newsletter subject string carried via
     * {@code WASmaxOutSpamEntitySubjectMixin}.
     */
    private final String spamListSubject;

    /**
     * The optional pre-built status payload child (the text-or-media
     * inner content) — when supplied, embedded verbatim under the
     * {@code <status>} envelope; when {@code null}, the status
     * envelope is emitted without a content child.
     */
    private final Node statusPayloadContent;

    /**
     * Constructs a new request.
     *
     * @param spamListJid          the newsletter JID; never
     *                             {@code null}
     * @param spamListSpamFlow     the spam-flow string; never
     *                             {@code null}
     * @param statusServerId       the status's server-id
     * @param statusTimestamp      the status's timestamp
     * @param spamListSubject      the optional subject; may be
     *                             {@code null}
     * @param statusPayloadContent the optional payload child; may be
     *                             {@code null}
     * @throws NullPointerException if any non-optional argument is
     *                              {@code null}
     */
    public SmaxStatusReportV2Request(Jid spamListJid, String spamListSpamFlow,
                   long statusServerId, long statusTimestamp,
                   String spamListSubject, Node statusPayloadContent) {
        this.spamListJid = Objects.requireNonNull(spamListJid, "spamListJid cannot be null");
        this.spamListSpamFlow = Objects.requireNonNull(spamListSpamFlow, "spamListSpamFlow cannot be null");
        this.statusServerId = statusServerId;
        this.statusTimestamp = statusTimestamp;
        this.spamListSubject = spamListSubject;
        this.statusPayloadContent = statusPayloadContent;
    }

    /**
     * Returns the newsletter JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid spamListJid() {
        return spamListJid;
    }

    /**
     * Returns the spam-flow string.
     *
     * @return the spam-flow; never {@code null}
     */
    public String spamListSpamFlow() {
        return spamListSpamFlow;
    }

    /**
     * Returns the status server-id.
     *
     * @return the server-id
     */
    public long statusServerId() {
        return statusServerId;
    }

    /**
     * Returns the status timestamp.
     *
     * @return the timestamp
     */
    public long statusTimestamp() {
        return statusTimestamp;
    }

    /**
     * Returns the optional subject string.
     *
     * @return an {@link Optional} carrying the subject, or empty when
     *         omitted
     */
    public Optional<String> spamListSubject() {
        return Optional.ofNullable(spamListSubject);
    }

    /**
     * Returns the optional pre-built payload child.
     *
     * @return an {@link Optional} carrying the node, or empty when
     *         omitted
     */
    public Optional<Node> statusPayloadContent() {
        return Optional.ofNullable(statusPayloadContent);
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         payload
     *
     * @implNote {@code WASmaxOutSpamStatusReportV2Request.makeStatusReportV2Request}
     *           composes
     *           {@code WASmaxOutSpamBaseIQSetRequestMixin}
     *           ({@code id=generateId() type="set"}) and
     *           {@code WASmaxOutSpamBaseReportMixin}
     *           ({@code to=S_WHATSAPP_NET xmlns="spam"
     *           spam_flow=…}) over a
     *           {@code <spam_list jid=…><status server_id t …/>}
     *           payload. The optional
     *           {@code WASmaxOutSpamEntitySubjectMixin} layers a
     *           {@code subject} attribute when present.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutSpamStatusReportV2Request",
            exports = "makeStatusReportV2Request", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutSpamReportableNewsletterStatusMixin: smax("status", {server_id, t})
        //   + optional payload child via mergeStatusNewsletterTextOrMediaMixinGroup
        var statusBuilder = new NodeBuilder()
                .description("status")
                .attribute("server_id", statusServerId)
                .attribute("t", statusTimestamp);
        if (statusPayloadContent != null) {
            statusBuilder.content(statusPayloadContent);
        }
        // WASmaxOutSpamBaseReportMixin: smax("spam_list", {spam_flow})
        // WASmaxOutSpamEntitySubjectMixin (optional): smax("spam_list", {subject})
        // WASmaxOutSpamStatusReportV2Request: smax("spam_list", {jid}, <status/>)
        var spamListBuilder = new NodeBuilder()
                .description("spam_list")
                .attribute("jid", spamListJid)
                .attribute("spam_flow", spamListSpamFlow);
        if (spamListSubject != null) {
            spamListBuilder.attribute("subject", spamListSubject);
        }
        spamListBuilder.content(statusBuilder.build());
        // WASmaxOutSpamBaseIQSetRequestMixin: smax("iq", {id: generateId(), type: "set"})
        // WASmaxOutSpamBaseReportMixin: smax("iq", {to: S_WHATSAPP_NET, xmlns: "spam"})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "spam")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(spamListBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxStatusReportV2Request) obj;
        return this.statusServerId == that.statusServerId
                && this.statusTimestamp == that.statusTimestamp
                && Objects.equals(this.spamListJid, that.spamListJid)
                && Objects.equals(this.spamListSpamFlow, that.spamListSpamFlow)
                && Objects.equals(this.spamListSubject, that.spamListSubject)
                && Objects.equals(this.statusPayloadContent, that.statusPayloadContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spamListJid, spamListSpamFlow, statusServerId, statusTimestamp,
                spamListSubject, statusPayloadContent);
    }

    @Override
    public String toString() {
        return "SmaxStatusReportV2Request[spamListJid=" + spamListJid
                + ", spamListSpamFlow=" + spamListSpamFlow
                + ", statusServerId=" + statusServerId
                + ", statusTimestamp=" + statusTimestamp
                + ", spamListSubject=" + spamListSubject + ']';
    }
}
