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
 * Fetches the list of product collections that belong to a WhatsApp Business
 * catalog.
 *
 * <p>Collections are named groups of products that business owners can define
 * inside a catalog. The query returns the top-level metadata of each
 * collection (id, name) along with the products nested inside it. WA Web
 * exposes both an owner and a guest entry point but both funnel the same
 * GraphQL document.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryProductCollections")
@WhatsAppWebModule(moduleName = "WAWebQueryProductCollectionsQuery.graphql")
public final class QueryProductCollectionsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled
     * {@code WAWebQueryProductCollectionsQuery} GraphQL operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryProductCollectionsQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "9430970660362540";

    /**
     * The GraphQL operation name fed into {@code MexPerfTracker.setOperationName}
     * when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryProductCollections", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
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
     * @param businessJid                   the target business JID owning
     *                                      the catalog
     * @param collectionLimit               the maximum number of
     *                                      collections per page
     * @param itemLimit                     the maximum number of products
     *                                      returned inside every collection
     * @param afterCursor                   the pagination cursor returned
     *                                      by a previous page, or
     *                                      {@code null} for the first page
     * @param width                         the requested image width in
     *                                      pixels used when the relay
     *                                      rewrites image URLs
     * @param height                        the requested image height in
     *                                      pixels
     * @param directConnectionEncryptedInfo the optional direct-connection
     *                                      encrypted payload, or
     *                                      {@code null} when not used
     * @param variantInfoFields             the optional variant-info field
     *                                      selector, or {@code null} when
     *                                      not requested
     * @param variantThumbnailHeight        the optional variant thumbnail
     *                                      height in pixels, or
     *                                      {@code null} when not requested
     * @param variantThumbnailWidth         the optional variant thumbnail
     *                                      width in pixels, or {@code null}
     *                                      when not requested
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
            writer.writeName("biz_jid");
            writer.writeColon();
            writer.writeString(businessJid);
            writer.writeName("collection_limit");
            writer.writeColon();
            writer.writeString(Integer.toString(collectionLimit));
            writer.writeName("item_limit");
            writer.writeColon();
            writer.writeString(Integer.toString(itemLimit));
            writer.writeName("after");
            writer.writeColon();
            if (afterCursor == null) {
                writer.writeNull();
            } else {
                writer.writeString(afterCursor);
            }
            writer.writeName("width");
            writer.writeColon();
            writer.writeString(Integer.toString(width));
            writer.writeName("height");
            writer.writeColon();
            writer.writeString(Integer.toString(height));
            writer.writeName("direct_connection_encrypted_info");
            writer.writeColon();
            if (directConnectionEncryptedInfo == null) {
                writer.writeNull();
            } else {
                writer.writeString(directConnectionEncryptedInfo);
            }
            writer.writeName("variant_info_fields");
            writer.writeColon();
            if (variantInfoFields == null) {
                writer.writeNull();
            } else {
                writer.writeString(variantInfoFields);
            }
            writer.writeName("variant_thumbnail_height");
            writer.writeColon();
            if (variantThumbnailHeight == null) {
                writer.writeNull();
            } else {
                writer.writeString(Integer.toString(variantThumbnailHeight));
            }
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
