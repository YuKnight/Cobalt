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
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsDeleteParentGroupRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsDeleteParentGroupRequest implements SmaxOperation.Request {
    /**
     * The parent (community) group JID to deactivate.
     */
    private final Jid parentGroupJid;

    /**
     * Constructs a request.
     *
     * @param parentGroupJid the community JID; never {@code null}
     * @throws NullPointerException if {@code parentGroupJid} is
     *                              {@code null}
     */
    public SmaxGroupsDeleteParentGroupRequest(Jid parentGroupJid) {
        this.parentGroupJid = Objects.requireNonNull(parentGroupJid, "parentGroupJid cannot be null");
    }

    /**
     * Returns the parent group JID.
     *
     * @return the parent group JID; never {@code null}
     */
    public Jid parentGroupJid() {
        return parentGroupJid;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <delete_parent/>} payload
     *
     * @implNote {@code WASmaxOutGroupsDeleteParentGroupRequest.makeDeleteParentGroupRequest}
     *           composes {@code WASmaxOutGroupsBaseSetGroupMixin}
     *           over a bare {@code <delete_parent/>} child.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsDeleteParentGroupRequest",
            exports = "makeDeleteParentGroupRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutGroupsDeleteParentGroupRequest: smax("delete_parent", null)
        var deleteParentNode = new NodeBuilder()
                .description("delete_parent")
                .build();
        // WASmaxOutGroupsBaseSetGroupMixin + WASmaxOutGroupsBaseIQSetRequestMixin
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", parentGroupJid)
                .attribute("type", "set")
                .content(deleteParentNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsDeleteParentGroupRequest) obj;
        return Objects.equals(this.parentGroupJid, that.parentGroupJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGroupJid);
    }

    @Override
    public String toString() {
        return "SmaxGroupsDeleteParentGroupRequest[parentGroupJid=" + parentGroupJid + ']';
    }
}
