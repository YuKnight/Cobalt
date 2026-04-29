package com.github.auties00.cobalt.node.mex.json.business;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.business.catalog.BusinessCatalog;
import com.github.auties00.cobalt.model.business.catalog.BusinessCatalogBuilder;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.mex.MexOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The response variant of the {@code queryProductCollections} MEX
 * operation that parses the JSON returned by the relay.
 *
 * @implNote WAWebQueryProductCollections: adapts the
 * {@code xwa_product_catalog_get_collections.collections} array into a
 * list of {@link BusinessCatalog}; the GraphQL {@code paging.after}
 * cursor is surfaced so callers can paginate.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryProductCollections")
public final class QueryProductCollectionsMexResponse implements MexOperation.Response.Json {
    private final List<BusinessCatalog> collections;
    private final String afterCursor;

    /**
     * Constructs a parsed collections response.
     *
     * @param collections the parsed business catalogs
     * @param afterCursor the {@code paging.after} cursor, or empty
     */
    private QueryProductCollectionsMexResponse(List<BusinessCatalog> collections, String afterCursor) {
        this.collections = collections;
        this.afterCursor = afterCursor;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebQueryProductCollections.default: the WA Web helper
     * reads {@code data.xwa_product_catalog_get_collections.collections}
     * and the sibling {@code paging.after} cursor. When the relay
     * returns a GraphQL error with code {@code 2498052} WA Web surfaces
     * an empty response; Cobalt applies the same behaviour by treating
     * a missing {@code xwa_product_catalog_get_collections} field as
     * an empty page.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryProductCollections", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<QueryProductCollectionsMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(QueryProductCollectionsMexResponse::of);
    }

    /**
     * Returns the collections returned by this page of the query.
     *
     * @return an unmodifiable list of collections, never {@code null}
     */
    public List<BusinessCatalog> collections() {
        return collections;
    }

    /**
     * Returns the {@code paging.after} cursor usable to request the
     * next page of collections.
     *
     * @return an {@link Optional} containing the cursor when the relay
     *         returned a non-empty value, or empty otherwise
     */
    public Optional<String> afterCursor() {
        return afterCursor == null || afterCursor.isEmpty() ? Optional.empty() : Optional.of(afterCursor);
    }

    /**
     * Parses the raw JSON bytes of the {@code <result>} child into a
     * structured response.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing the expected fields
     */
    private static Optional<QueryProductCollectionsMexResponse> of(byte[] json) {
        var root = JSON.parseObject(json);
        if (root == null) {
            return Optional.empty();
        }
        var data = root.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }
        var getResult = data.getJSONObject("xwa_product_catalog_get_collections");
        if (getResult == null) {
            // WAWebQueryProductCollections.default: missing field is treated as an empty page
            return Optional.of(new QueryProductCollectionsMexResponse(List.of(), ""));
        }
        var paging = getResult.getJSONObject("paging");
        var cursor = paging == null ? "" : Optional.ofNullable(paging.getString("after")).orElse("");
        var collectionsArray = getResult.getJSONArray("collections");
        var collections = parseCollections(collectionsArray);
        return Optional.of(new QueryProductCollectionsMexResponse(collections, cursor));
    }

    /**
     * Parses an array of GraphQL collection objects into a list of
     * {@link BusinessCatalog} values.
     *
     * @implNote WAWebQueryProductCollections.default: each collection is
     * mapped onto {@code {id, name, products, ...}}; Cobalt drops the
     * {@code status_info} and {@code canAppeal} side-channels since
     * {@link BusinessCatalog} does not expose them yet. The inner
     * {@code products} array is parsed via
     * {@link CatalogProductParser#parseProducts(JSONArray)} to share the
     * same field projection with the catalog query.
     * @param array the GraphQL collections array, possibly {@code null}
     * @return the parsed collections, never {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryProductCollections", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static List<BusinessCatalog> parseCollections(JSONArray array) {
        if (array == null || array.isEmpty()) {
            return List.of();
        }
        var out = new ArrayList<BusinessCatalog>(array.size());
        for (var i = 0; i < array.size(); i++) {
            parseCollection(array.getJSONObject(i)).ifPresent(out::add);
        }
        return List.copyOf(out);
    }

    /**
     * Parses a single GraphQL collection object into a
     * {@link BusinessCatalog}.
     *
     * @param obj the GraphQL collection object, possibly {@code null}
     * @return the parsed collection, or {@link Optional#empty()} if
     *         {@code obj} is {@code null}
     */
    private static Optional<BusinessCatalog> parseCollection(JSONObject obj) {
        if (obj == null) {
            return Optional.empty();
        }
        // WAWebQueryProductCollections.default: id defaults to empty string
        var id = Optional.ofNullable(obj.getString("id")).orElse("");
        // WAWebQueryProductCollections.default: name defaults to empty string
        var name = Optional.ofNullable(obj.getString("name")).orElse("");
        // WAWebQueryProductCollections.default: products parsed via parseProductGraphQL
        var products = CatalogProductParser.parseProducts(obj.getJSONArray("products"));
        // ADAPTED: BusinessCatalog constructor is package-private so Cobalt uses the generated builder
        var collection = new BusinessCatalogBuilder()
                .id(id)
                .name(name)
                .products(products)
                .build();
        return Optional.of(collection);
    }
}
