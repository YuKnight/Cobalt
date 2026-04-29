package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound stanza variant — wraps a single
 * {@code <list name=CATEGORY value="contact_blacklist"/>} child in
 * the canonical {@code <iq xmlns="privacy" type="get"><privacy>...
 * </privacy></iq>} envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryPrivacyDisallowedListPnJob")
public final class IqQueryPrivacyDisallowedListPnRequest implements IqOperation.Request {
    /**
     * The privacy category whose disallowed list is being queried;
     * routed verbatim into the {@code <list>}'s {@code name}
     * attribute (e.g. {@code "last"}, {@code "profile"},
     * {@code "groupadd"}).
     */
    private final String categoryName;

    /**
     * Constructs a new request.
     *
     * @param categoryName the privacy category name; never
     *                     {@code null}
     * @throws NullPointerException if {@code categoryName} is
     *                              {@code null}
     */
    public IqQueryPrivacyDisallowedListPnRequest(String categoryName) {
        this.categoryName = Objects.requireNonNull(categoryName, "categoryName cannot be null");
    }

    /**
     * Returns the privacy category being queried.
     *
     * @return the category name; never {@code null}
     */
    public String categoryName() {
        return categoryName;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <privacy>} payload
     *
     * @implNote {@code WAWebQueryPrivacyDisallowedListPnJob.queryPrivacyDisallowedListPn}:
     *           {@code wap("iq",{xmlns:"privacy", type:"get",
     *           to:S_WHATSAPP_NET, id}, wap("privacy", null,
     *           wap("list",{name:CATEGORY,
     *           value:"contact_blacklist"})))}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryPrivacyDisallowedListPnJob",
            exports = "queryPrivacyDisallowedListPn",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebQueryPrivacyDisallowedListPnJob: wap("list",{name:CATEGORY, value:"contact_blacklist"})
        var listNode = new NodeBuilder()
                .description("list")
                .attribute("name", categoryName)
                .attribute("value", "contact_blacklist")
                .build();
        // WAWebQueryPrivacyDisallowedListPnJob: wap("privacy", null, ...)
        var privacyNode = new NodeBuilder()
                .description("privacy")
                .content(listNode)
                .build();
        // WAWebQueryPrivacyDisallowedListPnJob: wap("iq",{xmlns:"privacy",type:"get",to:S_WHATSAPP_NET,id}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "privacy")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
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
        var that = (IqQueryPrivacyDisallowedListPnRequest) obj;
        return Objects.equals(this.categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName);
    }

    @Override
    public String toString() {
        return "IqQueryPrivacyDisallowedListPnRequest[categoryName=" + categoryName + ']';
    }
}
