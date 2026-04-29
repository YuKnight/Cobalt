package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound stanza variant.
 */
public final class IqQueryBusinessCategoriesRequest implements IqOperation.Request {
    /**
     * The free-text typeahead query (may be empty).
     */
    private final String query;

    /**
     * Constructs a request.
     *
     * @param query the typeahead query; never {@code null}
     * @throws NullPointerException if {@code query} is {@code null}
     */
    public IqQueryBusinessCategoriesRequest(String query) {
        this.query = Objects.requireNonNull(query, "query cannot be null");
    }

    /**
     * Returns the typeahead query.
     *
     * @return the query string; never {@code null}
     */
    public String query() {
        return query;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryBusinessCategoriesJob",
            exports = "queryBusinessCategories", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var queryNode = new NodeBuilder()
                .description("query")
                .content(query)
                .build();
        var requestNode = new NodeBuilder()
                .description("request")
                .attribute("op", "profile_typeahead")
                .attribute("type", "catkit")
                .attribute("v", "1")
                .content(queryNode)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(requestNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqQueryBusinessCategoriesRequest) obj;
        return Objects.equals(this.query, that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query);
    }

    @Override
    public String toString() {
        return "IqQueryBusinessCategoriesRequest[query=" + query + ']';
    }
}
