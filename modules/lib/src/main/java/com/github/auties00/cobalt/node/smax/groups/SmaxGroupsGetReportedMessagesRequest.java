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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetReportedMessagesRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQGetRequestMixin")
public final class SmaxGroupsGetReportedMessagesRequest implements SmaxOperation.Request {
    /**
     * The group whose moderation queue is being inspected.
     */
    private final Jid groupJid;

    /**
     * Constructs a request for the given group.
     *
     * @param groupJid the group JID; never {@code null}
     * @throws NullPointerException if {@code groupJid} is
     *                              {@code null}
     */
    public SmaxGroupsGetReportedMessagesRequest(Jid groupJid) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
    }

    /**
     * Returns the group JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         {@code <reports/>} payload
     *
     * @implNote {@code WASmaxOutGroupsGetReportedMessagesRequest.makeGetReportedMessagesRequest}
     *           wraps a bare {@code <reports/>} in
     *           {@code WASmaxOutGroupsBaseGetGroupMixin}
     *           ({@code xmlns="w:g2"}, {@code to=GROUP_JID(t)},
     *           {@code id=generateId()}, {@code type="get"}).
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsGetReportedMessagesRequest",
            exports = "makeGetReportedMessagesRequest",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var payload = new NodeBuilder()
                .description("reports")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "get")
                .content(payload);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsGetReportedMessagesRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid);
    }

    @Override
    public String toString() {
        return "SmaxGroupsGetReportedMessagesRequest[groupJid=" + groupJid + ']';
    }
}
