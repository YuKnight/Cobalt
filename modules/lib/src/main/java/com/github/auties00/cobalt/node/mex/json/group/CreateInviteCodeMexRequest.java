package com.github.auties00.cobalt.node.mex.json.group;

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
 * Creates a fresh invite code for a group, community or other receiver
 * thread, returning the freshly minted opaque code string.
 *
 * <p>The mutation is the rotation counterpart to
 * {@code FetchGroupInviteCodeMexRequest}: where the latter reads the current code
 * without altering it, this mutation asks the server to issue a new one,
 * implicitly invalidating any previously distributed link. The
 * {@code entry_point} variable carries the UI surface that triggered the
 * rotation so the relay can attribute usage telemetry to the originating
 * flow.
 */
@WhatsAppWebModule(moduleName = "WAWebMexCreateInviteCodeJob")
public final class CreateInviteCodeMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the compiled {@code mexCreateInviteCode} mutation.
     */
    public static final String QUERY_ID = "26155584267463745";

    /**
     * The GraphQL operation name reported by WA Web's {@code MexPerfTracker}
     * when dispatching this query, mirroring the {@code params.name} value of
     * the compiled {@code mexCreateInviteCode} operation.
     */
    public static final String OPERATION_NAME = "mexCreateInviteCode";
    private final String receiver;
    private final String entryPoint;

    /**
     * Constructs a request that asks the relay to mint a fresh invite code
     * for the given {@code receiver}.
     * @param receiver   the opaque receiver identifier the code is being
     *                   minted for, never {@code null}
     * @param entryPoint the UI-surface telemetry tag (e.g.
     *                   {@code "CHAT_INFO_INVITE_BUTTON"}) identifying what
     *                   triggered the rotation, never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public CreateInviteCodeMexRequest(String receiver, String entryPoint) {
        this.receiver = Objects.requireNonNull(receiver, "receiver cannot be null");
        this.entryPoint = Objects.requireNonNull(entryPoint, "entryPoint cannot be null");
    }

    /**
     * Returns the compiled GraphQL query identifier.
     *
     * @return the constant {@link #QUERY_ID}; never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name.
     *
     * @return the constant {@link #OPERATION_NAME}; never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the WhatsApp relay.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexCreateInviteCodeJob", exports = "mexCreateInviteCode",
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
            writer.writeName("receiver");
            writer.writeColon();
            writer.writeString(receiver);
            writer.writeName("entry_point");
            writer.writeColon();
            writer.writeString(entryPoint);
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
