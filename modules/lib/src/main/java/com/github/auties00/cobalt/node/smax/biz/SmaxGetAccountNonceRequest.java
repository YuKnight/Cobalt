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
 * The outbound stanza variant. Wraps the optional {@code <identifier>}
 * child inside the canonical
 * {@code <iq xmlns="fb:thrift_iq" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizLinkingGetAccountNonceRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizLinkingHackBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBizLinkingBaseIQGetRequestMixin")
public final class SmaxGetAccountNonceRequest implements SmaxOperation.Request {
    /**
     * The optional {@code scope} attribute of the {@code <identifier>}
     * child; {@code null} omits the child entirely.
     */
    private final String identifierScope;

    /**
     * The optional {@code from} attribute echoed onto the outbound IQ
     * via the {@code HackBaseIQGetRequestMixin}. The active user JID
     * is the only legal value; {@code null} omits the attribute.
     */
    private final Jid fromUserJid;

    /**
     * Constructs a request without an {@code <identifier>} child and no
     * {@code from} echo.
     */
    public SmaxGetAccountNonceRequest() {
        this(null, null);
    }

    /**
     * Constructs a request optionally carrying an
     * {@code <identifier scope="..."/>} child, with no {@code from} echo.
     *
     * @param identifierScope the {@code scope} attribute; may be
     *                        {@code null} to omit the child
     */
    public SmaxGetAccountNonceRequest(String identifierScope) {
        this(identifierScope, null);
    }

    /**
     * Constructs a request optionally carrying an
     * {@code <identifier scope="..."/>} child and optionally echoing the
     * supplied user JID onto the {@code from} attribute.
     *
     * @param identifierScope the {@code scope} attribute; may be
     *                        {@code null} to omit the child
     * @param fromUserJid     the optional user JID to echo onto the
     *                        {@code from} attribute; may be {@code null}
     */
    public SmaxGetAccountNonceRequest(String identifierScope, Jid fromUserJid) {
        this.identifierScope = identifierScope;
        this.fromUserJid = fromUserJid;
    }

    /**
     * Returns the optional identifier scope.
     *
     * @return an {@link Optional} carrying the scope, or empty when the
     *         child is omitted
     */
    public Optional<String> identifierScope() {
        return Optional.ofNullable(identifierScope);
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
     *         optional {@code <identifier/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizLinkingGetAccountNonceRequest",
            exports = "makeGetAccountNonceRequest", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizLinkingHackBaseIQGetRequestMixin",
            exports = "mergeHackBaseIQGetRequestMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizLinkingBaseIQGetRequestMixin",
            exports = "mergeBaseIQGetRequestMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var builder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq") // WASmaxOutBizLinkingGetAccountNonceRequest.makeGetAccountNonceRequest: smax("iq", {xmlns: "fb:thrift_iq", smax_id: INT(12)})
                .attribute("to", JidServer.user()) // WASmaxOutBizLinkingHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: to: WAWap.S_WHATSAPP_NET
                .attribute("type", "get"); // WASmaxOutBizLinkingBaseIQGetRequestMixin.mergeBaseIQGetRequestMixin: type: "get" (id=generateId() delegated to WhatsAppClient.sendNode)
        if (fromUserJid != null) {
            builder.attribute("from", fromUserJid); // WASmaxOutBizLinkingHackBaseIQGetRequestMixin.mergeHackBaseIQGetRequestMixin: from: OPTIONAL(USER_JID, t)
        }
        if (identifierScope != null) {
            var identifierNode = new NodeBuilder()
                    .description("identifier")
                    .attribute("scope", identifierScope)
                    .build();
            builder.content(identifierNode);
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
        var that = (SmaxGetAccountNonceRequest) obj;
        return Objects.equals(this.identifierScope, that.identifierScope)
                && Objects.equals(this.fromUserJid, that.fromUserJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifierScope, fromUserJid);
    }

    @Override
    public String toString() {
        return "SmaxGetAccountNonceRequest[identifierScope=" + identifierScope
                + ", fromUserJid=" + fromUserJid + ']';
    }
}
