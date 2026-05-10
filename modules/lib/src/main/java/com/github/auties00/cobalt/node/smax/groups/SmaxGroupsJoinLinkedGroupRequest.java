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
 * The outbound stanza variant — wraps the
 * {@code <join_linked_group jid type/>} payload in the canonical
 * {@code <iq xmlns="w:g2" type="set" to="<parentGroupJid>">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsJoinLinkedGroupRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsJoinLinkedGroupRequest implements SmaxOperation.Request {
    /**
     * The parent (community) group JID to which the IQ is addressed.
     * Routed verbatim into the IQ's {@code to} attribute.
     */
    private final Jid parentGroupJid;

    /**
     * The sub-group JID the caller wishes to join.
     */
    private final Jid joinLinkedGroupJid;

    /**
     * The optional join-type discriminator (e.g. {@code "default"});
     * when {@code null} the {@code type} attribute is omitted.
     */
    private final String joinLinkedGroupType;

    /**
     * Constructs a request.
     *
     * @param parentGroupJid       the parent community JID; never
     *                             {@code null}
     * @param joinLinkedGroupJid   the sub-group JID to join; never
     *                             {@code null}
     * @param joinLinkedGroupType  the optional join-type
     *                             discriminator; may be {@code null}
     * @throws NullPointerException if {@code parentGroupJid} or
     *                              {@code joinLinkedGroupJid} is
     *                              {@code null}
     */
    public SmaxGroupsJoinLinkedGroupRequest(Jid parentGroupJid, Jid joinLinkedGroupJid, String joinLinkedGroupType) {
        this.parentGroupJid = Objects.requireNonNull(parentGroupJid, "parentGroupJid cannot be null");
        this.joinLinkedGroupJid = Objects.requireNonNull(joinLinkedGroupJid, "joinLinkedGroupJid cannot be null");
        this.joinLinkedGroupType = joinLinkedGroupType;
    }

    /**
     * Returns the parent community group JID.
     *
     * @return the parent group JID; never {@code null}
     */
    public Jid parentGroupJid() {
        return parentGroupJid;
    }

    /**
     * Returns the sub-group JID being joined.
     *
     * @return the linked group JID; never {@code null}
     */
    public Jid joinLinkedGroupJid() {
        return joinLinkedGroupJid;
    }

    /**
     * Returns the optional join-type discriminator.
     *
     * @return an {@link Optional} carrying the join-type string, or
     *         empty when the request omits the {@code type} attribute
     */
    public Optional<String> joinLinkedGroupType() {
        return Optional.ofNullable(joinLinkedGroupType);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <join_linked_group/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsJoinLinkedGroupRequest",
            exports = "makeJoinLinkedGroupRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var joinBuilder = new NodeBuilder()
                .description("join_linked_group")
                .attribute("jid", joinLinkedGroupJid);
        if (joinLinkedGroupType != null) {
            joinBuilder.attribute("type", joinLinkedGroupType);
        }
        var joinNode = joinBuilder.build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", parentGroupJid)
                .attribute("type", "set")
                .content(joinNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsJoinLinkedGroupRequest) obj;
        return Objects.equals(this.parentGroupJid, that.parentGroupJid)
                && Objects.equals(this.joinLinkedGroupJid, that.joinLinkedGroupJid)
                && Objects.equals(this.joinLinkedGroupType, that.joinLinkedGroupType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGroupJid, joinLinkedGroupJid, joinLinkedGroupType);
    }

    @Override
    public String toString() {
        return "SmaxGroupsJoinLinkedGroupRequest[parentGroupJid=" + parentGroupJid
                + ", joinLinkedGroupJid=" + joinLinkedGroupJid
                + ", joinLinkedGroupType=" + joinLinkedGroupType + ']';
    }
}
