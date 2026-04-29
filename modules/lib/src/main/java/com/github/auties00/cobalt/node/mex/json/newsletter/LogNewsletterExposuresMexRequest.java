package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.newsletter.NewsletterExposure;
import com.github.auties00.cobalt.node.mex.MexOperation;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

/**
 * Logs a batch of newsletter exposure events for attribution and ranking.
 *
 * <p>When a user browses newsletters the client records lightweight exposure events which are later flushed to the server via this mutation. The backend uses the exposure signal to improve directory ranking.
 *
 * @implNote WAWebMexLogNewsletterExposuresJob: adapts the {@code mexLogNewsletterExposures} GraphQL mutation,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexLogNewsletterExposuresJob")
public final class LogNewsletterExposuresMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code LogNewsletterExposures} compiled mutation.
     *
     * @implNote WAWebMexLogNewsletterExposuresJobMutation.graphql: corresponds to the compiled
     * document id registered for the {@code mexLogNewsletterExposures} mutation,
     * extracted from the {@code params.id} field of the compiled GraphQL artifact.
     */
    public static final String QUERY_ID = "25260800823586918";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexLogNewsletterExposures
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexLogNewsletterExposuresJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexLogNewsletterExposures"}.
     */
    public static final String OPERATION_NAME = "mexLogNewsletterExposures";
    private final List<NewsletterExposure> exposures;

    /**
     * Constructs a {@link LogNewsletterExposuresMexRequest} from a batch of exposure entries.
     *
     * @implNote WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures: WA Web's exported
     * function takes the {@code e} array directly and maps each entry
     * into a {@code {newsletter_id, capability}} object. Cobalt accepts
     * the pre-mapped {@link NewsletterExposure} records so the call site can
     * resolve the capability enum once before queuing the request.
     * @param exposures the batch of exposure entries; must not be
     *                  {@code null}, but may be empty
     * @throws NullPointerException if {@code exposures} is {@code null}
     */
    public LogNewsletterExposuresMexRequest(List<NewsletterExposure> exposures) {
        Objects.requireNonNull(exposures, "exposures cannot be null");
        this.exposures = List.copyOf(exposures);
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexLogNewsletterExposuresJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexLogNewsletterExposuresJob: WA Web's
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
     * @implNote WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures: WA Web constructs the
     * {@code variables} object inline as
     * {@code {input:{exposures: e.map(({capability,newsletterJid}) =>
     * ({newsletter_id:newsletterJid, capability:
     * getNewsletterCapabilityFromEnum(capability)}))}}} and delegates to
     * {@code WAWebMexClient.fetchQuery}. Cobalt writes the same nested
     * JSON envelope directly via {@code fastjson2.JSONWriter} and wraps
     * the result through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexLogNewsletterExposuresJob", exports = "mexLogNewsletterExposures",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
            // var t={input:{exposures: e.map(...)}}
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();

            // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
            // exposures: e.map(function(e){var t=e.capability,n=e.newsletterJid;
            //                              return {newsletter_id:n, capability:getNewsletterCapabilityFromEnum(t)}})
            writer.writeName("exposures");
            writer.writeColon();
            writer.startArray();
            for (var i = 0; i < exposures.size(); i++) {
                if (i > 0) {
                    writer.writeComma();
                }
                var exposure = exposures.get(i);
                writer.startObject();
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(exposure.newsletterId().orElse(null));
                writer.writeName("capability");
                writer.writeColon();
                writer.writeString(exposure.capability().orElse(null));
                writer.endObject();
            }
            writer.endArray();

            writer.endObject();
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
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
