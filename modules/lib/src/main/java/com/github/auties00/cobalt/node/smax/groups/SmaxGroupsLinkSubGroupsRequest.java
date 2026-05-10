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
 * {@code <links><link link_type="sub_group">...</link></links>} payload
 * in the canonical {@code <iq xmlns="w:g2" type="set" to="<parent>">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsLinkSubGroupsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsLinkSubGroupsRequest implements SmaxOperation.Request {
    /**
     * The parent (community) group JID to which the sub-groups are
     * being linked. Routed verbatim into the IQ's {@code to}
     * attribute.
     */
    private final Jid parentGroupJid;

    /**
     * The list of candidate groups, each with an optional hidden-group
     * marker. Must be non-empty (1..1000 entries server-side).
     */
    private final List<RequestedGroup> groups;

    /**
     * Constructs a request.
     *
     * @param parentGroupJid the parent community JID; never
     *                       {@code null}
     * @param groups         the list of groups to link as sub-groups;
     *                       never {@code null} and must be non-empty
     * @throws NullPointerException     if either argument is
     *                                  {@code null}
     * @throws IllegalArgumentException when {@code groups} is empty
     */
    public SmaxGroupsLinkSubGroupsRequest(Jid parentGroupJid, List<RequestedGroup> groups) {
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
     * Returns the list of candidate sub-groups.
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
     *         {@code <links><link/></links>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsLinkSubGroupsRequest",
            exports = "makeLinkSubGroupsRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var groupNodes = new ArrayList<Node>();
        for (var group : groups) {
            var groupBuilder = new NodeBuilder()
                    .description("group")
                    .attribute("jid", group.jid());
            if (group.hiddenGroup()) {
                var hiddenNode = new NodeBuilder()
                        .description("hidden_group")
                        .build();
                groupBuilder.content(hiddenNode);
            }
            groupNodes.add(groupBuilder.build());
        }
        var linkNode = new NodeBuilder()
                .description("link")
                .attribute("link_type", "sub_group")
                .content(groupNodes)
                .build();
        var linksNode = new NodeBuilder()
                .description("links")
                .content(linkNode)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", parentGroupJid)
                .attribute("type", "set")
                .content(linksNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsLinkSubGroupsRequest) obj;
        return Objects.equals(this.parentGroupJid, that.parentGroupJid)
                && Objects.equals(this.groups, that.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGroupJid, groups);
    }

    @Override
    public String toString() {
        return "SmaxGroupsLinkSubGroupsRequest[parentGroupJid=" + parentGroupJid
                + ", groups=" + groups + ']';
    }

    /**
     * Single sub-group entry inside the outbound
     * {@code <links><link/></links>} payload.
     *
     * <p>The {@code hiddenGroup} flag, when {@code true}, attaches a
     * {@code <hidden_group/>} marker inside the {@code <group/>}
     * child to indicate that the linked group is hidden from the
     * community directory.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutGroupsLinkSubGroupsRequest")
    public static final class RequestedGroup {
        /**
         * The candidate sub-group JID.
         */
        private final Jid jid;

        /**
         * Whether to attach a {@code <hidden_group/>} marker inside
         * the {@code <group/>} child.
         */
        private final boolean hiddenGroup;

        /**
         * Constructs a requested-group entry.
         *
         * @param jid         the sub-group JID; never {@code null}
         * @param hiddenGroup whether the sub-group is hidden from the
         *                    community directory
         * @throws NullPointerException if {@code jid} is {@code null}
         */
        public RequestedGroup(Jid jid, boolean hiddenGroup) {
            this.jid = Objects.requireNonNull(jid, "jid cannot be null");
            this.hiddenGroup = hiddenGroup;
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
         * Returns whether the {@code <hidden_group/>} marker is
         * attached.
         *
         * @return {@code true} when the marker is emitted
         */
        public boolean hiddenGroup() {
            return hiddenGroup;
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
            return this.hiddenGroup == that.hiddenGroup
                    && Objects.equals(this.jid, that.jid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid, hiddenGroup);
        }

        @Override
        public String toString() {
            return "SmaxGroupsLinkSubGroupsRequest.RequestedGroup[jid=" + jid
                    + ", hiddenGroup=" + hiddenGroup + ']';
        }
    }
}
