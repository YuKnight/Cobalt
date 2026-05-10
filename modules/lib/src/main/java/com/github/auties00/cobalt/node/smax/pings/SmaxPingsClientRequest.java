package com.github.auties00.cobalt.node.smax.pings;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;

/**
 * Outbound {@code <iq xmlns="w:p" type="get" to="s.whatsapp.net">}
 * stanza emitted by the SMAX keep-alive pump to probe relay
 * reachability. Carries no payload — only the standard envelope
 * attributes.
 *
 * <p>The relay echoes the request {@code id} on the matching
 * {@code <iq type="result"/>} reply, which Cobalt parses via
 * {@link SmaxPingsClientResponseServerResponse}.
 *
 * <p>The request type carries no fields: WA Web's
 * {@code WASmaxOutPingsClientRequest.makeClientRequest} takes no
 * arguments and produces a stanza whose only request-side attribute
 * is {@code id = WAWap.generateId()}. Cobalt does not pre-stamp the
 * {@code id} attribute on the typed request because
 * {@link com.github.auties00.cobalt.client.WhatsAppClient#sendNode(NodeBuilder)}
 * auto-stamps a fresh {@code id} on every outbound stanza, matching
 * the {@code WAWap.generateId} contract.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPingsClientRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutPingsClientWellFormedToMixin")
public final class SmaxPingsClientRequest implements SmaxOperation.Request {
    /**
     * Constructs a request. Takes no parameters — every keep-alive
     * ping carries the same envelope; the only varying attribute is
     * the server-side-generated {@code id} stamped at dispatch by
     * {@link com.github.auties00.cobalt.client.WhatsAppClient#sendNode(NodeBuilder)}.
     */
    public SmaxPingsClientRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * <p>The stanza's {@code id} attribute is intentionally left
     * unset on the typed request: the dispatch path
     * ({@link com.github.auties00.cobalt.client.WhatsAppClient#sendNode(NodeBuilder)})
     * stamps a fresh id on every outbound stanza, matching WA Web's
     * {@code WAWap.generateId} contract.
     *
     * @return a {@link NodeBuilder} carrying the empty
     *         {@code <iq xmlns="w:p" type="get" to="s.whatsapp.net"/>}
     *         envelope; never {@code null}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPingsClientRequest",
            exports = "makeClientRequest", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        // WASmaxOutPingsClientRequest.makeClientRequest: smax("iq", {id, type:"get", xmlns:"w:p"})
        // WASmaxOutPingsClientWellFormedToMixin.mergeClientWellFormedToMixin: smax("iq", {to: S_WHATSAPP_NET})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:p")
                .attribute("to", JidServer.user())
                .attribute("type", "get");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return SmaxPingsClientRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "SmaxPingsClientRequest[]";
    }
}
