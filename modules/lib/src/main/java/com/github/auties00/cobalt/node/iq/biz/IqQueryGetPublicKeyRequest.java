package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound stanza variant.
 */
public final class IqQueryGetPublicKeyRequest implements IqOperation.Request {
    /**
     * The business JID whose public key is being requested. Routed
     * verbatim into the {@code jid} attribute of the
     * {@code <public_key/>} payload.
     */
    private final Jid businessJid;

    /**
     * Constructs a request for the given business.
     *
     * @param businessJid the business JID; never {@code null}
     * @throws NullPointerException if {@code businessJid} is
     *                              {@code null}
     */
    public IqQueryGetPublicKeyRequest(Jid businessJid) {
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
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryGetPublicKeyJob",
            exports = "QueryGetPublicKey", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var publicKey = new NodeBuilder()
                .description("public_key")
                .attribute("jid", businessJid)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz:catalog")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(publicKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqQueryGetPublicKeyRequest) obj;
        return Objects.equals(this.businessJid, that.businessJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessJid);
    }

    @Override
    public String toString() {
        return "IqQueryGetPublicKeyRequest[businessJid=" + businessJid + ']';
    }
}
