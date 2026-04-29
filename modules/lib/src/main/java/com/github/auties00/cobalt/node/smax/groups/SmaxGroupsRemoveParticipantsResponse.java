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
 * @implNote {@code WASmaxGroupsRemoveParticipantsRPC.sendRemoveParticipantsRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError}.
 */
public sealed interface SmaxGroupsRemoveParticipantsResponse extends SmaxOperation.Response
        permits SmaxGroupsRemoveParticipantsResponse.Success, SmaxGroupsRemoveParticipantsResponse.ClientError, SmaxGroupsRemoveParticipantsResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsRemoveParticipantsResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsRemoveParticipantsRPC",
            exports = "sendRemoveParticipantsRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsRemoveParticipantsResponse> of(Node node, Node request) {
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
     * request and returned a per-participant outcome list.
     *
     * @implNote {@code WASmaxInGroupsRemoveParticipantsResponseSuccess.parseRemoveParticipantsResponseSuccess}
     *           validates the IQ-result envelope, asserts the
     *           {@code <remove>} child, parses the optional
     *           {@code linked_groups="true"} echo and the
     *           {@code addressing_mode} mixin, then projects every
     *           {@code <participant>} child via
     *           {@code parseRemoveParticipantsResponseSuccessRemoveParticipant}
     *           which routes through the
     *           {@code ParticipantNotInGroupOrParticipantNotAllowedOrParticipantNotAcceptableOrRemoveParticipantsLinkedGroupsServerErrorMixinGroup}
     *           disjunction.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsRemoveParticipantsResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupAddressingModeMixin")
    final class Success implements SmaxGroupsRemoveParticipantsResponse {
        /**
         * The {@code linked_groups="true"} echo lifted from the
         * {@code <remove>} child of the success reply, when present.
         */
        private final boolean removeLinkedGroups;

        /**
         * The optional {@code addressing_mode} echo on the IQ
         * envelope.
         */
        private final String addressingMode;

        /**
         * The per-participant outcome entries.
         */
        private final List<RemoveParticipantResult> participants;

        /**
         * Constructs a new successful reply.
         *
         * @param removeLinkedGroups whether the relay echoed the
         *                           {@code linked_groups="true"} flag
         * @param addressingMode     the optional addressing mode
         *                           echo; may be {@code null}
         * @param participants       the per-participant outcomes;
         *                           never {@code null}
         */
        public Success(boolean removeLinkedGroups, String addressingMode,
                       List<RemoveParticipantResult> participants) {
            this.removeLinkedGroups = removeLinkedGroups;
            this.addressingMode = addressingMode;
            this.participants = List.copyOf(Objects.requireNonNullElse(participants, List.of()));
        }

        /**
         * Returns whether the relay echoed
         * {@code linked_groups="true"}.
         *
         * @return the flag
         */
        public boolean removeLinkedGroups() {
            return removeLinkedGroups;
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
         * Returns the per-participant outcome entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<RemoveParticipantResult> participants() {
            return participants;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsRemoveParticipantsResponseSuccess",
                exports = "parseRemoveParticipantsResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var remove = node.getChild("remove").orElse(null);
            if (remove == null) {
                return Optional.empty();
            }
            var linkedGroups = remove.hasAttribute("linked_groups", "true");
            var addressingMode = node.getAttributeAsString("addressing_mode").orElse(null);
            var participants = new ArrayList<RemoveParticipantResult>();
            for (var participantNode : remove.getChildren("participant")) {
                var participant = RemoveParticipantResult.of(participantNode).orElse(null);
                if (participant == null) {
                    return Optional.empty();
                }
                participants.add(participant);
            }
            return Optional.of(new Success(linkedGroups, addressingMode, participants));
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
            return this.removeLinkedGroups == that.removeLinkedGroups
                    && Objects.equals(this.addressingMode, that.addressingMode)
                    && Objects.equals(this.participants, that.participants);
        }

        @Override
        public int hashCode() {
            return Objects.hash(removeLinkedGroups, addressingMode, participants);
        }

        @Override
        public String toString() {
            return "SmaxGroupsRemoveParticipantsResponse.Success[removeLinkedGroups=" + removeLinkedGroups
                    + ", addressingMode=" + addressingMode
                    + ", participants=" + participants + ']';
        }

        /**
         * The per-participant outcome entry produced by the relay
         * for a single removed JID.
         *
         * <p>WhatsApp Web models this as a 4-arm disjunction
         * ({@code ParticipantNotInGroup},
         * {@code ParticipantNotAllowed},
         * {@code ParticipantNotAcceptable},
         * {@code RemoveParticipantsLinkedGroupsServerError}); Cobalt
         * fuses the disjunction into the always-present {@code jid}
         * plus an optional {@link RejectionReason} payload.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsParticipantNotInGroupOrParticipantNotAllowedOrParticipantNotAcceptableOrRemoveParticipantsLinkedGroupsServerErrorMixinGroup")
        public static final class RemoveParticipantResult {
            /**
             * The participant JID.
             */
            private final Jid jid;

            /**
             * The optional echoed {@code phone_number} attribute.
             */
            private final String phoneNumber;

            /**
             * The optional echoed {@code username} attribute.
             */
            private final String username;

            /**
             * The optional rejection-reason payload — present only
             * when the relay rejected this candidate.
             */
            private final RejectionReason rejectionReason;

            /**
             * Constructs a new outcome entry.
             *
             * @param jid             the participant JID; never
             *                        {@code null}
             * @param phoneNumber     the optional phone-number echo
             * @param username        the optional username echo
             * @param rejectionReason the optional rejection reason
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public RemoveParticipantResult(Jid jid, String phoneNumber, String username,
                                           RejectionReason rejectionReason) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.phoneNumber = phoneNumber;
                this.username = username;
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
             * Returns the optional username echo.
             *
             * @return an {@link Optional} carrying the username
             */
            public Optional<String> username() {
                return Optional.ofNullable(username);
            }

            /**
             * Returns the optional rejection-reason payload.
             *
             * @return an {@link Optional} carrying the rejection
             *         reason, or empty when this entry succeeded
             */
            public Optional<RejectionReason> rejectionReason() {
                return Optional.ofNullable(rejectionReason);
            }

            /**
             * Tries to parse an outcome entry.
             *
             * @param node the {@code <participant>} child
             * @return an {@link Optional} carrying the parsed entry
             *
             * @implNote {@code WASmaxInGroupsRemoveParticipantsResponseSuccess.parseRemoveParticipantsResponseSuccessRemoveParticipant}
             *           projects the {@code error} attribute through
             *           a 4-arm disjunction; Cobalt distinguishes the
             *           rejected arms by the presence of the
             *           {@code error} attribute on the
             *           {@code <participant>} node.
             */
            @WhatsAppWebExport(moduleName = "WASmaxInGroupsRemoveParticipantsResponseSuccess",
                    exports = "parseRemoveParticipantsResponseSuccessRemoveParticipant",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<RemoveParticipantResult> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("participant")) {
                    return Optional.empty();
                }
                var jid = node.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var phoneNumber = node.getAttributeAsString("phone_number").orElse(null);
                var username = node.getAttributeAsString("username").orElse(null);
                var rejectionReason = RejectionReason.of(node).orElse(null);
                return Optional.of(new RemoveParticipantResult(jid, phoneNumber, username,
                        rejectionReason));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (RemoveParticipantResult) obj;
                return Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.phoneNumber, that.phoneNumber)
                        && Objects.equals(this.username, that.username)
                        && Objects.equals(this.rejectionReason, that.rejectionReason);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, phoneNumber, username, rejectionReason);
            }

            @Override
            public String toString() {
                return "SmaxGroupsRemoveParticipantsResponse.Success.RemoveParticipantResult[jid=" + jid
                        + ", phoneNumber=" + phoneNumber
                        + ", username=" + username
                        + ", rejectionReason=" + rejectionReason + ']';
            }

            /**
             * The rejection-reason payload for a participant the
             * relay refused to remove.
             *
             * <p>Surfaces the raw error code lifted from the
             * {@code error} attribute of the {@code <participant>}
             * node; the documented codes correspond to the four arms
             * of the WA Web disjunction
             * ({@code ParticipantNotInGroup},
             * {@code ParticipantNotAllowed},
             * {@code ParticipantNotAcceptable},
             * {@code RemoveParticipantsLinkedGroupsServerError}).
             */
            public static final class RejectionReason {
                /**
                 * The numeric rejection-error code.
                 */
                private final int errorCode;

                /**
                 * Constructs a new rejection-reason payload.
                 *
                 * @param errorCode the numeric error code
                 */
                public RejectionReason(int errorCode) {
                    this.errorCode = errorCode;
                }

                /**
                 * Returns the numeric rejection-error code.
                 *
                 * @return the error code
                 */
                public int errorCode() {
                    return errorCode;
                }

                /**
                 * Tries to parse a rejection-reason payload.
                 *
                 * @param node the {@code <participant>} child
                 * @return an {@link Optional} carrying the payload
                 */
                public static Optional<RejectionReason> of(Node node) {
                    Objects.requireNonNull(node, "node cannot be null");
                    var error = node.getAttributeAsInt("error").orElse(-1);
                    if (error < 0) {
                        return Optional.empty();
                    }
                    return Optional.of(new RejectionReason(error));
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
                    return this.errorCode == that.errorCode;
                }

                @Override
                public int hashCode() {
                    return Objects.hash(errorCode);
                }

                @Override
                public String toString() {
                    return "SmaxGroupsRemoveParticipantsResponse.Success.RemoveParticipantResult.RejectionReason[errorCode="
                            + errorCode + ']';
                }
            }
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed or unauthorised at the stanza level.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsRemoveParticipantsResponseClientError")
    final class ClientError implements SmaxGroupsRemoveParticipantsResponse {
        /**
         * The numeric server-side error code.
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsRemoveParticipantsResponseClientError",
                exports = "parseRemoveParticipantsResponseClientError",
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
            return "SmaxGroupsRemoveParticipantsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — transient relay-side
     * failure.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsRemoveParticipantsResponseServerError")
    final class ServerError implements SmaxGroupsRemoveParticipantsResponse {
        /**
         * The numeric server-side error code.
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsRemoveParticipantsResponseServerError",
                exports = "parseRemoveParticipantsResponseServerError",
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
            return "SmaxGroupsRemoveParticipantsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
