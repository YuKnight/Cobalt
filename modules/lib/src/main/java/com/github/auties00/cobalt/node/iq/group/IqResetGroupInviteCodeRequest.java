package com.github.auties00.cobalt.node.iq.group;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound {@code <iq xmlns="w:g2" type="set" to="GROUP_JID">}
 * stanza variant — wraps a bare {@code <invite/>} payload.
 */
@WhatsAppWebModule(moduleName = "WAWebGroupInviteJob")
public final class IqResetGroupInviteCodeRequest implements IqOperation.Request {
    /**
     * The group whose invite code is being rotated. Routed
     * verbatim into the IQ envelope's {@code to} attribute.
     */
    private final Jid groupJid;

    /**
     * Constructs a request for the given group.
     *
     * @param groupJid the group whose invite code is to be rotated;
     *                 never {@code null}
     * @throws NullPointerException if {@code groupJid} is
     *                              {@code null}
     */
    public IqResetGroupInviteCodeRequest(Jid groupJid) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
    }

    /**
     * Returns the group whose invite code is being rotated.
     *
     * @return the group JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the bare {@code <invite/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebGroupInviteJob",
            exports = "resetGroupInviteCode", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebGroupInviteJob: wap("invite", null)
        var invitePayload = new NodeBuilder()
                .description("invite")
                .build();
        // WAWebGroupInviteJob: wap("iq", {type:"set", xmlns:"w:g2", to:GROUP_JID(t), id}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(invitePayload);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqResetGroupInviteCodeRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid);
    }

    @Override
    public String toString() {
        return "IqResetGroupInviteCodeRequest[groupJid=" + groupJid + ']';
    }
}
