package com.github.auties00.cobalt.node.mex.json;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.mex.MexOperation;

/**
 * Base interface for MEX operations whose GraphQL variables and responses
 * are encoded as JSON strings.
 *
 * <p>JSON MEX requests serialise their GraphQL variables under the standard
 * {@code {"variables": {...}}} envelope and attach that string as the text
 * content of a {@code <query>} element. The request is then wrapped in an
 * IQ stanza with the {@code w:mex} namespace. Responses arrive as an IQ
 * {@code <result>} child whose text content carries the JSON reply produced
 * by the WhatsApp relay server.
 *
 * <p>This is the default encoding used by the vast majority of WA Web MEX
 * jobs, including newsletter management, username administration, group
 * metadata queries and community subgroup operations.
 *
 * @implNote WAWebMexClient, WAWebMexNativeClient: every WA Web job ending in
 * {@code Job} or {@code JobMutation} that depends on {@code WAWebMexClient}
 * ultimately calls {@code fetchQuery(queryDef, variables)} which serialises
 * the variables as JSON before dispatching through
 * {@code WAWebMexNativeClient}. Cobalt collapses the same flow into a shared
 * {@link #createMexNode(String, String)} helper invoked from every concrete
 * {@code Request.toNode()} implementation.
 */
@WhatsAppWebModule(moduleName = "WAWebMexClient")
@WhatsAppWebModule(moduleName = "WAWebMexNativeClient")
public non-sealed interface MexJsonOperation extends MexOperation {
    /**
     * Builds the MEX IQ stanza that wraps a JSON-encoded GraphQL query.
     *
     * <p>The returned {@link NodeBuilder} is not yet built so callers can
     * attach additional attributes before the stanza is dispatched.
     *
     * @implNote WAWebMexClient.fetchQuery: in WA Web the JSON payload is
     * attached as the text body of the {@code <query>} stanza with
     * {@code query_id} identifying the compiled GraphQL operation; the IQ is
     * routed to the user server with {@code type="get"} and namespace
     * {@code w:mex}. Cobalt produces the same stanza shape directly, without
     * the intermediate relay client abstraction.
     * @param queryId the numeric query identifier assigned to the compiled
     *                GraphQL operation by the WA relay
     * @param jsonPayload the JSON string containing the serialised
     *                    {@code {"variables": ...}} envelope
     * @return a {@link NodeBuilder} prepared for the IQ stanza; callers may
     *         still mutate attributes before building
     */
    @WhatsAppWebExport(moduleName = "WAWebMexClient", exports = "fetchQuery",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static NodeBuilder createMexNode(String queryId, String jsonPayload) {
        // WAWebMexClient.fetchQuery
        // Builds the inner <query> element carrying the query_id attribute and the JSON variables envelope
        var queryNode = new NodeBuilder()
                .description("query")
                .attribute("query_id", queryId)
                .content(jsonPayload)
                .build();

        // WAWebMexClient.fetchQuery
        // Wraps the query in an IQ stanza routed to the user server under the w:mex namespace
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:mex")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(queryNode);
    }
}
