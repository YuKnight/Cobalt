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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fetches user directory metadata for a batch of contacts through the MEX transport variant of the usync protocol.
 *
 * <p>Usync is WhatsApp's bulk user-info query that powers contact sync, participant resolution and device list
 * refresh. The GraphQL variant accepted here lets callers toggle which optional fields are included in each entry,
 * including about status, phone country code and registered username. The response is a list of records, one per
 * queried user, with the selected metadata fields populated.
 */
@WhatsAppWebModule(moduleName = "WAWebMexUsync")
public final class UsyncMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled GraphQL operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsyncQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "29829202653362039";

    /**
     * The GraphQL operation name reported to {@code MexPerfTracker} when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsyncQuery.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "mexUsyncQuery";

    /**
     * Whether each result entry should carry the about-status field.
     */
    private final Boolean includeAboutStatus;

    /**
     * Whether each result entry should carry the phone country-code field.
     */
    private final Boolean includeCountryCode;

    /**
     * Whether each result entry should carry the registered-username field.
     */
    private final Boolean includeUsername;

    /**
     * The serialised batch of target user JIDs encoded as a single GraphQL input scalar.
     */
    private final String input;

    /**
     * Constructs a new request with the given selection toggles and serialised input.
     *
     * @param includeAboutStatus whether to include the about-status field, or {@code null} to omit the variable
     * @param includeCountryCode whether to include the phone country-code field, or {@code null} to omit the variable
     * @param includeUsername whether to include the registered-username field, or {@code null} to omit the variable
     * @param input the serialised list of target user JIDs, or {@code null} to omit the variable
     */
    public UsyncMexRequest(Boolean includeAboutStatus, Boolean includeCountryCode, Boolean includeUsername, String input) {
        this.includeAboutStatus = includeAboutStatus;
        this.includeCountryCode = includeCountryCode;
        this.includeUsername = includeUsername;
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
    @WhatsAppWebExport(moduleName = "WAWebMexUsync", exports = "mexUsyncQuery",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            if (includeAboutStatus != null) {
                writer.writeName("include_about_status");
                writer.writeColon();
                writer.writeBool(includeAboutStatus);
            }
            if (includeCountryCode != null) {
                writer.writeName("include_country_code");
                writer.writeColon();
                writer.writeBool(includeCountryCode);
            }
            if (includeUsername != null) {
                writer.writeName("include_username");
                writer.writeColon();
                writer.writeBool(includeUsername);
            }
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
