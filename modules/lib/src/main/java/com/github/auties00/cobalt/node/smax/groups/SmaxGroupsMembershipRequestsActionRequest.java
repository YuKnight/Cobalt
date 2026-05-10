package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps optional {@code <approve>} and
 * {@code <reject>} children inside a
 * {@code <membership_requests_action>} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsMembershipRequestsActionRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsMembershipRequestsActionRequest implements SmaxOperation.Request {
    /**
     * The group JID hosting the pending requests.
     */
    private final Jid groupJid;

    /**
     * The participant JIDs whose pending requests should be
     * approved. May be empty.
     */
    private final List<Jid> participantsToApprove;

    /**
     * The participant JIDs whose pending requests should be
     * rejected. May be empty.
     */
    private final List<Jid> participantsToReject;

    /**
     * Constructs a request.
     *
     * @param groupJid              the group JID; never {@code null}
     * @param participantsToApprove the JIDs to approve; never
     *                              {@code null}, may be empty
     * @param participantsToReject  the JIDs to reject; never
     *                              {@code null}, may be empty
     * @throws NullPointerException     if any argument is
     *                                  {@code null}
     * @throws IllegalArgumentException if both lists are empty
     */
    public SmaxGroupsMembershipRequestsActionRequest(Jid groupJid, List<Jid> participantsToApprove,
                   List<Jid> participantsToReject) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(participantsToApprove, "participantsToApprove cannot be null");
        Objects.requireNonNull(participantsToReject, "participantsToReject cannot be null");
        if (participantsToApprove.isEmpty() && participantsToReject.isEmpty()) {
            throw new IllegalArgumentException(
                    "at least one of participantsToApprove / participantsToReject must be non-empty");
        }
        this.participantsToApprove = List.copyOf(participantsToApprove);
        this.participantsToReject = List.copyOf(participantsToReject);
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
     * Returns the JIDs to approve.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<Jid> participantsToApprove() {
        return participantsToApprove;
    }

    /**
     * Returns the JIDs to reject.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<Jid> participantsToReject() {
        return participantsToReject;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <membership_requests_action>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsMembershipRequestsActionRequest",
            exports = "makeMembershipRequestsActionRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var actionBuilder = new NodeBuilder().description("membership_requests_action");
        if (!participantsToApprove.isEmpty()) {
            var approveChildren = new ArrayList<Node>(participantsToApprove.size());
            for (var participantJid : participantsToApprove) {
                var participantNode = new NodeBuilder()
                        .description("participant")
                        .attribute("jid", participantJid)
                        .build();
                approveChildren.add(participantNode);
            }
            var approveNode = new NodeBuilder()
                    .description("approve")
                    .content(approveChildren)
                    .build();
            actionBuilder.content(approveNode);
        }
        if (!participantsToReject.isEmpty()) {
            var rejectChildren = new ArrayList<Node>(participantsToReject.size());
            for (var participantJid : participantsToReject) {
                var participantNode = new NodeBuilder()
                        .description("participant")
                        .attribute("jid", participantJid)
                        .build();
                rejectChildren.add(participantNode);
            }
            var rejectNode = new NodeBuilder()
                    .description("reject")
                    .content(rejectChildren)
                    .build();
            actionBuilder.content(rejectNode);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(actionBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsMembershipRequestsActionRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.participantsToApprove, that.participantsToApprove)
                && Objects.equals(this.participantsToReject, that.participantsToReject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, participantsToApprove, participantsToReject);
    }

    @Override
    public String toString() {
        return "SmaxGroupsMembershipRequestsActionRequest[groupJid=" + groupJid
                + ", participantsToApprove=" + participantsToApprove
                + ", participantsToReject=" + participantsToReject + ']';
    }
}
