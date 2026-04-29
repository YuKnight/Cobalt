package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The outbound stanza variant.
 */
public final class IqGetMerchantComplianceRequest implements IqOperation.Request {
    /**
     * The merchant JIDs whose compliance bundles are being queried.
     */
    private final List<Jid> businessJids;

    /**
     * Constructs a request.
     *
     * @param businessJids the list of merchant JIDs; never {@code null}
     *                     and must be non-empty
     * @throws NullPointerException     if {@code businessJids} is
     *                                  {@code null}
     * @throws IllegalArgumentException when {@code businessJids} is
     *                                  empty
     */
    public IqGetMerchantComplianceRequest(List<Jid> businessJids) {
        Objects.requireNonNull(businessJids, "businessJids cannot be null");
        if (businessJids.isEmpty()) {
            throw new IllegalArgumentException("businessJids cannot be empty");
        }
        this.businessJids = List.copyOf(businessJids);
    }

    /**
     * Returns the queried merchant JIDs.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<Jid> businessJids() {
        return businessJids;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebMerchantComplianceJob",
            exports = "getMerchantCompliance", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        for (var jid : businessJids) {
            children.add(new NodeBuilder()
                    .description("merchant_info")
                    .attribute("jid", jid)
                    .build());
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz:merchant_info")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(children);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqGetMerchantComplianceRequest) obj;
        return Objects.equals(this.businessJids, that.businessJids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessJids);
    }

    @Override
    public String toString() {
        return "IqGetMerchantComplianceRequest[businessJids=" + businessJids + ']';
    }
}
