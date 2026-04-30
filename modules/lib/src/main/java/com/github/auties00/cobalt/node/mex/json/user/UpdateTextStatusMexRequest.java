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
import java.util.Optional;

/**
 * Publishes or clears the authenticated user's ephemeral text status entry.
 *
 * <p>The mutation takes the serialised status payload (text body, optional emoji and ephemeral duration) and commits
 * it as the current status for the account. Setting an empty input clears any existing status.
 */
@WhatsAppWebModule(moduleName = "WAWebMexUpdateTextStatusJob")
public final class UpdateTextStatusMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled GraphQL mutation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUpdateTextStatusJobMutation.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "9152604461510864";

    /**
     * The GraphQL operation name reported to {@code MexPerfTracker} when this mutation is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUpdateTextStatusJobMutation.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "mexUpdateTextStatus";

    /**
     * The body of the text status, or {@code null} / empty to clear it.
     */
    private final String text;

    /**
     * The optional emoji decoration shown next to the text status.
     */
    private final String emoji;

    /**
     * The ephemeral duration in seconds after which the status expires.
     */
    private final long ephemeralDurationSec;

    /**
     * Constructs a new request with the three status components. Applies the same normalisation as
     * {@code WAWebTextStatusParseUtils.createTextStatusObjectForUpdateRequest}. An empty {@code text} string is coerced
     * to {@code null}. A {@code null} {@code emoji} is omitted from the variables payload entirely. When both
     * {@code text} and {@code emoji} are absent the ephemeral duration is silently reset to {@code 0}.
     *
     * @param text the text body of the status, or {@code null} or empty to clear it
     * @param emoji the optional emoji decoration of the status, or {@code null} to omit
     * @param ephemeralDurationSec the ephemeral duration in seconds, or {@code 0} for no expiry
     */
    public UpdateTextStatusMexRequest(String text, String emoji, long ephemeralDurationSec) {
        this.text = text;
        this.emoji = emoji;
        this.ephemeralDurationSec = ephemeralDurationSec;
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
     * Serialises the GraphQL variables as JSON and wraps them in a {@code w:mex} IQ stanza. The {@code input} variable
     * carries the structured status payload of shape {@code {text, emoji?: {content}, ephemeral_duration_sec}}.
     *
     * @return the IQ {@link NodeBuilder} ready to be built and dispatched
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUpdateTextStatusJob", exports = "mexUpdateTextStatus",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // Coerce empty text to null per WAWebTextStatusParseUtils.createTextStatusObjectForUpdateRequest.
        var normalisedText = (text == null || text.isEmpty()) ? null : text;
        // Reset duration to zero when both text and emoji are absent.
        var normalisedDuration = ephemeralDurationSec;
        if (normalisedText == null && emoji == null && normalisedDuration != 0L) {
            normalisedDuration = 0L;
        }
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();
            writer.writeName("text");
            writer.writeColon();
            if (normalisedText == null) {
                writer.writeNull();
            } else {
                writer.writeString(normalisedText);
            }
            // The emoji object is only emitted when an emoji decoration is supplied.
            if (emoji != null) {
                writer.writeName("emoji");
                writer.writeColon();
                writer.startObject();
                writer.writeName("content");
                writer.writeColon();
                writer.writeString(emoji);
                writer.endObject();
            }
            writer.writeName("ephemeral_duration_sec");
            writer.writeColon();
            writer.writeInt64(normalisedDuration);
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
