package com.github.auties00.cobalt.node.mex.json.business;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.business.catalog.BusinessCatalogEntry;
import com.github.auties00.cobalt.model.business.catalog.BusinessCatalogEntryBuilder;
import com.github.auties00.cobalt.model.business.catalog.BusinessItemAvailability;
import com.github.auties00.cobalt.model.business.catalog.BusinessReviewStatus;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shared utility that parses GraphQL product objects into Cobalt
 * {@link BusinessCatalogEntry} values.
 *
 * <p>The parsing logic mirrors WA Web's
 * {@code WAWebBizParseProductGraphql.parseProductGraphQL} which is invoked
 * from both the catalog query and the product-collections query response
 * decoders. Cobalt centralises the projection into a single helper class so
 * the two decoders can share the exact same projection logic.
 *
 * @implNote WAWebBizParseProductGraphql: this helper restricts the parse to
 * the fields surfaced by {@link BusinessCatalogEntry} (id, retailer_id,
 * name, description, url, currency, price, visibility, first image URL,
 * status, stock availability). The remaining WA Web fields (variants,
 * compliance, videos, sale price) are intentionally dropped since Cobalt
 * does not expose them yet.
 */
@WhatsAppWebModule(moduleName = "WAWebBizParseProductGraphql")
@WhatsAppWebModule(moduleName = "WAWebBizParseProductGraphql_product.graphql")
public final class CatalogProductParser {
    /**
     * Private constructor — this is a stateless utility class.
     */
    private CatalogProductParser() {
        throw new AssertionError("No CatalogProductParser instances for you!");
    }

