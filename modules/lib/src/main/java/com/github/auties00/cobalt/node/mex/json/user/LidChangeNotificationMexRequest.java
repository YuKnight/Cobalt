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
 * Retrieves the most recent linked-identity (LID) change for the authenticated account, returning the previous and
 * current identifier pair.
 *
 * <p>LID is WhatsApp's non-phone account identifier used during the LID migration rollout. When the server rotates an
 * account's LID, clients reconcile local storage by issuing this query to learn the old-to-new mapping so that chat
 * references can be updated without losing history.
 */
@WhatsAppWebModule(moduleName = "WAWebMexLidChangeNotification")
public final class LidChangeNotificationMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled GraphQL operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexLidChangeNotificationQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "9892367127524985";

    /**
     * The GraphQL operation name reported to {@code MexPerfTracker} when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexLidChangeNotificationQuery.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "parseLidChangeNotification";

    /**
     * Constructs a new request. The query takes no variables.
     */
    public LidChangeNotificationMexRequest() {
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
     * Serialises an empty GraphQL variables envelope and wraps it in a {@code w:mex} IQ stanza. The compiled Relay
     * artifact declares an empty {@code argumentDefinitions} array.
     *
     * @return the IQ {@link NodeBuilder} ready to be built and dispatched
     */
    @WhatsAppWebExport(moduleName = "WAWebMexLidChangeNotification", exports = "parseLidChangeNotification",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
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
