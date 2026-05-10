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
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetMembershipApprovalRequestsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetMembershipApprovalRequestsRequestorFetchMixin")
public final class SmaxGroupsGetMembershipApprovalRequestsRequest implements SmaxOperation.Request {
    /**
     * The group whose pending approval requests are being queried.
     */
    private final Jid groupJid;

    /**
     * When {@code true}, asks the relay to populate the
     * {@code requestor}/{@code requestor_pn}/{@code requestor_username}
     * attributes on every {@code <membership_approval_request>}
     * entry — the "rich" projection used by sub-group admins
     * handling community-link join requests.
     */
    private final boolean requestorFetch;

    /**
     * Constructs a request without the {@code requestor_fetch}
     * projection.
     *
     * @param groupJid the group JID; never {@code null}
     * @throws NullPointerException if {@code groupJid} is
     *                              {@code null}
     */
    public SmaxGroupsGetMembershipApprovalRequestsRequest(Jid groupJid) {
        this(groupJid, false);
    }

    /**
     * Constructs a fully-parametrised request.
     *
     * @param groupJid       the group JID; never {@code null}
     * @param requestorFetch whether the relay should populate the
     *                       rich requestor projection
     * @throws NullPointerException if {@code groupJid} is
     *                              {@code null}
     */
    public SmaxGroupsGetMembershipApprovalRequestsRequest(Jid groupJid, boolean requestorFetch) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        this.requestorFetch = requestorFetch;
    }

    /**
     * Returns the group JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns whether the rich requestor projection is requested.
     *
     * @return {@code true} when the caller asked the relay to
     *         resolve {@code requestor}/{@code requestor_pn}/
     *         {@code requestor_username}; {@code false} otherwise
     */
    public boolean requestorFetch() {
        return requestorFetch;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         {@code <membership_approval_requests/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsGetMembershipApprovalRequestsRequest",
            exports = "makeGetMembershipApprovalRequestsRequest",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var payloadBuilder = new NodeBuilder()
                .description("membership_approval_requests");
        if (requestorFetch) {
            payloadBuilder.attribute("requestor_fetch", "true");
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "get")
                .content(payloadBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsGetMembershipApprovalRequestsRequest) obj;
        return this.requestorFetch == that.requestorFetch
                && Objects.equals(this.groupJid, that.groupJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, requestorFetch);
    }

    @Override
    public String toString() {
        return "SmaxGroupsGetMembershipApprovalRequestsRequest[groupJid=" + groupJid
                + ", requestorFetch=" + requestorFetch + ']';
    }
}
