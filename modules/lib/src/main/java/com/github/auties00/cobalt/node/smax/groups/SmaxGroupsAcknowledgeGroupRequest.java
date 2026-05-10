package com.github.auties00.cobalt.node.smax.groups;

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
 * The outbound stanza variant — wraps the bare {@code <ack/>} payload
 * in the canonical {@code <iq xmlns="w:g2" type="set" to="<groupJid>">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsAcknowledgeGroupRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsAcknowledgeGroupRequest implements SmaxOperation.Request {
    /**
     * The group JID to acknowledge. Routed verbatim into the IQ's
     * {@code to} attribute.
     */
    private final Jid groupJid;

    /**
     * Constructs a request for the given group.
     *
     * @param groupJid the group to acknowledge; never {@code null}
     * @throws NullPointerException if {@code groupJid} is {@code null}
     */
    public SmaxGroupsAcknowledgeGroupRequest(Jid groupJid) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
    }

    /**
     * Returns the group being acknowledged.
     *
     * @return the group JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <ack/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsAcknowledgeGroupRequest",
            exports = "makeAcknowledgeGroupRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var ackNode = new NodeBuilder()
                .description("ack")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(ackNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsAcknowledgeGroupRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid);
    }

    @Override
    public String toString() {
        return "SmaxGroupsAcknowledgeGroupRequest[groupJid=" + groupJid + ']';
    }
}
