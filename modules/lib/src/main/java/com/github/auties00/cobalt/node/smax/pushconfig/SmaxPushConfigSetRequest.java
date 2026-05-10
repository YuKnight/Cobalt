package com.github.auties00.cobalt.node.smax.pushconfig;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound {@code <iq xmlns="urn:xmpp:whatsapp:push">} stanza
 * variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPushConfigSetRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutPushConfigBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutPushConfigSetSetConfigOrSetClearMixinGroup")
public final class SmaxPushConfigSetRequest implements SmaxOperation.Request {
    /**
     * The exclusive payload variant. Exactly one of
     * {@code config} / {@code clear} must be non-null.
     */
    private final SmaxPushConfigSetSetVariant variant;

    /**
     * Constructs a new push-config request.
     *
     * @param variant the payload variant. Never {@code null}
     * @throws NullPointerException if {@code variant} is
     *                              {@code null}
     */
    public SmaxPushConfigSetRequest(SmaxPushConfigSetSetVariant variant) {
        this.variant = Objects.requireNonNull(variant, "variant cannot be null");
    }

    /**
     * Returns the payload variant.
     *
     * @return the variant. Never {@code null}
     */
    public SmaxPushConfigSetSetVariant variant() {
        return variant;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the payload variant
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigSetRequest",
            exports = "makeSetRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // <config>...</config> or <clear platform?/>.
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "urn:xmpp:whatsapp:push")
                .attribute("to", Jid.userServer())
                .attribute("type", "set")
                .content(variant.toNode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxPushConfigSetRequest) obj;
        return Objects.equals(this.variant, that.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant);
    }

    @Override
    public String toString() {
        return "SmaxPushConfigSetRequest[variant=" + variant + ']';
    }
}
