package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps a bare empty
 * {@code <linked_accounts/>} child in the canonical
 * {@code <iq xmlns="fb:thrift_iq" type="get">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizLinkingGetLinkedAccountsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizLinkingHackBaseIQGetRequestMixin")
public final class SmaxGetLinkedAccountsRequest implements SmaxOperation.Request {
    /**
     * Constructs a new request — the request carries no attributes
     * other than the static envelope.
     */
    public SmaxGetLinkedAccountsRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         empty {@code <linked_accounts/>} payload
     *
     * @implNote {@code WASmaxOutBizLinkingGetLinkedAccountsRequest.makeGetLinkedAccountsRequest}
     *           composes
     *           {@code WASmaxOutBizLinkingHackBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over a
     *           bare {@code <iq xmlns="fb:thrift_iq" smax_id=42>} root
     *           that carries a single empty
     *           {@code <linked_accounts/>} child.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizLinkingGetLinkedAccountsRequest",
            exports = "makeGetLinkedAccountsRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var linkedAccountsNode = new NodeBuilder()
                .description("linked_accounts")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("type", "get")
                .content(linkedAccountsNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return SmaxGetLinkedAccountsRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "SmaxGetLinkedAccountsRequest[]";
    }
}
