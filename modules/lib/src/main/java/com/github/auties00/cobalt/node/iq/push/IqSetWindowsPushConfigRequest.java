package com.github.auties00.cobalt.node.iq.push;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.util.RandomIdUtils;
import java.util.Objects;

/**
 * The outbound {@code <iq xmlns="urn:xmpp:whatsapp:push" type="set">}
 * stanza variant — wraps the {@code <config id platform version/>}
 * payload.
 */
@WhatsAppWebModule(moduleName = "WAWebSetWindowsPushConfig")
public final class IqSetWindowsPushConfigRequest implements IqOperation.Request {
    /**
     * The WNS channel URI registered with the relay. Routed verbatim
     * into the {@code id} attribute of the {@code <config/>} payload.
     */
    private final String channelUri;

    /**
     * The Windows-store distribution ring. Routed via
     * {@link IqSetWindowsPushConfigRing#wireValue()} into the {@code version} attribute,
     * or omitted when the ring is {@link IqSetWindowsPushConfigRing#PUBLIC}.
     */
    private final IqSetWindowsPushConfigRing ring;

    /**
     * Constructs a new set-windows-push-config request.
     *
     * @param channelUri the WNS channel URI; never {@code null}
     * @param ring       the distribution ring; never {@code null}
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    public IqSetWindowsPushConfigRequest(String channelUri, IqSetWindowsPushConfigRing ring) {
        this.channelUri = Objects.requireNonNull(channelUri, "channelUri cannot be null");
        this.ring = Objects.requireNonNull(ring, "ring cannot be null");
    }

    /**
     * Returns the WNS channel URI.
     *
     * @return the URI; never {@code null}
     */
    public String channelUri() {
        return channelUri;
    }

    /**
     * Returns the distribution ring.
     *
     * @return the ring; never {@code null}
     */
    public IqSetWindowsPushConfigRing ring() {
        return ring;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <config/>} payload
     *
     * @implNote {@code WAWebSetWindowsPushConfig.setWindowsPushConfig}
     *           composes {@code wap("iq", {to:S_WHATSAPP_NET,
     *           type:"set", xmlns:"urn:xmpp:whatsapp:push", id},
     *           wap("config", {id:CUSTOM_STRING(channelUri),
     *           platform:"wns", version:m(ring)}))}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSetWindowsPushConfig",
            exports = "setWindowsPushConfig", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var configBuilder = new NodeBuilder()
                .description("config")
                .attribute("id", channelUri)
                .attribute("platform", "wns");
        var version = ring.wireValue();
        if (version != null) {
            configBuilder = configBuilder.attribute("version", version);
        }
        var configNode = configBuilder.build();
        return new NodeBuilder()
                .description("iq")
                .attribute("id", RandomIdUtils.newId())
                .attribute("xmlns", "urn:xmpp:whatsapp:push")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(configNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSetWindowsPushConfigRequest) obj;
        return Objects.equals(this.channelUri, that.channelUri)
                && this.ring == that.ring;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelUri, ring);
    }

    @Override
    public String toString() {
        return "IqSetWindowsPushConfigRequest[channelUri=" + channelUri
                + ", ring=" + ring + ']';
    }
}
