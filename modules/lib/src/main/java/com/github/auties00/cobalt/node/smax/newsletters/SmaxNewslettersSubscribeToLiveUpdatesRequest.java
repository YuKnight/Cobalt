package com.github.auties00.cobalt.node.smax.newsletters;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the bare {@code <live_updates/>}
 * payload in the canonical
 * {@code <iq xmlns="newsletter" type="set" to="<newsletterJid>">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersSubscribeToLiveUpdatesRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersNewsletterIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersBaseIQSetRequestMixin")
public final class SmaxNewslettersSubscribeToLiveUpdatesRequest implements SmaxOperation.Request {
    /**
     * The newsletter JID to subscribe to. Routed verbatim into the IQ's
     * {@code to} attribute.
     */
    private final Jid newsletterJid;

    /**
     * Constructs a request for the given newsletter.
     *
     * @param newsletterJid the newsletter to subscribe to; never
     *                      {@code null}
     * @throws NullPointerException if {@code newsletterJid} is
     *                              {@code null}
     */
    public SmaxNewslettersSubscribeToLiveUpdatesRequest(Jid newsletterJid) {
        this.newsletterJid = Objects.requireNonNull(newsletterJid, "newsletterJid cannot be null");
    }

    /**
     * Returns the newsletter being subscribed to.
     *
     * @return the newsletter JID; never {@code null}
     */
    public Jid newsletterJid() {
        return newsletterJid;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <live_updates/>} payload
     *
     * @implNote {@code WASmaxOutNewslettersSubscribeToLiveUpdatesRequest.makeSubscribeToLiveUpdatesRequest}
     *           composes {@code WASmaxOutNewslettersNewsletterIQSetRequestMixin}
     *           ({@code xmlns="newsletter"}, {@code to=JID(iqTo)},
     *           {@code id=generateId()}, {@code type="set"}) over a bare
     *           {@code <live_updates/>} child.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutNewslettersSubscribeToLiveUpdatesRequest",
            exports = "makeSubscribeToLiveUpdatesRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var liveUpdatesNode = new NodeBuilder()
                .description("live_updates")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "newsletter")
                .attribute("to", newsletterJid)
                .attribute("type", "set")
                .content(liveUpdatesNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxNewslettersSubscribeToLiveUpdatesRequest) obj;
        return Objects.equals(this.newsletterJid, that.newsletterJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newsletterJid);
    }

    @Override
    public String toString() {
        return "SmaxNewslettersSubscribeToLiveUpdatesRequest[newsletterJid=" + newsletterJid + ']';
    }
}
