package com.github.auties00.cobalt.node.mex.json.user;

import com.alibaba.fastjson2.JSON;
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
import java.util.Objects;
import java.util.Optional;

/**
 * Claims or updates the authenticated user's WhatsApp username, optionally reserving it during a sign-up session.
 *
 * <p>The mutation accepts the requested username together with a {@code reserved} flag (used to pre-claim a username
 * during onboarding without finalising it), a {@code session_id} tying the reservation to a specific registration flow
 * and a {@code source} tag identifying the entry point. The response echoes a status token that drives the post-submit
 * UI state.
 */
@WhatsAppWebModule(moduleName = "WAWebMexSetUsernameJob")
public final class SetUsernameMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled GraphQL mutation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexSetUsernameJobMutation.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "25757341163897635";

    /**
     * The GraphQL operation name reported to {@code MexPerfTracker} when this mutation is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexSetUsernameJobMutation.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "mexSetUsernameQueryJob";

    /**
     * The candidate username to claim or update.
     */
    private final String input;

    /**
     * Whether the username should be reserved (pre-claimed during onboarding without finalising).
     */
    private final Boolean reserved;

    /**
     * The session identifier tying the reservation to a registration flow.
     */
    private final String sessionId;

    /**
     * A free-form tag identifying the entry point that initiated the mutation.
     */
    private final String source;

    /**
     * Constructs a new request carrying the given fields.
     *
     * @param input the candidate username to claim or update
     * @param reserved whether the username should be reserved, or {@code null} to omit the flag
     * @param sessionId the registration flow session identifier, or {@code null} to omit it
     * @param source the entry-point tag, or {@code null} to omit it
     */
    public SetUsernameMexRequest(String input, Boolean reserved, String sessionId, String source) {
        this.input = input;
        this.reserved = reserved;
        this.sessionId = sessionId;
        this.source = source;
    }

    /**
     * Returns the compiled GraphQL query identifier.
     *
     * @return the constant {@link #QUERY_ID}, never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name.
     *
     * @return the constant {@link #OPERATION_NAME}, never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Serialises the GraphQL variables as JSON and wraps them in a {@code w:mex} IQ stanza.
     *
     * <p>Mirrors the WA Web {@code isStringNullOrEmpty(t.input) ? {} : t} gate. When {@code input} is {@code null} or
     * empty the variables object is emitted as {@code {}} regardless of the other fields. When {@code input} is
     * present, all four variables are forwarded verbatim so the relay receives the full payload.
     *
     * @return the IQ {@link NodeBuilder} ready to be built and dispatched
     */
    @WhatsAppWebExport(moduleName = "WAWebMexSetUsernameJob", exports = "mexSetUsernameQueryJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // Mirrors isStringNullOrEmpty(t.input) ? {} : t in the WA Web job.
            if (input != null && !input.isEmpty()) {
                writer.writeName("input");
                writer.writeColon();
                writer.writeString(input);
                if (reserved != null) {
                    writer.writeName("reserved");
                    writer.writeColon();
                    writer.writeBool(reserved);
                }
                if (sessionId != null) {
                    writer.writeName("session_id");
                    writer.writeColon();
                    writer.writeString(sessionId);
                }
                if (source != null) {
                    writer.writeName("source");
                    writer.writeColon();
                    writer.writeString(source);
                }
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
