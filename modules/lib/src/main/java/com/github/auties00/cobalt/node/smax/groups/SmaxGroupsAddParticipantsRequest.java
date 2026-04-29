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
 * The outbound stanza variant — wraps an {@code <add>} payload
 * carrying one {@code <participant jid="..."/>} entry per candidate
 * in the canonical {@code <iq xmlns="w:g2" type="set" to=GROUP_JID>}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsAddParticipantsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsAddParticipantsRequest implements SmaxOperation.Request {
    /**
     * The group JID to which participants are being added. Routed
     * verbatim into the IQ's {@code to} attribute.
     */
    private final Jid groupJid;

    /**
     * The list of candidate participant JIDs. Mandatory and
     * non-empty; the relay enforces a 1..1024 cardinality on the
     * {@code <participant>} children.
     */
    private final List<Jid> participants;

    /**
     * Constructs a request for the given group and participants.
     *
     * @param groupJid     the group JID; never {@code null}
     * @param participants the participant JIDs to add; never
     *                     {@code null} and must contain at least
     *                     one entry
     * @throws NullPointerException     if {@code groupJid} or
     *                                  {@code participants} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code participants} is
     *                                  empty
     */
    public SmaxGroupsAddParticipantsRequest(Jid groupJid, List<Jid> participants) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(participants, "participants cannot be null");
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("participants cannot be empty");
        }
        this.participants = List.copyOf(participants);
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
     * Returns the candidate participant JIDs.
     *
     * @return an unmodifiable list of participant JIDs; never
     *         {@code null} and never empty
     */
    public List<Jid> participants() {
        return participants;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <add>} payload
     *
     * @implNote {@code WASmaxOutGroupsAddParticipantsRequest.makeAddParticipantsRequest}
     *           composes {@code WASmaxOutGroupsBaseSetGroupMixin}
     *           ({@code xmlns="w:g2"}, {@code to=GROUP_JID(t)},
     *           {@code id=generateId()}, {@code type="set"}) over an
     *           {@code <add>REPEATED_CHILD(participant, 1, 1024)
     *           </add>} child. Each {@code <participant>} carries a
     *           mandatory {@code jid} plus optional {@code phone_number}
     *           and {@code username} echoes; Cobalt only emits the
     *           mandatory {@code jid} since the optional attrs are
     *           consumed solely by the relay's permission-token mixin
     *           (not validated client-side).
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsAddParticipantsRequest",
            exports = "makeAddParticipantsRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutGroupsAddParticipantsRequest: smax("participant", {jid: JID(t)})
        var participantNodes = new ArrayList<Node>(participants.size());
        for (var participantJid : participants) {
            var participantNode = new NodeBuilder()
                    .description("participant")
                    .attribute("jid", participantJid)
                    .build();
            participantNodes.add(participantNode);
        }
        // WASmaxOutGroupsAddParticipantsRequest: smax("add", null, REPEATED_CHILD(participant, 1, 1024))
        var addNode = new NodeBuilder()
                .description("add")
                .content(participantNodes)
                .build();
        // WASmaxOutGroupsBaseSetGroupMixin: smax("iq", {to: GROUP_JID(t), xmlns: "w:g2"})
        // WASmaxOutGroupsBaseIQSetRequestMixin: smax("iq", {id: generateId(), type: "set"})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(addNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsAddParticipantsRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, participants);
    }

    @Override
    public String toString() {
        return "SmaxGroupsAddParticipantsRequest[groupJid=" + groupJid
                + ", participants=" + participants + ']';
    }
}
