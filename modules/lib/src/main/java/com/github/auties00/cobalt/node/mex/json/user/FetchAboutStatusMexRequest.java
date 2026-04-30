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
 * Fetches the "about" status line of one or more WhatsApp users together with the associated update history entries.
 *
 * <p>The about status is the short biographical line (for example "At the movies", "Busy") that a user sets in their
 * profile. It is distinct from the ephemeral text status shown on the status tab. The query returns the current text
 * and, when available, the update log used to derive the "last updated" timestamp shown in the profile view.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchAboutStatusJob")
public final class FetchAboutStatusMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric query identifier assigned to the compiled GraphQL operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAboutStatusJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String QUERY_ID = "24535500086059408";

    /**
     * The GraphQL operation name reported to {@code MexPerfTracker} when this query is dispatched.
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAboutStatusJobQuery.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "mexGetAbout";

    /**
     * The target user JID whose about status is being requested.
     */
    private final String user;

    /**
     * Constructs a new request for the given target user JID.
     *
     * @param user the target user JID, or {@code null} to omit the variable
     */
    public FetchAboutStatusMexRequest(String user) {
        this.user = user;
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
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAboutStatusJob", exports = "mexGetAbout",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            if (user != null) {
                writer.writeName("user");
                writer.writeColon();
                writer.writeString(user);
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
