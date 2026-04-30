package com.github.auties00.cobalt.node.mex.json.community;

import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.mex.MexOperation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

/**
 * Fetches the current participant count for one or more subgroups inside a
 * community.
 *
 * <p>This query is used to keep participant counts fresh without reloading
 * full subgroup metadata. Only the subgroup id and total participant count
 * are returned. It is typically issued on demand when displaying or sorting
 * subgroups in the community panel.
 */
public final class QuerySubgroupParticipantCountMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled
     * {@code WAWebMexQuerySubgroupParticipantCountJobQuery} GraphQL operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexQuerySubgroupParticipantCountJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "24079399904996141";

    /**
     * The GraphQL operation name fed into {@code MexPerfTracker.setOperationName}
     * when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexQuerySubgroupParticipantCountJobQuery.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "mexQuerySubgroupParticipantCount";

    private final String input;

    /**
     * Constructs a new request carrying the serialised list of subgroup ids
     * to query.
     *
     * @param input the serialised input variable, may be {@code null} to
     *              omit
     */
    public QuerySubgroupParticipantCountMexRequest(String input) {
        this.input = input;
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
     * Serialises the GraphQL variables and wraps them in a {@code w:mex} IQ
     * stanza.
     *
     * @return the IQ {@link NodeBuilder} ready to be built and dispatched
     */
    @WhatsAppWebExport(moduleName = "WAWebMexQuerySubgroupParticipantCountJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            if (input != null) {
                writer.writeName("input");
                writer.writeColon();
                writer.writeString(input);
            }
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
