package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps a bare empty
 * {@code <linked_accounts/>} child in the canonical
 * {@code <iq xmlns="fb:thrift_iq" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizLinkingGetLinkedAccountsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizLinkingHackBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBizLinkingBaseIQGetRequestMixin")
public final class SmaxGetLinkedAccountsRequest implements SmaxOperation.Request {
    /**
     * The optional {@code from} attribute echoed onto the outbound IQ
     * via the {@code HackBaseIQGetRequestMixin}. The active user JID
     * is the only legal value; {@code null} omits the attribute.
     */
    private final Jid fromUserJid;

    /**
     * Constructs a new request with no {@code from} echo. The request
     * carries no attributes other than the static envelope.
     */
    public SmaxGetLinkedAccountsRequest() {
        this(null);
    }

    /**
     * Constructs a new request optionally echoing the supplied user JID
     * onto the {@code from} attribute.
     *
     * @param fromUserJid the optional user JID to echo onto the
     *                    {@code from} attribute; may be {@code null}
     */
    public SmaxGetLinkedAccountsRequest(Jid fromUserJid) {
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
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         empty {@code <linked_accounts/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizLinkingGetLinkedAccountsRequest",
            exports = "makeGetLinkedAccountsRequest", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizLinkingHackBaseIQGetRequestMixin",
            exports = "mergeHackBaseIQGetRequestMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizLinkingBaseIQGetRequestMixin",
            exports = "mergeBaseIQGetRequestMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var linkedAccountsNode = new NodeBuilder()
                .description("linked_accounts")
                .build();
        var builder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq") // WASmaxOutBizLinkingGetLinkedAccountsRequest.makeGetLinkedAccountsRequest: smax("iq", {xmlns: "fb:thrift_iq", smax_id: INT(42)})
                .attribute("to", JidServer.user()) // WASmaxOutBizLinkingHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: to: WAWap.S_WHATSAPP_NET
                .attribute("type", "get") // WASmaxOutBizLinkingBaseIQGetRequestMixin.mergeBaseIQGetRequestMixin: type: "get" (id=generateId() delegated to WhatsAppClient.sendNode)
                .content(linkedAccountsNode);
        if (fromUserJid != null) {
            builder.attribute("from", fromUserJid); // WASmaxOutBizLinkingHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: from: OPTIONAL(USER_JID, t)
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
        var that = (SmaxGetLinkedAccountsRequest) obj;
        return Objects.equals(this.fromUserJid, that.fromUserJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromUserJid);
    }

    @Override
    public String toString() {
        return "SmaxGetLinkedAccountsRequest[fromUserJid=" + fromUserJid + ']';
    }
}
