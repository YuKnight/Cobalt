package com.github.auties00.cobalt.node.mex.json.business;

import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.mex.MexOperation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

/**
 * Fetches the list of products that belong to a WhatsApp Business catalog.
 *
 * <p>A catalog is the flat storefront attached to a business JID. The query
 * returns the products it contains together with their core metadata such as
 * name, description, currency, price and image URLs. WA Web exposes the
 * operation under two names depending on whether the caller is the catalog
 * owner ({@code queryCatalogGraphQLByOwner}) or a guest browser
 * ({@code queryCatalogGraphQLByGuest}); both paths materialise the same
 * GraphQL query.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryCatalog")
@WhatsAppWebModule(moduleName = "WAWebQueryCatalogQuery.graphql")
public final class QueryCatalogMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled
     * {@code WAWebQueryCatalogQuery} GraphQL operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryCatalogQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "9916553288394782";

    /**
     * The GraphQL operation name fed into {@code MexPerfTracker.setOperationName}
     * when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryCatalog", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "queryCatalog";

    private final String catalogJid;
    private final int limit;
    private final int width;
    private final int height;
    private final String afterCursor;
    private final boolean allowShopSource;

    /**
     * Creates a new catalog query request with the WA Web default
     * {@code allowShopSource=false}.
     *
     * @param catalogJid  the target business JID owning the catalog
     * @param limit       the page size (maximum number of products returned
     *                    per page)
     * @param width       the requested image width in pixels used when the
     *                    relay rewrites image URLs
     * @param height      the requested image height in pixels
     * @param afterCursor the pagination cursor returned by a previous page,
     *                    or {@code null} for the first page
     */
    public QueryCatalogMexRequest(String catalogJid, int limit, int width, int height, String afterCursor) {
        this(catalogJid, limit, width, height, afterCursor, false);
    }

    /**
     * Creates a new catalog query request.
     *
     * @param catalogJid      the target business JID owning the catalog
     * @param limit           the page size (maximum number of products
     *                        returned per page)
     * @param width           the requested image width in pixels used when
     *                        the relay rewrites image URLs
     * @param height          the requested image height in pixels
     * @param afterCursor     the pagination cursor returned by a previous
     *                        page, or {@code null} for the first page
     * @param allowShopSource whether the request opts into the WhatsApp shop
     *                        source surface, mapped to the WA Web
     *                        {@code allow_shop_source} enum string
     */
    public QueryCatalogMexRequest(String catalogJid, int limit, int width, int height, String afterCursor, boolean allowShopSource) {
        this.catalogJid = catalogJid;
        this.limit = limit;
        this.width = width;
        this.height = height;
        this.afterCursor = afterCursor;
        this.allowShopSource = allowShopSource;
    }

    /**
     * Returns the compiled GraphQL query identifier.
     *
     * @return the constant {@link #QUERY_ID}, never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name.
     *
     * @return the constant {@link #OPERATION_NAME}, never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the WhatsApp
     * relay.
     *
     * @implNote Mirrors the exact WA Web {@code request.product_catalog}
     *           variable shape with {@code jid}, {@code allow_shop_source}
     *           (enum string), {@code width}, {@code height},
     *           {@code direct_connection_encrypted_info} (always
     *           {@code null} from this surface), {@code limit}, {@code after}
     *           (always emitted, possibly {@code null}),
     *           {@code catalog_session_id}, {@code variant_info_fields},
     *           {@code variant_thumbnail_height} and
     *           {@code variant_thumbnail_width}. WA Web emits explicit
     *           {@code null} values for the optional fields rather than
     *           omitting the keys.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryCatalog", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            writer.writeName("request");
            writer.writeColon();
            writer.startObject();
            writer.writeName("product_catalog");
            writer.writeColon();
            writer.startObject();
            writer.writeName("jid");
            writer.writeColon();
            writer.writeString(catalogJid);
            writer.writeName("allow_shop_source");
            writer.writeColon();
            writer.writeString(allowShopSource ? "ALLOWSHOPSOURCE_TRUE" : "ALLOWSHOPSOURCE_FALSE");
            writer.writeName("width");
            writer.writeColon();
            writer.writeString(Integer.toString(width));
            writer.writeName("height");
            writer.writeColon();
            writer.writeString(Integer.toString(height));
            writer.writeName("direct_connection_encrypted_info");
            writer.writeColon();
            writer.writeNull();
            writer.writeName("limit");
            writer.writeColon();
            writer.writeString(Integer.toString(limit));
            writer.writeName("after");
            writer.writeColon();
            if (afterCursor != null) {
                writer.writeString(afterCursor);
            } else {
                writer.writeNull();
            }
            writer.writeName("catalog_session_id");
            writer.writeColon();
            writer.writeNull();
            writer.writeName("variant_info_fields");
            writer.writeColon();
            writer.writeNull();
            writer.writeName("variant_thumbnail_height");
            writer.writeColon();
            writer.writeNull();
            writer.writeName("variant_thumbnail_width");
            writer.writeColon();
            writer.writeNull();
            writer.endObject();
            writer.endObject();
            writer.endObject();
            writer.endObject();
            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return MexOperation.Request.Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
