package com.github.auties00.cobalt.node.smax.presence;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the {@code to} (peer JID),
 * optional {@code name} (display hint), and optional
 * {@code context} (parent group JID) into a
 * {@code <presence type="subscribe"/>} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPresenceSubscribeRequest")
public final class SmaxSubscribeRequest implements SmaxOperation.Request {
    /**
     * The peer being subscribed to. Routed verbatim into the
     * stanza's {@code to} attribute.
     */
    private final Jid presenceTo;

    /**
     * The optional display-name hint to advertise to the peer when
     * the subscription propagates.
     */
    private final String presenceName;

    /**
     * The optional parent group JID — supplied when the
     * subscription targets a participant of an open group chat,
     * letting the relay scope the push to that group's frame only.
     */
    private final Jid presenceContext;

    /**
     * Constructs a new subscription request.
     *
     * @param presenceTo      the peer to subscribe to; never
     *                        {@code null}
     * @param presenceName    the optional display-name hint; may be
     *                        {@code null}
     * @param presenceContext the optional parent group JID; may be
     *                        {@code null}
     * @throws NullPointerException if {@code presenceTo} is
     *                              {@code null}
     */
    public SmaxSubscribeRequest(Jid presenceTo, String presenceName, Jid presenceContext) {
        this.presenceTo = Objects.requireNonNull(presenceTo, "presenceTo cannot be null");
        this.presenceName = presenceName;
        this.presenceContext = presenceContext;
    }

    /**
     * Returns the peer JID being subscribed to.
     *
     * @return the peer JID; never {@code null}
     */
    public Jid presenceTo() {
        return presenceTo;
    }

    /**
     * Returns the optional display-name hint.
     *
     * @return an {@link Optional} carrying the hint, or empty when
     *         omitted
     */
    public Optional<String> presenceName() {
        return Optional.ofNullable(presenceName);
    }

    /**
     * Returns the optional parent group JID.
     *
     * @return an {@link Optional} carrying the group JID, or empty
     *         when the subscription is not group-scoped
     */
    public Optional<Jid> presenceContext() {
        return Optional.ofNullable(presenceContext);
    }

    /**
     * Builds the outbound presence stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the
     *         {@code <presence type="subscribe" to=… name? context?/>}
     *         envelope
     *
     * @implNote {@code WASmaxOutPresenceSubscribeRequest.makeSubscribeRequest}
     *           emits {@code smax("presence", {type:"subscribe",
     *           to:JID, name:OPTIONAL, context:OPTIONAL_GROUP_JID})}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPresenceSubscribeRequest",
            exports = "makeSubscribeRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        return new NodeBuilder()
                .description("presence")
                .attribute("type", "subscribe")
                .attribute("to", presenceTo)
                .attribute("name", presenceName)
                .attribute("context", presenceContext);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxSubscribeRequest) obj;
        return Objects.equals(this.presenceTo, that.presenceTo)
                && Objects.equals(this.presenceName, that.presenceName)
                && Objects.equals(this.presenceContext, that.presenceContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(presenceTo, presenceName, presenceContext);
    }

    @Override
    public String toString() {
        return "SmaxSubscribeRequest[presenceTo=" + presenceTo
                + ", presenceName=" + presenceName
                + ", presenceContext=" + presenceContext + ']';
    }
}