    /**
     * Parses an array of GraphQL product objects into a list of
     * {@link BusinessCatalogEntry} values.
     *
     * <p>This helper is shared between {@link QueryCatalogMexResponse} and
     * {@link QueryProductCollectionsMexResponse} since WA Web's
     * {@code WAWebBizParseProductGraphql.parseProductGraphQL} applies to
     * both response shapes.
     *
     * @implNote WAWebBizParseProductGraphql.parseProductGraphQL: Cobalt
     * restricts the parse to the fields surfaced by
     * {@link BusinessCatalogEntry} (id, retailer_id, name, description,
     * url, currency, price, visibility, first image URL, status, stock
     * availability). The remaining WA Web fields (variants, compliance,
     * videos, sale price) are intentionally dropped since Cobalt does not
     * expose them yet.
     * @param array the GraphQL products array, possibly {@code null}
     * @return the parsed entries, never {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebBizParseProductGraphql", exports = "parseProductGraphQL",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static List<BusinessCatalogEntry> parseProducts(JSONArray array) {
        if (array == null || array.isEmpty()) {
            return List.of();
        }
        var out = new ArrayList<BusinessCatalogEntry>(array.size());
        for (var i = 0; i < array.size(); i++) {
            parseProduct(array.getJSONObject(i)).ifPresent(out::add);
        }
        return List.copyOf(out);
    }

    /**
     * Parses a single GraphQL product object into a
     * {@link BusinessCatalogEntry}.
     *
     * @param obj the GraphQL product object, possibly {@code null}
     * @return the parsed entry, or {@link Optional#empty()} if {@code obj}
     *         is {@code null}
     */
    private static Optional<BusinessCatalogEntry> parseProduct(JSONObject obj) {
        if (obj == null) {
            return Optional.empty();
        }
        // WAWebBizParseProductGraphql.parseProductGraphQL: id
        var id = obj.getString("id");
        // WAWebBizParseProductGraphql.parseProductGraphQL: retailer_id
        var retailerId = obj.getString("retailer_id");
        // WAWebBizParseProductGraphql.parseProductGraphQL: name defaults to empty string in WA Web via WANullthrows
        var name = obj.getString("name");
        // WAWebBizParseProductGraphql.parseProductGraphQL: description defaults to empty string
        var description = obj.getString("description");
        // WAWebBizParseProductGraphql.parseProductGraphQL: url defaults to empty string
        var urlString = obj.getString("url");
        URI url = null;
        if (urlString != null && !urlString.isEmpty()) {
            try {
                url = URI.create(urlString);
            } catch (IllegalArgumentException ignored) {
                // url stays null
            }
        }
        // WAWebBizParseProductGraphql.parseProductGraphQL: currency
        var currency = obj.getString("currency");
        // WAWebBizParseProductGraphql.parseProductGraphQL: price arrives as a stringified decimal amount
        long price = 0L;
        var priceString = obj.getString("price");
        if (priceString != null && !priceString.isEmpty()) {
            try {
                price = Long.parseLong(priceString);
            } catch (NumberFormatException ignored) {
                // price stays 0L
            }
        }
        // WAWebBizParseProductGraphql.parseProductGraphQL: is_hidden is expressed as the "ISHIDDEN_TRUE" enum string
        var hidden = "ISHIDDEN_TRUE".equals(obj.getString("is_hidden"));
        // WAWebBizParseProductGraphql.parseProductGraphQL: product_availability enum mapped via pretty name lookup
        var availability = parseAvailability(obj.getString("product_availability"));
        // WAWebBizParseProductGraphql.parseProductGraphQL: capability_to_review_status defaults to "APPROVED"
        BusinessReviewStatus reviewStatus = null;
        var statusInfo = obj.getJSONObject("status_info");
        if (statusInfo != null) {
            var status = statusInfo.getString("status");
            if (status != null) {
                reviewStatus = BusinessReviewStatus.ofName(status).orElse(null);
            }
        }
        // WAWebBizParseProductGraphql.parseProductGraphQL: image_cdn_urls "full" entry
        URI encryptedImage = null;
        var media = obj.getJSONObject("media");
        if (media != null) {
            var images = media.getJSONArray("images");
            if (images != null && !images.isEmpty()) {
                var first = images.getJSONObject(0);
                if (first != null) {
                    var originalUrl = first.getString("original_image_url");
                    if (originalUrl != null && !originalUrl.isEmpty()) {
                        try {
                            encryptedImage = URI.create(originalUrl);
                        } catch (IllegalArgumentException ignored) {
                            // encryptedImage stays null
                        }
                    }
                }
            }
        }
        // ADAPTED: BusinessCatalogEntry constructor is package-private so Cobalt uses the generated builder
        var entry = new BusinessCatalogEntryBuilder()
                .id(id)
                .encryptedImage(encryptedImage)
                .reviewStatus(reviewStatus)
                .availability(availability)
                .name(name)
                .sellerId(retailerId)
                .uri(url)
                .description(description)
                .price(price)
                .currency(currency)
                .hidden(hidden)
                .build();
        return Optional.of(entry);
    }

    /**
     * Maps the WA Web {@code product_availability} enum string to the
     * Cobalt {@link BusinessItemAvailability} constant.
     *
     * <p>WA Web uses the prefixed enum literals {@code PRODUCTAVAILABILITY_IN_STOCK},
     * {@code PRODUCTAVAILABILITY_OUT_OF_STOCK} and
     * {@code PRODUCTAVAILABILITY_UNKNOWN}. The prefix is stripped before
     * feeding the value into {@link BusinessItemAvailability#ofName(String)}
     * which expects the pretty-printed form ({@code "in stock"}).
     *
     * @implNote WAWebProductTypes.flow: defines the ProductAvailability
     * enum values; Cobalt keeps only the user-visible states.
     * @param raw the raw enum string, or {@code null}
     * @return the matched constant, or {@code null} when the input is
     *         absent or unknown
     */
    @WhatsAppWebExport(moduleName = "WAWebProductTypes.flow", exports = "ProductAvailability",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static BusinessItemAvailability parseAvailability(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        var stripped = raw.startsWith("PRODUCTAVAILABILITY_")
                ? raw.substring("PRODUCTAVAILABILITY_".length())
                : raw;
        var pretty = stripped.toLowerCase().replace('_', ' ');
        return BusinessItemAvailability.ofName(pretty).orElse(null);
    }
}
