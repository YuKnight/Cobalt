package com.github.auties00.cobalt.node.smax.privacy;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps an optional {@code <item dhash/>}
 * child in the canonical {@code <iq xmlns="blocklist" type="get">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsGetBlockListRequest")
public final class SmaxGetBlockListRequest implements SmaxOperation.Request {
    /**
     * The optional client-side {@code dhash} digest of the cached
     * blocklist. Supplied so the relay can return
     * {@link SmaxGetBlockListResponse.SuccessWithMatch} when the client's cache is
     * up to date, avoiding a re-download of the full list.
     */
    private final String itemDhash;

    /**
     * Constructs a request.
     *
     * @param itemDhash the cached-list digest; may be {@code null} to
     *                  request a full list
     */
    public SmaxGetBlockListRequest(String itemDhash) {
        this.itemDhash = itemDhash;
    }

    /**
     * Returns the optional cached-list digest.
     *
     * @return an {@link Optional} carrying the digest, or empty when
     *         the caller is requesting a full list
     */
    public Optional<String> itemDhash() {
        return Optional.ofNullable(itemDhash);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsGetBlockListRequest",
            exports = "makeGetBlockListRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var iqBuilder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "blocklist")
                .attribute("to", JidServer.user())
                .attribute("type", "get");
        if (itemDhash != null) {
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
        var that = (SmaxGetBlockListRequest) obj;
        return Objects.equals(this.itemDhash, that.itemDhash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemDhash);
    }

    @Override
    public String toString() {
        return "SmaxGetBlockListRequest[itemDhash=" + itemDhash + ']';
    }
}
