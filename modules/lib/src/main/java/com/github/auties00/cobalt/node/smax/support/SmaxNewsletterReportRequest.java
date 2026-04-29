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
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutSpamNewsletterReportRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBaseReportMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamEntitySubjectMixin")
public final class SmaxNewsletterReportRequest implements SmaxOperation.Request {
    /**
     * The newsletter JID being reported — routed into the
     * {@code <spam_list jid>} attribute.
     */
    private final Jid spamListJid;

    /**
     * The spam-flow string surfacing the user-visible report flow
     * (e.g. one of the WA Web spam-flow enum values).
     */
    private final String spamListSpamFlow;

    /**
     * The newsletter subject string echoed by the relay for
     * attribution context.
     */
    private final String spamListSubject;

    /**
     * The list of offending {@code <message>} entries to attach
     * (1..65 per WA Web).
     */
    private final List<SmaxNewsletterReportMessageEntry> messages;

    /**
     * Constructs a new request.
     *
     * @param spamListJid      the newsletter JID; never {@code null}
     * @param spamListSpamFlow the spam-flow string; never {@code null}
     * @param spamListSubject  the subject string; never {@code null}
     * @param messages         the message entries; never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxNewsletterReportRequest(Jid spamListJid, String spamListSpamFlow, String spamListSubject,
                   List<SmaxNewsletterReportMessageEntry> messages) {
        this.spamListJid = Objects.requireNonNull(spamListJid, "spamListJid cannot be null");
        this.spamListSpamFlow = Objects.requireNonNull(spamListSpamFlow, "spamListSpamFlow cannot be null");
        this.spamListSubject = Objects.requireNonNull(spamListSubject, "spamListSubject cannot be null");
        this.messages = List.copyOf(Objects.requireNonNull(messages, "messages cannot be null"));
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
     * Returns the subject string.
     *
     * @return the subject; never {@code null}
     */
    public String spamListSubject() {
        return spamListSubject;
    }

    /**
     * Returns the message entries.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<SmaxNewsletterReportMessageEntry> messages() {
        return messages;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         payload
     *
     * @implNote {@code WASmaxOutSpamNewsletterReportRequest.makeNewsletterReportRequest}
     *           composes
     *           {@code WASmaxOutSpamBaseIQSetRequestMixin}
     *           ({@code id=generateId() type="set"}) and
     *           {@code WASmaxOutSpamBaseReportMixin}
     *           ({@code to=S_WHATSAPP_NET xmlns="spam"} +
     *           {@code <spam_list spam_flow>}) over the
     *           {@code <spam_list jid subject>} payload, then layers
     *           {@code WASmaxOutSpamEntitySubjectMixin} for the
     *           {@code subject} attribute and embeds 0..65 message
     *           children.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutSpamNewsletterReportRequest",
            exports = "makeNewsletterReportRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutSpamMessageMixin: smax("message", {from, t, id, …}) — repeated 0..65
        var children = new ArrayList<Node>(messages.size());
        for (var message : messages) {
            children.add(message.toNode());
        }
        // WASmaxOutSpamBaseReportMixin: smax("spam_list", {spam_flow})
        // WASmaxOutSpamEntitySubjectMixin: smax("spam_list", {subject})
        // WASmaxOutSpamNewsletterReportRequest: smax("spam_list", {jid}, REPEATED_CHILD(message))
        var spamListBuilder = new NodeBuilder()
                .description("spam_list")
                .attribute("jid", spamListJid)
                .attribute("spam_flow", spamListSpamFlow)
                .attribute("subject", spamListSubject)
                .content(children);
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
        var that = (SmaxNewsletterReportRequest) obj;
        return Objects.equals(this.spamListJid, that.spamListJid)
                && Objects.equals(this.spamListSpamFlow, that.spamListSpamFlow)
                && Objects.equals(this.spamListSubject, that.spamListSubject)
                && Objects.equals(this.messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spamListJid, spamListSpamFlow, spamListSubject, messages);
    }

    @Override
    public String toString() {
        return "SmaxNewsletterReportRequest[spamListJid=" + spamListJid
                + ", spamListSpamFlow=" + spamListSpamFlow
                + ", spamListSubject=" + spamListSubject
                + ", messages=" + messages + ']';
    }
}
