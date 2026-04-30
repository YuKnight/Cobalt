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
 * Sets or rotates the recovery PIN associated with the authenticated user's WhatsApp username.
 *
 * <p>The username PIN is the secret that allows account recovery when the primary phone number is unavailable. This
 * mutation registers a new PIN or updates the existing one after the user completes the username PIN flow in
 * settings.
 */
@WhatsAppWebModule(moduleName = "WAWebMexSetUsernameKeyJob")
public final class SetUsernameKeyMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled GraphQL mutation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexSetUsernameKeyJobMutation.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "9749436995157074";

    /**
     * The GraphQL operation name reported to {@code MexPerfTracker} when this mutation is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexSetUsernameKeyJobMutation.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "mexSetUsernameKeyQueryJob";

    /**
     * The new recovery PIN to register against the username.
     */
    private final String pin;

    /**
     * Constructs a new request carrying the given PIN.
     *
     * @param pin the new recovery PIN, or {@code null} to omit the variable
     */
    public SetUsernameKeyMexRequest(String pin) {
        this.pin = pin;
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
    @WhatsAppWebExport(moduleName = "WAWebMexSetUsernameKeyJob", exports = "mexSetUsernameKeyQueryJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            if (pin != null) {
                writer.writeName("pin");
                writer.writeColon();
                writer.writeString(pin);
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
