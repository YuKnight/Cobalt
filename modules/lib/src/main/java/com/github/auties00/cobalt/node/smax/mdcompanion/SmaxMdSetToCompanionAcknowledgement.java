package com.github.auties00.cobalt.node.smax.mdcompanion;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound ack stanza. Emitted by the companion back through
 * the socket pipeline after consuming the {@link SmaxMdSetToCompanionResponse} stanza.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMdSetToCompanionResponseClientResponse")
public final class SmaxMdSetToCompanionAcknowledgement implements SmaxOperation.Request {
    /**
     * The id of the inbound IQ being acknowledged (echoed into
     * {@code <iq id=…>}).
     */
    private final String iqId;

    /**
     * The from of the inbound IQ (echoed into {@code <iq to=…>}).
     */
    private final Jid iqTo;

    /**
     * Constructs a new ack response.
     *
     * @param iqId the IQ id; never {@code null}
     * @param iqTo the destination JID; never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public SmaxMdSetToCompanionAcknowledgement(String iqId, Jid iqTo) {
        this.iqId = Objects.requireNonNull(iqId, "iqId cannot be null");
        this.iqTo = Objects.requireNonNull(iqTo, "iqTo cannot be null");
    }

    /**
     * Constructs an ack from a parsed {@link SmaxMdSetToCompanionResponse} projection.
     *
     * @param inbound the inbound projection; never {@code null}
     * @return a new {@link SmaxMdSetToCompanionAcknowledgement}
     * @throws NullPointerException if {@code inbound} is {@code null}
     */
    public static SmaxMdSetToCompanionAcknowledgement from(SmaxMdSetToCompanionResponse inbound) {
        Objects.requireNonNull(inbound, "inbound cannot be null");
        return new SmaxMdSetToCompanionAcknowledgement(inbound.iqId(), inbound.iqFrom());
    }

    /**
     * Returns the IQ id being echoed.
     *
     * @return the id; never {@code null}
     */
    public String iqId() {
        return iqId;
    }

    /**
     * Returns the destination JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid iqTo() {
        return iqTo;
    }

    /**
     * Builds the outbound ack stanza.
     *
     * @return a {@link NodeBuilder} carrying the ack envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutMdSetToCompanionResponseClientResponse",
            exports = "makeSetToCompanionResponseClientResponse",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        return new NodeBuilder()
                .description("iq")
                .attribute("id", iqId)
                .attribute("to", iqTo)
                .attribute("type", "result");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdSetToCompanionAcknowledgement) obj;
        return Objects.equals(this.iqId, that.iqId)
                && Objects.equals(this.iqTo, that.iqTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iqId, iqTo);
    }

    @Override
    public String toString() {
        return "SmaxMdSetToCompanionAcknowledgement[iqId=" + iqId
                + ", iqTo=" + iqTo + ']';
    }
}
