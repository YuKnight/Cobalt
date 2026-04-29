package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound stanza variant — wraps a single
 * {@code <signed_user_info biz_jid="…"/>} payload in the canonical
 * {@code <iq xmlns="w:biz:catalog" type="get">} envelope.
 */
public final class IqQueryGetSignedUserInfoRequest implements IqOperation.Request {
    /**
     * The business JID whose signed user-info bundle is being requested.
     * Routed verbatim into the {@code biz_jid} attribute of the
     * {@code <signed_user_info/>} payload.
     */
    private final Jid businessJid;

    /**
     * Constructs a request for the given business.
     *
     * @param businessJid the business JID; never {@code null}
     * @throws NullPointerException if {@code businessJid} is
     *                              {@code null}
     */
    public IqQueryGetSignedUserInfoRequest(Jid businessJid) {
        this.businessJid = Objects.requireNonNull(businessJid, "businessJid cannot be null");
    }

    /**
     * Returns the business JID being queried.
     *
     * @return the business JID; never {@code null}
     */
    public Jid businessJid() {
        return businessJid;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <signed_user_info/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryGetSignedUserInfoJob",
            exports = "QueryGetSignedUserInfo", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var signedUserInfo = new NodeBuilder()
                .description("signed_user_info")
                .attribute("biz_jid", businessJid)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz:catalog")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(signedUserInfo);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqQueryGetSignedUserInfoRequest) obj;
        return Objects.equals(this.businessJid, that.businessJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessJid);
    }

    @Override
    public String toString() {
        return "IqQueryGetSignedUserInfoRequest[businessJid=" + businessJid + ']';
    }
}
