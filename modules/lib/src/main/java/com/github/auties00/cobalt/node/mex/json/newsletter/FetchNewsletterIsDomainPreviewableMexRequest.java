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
import java.util.Optional;

/**
 * Checks whether a given list of domains is previewable inside newsletter messages.
 *
 * <p>The WhatsApp backend maintains a list of allowed domains whose link previews may be rendered inside newsletter messages. This query validates one or more URL domains before publishing.
 *
 * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob: adapts the {@code mexFetchNewsletterIsDomainPreviewable} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterIsDomainPreviewableJob")
public final class FetchNewsletterIsDomainPreviewableMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterIsDomainPreviewable} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexFetchNewsletterIsDomainPreviewable} query
     * (see {@code params.id} in the generated relay descriptor).
     */
    public static final String QUERY_ID = "9849510985088294";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterIsDomainPreviewable
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexFetchNewsletterIsDomainPreviewable"}.
     */
    public static final String OPERATION_NAME = "mexFetchNewsletterIsDomainPreviewable";
    private final List<String> urlDomains;

    /**
     * Creates a new request variant carrying the given list of URL domains.
     *
     * @param urlDomains the URL domains to validate; may be {@code null} or empty
     */
    public FetchNewsletterIsDomainPreviewableMexRequest(List<String> urlDomains) {
        this.urlDomains = urlDomains;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob: WA Web's
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
     * @implNote WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable: WA Web constructs the
     * {@code variables} object inline as {@code {url_domains: e}} where
     * {@code e} is a string array, and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterIsDomainPreviewableJob", exports = "mexFetchNewsletterIsDomainPreviewable",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
            // Emits the url_domains variable as a JSON array; matches the JS object literal {url_domains: e}
            // which always populates the key (the array itself may be empty).
            writer.writeName("url_domains");
            writer.writeColon();
            writeStringArray(writer, urlDomains);
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexFetchNewsletterIsDomainPreviewableJob.mexFetchNewsletterIsDomainPreviewable
            // Flushes the JSON buffer into a StringWriter and wraps it in the shared MEX IQ envelope
            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    /**
     * Writes a list of strings as a JSON array into the given writer.
     *
     * @implNote The array is always emitted (possibly empty) so the on-wire
     * shape always contains the {@code url_domains} key, mirroring the JS
     * object literal {@code {url_domains: e}} which never omits the key.
     * @param writer the JSON writer to emit into
     * @param values the string values to serialise, may be {@code null}
     */
    private static void writeStringArray(JSONWriter writer, List<String> values) {
        writer.startArray();
        if (values != null) {
            for (var i = 0; i < values.size(); i++) {
                if (i > 0) {
                    writer.writeComma();
                }
                writer.writeString(values.get(i));
            }
        }
        writer.endArray();
    }
}
