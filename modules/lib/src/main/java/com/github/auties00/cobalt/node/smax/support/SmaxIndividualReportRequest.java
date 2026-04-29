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
 */
@WhatsAppWebModule(moduleName = "WASmaxOutSpamIndividualReportRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBaseReportMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamFRXMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamIsKnownChatMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBizOptOutMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamBizReportMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamUIStateSetMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutSpamTCTokenMixin")
public final class SmaxIndividualReportRequest implements SmaxOperation.Request {
    /**
     * The optional reportee JID — when supplied, routed into the
     * {@code <spam_list jid>} attribute. The {@code jid} attribute
     * is optional per the WA Web schema (some flows synthesise the
     * subject from the message attribution alone).
     */
    private final Jid spamListJid;

    /**
     * The spam-flow string surfacing the user-visible report flow.
     */
    private final String spamListSpamFlow;

    /**
     * The optional {@code is_known_chat} marker.
     */
    private final String spamListIsKnownChat;

    /**
     * The optional biz-opt-out child node (pre-built).
     */
    private final Node bizOptOutChild;

    /**
     * The optional ui-state-set child node (pre-built).
     */
    private final Node uistateSetChild;

    /**
     * The optional biz-report child node (pre-built).
     */
    private final Node bizReportChild;

    /**
     * The optional TC-token child node (pre-built).
     */
    private final Node tcTokenChild;

    /**
     * The optional FRX (free-form reporting extensions) child node
     * (pre-built).
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
     * The pre-built {@code <user_initiated_extension>} children
     * (0..5).
     */
    private final List<Node> userInitiatedExtensionChildren;

