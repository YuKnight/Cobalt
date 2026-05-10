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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 *
 * <p>The wire shape is intentionally extensible. The WA Web
 * {@code makeGroupReportRequest} layers up to six optional mixins
 * over the base envelope, and the {@link Builder} surface mirrors
 * each one.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutSpamGroupReportRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBaseReportMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamEntitySubjectMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamFRXMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamIsKnownChatMixin")
public final class SmaxGroupReportRequest implements SmaxOperation.Request {
    /**
     * The group JID being reported, routed into the
     * {@code <spam_list jid>} attribute.
     */
    private final Jid spamListJid;

    /**
     * The spam-flow string surfacing the user-visible report flow.
     */
    private final String spamListSpamFlow;

    /**
     * The optional adder JID. When supplied, routed into the
     * {@code <spam_list source>} attribute (the user who added the
     * reporter to the group).
     */
    private final Jid spamListSource;

    /**
     * The optional group subject string echoed by the relay for
     * attribution context.
     */
    private final String spamListSubject;

    /**
     * The optional {@code is_known_chat} marker, surfaces whether
     * the reporter has a prior history with the group.
     */
    private final String spamListIsKnownChat;

    /**
     * The optional FRX (free-form reporting extensions) bundle. A
     * pre-built {@code <frx>} child including tagset, context and
     * parameters; embedded verbatim when supplied.
     */
    private final Node frxChild;

    /**
     * The pre-built {@code <message>} children (0..210).
     */
    private final List<Node> messageChildren;

    /**
     * The pre-built {@code <call>} children (0..5).
     */
    private final List<Node> callChildren;

