package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps a per-group {@code <group jid/>}
 * list in the canonical {@code <iq xmlns="w:g2" type="get" to="g.us">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBatchGetGroupInfoRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetServerMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQGetRequestMixin")
public final class SmaxGroupsBatchGetGroupInfoRequest implements SmaxOperation.Request {
    /**
     * The list of group JIDs to fetch metadata for. The relay
     * accepts between {@code 1} and {@code 10000} entries.
     */
    private final List<Jid> groupJids;

    /**
     * The optional caller-supplied correlation token surfaced as the
     * {@code <query context="…">} attribute.
     */
    private final String queryContext;

    /**
     * Constructs a request for the given list of groups.
     *
     * @param groupJids the group JIDs to fetch; never {@code null}
     *                  and never empty
     * @throws NullPointerException     if {@code groupJids} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code groupJids} is empty
     */
    public SmaxGroupsBatchGetGroupInfoRequest(List<Jid> groupJids) {
        this(groupJids, null);
    }

    /**
     * Constructs a request for the given list of groups with the
     * given correlation token.
     *
     * @param groupJids    the group JIDs to fetch; never
     *                     {@code null} and never empty
     * @param queryContext the optional correlation token; may be
     *                     {@code null}
     * @throws NullPointerException     if {@code groupJids} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code groupJids} is empty
     */
    public SmaxGroupsBatchGetGroupInfoRequest(List<Jid> groupJids, String queryContext) {
        Objects.requireNonNull(groupJids, "groupJids cannot be null");
        if (groupJids.isEmpty()) {
            throw new IllegalArgumentException("groupJids cannot be empty");
        }
        this.groupJids = List.copyOf(groupJids);
        this.queryContext = queryContext;
    }

    /**
     * Returns the list of groups carried by this request.
     *
     * @return an unmodifiable list of group JIDs; never {@code null}
     */
    public List<Jid> groupJids() {
        return groupJids;
    }

    /**
     * Returns the optional correlation token.
     *
     * @return an {@link Optional} carrying the correlation token, or
     *         empty when the caller did not supply one
     */
    public Optional<String> queryContext() {
        return Optional.ofNullable(queryContext);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         per-group {@code <query/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsBatchGetGroupInfoRequest",
            exports = "makeBatchGetGroupInfoRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var groupNodes = new ArrayList<Node>(groupJids.size());
        for (var groupJid : groupJids) {
            var groupNode = new NodeBuilder()
                    .description("group")
                    .attribute("jid", groupJid)
                    .build();
            groupNodes.add(groupNode);
        }
        var queryBuilder = new NodeBuilder()
                .description("query")
                .content(groupNodes);
        if (queryContext != null) {
            queryBuilder.attribute("context", queryContext);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", JidServer.groupOrCommunity())
                .attribute("type", "get")
                .content(queryBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsBatchGetGroupInfoRequest) obj;
        return Objects.equals(this.groupJids, that.groupJids)
                && Objects.equals(this.queryContext, that.queryContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJids, queryContext);
    }

    @Override
    public String toString() {
        return "SmaxGroupsBatchGetGroupInfoRequest[groupJids=" + groupJids
                + ", queryContext=" + queryContext + ']';
    }
}
