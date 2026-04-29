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
 * <p>A catalog is the flat storefront attached to a business JID; this query
 * returns the products it contains along with their core metadata such as
 * name, description, currency, price, and image URLs. WA Web exposes the
 * operation under two names depending on whether the caller is the catalog
 * owner ({@code queryCatalogGraphQLByOwner}) or a guest browser
 * ({@code queryCatalogGraphQLByGuest}); both paths materialise the same
 * GraphQL query which the relay dispatches through
 * {@code WAWebMexClient.fetchQuery}.
 *
 * @implNote WAWebQueryCatalog: adapts the
 * {@code WAWebQueryCatalogQuery.graphql} operation used both for the owner
 * and guest queryCatalog flows. The WA Web request pre-serialises the
 * GraphQL variables under a {@code request.product_catalog} envelope; this
 * class reproduces the exact shape directly.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryCatalog")
@WhatsAppWebModule(moduleName = "WAWebQueryCatalogQuery.graphql")
public final class QueryCatalogMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code WAWebQueryCatalogQuery} compiled query.
     *
     * @implNote WAWebQueryCatalogQuery.graphql: corresponds to the
     * {@code params.id} field of the compiled query, extracted from the
     * current snapshot of the WA Web bundle.
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryCatalogQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "9916553288394782";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching the catalog query.
     *
     * <p>The constant is exposed through {@link #name()} so call sites can
     * reach the same telemetry tag WA Web emits without duplicating the
     * literal at every dispatch site.
     *
     * @implNote WAWebQueryCatalog: WA Web invokes the operation through
     *           {@code WAWebMexClient.fetchQuery}; the native client passes
     *           the {@code params.name} of the compiled GraphQL artifact to
     *           {@code MexPerfTracker.setOperationName}.
     */
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
     * @param limit       the page size (maximum number of products
     *                    returned per page)
     * @param width       the requested image width in pixels used when
     *                    the relay rewrites image URLs
     * @param height      the requested image height in pixels
     * @param afterCursor the pagination cursor returned by a previous
     *                    page, or {@code null} for the first page
     */
    public QueryCatalogMexRequest(String catalogJid, int limit, int width, int height, String afterCursor) {
        this(catalogJid, limit, width, height, afterCursor, false);
    }

    /**
     * Creates a new catalog query request.
     *
     * @param catalogJid       the target business JID owning the
     *                         catalog
     * @param limit            the page size (maximum number of products
     *                         returned per page)
     * @param width            the requested image width in pixels used
     *                         when the relay rewrites image URLs
     * @param height           the requested image height in pixels
     * @param afterCursor      the pagination cursor returned by a
     *                         previous page, or {@code null} for the
     *                         first page
     * @param allowShopSource  whether the request opts into the
     *                         WhatsApp shop source surface; mapped to
     *                         the WA Web {@code allow_shop_source}
     *                         enum string
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
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @return the constant {@link #QUERY_ID}; never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name projected from
     * {@link #OPERATION_NAME}.
     *
     * @return the constant {@link #OPERATION_NAME}; never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the
     * WhatsApp relay.
     *
     * @implNote WAWebQueryCatalog: mirrors the exact WA Web
     * {@code request.product_catalog} variable shape, in order:
     * {@code jid}, {@code allow_shop_source} (enum string),
     * {@code width}, {@code height}, {@code direct_connection_encrypted_info}
     * (always {@code null} from this surface),
     * {@code limit}, {@code after} (always emitted, possibly {@code null}),
     * {@code catalog_session_id}, {@code variant_info_fields},
     * {@code variant_thumbnail_height}, {@code variant_thumbnail_width}.
     * WA Web emits explicit {@code null} values for the optional fields
     * rather than omitting the keys.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryCatalog", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebQueryCatalog.default
        // Opens a UTF-8 JSON writer that will serialise the request.product_catalog variables envelope
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
            // WAWebQueryCatalog.default: jid: catalogWid.toString()
            writer.writeName("jid");
            writer.writeColon();
            writer.writeString(catalogJid);
            // WAWebQueryCatalog.default: allow_shop_source: u ? "ALLOWSHOPSOURCE_TRUE" : "ALLOWSHOPSOURCE_FALSE"
            writer.writeName("allow_shop_source");
            writer.writeColon();
            writer.writeString(allowShopSource ? "ALLOWSHOPSOURCE_TRUE" : "ALLOWSHOPSOURCE_FALSE");
            // WAWebQueryCatalog.default: width: String(y)
            writer.writeName("width");
            writer.writeColon();
            writer.writeString(Integer.toString(width));
            // WAWebQueryCatalog.default: height: String(p)
            writer.writeName("height");
            writer.writeColon();
            writer.writeString(Integer.toString(height));
            // WAWebQueryCatalog.default: direct_connection_encrypted_info: m (always null at this surface)
            writer.writeName("direct_connection_encrypted_info");
            writer.writeColon();
            writer.writeNull();
            // WAWebQueryCatalog.default: limit: String(_)
            writer.writeName("limit");
            writer.writeColon();
            writer.writeString(Integer.toString(limit));
            // WAWebQueryCatalog.default: after: l (afterCursor, may be null but key always present)
            writer.writeName("after");
            writer.writeColon();
            if (afterCursor != null) {
                writer.writeString(afterCursor);
            } else {
                writer.writeNull();
            }
            // WAWebQueryCatalog.default: catalog_session_id: d (checkmarkCollectionId, null at this surface)
            writer.writeName("catalog_session_id");
            writer.writeColon();
            writer.writeNull();
            // WAWebQueryCatalog.default: variant_info_fields: f (null at this surface)
            writer.writeName("variant_info_fields");
            writer.writeColon();
            writer.writeNull();
            // WAWebQueryCatalog.default: variant_thumbnail_height: g!=null ? String(g) : null
            writer.writeName("variant_thumbnail_height");
            writer.writeColon();
            writer.writeNull();
            // WAWebQueryCatalog.default: variant_thumbnail_width: h!=null ? String(h) : null
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
