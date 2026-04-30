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
     *
     * @implNote {@code WASmaxOutBizAccessTokenRequestSilentNonceRequest.makeRequestSilentNonceRequest}
     *           composes {@code WASmaxOutBizAccessTokenHackBaseIQGetRequestMixin}
     *           ({@code from=USER_JID(t)?}, {@code to=S_WHATSAPP_NET})
     *           over a bare {@code <iq xmlns="fb:thrift_iq">} with
     *           {@code id=generateId()}, {@code type="get"} from the
     *           {@code BaseIQGetRequestMixin}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizAccessTokenRequestSilentNonceRequest",
            exports = "makeRequestSilentNonceRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var builder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("to", JidServer.user())
                .attribute("type", "get");
        if (fromUserJid != null) {
            builder.attribute("from", fromUserJid);
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
