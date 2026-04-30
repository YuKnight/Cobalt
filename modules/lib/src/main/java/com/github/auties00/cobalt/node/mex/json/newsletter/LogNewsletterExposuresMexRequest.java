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
 */
@WhatsAppWebModule(moduleName = "WAWebMexLogNewsletterExposuresJob")
public final class LogNewsletterExposuresMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code LogNewsletterExposures} compiled mutation.
     */
    public static final String QUERY_ID = "25260800823586918";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexLogNewsletterExposures
     * operation.
     */
    public static final String OPERATION_NAME = "mexLogNewsletterExposures";
    private final List<NewsletterExposure> exposures;

    /**
     * Constructs a {@link LogNewsletterExposuresMexRequest} from a batch of exposure entries.
     *
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
    @WhatsAppWebExport(moduleName = "WAWebMexLogNewsletterExposuresJob", exports = "mexLogNewsletterExposures",
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

            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
