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
 * The outbound stanza variant. Wraps the disjunctive
 * {@link SmaxStatusPublishPostNewsletterStatusPayload} children inside a
 * {@code <status to=NEWSLETTER_JID>} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutStatusPublishPostNewsletterStatusRequest")
public final class SmaxStatusPublishPostNewsletterStatusRequest implements SmaxOperation.Request {
    /**
     * The newsletter JID being posted to. Routed verbatim into the
     * status's {@code to} attribute.
     */
    private final Jid newsletterJid;

    /**
     * The publish payload. Selects between "client + server id"
     * and "client id only".
     */
    private final SmaxStatusPublishPostNewsletterStatusPayload payload;

    /**
     * Constructs a new request.
     *
     * @param newsletterJid the target newsletter JID; never
     *                      {@code null}
     * @param payload       the publish payload; never {@code null}
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    public SmaxStatusPublishPostNewsletterStatusRequest(Jid newsletterJid, SmaxStatusPublishPostNewsletterStatusPayload payload) {
        this.newsletterJid = Objects.requireNonNull(newsletterJid, "newsletterJid cannot be null");
        this.payload = Objects.requireNonNull(payload, "payload cannot be null");
    }

    /**
     * Returns the target newsletter JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid newsletterJid() {
        return newsletterJid;
    }

    /**
     * Returns the publish payload.
     *
     * @return the payload; never {@code null}
     */
    public SmaxStatusPublishPostNewsletterStatusPayload payload() {
        return payload;
    }

    /**
     * Builds the outbound status stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the status envelope
     *         and the disjunctive payload children
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutStatusPublishPostNewsletterStatusRequest",
            exports = "makePostNewsletterStatusRequest",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var builder = new NodeBuilder()
                .description("status")
                .attribute("to", newsletterJid);
        switch (payload) {
            case SmaxStatusPublishPostNewsletterStatusPayload.WithServerId withServerId -> {
                builder.attribute("id", withServerId.stanzaId());
                builder.attribute("server_id", withServerId.statusServerId());
                builder.content(withServerId.innerContent());
            }
            case SmaxStatusPublishPostNewsletterStatusPayload.WithClientIdOnly withClientId -> {
                builder.attribute("id", withClientId.stanzaId());
                builder.content(withClientId.clientIdContent());
            }
        }
        return builder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxStatusPublishPostNewsletterStatusRequest) obj;
        return Objects.equals(this.newsletterJid, that.newsletterJid)
                && Objects.equals(this.payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newsletterJid, payload);
    }

    @Override
    public String toString() {
        return "SmaxStatusPublishPostNewsletterStatusRequest[newsletterJid=" + newsletterJid
                + ", payload=" + payload + ']';
    }
}
