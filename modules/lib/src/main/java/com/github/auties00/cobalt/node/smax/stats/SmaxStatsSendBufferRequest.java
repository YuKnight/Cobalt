package com.github.auties00.cobalt.node.smax.stats;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound {@code <iq xmlns="w:stats">} stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutStatsSendBufferRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutStatsBaseIQSetRequestMixin")
public final class SmaxStatsSendBufferRequest implements SmaxOperation.Request {
    /**
     * The Unix-epoch timestamp at which the batch was sealed.
     * Routed verbatim into the {@code <add t="…"/>} attribute.
     */
    private final long addT;

    /**
     * The encoded WAM payload bytes carried as the {@code <add>}
     * child's content.
     */
    private final byte[] addElementValue;

    /**
     * Constructs a new send-buffer request.
     *
     * @param addT             the batch timestamp
     * @param addElementValue  the encoded WAM payload bytes. Never
     *                         {@code null}
     * @throws NullPointerException if {@code addElementValue} is
     *                              {@code null}
     */
    public SmaxStatsSendBufferRequest(long addT, byte[] addElementValue) {
        this.addT = addT;
        this.addElementValue = Objects.requireNonNull(addElementValue,
                "addElementValue cannot be null");
    }

    /**
     * Returns the batch timestamp.
     *
     * @return the timestamp
     */
    public long addT() {
        return addT;
    }

    /**
     * Returns the encoded WAM payload bytes.
     *
     * @return the bytes. Never {@code null}
     */
    public byte[] addElementValue() {
        return addElementValue;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <add>} child
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutStatsSendBufferRequest",
            exports = "makeSendBufferRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var addNode = new NodeBuilder()
                .description("add")
                .attribute("t", addT)
                .content(addElementValue)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:stats")
                .attribute("to", Jid.userServer())
                .attribute("type", "set")
                .content(addNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxStatsSendBufferRequest) obj;
        return this.addT == that.addT
                && Arrays.equals(this.addElementValue, that.addElementValue);
    }

    @Override
    public int hashCode() {
        var result = Long.hashCode(addT);
        result = 31 * result + Arrays.hashCode(addElementValue);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxStatsSendBufferRequest[addT=" + addT
                + ", addElementValue=" + Arrays.toString(addElementValue) + ']';
    }
}
