package com.github.auties00.cobalt.node.smax.offlinebatch;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;

/**
 * The outbound stanza variant — wraps the {@code <offline_batch
 * count/>} payload in the bare {@code <ib>} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutOfflineBatchRequest")
public final class SmaxOfflineBatchRequest implements SmaxOperation.Request {
    /**
     * The number of offline messages the client expects to absorb in
     * the upcoming batch.
     */
    private final int offlineBatchCount;

    /**
     * Constructs a request with the given offline-batch count.
     *
     * @param offlineBatchCount the expected offline-batch size
     */
    public SmaxOfflineBatchRequest(int offlineBatchCount) {
        this.offlineBatchCount = offlineBatchCount;
    }

    /**
     * Returns the expected offline-batch size.
     *
     * @return the count
     */
    public int offlineBatchCount() {
        return offlineBatchCount;
    }

    /**
     * Builds the outbound stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the {@code <ib>} envelope
     *         and the {@code <offline_batch/>} payload
     *
     * @implNote {@code WASmaxOutOfflineBatchRequest.makeBatchRequest}
     *           emits {@code <ib><offline_batch count=INT(t)/></ib>}.
     *           No id/xmlns/to attributes are populated — the relay
     *           identifies the cast by the {@code <offline_batch/>}
     *           grandchild's tag alone.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutOfflineBatchRequest",
            exports = "makeBatchRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var offlineBatchNode = new NodeBuilder()
                .description("offline_batch")
                .attribute("count", offlineBatchCount)
                .build();
        return new NodeBuilder()
                .description("ib")
                .content(offlineBatchNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxOfflineBatchRequest) obj;
        return this.offlineBatchCount == that.offlineBatchCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offlineBatchCount);
    }

    @Override
    public String toString() {
        return "SmaxOfflineBatchRequest[offlineBatchCount=" + offlineBatchCount + ']';
    }
}
