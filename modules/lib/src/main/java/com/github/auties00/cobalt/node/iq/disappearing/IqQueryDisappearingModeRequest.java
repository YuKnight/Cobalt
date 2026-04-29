package com.github.auties00.cobalt.node.iq.disappearing;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;

/**
 * The outbound stanza variant — bare
 * {@code <iq xmlns="disappearing_mode" type="get"/>} envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryDisappearingModeJob")
public final class IqQueryDisappearingModeRequest implements IqOperation.Request {
    /**
     * Constructs a new request.
     */
    public IqQueryDisappearingModeRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     *
     * @implNote {@code WAWebQueryDisappearingModeJob.queryDisappearingMode}:
     *           {@code wap("iq",{xmlns:"disappearing_mode",
     *           to:S_WHATSAPP_NET, type:"get", id})}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryDisappearingModeJob",
            exports = "queryDisappearingMode",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "disappearing_mode")
                .attribute("to", JidServer.user())
                .attribute("type", "get");
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
        return IqQueryDisappearingModeRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "IqQueryDisappearingModeRequest[]";
    }
}
