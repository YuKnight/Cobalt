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
 * Fetches a preview of newsletter directory categories with featured channels.
 *
 * <p>This query powers the newsletter directory landing screen, returning a list of categories each accompanied by a handful of featured newsletters for visual preview.
 *
 * @implNote WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob: adapts the {@code mexFetchNewsletterDirectoryCategoriesPreview} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob")
public final class FetchNewsletterDirectoryCategoriesPreviewMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterDirectoryCategoriesPreview} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterDirectoryCategoriesPreviewJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexFetchNewsletterDirectoryCategoriesPreview} query.
     */
    public static final String QUERY_ID = "35266481849605779";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterDirectoryCategoriesPreview
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexFetchNewsletterDirectoryCategoriesPreview"}.
     */
    public static final String OPERATION_NAME = "mexFetchNewsletterDirectoryCategoriesPreview";
    private final String input;

    public FetchNewsletterDirectoryCategoriesPreviewMexRequest(String input) {
        this.input = input;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob: WA Web's
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
     * @implNote WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob.mexFetchNewsletterDirectoryCategoriesPreview: WA Web constructs the
     * {@code variables} object inline and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob", exports = "mexFetchNewsletterDirectoryCategoriesPreview",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob.mexFetchNewsletterDirectoryCategoriesPreview
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob.mexFetchNewsletterDirectoryCategoriesPreview
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob.mexFetchNewsletterDirectoryCategoriesPreview
            // Emits the input variable when present
            if (input != null) {
                writer.writeName("input");
                writer.writeColon();
                writer.writeString(input);
            }
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchNewsletterDirectoryCategoriesPreviewJob.mexFetchNewsletterDirectoryCategoriesPreview
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
