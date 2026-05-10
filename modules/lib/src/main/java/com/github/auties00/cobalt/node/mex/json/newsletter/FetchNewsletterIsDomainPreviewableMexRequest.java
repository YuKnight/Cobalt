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
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterIsDomainPreviewableJob")
public final class FetchNewsletterIsDomainPreviewableMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterIsDomainPreviewable} compiled query.
     */
    public static final String QUERY_ID = "9849510985088294";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexFetchNewsletterIsDomainPreviewable
     * operation.
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
    @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterIsDomainPreviewableJob", exports = "mexFetchNewsletterIsDomainPreviewable",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // which always populates the key (the array itself may be empty).
            writer.writeName("url_domains");
            writer.writeColon();
            writeStringArray(writer, urlDomains);
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

    /**
     * Writes a list of strings as a JSON array into the given writer.
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
