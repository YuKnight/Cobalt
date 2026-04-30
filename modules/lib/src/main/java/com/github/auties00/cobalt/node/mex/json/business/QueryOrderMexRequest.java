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
 * Fetches the detail of a business order identified by a message id and a
 * server-issued token.
 *
 * <p>An order is produced when a customer submits an {@code OrderMessage} to
 * a business contact. The business response carries a sensitive token that
 * authenticates subsequent order look-ups. WA Web's
 * {@code WAWebBizQueryOrderJob.queryOrder} routes the request through the
 * GraphQL operation when the GraphQL gate is enabled and falls back to a
 * legacy {@code fb:thrift_iq} IQ otherwise. Cobalt mirrors the GraphQL path
 * and omits the {@code fb:thrift_iq} fallback.
 */
@WhatsAppWebModule(moduleName = "WAWebBizQueryOrderJob")
@WhatsAppWebModule(moduleName = "WAWebBizQueryOrderJobQuery.graphql")
public final class QueryOrderMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled
     * {@code WAWebBizQueryOrderJobQuery} GraphQL operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebBizQueryOrderJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "26593811266898374";

    /**
     * The GraphQL operation name fed into {@code MexPerfTracker.setOperationName}
     * when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebBizQueryOrderJob", exports = "queryOrder",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "queryOrder";

    private final String userJid;
    private final String orderId;
    private final String tokenBase64;
    private final int imageWidth;
    private final int imageHeight;

    /**
     * Creates a new order query request.
     *
     * @param userJid     the logged-in user JID stringified via
     *                    {@code toString()}, mirroring the WA Web
     *                    {@code WAWebUserPrefsMeUser.getMePnUserOrThrow_DO_NOT_USE().toString()}
     * @param orderId     the server-issued order identifier, typically the
     *                    id of the {@code OrderMessage} carrying the order
     * @param tokenBase64 the sensitive base64-encoded token returned by the
     *                    business with the order message
     * @param imageWidth  the requested thumbnail width in pixels used when
     *                    the relay rewrites image URLs
     * @param imageHeight the requested thumbnail height in pixels
     */
    public QueryOrderMexRequest(String userJid, String orderId, String tokenBase64, int imageWidth, int imageHeight) {
        this.userJid = userJid;
        this.orderId = orderId;
        this.tokenBase64 = tokenBase64;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
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
     * @implNote Mirrors the WA Web {@code request.order} variable shape
     *           with {@code jid}, {@code token.sensitive_string_value},
     *           {@code id}, {@code image_dimensions.height},
     *           {@code image_dimensions.width} and
     *           {@code direct_connection_encrypted_info} (omitted here).
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebBizQueryOrderJob", exports = "queryOrder",
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
            writer.writeName("order");
            writer.writeColon();
            writer.startObject();
            writer.writeName("jid");
            writer.writeColon();
            writer.writeString(userJid);
            writer.writeName("token");
            writer.writeColon();
            writer.startObject();
            writer.writeName("sensitive_string_value");
            writer.writeColon();
            writer.writeString(tokenBase64);
            writer.endObject();
            writer.writeName("id");
            writer.writeColon();
            writer.writeString(orderId);
            writer.writeName("image_dimensions");
            writer.writeColon();
            writer.startObject();
            writer.writeName("height");
            writer.writeColon();
            writer.writeInt32(imageHeight);
            writer.writeName("width");
            writer.writeColon();
            writer.writeInt32(imageWidth);
            writer.endObject();
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
