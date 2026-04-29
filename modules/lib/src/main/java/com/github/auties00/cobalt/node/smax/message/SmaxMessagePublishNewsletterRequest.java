package com.github.auties00.cobalt.node.smax.message;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the disjunctive
 * {@link SmaxMessagePublishNewsletterPayload} children inside a
 * {@code <message to=NEWSLETTER_JID>} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishNewsletterRequest")
public final class SmaxMessagePublishNewsletterRequest implements SmaxOperation.Request {
    /**
     * The newsletter JID being posted to. Routed verbatim into the
     * message's {@code to} attribute.
     */
    private final Jid newsletterJid;

    /**
     * The publish payload — selects between "client + server id" and
     * "client id only".
     */
    private final SmaxMessagePublishNewsletterPayload payload;

    /**
     * Constructs a new request.
     *
     * @param newsletterJid the target newsletter JID; never
     *                      {@code null}
     * @param payload       the publish payload; never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public SmaxMessagePublishNewsletterRequest(Jid newsletterJid, SmaxMessagePublishNewsletterPayload payload) {
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
    public SmaxMessagePublishNewsletterPayload payload() {
        return payload;
    }

    /**
     * Builds the outbound message stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the message envelope
     *         and the disjunctive payload children
     *
     * @implNote {@code WASmaxOutMessagePublishNewsletterRequest.makeNewsletterRequest}
     *           composes
     *           {@code WASmaxOutMessagePublishClientNewsletterAndServerOrNewsletterIDMixinGroup}
     *           over a {@code <message to=JID(messageTo)>} envelope.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutMessagePublishNewsletterRequest",
            exports = "makeNewsletterRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var builder = new NodeBuilder()
                .description("message")
                .attribute("to", newsletterJid);
        switch (payload) {
            case SmaxMessagePublishNewsletterPayload.WithServerId withServerId -> {
                builder.attribute("id", withServerId.stanzaId());
                builder.attribute("server_id", withServerId.messageServerId());
                builder.content(withServerId.innerContent());
            }
            case SmaxMessagePublishNewsletterPayload.WithClientIdOnly withClientId -> {
                builder.attribute("id", withClientId.stanzaId());
                var children = new ArrayList<Node>(3);
                withClientId.msgMetaOrigin().ifPresent(children::add);
                withClientId.senderContentTypeMediaRcat().ifPresent(children::add);
                children.add(withClientId.clientIdContent());
                builder.content(children.toArray(Node[]::new));
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
        var that = (SmaxMessagePublishNewsletterRequest) obj;
        return Objects.equals(this.newsletterJid, that.newsletterJid)
                && Objects.equals(this.payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newsletterJid, payload);
    }

    @Override
    public String toString() {
        return "SmaxMessagePublishNewsletterRequest[newsletterJid=" + newsletterJid
                + ", payload=" + payload + ']';
    }
}
