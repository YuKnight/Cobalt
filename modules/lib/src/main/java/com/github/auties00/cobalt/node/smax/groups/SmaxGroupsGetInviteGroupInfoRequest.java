package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the {@code <invite code="..."/>}
 * payload in the canonical {@code <iq xmlns="w:g2" type="get" to="g.us">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetInviteGroupInfoRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetServerMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQGetRequestMixin")
public final class SmaxGroupsGetInviteGroupInfoRequest implements SmaxOperation.Request {
    /**
     * The public group-invite code the caller wants to inspect.
     * Mandatory — the relay rejects the request when this attribute is
     * absent or empty.
     */
    private final String inviteCode;

    /**
     * Constructs a request for the given invite code.
     *
     * @param inviteCode the public invite code (the suffix of a
     *                   {@code chat.whatsapp.com/<code>} URL); never
     *                   {@code null}
     * @throws NullPointerException if {@code inviteCode} is {@code null}
     */
    public SmaxGroupsGetInviteGroupInfoRequest(String inviteCode) {
        this.inviteCode = Objects.requireNonNull(inviteCode, "inviteCode cannot be null");
    }

    /**
     * Returns the invite code carried by this request.
     *
     * @return the invite code; never {@code null}
     */
    public String inviteCode() {
        return inviteCode;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised invite payload
     *
     * @implNote {@code WASmaxOutGroupsGetInviteGroupInfoRequest.makeGetInviteGroupInfoRequest}
     *           composes {@code WASmaxOutGroupsBaseGetServerMixin}
     *           ({@code xmlns="w:g2"}, {@code to=G_US}) with
     *           {@code WASmaxOutGroupsBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over a
     *           bare {@code <invite code=CUSTOM_STRING(t)/>} child.
     *           Cobalt builds the same shape inline; the {@code id}
     *           attribute is injected by
     *           {@code WhatsAppClient.sendNode} when missing.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsGetInviteGroupInfoRequest",
            exports = "makeGetInviteGroupInfoRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutGroupsGetInviteGroupInfoRequest: smax("invite", {code: CUSTOM_STRING(t)})
        var inviteNode = new NodeBuilder()
                .description("invite")
                .attribute("code", inviteCode)
                .build();
        // WASmaxOutGroupsBaseGetServerMixin: smax("iq", {to: G_US, xmlns: "w:g2"})
        // WASmaxOutGroupsBaseIQGetRequestMixin: smax("iq", {id: generateId(), type: "get"})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", JidServer.groupOrCommunity())
                .attribute("type", "get")
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
        var that = (SmaxGroupsGetInviteGroupInfoRequest) obj;
        return Objects.equals(this.inviteCode, that.inviteCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inviteCode);
    }

    @Override
    public String toString() {
        return "SmaxGroupsGetInviteGroupInfoRequest[inviteCode=" + inviteCode + ']';
    }
}
