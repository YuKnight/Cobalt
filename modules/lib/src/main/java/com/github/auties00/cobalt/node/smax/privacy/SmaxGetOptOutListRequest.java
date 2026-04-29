package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsGetOptOutListRequest")
public final class SmaxGetOptOutListRequest implements SmaxOperation.Request {
    /**
     * The optional client-side digest of the cached opt-out list.
     */
    private final String itemDhash;

    /**
     * The optional category filter — used to narrow the query to a
     * subset of opt-out entries.
     */
    private final String iqCategory;

    /**
     * Constructs a request.
     *
     * @param itemDhash  the cached digest; may be {@code null}
     * @param iqCategory the category filter; may be {@code null}
     */
    public SmaxGetOptOutListRequest(String itemDhash, String iqCategory) {
        this.itemDhash = itemDhash;
        this.iqCategory = iqCategory;
    }

    /**
     * Returns the optional cached digest.
     *
     * @return an {@link Optional} carrying the digest, or empty when
     *         omitted
     */
    public Optional<String> itemDhash() {
        return Optional.ofNullable(itemDhash);
    }

    /**
     * Returns the optional category filter.
     *
     * @return an {@link Optional} carrying the category, or empty
     *         when omitted
     */
    public Optional<String> iqCategory() {
        return Optional.ofNullable(iqCategory);
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     *
     * @implNote {@code WASmaxOutBlocklistsGetOptOutListRequest.makeGetOptOutListRequest}
     *           composes {@code <iq to="s.whatsapp.net" xmlns="optoutlist"
     *           type="get" category? id="…"><item dhash/></iq>}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsGetOptOutListRequest",
            exports = "makeGetOptOutListRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutBlocklistsGetOptOutListRequest: smax("iq", {to: S_WHATSAPP_NET, xmlns: "optoutlist", type: "get", category?, id: generateId()})
        var iqBuilder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "optoutlist")
                .attribute("to", JidServer.user())
                .attribute("type", "get");
        if (iqCategory != null) {
            iqBuilder.attribute("category", iqCategory);
        }
        if (itemDhash != null) {
            // WASmaxOutBlocklistsGetOptOutListRequest: smax("item", {dhash})
            var itemNode = new NodeBuilder()
                    .description("item")
                    .attribute("dhash", itemDhash)
                    .build();
            iqBuilder.content(itemNode);
        }
        return iqBuilder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGetOptOutListRequest) obj;
        return Objects.equals(this.itemDhash, that.itemDhash)
                && Objects.equals(this.iqCategory, that.iqCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemDhash, iqCategory);
    }

    @Override
    public String toString() {
        return "SmaxGetOptOutListRequest[itemDhash=" + itemDhash
                + ", iqCategory=" + iqCategory + ']';
    }
}
