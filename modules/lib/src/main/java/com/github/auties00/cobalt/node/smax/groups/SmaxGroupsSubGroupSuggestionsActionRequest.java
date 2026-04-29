package com.github.auties00.cobalt.node.smax.groups;

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
 * The outbound stanza variant — wraps the
 * {@code <sub_group_suggestions_action/>} payload in the canonical
 * {@code <iq xmlns="w:g2" type="set" to="<parent>">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsSubGroupSuggestionsActionRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsSubGroupSuggestionsActionRequest implements SmaxOperation.Request {
    /**
     * The parent (community) group JID. Routed verbatim into the IQ's
     * {@code to} attribute.
     */
    private final Jid parentGroupJid;

    /**
     * The optional list of suggestions to approve. Each entry must
     * carry both a {@code creator} and a {@code jid} attribute.
     */
    private final List<CreatorSuggestion> approve;

    /**
     * The optional list of suggestions to reject. Each entry must
     * carry both a {@code creator} and a {@code jid} attribute.
     */
    private final List<CreatorSuggestion> reject;

    /**
     * The optional list of suggestions to cancel. Each entry carries
     * only the {@code jid} attribute (the cancelling caller is
     * implicit — the relay enforces ownership server-side).
     */
    private final List<JidSuggestion> cancel;

    /**
     * Constructs a request.
     *
     * @param parentGroupJid the parent community JID; never
     *                       {@code null}
     * @param approve        the optional approve list; never
     *                       {@code null} (empty when omitted)
     * @param reject         the optional reject list; never
     *                       {@code null} (empty when omitted)
     * @param cancel         the optional cancel list; never
     *                       {@code null} (empty when omitted)
     * @throws NullPointerException     if any argument is
     *                                  {@code null}
     * @throws IllegalArgumentException when every list is empty
     */
    public SmaxGroupsSubGroupSuggestionsActionRequest(Jid parentGroupJid,
                   List<CreatorSuggestion> approve,
                   List<CreatorSuggestion> reject,
                   List<JidSuggestion> cancel) {
        Objects.requireNonNull(parentGroupJid, "parentGroupJid cannot be null");
        Objects.requireNonNull(approve, "approve cannot be null");
        Objects.requireNonNull(reject, "reject cannot be null");
        Objects.requireNonNull(cancel, "cancel cannot be null");
        if (approve.isEmpty() && reject.isEmpty() && cancel.isEmpty()) {
            throw new IllegalArgumentException("at least one of approve/reject/cancel must be non-empty");
        }
        this.parentGroupJid = parentGroupJid;
        this.approve = List.copyOf(approve);
        this.reject = List.copyOf(reject);
        this.cancel = List.copyOf(cancel);
    }

    /**
     * Returns the parent group JID.
     *
     * @return the parent group JID; never {@code null}
     */
    public Jid parentGroupJid() {
        return parentGroupJid;
    }

    /**
     * Returns the approve list.
     *
     * @return an unmodifiable list of suggestions to approve
     */
    public List<CreatorSuggestion> approve() {
        return approve;
    }

    /**
     * Returns the reject list.
     *
     * @return an unmodifiable list of suggestions to reject
     */
    public List<CreatorSuggestion> reject() {
        return reject;
    }

    /**
     * Returns the cancel list.
     *
     * @return an unmodifiable list of suggestions to cancel
     */
    public List<JidSuggestion> cancel() {
        return cancel;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <sub_group_suggestions_action/>} payload
     *
     * @implNote {@code WASmaxOutGroupsSubGroupSuggestionsActionRequest.makeSubGroupSuggestionsActionRequest}
     *           composes
     *           {@code WASmaxOutGroupsBaseSetGroupMixin} over a
     *           {@code <sub_group_suggestions_action/>} root with
     *           up to three optional children:
     *           {@code <approve/>}, {@code <reject/>},
     *           {@code <cancel/>}, each carrying 1..1000 repeated
     *           {@code <sub_group_suggestion/>} entries.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsSubGroupSuggestionsActionRequest",
            exports = "makeSubGroupSuggestionsActionRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var actionChildren = new ArrayList<Node>();
        if (!approve.isEmpty()) {
            var entries = new ArrayList<Node>();
            for (var entry : approve) {
                entries.add(entry.toNode());
            }
            var approveNode = new NodeBuilder()
                    .description("approve")
                    .content(entries)
                    .build();
            actionChildren.add(approveNode);
        }
        if (!reject.isEmpty()) {
            var entries = new ArrayList<Node>();
            for (var entry : reject) {
                entries.add(entry.toNode());
            }
            var rejectNode = new NodeBuilder()
                    .description("reject")
                    .content(entries)
                    .build();
            actionChildren.add(rejectNode);
        }
        if (!cancel.isEmpty()) {
            var entries = new ArrayList<Node>();
            for (var entry : cancel) {
                entries.add(entry.toNode());
            }
            var cancelNode = new NodeBuilder()
                    .description("cancel")
                    .content(entries)
                    .build();
            actionChildren.add(cancelNode);
        }
        var actionNode = new NodeBuilder()
                .description("sub_group_suggestions_action")
                .content(actionChildren)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", parentGroupJid)
                .attribute("type", "set")
                .content(actionNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsSubGroupSuggestionsActionRequest) obj;
        return Objects.equals(this.parentGroupJid, that.parentGroupJid)
                && Objects.equals(this.approve, that.approve)
                && Objects.equals(this.reject, that.reject)
                && Objects.equals(this.cancel, that.cancel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGroupJid, approve, reject, cancel);
    }

    @Override
    public String toString() {
        return "SmaxGroupsSubGroupSuggestionsActionRequest[parentGroupJid=" + parentGroupJid
                + ", approve=" + approve
                + ", reject=" + reject
                + ", cancel=" + cancel + ']';
    }

    /**
     * Suggestion entry that carries the {@code creator}+{@code jid}
     * pair (used by approve/reject lists in both the request and
     * the response).
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutGroupsSubGroupSuggestionMixin")
    public static final class CreatorSuggestion {
        /**
         * The user JID who created the suggestion.
         */
        private final Jid creator;

        /**
         * The proposed sub-group JID.
         */
        private final Jid jid;

        /**
         * The optional creator phone-number JID (echoed in
         * approve/reject responses; ignored on the request side).
         */
        private final Jid creatorPn;

        /**
         * Constructs a creator-suggestion entry.
         *
         * @param creator   the creator JID; never {@code null}
         * @param jid       the sub-group JID; never {@code null}
         * @param creatorPn the optional creator phone JID; may be
         *                  {@code null}
         * @throws NullPointerException if {@code creator} or
         *                              {@code jid} is {@code null}
         */
        public CreatorSuggestion(Jid creator, Jid jid, Jid creatorPn) {
            this.creator = Objects.requireNonNull(creator, "creator cannot be null");
            this.jid = Objects.requireNonNull(jid, "jid cannot be null");
            this.creatorPn = creatorPn;
        }

        /**
         * Returns the creator JID.
         *
         * @return the creator JID; never {@code null}
         */
        public Jid creator() {
            return creator;
        }

        /**
         * Returns the sub-group JID.
         *
         * @return the sub-group JID; never {@code null}
         */
        public Jid jid() {
            return jid;
        }

        /**
         * Returns the optional creator phone-number JID.
         *
         * @return an {@link Optional} carrying the creator phone
         *         JID, or empty when omitted
         */
        public Optional<Jid> creatorPn() {
            return Optional.ofNullable(creatorPn);
        }

        /**
         * Builds the {@code <sub_group_suggestion/>} child.
         *
         * @return the materialised {@link Node}
         */
        public Node toNode() {
            var builder = new NodeBuilder()
                    .description("sub_group_suggestion")
                    .attribute("creator", creator)
                    .attribute("jid", jid);
            if (creatorPn != null) {
                builder.attribute("creator_pn", creatorPn);
            }
            return builder.build();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (CreatorSuggestion) obj;
            return Objects.equals(this.creator, that.creator)
                    && Objects.equals(this.jid, that.jid)
                    && Objects.equals(this.creatorPn, that.creatorPn);
        }

        @Override
        public int hashCode() {
            return Objects.hash(creator, jid, creatorPn);
        }

        @Override
        public String toString() {
            return "SmaxGroupsSubGroupSuggestionsActionRequest.CreatorSuggestion[creator=" + creator
                    + ", jid=" + jid
                    + ", creatorPn=" + creatorPn + ']';
        }
    }

    /**
     * Suggestion entry that carries the {@code jid} only (used by
     * the cancel list in both the request and the response).
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutGroupsSubGroupSuggestionWithoutCreatorMixin")
    public static final class JidSuggestion {
        /**
         * The proposed sub-group JID to cancel.
         */
        private final Jid jid;

        /**
         * Constructs a jid-only suggestion entry.
         *
         * @param jid the sub-group JID; never {@code null}
         * @throws NullPointerException if {@code jid} is
         *                              {@code null}
         */
        public JidSuggestion(Jid jid) {
            this.jid = Objects.requireNonNull(jid, "jid cannot be null");
        }

        /**
         * Returns the sub-group JID.
         *
         * @return the sub-group JID; never {@code null}
         */
        public Jid jid() {
            return jid;
        }

        /**
         * Builds the {@code <sub_group_suggestion jid/>} child.
         *
         * @return the materialised {@link Node}
         */
        public Node toNode() {
            return new NodeBuilder()
                    .description("sub_group_suggestion")
                    .attribute("jid", jid)
                    .build();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (JidSuggestion) obj;
            return Objects.equals(this.jid, that.jid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid);
        }

        @Override
        public String toString() {
            return "SmaxGroupsSubGroupSuggestionsActionRequest.JidSuggestion[jid=" + jid + ']';
        }
    }
}
