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
 * The outbound stanza variant. Wraps the
 * {@code <status_updates count since? before|after>} payload in the
 * canonical
 * {@code <iq xmlns="newsletter" type="get" to=NEWSLETTER_JID>}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersGetNewsletterStatusUpdatesRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersNewsletterIQGetRequestMixin")
public final class SmaxNewslettersGetNewsletterStatusUpdatesRequest implements SmaxOperation.Request {
    /**
     * The newsletter JID being polled.
     */
    private final Jid newsletterJid;

    /**
     * The maximum number of {@code <status>} entries the relay should
     * return.
     */
    private final int count;

    /**
     * The optional unix-second floor.
     */
    private final Long since;

    /**
     * The pagination cursor; never {@code null}.
     */
    private final SmaxNewslettersGetNewsletterStatusUpdatesDirection direction;

    /**
     * Constructs a new request.
     *
     * @param newsletterJid the newsletter JID; never {@code null}
     * @param count         the per-call cap; must be non-negative
     * @param since         the optional unix-second floor; may be
     *                      {@code null}
     * @param direction     the cursor; never {@code null}
     * @throws NullPointerException if {@code newsletterJid} or
     *                              {@code direction} is {@code null}
     */
    public SmaxNewslettersGetNewsletterStatusUpdatesRequest(Jid newsletterJid, int count, Long since, SmaxNewslettersGetNewsletterStatusUpdatesDirection direction) {
        this.newsletterJid = Objects.requireNonNull(newsletterJid, "newsletterJid cannot be null");
        this.count = count;
        this.since = since;
        this.direction = Objects.requireNonNull(direction, "direction cannot be null");
    }

    /**
     * Returns the newsletter JID being polled.
     *
     * @return the JID; never {@code null}
     */
    public Jid newsletterJid() {
        return newsletterJid;
    }

    /**
     * Returns the per-call cap.
     *
     * @return the count
     */
    public int count() {
        return count;
    }

    /**
     * Returns the optional unix-second floor.
     *
     * @return an {@link Optional} carrying the floor, or empty when
     *         omitted
     */
    public Optional<Long> since() {
        return Optional.ofNullable(since);
    }

    /**
     * Returns the cursor direction.
     *
     * @return the cursor; never {@code null}
     */
    public SmaxNewslettersGetNewsletterStatusUpdatesDirection direction() {
        return direction;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <status_updates>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutNewslettersGetNewsletterStatusUpdatesRequest",
            exports = "makeGetNewsletterStatusUpdatesRequest",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var updatesBuilder = new NodeBuilder()
                .description("status_updates")
                .attribute("count", count);
        if (since != null) {
            updatesBuilder.attribute("since", since);
        }
        switch (direction) {
            case SmaxNewslettersGetNewsletterStatusUpdatesDirection.Before before -> updatesBuilder.attribute("before", before.pivot());
            case SmaxNewslettersGetNewsletterStatusUpdatesDirection.After after -> updatesBuilder.attribute("after", after.pivot());
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "newsletter")
                .attribute("to", newsletterJid)
                .attribute("type", "get")
                .content(updatesBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxNewslettersGetNewsletterStatusUpdatesRequest) obj;
        return this.count == that.count
                && Objects.equals(this.newsletterJid, that.newsletterJid)
                && Objects.equals(this.since, that.since)
                && Objects.equals(this.direction, that.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newsletterJid, count, since, direction);
    }

    @Override
    public String toString() {
        return "SmaxNewslettersGetNewsletterStatusUpdatesRequest[newsletterJid="
                + newsletterJid + ", count=" + count
                + ", since=" + since + ", direction=" + direction + ']';
    }
}
