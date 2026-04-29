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
 *
 * @implNote WAWebMexUpdateNewsletterJob: adapts the {@code mexUpdateNewsletter} GraphQL mutation,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexUpdateNewsletterJob")
public final class UpdateNewsletterMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code UpdateNewsletter} compiled mutation.
     *
     * @implNote WAWebMexUpdateNewsletterJobMutation.graphql: corresponds to the compiled
     * document id registered for the {@code mexUpdateNewsletter} mutation.
     */
    public static final String QUERY_ID = "24250201037901610";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexUpdateNewsletter
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexUpdateNewsletterJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexUpdateNewsletter"}.
     */
    public static final String OPERATION_NAME = "mexUpdateNewsletter";
    private final String newsletterId;
    private final JSONObject updates;

    public UpdateNewsletterMexRequest(String newsletterId, JSONObject updates) {
        this.newsletterId = newsletterId;
        this.updates = updates;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexUpdateNewsletterJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexUpdateNewsletterJob: WA Web's
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
     * @implNote WAWebMexUpdateNewsletterJob.mexUpdateNewsletter: WA Web constructs the
     * {@code variables} object inline as
     * {@code {newsletter_id, updates: {name, description, picture, settings}}}
     * and delegates to {@code WAWebMexClient.fetchQuery}. Cobalt writes
     * the same nested object via {@code fastjson2.JSONWriter} and wraps
     * it through {@link Json#createMexNode(String, String)}.
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
        // WAWebMexUpdateNewsletterJob.mexUpdateNewsletter
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexUpdateNewsletterJob.mexUpdateNewsletter
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // WAWebMexUpdateNewsletterJob.mexUpdateNewsletter
            // Emits the newsletter_id variable when present
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }

            // WAWebMexUpdateNewsletterJob.mexUpdateNewsletter
            // Emits the updates variable as a nested JSON object (NOT a quoted JSON string),
            // mirroring the inline {name, description, picture, settings} object built in JS
            if (updates != null) {
                writer.writeName("updates");
                writer.writeColon();
                writer.writeAny(updates);
            }
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexUpdateNewsletterJob.mexUpdateNewsletter
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
