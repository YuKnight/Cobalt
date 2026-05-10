package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the empty silent-nonce request
 * payload in the canonical
 * {@code <iq xmlns="fb:thrift_iq" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizAccessTokenRequestSilentNonceRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizAccessTokenHackBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBizAccessTokenBaseIQGetRequestMixin")
public final class SmaxRequestSilentNonceRequest implements SmaxOperation.Request {
    /**
     * The optional {@code from} attribute echoed onto the outbound IQ
     * via the {@code HackBaseIQGetRequestMixin}. The active user JID
     * is the only legal value; {@code null} omits the attribute.
     */
    private final Jid fromUserJid;

    /**
     * Constructs a request with no {@code from} echo.
     */
    public SmaxRequestSilentNonceRequest() {
        this(null);
    }

    /**
     * Constructs a request optionally echoing the supplied user JID
     * onto the {@code from} attribute.
     *
     * @param fromUserJid the optional user JID to echo onto the
     *                    {@code from} attribute; may be {@code null}
     */
    public SmaxRequestSilentNonceRequest(Jid fromUserJid) {
        this.fromUserJid = fromUserJid;
    }

    /**
     * Returns the optional {@code from} echo.
     *
     * @return an {@link Optional} carrying the user JID, or empty when
     *         no echo was supplied
     */
    public Optional<Jid> fromUserJid() {
        return Optional.ofNullable(fromUserJid);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizAccessTokenRequestSilentNonceRequest",
            exports = "makeRequestSilentNonceRequest", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizAccessTokenHackBaseIQGetRequestMixin",
            exports = "mergeHackBaseIQGetRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizAccessTokenBaseIQGetRequestMixin",
            exports = "mergeBaseIQGetRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var builder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq") // WASmaxOutBizAccessTokenRequestSilentNonceRequest.makeRequestSilentNonceRequest: smax("iq", {xmlns: "fb:thrift_iq", smax_id: INT(118)})
                .attribute("to", JidServer.user()) // WASmaxOutBizAccessTokenHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: to: WAWap.S_WHATSAPP_NET
                .attribute("type", "get"); // WASmaxOutBizAccessTokenBaseIQGetRequestMixin.mergeBaseIQGetRequestMixin: type: "get" (id: generateId() is set by WhatsAppClient.sendNode dispatcher)
        if (fromUserJid != null) {
            builder.attribute("from", fromUserJid); // WASmaxOutBizAccessTokenHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: from: OPTIONAL(USER_JID, t)
        }
        return builder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxRequestSilentNonceRequest) obj;
        return Objects.equals(this.fromUserJid, that.fromUserJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromUserJid);
    }

    @Override
    public String toString() {
        return "SmaxRequestSilentNonceRequest[fromUserJid=" + fromUserJid + ']';
    }
}
