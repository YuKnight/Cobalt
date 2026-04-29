package com.github.auties00.cobalt.node.smax.psa;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the {@code <query><blocking_status/></query>}
 * payload in the canonical {@code <iq xmlns="w:comms:chat" type="get"
 * to="s.whatsapp.net">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPsaChatBlockGetRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutPsaBaseIQGetRequestMixin")
public final class SmaxPsaChatBlockGetRequest implements SmaxOperation.Request {
    /**
     * Constructs a new request — no parameters.
     */
    public SmaxPsaChatBlockGetRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <query><blocking_status/></query>} payload
     *
     * @implNote {@code WASmaxOutPsaChatBlockGetRequest.makeChatBlockGetRequest}
     *           composes {@code WASmaxOutPsaBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over a
     *           bare {@code <iq xmlns="w:comms:chat" to="s.whatsapp.net">}
     *           with a single {@code <query><blocking_status/></query>}
     *           child.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPsaChatBlockGetRequest",
            exports = "makeChatBlockGetRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutPsaChatBlockGetRequest: smax("blocking_status", null)
        var blockingStatusNode = new NodeBuilder()
                .description("blocking_status")
                .build();
        // WASmaxOutPsaChatBlockGetRequest: smax("query", null, blocking_status)
        var queryNode = new NodeBuilder()
                .description("query")
                .content(blockingStatusNode)
                .build();
        // smax("iq", {to: S_WHATSAPP_NET, xmlns: "w:comms:chat", id: generateId(), type: "get"})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:comms:chat")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(queryNode);
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
        return SmaxPsaChatBlockGetRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "SmaxPsaChatBlockGetRequest[]";
    }
}
