package com.github.auties00.cobalt.node.smax.support;

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
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutSpamStatusReportRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBaseReportMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamMessageRecipientMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamFRXMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamIsKnownChatMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBizOptOutMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBizReportMixin")
public final class SmaxStatusReportRequest implements SmaxOperation.Request {
    /**
     * The status owner / target JID, routed into the
     * {@code <spam_list jid>} attribute.
     */
    private final Jid spamListJid;

    /**
     * The spam-flow string surfacing the user-visible report flow.
     */
    private final String spamListSpamFlow;

    /**
     * The sender JID of the offending status message.
     */
    private final Jid messageFrom;

    /**
     * The status message timestamp.
     */
    private final long messageTimestamp;

    /**
     * The status message stanza id.
     */
    private final String messageId;

    /**
     * The optional recipient JID. When supplied, routed into the
     * {@code <message to>} attribute via
     * {@code WASmaxOutSpamMessageRecipientMixin}.
     */
    private final Jid messageTo;

    /**
     * The optional {@code is_known_chat} marker.
     */
    private final String spamListIsKnownChat;

    /**
     * The optional biz-opt-out child node.
     */
    private final Node bizOptOutChild;

    /**
     * The optional biz-report child node.
     */
    private final Node bizReportChild;

    /**
     * The optional FRX child node.
     */
    private final Node frxChild;

    /**
     * The optional pre-built message child. When set, the request
     * embeds it verbatim instead of synthesising the
     * {@code <message>} envelope from the scalar fields.
     */
    private final Node messageChild;

    /**
     * Constructs a request from the {@link Builder}.
     *
     * @param builder the source builder; never {@code null}
     */
    private SmaxStatusReportRequest(Builder builder) {
        this.spamListJid = Objects.requireNonNull(builder.spamListJid, "spamListJid cannot be null");
        this.spamListSpamFlow = Objects.requireNonNull(builder.spamListSpamFlow,
                "spamListSpamFlow cannot be null");
        this.messageFrom = Objects.requireNonNull(builder.messageFrom, "messageFrom cannot be null");
        this.messageTimestamp = builder.messageTimestamp;
        this.messageId = Objects.requireNonNull(builder.messageId, "messageId cannot be null");
        this.messageTo = builder.messageTo;
        this.spamListIsKnownChat = builder.spamListIsKnownChat;
        this.bizOptOutChild = builder.bizOptOutChild;
        this.bizReportChild = builder.bizReportChild;
        this.frxChild = builder.frxChild;
        this.messageChild = builder.messageChild;
    }

    /**
     * Returns a new {@link Builder}.
     *
     * @return a new builder; never {@code null}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the status owner JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid spamListJid() {
        return spamListJid;
    }

    /**
     * Returns the spam-flow string.
     *
     * @return the spam-flow; never {@code null}
     */
    public String spamListSpamFlow() {
        return spamListSpamFlow;
    }

    /**
     * Returns the message sender JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid messageFrom() {
        return messageFrom;
    }

    /**
     * Returns the message timestamp.
     *
     * @return the timestamp
     */
    public long messageTimestamp() {
        return messageTimestamp;
    }

    /**
     * Returns the message stanza id.
     *
     * @return the id; never {@code null}
     */
    public String messageId() {
        return messageId;
    }

    /**
     * Returns the optional recipient JID.
     *
     * @return an {@link Optional} carrying the JID, or empty when
     *         omitted
     */
    public Optional<Jid> messageTo() {
        return Optional.ofNullable(messageTo);
    }

    /**
     * Returns the optional {@code is_known_chat} marker.
     *
     * @return an {@link Optional} carrying the marker, or empty when
     *         omitted
     */
    public Optional<String> spamListIsKnownChat() {
        return Optional.ofNullable(spamListIsKnownChat);
    }

    /**
     * Returns the optional biz-opt-out child.
     *
     * @return an {@link Optional} carrying the node, or empty when
     *         omitted
     */
    public Optional<Node> bizOptOutChild() {
        return Optional.ofNullable(bizOptOutChild);
    }

