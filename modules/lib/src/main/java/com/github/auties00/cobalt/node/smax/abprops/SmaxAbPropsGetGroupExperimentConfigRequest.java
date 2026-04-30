package com.github.auties00.cobalt.node.smax.abprops;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
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
@WhatsAppWebModule(moduleName = "WASmaxOutAbPropsGetGroupExperimentConfigRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutAbPropsBaseIQGetRequestMixin")
public final class SmaxAbPropsGetGroupExperimentConfigRequest implements SmaxOperation.Request {
    /**
     * The group JID whose experiment configuration is requested. Routed
     * verbatim into the {@code <props group/>} attribute.
     */
    private final Jid groupJid;

    /**
     * The optional content hash echoed back to the relay so it can
     * short-circuit the reply to a delta when the client's snapshot is
     * already up to date.
     */
    private final String propsHash;

    /**
     * Constructs a request for the given group and hash.
     *
     * @param groupJid  the target group JID. Never {@code null}
     * @param propsHash the client's currently-cached props hash. May
     *                  be {@code null} on the first fetch
     * @throws NullPointerException if {@code groupJid} is {@code null}
     */
    public SmaxAbPropsGetGroupExperimentConfigRequest(Jid groupJid, String propsHash) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        this.propsHash = propsHash;
    }

    /**
     * Constructs an unconditional request for the given group.
     *
     * @param groupJid the target group JID. Never {@code null}
     * @throws NullPointerException if {@code groupJid} is {@code null}
     */
    public SmaxAbPropsGetGroupExperimentConfigRequest(Jid groupJid) {
        this(groupJid, null);
    }

    /**
     * Returns the target group JID.
     *
     * @return the group JID. Never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns the client's currently-cached props hash, when set.
     *
     * @return an {@link Optional} carrying the hash, or empty
     */
    public Optional<String> propsHash() {
        return Optional.ofNullable(propsHash);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <props/>} payload
     *
     * @implNote {@code WASmaxOutAbPropsGetGroupExperimentConfigRequest.makeGetGroupExperimentConfigRequest}
     *           composes {@code WASmaxOutAbPropsBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over a
     *           {@code <props group=GROUP_JID hash?/>} child inside
     *           an {@code <iq xmlns="abt" to=S_WHATSAPP_NET>} envelope.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutAbPropsGetGroupExperimentConfigRequest",
            exports = "makeGetGroupExperimentConfigRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var propsBuilder = new NodeBuilder()
                .description("props")
                .attribute("group", groupJid);
        if (propsHash != null) {
            propsBuilder.attribute("hash", propsHash);
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
        var that = (SmaxAbPropsGetGroupExperimentConfigRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.propsHash, that.propsHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, propsHash);
    }

    @Override
    public String toString() {
        return "SmaxAbPropsGetGroupExperimentConfigRequest[groupJid=" + groupJid
                + ", propsHash=" + propsHash + ']';
    }
}
