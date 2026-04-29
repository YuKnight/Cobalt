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
 * Fetches analytics insights for a newsletter.
 *
 * <p>Insights include aggregated metrics such as views, reactions, forwards and follower growth over time. The admin dashboard consumes this query to display publisher analytics.
 *
 * @implNote WAWebMexFetchNewsletterInsightsJob: adapts the {@code mexFetchNewsletterInsights} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterInsightsJob")
public final class FetchNewsletterInsightsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterInsights} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterInsightsJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexFetchNewsletterInsights} query.
     */
    public static final String QUERY_ID = "9853618868050977";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterInsights
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchNewsletterInsightsJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexFetchNewsletterInsights"}.
     */
    public static final String OPERATION_NAME = "mexFetchNewsletterInsights";
    /**
     * The identifier of the newsletter whose insights are being fetched.
     *
     * @implNote WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights: mirrors the
     * {@code newsletter_id} field of the GraphQL {@code input} variable
     * destructured from {@code e.newsletterJid}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterInsightsJob", exports = "mexFetchNewsletterInsights",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String newsletterId;

    /**
     * The list of metric identifiers to fetch values for.
     *
     * @implNote WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights: mirrors the
     * {@code metrics} field of the GraphQL {@code input} variable
     * destructured from {@code e.requestedMetrics}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterInsightsJob", exports = "mexFetchNewsletterInsights",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final List<String> metrics;

    /**
     * Creates a request fetching the supplied metrics for the given newsletter.
     *
     * @implNote WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights: mirrors the
     * {@code {newsletterJid, requestedMetrics}} object destructured at the
     * head of the JS function.
     * @param newsletterId the newsletter identifier passed as
     *                     {@code newsletter_id}
     * @param metrics      the list of metric identifiers passed as
     *                     {@code metrics}; may be {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterInsightsJob", exports = "mexFetchNewsletterInsights",
            adaptation = WhatsAppAdaptation.DIRECT)
    public FetchNewsletterInsightsMexRequest(String newsletterId, List<String> metrics) {
        this.newsletterId = newsletterId;
        this.metrics = metrics;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchNewsletterInsightsJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchNewsletterInsightsJob: WA Web's
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
     * @implNote WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights: WA Web constructs
     * {@code {input:{newsletter_id:l, metrics:u}}} inline and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the same nested
     * variables envelope directly via {@code fastjson2.JSONWriter} and
     * wraps it through {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterInsightsJob", exports = "mexFetchNewsletterInsights",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights
            // {input:{newsletter_id:l, metrics:u}} - opens the nested input object
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();

            // WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights
            // input.newsletter_id = l (e.newsletterJid)
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }

            // WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights
            // input.metrics = u (e.requestedMetrics) - serialised as a JSON array of strings
            if (metrics != null) {
                writer.writeName("metrics");
                writer.writeColon();
                writer.startArray();
                for (var i = 0; i < metrics.size(); i++) {
                    if (i > 0) {
                        writer.writeComma();
                    }
                    writer.writeString(metrics.get(i));
                }
                writer.endArray();
            }

            writer.endObject();
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights
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
