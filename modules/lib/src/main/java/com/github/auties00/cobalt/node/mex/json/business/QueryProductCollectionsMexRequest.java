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
 * Fetches the list of product collections that belong to a WhatsApp
 * Business catalog.
 *
 * <p>Collections are named groups of products that business owners can
 * define inside a catalog. The query returns the top-level metadata of
 * each collection (id, name) along with the products nested inside it.
 * WA Web exposes both an owner and a guest entry point but both funnel the
 * same GraphQL document through {@code WAWebMexClient.fetchQuery}.
 *
 * @implNote WAWebQueryProductCollections: adapts the
 * {@code WAWebQueryProductCollectionsQuery.graphql} operation used by the
 * owner and guest collection browsing flows.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryProductCollections")
@WhatsAppWebModule(moduleName = "WAWebQueryProductCollectionsQuery.graphql")
public final class QueryProductCollectionsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code WAWebQueryProductCollectionsQuery} compiled query.
     *
     * @implNote WAWebQueryProductCollectionsQuery.graphql: corresponds to
     * the {@code params.id} field of the compiled query, extracted from
     * the current snapshot of the WA Web bundle.
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryProductCollectionsQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "9430970660362540";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching the collections query.
     *
     * <p>The constant is exposed through {@link #name()} so call sites can
     * reach the same telemetry tag WA Web emits without duplicating the
     * literal at every dispatch site.
     *
     * @implNote WAWebQueryProductCollections: WA Web invokes the operation
     *           through {@code WAWebMexClient.fetchQuery}; the native
     *           client passes the {@code params.name} of the compiled
     *           GraphQL artifact to {@code MexPerfTracker.setOperationName}.
     */
    public static final String OPERATION_NAME = "queryProductCollections";

    private final String businessJid;
    private final int collectionLimit;
    private final int itemLimit;
    private final String afterCursor;
    private final int width;
    private final int height;
    private final String directConnectionEncryptedInfo;
    private final String variantInfoFields;
    private final Integer variantThumbnailHeight;
    private final Integer variantThumbnailWidth;

    /**
     * Creates a new collections query request.
     *
     * @param businessJid                   the target business JID owning the catalog
     * @param collectionLimit               the maximum number of collections per page
     * @param itemLimit                     the maximum number of products returned
     *                                      inside every collection
     * @param afterCursor                   the pagination cursor returned by a
     *                                      previous page, or {@code null} for the
     *                                      first page
     * @param width                         the requested image width in pixels used
     *                                      when the relay rewrites image URLs
     * @param height                        the requested image height in pixels
     * @param directConnectionEncryptedInfo the optional direct-connection encrypted
     *                                      payload, or {@code null} when not used
     * @param variantInfoFields             the optional variant-info field selector,
     *                                      or {@code null} when not requested
     * @param variantThumbnailHeight        the optional variant thumbnail height in
     *                                      pixels, or {@code null} when not requested
     * @param variantThumbnailWidth         the optional variant thumbnail width in
     *                                      pixels, or {@code null} when not requested
     */
    public QueryProductCollectionsMexRequest(String businessJid, int collectionLimit, int itemLimit, String afterCursor, int width, int height,
                                             String directConnectionEncryptedInfo, String variantInfoFields,
                                             Integer variantThumbnailHeight, Integer variantThumbnailWidth) {
        this.businessJid = businessJid;
        this.collectionLimit = collectionLimit;
        this.itemLimit = itemLimit;
        this.afterCursor = afterCursor;
        this.width = width;
        this.height = height;
        this.directConnectionEncryptedInfo = directConnectionEncryptedInfo;
        this.variantInfoFields = variantInfoFields;
        this.variantThumbnailHeight = variantThumbnailHeight;
        this.variantThumbnailWidth = variantThumbnailWidth;
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
     * @implNote WAWebQueryProductCollections: mirrors the
     * {@code request.collections} variable shape with
     * {@code biz_jid}, {@code collection_limit}, {@code item_limit},
     * {@code after}, {@code width}, {@code height},
     * {@code direct_connection_encrypted_info},
     * {@code variant_info_fields}, {@code variant_thumbnail_height} and
     * {@code variant_thumbnail_width}, in that order, with every numeric
     * field stringified as WA expects. Optional string fields and the
     * {@code after} cursor are emitted as JSON {@code null} when absent
     * to preserve byte-for-byte parity with the WA Web payload.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryProductCollections", exports = "default",
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
            writer.writeName("collections");
            writer.writeColon();
            writer.startObject();
            // WAWebQueryProductCollections.default: biz_jid: a.toString()
            writer.writeName("biz_jid");
            writer.writeColon();
            writer.writeString(businessJid);
            // WAWebQueryProductCollections.default: collection_limit: String(s)
            writer.writeName("collection_limit");
            writer.writeColon();
            writer.writeString(Integer.toString(collectionLimit));
            // WAWebQueryProductCollections.default: item_limit: String(c)
            writer.writeName("item_limit");
            writer.writeColon();
            writer.writeString(Integer.toString(itemLimit));
            // WAWebQueryProductCollections.default: after: r (nullable string passthrough)
            writer.writeName("after");
            writer.writeColon();
            if (afterCursor == null) {
                writer.writeNull();
            } else {
                writer.writeString(afterCursor);
            }
            // WAWebQueryProductCollections.default: width: String(_)
            writer.writeName("width");
            writer.writeColon();
            writer.writeString(Integer.toString(width));
            // WAWebQueryProductCollections.default: height: String(l)
            writer.writeName("height");
            writer.writeColon();
            writer.writeString(Integer.toString(height));
            // WAWebQueryProductCollections.default: direct_connection_encrypted_info: i
            writer.writeName("direct_connection_encrypted_info");
            writer.writeColon();
            if (directConnectionEncryptedInfo == null) {
                writer.writeNull();
            } else {
                writer.writeString(directConnectionEncryptedInfo);
            }
            // WAWebQueryProductCollections.default: variant_info_fields: d
            writer.writeName("variant_info_fields");
            writer.writeColon();
            if (variantInfoFields == null) {
                writer.writeNull();
            } else {
                writer.writeString(variantInfoFields);
            }
            // WAWebQueryProductCollections.default: variant_thumbnail_height: m!=null?String(m):null
            writer.writeName("variant_thumbnail_height");
            writer.writeColon();
            if (variantThumbnailHeight == null) {
                writer.writeNull();
            } else {
                writer.writeString(Integer.toString(variantThumbnailHeight));
            }
            // WAWebQueryProductCollections.default: variant_thumbnail_width: p!=null?String(p):null
            writer.writeName("variant_thumbnail_width");
            writer.writeColon();
            if (variantThumbnailWidth == null) {
                writer.writeNull();
            } else {
                writer.writeString(Integer.toString(variantThumbnailWidth));
            }
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
