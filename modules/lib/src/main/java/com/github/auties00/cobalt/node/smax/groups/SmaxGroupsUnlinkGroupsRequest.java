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
 * The outbound stanza variant — wraps the
 * {@code <unlink unlink_type="sub_group">...</unlink>} payload in the
 * canonical {@code <iq xmlns="w:g2" type="set" to="<parent>">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsUnlinkGroupsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsUnlinkGroupsRequest implements SmaxOperation.Request {
    /**
     * The parent (community) group JID. Routed verbatim into the IQ's
     * {@code to} attribute.
     */
    private final Jid parentGroupJid;

    /**
     * The list of sub-groups to unlink. Must be non-empty (1..1000
     * entries server-side).
     */
    private final List<RequestedGroup> groups;

    /**
     * Constructs a request.
     *
     * @param parentGroupJid the parent community JID; never
     *                       {@code null}
     * @param groups         the list of sub-groups to unlink; never
     *                       {@code null} and must be non-empty
     * @throws NullPointerException     if either argument is
     *                                  {@code null}
     * @throws IllegalArgumentException when {@code groups} is empty
     */
    public SmaxGroupsUnlinkGroupsRequest(Jid parentGroupJid, List<RequestedGroup> groups) {
        Objects.requireNonNull(parentGroupJid, "parentGroupJid cannot be null");
        Objects.requireNonNull(groups, "groups cannot be null");
        if (groups.isEmpty()) {
            throw new IllegalArgumentException("groups must contain at least one entry");
        }
        this.parentGroupJid = parentGroupJid;
        this.groups = List.copyOf(groups);
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
     * Returns the list of sub-groups to unlink.
     *
     * @return an unmodifiable list; never {@code null} or empty
     */
    public List<RequestedGroup> groups() {
        return groups;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <unlink/>} payload
     *
     * @implNote {@code WASmaxOutGroupsUnlinkGroupsRequest.makeUnlinkGroupsRequest}
     *           composes
     *           {@code WASmaxOutGroupsBaseSetGroupMixin} over
     *           {@code <unlink unlink_type="sub_group">REPEATED_CHILD(<group jid [remove_orphaned_members]/>)</unlink>}.
     *           Cobalt mirrors the same nesting; the
     *           {@code remove_orphaned_members} attribute is emitted
     *           as the literal string {@code "true"} (matching the
     *           server-side {@code OPTIONAL_LITERAL("true", flag)}
     *           semantics).
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsUnlinkGroupsRequest",
            exports = "makeUnlinkGroupsRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var groupNodes = new ArrayList<Node>();
        for (var group : groups) {
            var groupBuilder = new NodeBuilder()
                    .description("group")
                    .attribute("jid", group.jid());
            if (group.removeOrphanedMembers()) {
                groupBuilder.attribute("remove_orphaned_members", "true");
            }
            groupNodes.add(groupBuilder.build());
        }
        var unlinkNode = new NodeBuilder()
                .description("unlink")
                .attribute("unlink_type", "sub_group")
                .content(groupNodes)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", parentGroupJid)
                .attribute("type", "set")
                .content(unlinkNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsUnlinkGroupsRequest) obj;
        return Objects.equals(this.parentGroupJid, that.parentGroupJid)
                && Objects.equals(this.groups, that.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGroupJid, groups);
    }

    @Override
    public String toString() {
        return "SmaxGroupsUnlinkGroupsRequest[parentGroupJid=" + parentGroupJid
                + ", groups=" + groups + ']';
    }

    /**
     * Single sub-group entry inside the outbound {@code <unlink/>}
     * payload.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutGroupsUnlinkGroupsRequest")
    public static final class RequestedGroup {
        /**
         * The sub-group JID to unlink.
         */
        private final Jid jid;

        /**
         * Whether to ask the relay to evict community members no
         * longer affiliated with any sub-group.
         */
        private final boolean removeOrphanedMembers;

        /**
         * Constructs a requested-group entry.
         *
         * @param jid                   the sub-group JID; never
         *                              {@code null}
         * @param removeOrphanedMembers whether to evict orphaned
         *                              community members
         * @throws NullPointerException if {@code jid} is {@code null}
         */
        public RequestedGroup(Jid jid, boolean removeOrphanedMembers) {
            this.jid = Objects.requireNonNull(jid, "jid cannot be null");
            this.removeOrphanedMembers = removeOrphanedMembers;
        }

        /**
         * Returns the sub-group JID.
         *
         * @return the sub-group JID; never {@code null}
         */
        public Jid jid() {
            return jid;
        }

        /**
         * Returns whether the eviction flag is set.
         *
         * @return {@code true} when the
         *         {@code remove_orphaned_members="true"} attribute
         *         is emitted
         */
        public boolean removeOrphanedMembers() {
            return removeOrphanedMembers;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (RequestedGroup) obj;
            return this.removeOrphanedMembers == that.removeOrphanedMembers
                    && Objects.equals(this.jid, that.jid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid, removeOrphanedMembers);
        }

        @Override
        public String toString() {
            return "SmaxGroupsUnlinkGroupsRequest.RequestedGroup[jid=" + jid
                    + ", removeOrphanedMembers=" + removeOrphanedMembers + ']';
        }
    }
}
