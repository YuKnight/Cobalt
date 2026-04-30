package com.github.auties00.cobalt.node.smax.newsletters;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
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
 * The outbound stanza variant. Wraps the {@code <statuses count …>}
 * payload in the canonical
 * {@code <iq xmlns="newsletter" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersGetNewsletterStatusesRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersNewsletterStatusRequestIQPayloadMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersNewsletterStatusRequestPayloadMixin")
public final class SmaxNewslettersGetNewsletterStatusesRequest implements SmaxOperation.Request {
    /**
     * The maximum number of {@code <status>} entries the relay should
     * return.
     */
    private final int count;

    /**
     * The newsletter addressing parameters (jid or invite); reused
     * from the {@link SmaxNewslettersGetNewsletterMessages} sibling.
     */
    private final SmaxNewslettersGetNewsletterMessagesQueryParams queryParams;

    /**
     * The optional pagination cursor; {@code null} fetches the latest
     * slice.
     */
    private final SmaxNewslettersGetNewsletterStatusesDirection direction;

    /**
     * Constructs a new request.
     *
     * @param count       the per-call cap; must be non-negative
     * @param queryParams the newsletter addressing parameters; never
     *                    {@code null}
     * @param direction   the optional pagination cursor; may be
     *                    {@code null}
     * @throws NullPointerException if {@code queryParams} is
     *                              {@code null}
     */
    public SmaxNewslettersGetNewsletterStatusesRequest(int count,
                   SmaxNewslettersGetNewsletterMessagesQueryParams queryParams,
                   SmaxNewslettersGetNewsletterStatusesDirection direction) {
        this.count = count;
        this.queryParams = Objects.requireNonNull(queryParams, "queryParams cannot be null");
        this.direction = direction;
    }

    /**
     * Returns the per-call status cap.
     *
     * @return the count
     */
    public int count() {
        return count;
    }

    /**
     * Returns the newsletter addressing parameters.
     *
     * @return the parameters; never {@code null}
     */
    public SmaxNewslettersGetNewsletterMessagesQueryParams queryParams() {
        return queryParams;
    }

    /**
     * Returns the optional pagination cursor.
     *
     * @return an {@link Optional} carrying the cursor, or empty when
     *         requesting the latest slice
     */
    public Optional<SmaxNewslettersGetNewsletterStatusesDirection> direction() {
        return Optional.ofNullable(direction);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <statuses>} payload
     *
     * @implNote {@code WASmaxOutNewslettersGetNewsletterStatusesRequest.makeGetNewsletterStatusesRequest}
     *           composes
     *           {@code WASmaxOutNewslettersNewsletterStatusRequestIQPayloadMixin}
     *           around the {@code <statuses count="…">} root, embeds
     *           the addressing block via
     *           {@code mergeQueryNewsletterParamsMixin}, and layers the
     *           optional direction via {@code mergeStatusDirections}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutNewslettersGetNewsletterStatusesRequest",
            exports = "makeGetNewsletterStatusesRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var anyBuilder = new NodeBuilder()
                .description("smax$any");
        switch (queryParams) {
            case SmaxNewslettersGetNewsletterMessagesQueryParams.ByJid byJid -> {
                anyBuilder.attribute("type", "jid");
                anyBuilder.attribute("jid", byJid.newsletterJid());
                byJid.viewRole().ifPresent(role -> anyBuilder.attribute("view_role", role));
            }
            case SmaxNewslettersGetNewsletterMessagesQueryParams.ByInvite byInvite -> {
                anyBuilder.attribute("type", "invite");
                anyBuilder.attribute("key", byInvite.inviteKey());
                byInvite.viewRole().ifPresent(role -> anyBuilder.attribute("view_role", role));
            }
        }
        var statusesBuilder = new NodeBuilder()
                .description("statuses")
                .attribute("count", count);
        if (direction != null) {
            switch (direction) {
                case SmaxNewslettersGetNewsletterStatusesDirection.Before before -> statusesBuilder.attribute("before", before.pivot());
                case SmaxNewslettersGetNewsletterStatusesDirection.After after -> statusesBuilder.attribute("after", after.pivot());
            }
        }
        statusesBuilder.content(anyBuilder.build());
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "newsletter")
                .attribute("to", Jid.userServer())
                .attribute("type", "get")
                .content(statusesBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxNewslettersGetNewsletterStatusesRequest) obj;
        return this.count == that.count
                && Objects.equals(this.queryParams, that.queryParams)
                && Objects.equals(this.direction, that.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, queryParams, direction);
    }

    @Override
    public String toString() {
        return "SmaxNewslettersGetNewsletterStatusesRequest[count=" + count
                + ", queryParams=" + queryParams
                + ", direction=" + direction + ']';
    }
}
