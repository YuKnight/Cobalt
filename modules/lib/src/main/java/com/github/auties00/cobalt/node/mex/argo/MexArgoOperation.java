package com.github.auties00.cobalt.node.mex.argo;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.mex.MexOperation;

/**
 * Base interface for MEX operations whose GraphQL variables and responses
 * are encoded with the Argo binary format rather than JSON.
 *
 * <p>Argo is a compact binary wire format used by a small number of
 * performance-sensitive MEX endpoints. The transport envelope is identical
 * to the JSON variant: an IQ stanza with the {@code w:mex} namespace wrapping
 * a {@code <query>} node tagged with {@code query_id}. Only the body of the
 * query and the server reply differ, carrying raw Argo-encoded bytes.
 *
 * @implNote WAWebMexClient, WAWebMexNativeClient: this interface mirrors the
 * Argo path through {@code WAWebMexClient.fetchQuery} / {@code
 * WAWebMexNativeClient.fetchQuery}. In WA Web the transport choice is driven
 * by the GraphQL query metadata; in Cobalt it is expressed as a separate
 * sealed sub-hierarchy.
 */
@WhatsAppWebModule(moduleName = "WAWebMexClient")
@WhatsAppWebModule(moduleName = "WAWebMexNativeClient")
public non-sealed interface MexArgoOperation extends MexOperation {
    /**
     * Builds the MEX IQ stanza that wraps an Argo-encoded GraphQL query.
     *
     * <p>The returned {@link NodeBuilder} is not yet built so callers can
     * attach additional attributes before the stanza is dispatched.
     *
     * @implNote WAWebMexClient.fetchQuery: in WA Web the Argo payload is
     * attached as the binary body of the {@code <query>} stanza with
     * {@code query_id} identifying the compiled GraphQL operation. The IQ
     * is routed to the user server with {@code type="get"} and namespace
     * {@code w:mex}, matching Cobalt's output exactly.
     * @param queryId the numeric query identifier assigned to the compiled
     *                GraphQL operation by the WA relay
     * @param argoPayload the Argo-encoded GraphQL variables
     * @return a {@link NodeBuilder} prepared for the IQ stanza; callers may
     *         still mutate attributes before building
     */
    @WhatsAppWebExport(moduleName = "WAWebMexClient", exports = "fetchQuery",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static NodeBuilder createMexNode(String queryId, byte[] argoPayload) {
        // WAWebMexClient.fetchQuery
        // Builds the inner <query> element carrying the query_id attribute and the Argo payload bytes
        var queryNode = new NodeBuilder()
                .description("query")
                .attribute("query_id", queryId)
                .content(argoPayload)
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
