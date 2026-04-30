package com.github.auties00.cobalt.node.smax.usernotice;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
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
 * The outbound stanza variant. Wraps zero or more
 * {@code <get_disclosure_stage_by_id id="…" t="…"/>} children in the
 * canonical {@code <iq xmlns="tos" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutUserNoticeGetDisclosureStageByIdsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutUserNoticeBaseIQGetRequestMixin")
public final class SmaxUserNoticeGetDisclosureStageByIdsRequest implements SmaxOperation.Request {
    /**
     * The list of (disclosure id, timestamp) pairs to query.
     */
    private final List<DisclosureStageQuery> queries;

    /**
     * Constructs a request.
     *
     * @param queries the list of per-disclosure queries. Never
     *                {@code null}, may be empty
     * @throws NullPointerException if {@code queries} is {@code null}
     */
    public SmaxUserNoticeGetDisclosureStageByIdsRequest(List<DisclosureStageQuery> queries) {
        Objects.requireNonNull(queries, "queries cannot be null");
        this.queries = List.copyOf(queries);
    }

    /**
     * Returns the per-disclosure queries.
     *
     * @return an unmodifiable list. Never {@code null}
     */
    public List<DisclosureStageQuery> queries() {
        return queries;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <get_disclosure_stage_by_id/>} children
     *
     * @implNote {@code WASmaxOutUserNoticeGetDisclosureStageByIdsRequest.makeGetDisclosureStageByIdsRequest}
     *           composes {@code WASmaxOutUserNoticeBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over
     *           {@code <iq xmlns="tos" to="s.whatsapp.net">} with
     *           {@code REPEATED_CHILD(get_disclosure_stage_by_id, args, 0, ∞)}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutUserNoticeGetDisclosureStageByIdsRequest",
            exports = "makeGetDisclosureStageByIdsRequest",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var iqBuilder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "tos")
                .attribute("to", JidServer.user())
                .attribute("type", "get");
        for (var query : queries) {
            var child = new NodeBuilder()
                    .description("get_disclosure_stage_by_id")
                    .attribute("id", query.disclosureId())
                    .attribute("t", query.timestampSeconds())
                    .build();
            iqBuilder.content(child);
        }
        return iqBuilder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxUserNoticeGetDisclosureStageByIdsRequest) obj;
        return Objects.equals(this.queries, that.queries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queries);
    }

    @Override
    public String toString() {
        return "SmaxUserNoticeGetDisclosureStageByIdsRequest[queries=" + queries + ']';
    }

    /**
     * One {@code <get_disclosure_stage_by_id id="…" t="…"/>} child.
     * A (disclosure id, timestamp) pair routed verbatim into the
     * outbound stanza.
     */
    public static final class DisclosureStageQuery {
        /**
         * The disclosure id to query.
         */
        private final long disclosureId;

        /**
         * The client-side timestamp in seconds.
         */
        private final long timestampSeconds;

        /**
         * Constructs a new query.
         *
         * @param disclosureId     the disclosure id
         * @param timestampSeconds the client-side timestamp in
         *                         seconds
         */
        public DisclosureStageQuery(long disclosureId, long timestampSeconds) {
            this.disclosureId = disclosureId;
            this.timestampSeconds = timestampSeconds;
        }

        /**
         * Returns the disclosure id.
         *
         * @return the disclosure id
         */
        public long disclosureId() {
            return disclosureId;
        }

        /**
         * Returns the client-side timestamp.
         *
         * @return the timestamp in seconds
         */
        public long timestampSeconds() {
            return timestampSeconds;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (DisclosureStageQuery) obj;
            return this.disclosureId == that.disclosureId
                    && this.timestampSeconds == that.timestampSeconds;
        }

        @Override
        public int hashCode() {
            return Objects.hash(disclosureId, timestampSeconds);
        }

        @Override
        public String toString() {
            return "SmaxUserNoticeGetDisclosureStageByIdsRequest.DisclosureStageQuery[disclosureId="
                    + disclosureId + ", timestampSeconds=" + timestampSeconds + ']';
        }
    }
}
