package com.github.auties00.cobalt.node.smax.mdcompanion;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqErrorResponseMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the bare
 * {@code <link_code_companion_reg stage="get_country_code"/>} payload
 * in the canonical {@code <iq xmlns="md" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMdGetCountryCodeRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutMdBaseIQGetRequestMixin")
public final class SmaxMdGetCountryCodeRequest implements SmaxOperation.Request {
    /**
     * Constructs a new request. The stanza carries no parameters.
     */
    public SmaxMdGetCountryCodeRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutMdGetCountryCodeRequest",
            exports = "makeGetCountryCodeRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var regNode = new NodeBuilder()
                .description("link_code_companion_reg")
                .attribute("stage", "get_country_code")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "md")
                .attribute("to", "s.whatsapp.net")
                .attribute("type", "get")
                .content(regNode);
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
        return SmaxMdGetCountryCodeRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "SmaxMdGetCountryCodeRequest[]";
    }
}
