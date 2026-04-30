package com.github.auties00.cobalt.node.smax.waffle;

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
 * The outbound stanza variant. Wraps a {@code <timestamp/>} child in
 * the canonical {@code <iq xmlns="waffle" smax_id="142" type="get"
 * to="s.whatsapp.net">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleStateExistsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleBaseIQGetRequestMixin")
public final class SmaxWaffleStateExistsRequest implements SmaxOperation.Request {
    /**
     * The client's wall-clock at request time, in seconds since the
     * UNIX epoch.
     */
    private final long timestamp;

    /**
     * Constructs a request stamped at the given timestamp.
     *
     * @param timestamp the UNIX epoch seconds at request time
     */
    public SmaxWaffleStateExistsRequest(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the request timestamp.
     *
     * @return the UNIX epoch seconds
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <timestamp/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutWaffleStateExistsRequest",
            exports = "makeStateExistsRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var timestampNode = new NodeBuilder()
                .description("timestamp")
                .content(timestamp)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "waffle")
                .attribute("smax_id", 142)
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(timestampNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxWaffleStateExistsRequest) obj;
        return this.timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(timestamp);
    }

    @Override
    public String toString() {
        return "SmaxWaffleStateExistsRequest[timestamp=" + timestamp + ']';
    }
}
