package com.github.auties00.cobalt.node.smax.passivemode;

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
 * The outbound stanza variant — wraps the bare {@code <passive/>}
 * payload in the canonical {@code <iq xmlns="passive" type="set"
 * to="s.whatsapp.net">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPassiveModePassiveIQRequest")
public final class SmaxPassiveModePassiveIQRequest implements SmaxOperation.Request {

    /**
     * Constructs a new request.
     */
    public SmaxPassiveModePassiveIQRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <passive/>} payload
     *
     * @implNote {@code WASmaxOutPassiveModePassiveIQRequest.makePassiveIQRequest}
     *           emits {@code <iq id=generateId() type="set"
     *           xmlns="passive" to=S_WHATSAPP_NET><passive/></iq>}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPassiveModePassiveIQRequest",
            exports = "makePassiveIQRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var passiveNode = new NodeBuilder()
                .description("passive")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "passive")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(passiveNode);
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
        return SmaxPassiveModePassiveIQRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "SmaxPassiveModePassiveIQRequest[]";
    }
}
