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
 *
 * @implNote {@code WASmaxGroupsCancelGroupMembershipRequestsRPC.sendCancelGroupMembershipRequestsRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError}.
 */
public sealed interface SmaxGroupsCancelGroupMembershipRequestsResponse extends SmaxOperation.Response
        permits SmaxGroupsCancelGroupMembershipRequestsResponse.Success, SmaxGroupsCancelGroupMembershipRequestsResponse.ClientError, SmaxGroupsCancelGroupMembershipRequestsResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsCancelGroupMembershipRequestsResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsCancelGroupMembershipRequestsRPC",
            exports = "sendCancelGroupMembershipRequestsRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsCancelGroupMembershipRequestsResponse> of(Node node, Node request) {
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
     * cancellation list and returned the per-participant outcomes.
     *
     * @implNote {@code WASmaxInGroupsCancelGroupMembershipRequestsResponseSuccess.parseCancelGroupMembershipRequestsResponseSuccess}
     *           validates the IQ-result envelope, asserts the
     *           {@code <cancel_membership_requests>} child, parses
     *           the optional {@code addressing_mode} mixin, then
     *           projects every {@code <participant>} via the
     *           {@code WASmaxInGroupsMembershipRequestsCancellationParticipantMixins}
     *           disjunction
     *           ({@code CancelGroupMembershipRequestsParticipantRequestNotFound}
     *           vs.
     *           {@code ParticipantNotAuthorized}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsCancelGroupMembershipRequestsResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupAddressingModeMixin")
    final class Success implements SmaxGroupsCancelGroupMembershipRequestsResponse {
        /**
         * The optional addressing-mode echo on the IQ envelope.
         */
        private final String addressingMode;

        /**
         * The per-participant outcomes.
         */
        private final List<CancelParticipantResult> participants;

        /**
         * Constructs a new successful reply.
         *
         * @param addressingMode the optional addressing-mode echo
         * @param participants   the per-participant outcomes; never
         *                       {@code null}
         */
        public Success(String addressingMode, List<CancelParticipantResult> participants) {
            this.addressingMode = addressingMode;
            this.participants = List.copyOf(Objects.requireNonNullElse(participants, List.of()));
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
         * Returns the per-participant outcomes.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<CancelParticipantResult> participants() {
            return participants;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsCancelGroupMembershipRequestsResponseSuccess",
                exports = "parseCancelGroupMembershipRequestsResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var cancel = node.getChild("cancel_membership_requests").orElse(null);
            if (cancel == null) {
                return Optional.empty();
            }
            var addressingMode = node.getAttributeAsString("addressing_mode").orElse(null);
            var participants = new ArrayList<CancelParticipantResult>();
            for (var participantNode : cancel.getChildren("participant")) {
                var participant = CancelParticipantResult.of(participantNode).orElse(null);
                if (participant == null) {
                    return Optional.empty();
                }
                participants.add(participant);
            }
            return Optional.of(new Success(addressingMode, participants));
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
                    && Objects.equals(this.participants, that.participants);
        }

        @Override
        public int hashCode() {
            return Objects.hash(addressingMode, participants);
        }

        @Override
        public String toString() {
            return "SmaxGroupsCancelGroupMembershipRequestsResponse.Success[addressingMode="
                    + addressingMode + ", participants=" + participants + ']';
        }

        /**
         * The per-participant outcome entry for a single
         * cancellation target.
         *
         * <p>Surfaces the cancelled JID, the optional phone-number
         * echo, and an optional {@link RejectionReason} payload
         * documenting which arm of the WA Web cancellation
         * disjunction was taken when the relay refused the entry.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsMembershipRequestsCancellationParticipantMixins")
        public static final class CancelParticipantResult {
            /**
             * The participant JID.
             */
            private final Jid jid;

            /**
             * The optional phone-number echo.
             */
            private final String phoneNumber;

            /**
             * The optional rejection-reason payload — present only
             * when the relay refused the entry.
             */
            private final RejectionReason rejectionReason;

            /**
             * Constructs a new outcome entry.
             *
             * @param jid             the participant JID; never
             *                        {@code null}
             * @param phoneNumber     the optional phone-number echo
             * @param rejectionReason the optional rejection reason
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public CancelParticipantResult(Jid jid, String phoneNumber,
                                           RejectionReason rejectionReason) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.phoneNumber = phoneNumber;
                this.rejectionReason = rejectionReason;
            }

            /**
             * Returns the participant JID.
             *
             * @return the JID; never {@code null}
             */
            public Jid jid() {
                return jid;
            }

            /**
             * Returns the optional phone-number echo.
             *
             * @return an {@link Optional} carrying the phone number
             */
            public Optional<String> phoneNumber() {
                return Optional.ofNullable(phoneNumber);
            }

            /**
             * Returns the optional rejection-reason payload.
             *
             * @return an {@link Optional} carrying the reason, or
             *         empty when the cancellation succeeded
             */
            public Optional<RejectionReason> rejectionReason() {
                return Optional.ofNullable(rejectionReason);
            }

            /**
             * Tries to parse an outcome entry.
             *
             * @param node the {@code <participant>} child
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInGroupsCancelGroupMembershipRequestsResponseSuccess",
                    exports = "parseCancelGroupMembershipRequestsResponseSuccessCancelMembershipRequestsParticipant",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<CancelParticipantResult> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("participant")) {
                    return Optional.empty();
                }
                var jid = node.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var phoneNumber = node.getAttributeAsString("phone_number").orElse(null);
                var rejectionReason = RejectionReason.of(node).orElse(null);
                return Optional.of(new CancelParticipantResult(jid, phoneNumber, rejectionReason));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (CancelParticipantResult) obj;
                return Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.phoneNumber, that.phoneNumber)
                        && Objects.equals(this.rejectionReason, that.rejectionReason);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, phoneNumber, rejectionReason);
            }

            @Override
            public String toString() {
                return "SmaxGroupsCancelGroupMembershipRequestsResponse.Success.CancelParticipantResult[jid="
                        + jid
                        + ", phoneNumber=" + phoneNumber
                        + ", rejectionReason=" + rejectionReason + ']';
            }

            /**
             * The rejection-reason payload for a participant the
             * relay refused to cancel.
             *
             * <p>Identifies which arm of the WA Web disjunction the
             * relay took:
             * {@link Kind#REQUEST_NOT_FOUND} (the participant has no
             * pending request on this group) vs.
             * {@link Kind#NOT_AUTHORIZED} (the caller does not own
             * the targeted request and lacks admin rights to cancel
             * on behalf of others).
             */
            @WhatsAppWebModule(moduleName = "WASmaxInGroupsCancelGroupMembershipRequestsParticipantRequestNotFoundMixin")
            @WhatsAppWebModule(moduleName = "WASmaxInGroupsParticipantNotAuthorizedMixin")
            public static final class RejectionReason {
                /**
                 * The rejection-arm marker.
                 */
                private final Kind kind;

                /**
                 * Constructs a new rejection-reason payload.
                 *
                 * @param kind the rejection arm; never {@code null}
                 * @throws NullPointerException if {@code kind} is
                 *                              {@code null}
                 */
                public RejectionReason(Kind kind) {
                    this.kind = Objects.requireNonNull(kind, "kind cannot be null");
                }

                /**
                 * Returns the rejection-arm marker.
                 *
                 * @return the kind; never {@code null}
                 */
                public Kind kind() {
                    return kind;
                }

                /**
                 * Tries to parse a rejection-reason payload from a
                 * {@code <participant>} child.
                 *
                 * @param node the {@code <participant>} child
                 * @return an {@link Optional} carrying the parsed
                 *         payload, or empty when the entry succeeded
                 */
                @WhatsAppWebExport(moduleName = "WASmaxInGroupsMembershipRequestsCancellationParticipantMixins",
                        exports = "parseMembershipRequestsCancellationParticipantMixins",
                        adaptation = WhatsAppAdaptation.ADAPTED)
                public static Optional<RejectionReason> of(Node node) {
                    Objects.requireNonNull(node, "node cannot be null");
                    if (node.getChild("request_not_found").isPresent()) {
                        return Optional.of(new RejectionReason(Kind.REQUEST_NOT_FOUND));
                    }
                    if (node.getChild("not_authorized").isPresent()) {
                        return Optional.of(new RejectionReason(Kind.NOT_AUTHORIZED));
                    }
                    return Optional.empty();
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) {
                        return true;
                    }
                    if (obj == null || obj.getClass() != this.getClass()) {
                        return false;
                    }
                    var that = (RejectionReason) obj;
                    return this.kind == that.kind;
                }

                @Override
                public int hashCode() {
                    return Objects.hash(kind);
                }

                @Override
                public String toString() {
                    return "SmaxGroupsCancelGroupMembershipRequestsResponse.Success.CancelParticipantResult.RejectionReason[kind="
                            + kind + ']';
                }

                /**
                 * The rejection-arm enum — identifies which arm of
                 * the WA Web cancellation disjunction the relay
                 * took.
                 */
                public enum Kind {
                    /**
                     * The relay could not find a pending membership
                     * request matching the supplied participant on
                     * this group.
                     */
                    REQUEST_NOT_FOUND,

                    /**
                     * The caller is not authorised to cancel the
                     * targeted request — typically because they
                     * neither own it nor hold admin rights on the
                     * group.
                     */
                    NOT_AUTHORIZED
                }
            }
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsCancelGroupMembershipRequestsResponseClientError")
    final class ClientError implements SmaxGroupsCancelGroupMembershipRequestsResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsCancelGroupMembershipRequestsResponseClientError",
                exports = "parseCancelGroupMembershipRequestsResponseClientError",
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
            return "SmaxGroupsCancelGroupMembershipRequestsResponse.ClientError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsCancelGroupMembershipRequestsResponseServerError")
    final class ServerError implements SmaxGroupsCancelGroupMembershipRequestsResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsCancelGroupMembershipRequestsResponseServerError",
                exports = "parseCancelGroupMembershipRequestsResponseServerError",
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
            return "SmaxGroupsCancelGroupMembershipRequestsResponse.ServerError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }
}
