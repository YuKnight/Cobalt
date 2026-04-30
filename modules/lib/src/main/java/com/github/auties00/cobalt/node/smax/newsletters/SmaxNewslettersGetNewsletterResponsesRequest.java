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
 * The outbound stanza variant. Wraps the {@code <question_responses>}
 * payload in the canonical
 * {@code <iq xmlns="newsletter" type="get" to=NEWSLETTER_JID>}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersGetNewsletterResponsesRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersNewsletterIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersBeforeQuestionResponseMixinMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersFilterQuestionResponseMixinMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersSearchQuestionResponseMixinMixin")
public final class SmaxNewslettersGetNewsletterResponsesRequest implements SmaxOperation.Request {
    /**
     * The newsletter JID being queried; routed verbatim into the IQ's
     * {@code to} attribute.
     */
    private final Jid newsletterJid;

    /**
     * The server-id of the question message whose responses are being
     * fetched.
     */
    private final long questionResponsesServerId;

    /**
     * The maximum number of {@code <question_response>} entries the
     * relay should return in this slice.
     */
    private final int questionResponsesCount;

    /**
     * The optional opaque pagination cursor, a previous slice's
     * tail-cursor handed back verbatim by the caller.
     */
    private final String questionResponsesBefore;

    /**
     * The optional contacts/replied filter; {@code null} disables
     * filtering.
     */
    private final SmaxNewslettersGetNewsletterResponsesFilter filter;

    /**
     * The optional free-text search string applied against the
     * response payloads.
     */
    private final String searchText;

    /**
     * Constructs a new request.
     *
     * @param newsletterJid             the newsletter JID; never
     *                                  {@code null}
     * @param questionResponsesServerId the question's server-id
     * @param questionResponsesCount    the per-call entry cap
     * @param questionResponsesBefore   the optional pagination cursor;
     *                                  may be {@code null}
     * @param filter                    the optional filter; may be
     *                                  {@code null}
     * @param searchText                the optional search string; may
     *                                  be {@code null}
     * @throws NullPointerException if {@code newsletterJid} is
     *                              {@code null}
     */
    public SmaxNewslettersGetNewsletterResponsesRequest(Jid newsletterJid, long questionResponsesServerId,
                   int questionResponsesCount, String questionResponsesBefore,
                   SmaxNewslettersGetNewsletterResponsesFilter filter, String searchText) {
        this.newsletterJid = Objects.requireNonNull(newsletterJid, "newsletterJid cannot be null");
        this.questionResponsesServerId = questionResponsesServerId;
        this.questionResponsesCount = questionResponsesCount;
        this.questionResponsesBefore = questionResponsesBefore;
        this.filter = filter;
        this.searchText = searchText;
    }

    /**
     * Returns the newsletter JID being queried.
     *
     * @return the JID; never {@code null}
     */
    public Jid newsletterJid() {
        return newsletterJid;
    }

    /**
     * Returns the question's server-id.
     *
     * @return the server-id
     */
    public long questionResponsesServerId() {
        return questionResponsesServerId;
    }

    /**
     * Returns the per-call entry cap.
     *
     * @return the count
     */
    public int questionResponsesCount() {
        return questionResponsesCount;
    }

    /**
     * Returns the optional pagination cursor.
     *
     * @return an {@link Optional} carrying the cursor, or empty when
     *         requesting the first slice
     */
    public Optional<String> questionResponsesBefore() {
        return Optional.ofNullable(questionResponsesBefore);
    }

    /**
     * Returns the optional contacts/replied filter.
     *
     * @return an {@link Optional} carrying the filter, or empty when
     *         no filter is applied
     */
    public Optional<SmaxNewslettersGetNewsletterResponsesFilter> filter() {
        return Optional.ofNullable(filter);
    }

    /**
     * Returns the optional free-text search string.
     *
     * @return an {@link Optional} carrying the search string, or empty
     *         when no search is applied
     */
    public Optional<String> searchText() {
        return Optional.ofNullable(searchText);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <question_responses>} payload
     *
     * @implNote {@code WASmaxOutNewslettersGetNewsletterResponsesRequest.makeGetNewsletterResponsesRequest}
     *           composes
     *           {@code WASmaxOutNewslettersNewsletterIQGetRequestMixin}
     *           ({@code <iq to=NEWSLETTER_JID xmlns="newsletter">})
     *           with {@code WASmaxOutNewslettersBaseIQGetRequestMixin}
     *           ({@code id=generateId() type="get"}) over a
     *           {@code <question_responses server_id count>} root,
     *           then layers
     *           {@code mergeBeforeQuestionResponseMixinMixin},
     *           {@code mergeFilterQuestionResponseMixinMixin}, and
     *           {@code mergeSearchQuestionResponseMixinMixin} as
     *           optional attribute / sibling overlays.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutNewslettersGetNewsletterResponsesRequest",
            exports = "makeGetNewsletterResponsesRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        if (filter != null) {
            var filtersBuilder = new NodeBuilder()
                    .description("filters");
            switch (filter) {
                case SmaxNewslettersGetNewsletterResponsesFilter.Contacts ignored -> filtersBuilder.content(new NodeBuilder()
                        .description("contacts")
                        .build());
                case SmaxNewslettersGetNewsletterResponsesFilter.Replied ignored -> filtersBuilder.content(new NodeBuilder()
                        .description("replied")
                        .build());
            }
            children.add(filtersBuilder.build());
        }
        if (searchText != null) {
            children.add(new NodeBuilder()
                    .description("search")
                    .attribute("text", searchText)
                    .build());
        }
        var qrBuilder = new NodeBuilder()
                .description("question_responses")
                .attribute("server_id", questionResponsesServerId)
                .attribute("count", questionResponsesCount);
        if (questionResponsesBefore != null) {
            qrBuilder.attribute("before", questionResponsesBefore);
        }
        if (!children.isEmpty()) {
            qrBuilder.content(children);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "newsletter")
                .attribute("to", newsletterJid)
                .attribute("type", "get")
                .content(qrBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxNewslettersGetNewsletterResponsesRequest) obj;
        return this.questionResponsesServerId == that.questionResponsesServerId
                && this.questionResponsesCount == that.questionResponsesCount
                && Objects.equals(this.newsletterJid, that.newsletterJid)
                && Objects.equals(this.questionResponsesBefore, that.questionResponsesBefore)
                && Objects.equals(this.filter, that.filter)
                && Objects.equals(this.searchText, that.searchText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newsletterJid, questionResponsesServerId, questionResponsesCount,
                questionResponsesBefore, filter, searchText);
    }

    @Override
    public String toString() {
        return "SmaxNewslettersGetNewsletterResponsesRequest[newsletterJid=" + newsletterJid
                + ", questionResponsesServerId=" + questionResponsesServerId
                + ", questionResponsesCount=" + questionResponsesCount
                + ", questionResponsesBefore=" + questionResponsesBefore
                + ", filter=" + filter
                + ", searchText=" + searchText + ']';
    }
}
