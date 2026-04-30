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
 * The outbound stanza variant — wraps a {@code <remove>} payload
 * carrying one {@code <participant jid="..."/>} entry per target.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsRemoveParticipantsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsRemoveParticipantsRequest implements SmaxOperation.Request {
    /**
     * The group JID from which participants are being removed.
     */
    private final Jid groupJid;

    /**
     * The list of target participant JIDs. Mandatory and non-empty;
     * the relay enforces a 1..1024 cardinality on the
     * {@code <participant>} children.
     */
    private final List<Jid> participants;

    /**
     * When {@code true}, the relay also drops the listed participants
     * from every linked sub-group of a community parent group.
     */
    private final boolean removeLinkedGroups;

    /**
     * Constructs a request for the given group and participants.
     *
     * @param groupJid           the group JID; never {@code null}
     * @param participants       the participant JIDs to remove;
     *                           never {@code null} and must contain
     *                           at least one entry
     * @param removeLinkedGroups whether to cascade the removal across
     *                           every linked sub-group (community
     *                           parent groups only)
     * @throws NullPointerException     if {@code groupJid} or
     *                                  {@code participants} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code participants} is
     *                                  empty
     */
    public SmaxGroupsRemoveParticipantsRequest(Jid groupJid, List<Jid> participants, boolean removeLinkedGroups) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(participants, "participants cannot be null");
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("participants cannot be empty");
        }
        this.participants = List.copyOf(participants);
        this.removeLinkedGroups = removeLinkedGroups;
    }

    /**
     * Returns the target group JID.
     *
     * @return the group JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns the participant JIDs to remove.
     *
     * @return an unmodifiable list; never {@code null} and never
     *         empty
     */
    public List<Jid> participants() {
        return participants;
    }

    /**
     * Returns whether the linked-groups cascade is enabled.
     *
     * @return {@code true} when the {@code linked_groups="true"}
     *         attribute is emitted; {@code false} otherwise
     */
    public boolean removeLinkedGroups() {
        return removeLinkedGroups;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <remove>} payload
     *
     * @implNote {@code WASmaxOutGroupsRemoveParticipantsRequest.makeRemoveParticipantsRequest}
     *           composes
     *           {@code <remove linked_groups=OPTIONAL_LITERAL("true", r)>
     *           REPEATED_CHILD(participant, 1, 1024)</remove>}.
     *           Cobalt emits {@code linked_groups="true"} only when
     *           the flag is set; absence of the attribute is the
     *           default per the WA Web mixin.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsRemoveParticipantsRequest",
            exports = "makeRemoveParticipantsRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var participantNodes = new ArrayList<Node>(participants.size());
        for (var participantJid : participants) {
            var participantNode = new NodeBuilder()
                    .description("participant")
                    .attribute("jid", participantJid)
                    .build();
            participantNodes.add(participantNode);
        }
        var removeBuilder = new NodeBuilder()
                .description("remove")
                .content(participantNodes);
        if (removeLinkedGroups) {
            removeBuilder.attribute("linked_groups", "true");
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(removeBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsRemoveParticipantsRequest) obj;
        return this.removeLinkedGroups == that.removeLinkedGroups
                && Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, participants, removeLinkedGroups);
    }

    @Override
    public String toString() {
        return "SmaxGroupsRemoveParticipantsRequest[groupJid=" + groupJid
                + ", participants=" + participants
                + ", removeLinkedGroups=" + removeLinkedGroups + ']';
    }
}
