package com.github.auties00.cobalt.node.smax.abprops;

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
 * The outbound stanza variant. Wraps the {@code <props/>} payload in
 * the canonical {@code <iq xmlns="abt" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutAbPropsGetExperimentConfigRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutAbPropsBaseIQGetRequestMixin")
public final class SmaxAbPropsGetExperimentConfigRequest implements SmaxOperation.Request {
    /**
     * The optional content hash echoed back to the relay so it can
     * short-circuit the reply to a delta when the client's snapshot is
     * already up to date.
     */
    private final String propsHash;

    /**
     * The optional refresh id echoed back to the relay so it can
     * correlate this fetch with a prior server-pushed
     * {@code <notification type="abprops">} bump.
     */
    private final Integer propsRefreshId;

    /**
     * Constructs a request for the given hash and refresh id.
     *
     * @param propsHash      the client's currently-cached props hash;
     *                       may be {@code null} on the first fetch
     * @param propsRefreshId the client's currently-cached refresh id;
     *                       may be {@code null} on the first fetch
     */
    public SmaxAbPropsGetExperimentConfigRequest(String propsHash, Integer propsRefreshId) {
        this.propsHash = propsHash;
        this.propsRefreshId = propsRefreshId;
    }

    /**
     * Constructs an unconditional request. The relay always replies
     * with the full props bundle.
     */
    public SmaxAbPropsGetExperimentConfigRequest() {
        this(null, null);
    }

    /**
     * Returns the client's currently-cached props hash, when set.
     *
     * @return an {@link Optional} carrying the hash, or empty when not
     *         supplied
     */
    public Optional<String> propsHash() {
        return Optional.ofNullable(propsHash);
    }

    /**
     * Returns the client's currently-cached refresh id, when set.
     *
     * @return an {@link Optional} carrying the refresh id, or empty
     *         when not supplied
     */
    public Optional<Integer> propsRefreshId() {
        return Optional.ofNullable(propsRefreshId);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <props/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutAbPropsGetExperimentConfigRequest",
            exports = "makeGetExperimentConfigRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var propsBuilder = new NodeBuilder()
                .description("props")
                .attribute("protocol", "1");
        if (propsHash != null) {
            propsBuilder.attribute("hash", propsHash);
        }
        if (propsRefreshId != null) {
            propsBuilder.attribute("refresh_id", propsRefreshId);
        }
        var propsNode = propsBuilder.build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "abt")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(propsNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxAbPropsGetExperimentConfigRequest) obj;
        return Objects.equals(this.propsHash, that.propsHash)
                && Objects.equals(this.propsRefreshId, that.propsRefreshId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propsHash, propsRefreshId);
    }

    @Override
    public String toString() {
        return "SmaxAbPropsGetExperimentConfigRequest[propsHash=" + propsHash
                + ", propsRefreshId=" + propsRefreshId + ']';
    }
}
