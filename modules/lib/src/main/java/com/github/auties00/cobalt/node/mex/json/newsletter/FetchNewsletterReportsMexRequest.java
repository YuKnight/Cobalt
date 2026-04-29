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
 * Fetches the list of user-submitted reports against a newsletter.
 *
 * <p>Newsletter admins and moderators use this query to review reports filed by followers, including the report reason, reporter metadata and timestamps.
 *
 * @implNote WAWebMexFetchNewsletterReportsJob: adapts the {@code mexFetchNewsletterReports} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterReportsJob")
public final class FetchNewsletterReportsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterReports} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterReportsJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexFetchNewsletterReports} query,
     * extracted from the relay-supplied {@code params.id} property.
     */
    public static final String QUERY_ID = "24241374008893508";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterReports
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchNewsletterReportsJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexFetchNewsletterReports"}.
     */
    public static final String OPERATION_NAME = "mexFetchNewsletterReports";

    /**
     * Constructs an empty request. The underlying GraphQL operation does not
     * accept arguments, so the corresponding {@code variables} object is
     * always serialised as an empty JSON object.
     */
    public FetchNewsletterReportsMexRequest() {
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchNewsletterReportsJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchNewsletterReportsJob: WA Web's
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
     * @implNote WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports: WA Web constructs the
     * {@code variables} object inline as {@code var e = {}} and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterReportsJob", exports = "mexFetchNewsletterReports",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
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
