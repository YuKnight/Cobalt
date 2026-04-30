package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.mex.MexOperation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Fetches the list of users who reacted to a newsletter message with a given emoji.
 *
 * <p>Admins can view who reacted to a specific newsletter message with a chosen reaction emoji. The result is paginated so large reaction sets can be walked in batches.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterMessageReactionSenderListJob")
public final class FetchNewsletterMessageReactionSenderListMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterMessageReactionSenderList} compiled query.
     */
    public static final String QUERY_ID = "29575462448733991";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterMessageReactionSenderList
     * operation.
     */
    public static final String OPERATION_NAME = "mexFetchNewsletterMessageReactionSenderList";
    private final String newsletterId;
    private final long serverId;

    /**
     * Creates a new request that targets the reaction senders for
     * {@code (newsletterId, serverId)}.
     *     */
    public FetchNewsletterMessageReactionSenderListMexRequest(String newsletterId, long serverId) {
        this.newsletterId = Objects.requireNonNull(newsletterId, "newsletterId");
        this.serverId = serverId;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @return the constant {@link #QUERY_ID}, never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name projected from
     * {@link #OPERATION_NAME}.
     *
     * @return the constant {@link #OPERATION_NAME}, never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the
     * WhatsApp relay.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterMessageReactionSenderListJob", exports = "mexFetchNewsletterMessageReactionSenderList",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();
            writer.writeName("id");
            writer.writeColon();
            writer.writeString(newsletterId);
            writer.writeName("server_id");
            writer.writeColon();
            writer.writeInt64(serverId);
            writer.endObject();
            writer.endObject();
            writer.endObject();

            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
