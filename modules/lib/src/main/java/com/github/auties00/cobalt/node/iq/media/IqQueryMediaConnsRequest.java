package com.github.auties00.cobalt.node.iq.media;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.util.RandomIdUtils;

/**
 * The outbound {@code <iq xmlns="w:m" type="set">} stanza variant —
 * wraps a single bare {@code <media_conn/>} payload.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryMediaConnsJob")
public final class IqQueryMediaConnsRequest implements IqOperation.Request {
    /**
     * Constructs a new query-media-conn request.
     */
    public IqQueryMediaConnsRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <media_conn/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryMediaConnsJob",
            exports = "queryMediaConn", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var mediaConnNode = new NodeBuilder()
                .description("media_conn")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("id", RandomIdUtils.newId())
                .attribute("xmlns", "w:m")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(mediaConnNode);
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
        return IqQueryMediaConnsRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "IqQueryMediaConnsRequest[]";
    }
}