    /**
     * Constructs a request from the {@link Builder}.
     *
     * @param builder the source builder; never {@code null}
     */
    private SmaxGroupReportRequest(Builder builder) {
        this.spamListJid = Objects.requireNonNull(builder.spamListJid, "spamListJid cannot be null");
        this.spamListSpamFlow = Objects.requireNonNull(builder.spamListSpamFlow,
                "spamListSpamFlow cannot be null");
        this.spamListSource = builder.spamListSource;
        this.spamListSubject = builder.spamListSubject;
        this.spamListIsKnownChat = builder.spamListIsKnownChat;
        this.frxChild = builder.frxChild;
        this.messageChildren = List.copyOf(builder.messageChildren);
        this.callChildren = List.copyOf(builder.callChildren);
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
     * Returns the group JID.
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
     * Returns the optional adder JID.
     *
     * @return an {@link Optional} carrying the JID, or empty when
     *         omitted
     */
    public Optional<Jid> spamListSource() {
        return Optional.ofNullable(spamListSource);
    }

    /**
     * Returns the optional subject string.
     *
     * @return an {@link Optional} carrying the subject, or empty when
     *         omitted
     */
    public Optional<String> spamListSubject() {
        return Optional.ofNullable(spamListSubject);
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
     * Returns the optional FRX child.
     *
     * @return an {@link Optional} carrying the node, or empty when
     *         omitted
     */
    public Optional<Node> frxChild() {
        return Optional.ofNullable(frxChild);
    }

    /**
     * Returns the message children.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<Node> messageChildren() {
        return messageChildren;
    }

    /**
     * Returns the call children.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<Node> callChildren() {
        return callChildren;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutSpamGroupReportRequest",
            exports = "makeGroupReportRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutSpamEntitySubjectMixin (optional): smax("spam_list", {subject})
        // WASmaxOutSpamIsKnownChatMixin (optional): smax("spam_list", {is_known_chat})
        //   REPEATED_CHILD(message, 0, 210), REPEATED_CHILD(call, 0, 5))
        var spamListBuilder = new NodeBuilder()
                .description("spam_list")
                .attribute("jid", spamListJid)
                .attribute("spam_flow", spamListSpamFlow);
        if (spamListSource != null) {
            spamListBuilder.attribute("source", spamListSource);
        }
        if (spamListSubject != null) {
            spamListBuilder.attribute("subject", spamListSubject);
        }
        if (spamListIsKnownChat != null) {
            spamListBuilder.attribute("is_known_chat", spamListIsKnownChat);
        }
        var spamListChildren = new ArrayList<Node>(messageChildren.size() + callChildren.size());
        spamListChildren.addAll(messageChildren);
        spamListChildren.addAll(callChildren);
        if (!spamListChildren.isEmpty()) {
            spamListBuilder.content(spamListChildren);
        }
        var iqChildren = new ArrayList<Node>();
        iqChildren.add(spamListBuilder.build());
        if (frxChild != null) {
            iqChildren.add(frxChild);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "spam")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(iqChildren);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupReportRequest) obj;
        return Objects.equals(this.spamListJid, that.spamListJid)
                && Objects.equals(this.spamListSpamFlow, that.spamListSpamFlow)
                && Objects.equals(this.spamListSource, that.spamListSource)
                && Objects.equals(this.spamListSubject, that.spamListSubject)
                && Objects.equals(this.spamListIsKnownChat, that.spamListIsKnownChat)
                && Objects.equals(this.frxChild, that.frxChild)
                && Objects.equals(this.messageChildren, that.messageChildren)
                && Objects.equals(this.callChildren, that.callChildren);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spamListJid, spamListSpamFlow, spamListSource, spamListSubject,
                spamListIsKnownChat, frxChild, messageChildren, callChildren);
    }

    @Override
    public String toString() {
        return "SmaxGroupReportRequest[spamListJid=" + spamListJid
                + ", spamListSpamFlow=" + spamListSpamFlow
                + ", spamListSource=" + spamListSource
                + ", spamListSubject=" + spamListSubject
                + ", spamListIsKnownChat=" + spamListIsKnownChat
                + ", messageChildren=" + messageChildren.size()
                + ", callChildren=" + callChildren.size() + ']';
    }

    /**
     * Builder for {@link SmaxGroupReportRequest}. The canonical entry point for
     * assembling a group spam report.
     */
    public static final class Builder {
        /**
         * The group JID; required.
         */
        private Jid spamListJid;

        /**
         * The spam-flow string; required.
         */
        private String spamListSpamFlow;

        /**
         * The optional adder JID.
         */
        private Jid spamListSource;

        /**
         * The optional subject string.
         */
        private String spamListSubject;

        /**
         * The optional is-known-chat marker.
         */
        private String spamListIsKnownChat;

        /**
         * The optional FRX child node.
         */
        private Node frxChild;

        /**
         * The accumulated message children.
         */
        private final List<Node> messageChildren = new ArrayList<>();

        /**
         * The accumulated call children.
         */
        private final List<Node> callChildren = new ArrayList<>();

        /**
         * Constructs a new builder.
         */
        public Builder() {
        }

        /**
         * Sets the target group JID.
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
         * Sets the optional adder JID.
         *
         * @param spamListSource the JID; may be {@code null}
         * @return this builder
         */
        public Builder spamListSource(Jid spamListSource) {
            this.spamListSource = spamListSource;
            return this;
        }

        /**
         * Sets the optional subject string.
         *
         * @param spamListSubject the subject; may be {@code null}
         * @return this builder
         */
        public Builder spamListSubject(String spamListSubject) {
            this.spamListSubject = spamListSubject;
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
         * Sets the optional FRX (free-form reporting extensions)
         * child node.
         *
         * @param frxChild the FRX node; may be {@code null}
         * @return this builder
         */
        public Builder frxChild(Node frxChild) {
            this.frxChild = frxChild;
            return this;
        }

        /**
         * Appends a pre-built {@code <message>} child.
         *
         * @param messageNode the node; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code messageNode} is
         *                              {@code null}
         */
        public Builder addMessageChild(Node messageNode) {
            Objects.requireNonNull(messageNode, "messageNode cannot be null");
            messageChildren.add(messageNode);
            return this;
        }

        /**
         * Appends a pre-built {@code <call>} child.
         *
         * @param callNode the node; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code callNode} is
         *                              {@code null}
         */
        public Builder addCallChild(Node callNode) {
            Objects.requireNonNull(callNode, "callNode cannot be null");
            callChildren.add(callNode);
            return this;
        }

        /**
         * Builds the request.
         *
         * @return a new {@link SmaxGroupReportRequest}; never {@code null}
         * @throws NullPointerException if any required field is
         *                              missing
         */
        public SmaxGroupReportRequest build() {
            return new SmaxGroupReportRequest(this);
        }
    }
}
