package com.github.auties00.cobalt.node.iq.encrypt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.util.RandomIdUtils;

/**
 * The outbound {@code <iq xmlns="encrypt" type="get">} stanza variant
 * — wraps a single bare {@code <digest/>} payload.
 */
@WhatsAppWebModule(moduleName = "WAWebDigestKeyJob")
public final class IqDigestKeyRequest implements IqOperation.Request {
    /**
     * Constructs a new digest-key request.
     */
    public IqDigestKeyRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <digest/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDigestKeyJob",
            exports = "digestKey", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var digestNode = new NodeBuilder()
                .description("digest")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("id", RandomIdUtils.newId())
                .attribute("xmlns", "encrypt")
                .attribute("type", "get")
                .attribute("to", JidServer.user())
                .content(digestNode);
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
        return IqDigestKeyRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "IqDigestKeyRequest[]";
    }
}
