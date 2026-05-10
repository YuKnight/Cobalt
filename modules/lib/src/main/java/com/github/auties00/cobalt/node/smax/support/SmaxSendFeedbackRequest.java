package com.github.auties00.cobalt.node.smax.support;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutSupportMessageFeedbackSendFeedbackRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutSupportMessageFeedbackHackBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSupportMessageFeedbackBaseIQSetRequestMixin")
public final class SmaxSendFeedbackRequest implements SmaxOperation.Request {
    /**
     * The optional sender JID. When set, routed verbatim into
     * the IQ's {@code from} attribute.
     */
    private final Jid iqFrom;

    /**
     * The id of the rated message, echoed into the
     * {@code <message id=…/>} child.
     */
    private final String messageId;

    /**
     * The 1..10 feedback entries. Each carrying a server-side
     * {@code kind} label.
     */
    private final List<String> feedbackKinds;

    /**
     * Constructs a new feedback request.
     *
     * @param iqFrom        the optional sender JID; may be
     *                      {@code null}
     * @param messageId     the rated message id; never
     *                      {@code null}
     * @param feedbackKinds the 1..10 feedback labels; never
     *                      {@code null}
     * @throws NullPointerException     if {@code messageId} or
     *                                  {@code feedbackKinds} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code feedbackKinds}
     *                                  is empty or has more than
     *                                  ten entries
     */
    public SmaxSendFeedbackRequest(Jid iqFrom, String messageId, List<String> feedbackKinds) {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(feedbackKinds, "feedbackKinds cannot be null");
        if (feedbackKinds.isEmpty() || feedbackKinds.size() > 10) {
            throw new IllegalArgumentException("feedbackKinds must contain 1..10 entries");
        }
        this.iqFrom = iqFrom;
        this.messageId = messageId;
        this.feedbackKinds = List.copyOf(feedbackKinds);
    }

    /**
     * Returns the optional sender JID.
     *
     * @return an {@link Optional} carrying the sender JID, or
     *         empty when omitted
     */
    public Optional<Jid> iqFrom() {
        return Optional.ofNullable(iqFrom);
    }

    /**
     * Returns the rated message id.
     *
     * @return the message id; never {@code null}
     */
    public String messageId() {
        return messageId;
    }

    /**
     * Returns the feedback labels.
     *
     * @return an unmodifiable list of 1..10 entries; never
     *         {@code null}
     */
    public List<String> feedbackKinds() {
        return feedbackKinds;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutSupportMessageFeedbackSendFeedbackRequest",
            exports = "makeSendFeedbackRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var messageNode = new NodeBuilder()
                .description("message")
                .attribute("id", messageId)
                .build();
        var feedbackNodes = feedbackKinds.stream()
                .map(kind -> new NodeBuilder()
                        .description("feedback")
                        .attribute("kind", kind)
                        .build())
                .toList();
        var feedbackListNode = new NodeBuilder()
                .description("feedback_list")
                .content(feedbackNodes)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("smax_id", 138)
                .attribute("from", iqFrom)
                .attribute("to", Jid.userServer())
                .attribute("type", "set")
                .content(List.of(messageNode, feedbackListNode));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxSendFeedbackRequest) obj;
        return Objects.equals(this.iqFrom, that.iqFrom)
                && Objects.equals(this.messageId, that.messageId)
                && Objects.equals(this.feedbackKinds, that.feedbackKinds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iqFrom, messageId, feedbackKinds);
    }

    @Override
    public String toString() {
        return "SmaxSendFeedbackRequest[iqFrom=" + iqFrom
                + ", messageId=" + messageId
                + ", feedbackKinds=" + feedbackKinds + ']';
    }
}
