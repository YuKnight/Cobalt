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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the {@code <my_addons/>} payload
 * in the canonical
 * {@code <iq xmlns="newsletter" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersMyAddOnsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersSelfIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersBaseIQGetRequestMixin")
public final class SmaxNewslettersMyAddOnsRequest implements SmaxOperation.Request {
    /**
     * The maximum number of {@code <messages>} blocks the relay should
     * return.
     */
    private final int limit;

    /**
     * The optional newsletter JID to scope the query to a single
     * newsletter; {@code null} fetches add-ons across every newsletter
     * the user follows.
     */
    private final Jid newsletterJid;

    /**
     * Constructs a request with the given limit and optional
     * newsletter scope.
     *
     * @param limit         the per-newsletter cap; must be non-negative
     * @param newsletterJid the optional newsletter to scope to; may be
     *                      {@code null}
     */
    public SmaxNewslettersMyAddOnsRequest(int limit, Jid newsletterJid) {
        this.limit = limit;
        this.newsletterJid = newsletterJid;
    }

    /**
     * Returns the per-newsletter cap on returned messages.
     *
     * @return the limit
     */
    public int limit() {
        return limit;
    }

    /**
     * Returns the optional newsletter scope.
     *
     * @return an {@link Optional} carrying the newsletter JID, or empty
     *         when the request fetches add-ons across every newsletter
     */
    public Optional<Jid> newsletterJid() {
        return Optional.ofNullable(newsletterJid);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <my_addons/>} payload
     *
     * @implNote {@code WASmaxOutNewslettersMyAddOnsRequest.makeMyAddOnsRequest}
     *           composes {@code WASmaxOutNewslettersSelfIQGetRequestMixin}
     *           ({@code xmlns="newsletter"}, {@code to=S_WHATSAPP_NET},
     *           {@code id=generateId()}, {@code type="get"}) over a
     *           {@code <my_addons limit=INT(t) jid=OPTIONAL(JID, n)/>}
     *           child.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutNewslettersMyAddOnsRequest",
            exports = "makeMyAddOnsRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var myAddOnsBuilder = new NodeBuilder()
                .description("my_addons")
                .attribute("limit", limit);
        if (newsletterJid != null) {
            myAddOnsBuilder.attribute("jid", newsletterJid);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "newsletter")
                .attribute("to", Jid.userServer())
                .attribute("type", "get")
                .content(myAddOnsBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxNewslettersMyAddOnsRequest) obj;
        return this.limit == that.limit && Objects.equals(this.newsletterJid, that.newsletterJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, newsletterJid);
    }

    @Override
    public String toString() {
        return "SmaxNewslettersMyAddOnsRequest[limit=" + limit
                + ", newsletterJid=" + newsletterJid + ']';
    }
}
