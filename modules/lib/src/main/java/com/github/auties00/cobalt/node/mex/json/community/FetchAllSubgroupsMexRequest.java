package com.github.auties00.cobalt.node.mex.json.community;

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
 * Fetches every subgroup belonging to a WhatsApp community, including the
 * announcement (default) subgroup and all regular subgroups together with
 * their membership approval state.
 *
 * <p>This is the primary query issued when loading a community. The response
 * carries the default subgroup (always a single announcement group) and the
 * full edge list of regular subgroups, each with its id, subject, properties
 * (general chat flag, membership approval mode, hidden flag) and the total
 * count of pending membership approval requests scoped to the current user.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchAllSubgroupsJob")
public final class FetchAllSubgroupsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled
     * {@code WAWebMexFetchAllSubgroupsJobQuery} GraphQL operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAllSubgroupsJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "9935467776504344";

    /**
     * The GraphQL operation name fed into {@code MexPerfTracker.setOperationName}
     * when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAllSubgroupsJob", exports = "mexFetchAllSubgroups",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "mexCommunityGetSubgroups";

    private final String groupId;
    private final String queryContext;
    private final String subGroupHintId;

    /**
     * Constructs a new request with the three GraphQL variables.
     *
     * @param groupId        the community parent group id, may be
     *                       {@code null} to omit
     * @param queryContext   the optional query-context tag, may be
     *                       {@code null} to omit
     * @param subGroupHintId the optional subgroup hint identifier, may be
     *                       {@code null} to omit
     */
    public FetchAllSubgroupsMexRequest(String groupId, String queryContext, String subGroupHintId) {
        this.groupId = groupId;
        this.queryContext = queryContext;
        this.subGroupHintId = subGroupHintId;
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
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAllSubgroupsJob", exports = "mexFetchAllSubgroups",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            if (groupId != null) {
                writer.writeName("group_id");
                writer.writeColon();
                writer.writeString(groupId);
            }
            if (queryContext != null) {
                writer.writeName("query_context");
                writer.writeColon();
                writer.writeString(queryContext);
            }
            if (subGroupHintId != null) {
                writer.writeName("sub_group_hint_id");
                writer.writeColon();
                writer.writeString(subGroupHintId);
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
