package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound stanza variant. Wraps a single
 * {@code <category name="readreceipts" value="all|none"/>} child in
 * the canonical {@code <iq xmlns="privacy" type="set"><privacy>...
 * </privacy></iq>} envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebSetReadReceiptJob")
public final class IqSetReadReceiptRequest implements IqOperation.Request {
    /**
     * The new toggle state. {@code true} maps to wire value
     * {@code "all"}, {@code false} maps to wire value {@code "none"}.
     */
    private final boolean enabled;

    /**
     * Constructs a new request.
     *
     * @param enabled {@code true} to enable read receipts, {@code false}
     *                to disable
     */
    public IqSetReadReceiptRequest(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the requested toggle state.
     *
     * @return {@code true} when read receipts are being enabled,
     *         {@code false} when disabled
     */
    public boolean enabled() {
        return enabled;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <privacy>} payload
     *
     * @implNote {@code WAWebSetReadReceiptJob.default}:
     *           {@code wap("iq",{to:S_WHATSAPP_NET,type:"set",
     *           xmlns:"privacy",id}, wap("privacy", null,
     *           wap("category",{name:"readreceipts",
     *           value: t ? "all" : "none"})))}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSetReadReceiptJob",
            exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var value = enabled ? "all" : "none";
        // WAWebSetReadReceiptJob: wap("category",{name:"readreceipts",value:...})
        var categoryNode = new NodeBuilder()
                .description("category")
                .attribute("name", "readreceipts")
                .attribute("value", value)
                .build();
        // WAWebSetReadReceiptJob: wap("privacy", null, ...)
        var privacyNode = new NodeBuilder()
                .description("privacy")
                .content(categoryNode)
                .build();
        // WAWebSetReadReceiptJob: wap("iq",{to:S_WHATSAPP_NET,type:"set",xmlns:"privacy",id}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "privacy")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(privacyNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSetReadReceiptRequest) obj;
        return this.enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled);
    }

    @Override
    public String toString() {
        return "IqSetReadReceiptRequest[enabled=" + enabled + ']';
    }
}
