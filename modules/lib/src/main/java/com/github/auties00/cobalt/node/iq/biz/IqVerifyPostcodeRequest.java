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
public final class IqVerifyPostcodeRequest implements IqOperation.Request {
    /**
     * The business JID whose service-area should be checked. Routed
     * verbatim into the {@code biz_jid} attribute of the
     * {@code <verify_postcode/>} payload.
     */
    private final Jid businessJid;

    /**
     * The opaque encrypted-postcode blob produced by the buyer-side
     * direct-connection encryption flow.
     */
    private final String directConnectionEncryptedInfo;

    /**
     * Constructs a request.
     *
     * @param businessJid                   the business JID; never
     *                                      {@code null}
     * @param directConnectionEncryptedInfo the encrypted-postcode blob;
     *                                      never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public IqVerifyPostcodeRequest(Jid businessJid, String directConnectionEncryptedInfo) {
        this.businessJid = Objects.requireNonNull(businessJid, "businessJid cannot be null");
        this.directConnectionEncryptedInfo = Objects.requireNonNull(
                directConnectionEncryptedInfo, "directConnectionEncryptedInfo cannot be null");
    }

    /**
     * Returns the business JID being checked.
     *
     * @return the business JID; never {@code null}
     */
    public Jid businessJid() {
        return businessJid;
    }

    /**
     * Returns the encrypted-postcode blob.
     *
     * @return the blob; never {@code null}
     */
    public String directConnectionEncryptedInfo() {
        return directConnectionEncryptedInfo;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebVerifyPostcodeJob",
            exports = "VerifyPostcode", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var encryptedInfo = new NodeBuilder()
                .description("direct_connection_encrypted_info")
                .content(directConnectionEncryptedInfo)
                .build();
        var verifyPostcode = new NodeBuilder()
                .description("verify_postcode")
                .attribute("biz_jid", businessJid)
                .content(encryptedInfo)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz:catalog")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(verifyPostcode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqVerifyPostcodeRequest) obj;
        return Objects.equals(this.businessJid, that.businessJid)
                && Objects.equals(this.directConnectionEncryptedInfo, that.directConnectionEncryptedInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessJid, directConnectionEncryptedInfo);
    }

    @Override
    public String toString() {
        return "IqVerifyPostcodeRequest[businessJid=" + businessJid
                + ", directConnectionEncryptedInfo=" + directConnectionEncryptedInfo + ']';
    }
}