    /**
     * Returns the optional biz-report child.
     *
     * @return an {@link Optional} carrying the node, or empty when
     *         omitted
     */
    public Optional<Node> bizReportChild() {
        return Optional.ofNullable(bizReportChild);
    }

    /**
     * Returns the optional FRX child.
     *
     * @return an {@link Optional} carrying the node, or empty when
     *         omitted
     */
    public Optional<Node> frxChild() {
        return Optional.ofNullable(frxChild);
    }

    /**
     * Returns the optional pre-built message child.
     *
     * @return an {@link Optional} carrying the node, or empty when
     *         the request should synthesise the envelope from the
     *         scalar fields
     */
    public Optional<Node> messageChild() {
        return Optional.ofNullable(messageChild);
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutSpamStatusReportRequest",
            exports = "makeStatusReportRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutSpamMessageRecipientMixin (optional): smax("message", {to})
        Node messageNode;
        if (messageChild != null) {
            messageNode = messageChild;
        } else {
            var messageBuilder = new NodeBuilder()
                    .description("message")
                    .attribute("from", messageFrom)
                    .attribute("t", messageTimestamp)
                    .attribute("id", messageId);
            if (messageTo != null) {
                messageBuilder.attribute("to", messageTo);
            }
            messageNode = messageBuilder.build();
        }
        // WASmaxOutSpamIsKnownChatMixin (optional): smax("spam_list", {is_known_chat})
        var spamListBuilder = new NodeBuilder()
                .description("spam_list")
                .attribute("jid", spamListJid)
                .attribute("spam_flow", spamListSpamFlow);
        if (spamListIsKnownChat != null) {
            spamListBuilder.attribute("is_known_chat", spamListIsKnownChat);
        }
        spamListBuilder.content(messageNode);
        if (bizOptOutChild != null) {
            spamListBuilder.content(bizOptOutChild);
        }
        if (bizReportChild != null) {
            spamListBuilder.content(bizReportChild);
        }
        var iqBuilder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "spam")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(spamListBuilder.build());
        if (frxChild != null) {
            iqBuilder.content(frxChild);
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
        var that = (SmaxStatusReportRequest) obj;
        return this.messageTimestamp == that.messageTimestamp
                && Objects.equals(this.spamListJid, that.spamListJid)
                && Objects.equals(this.spamListSpamFlow, that.spamListSpamFlow)
                && Objects.equals(this.messageFrom, that.messageFrom)
                && Objects.equals(this.messageId, that.messageId)
                && Objects.equals(this.messageTo, that.messageTo)
                && Objects.equals(this.spamListIsKnownChat, that.spamListIsKnownChat)
                && Objects.equals(this.bizOptOutChild, that.bizOptOutChild)
                && Objects.equals(this.bizReportChild, that.bizReportChild)
                && Objects.equals(this.frxChild, that.frxChild)
                && Objects.equals(this.messageChild, that.messageChild);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spamListJid, spamListSpamFlow, messageFrom, messageTimestamp, messageId,
                messageTo, spamListIsKnownChat, bizOptOutChild, bizReportChild, frxChild, messageChild);
    }

    @Override
    public String toString() {
        return "SmaxStatusReportRequest[spamListJid=" + spamListJid
                + ", spamListSpamFlow=" + spamListSpamFlow
                + ", messageFrom=" + messageFrom
                + ", messageTimestamp=" + messageTimestamp
                + ", messageId=" + messageId
                + ", messageTo=" + messageTo + ']';
    }

    /**
     * Builder for {@link SmaxStatusReportRequest}. The canonical entry point for
     * assembling a status spam report.
     */
    public static final class Builder {
        /**
         * The status owner JID; required.
         */
        private Jid spamListJid;

        /**
         * The spam-flow string; required.
         */
        private String spamListSpamFlow;

        /**
         * The sender JID; required.
         */
        private Jid messageFrom;

        /**
         * The message timestamp.
         */
        private long messageTimestamp;

        /**
         * The message stanza id; required.
         */
        private String messageId;

        /**
         * The optional recipient JID.
         */
        private Jid messageTo;

        /**
         * The optional is-known-chat marker.
         */
        private String spamListIsKnownChat;

        /**
         * The optional biz-opt-out child.
         */
        private Node bizOptOutChild;

        /**
         * The optional biz-report child.
         */
        private Node bizReportChild;

        /**
         * The optional FRX child.
         */
        private Node frxChild;

        /**
         * The optional pre-built message child.
         */
        private Node messageChild;

        /**
         * Constructs a new builder.
         */
        public Builder() {
        }

        /**
         * Sets the status owner JID.
         *
         * @param spamListJid the JID; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code spamListJid} is
         *                              {@code null}
         */
        public Builder spamListJid(Jid spamListJid) {
            this.spamListJid = Objects.requireNonNull(spamListJid, "spamListJid cannot be null");
            return this;
        }

        /**
         * Sets the spam-flow string.
         *
         * @param spamListSpamFlow the spam-flow; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code spamListSpamFlow} is
         *                              {@code null}
         */
        public Builder spamListSpamFlow(String spamListSpamFlow) {
            this.spamListSpamFlow = Objects.requireNonNull(spamListSpamFlow,
                    "spamListSpamFlow cannot be null");
            return this;
        }

        /**
         * Sets the sender JID.
         *
         * @param messageFrom the JID; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code messageFrom} is
         *                              {@code null}
         */
        public Builder messageFrom(Jid messageFrom) {
            this.messageFrom = Objects.requireNonNull(messageFrom, "messageFrom cannot be null");
            return this;
        }

        /**
         * Sets the message timestamp.
         *
         * @param messageTimestamp the timestamp
         * @return this builder
         */
        public Builder messageTimestamp(long messageTimestamp) {
            this.messageTimestamp = messageTimestamp;
            return this;
        }

        /**
         * Sets the message stanza id.
         *
         * @param messageId the id; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code messageId} is
         *                              {@code null}
         */
        public Builder messageId(String messageId) {
            this.messageId = Objects.requireNonNull(messageId, "messageId cannot be null");
            return this;
        }

        /**
         * Sets the optional recipient JID.
         *
         * @param messageTo the JID; may be {@code null}
         * @return this builder
         */
        public Builder messageTo(Jid messageTo) {
            this.messageTo = messageTo;
            return this;
        }

        /**
         * Sets the optional is-known-chat marker.
         *
         * @param spamListIsKnownChat the marker; may be {@code null}
         * @return this builder
         */
        public Builder spamListIsKnownChat(String spamListIsKnownChat) {
            this.spamListIsKnownChat = spamListIsKnownChat;
            return this;
        }

        /**
         * Sets the optional biz-opt-out child node.
         *
         * @param bizOptOutChild the node; may be {@code null}
         * @return this builder
         */
        public Builder bizOptOutChild(Node bizOptOutChild) {
            this.bizOptOutChild = bizOptOutChild;
            return this;
        }

        /**
         * Sets the optional biz-report child node.
         *
         * @param bizReportChild the node; may be {@code null}
         * @return this builder
         */
        public Builder bizReportChild(Node bizReportChild) {
            this.bizReportChild = bizReportChild;
            return this;
        }

        /**
         * Sets the optional FRX child node.
         *
         * @param frxChild the node; may be {@code null}
         * @return this builder
         */
        public Builder frxChild(Node frxChild) {
            this.frxChild = frxChild;
            return this;
        }

        /**
         * Sets the optional pre-built message child, embeds the
         * supplied node verbatim instead of synthesising the
         * {@code <message>} envelope from the scalar fields.
         *
         * @param messageChild the node; may be {@code null}
         * @return this builder
         */
        public Builder messageChild(Node messageChild) {
            this.messageChild = messageChild;
            return this;
        }

        /**
         * Builds the request.
         *
         * @return a new {@link SmaxStatusReportRequest}; never {@code null}
         * @throws NullPointerException if any required field is
         *                              missing
         */
        public SmaxStatusReportRequest build() {
            return new SmaxStatusReportRequest(this);
        }
    }
}
