package com.github.auties00.cobalt.graphql.web.business;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.business.catalog.BusinessProduct;

import java.util.List;
import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the public product-list query built by
 * {@link QueryProductListCatalogJobWhatsAppWebGraphQlRequest} into a list of {@link BusinessProduct}.
 *
 * <p>Reads the linked chain {@code xwa_product_catalog_get_product_list -> product_list -> products}
 * and projects each product onto the Cobalt domain model.
 *
 * @see QueryProductListCatalogJobWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebQueryProductListCatalogJobQuery")
public final class QueryProductListCatalogJobWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the parsed products.
     */
    private final List<BusinessProduct> products;

    /**
     * Constructs a response wrapping the parsed products.
     *
     * <p>Reserved for the static parser.
     *
     * @param products the parsed products
     */
    private QueryProductListCatalogJobWhatsAppWebGraphQlResponse(List<BusinessProduct> products) {
        this.products = products;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object.
     *
     * <p>Reads the linked chain {@code xwa_product_catalog_get_product_list -> product_list ->
     * products} and projects each product onto a {@link BusinessProduct}.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} or the product-list projection is missing
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryProductListCatalogJob", exports = "QueryProductListCatalog",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<QueryProductListCatalogJobWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }
        var root = data.getJSONObject("xwa_product_catalog_get_product_list");
        if (root == null) {
            return Optional.empty();
        }
        var productList = root.getJSONObject("product_list");
        if (productList == null) {
            return Optional.empty();
        }
        var products = CatalogProductInfoParser.parseProducts(productList.getJSONArray("products"));
        return Optional.of(new QueryProductListCatalogJobWhatsAppWebGraphQlResponse(products));
    }

    /**
     * Returns the parsed products.
     *
     * @return an unmodifiable list of {@link BusinessProduct} values, never {@code null}
     */
    public List<BusinessProduct> products() {
        return products;
    }
}
