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
 * Sealed family of inbound reply variants.
 */
public sealed interface SmaxGroupsGetMembershipApprovalRequestsResponse extends SmaxOperation.Response
        permits SmaxGroupsGetMembershipApprovalRequestsResponse.Success, SmaxGroupsGetMembershipApprovalRequestsResponse.ClientError, SmaxGroupsGetMembershipApprovalRequestsResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsGetMembershipApprovalRequestsResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsGetMembershipApprovalRequestsRPC",
            exports = "sendGetMembershipApprovalRequestsRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsGetMembershipApprovalRequestsResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — carries the list of
     * pending {@link Approval} entries plus the echoed
     * {@link #requestorFetch} flag (which is non-{@code null} only
     * when the caller asked for the rich projection).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetMembershipApprovalRequestsResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetMembershipApprovalRequestsRequestorFetchMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupAddressingModeMixin")
    final class Success implements SmaxGroupsGetMembershipApprovalRequestsResponse {
        /**
         * The list of pending membership-approval requests.
         */
        private final List<Approval> approvals;

        /**
         * Whether the relay echoed the {@code requestor_fetch="true"}
         * flag — populated only when the wrapper carried it.
         */
        private final boolean requestorFetch;

        /**
         * Constructs a new successful reply.
         *
         * @param approvals      the per-approval entries; never
         *                       {@code null}
         * @param requestorFetch whether the rich requestor
         *                       projection was returned
         * @throws NullPointerException if {@code approvals} is
         *                              {@code null}
         */
        public Success(List<Approval> approvals, boolean requestorFetch) {
            Objects.requireNonNull(approvals, "approvals cannot be null");
            this.approvals = List.copyOf(approvals);
            this.requestorFetch = requestorFetch;
        }

        /**
         * Returns the list of pending approval entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<Approval> approvals() {
            return approvals;
        }

        /**
         * Returns whether the rich requestor projection was
         * returned.
         *
         * @return {@code true} when the relay echoed
         *         {@code requestor_fetch="true"} on the wrapper
         */
        public boolean requestorFetch() {
            return requestorFetch;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetMembershipApprovalRequestsResponseSuccess",
                exports = "parseGetMembershipApprovalRequestsResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var wrapper = node.getChild("membership_approval_requests").orElse(null);
            if (wrapper == null) {
                return Optional.empty();
            }
            var requestorFetch = wrapper.hasAttribute("requestor_fetch", "true");
            var approvalNodes = wrapper.getChildren("membership_approval_request");
            var approvals = new ArrayList<Approval>(approvalNodes.size());
            for (var approvalNode : approvalNodes) {
                var approval = Approval.of(approvalNode).orElse(null);
                if (approval == null) {
                    return Optional.empty();
                }
                approvals.add(approval);
            }
            return Optional.of(new Success(approvals, requestorFetch));
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
            return this.requestorFetch == that.requestorFetch
                    && Objects.equals(this.approvals, that.approvals);
        }

        @Override
        public int hashCode() {
            return Objects.hash(approvals, requestorFetch);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetMembershipApprovalRequestsResponse.Success[approvals="
                    + approvals + ", requestorFetch=" + requestorFetch + ']';
        }
    }

    /**
     * Per-approval projection — carries the requesting user's JID
     * alongside the optional resolved
     * {@code requestor}/{@code requestor_pn}/{@code requestor_username}
     * attributes (populated only when the caller asked for the rich
     * projection), the request timestamp, and the optional request
     * method enum (one of {@code "InviteLink"},
     * {@code "LinkedGroupJoin"}, {@code "NonAdminAdd"}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetMembershipApprovalRequestsMembershipApprovalRequestMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsMembershipRequestMethodAttributeMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsIdentityMixin")
    final class Approval {
        /**
         * The requesting user's primary JID.
         */
        private final Jid jid;

        /**
         * The optional resolved {@code requestor} JID — set on the
         * rich projection when the request originates from a
         * different identity (community sub-group join requests).
         */
        private final Jid requestor;

        /**
         * The optional resolved {@code requestor_pn} JID — the
         * requestor's phone-number JID surfaced when the relay can
         * map LID to PN.
         */
        private final Jid requestorPn;

        /**
         * The optional resolved {@code requestor_username} —
         * surfaced when the requestor has a username.
         */
        private final String requestorUsername;

        /**
         * The optional parent-community JID — set on the rich
         * projection for community-link join requests.
         */
        private final Jid parentGroupJid;

        /**
         * The unix-time timestamp at which the request was filed.
         */
        private final long requestTime;

        /**
         * The optional request method enum
         * ({@code "InviteLink"}/{@code "LinkedGroupJoin"}/
         * {@code "NonAdminAdd"}).
         */
        private final String requestMethod;

        /**
         * Constructs an approval entry.
         *
         * @param jid               the requesting user's JID; never
         *                          {@code null}
         * @param requestor         optional resolved requestor JID
         * @param requestorPn       optional resolved requestor PN
         * @param requestorUsername optional resolved requestor
         *                          username
         * @param parentGroupJid    optional parent-community JID
         * @param requestTime       the request timestamp
         * @param requestMethod     optional request method enum
         * @throws NullPointerException     if {@code jid} is
         *                                  {@code null}
         * @throws IllegalArgumentException if {@code requestTime}
         *                                  is negative
         */
        public Approval(Jid jid, Jid requestor, Jid requestorPn, String requestorUsername,
                        Jid parentGroupJid, long requestTime, String requestMethod) {
            this.jid = Objects.requireNonNull(jid, "jid cannot be null");
            if (requestTime < 0) {
                throw new IllegalArgumentException("requestTime must be non-negative");
            }
            this.requestor = requestor;
            this.requestorPn = requestorPn;
            this.requestorUsername = requestorUsername;
            this.parentGroupJid = parentGroupJid;
            this.requestTime = requestTime;
            this.requestMethod = requestMethod;
        }

        /**
         * Returns the requesting user's primary JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid jid() {
            return jid;
        }

        /**
         * Returns the resolved requestor JID when supplied.
         *
         * @return an {@link Optional} carrying the requestor JID
         */
        public Optional<Jid> requestor() {
            return Optional.ofNullable(requestor);
        }

        /**
         * Returns the resolved requestor PN JID when supplied.
         *
         * @return an {@link Optional} carrying the JID
         */
        public Optional<Jid> requestorPn() {
            return Optional.ofNullable(requestorPn);
        }

        /**
         * Returns the resolved requestor username when supplied.
         *
         * @return an {@link Optional} carrying the username
         */
        public Optional<String> requestorUsername() {
            return Optional.ofNullable(requestorUsername);
        }

        /**
         * Returns the parent-community JID when supplied.
         *
         * @return an {@link Optional} carrying the JID
         */
        public Optional<Jid> parentGroupJid() {
            return Optional.ofNullable(parentGroupJid);
        }

        /**
         * Returns the request timestamp.
         *
         * @return the timestamp in unix-seconds
         */
        public long requestTime() {
            return requestTime;
        }

        /**
         * Returns the request method enum when supplied.
         *
         * @return an {@link Optional} carrying the enum token
         */
        public Optional<String> requestMethod() {
            return Optional.ofNullable(requestMethod);
        }

        /**
         * Tries to parse an {@link Approval} from the given
         * {@code <membership_approval_request/>} child.
         *
         * @param node the child node
         * @return an {@link Optional} carrying the parsed approval,
         *         or empty when the child does not match the
         *         schema
         */
        public static Optional<Approval> of(Node node) {
            Objects.requireNonNull(node, "node cannot be null");
            if (!node.hasDescription("membership_approval_request")) {
                return Optional.empty();
            }
            var jid = node.getAttributeAsJid("jid").orElse(null);
            if (jid == null) {
                return Optional.empty();
            }
            var requestTimeOptional = node.getAttributeAsLong("request_time");
            if (requestTimeOptional.isEmpty()) {
                return Optional.empty();
            }
            var requestor = node.getAttributeAsJid("requestor").orElse(null);
            var requestorPn = node.getAttributeAsJid("requestor_pn").orElse(null);
            var requestorUsername = node.getAttributeAsString("requestor_username").orElse(null);
            var parentGroupJid = node.getAttributeAsJid("parent_group_jid").orElse(null);
            var requestMethod = node.getAttributeAsString("request_method").orElse(null);
            var approval = new Approval(jid, requestor, requestorPn, requestorUsername,
                    parentGroupJid, requestTimeOptional.getAsLong(), requestMethod);
            return Optional.of(approval);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Approval) obj;
            return this.requestTime == that.requestTime
                    && Objects.equals(this.jid, that.jid)
                    && Objects.equals(this.requestor, that.requestor)
                    && Objects.equals(this.requestorPn, that.requestorPn)
                    && Objects.equals(this.requestorUsername, that.requestorUsername)
                    && Objects.equals(this.parentGroupJid, that.parentGroupJid)
                    && Objects.equals(this.requestMethod, that.requestMethod);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid, requestor, requestorPn, requestorUsername,
                    parentGroupJid, requestTime, requestMethod);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetMembershipApprovalRequestsResponse.Approval[jid=" + jid
                    + ", requestor=" + requestor
                    + ", requestorPn=" + requestorPn
                    + ", requestorUsername=" + requestorUsername
                    + ", parentGroupJid=" + parentGroupJid
                    + ", requestTime=" + requestTime
                    + ", requestMethod=" + requestMethod + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetMembershipApprovalRequestsResponseClientError")
    final class ClientError implements SmaxGroupsGetMembershipApprovalRequestsResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied
         * one.
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
         * @return an {@link Optional} carrying the error text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetMembershipApprovalRequestsResponseClientError",
                exports = "parseGetMembershipApprovalRequestsResponseClientError",
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
            return "SmaxGroupsGetMembershipApprovalRequestsResponse.ClientError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetMembershipApprovalRequestsResponseServerError")
    final class ServerError implements SmaxGroupsGetMembershipApprovalRequestsResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied
         * one.
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
         * @return an {@link Optional} carrying the error text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetMembershipApprovalRequestsResponseServerError",
                exports = "parseGetMembershipApprovalRequestsResponseServerError",
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
            return "SmaxGroupsGetMembershipApprovalRequestsResponse.ServerError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }
}
