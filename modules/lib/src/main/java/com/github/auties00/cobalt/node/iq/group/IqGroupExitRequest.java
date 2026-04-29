package com.github.auties00.cobalt.node.iq.group;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The outbound {@code <iq xmlns="w:g2" type="set" to="g.us">}
 * stanza variant — wraps a per-target {@code <group/>} or
 * {@code <linked_groups/>} list in the canonical envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebGroupExitJob")
public final class IqGroupExitRequest implements IqOperation.Request {
    /**
     * Discriminates between the {@code group} grandchild shape
     * (regular groups) and the {@code linked_groups} grandchild
     * shape (communities) carried inside the {@code <leave>} payload.
     */
    public enum Mode {
        /**
         * Leaves regular groups. Each target is encoded as
         * {@code <group id="GROUP_JID"/>}.
         */
        GROUP,
        /**
         * Leaves communities (and every sub-group linked to them).
         * Each target is encoded as
         * {@code <linked_groups parent_group_jid="COMMUNITY_JID"/>}.
         */
        LINKED_GROUPS
    }

    /**
     * The list of target JIDs to leave. For {@link Mode#GROUP} every
     * entry is a regular group JID; for {@link Mode#LINKED_GROUPS}
     * every entry is a parent-community JID.
     */
    private final List<Jid> targets;

    /**
     * The grandchild-shape discriminator that selects between the
     * {@code group}/{@code linked_groups} payload variants.
     */
    private final Mode mode;

    /**
     * Constructs a request that leaves the given list of targets in
     * the given mode.
     *
     * @param targets the list of group or community JIDs to leave;
     *                never {@code null} and never empty
     * @param mode    the grandchild-shape discriminator; never
     *                {@code null}
     * @throws NullPointerException     if any argument is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code targets} is empty
     */
    public IqGroupExitRequest(List<Jid> targets, Mode mode) {
        Objects.requireNonNull(targets, "targets cannot be null");
        Objects.requireNonNull(mode, "mode cannot be null");
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("targets cannot be empty");
        }
        this.targets = List.copyOf(targets);
        this.mode = mode;
    }

    /**
     * Returns the list of JIDs being left.
     *
     * @return an unmodifiable list of target JIDs; never {@code null}
     */
    public List<Jid> targets() {
        return targets;
    }

    /**
     * Returns the grandchild-shape discriminator.
     *
     * @return the mode; never {@code null}
     */
    public Mode mode() {
        return mode;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <leave>} payload
     *
     * @implNote {@code WAWebGroupExitJob} composes
     *           {@code WAWap.wap("iq", {to:G_US, type:"set",
     *           xmlns:"w:g2", id}, wap("leave", null, [...children]))}
     *           where each child is either
     *           {@code wap("group", {id:GROUP_JID(t)})} or
     *           {@code wap("linked_groups", {parent_group_jid:GROUP_JID(t)})}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
            exports = "leaveGroup", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
            exports = "leaveCommunity", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
            exports = "leaveCommunities", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebGroupExitJob: targets.map(t => wap("group" | "linked_groups", {id | parent_group_jid: GROUP_JID(t)}))
        var grandchildren = new ArrayList<Node>(targets.size());
        for (var target : targets) {
            NodeBuilder childBuilder;
            if (mode == Mode.GROUP) {
                childBuilder = new NodeBuilder()
                        .description("group")
                        .attribute("id", target);
            } else {
                childBuilder = new NodeBuilder()
                        .description("linked_groups")
                        .attribute("parent_group_jid", target);
            }
            grandchildren.add(childBuilder.build());
        }
        // WAWebGroupExitJob: wap("leave", null, [...children])
        var leaveNode = new NodeBuilder()
                .description("leave")
                .content(grandchildren)
                .build();
        // WAWebGroupExitJob: wap("iq", {to:G_US, type:"set", xmlns:"w:g2", id})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", JidServer.groupOrCommunity())
                .attribute("type", "set")
                .content(leaveNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqGroupExitRequest) obj;
        return Objects.equals(this.targets, that.targets)
                && this.mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targets, mode);
    }

    @Override
    public String toString() {
        return "IqGroupExitRequest[targets=" + targets
                + ", mode=" + mode + ']';
    }
}
