package com.github.auties00.cobalt.node.mex.json.group;

import com.alibaba.fastjson2.JSON;
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
import java.util.Optional;

/**
 * Reports whether a group is flagged as "internal" by the WhatsApp relay.
 *
 * <p>Internal groups are the Meta-side staff or testing groups whose
 * lifecycle differs from regular consumer groups; the flag is exposed under
 * the {@code XWA2*Properties.internal} scalar across all four group
 * variants ({@code XWA2GroupRegularGroup}, {@code XWA2CommunityGroup},
 * {@code XWA2CommunityDefaultSubGroup}, {@code XWA2CommunitySubGroup}).
 * The query collapses the four inline fragments into a single boolean by
 * walking the response shape that the relay actually populates.
 *
 * @implNote WAWebMexFetchGroupIsInternalJob: adapts the
 * {@code mexFetchGroupIsInternal} GraphQL query, which in WA Web is
 * invoked via {@code WAWebMexClient.fetchQuery} and whose response is
 * unwrapped by the same module to expose the {@code internal} scalar.
 * Cobalt models the request and response as sibling variants of a sealed
 * interface rather than a free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchGroupIsInternalJob")
public final class FetchGroupIsInternalMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchGroupIsInternal} compiled query.
     *
     * @implNote WAWebMexFetchGroupIsInternalJobQuery.graphql: corresponds
     * to the compiled document id registered for the
     * {@code mexFetchGroupIsInternal} query.
     */
    public static final String QUERY_ID = "34119218944390847";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchGroupIsInternal
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchGroupIsInternalJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexFetchGroupIsInternal"}.
     */
    public static final String OPERATION_NAME = "mexFetchGroupIsInternal";
    private final String groupId;

    /**
     * Constructs a request that asks the relay whether the given group
     * is flagged as internal.
     *
     * @implNote WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal:
     * WA Web's {@code function*(e)} accepts the group identifier
     * {@code e} and constructs {@code {id: e}} as the GraphQL
     * variables payload.
     * @param groupId the group identifier emitted as the {@code id}
     *                variable; the compiled query injects the literal
     *                {@code query_context: "INTERACTIVE"} alongside it
     */
    public FetchGroupIsInternalMexRequest(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchGroupIsInternalJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchGroupIsInternalJob: WA Web's
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
     * @implNote WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal:
     * WA Web constructs the {@code variables} object inline as
     * {@code {id: e}} and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON
     * directly via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchGroupIsInternalJob", exports = "mexFetchGroupIsInternal",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal
            // Emits the id variable when present
            if (groupId != null) {
                writer.writeName("id");
                writer.writeColon();
                writer.writeString(groupId);
            }
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal
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
