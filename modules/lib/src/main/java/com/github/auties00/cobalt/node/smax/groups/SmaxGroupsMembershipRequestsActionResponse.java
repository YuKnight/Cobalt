package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxGroupsMembershipRequestsActionRPC.sendMembershipRequestsActionRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError}.
 */
public sealed interface SmaxGroupsMembershipRequestsActionResponse extends SmaxOperation.Response
        permits SmaxGroupsMembershipRequestsActionResponse.Success, SmaxGroupsMembershipRequestsActionResponse.ClientError, SmaxGroupsMembershipRequestsActionResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsMembershipRequestsActionResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsMembershipRequestsActionRPC",
            exports = "sendMembershipRequestsActionRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsMembershipRequestsActionResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — the relay processed the
     * action and returned the per-participant outcomes split into
     * approved / rejected lists.
     *
     * @implNote {@code WASmaxInGroupsMembershipRequestsActionResponseSuccess.parseMembershipRequestsActionResponseSuccess}
     *           validates the {@code <iq from id type="result">}
     *           envelope, then walks the
     *           {@code <membership_requests_action>} child for
     *           optional {@code <approve>} / {@code <reject>}
     *           grand-children — each carrying its own
     *           {@code 1..19999} {@code <participant>} list with
     *           the corresponding mixin projection.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsMembershipRequestsActionResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupAddressingModeMixin")
    final class Success implements SmaxGroupsMembershipRequestsActionResponse {
        /**
         * The optional addressing-mode echo on the IQ envelope.
         */
        private final String addressingMode;

        /**
         * The per-participant approval outcomes; may be empty when
         * the request didn't carry an {@code <approve>} list or the
         * relay produced an empty list.
         */
        private final List<ApproveParticipantResult> approveParticipants;

        /**
         * The per-participant rejection outcomes; may be empty
         * symmetrically.
         */
        private final List<RejectParticipantResult> rejectParticipants;

        /**
         * Constructs a new successful reply.
         *
         * @param addressingMode      the optional addressing-mode
         *                            echo
         * @param approveParticipants the per-participant approval
         *                            outcomes; never {@code null}
         * @param rejectParticipants  the per-participant rejection
         *                            outcomes; never {@code null}
         */
        public Success(String addressingMode,
                       List<ApproveParticipantResult> approveParticipants,
                       List<RejectParticipantResult> rejectParticipants) {
            this.addressingMode = addressingMode;
            this.approveParticipants = List.copyOf(
                    Objects.requireNonNullElse(approveParticipants, List.of()));
            this.rejectParticipants = List.copyOf(
                    Objects.requireNonNullElse(rejectParticipants, List.of()));
        }

        /**
         * Returns the optional addressing-mode echo.
         *
         * @return an {@link Optional} carrying the addressing mode
         */
        public Optional<String> addressingMode() {
            return Optional.ofNullable(addressingMode);
        }

        /**
         * Returns the per-participant approval outcomes.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<ApproveParticipantResult> approveParticipants() {
            return approveParticipants;
        }

        /**
         * Returns the per-participant rejection outcomes.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<RejectParticipantResult> rejectParticipants() {
            return rejectParticipants;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsMembershipRequestsActionResponseSuccess",
                exports = "parseMembershipRequestsActionResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "result")) {
                return Optional.empty();
            }
            var requestId = request.getAttributeAsString("id").orElse(null);
            if (requestId == null || !node.hasAttribute("id", requestId)) {
                return Optional.empty();
            }
            var requestTo = request.getAttributeAsString("to").orElse(null);
            if (requestTo != null && !node.hasAttribute("from", requestTo)) {
                return Optional.empty();
            }
            var action = node.getChild("membership_requests_action").orElse(null);
            if (action == null) {
                return Optional.empty();
            }
            var addressingMode = node.getAttributeAsString("addressing_mode").orElse(null);
            var approveContainer = action.getChild("approve").orElse(null);
            var approveParticipants = new ArrayList<ApproveParticipantResult>();
            if (approveContainer != null) {
                for (var participantNode : approveContainer.getChildren("participant")) {
                    var participant = ApproveParticipantResult.of(participantNode).orElse(null);
                    if (participant == null) {
                        return Optional.empty();
                    }
                    approveParticipants.add(participant);
                }
            }
            var rejectContainer = action.getChild("reject").orElse(null);
            var rejectParticipants = new ArrayList<RejectParticipantResult>();
            if (rejectContainer != null) {
                for (var participantNode : rejectContainer.getChildren("participant")) {
                    var participant = RejectParticipantResult.of(participantNode).orElse(null);
                    if (participant == null) {
                        return Optional.empty();
                    }
                    rejectParticipants.add(participant);
                }
            }
            return Optional.of(new Success(addressingMode, approveParticipants, rejectParticipants));
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
            return Objects.equals(this.addressingMode, that.addressingMode)
                    && Objects.equals(this.approveParticipants, that.approveParticipants)
                    && Objects.equals(this.rejectParticipants, that.rejectParticipants);
        }

        @Override
        public int hashCode() {
            return Objects.hash(addressingMode, approveParticipants, rejectParticipants);
        }

        @Override
        public String toString() {
            return "SmaxGroupsMembershipRequestsActionResponse.Success[addressingMode=" + addressingMode
                    + ", approveParticipants=" + approveParticipants
                    + ", rejectParticipants=" + rejectParticipants + ']';
        }

        /**
         * The per-participant approval outcome — surfaces the
         * approved JID and the optional identity-mixin payload
         * carried by the relay.
         *
         * @implNote {@code WASmaxInGroupsIdentityMixin} carries the
         *           Signal-protocol identity key fingerprints used
         *           by clients that pre-fetch sender keys for
         *           imminent group messaging; Cobalt does not
         *           project the inner mixin fields here, exposing
         *           presence-only via
         *           {@link #hasIdentityPayload()} for callers that
         *           need to drive the identity refresh.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsIdentityMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsMembershipRequestsActionAcceptParticipantMixins")
        public static final class ApproveParticipantResult {
            /**
             * The approved participant JID.
             */
            private final Jid jid;

            /**
             * Whether the relay supplied an identity-mixin payload
             * for this participant.
             */
            private final boolean hasIdentityPayload;

            /**
             * Constructs a new approval-outcome entry.
             *
             * @param jid                the participant JID; never
             *                           {@code null}
             * @param hasIdentityPayload whether the relay carried
             *                           the identity mixin
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public ApproveParticipantResult(Jid jid, boolean hasIdentityPayload) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.hasIdentityPayload = hasIdentityPayload;
            }

            /**
             * Returns the approved participant JID.
             *
             * @return the JID; never {@code null}
             */
            public Jid jid() {
                return jid;
            }

            /**
             * Returns whether the entry carried an identity-mixin
             * payload.
             *
             * @return {@code true} when the {@code <identity/>}
             *         child was present
             */
            public boolean hasIdentityPayload() {
                return hasIdentityPayload;
            }

            /**
             * Tries to parse an approval-outcome entry.
             *
             * @param node the {@code <participant>} child
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInGroupsMembershipRequestsActionResponseSuccess",
                    exports = "parseMembershipRequestsActionResponseSuccessMembershipRequestsActionApproveParticipant",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<ApproveParticipantResult> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("participant")) {
                    return Optional.empty();
                }
                var jid = node.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var hasIdentity = node.getChild("identity").isPresent();
                return Optional.of(new ApproveParticipantResult(jid, hasIdentity));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (ApproveParticipantResult) obj;
                return this.hasIdentityPayload == that.hasIdentityPayload
                        && Objects.equals(this.jid, that.jid);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, hasIdentityPayload);
            }

            @Override
            public String toString() {
                return "SmaxGroupsMembershipRequestsActionResponse.Success.ApproveParticipantResult[jid=" + jid
                        + ", hasIdentityPayload=" + hasIdentityPayload + ']';
            }
        }

        /**
         * The per-participant rejection outcome — surfaces the
         * rejected JID and the optional identity-mixin presence.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsMembershipRequestsActionRejectParticipantMixins")
        public static final class RejectParticipantResult {
            /**
             * The rejected participant JID.
             */
            private final Jid jid;

            /**
             * Whether the entry carried an identity-mixin payload.
             */
            private final boolean hasIdentityPayload;

            /**
             * Constructs a new rejection-outcome entry.
             *
             * @param jid                the participant JID; never
             *                           {@code null}
             * @param hasIdentityPayload whether the identity mixin
             *                           was present
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public RejectParticipantResult(Jid jid, boolean hasIdentityPayload) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.hasIdentityPayload = hasIdentityPayload;
            }

            /**
             * Returns the rejected participant JID.
             *
             * @return the JID; never {@code null}
             */
            public Jid jid() {
                return jid;
            }

            /**
             * Returns whether the entry carried an identity-mixin
             * payload.
             *
             * @return {@code true} when the {@code <identity/>}
             *         child was present
             */
            public boolean hasIdentityPayload() {
                return hasIdentityPayload;
            }

            /**
             * Tries to parse a rejection-outcome entry.
             *
             * @param node the {@code <participant>} child
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInGroupsMembershipRequestsActionResponseSuccess",
                    exports = "parseMembershipRequestsActionResponseSuccessMembershipRequestsActionRejectParticipant",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<RejectParticipantResult> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("participant")) {
                    return Optional.empty();
                }
                var jid = node.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var hasIdentity = node.getChild("identity").isPresent();
                return Optional.of(new RejectParticipantResult(jid, hasIdentity));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (RejectParticipantResult) obj;
                return this.hasIdentityPayload == that.hasIdentityPayload
                        && Objects.equals(this.jid, that.jid);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, hasIdentityPayload);
            }

            @Override
            public String toString() {
                return "SmaxGroupsMembershipRequestsActionResponse.Success.RejectParticipantResult[jid=" + jid
                        + ", hasIdentityPayload=" + hasIdentityPayload + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsMembershipRequestsActionResponseClientError")
    final class ClientError implements SmaxGroupsMembershipRequestsActionResponse {
        /**
         * The numeric error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the error code
         * @param errorText the optional text; may be {@code null}
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
         * Returns the optional error text.
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsMembershipRequestsActionResponseClientError",
                exports = "parseMembershipRequestsActionResponseClientError",
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
            return "SmaxGroupsMembershipRequestsActionResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsMembershipRequestsActionResponseServerError")
    final class ServerError implements SmaxGroupsMembershipRequestsActionResponse {
        /**
         * The numeric error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the error code
         * @param errorText the optional text; may be {@code null}
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
         * Returns the optional error text.
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsMembershipRequestsActionResponseServerError",
                exports = "parseMembershipRequestsActionResponseServerError",
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
            return "SmaxGroupsMembershipRequestsActionResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
