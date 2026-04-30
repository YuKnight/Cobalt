package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;

/**
 * The outbound {@code <iq xmlns="fb:thrift_iq" type="set">} stanza that
 * toggles the cart-enabled flag in the current merchant's commerce
 * settings.
 */
@WhatsAppWebModule(moduleName = "WAWebBusinessProfileJob")
public final class IqUpdateCartEnabledRequest implements IqOperation.Request {
    /**
     * Whether the cart should be enabled.
     */
    private final boolean cartEnabled;

    /**
     * Constructs a request.
     *
     * @param cartEnabled the desired state
     */
    public IqUpdateCartEnabledRequest(boolean cartEnabled) {
        this.cartEnabled = cartEnabled;
    }

    /**
     * Returns the desired cart-enabled flag.
     *
     * @return the flag
     */
    public boolean cartEnabled() {
        return cartEnabled;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBusinessProfileJob",
            exports = "updateCartEnabled", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var cartNode = new NodeBuilder()
                .description("cart")
                .attribute("enabled", String.valueOf(cartEnabled))
                .build();
        var commerceSettingsNode = new NodeBuilder()
                .description("commerce_settings")
                .content(cartNode)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(commerceSettingsNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqUpdateCartEnabledRequest) obj;
        return this.cartEnabled == that.cartEnabled;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(cartEnabled);
    }

    @Override
    public String toString() {
        return "IqUpdateCartEnabledRequest[cartEnabled=" + cartEnabled + ']';
    }
}
