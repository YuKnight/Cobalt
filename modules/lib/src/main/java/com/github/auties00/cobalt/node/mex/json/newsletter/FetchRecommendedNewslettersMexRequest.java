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
import java.util.OptionalLong;

/**
 * Fetches a list of newsletters recommended to the authenticated user.
 *
 * <p>The recommendation engine returns channels that the WhatsApp backend estimates are relevant to the user based on follow history, directory browsing and regional signals.
 *
 * @implNote WAWebMexFetchRecommendedNewslettersJob: adapts the {@code mexFetchRecommendedNewsletters} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchRecommendedNewslettersJob")
public final class FetchRecommendedNewslettersMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchRecommendedNewsletters} compiled query.
     *
     * @implNote WAWebMexFetchRecommendedNewslettersJobQuery.graphql: corresponds to the
     * {@code params.id} field of the compiled GraphQL document
     * ({@code id:"25806748772361516"}) bundled in the WA Web client.
     */
    public static final String QUERY_ID = "25806748772361516";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchRecommendedNewsletters
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchRecommendedNewslettersJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexFetchRecommendedNewsletters"}.
     */
    public static final String OPERATION_NAME = "mexFetchRecommendedNewsletters";
    private final Long limit;
    private final List<String> countryCodes;
    private final boolean fetchStatusMetadata;

    /**
     * Constructs a new request with the given variables.
     *
     * @param limit               the maximum number of recommended
     *                            newsletters to return, or {@code null}
     *                            to omit the field (the relay then
     *                            applies its default page size)
     * @param countryCodes        the list of ISO country codes used to
     *                            scope the recommendation, or
     *                            {@code null} to omit the field
     * @param fetchStatusMetadata {@code true} to request the optional
     *                            {@code status_metadata} sub-selection,
     *                            mirroring
     *                            {@code WAWebNewsletterGatingUtils.isNewsletterStatusReceiverEnabled()}
     *                            in the JS source
     */
    public FetchRecommendedNewslettersMexRequest(Long limit, List<String> countryCodes, boolean fetchStatusMetadata) {
        this.limit = limit;
        this.countryCodes = countryCodes;
        this.fetchStatusMetadata = fetchStatusMetadata;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchRecommendedNewslettersJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchRecommendedNewslettersJob: WA Web's
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
     * @implNote WAWebMexFetchRecommendedNewslettersJob.mexFetchRecommendedNewsletters: WA Web constructs the
     * {@code variables} object inline as
     * {@code {input:{limit:t,country_codes:r}, fetch_status_metadata: <bool>}}
     * and delegates to {@code WAWebMexClient.fetchQuery}. Cobalt writes
     * the JSON directly via {@code fastjson2.JSONWriter} and wraps it
     * through {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchRecommendedNewslettersJob", exports = "mexFetchRecommendedNewsletters",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchRecommendedNewslettersJob.mexFetchRecommendedNewsletters
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchRecommendedNewslettersJob.mexFetchRecommendedNewsletters
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexFetchRecommendedNewslettersJob.mexFetchRecommendedNewsletters
            // Emits {input:{limit:t, country_codes:r}}; the inner object is emitted even when both children are null
            // to mirror the JS object literal shape.
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();
            if (limit != null) {
                writer.writeName("limit");
                writer.writeColon();
                writer.writeInt64(limit);
            }
            if (countryCodes != null) {
                writer.writeName("country_codes");
                writer.writeColon();
                writer.startArray();
                for (var i = 0; i < countryCodes.size(); i++) {
                    if (i > 0) {
                        writer.writeComma();
                    }
                    writer.writeString(countryCodes.get(i));
                }
                writer.endArray();
            }
            writer.endObject();

            // WAWebMexFetchRecommendedNewslettersJob.mexFetchRecommendedNewsletters
            // Emits the sibling {fetch_status_metadata: <bool>} field, mirroring the JS variables literal
            writer.writeName("fetch_status_metadata");
            writer.writeColon();
            writer.writeBool(fetchStatusMetadata);

            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchRecommendedNewslettersJob.mexFetchRecommendedNewsletters
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
