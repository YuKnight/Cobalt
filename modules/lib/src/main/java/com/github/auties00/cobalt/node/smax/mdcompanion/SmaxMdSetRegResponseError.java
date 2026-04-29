package com.github.auties00.cobalt.node.smax.mdcompanion;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The error reply variant — emitted when the companion declines
 * to complete the link.
 *
 * @implNote {@code WASmaxOutMdSetRegResponseError.makeSetRegResponseError}
 *           composes
 *           {@code WASmaxOutMdIQErrorNotAuthorizedMixin.mergeIQErrorNotAuthorizedMixin}
 *           which sets the canonical {@code <error code="401"
 *           text="not-authorized"/>} child.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMdSetRegResponseError")
@WhatsAppWebModule(moduleName = "WASmaxOutMdIQErrorNotAuthorizedMixin")
public final class SmaxMdSetRegResponseError implements SmaxOperation.Request {
    /**
     * The id of the inbound IQ being replied to.
     */
    private final String iqId;

    /**
     * Constructs a new error reply.
     *
     * @param iqId the IQ id; never {@code null}
     * @throws NullPointerException if {@code iqId} is {@code null}
     */
    public SmaxMdSetRegResponseError(String iqId) {
        this.iqId = Objects.requireNonNull(iqId, "iqId cannot be null");
    }

    /**
     * Returns the IQ id.
     *
     * @return the id; never {@code null}
     */
    public String iqId() {
        return iqId;
    }

    /**
     * Builds the outbound error stanza.
     *
     * @return a {@link NodeBuilder} carrying the error envelope
     *
     * @implNote emits {@code <iq id to="s.whatsapp.net"
     *           type="error"><error code="401"
     *           text="not-authorized"/></iq>}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutMdSetRegResponseError",
            exports = "makeSetRegResponseError",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var errorNode = new NodeBuilder()
                .description("error")
                .attribute("text", "not-authorized")
                .attribute("code", 401)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("id", iqId)
                .attribute("to", "s.whatsapp.net")
                .attribute("type", "error")
                .content(errorNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdSetRegResponseError) obj;
        return Objects.equals(this.iqId, that.iqId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iqId);
    }

    @Override
    public String toString() {
        return "SmaxMdSetRegResponseError[iqId=" + iqId + ']';
    }
}
