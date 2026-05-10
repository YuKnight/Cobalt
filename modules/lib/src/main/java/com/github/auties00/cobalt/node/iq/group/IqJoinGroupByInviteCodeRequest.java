package com.github.auties00.cobalt.node.iq.group;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound stanza variant — wraps a single
 * {@code <invite code=INVITE_CODE/>} child in the canonical
 * {@code <iq xmlns="w:g2" type="set" to="g.us">} envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebGroupInviteJob")
public final class IqJoinGroupByInviteCodeRequest implements IqOperation.Request {
    /**
     * The invite code being redeemed.
     */
    private final String code;

    /**
     * {@code true} when the caller expects the group to require
     * moderator approval (and thus a
     * {@code <membership_approval_request>} reply); {@code false}
     * for an immediate admission ({@code <group>} reply).
     */
    private final boolean expectsMembershipApproval;

    /**
     * Constructs a new request.
     *
     * @param code                      the invite code; never
     *                                  {@code null}
     * @param expectsMembershipApproval {@code true} when the caller
     *                                  expects approval-gated entry
     * @throws NullPointerException if {@code code} is {@code null}
     */
    public IqJoinGroupByInviteCodeRequest(String code, boolean expectsMembershipApproval) {
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.expectsMembershipApproval = expectsMembershipApproval;
    }

    /**
     * Returns the invite code being redeemed.
     *
     * @return the code; never {@code null}
     */
    public String code() {
        return code;
    }

    /**
     * Returns whether the caller expects approval-gated entry.
     *
     * @return {@code true} when the caller expects a
     *         {@code <membership_approval_request>} reply,
     *         {@code false} for an immediate {@code <group>} reply
     */
    public boolean expectsMembershipApproval() {
        return expectsMembershipApproval;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <invite>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebGroupInviteJob",
            exports = "joinGroupViaInvite",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebGroupInviteJob: wap("invite",{code:CUSTOM_STRING(e)})
        var inviteNode = new NodeBuilder()
                .description("invite")
                .attribute("code", code)
                .build();
        // WAWebGroupInviteJob: wap("iq",{type:"set", xmlns:"w:g2", to:G_US, id}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", JidServer.groupOrCommunity())
                .attribute("type", "set")
                .content(inviteNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqJoinGroupByInviteCodeRequest) obj;
        return this.expectsMembershipApproval == that.expectsMembershipApproval
                && Objects.equals(this.code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, expectsMembershipApproval);
    }

    @Override
    public String toString() {
        return "IqJoinGroupByInviteCodeRequest[code=" + code
                + ", expectsMembershipApproval=" + expectsMembershipApproval + ']';
    }
}
