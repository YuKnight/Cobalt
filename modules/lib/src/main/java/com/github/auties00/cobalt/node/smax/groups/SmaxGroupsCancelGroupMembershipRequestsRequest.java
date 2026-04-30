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
 * The outbound stanza variant — wraps a
 * {@code <cancel_membership_requests>} payload.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsCancelGroupMembershipRequestsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsCancelGroupMembershipRequestsRequest implements SmaxOperation.Request {
    /**
     * The group JID hosting the pending requests.
     */
    private final Jid groupJid;

    /**
     * The participant JIDs whose pending requests should be
     * cancelled. Mandatory and non-empty; the relay enforces a
     * 1..19999 cardinality on the {@code <participant>} children.
     */
    private final List<Jid> participants;

    /**
     * Constructs a request.
     *
     * @param groupJid     the group JID; never {@code null}
     * @param participants the participant JIDs; never {@code null}
     *                     and must contain at least one entry
     * @throws NullPointerException     if any argument is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code participants} is
     *                                  empty
     */
    public SmaxGroupsCancelGroupMembershipRequestsRequest(Jid groupJid, List<Jid> participants) {
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
     * Returns the participant JIDs whose pending requests are being
     * cancelled.
     *
     * @return an unmodifiable list; never {@code null} and never
     *         empty
     */
    public List<Jid> participants() {
        return participants;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <cancel_membership_requests>} payload
     *
     * @implNote {@code WASmaxOutGroupsCancelGroupMembershipRequestsRequest.makeCancelGroupMembershipRequestsRequest}
     *           composes
     *           {@code <iq><cancel_membership_requests>REPEATED_CHILD(participant,
     *           1, 19999)</cancel_membership_requests></iq>}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsCancelGroupMembershipRequestsRequest",
            exports = "makeCancelGroupMembershipRequestsRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var participantNodes = new ArrayList<Node>(participants.size());
        for (var participantJid : participants) {
            var participantNode = new NodeBuilder()
                    .description("participant")
                    .attribute("jid", participantJid)
                    .build();
            participantNodes.add(participantNode);
        }
        var cancelNode = new NodeBuilder()
                .description("cancel_membership_requests")
                .content(participantNodes)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(cancelNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsCancelGroupMembershipRequestsRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, participants);
    }

    @Override
    public String toString() {
        return "SmaxGroupsCancelGroupMembershipRequestsRequest[groupJid=" + groupJid
                + ", participants=" + participants + ']';
    }
}
