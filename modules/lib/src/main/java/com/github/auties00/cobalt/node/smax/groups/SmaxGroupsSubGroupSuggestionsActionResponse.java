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
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGroupsSubGroupSuggestionsActionRequest}.
 */
public sealed interface SmaxGroupsSubGroupSuggestionsActionResponse extends SmaxOperation.Response
        permits SmaxGroupsSubGroupSuggestionsActionResponse.Success, SmaxGroupsSubGroupSuggestionsActionResponse.ClientError, SmaxGroupsSubGroupSuggestionsActionResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsSubGroupSuggestionsActionResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to
     *                validate echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsSubGroupSuggestionsActionRPC",
            exports = "sendSubGroupSuggestionsActionRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsSubGroupSuggestionsActionResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant — the relay processed every
     * sub-action and returned per-suggestion echo rows.
     *
     * <p>Each echo row is shaped after the corresponding request
     * entry: approve rows carry the
     * {@code (creator, jid, creator_pn?)} triple plus an optional
     * approval-error discriminator; reject and cancel rows
     * additionally carry an optional identity-mixin tag and an
     * optional not-found marker.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsSubGroupSuggestionsActionResponseSuccess")
    final class Success implements SmaxGroupsSubGroupSuggestionsActionResponse {
        /**
         * The approve sub-action echo (empty when the request did
         * not include an approve list).
         */
        private final List<ApprovedSuggestion> approve;

        /**
         * The reject sub-action echo (empty when the request did
         * not include a reject list).
         */
        private final List<RejectedSuggestion> reject;

        /**
         * The cancel sub-action echo (empty when the request did
         * not include a cancel list).
         */
        private final List<CancelledSuggestion> cancel;

        /**
         * Constructs a success reply.
         *
         * @param approve the approve echo rows; never {@code null}
         * @param reject  the reject echo rows; never {@code null}
         * @param cancel  the cancel echo rows; never {@code null}
         * @throws NullPointerException if any argument is
         *                              {@code null}
         */
        public Success(List<ApprovedSuggestion> approve,
                       List<RejectedSuggestion> reject,
                       List<CancelledSuggestion> cancel) {
            Objects.requireNonNull(approve, "approve cannot be null");
            Objects.requireNonNull(reject, "reject cannot be null");
            Objects.requireNonNull(cancel, "cancel cannot be null");
            this.approve = List.copyOf(approve);
            this.reject = List.copyOf(reject);
            this.cancel = List.copyOf(cancel);
        }

        /**
         * Returns the approve echo rows.
         *
         * @return an unmodifiable list
         */
        public List<ApprovedSuggestion> approve() {
            return approve;
        }

        /**
         * Returns the reject echo rows.
         *
         * @return an unmodifiable list
         */
        public List<RejectedSuggestion> reject() {
            return reject;
        }

        /**
         * Returns the cancel echo rows.
         *
         * @return an unmodifiable list
         */
        public List<CancelledSuggestion> cancel() {
            return cancel;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the success
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsSubGroupSuggestionsActionResponseSuccess",
                exports = "parseSubGroupSuggestionsActionResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var actionNode = node.getChild("sub_group_suggestions_action").orElse(null);
            if (actionNode == null) {
                return Optional.empty();
            }
            var approveList = new ArrayList<ApprovedSuggestion>();
            var approveSection = actionNode.getChild("approve").orElse(null);
            if (approveSection != null) {
                for (var suggestion : approveSection.getChildren("sub_group_suggestion")) {
                    var parsed = ApprovedSuggestion.of(suggestion).orElse(null);
                    if (parsed == null) {
                        return Optional.empty();
                    }
                    approveList.add(parsed);
                }
            }
            var rejectList = new ArrayList<RejectedSuggestion>();
            var rejectSection = actionNode.getChild("reject").orElse(null);
            if (rejectSection != null) {
                for (var suggestion : rejectSection.getChildren("sub_group_suggestion")) {
                    var parsed = RejectedSuggestion.of(suggestion).orElse(null);
                    if (parsed == null) {
                        return Optional.empty();
                    }
                    rejectList.add(parsed);
                }
            }
            var cancelList = new ArrayList<CancelledSuggestion>();
            var cancelSection = actionNode.getChild("cancel").orElse(null);
            if (cancelSection != null) {
                for (var suggestion : cancelSection.getChildren("sub_group_suggestion")) {
                    var parsed = CancelledSuggestion.of(suggestion).orElse(null);
                    if (parsed == null) {
                        return Optional.empty();
                    }
                    cancelList.add(parsed);
                }
            }
            return Optional.of(new Success(approveList, rejectList, cancelList));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Success) obj;
            return Objects.equals(this.approve, that.approve)
                    && Objects.equals(this.reject, that.reject)
                    && Objects.equals(this.cancel, that.cancel);
        }

        @Override
        public int hashCode() {
            return Objects.hash(approve, reject, cancel);
        }

        @Override
        public String toString() {
            return "SmaxGroupsSubGroupSuggestionsActionResponse.Success[approve=" + approve
                    + ", reject=" + reject
                    + ", cancel=" + cancel + ']';
        }

        /**
         * Approve-list echo row — carries the
         * {@code (creator, jid, creator_pn?)} triple plus an optional
         * approval-error discriminator surfaced when the relay
         * could not commit the approval.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsSubGroupSuggestionMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsSubGroupSuggestionsApprovalErrors")
        public static final class ApprovedSuggestion {
            /**
             * Returns whether {@code description} matches one of the
             * documented approval-error discriminator tags.
             *
             * @param description the child tag to test
             * @return {@code true} when the tag is recognised
             */
            private static boolean isApprovalErrorTag(String description) {
                return "sub_group_creation_internal_server_error".equals(description)
                        || "pending_group_adds_error".equals(description)
                        || "resource_constraint".equals(description)
                        || "suggestion_conflict".equals(description)
                        || "suggestion_not_found".equals(description);
            }

            /**
             * The creator JID echoed by the relay.
             */
            private final Jid creator;

            /**
             * The sub-group JID echoed by the relay.
             */
            private final Jid jid;

            /**
             * The optional creator phone-number JID echoed by the
             * relay.
             */
            private final Jid creatorPn;

            /**
             * The optional approval-error discriminator tag (one of
             * {@code "sub_group_creation_internal_server_error"},
             * {@code "pending_group_adds_error"},
             * {@code "resource_constraint"},
             * {@code "suggestion_conflict"},
             * {@code "suggestion_not_found"}). {@code null} when
             * the approval committed cleanly.
             */
            private final String approvalErrorTag;

            /**
             * Constructs an approved-suggestion echo row.
             *
             * @param creator          the creator JID; never
             *                         {@code null}
             * @param jid              the sub-group JID; never
             *                         {@code null}
             * @param creatorPn        the optional creator phone
             *                         JID; may be {@code null}
             * @param approvalErrorTag the optional approval-error
             *                         tag; may be {@code null}
             * @throws NullPointerException if {@code creator} or
             *                              {@code jid} is
             *                              {@code null}
             */
            public ApprovedSuggestion(Jid creator, Jid jid, Jid creatorPn, String approvalErrorTag) {
                this.creator = Objects.requireNonNull(creator, "creator cannot be null");
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.creatorPn = creatorPn;
                this.approvalErrorTag = approvalErrorTag;
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
             * Returns the optional approval-error tag.
             *
             * @return an {@link Optional} carrying the tag, or empty
             *         when the approval committed cleanly
             */
            public Optional<String> approvalErrorTag() {
                return Optional.ofNullable(approvalErrorTag);
            }

            /**
             * Tries to parse an approved-suggestion row from the
             * supplied {@code <sub_group_suggestion/>} node.
             *
             * @param suggestion the suggestion node
             * @return an {@link Optional} carrying the parsed row,
             *         or empty when the node is malformed
             */
            static Optional<ApprovedSuggestion> of(Node suggestion) {
                var creator = suggestion.getAttributeAsJid("creator").orElse(null);
                if (creator == null) {
                    return Optional.empty();
                }
                var jid = suggestion.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var creatorPn = suggestion.getAttributeAsJid("creator_pn").orElse(null);
                String approvalErrorTag = null;
                for (var child : suggestion.children()) {
                    var description = child.description();
                    if (description == null) {
                        continue;
                    }
                    if (isApprovalErrorTag(description)) {
                        approvalErrorTag = description;
                        break;
                    }
                }
                return Optional.of(new ApprovedSuggestion(creator, jid, creatorPn, approvalErrorTag));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (ApprovedSuggestion) obj;
                return Objects.equals(this.creator, that.creator)
                        && Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.creatorPn, that.creatorPn)
                        && Objects.equals(this.approvalErrorTag, that.approvalErrorTag);
            }

            @Override
            public int hashCode() {
                return Objects.hash(creator, jid, creatorPn, approvalErrorTag);
            }

            @Override
            public String toString() {
                return "SmaxGroupsSubGroupSuggestionsActionResponse.Success.ApprovedSuggestion[creator=" + creator
                        + ", jid=" + jid
                        + ", creatorPn=" + creatorPn
                        + ", approvalErrorTag=" + approvalErrorTag + ']';
            }
        }

        /**
         * Reject-list echo row — same shape as
         * {@link ApprovedSuggestion} but with an optional identity
         * tag and an optional not-found marker instead of the
         * approval-error discriminator.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsSubGroupSuggestionMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsIdentityMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsSubGroupSuggestionsActionSubGroupSuggestionNotFoundMixin")
        public static final class RejectedSuggestion {
            /**
             * The creator JID echoed by the relay.
             */
            private final Jid creator;

            /**
             * The sub-group JID echoed by the relay.
             */
            private final Jid jid;

            /**
             * The optional creator phone-number JID echoed by the
             * relay.
             */
            private final Jid creatorPn;

            /**
             * The optional identity-mixin discriminator tag echoed
             * by the relay (raw string; the WA Web parser routes it
             * through {@code WASmaxInGroupsIdentityTypes}).
             */
            private final String identityTag;

            /**
             * Whether the relay marked the suggestion as not-found
             * (i.e. carried an inner
             * {@code <suggestion_not_found/>} child).
             */
            private final boolean notFound;

            /**
             * Constructs a rejected-suggestion echo row.
             *
             * @param creator     the creator JID; never {@code null}
             * @param jid         the sub-group JID; never
             *                    {@code null}
             * @param creatorPn   the optional creator phone JID; may
             *                    be {@code null}
             * @param identityTag the optional identity tag; may be
             *                    {@code null}
             * @param notFound    whether the not-found marker is
             *                    present
             * @throws NullPointerException if {@code creator} or
             *                              {@code jid} is
             *                              {@code null}
             */
            public RejectedSuggestion(Jid creator, Jid jid, Jid creatorPn, String identityTag, boolean notFound) {
                this.creator = Objects.requireNonNull(creator, "creator cannot be null");
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.creatorPn = creatorPn;
                this.identityTag = identityTag;
                this.notFound = notFound;
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
             * Returns the optional identity-mixin discriminator tag.
             *
             * @return an {@link Optional} carrying the tag, or empty
             *         when omitted
             */
            public Optional<String> identityTag() {
                return Optional.ofNullable(identityTag);
            }

            /**
             * Returns whether the not-found marker is present.
             *
             * @return {@code true} when the relay surfaced the
             *         not-found marker
             */
            public boolean notFound() {
                return notFound;
            }

            /**
             * Tries to parse a rejected-suggestion row from the
             * supplied {@code <sub_group_suggestion/>} node.
             *
             * @param suggestion the suggestion node
             * @return an {@link Optional} carrying the parsed row,
             *         or empty when the node is malformed
             */
            static Optional<RejectedSuggestion> of(Node suggestion) {
                var creator = suggestion.getAttributeAsJid("creator").orElse(null);
                if (creator == null) {
                    return Optional.empty();
                }
                var jid = suggestion.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var creatorPn = suggestion.getAttributeAsJid("creator_pn").orElse(null);
                var notFound = suggestion.getChild("suggestion_not_found").isPresent();
                String identityTag = null;
                for (var child : suggestion.children()) {
                    var description = child.description();
                    if (description == null) {
                        continue;
                    }
                    if ("suggestion_not_found".equals(description)) {
                        continue;
                    }
                    identityTag = description;
                    break;
                }
                return Optional.of(new RejectedSuggestion(creator, jid, creatorPn, identityTag, notFound));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (RejectedSuggestion) obj;
                return this.notFound == that.notFound
                        && Objects.equals(this.creator, that.creator)
                        && Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.creatorPn, that.creatorPn)
                        && Objects.equals(this.identityTag, that.identityTag);
            }

            @Override
            public int hashCode() {
                return Objects.hash(creator, jid, creatorPn, identityTag, notFound);
            }

            @Override
            public String toString() {
                return "SmaxGroupsSubGroupSuggestionsActionResponse.Success.RejectedSuggestion[creator=" + creator
                        + ", jid=" + jid
                        + ", creatorPn=" + creatorPn
                        + ", identityTag=" + identityTag
                        + ", notFound=" + notFound + ']';
            }
        }

        /**
         * Cancel-list echo row — only carries the {@code jid} plus
         * optional identity-mixin tag and not-found marker (no
         * creator attribute, by parity with the request side).
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsSubGroupSuggestionWithoutCreatorMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsIdentityMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsSubGroupSuggestionsActionSubGroupSuggestionNotFoundMixin")
        public static final class CancelledSuggestion {
            /**
             * The sub-group JID echoed by the relay.
             */
            private final Jid jid;

            /**
             * The optional identity-mixin discriminator tag echoed
             * by the relay.
             */
            private final String identityTag;

            /**
             * Whether the relay marked the suggestion as not-found.
             */
            private final boolean notFound;

            /**
             * Constructs a cancelled-suggestion echo row.
             *
             * @param jid         the sub-group JID; never
             *                    {@code null}
             * @param identityTag the optional identity tag; may be
             *                    {@code null}
             * @param notFound    whether the not-found marker is
             *                    present
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public CancelledSuggestion(Jid jid, String identityTag, boolean notFound) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.identityTag = identityTag;
                this.notFound = notFound;
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
             * Returns the optional identity-mixin discriminator tag.
             *
             * @return an {@link Optional} carrying the tag, or empty
             *         when omitted
             */
            public Optional<String> identityTag() {
                return Optional.ofNullable(identityTag);
            }

            /**
             * Returns whether the not-found marker is present.
             *
             * @return {@code true} when the relay surfaced the
             *         not-found marker
             */
            public boolean notFound() {
                return notFound;
            }

            /**
             * Tries to parse a cancelled-suggestion row from the
             * supplied {@code <sub_group_suggestion/>} node.
             *
             * @param suggestion the suggestion node
             * @return an {@link Optional} carrying the parsed row,
             *         or empty when the node is malformed
             */
            static Optional<CancelledSuggestion> of(Node suggestion) {
                var jid = suggestion.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var notFound = suggestion.getChild("suggestion_not_found").isPresent();
                String identityTag = null;
                for (var child : suggestion.children()) {
                    var description = child.description();
                    if (description == null) {
                        continue;
                    }
                    if ("suggestion_not_found".equals(description)) {
                        continue;
                    }
                    identityTag = description;
                    break;
                }
                return Optional.of(new CancelledSuggestion(jid, identityTag, notFound));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (CancelledSuggestion) obj;
                return this.notFound == that.notFound
                        && Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.identityTag, that.identityTag);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, identityTag, notFound);
            }

            @Override
            public String toString() {
                return "SmaxGroupsSubGroupSuggestionsActionResponse.Success.CancelledSuggestion[jid=" + jid
                        + ", identityTag=" + identityTag
                        + ", notFound=" + notFound + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, or referencing
     * non-existent suggestions.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsSubGroupSuggestionsActionResponseClientError")
    final class ClientError implements SmaxGroupsSubGroupSuggestionsActionResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ClientError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsSubGroupSuggestionsActionResponseClientError",
                exports = "parseSubGroupSuggestionsActionResponseClientError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ClientError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ClientError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGroupsSubGroupSuggestionsActionResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsSubGroupSuggestionsActionResponseServerError")
    final class ServerError implements SmaxGroupsSubGroupSuggestionsActionResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ServerError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsSubGroupSuggestionsActionResponseServerError",
                exports = "parseSubGroupSuggestionsActionResponseServerError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ServerError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ServerError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGroupsSubGroupSuggestionsActionResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
