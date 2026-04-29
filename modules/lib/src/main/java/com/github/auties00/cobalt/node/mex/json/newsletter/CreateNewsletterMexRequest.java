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
 * Creates a new newsletter owned by the authenticated user.
 *
 * <p>This mutation registers a new newsletter channel with the provided name, description and picture metadata. On success the server returns the fully-hydrated newsletter metadata including the generated id, initial state, thread metadata and viewer role.
 *
 * @implNote WAWebMexCreateNewsletterJob: adapts the {@code mexCreateNewsletter} GraphQL mutation,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexCreateNewsletterJob")
public final class CreateNewsletterMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code CreateNewsletter} compiled mutation.
     *
     * @implNote WAWebMexCreateNewsletterJobMutation.graphql: corresponds to the compiled
     * document id registered for the {@code mexCreateNewsletter} mutation.
     */
    public static final String QUERY_ID = "25149874324715067";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexCreateNewsletter
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexCreateNewsletterJob: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexCreateNewsletter"}.
     */
    public static final String OPERATION_NAME = "mexCreateNewsletter";
    private final String name;
    private final String description;
    private final String picture;

    /**
     * Constructs a new request with the given mutation variables.
     *
     * @implNote WAWebMexCreateNewsletterJob.mexCreateNewsletter: mirrors the
     * {@code (t, r, a)} positional arguments of the JS function which are
     * placed under {@code input.name}, {@code input.description} and
     * {@code input.picture}.
     * @param name        the newsletter display name; may be {@code null}
     * @param description the newsletter description; may be {@code null}
     * @param picture     the base64 or direct-path encoded picture payload;
     *                    may be {@code null}
     */
    public CreateNewsletterMexRequest(String name, String description, String picture) {
        this.name = name;
        this.description = description;
        this.picture = picture;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexCreateNewsletterJob: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexCreateNewsletterJob: WA Web's
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
     * @implNote WAWebMexCreateNewsletterJob.mexCreateNewsletter: WA Web constructs the
     * {@code {input: {name: t, description: r, picture: a}}} variables
     * object inline and delegates to {@code WAWebMexClient.fetchQuery}.
     * Cobalt writes the JSON directly via {@code fastjson2.JSONWriter} and
     * wraps it through {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexCreateNewsletterJob", exports = "mexCreateNewsletter",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexCreateNewsletterJob.mexCreateNewsletter
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexCreateNewsletterJob.mexCreateNewsletter
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // WAWebMexCreateNewsletterJob.mexCreateNewsletter: {input: {name: t, description: r, picture: a}}
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();
            writer.writeName("name");
            writer.writeColon();
            writer.writeString(name);
            writer.writeName("description");
            writer.writeColon();
            writer.writeString(description);
            writer.writeName("picture");
            writer.writeColon();
            writer.writeString(picture);
            writer.endObject();
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexCreateNewsletterJob.mexCreateNewsletter
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
