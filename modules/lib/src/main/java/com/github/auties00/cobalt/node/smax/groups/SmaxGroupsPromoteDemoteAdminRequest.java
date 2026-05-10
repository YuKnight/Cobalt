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
 * The outbound stanza variant — wraps the {@code <promote>} /
 * {@code <demote>} children inside an {@code <admin>} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsPromoteDemoteAdminRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsPromoteDemoteAdminRequest implements SmaxOperation.Request {
    /**
     * The community parent-group JID.
     */
    private final Jid groupJid;

    /**
     * The participants to promote to community admin. May be empty.
     */
    private final List<Jid> participantsToPromote;

    /**
     * The participants to demote from community admin. May be
     * empty.
     */
    private final List<Jid> participantsToDemote;

    /**
     * Constructs a request.
     *
     * @param groupJid              the community parent-group JID;
     *                              never {@code null}
     * @param participantsToPromote the JIDs to promote; never
     *                              {@code null}, may be empty
     * @param participantsToDemote  the JIDs to demote; never
     *                              {@code null}, may be empty
     * @throws NullPointerException     if any argument is
     *                                  {@code null}
     * @throws IllegalArgumentException if both lists are empty
     */
    public SmaxGroupsPromoteDemoteAdminRequest(Jid groupJid, List<Jid> participantsToPromote,
                   List<Jid> participantsToDemote) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(participantsToPromote, "participantsToPromote cannot be null");
        Objects.requireNonNull(participantsToDemote, "participantsToDemote cannot be null");
        if (participantsToPromote.isEmpty() && participantsToDemote.isEmpty()) {
            throw new IllegalArgumentException(
                    "at least one of participantsToPromote / participantsToDemote must be non-empty");
        }
        this.participantsToPromote = List.copyOf(participantsToPromote);
        this.participantsToDemote = List.copyOf(participantsToDemote);
    }

    /**
     * Returns the community parent-group JID.
     *
     * @return the group JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns the JIDs to promote.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<Jid> participantsToPromote() {
        return participantsToPromote;
    }

    /**
     * Returns the JIDs to demote.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<Jid> participantsToDemote() {
        return participantsToDemote;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <admin>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsPromoteDemoteAdminRequest",
            exports = "makePromoteDemoteAdminRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var adminBuilder = new NodeBuilder().description("admin");
        if (!participantsToPromote.isEmpty()) {
            var promoteChildren = new ArrayList<Node>(participantsToPromote.size());
            for (var participantJid : participantsToPromote) {
                var participantNode = new NodeBuilder()
                        .description("participant")
                        .attribute("jid", participantJid)
                        .build();
                promoteChildren.add(participantNode);
            }
            var promoteNode = new NodeBuilder()
                    .description("promote")
                    .content(promoteChildren)
                    .build();
            adminBuilder.content(promoteNode);
        }
        if (!participantsToDemote.isEmpty()) {
            var demoteChildren = new ArrayList<Node>(participantsToDemote.size());
            for (var participantJid : participantsToDemote) {
                var participantNode = new NodeBuilder()
                        .description("participant")
                        .attribute("jid", participantJid)
                        .build();
                demoteChildren.add(participantNode);
            }
            var demoteNode = new NodeBuilder()
                    .description("demote")
                    .content(demoteChildren)
                    .build();
            adminBuilder.content(demoteNode);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(adminBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsPromoteDemoteAdminRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.participantsToPromote, that.participantsToPromote)
                && Objects.equals(this.participantsToDemote, that.participantsToDemote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, participantsToPromote, participantsToDemote);
    }

    @Override
    public String toString() {
        return "SmaxGroupsPromoteDemoteAdminRequest[groupJid=" + groupJid
                + ", participantsToPromote=" + participantsToPromote
                + ", participantsToDemote=" + participantsToDemote + ']';
    }
}
