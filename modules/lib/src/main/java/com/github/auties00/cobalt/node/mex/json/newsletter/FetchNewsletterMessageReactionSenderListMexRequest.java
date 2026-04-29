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
 *
 * @implNote WAWebMexFetchNewsletterMessageReactionSenderListJob: adapts the {@code mexFetchNewsletterMessageReactionSenderList} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterMessageReactionSenderListJob")
public final class FetchNewsletterMessageReactionSenderListMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterMessageReactionSenderList} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterMessageReactionSenderListJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexFetchNewsletterMessageReactionSenderList} query.
     */
    public static final String QUERY_ID = "29575462448733991";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterMessageReactionSenderList
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchNewsletterMessageReactionSenderListJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexFetchNewsletterMessageReactionSenderList"}.
     */
    public static final String OPERATION_NAME = "mexFetchNewsletterMessageReactionSenderList";
    private final String newsletterId;
    private final long serverId;

    /**
     * Creates a new request that targets the reaction senders for
     * {@code (newsletterId, serverId)}.
     *
     * @implNote WAWebMexFetchNewsletterMessageReactionSenderListJob.mexFetchNewsletterMessageReactionSenderList: mirrors
     * the JS function signature {@code function c(e, t)} where {@code e} is
     * the newsletter id and {@code t} is the server-assigned message id.
     * Both values land in the nested {@code variables.input} object.
     * @param newsletterId the newsletter id (becomes {@code input.id});
     *                     must not be {@code null}
     * @param serverId     the server-assigned message id (becomes
     *                     {@code input.server_id})
     */
    public FetchNewsletterMessageReactionSenderListMexRequest(String newsletterId, long serverId) {
        this.newsletterId = Objects.requireNonNull(newsletterId, "newsletterId");
        this.serverId = serverId;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchNewsletterMessageReactionSenderListJob: WA Web reads the {@code params.id}
     *           field of the compiled artifact and forwards it to
     *           {@code MexPerfTracker.setQueryId}; Cobalt projects
     *           the same scalar through this accessor.
     * @return the constant {@link #QUERY_ID}; never
     *         {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name projected from
     * {@link #OPERATION_NAME}.
     *
     * @implNote WAWebMexFetchNewsletterMessageReactionSenderListJob: WA Web's
     *           {@code WAWebMexNativeClient.fetchQuery} reads
     *           {@code params.name} from the compiled GraphQL
     *           artifact and forwards it to
     *           {@code MexPerfTracker.setOperationName}; Cobalt
     *           projects the same scalar through this accessor.
     * @return the constant {@link #OPERATION_NAME};
     *         never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the
     * WhatsApp relay.
     *
     * @implNote WAWebMexFetchNewsletterMessageReactionSenderListJob.mexFetchNewsletterMessageReactionSenderList: WA Web builds the
     * variables object as {@code {input: {id: e, server_id: t}}} inline and
     * delegates to {@code WAWebMexClient.fetchQuery}. Cobalt writes the
     * same nested envelope directly via {@code fastjson2.JSONWriter} and
     * wraps it through {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterMessageReactionSenderListJob", exports = "mexFetchNewsletterMessageReactionSenderList",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchNewsletterMessageReactionSenderListJob.mexFetchNewsletterMessageReactionSenderList
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchNewsletterMessageReactionSenderListJob.mexFetchNewsletterMessageReactionSenderList
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // WAWebMexFetchNewsletterMessageReactionSenderListJob.mexFetchNewsletterMessageReactionSenderList:
            // var a = { input: { id: e, server_id: t } }
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

            // ADAPTED: WAWebMexFetchNewsletterMessageReactionSenderListJob.mexFetchNewsletterMessageReactionSenderList
            // Flushes the JSON buffer into a StringWriter and wraps it in the shared MEX IQ envelope
            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
