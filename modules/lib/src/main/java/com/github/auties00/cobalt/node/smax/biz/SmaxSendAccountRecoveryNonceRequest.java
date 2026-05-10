package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the empty
 * send-account-recovery-nonce request payload in the canonical
 * {@code <iq xmlns="fb:thrift_iq" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaAdAccountSendAccountRecoveryNonceRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaAdAccountHackBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaAdAccountBaseIQGetRequestMixin")
public final class SmaxSendAccountRecoveryNonceRequest implements SmaxOperation.Request {
    /**
     * The optional {@code from} attribute echoed onto the outbound
     * IQ via the {@code HackBaseIQGetRequestMixin}; may be
     * {@code null}.
     */
    private final Jid fromUserJid;

    /**
     * Constructs a request with no {@code from} echo.
     */
    public SmaxSendAccountRecoveryNonceRequest() {
        this(null);
    }

    /**
     * Constructs a request optionally echoing the supplied user JID
     * onto the {@code from} attribute.
     *
     * @param fromUserJid the optional user JID to echo onto the
     *                    {@code from} attribute; may be {@code null}
     */
    public SmaxSendAccountRecoveryNonceRequest(Jid fromUserJid) {
        this.fromUserJid = fromUserJid;
    }

    /**
     * Returns the optional {@code from} echo.
     *
     * @return an {@link Optional} carrying the user JID, or empty
     *         when no echo was supplied
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
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaAdAccountSendAccountRecoveryNonceRequest",
            exports = "makeSendAccountRecoveryNonceRequest", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaAdAccountHackBaseIQGetRequestMixin",
            exports = "mergeHackBaseIQGetRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaAdAccountBaseIQGetRequestMixin",
            exports = "mergeBaseIQGetRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var builder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq") // WASmaxOutBizCtwaAdAccountSendAccountRecoveryNonceRequest.makeSendAccountRecoveryNonceRequest: smax("iq", {xmlns: "fb:thrift_iq", smax_id: INT(112)})
                .attribute("to", JidServer.user()) // WASmaxOutBizCtwaAdAccountHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: to: WAWap.S_WHATSAPP_NET
                .attribute("type", "get"); // WASmaxOutBizCtwaAdAccountBaseIQGetRequestMixin.mergeBaseIQGetRequestMixin: type: "get" (id=generateId() delegated to WhatsAppClient.sendNode)
        if (fromUserJid != null) {
            builder.attribute("from", fromUserJid); // WASmaxOutBizCtwaAdAccountHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: from: OPTIONAL(USER_JID, t)
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
        var that = (SmaxSendAccountRecoveryNonceRequest) obj;
        return Objects.equals(this.fromUserJid, that.fromUserJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromUserJid);
    }

    @Override
    public String toString() {
        return "SmaxSendAccountRecoveryNonceRequest[fromUserJid=" + fromUserJid + ']';
    }
}
