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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fetches metadata for every newsletter followed by the authenticated user.
 *
 * <p>WA Web uses this query during login and periodic syncs to hydrate the local newsletter list. The response provides a collection of newsletter entries each with thread metadata, viewer role, and state.
 *
 * @implNote WAWebMexFetchAllNewslettersMetadataJob: adapts the {@code mexFetchAllNewsletters} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchAllNewslettersMetadataJob")
public final class FetchAllNewslettersMetadataMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchAllNewslettersMetadata} compiled query.
     *
     * @implNote WAWebMexFetchAllNewslettersMetadataJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexFetchAllNewsletters} query.
     */
    public static final String QUERY_ID = "25399611239711790";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchAllNewsletters
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchAllNewslettersMetadataJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexFetchAllNewsletters"}.
     */
    public static final String OPERATION_NAME = "mexFetchAllNewsletters";
    private final Boolean fetchWamoSub;
    private final Boolean fetchStatusMetadata;

    /**
     * Constructs a request that selects only the {@code fetch_wamo_sub}
     * gating flag.
     *
     * @implNote Convenience overload kept for backwards compatibility with
     *           callers that predate the {@code fetch_status_metadata}
     *           variable.
     * @param fetchWamoSub the value of the {@code fetch_wamo_sub} GraphQL
     *                     variable, or {@code null} to omit the field
     */
    public FetchAllNewslettersMetadataMexRequest(Boolean fetchWamoSub) {
        this(fetchWamoSub, null);
    }

    /**
     * Constructs a request with both GraphQL gating variables.
     *
     * @implNote WAWebMexFetchAllNewslettersMetadataJob.mexFetchAllNewsletters:
     *           WA Web invokes
     *           {@code WAWebMexClient.fetchQuery(query, {fetch_wamo_sub,
     *           fetch_status_metadata})}; Cobalt mirrors the same two
     *           variables on the request object.
     * @param fetchWamoSub        the value of the {@code fetch_wamo_sub}
     *                            variable, or {@code null} to omit
     * @param fetchStatusMetadata the value of the
     *                            {@code fetch_status_metadata} variable,
     *                            or {@code null} to omit
     */
    public FetchAllNewslettersMetadataMexRequest(Boolean fetchWamoSub, Boolean fetchStatusMetadata) {
        this.fetchWamoSub = fetchWamoSub;
        this.fetchStatusMetadata = fetchStatusMetadata;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchAllNewslettersMetadataJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchAllNewslettersMetadataJob: WA Web's
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
     * @implNote WAWebMexFetchAllNewslettersMetadataJob.mexFetchAllNewsletters: WA Web constructs the
     * {@code variables} object inline ({@code {fetch_wamo_sub: ...,
     * fetch_status_metadata: ...}}) and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAllNewslettersMetadataJob", exports = "mexFetchAllNewsletters",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchAllNewslettersMetadataJob.mexFetchAllNewsletters
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchAllNewslettersMetadataJob.mexFetchAllNewsletters
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // WAWebMexFetchAllNewslettersMetadataJob.mexFetchAllNewsletters
            // Emits the fetch_wamo_sub boolean variable when present
            if (fetchWamoSub != null) {
                writer.writeName("fetch_wamo_sub");
                writer.writeColon();
                writer.writeBool(fetchWamoSub);
            }
            // WAWebMexFetchAllNewslettersMetadataJob.mexFetchAllNewsletters
            // Emits the fetch_status_metadata boolean variable when present
            if (fetchStatusMetadata != null) {
                writer.writeName("fetch_status_metadata");
                writer.writeColon();
                writer.writeBool(fetchStatusMetadata);
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
