package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps an optional {@code <item dhash/>}
 * child in the canonical {@code <iq xmlns="optoutlist" type="get">}
 * envelope and optionally narrows the query to a single
 * {@code category}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsGetOptOutListRequest")
public final class SmaxGetOptOutListRequest implements SmaxOperation.Request {
    /**
     * The optional client-side {@code dhash} digest of the cached
     * opt-out list. Supplied so the relay can return
     * {@link SmaxGetOptOutListResponse.SuccessWithMatch} when the client's
     * cache is up to date, avoiding a re-download of the full list.
     */
    private final String itemDhash;

    /**
     * The optional category filter. Used to narrow the query to a
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
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * <p>Mirrors {@code makeGetOptOutListRequest(t)} from the WA Web
     * source: emits {@code <iq to=S_WHATSAPP_NET xmlns="optoutlist"
     * type="get">}, conditionally appends the {@code category}
     * attribute via {@code OPTIONAL(CUSTOM_STRING, r)}, and
     * conditionally attaches the {@code <item dhash/>} child via
     * {@code OPTIONAL_CHILD(e, n)}. The {@code id} attribute is
     * generated downstream by {@code WhatsAppClient.sendNode}.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsGetOptOutListRequest",
            exports = "makeGetOptOutListRequest", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsGetOptOutListRequest",
            exports = "makeGetOptOutListRequestItem", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        // WASmaxOutBlocklistsGetOptOutListRequest.makeGetOptOutListRequest: smax("iq", {to: S_WHATSAPP_NET, xmlns: "optoutlist", type: "get", category: OPTIONAL(CUSTOM_STRING, r), id: generateId()}, OPTIONAL_CHILD(e, n))
        var iqBuilder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "optoutlist")
                .attribute("to", JidServer.user())
                .attribute("type", "get");
        if (iqCategory != null) {
            // WASmaxOutBlocklistsGetOptOutListRequest.makeGetOptOutListRequest: category: OPTIONAL(CUSTOM_STRING, r)
            iqBuilder.attribute("category", iqCategory);
        }
        if (itemDhash != null) {
            // WASmaxOutBlocklistsGetOptOutListRequest.makeGetOptOutListRequestItem: smax("item", {dhash: CUSTOM_STRING(t)})
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
