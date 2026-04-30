package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.mex.MexOperation;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

/**
 * Creates a new newsletter owned by the authenticated user.
 *
 * <p>This mutation registers a new newsletter channel with the provided name,
 * description and picture metadata. On success the server returns the
 * fully-hydrated newsletter metadata including the generated id, initial
 * state, thread metadata and viewer role.
 */
@WhatsAppWebModule(moduleName = "WAWebMexCreateNewsletterJob")
public final class CreateNewsletterMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the {@code CreateNewsletter} compiled mutation.
     */
    public static final String QUERY_ID = "25149874324715067";

    /**
     * The GraphQL operation name reported by WA Web's {@code MexPerfTracker}
     * when dispatching this mutation.
     */
    public static final String OPERATION_NAME = "mexCreateNewsletter";

    /**
     * The newsletter display name.
     */
    private final String name;

    /**
     * The newsletter description text.
     */
    private final String description;

    /**
     * The base64 or direct-path encoded picture payload.
     */
    private final String picture;

    /**
     * Constructs a new request with the given mutation variables.
     *
     * @param name        the newsletter display name, may be {@code null}
     * @param description the newsletter description, may be {@code null}
     * @param picture     the base64 or direct-path encoded picture payload,
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
     * Builds the IQ stanza that dispatches this mutation to the WhatsApp
     * relay.
     *
     * <p>The variables are wrapped under an {@code input} sub-object as
     * {@code {input: {name, description, picture}}}.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexCreateNewsletterJob", exports = "mexCreateNewsletter",
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

            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
