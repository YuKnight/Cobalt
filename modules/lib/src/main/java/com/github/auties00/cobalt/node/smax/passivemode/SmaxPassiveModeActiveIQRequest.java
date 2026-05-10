package com.github.auties00.cobalt.node.smax.passivemode;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;

/**
 * The outbound stanza variant. Wraps the bare {@code <active/>} payload
 * in the canonical {@code <iq xmlns="passive" type="set"
 * to="s.whatsapp.net">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPassiveModeActiveIQRequest")
public final class SmaxPassiveModeActiveIQRequest implements SmaxOperation.Request {

    /**
     * Constructs a new request.
     */
    public SmaxPassiveModeActiveIQRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <active/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPassiveModeActiveIQRequest",
            exports = "makeActiveIQRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var activeNode = new NodeBuilder()
                .description("active")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "passive")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(activeNode);
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
        return SmaxPassiveModeActiveIQRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "SmaxPassiveModeActiveIQRequest[]";
    }
}
