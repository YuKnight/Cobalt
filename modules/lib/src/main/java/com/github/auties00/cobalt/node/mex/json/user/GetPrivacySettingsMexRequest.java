package com.github.auties00.cobalt.node.mex.json.user;

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
 * Fetches the user's current privacy preferences such as last-seen visibility, profile picture visibility, about
 * visibility and read receipts.
 *
 * <p>The response carries the full privacy settings record for the authenticated account. These values drive the
 * Settings privacy screen and are used by outgoing send paths to decide whether receipts and presence should be
 * emitted.
 */
public final class GetPrivacySettingsMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled GraphQL operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetPrivacySettingsQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "25637004609323493";

    /**
     * The GraphQL operation name reported to {@code MexPerfTracker} when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetPrivacySettingsQuery.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "fetchPrivacySettings";

    /**
     * The serialised input variable carried by the GraphQL request.
     */
    private final String input;

    /**
     * Constructs a new request with the given input variable.
     *
     * @param input the serialised input variable, or {@code null} to omit it
     */
    public GetPrivacySettingsMexRequest(String input) {
        this.input = input;
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
     * @return the IQ {@link NodeBuilder} ready to be built and dispatched
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetPrivacySettingsQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            if (input != null) {
                writer.writeName("input");
                writer.writeColon();
                writer.writeString(input);
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