    /**
     * Constructs a request from the {@link Builder}.
     *
     * @param builder the source builder; never {@code null}
     */
    private SmaxIndividualReportRequest(Builder builder) {
        this.spamListJid = builder.spamListJid;
        this.spamListSpamFlow = Objects.requireNonNull(builder.spamListSpamFlow,
                "spamListSpamFlow cannot be null");
        this.spamListIsKnownChat = builder.spamListIsKnownChat;
        this.bizOptOutChild = builder.bizOptOutChild;
        this.uistateSetChild = builder.uistateSetChild;
        this.bizReportChild = builder.bizReportChild;
        this.tcTokenChild = builder.tcTokenChild;
        this.frxChild = builder.frxChild;
        this.messageChildren = List.copyOf(builder.messageChildren);
        this.callChildren = List.copyOf(builder.callChildren);
        this.userInitiatedExtensionChildren = List.copyOf(builder.userInitiatedExtensionChildren);
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
     * Returns the optional reportee JID.
     *
     * @return an {@link Optional} carrying the JID, or empty when
     *         omitted
     */
    public Optional<Jid> spamListJid() {
        return Optional.ofNullable(spamListJid);
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
     * Returns the optional ui-state-set child.
     *
     * @return an {@link Optional} carrying the node, or empty when
     *         omitted
     */
    public Optional<Node> uistateSetChild() {
        return Optional.ofNullable(uistateSetChild);
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
     * Returns the optional TC-token child.
     *
     * @return an {@link Optional} carrying the node, or empty when
     *         omitted
     */
    public Optional<Node> tcTokenChild() {
        return Optional.ofNullable(tcTokenChild);
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
     * Returns the user-initiated-extension children.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<Node> userInitiatedExtensionChildren() {
        return userInitiatedExtensionChildren;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         payload
     *
     * @implNote {@code WASmaxOutSpamIndividualReportRequest.makeIndividualReportRequest}
     *           composes
     *           {@code WASmaxOutSpamBaseIQSetRequestMixin} and
     *           {@code WASmaxOutSpamBaseReportMixin} over a
     *           {@code <spam_list jid?>} payload, optionally
     *           wrapping the spam-list in a chain of
     *           {@code WASmaxOutSpamBizOptOutMixin},
     *           {@code WASmaxOutSpamUIStateSetMixin},
     *           {@code WASmaxOutSpamBizReportMixin}, and
     *           {@code WASmaxOutSpamTCTokenMixin} (each pulled in
     *           via {@code optionalMerge}).
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutSpamIndividualReportRequest",
            exports = "makeIndividualReportRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutSpamBaseReportMixin: smax("spam_list", {spam_flow})
        // WASmaxOutSpamIsKnownChatMixin (optional): smax("spam_list", {is_known_chat})
        // WASmaxOutSpamIndividualReportRequest: smax("spam_list", {jid?},
        //   REPEATED_CHILD(message, 0, 210),
        //   REPEATED_CHILD(call, 0, 5),
        //   REPEATED_CHILD(user_initiated_extension, 0, 5))
        var spamListBuilder = new NodeBuilder()
                .description("spam_list")
                .attribute("spam_flow", spamListSpamFlow);
        if (spamListJid != null) {
            spamListBuilder.attribute("jid", spamListJid);
        }
        if (spamListIsKnownChat != null) {
            spamListBuilder.attribute("is_known_chat", spamListIsKnownChat);
        }
        var spamListChildren = new ArrayList<Node>(
                messageChildren.size() + callChildren.size() + userInitiatedExtensionChildren.size());
        spamListChildren.addAll(messageChildren);
        spamListChildren.addAll(callChildren);
        spamListChildren.addAll(userInitiatedExtensionChildren);
        // The biz-opt-out / biz-report / ui-state-set children attach to <spam_list> itself
        // per WA Web's optional-merge wiring of those mixins.
        if (bizOptOutChild != null) {
            spamListChildren.add(bizOptOutChild);
        }
        if (uistateSetChild != null) {
            spamListChildren.add(uistateSetChild);
        }
        if (bizReportChild != null) {
            spamListChildren.add(bizReportChild);
        }
        if (tcTokenChild != null) {
            spamListChildren.add(tcTokenChild);
        }
        if (!spamListChildren.isEmpty()) {
            spamListBuilder.content(spamListChildren);
        }
        // WASmaxOutSpamBaseIQSetRequestMixin: smax("iq", {id: generateId(), type: "set"})
        // WASmaxOutSpamBaseReportMixin: smax("iq", {to: S_WHATSAPP_NET, xmlns: "spam"})
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
        var that = (SmaxIndividualReportRequest) obj;
        return Objects.equals(this.spamListJid, that.spamListJid)
                && Objects.equals(this.spamListSpamFlow, that.spamListSpamFlow)
                && Objects.equals(this.spamListIsKnownChat, that.spamListIsKnownChat)
                && Objects.equals(this.bizOptOutChild, that.bizOptOutChild)
                && Objects.equals(this.uistateSetChild, that.uistateSetChild)
                && Objects.equals(this.bizReportChild, that.bizReportChild)
                && Objects.equals(this.tcTokenChild, that.tcTokenChild)
                && Objects.equals(this.frxChild, that.frxChild)
                && Objects.equals(this.messageChildren, that.messageChildren)
                && Objects.equals(this.callChildren, that.callChildren)
                && Objects.equals(this.userInitiatedExtensionChildren, that.userInitiatedExtensionChildren);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spamListJid, spamListSpamFlow, spamListIsKnownChat,
                bizOptOutChild, uistateSetChild, bizReportChild, tcTokenChild, frxChild,
                messageChildren, callChildren, userInitiatedExtensionChildren);
    }

    @Override
    public String toString() {
        return "SmaxIndividualReportRequest[spamListJid=" + spamListJid
                + ", spamListSpamFlow=" + spamListSpamFlow
                + ", spamListIsKnownChat=" + spamListIsKnownChat
                + ", messageChildren=" + messageChildren.size()
                + ", callChildren=" + callChildren.size()
                + ", userInitiatedExtensionChildren=" + userInitiatedExtensionChildren.size() + ']';
    }

    /**
     * Builder for {@link SmaxIndividualReportRequest} — the canonical entry point for
     * assembling an individual spam report.
     */
    public static final class Builder {
        /**
         * The optional reportee JID.
         */
        private Jid spamListJid;

        /**
         * The spam-flow string; required.
         */
        private String spamListSpamFlow;

        /**
         * The optional is-known-chat marker.
         */
        private String spamListIsKnownChat;

        /**
         * The optional biz-opt-out child.
         */
        private Node bizOptOutChild;

        /**
         * The optional ui-state-set child.
         */
        private Node uistateSetChild;

        /**
         * The optional biz-report child.
         */
        private Node bizReportChild;

        /**
         * The optional TC-token child.
         */
        private Node tcTokenChild;

        /**
         * The optional FRX child.
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
         * The accumulated user-initiated-extension children.
         */
        private final List<Node> userInitiatedExtensionChildren = new ArrayList<>();

        /**
         * Constructs a new builder.
         */
        public Builder() {
        }

        /**
         * Sets the optional reportee JID.
         *
         * @param spamListJid the JID; may be {@code null}
         * @return this builder
         */
        public Builder spamListJid(Jid spamListJid) {
            this.spamListJid = spamListJid;
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
         * Sets the optional ui-state-set child node.
         *
         * @param uistateSetChild the node; may be {@code null}
         * @return this builder
         */
        public Builder uistateSetChild(Node uistateSetChild) {
            this.uistateSetChild = uistateSetChild;
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
         * Sets the optional TC-token child node.
         *
         * @param tcTokenChild the node; may be {@code null}
         * @return this builder
         */
        public Builder tcTokenChild(Node tcTokenChild) {
            this.tcTokenChild = tcTokenChild;
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
         * Appends a pre-built
         * {@code <user_initiated_extension>} child.
         *
         * @param node the node; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code node} is
         *                              {@code null}
         */
        public Builder addUserInitiatedExtensionChild(Node node) {
            Objects.requireNonNull(node, "node cannot be null");
            userInitiatedExtensionChildren.add(node);
            return this;
        }

        /**
         * Builds the request.
         *
         * @return a new {@link SmaxIndividualReportRequest}; never {@code null}
         * @throws NullPointerException if {@code spamListSpamFlow}
         *                              was not set
         */
        public SmaxIndividualReportRequest build() {
            return new SmaxIndividualReportRequest(this);
        }
    }
}
