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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Checks whether a candidate username is available for registration and returns a list of alternative suggestions when
 * it is not.
 *
 * <p>This is the query that powers the live-validation indicator in the username picker. The response carries a status
 * token (available, taken, invalid, reserved) together with a list of suggested alternatives the client may present to
 * the user.
 */
@WhatsAppWebModule(moduleName = "WAWebMexUsernameAvailability")
public final class UsernameAvailabilityMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled GraphQL operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsernameAvailabilityQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "9615795045169045";

    /**
     * The GraphQL operation name reported to {@code MexPerfTracker} when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsernameAvailabilityQuery.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "mexCheckUsernameAvailabilityQueryJob";

    /**
     * The candidate username to check for availability.
     */
    private final String input;

    /**
     * Constructs a new request carrying the given candidate username.
     *
     * @param input the candidate username, or {@code null} to omit the variable
     */
    public UsernameAvailabilityMexRequest(String input) {
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
    @WhatsAppWebExport(moduleName = "WAWebMexUsernameAvailability", exports = "mexCheckUsernameAvailabilityQueryJob",
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
