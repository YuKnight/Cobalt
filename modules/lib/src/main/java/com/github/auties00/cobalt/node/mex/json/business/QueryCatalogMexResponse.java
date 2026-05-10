package com.github.auties00.cobalt.node.mex.json.business;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.business.catalog.BusinessCatalogEntry;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.mex.MexOperation;

import java.util.List;
import java.util.Optional;

/**
 * Parsed response of the {@code queryCatalog} MEX query carrying the
 * {@code xwa_product_catalog_get_product_catalog.product_catalog} projection
 * and the {@code paging.after} cursor.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryCatalog")
public final class QueryCatalogMexResponse implements MexOperation.Response.Json {
    private final List<BusinessCatalogEntry> products;
    private final String afterCursor;

    /**
     * Constructs a parsed catalog response.
     *
     * @param products    the catalog entries returned by this page
     * @param afterCursor the {@code paging.after} cursor, or empty
     */
    private QueryCatalogMexResponse(List<BusinessCatalogEntry> products, String afterCursor) {
        this.products = products;
        this.afterCursor = afterCursor;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or empty if the expected JSON shape is
     *         absent
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryCatalog", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<QueryCatalogMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(QueryCatalogMexResponse::of);
    }

    /**
     * Returns the products returned by this page of the catalog.
     *
     * @return an unmodifiable list of entries, never {@code null}
     */
    public List<BusinessCatalogEntry> products() {
        return products;
    }

    /**
     * Returns the {@code paging.after} cursor usable to request the next
     * page of products.
     *
     * @return an {@link Optional} carrying the cursor when the relay
     *         returned a non-empty value, or empty otherwise
     */
    public Optional<String> afterCursor() {
        return afterCursor == null || afterCursor.isEmpty() ? Optional.empty() : Optional.of(afterCursor);
    }

    /**
     * Parses the raw JSON bytes of the {@code <result>} child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return the parsed response, or empty if the envelope is missing the
     *         expected fields
     */
    private static Optional<QueryCatalogMexResponse> of(byte[] json) {
        var root = JSON.parseObject(json);
        if (root == null) {
            return Optional.empty();
        }
        var data = root.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }
        var getResult = data.getJSONObject("xwa_product_catalog_get_product_catalog");
        if (getResult == null) {
            return Optional.of(new QueryCatalogMexResponse(List.of(), ""));
        }
        var catalog = getResult.getJSONObject("product_catalog");
        if (catalog == null) {
            return Optional.of(new QueryCatalogMexResponse(List.of(), ""));
        }
        var paging = catalog.getJSONObject("paging");
        var cursor = paging == null ? "" : Optional.ofNullable(paging.getString("after")).orElse("");
        var products = CatalogProductParser.parseProducts(catalog.getJSONArray("products"));
        return Optional.of(new QueryCatalogMexResponse(products, cursor));
    }
}
