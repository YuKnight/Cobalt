package com.github.auties00.cobalt.node.iq.tos;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The outbound {@code <iq xmlns="tos" type="get">} stanza variant —
 * wraps a {@code <request>} child whose contents are one
 * {@code <notice id="…"/>} per requested notice id.
 */
@WhatsAppWebModule(moduleName = "WAWebTosJob")
public final class IqQueryTosRequest implements IqOperation.Request {
    /**
     * The list of notice ids being queried. Each entry is routed
     * verbatim into the {@code id} attribute of one {@code <notice/>}
     * child.
     */
    private final List<String> noticeIds;

    /**
     * Constructs a new query-tos request.
     *
     * @param noticeIds the list of notice ids to query; never
     *                  {@code null}, may be empty
     * @throws NullPointerException if {@code noticeIds} is {@code null}
     */
    public IqQueryTosRequest(List<String> noticeIds) {
        Objects.requireNonNull(noticeIds, "noticeIds cannot be null");
        this.noticeIds = List.copyOf(noticeIds);
    }

    /**
     * Returns the unmodifiable list of notice ids being queried.
     *
     * @return the notice ids; never {@code null}
     */
    public List<String> noticeIds() {
        return noticeIds;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <request>} payload
     *
     * @implNote {@code WAWebTosJob.queryTosState} composes the IQ via
     *           {@code wap("iq", {xmlns:"tos", id, type:"get",
     *           to:S_WHATSAPP_NET}, wap("request", null,
     *           e.map(id =&#42;> wap("notice", {id}))))}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebTosJob",
            exports = "queryTosState", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebTosJob: e.map(id => wap("notice", {id: CUSTOM_STRING(id)}))
        var noticeNodes = new ArrayList<Node>(noticeIds.size());
        for (var id : noticeIds) {
            var noticeNode = new NodeBuilder()
                    .description("notice")
                    .attribute("id", id)
                    .build();
            noticeNodes.add(noticeNode);
        }
        // WAWebTosJob: wap("request", null, [...])
        var requestNode = new NodeBuilder()
                .description("request")
                .content(noticeNodes)
                .build();
        // WAWebTosJob: wap("iq", {xmlns:"tos", id, type:"get", to:S_WHATSAPP_NET}, request)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "tos")
                .attribute("to", Jid.userServer())
                .attribute("type", "get")
                .content(requestNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqQueryTosRequest) obj;
        return Objects.equals(this.noticeIds, that.noticeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noticeIds);
    }

    @Override
    public String toString() {
        return "IqQueryTosRequest[noticeIds=" + noticeIds + ']';
    }
}
