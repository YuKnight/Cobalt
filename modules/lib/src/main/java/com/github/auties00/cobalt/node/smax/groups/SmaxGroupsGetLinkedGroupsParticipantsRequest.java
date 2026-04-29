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
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetLinkedGroupsParticipantsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQGetRequestMixin")
public final class SmaxGroupsGetLinkedGroupsParticipantsRequest implements SmaxOperation.Request {
    /**
     * The community parent group whose sub-group participant union
     * is being queried.
     */
    private final Jid groupJid;

    /**
     * Constructs a request for the given community parent group.
     *
     * @param groupJid the community parent group JID; never
     *                 {@code null}
     * @throws NullPointerException if {@code groupJid} is
     *                              {@code null}
     */
    public SmaxGroupsGetLinkedGroupsParticipantsRequest(Jid groupJid) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
    }

    /**
     * Returns the community parent group JID.
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
     *         {@code <linked_groups_participants/>} payload
     *
     * @implNote {@code WASmaxOutGroupsGetLinkedGroupsParticipantsRequest.makeGetLinkedGroupsParticipantsRequest}
     *           wraps a bare {@code <linked_groups_participants/>}
     *           in {@code WASmaxOutGroupsBaseGetGroupMixin}
     *           ({@code xmlns="w:g2"}, {@code to=GROUP_JID(t)},
     *           {@code id=generateId()}, {@code type="get"}).
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsGetLinkedGroupsParticipantsRequest",
            exports = "makeGetLinkedGroupsParticipantsRequest",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var payload = new NodeBuilder()
                .description("linked_groups_participants")
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
        var that = (SmaxGroupsGetLinkedGroupsParticipantsRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid);
    }

    @Override
    public String toString() {
        return "SmaxGroupsGetLinkedGroupsParticipantsRequest[groupJid=" + groupJid + ']';
    }
}
