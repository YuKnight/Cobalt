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
 * Updates the authenticated user's personal settings for a newsletter.
 *
 * <p>User-scoped newsletter settings include notification mutes and other per-viewer preferences. This mutation applies the supplied setting change and returns the updated setting object.
 */
@WhatsAppWebModule(moduleName = "WAWebMexUpdateNewsletterUserSetting")
public final class UpdateNewsletterUserSettingMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code UpdateNewsletterUserSetting} compiled mutation.
     */
    public static final String QUERY_ID = "31938993655691868";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexUpdateNewsletterUserSetting
     * operation.
     */
    public static final String OPERATION_NAME = "mexUpdateNewsletterUserSetting";
    private final String newsletterId;
    private final String type;
    private final String value;

    /**
     * Creates a request with the given variables.
     *
     * @param newsletterId the newsletter id
     * @param type the type
     * @param value the value
     */
    public UpdateNewsletterUserSettingMexRequest(String newsletterId, String type, String value) {
        this.newsletterId = newsletterId;
        this.type = type;
        this.value = value;
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
    @WhatsAppWebExport(moduleName = "WAWebMexUpdateNewsletterUserSetting", exports = "mexUpdateNewsletterUserSetting",
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
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }
            if (type != null) {
                writer.writeName("type");
                writer.writeColon();
                writer.writeString(type);
            }
            if (value != null) {
                writer.writeName("value");
                writer.writeColon();
                writer.writeString(value);
            }
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
