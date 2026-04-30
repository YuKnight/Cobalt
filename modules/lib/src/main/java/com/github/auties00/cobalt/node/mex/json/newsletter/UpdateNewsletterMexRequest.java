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
 * Updates the mutable metadata of a newsletter such as name, description or picture.
 *
 * <p>Only the owner may edit newsletter metadata. The mutation returns the full updated thread metadata so the client can refresh its local cache.
 */
@WhatsAppWebModule(moduleName = "WAWebMexUpdateNewsletterJob")
public final class UpdateNewsletterMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code UpdateNewsletter} compiled mutation.
     */
    public static final String QUERY_ID = "24250201037901610";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexUpdateNewsletter
     * operation.
     */
    public static final String OPERATION_NAME = "mexUpdateNewsletter";
    private final String newsletterId;
    private final JSONObject updates;

    /**
     * Creates a request with the given variables.
     *
     * @param newsletterId the newsletter id
     * @param updates the updates
     */
    public UpdateNewsletterMexRequest(String newsletterId, JSONObject updates) {
        this.newsletterId = newsletterId;
        this.updates = updates;
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
     * The {@code updates} variable is emitted as a structured JSON
     * object, not a string-encoded payload, matching the GraphQL input
     * type expected by the relay.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUpdateNewsletterJob", exports = "mexUpdateNewsletter",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }

            // mirroring the inline {name, description, picture, settings} object built in JS
            if (updates != null) {
                writer.writeName("updates");
                writer.writeColon();
                writer.writeAny(updates);
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
