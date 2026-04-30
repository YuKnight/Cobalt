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
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterInsightsJob")
public final class FetchNewsletterInsightsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterInsights} compiled query.
     */
    public static final String QUERY_ID = "9853618868050977";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterInsights
     * operation.
     */
    public static final String OPERATION_NAME = "mexFetchNewsletterInsights";
    /**
     * The identifier of the newsletter whose insights are being fetched.
     *     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterInsightsJob", exports = "mexFetchNewsletterInsights",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String newsletterId;

    /**
     * The list of metric identifiers to fetch values for.
     *     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterInsightsJob", exports = "mexFetchNewsletterInsights",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final List<String> metrics;

    /**
     * Creates a request fetching the supplied metrics for the given newsletter.
     *
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
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterInsightsJob", exports = "mexFetchNewsletterInsights",
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

            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }

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

            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
