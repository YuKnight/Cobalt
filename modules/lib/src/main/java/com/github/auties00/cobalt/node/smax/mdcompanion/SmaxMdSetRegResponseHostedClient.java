package com.github.auties00.cobalt.node.smax.mdcompanion;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The hosted (Meta-AI / business-platform) companion's pair-success
 * reply, carrying the bare
 * {@code <hosted-pair-set><device-identity/></hosted-pair-set>}
 * payload.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMdSetRegResponseHostedClientResponse")
@WhatsAppWebModule(moduleName = "WASmaxOutMdHostedCompanionSetRegResponseBundleMixin")
public final class SmaxMdSetRegResponseHostedClient implements SmaxOperation.Request {
    /**
     * The id of the inbound IQ being replied to.
     */
    private final String iqId;

    /**
     * The signed device-identity content bytes.
     */
    private final byte[] deviceIdentity;

    /**
     * Constructs a new hosted pair-success reply.
     *
     * @param iqId           the IQ id; never {@code null}
     * @param deviceIdentity the signed device-identity bytes; never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public SmaxMdSetRegResponseHostedClient(String iqId, byte[] deviceIdentity) {
        this.iqId = Objects.requireNonNull(iqId, "iqId cannot be null");
        this.deviceIdentity = Objects.requireNonNull(deviceIdentity, "deviceIdentity cannot be null");
    }

    /**
     * Returns the IQ id.
     *
     * @return the id; never {@code null}
     */
    public String iqId() {
        return iqId;
    }

    /**
     * Returns the signed device-identity bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] deviceIdentity() {
        return deviceIdentity;
    }

    /**
     * Builds the outbound hosted pair-success reply stanza.
     *
     * @return a {@link NodeBuilder} carrying the reply envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutMdSetRegResponseHostedClientResponse",
            exports = "makeSetRegResponseHostedClientResponse",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var deviceIdentityNode = new NodeBuilder()
                .description("device-identity")
                .content(deviceIdentity)
                .build();
        var hostedPairSetNode = new NodeBuilder()
                .description("hosted-pair-set")
                .content(deviceIdentityNode)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("id", iqId)
                .attribute("to", "s.whatsapp.net")
                .attribute("type", "result")
                .content(hostedPairSetNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdSetRegResponseHostedClient) obj;
        return Objects.equals(this.iqId, that.iqId)
                && Arrays.equals(this.deviceIdentity, that.deviceIdentity);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(iqId);
        result = 31 * result + Arrays.hashCode(deviceIdentity);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxMdSetRegResponseHostedClient[iqId=" + iqId
                + ", deviceIdentity=" + Arrays.toString(deviceIdentity) + ']';
    }
}
