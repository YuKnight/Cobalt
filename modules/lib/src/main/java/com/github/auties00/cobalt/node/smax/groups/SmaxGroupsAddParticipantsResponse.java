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
 * response to a {@link SmaxGroupsAddParticipantsRequest}.
 */
public sealed interface SmaxGroupsAddParticipantsResponse extends SmaxOperation.Response
        permits SmaxGroupsAddParticipantsResponse.Success, SmaxGroupsAddParticipantsResponse.ClientError, SmaxGroupsAddParticipantsResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsAddParticipantsResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsAddParticipantsRPC",
            exports = "sendAddParticipantsRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsAddParticipantsResponse> of(Node node, Node request) {
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
     * <p>The IQ envelope itself succeeds even when every candidate
     * was rejected at the participant-policy level; callers must
     * walk {@link #participants()} to detect partial / total
     * rejections.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsAddParticipantsResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsAddParticipantsParticipantAddedOrNonRegisteredWaUserParticipantErrorLidResponseMixinGroup")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupAddressingModeMixin")
    final class Success implements SmaxGroupsAddParticipantsResponse {
        /**
         * The optional {@code addressing_mode} attribute echoed by
         * the relay on the {@code <iq>} envelope; {@code "lid"} or
         * {@code "pn"} when present, {@code null} otherwise.
         */
        private final String addressingMode;

        /**
         * The per-participant outcome entries projected from the
         * {@code <add>} child.
         */
        private final List<AddParticipantResult> participants;

        /**
         * Constructs a new successful reply.
         *
         * @param addressingMode the optional addressing mode echo;
         *                       may be {@code null}
         * @param participants   the per-participant outcomes; never
         *                       {@code null}
         */
        public Success(String addressingMode, List<AddParticipantResult> participants) {
            this.addressingMode = addressingMode;
            this.participants = List.copyOf(Objects.requireNonNullElse(participants, List.of()));
        }

        /**
         * Returns the optional {@code addressing_mode} echo.
         *
         * @return an {@link Optional} carrying the addressing mode,
         *         or empty when the relay omitted it
         */
        public Optional<String> addressingMode() {
            return Optional.ofNullable(addressingMode);
        }

        /**
         * Returns the per-participant outcome entries.
         *
         * @return an unmodifiable list of outcomes; never
         *         {@code null}
         */
        public List<AddParticipantResult> participants() {
            return participants;
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsAddParticipantsResponseSuccess",
                exports = "parseAddParticipantsResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var add = node.getChild("add").orElse(null);
            if (add == null) {
                return Optional.empty();
            }
            var addressingMode = node.getAttributeAsString("addressing_mode").orElse(null);
            var participants = new ArrayList<AddParticipantResult>();
            for (var participantNode : add.getChildren("participant")) {
                var participant = AddParticipantResult.of(participantNode).orElse(null);
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
            return "SmaxGroupsAddParticipantsResponse.Success[addressingMode=" + addressingMode
                    + ", participants=" + participants + ']';
        }

        /**
         * The per-participant outcome entry produced by the relay
         * for a single candidate JID.
         *
         * <p>WhatsApp Web models this as a 2-arm disjunction
         * ({@code AddParticipantsParticipantAddedResponse} vs
         * {@code NonRegisteredWaUserParticipantErrorLidResponse});
         * Cobalt fuses the disjunction into a single class that
         * exposes the always-present {@code jid} plus an optional
         * {@link NonRegisteredWaUser} payload distinguishing the two
         * arms.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsAddParticipantsParticipantAddedResponseMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsNonRegisteredWaUserParticipantErrorLidResponseMixin")
        public static final class AddParticipantResult {
            /**
             * The participant JID (always present on both arms).
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
             * The optional non-registered-user payload — present only
             * when this entry represents the
             * {@code NonRegisteredWaUserParticipantErrorLidResponse}
             * arm of the disjunction.
             */
            private final NonRegisteredWaUser nonRegisteredUser;

            /**
             * Constructs a new outcome entry.
             *
             * @param jid               the participant JID; never
             *                          {@code null}
             * @param phoneNumber       the optional phone-number
             *                          echo; may be {@code null}
             * @param username          the optional username echo;
             *                          may be {@code null}
             * @param nonRegisteredUser the optional
             *                          non-registered-user payload;
             *                          may be {@code null}
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public AddParticipantResult(Jid jid, String phoneNumber, String username,
                                        NonRegisteredWaUser nonRegisteredUser) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.phoneNumber = phoneNumber;
                this.username = username;
                this.nonRegisteredUser = nonRegisteredUser;
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
             * Returns the optional non-registered-user payload.
             *
             * @return an {@link Optional} carrying the payload, or
             *         empty when this entry is the
             *         {@code Added}-arm of the disjunction
             */
            public Optional<NonRegisteredWaUser> nonRegisteredUser() {
                return Optional.ofNullable(nonRegisteredUser);
            }

            /**
             * Tries to parse an outcome entry from a single
             * {@code <participant>} child of the {@code <add>}
             * payload.
             *
             * @param node the {@code <participant>} child; never
             *             {@code null}
             * @return an {@link Optional} carrying the parsed entry,
             *         or empty when the node does not match either
             *         disjunction arm
             */
            @WhatsAppWebExport(moduleName = "WASmaxInGroupsAddParticipantsResponseSuccess",
                    exports = "parseAddParticipantsResponseSuccessAddParticipant",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<AddParticipantResult> of(Node node) {
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
                var nonRegisteredUser = NonRegisteredWaUser.of(node).orElse(null);
                return Optional.of(new AddParticipantResult(jid, phoneNumber, username,
                        nonRegisteredUser));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (AddParticipantResult) obj;
                return Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.phoneNumber, that.phoneNumber)
                        && Objects.equals(this.username, that.username)
                        && Objects.equals(this.nonRegisteredUser, that.nonRegisteredUser);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, phoneNumber, username, nonRegisteredUser);
            }

            @Override
            public String toString() {
                return "SmaxGroupsAddParticipantsResponse.Success.AddParticipantResult[jid=" + jid
                        + ", phoneNumber=" + phoneNumber
                        + ", username=" + username
                        + ", nonRegisteredUser=" + nonRegisteredUser + ']';
            }

            /**
             * The {@code NonRegisteredWaUserParticipantErrorLidResponse}
             * arm payload — surfaces why a candidate could not be
             * added because the supplied phone number / LID does not
             * map to a registered WhatsApp user.
             *
             * <p>Carries the error code lifted from the inner
             * {@code WASmaxInGroupsParticipantRequestCodeCanBeSentOrRequestCodeCannotBeCreatedForLegalConcernsOrHasInvalidPNMixinGroup}
             * disjunction; the phone-number echo is also surfaced
             * for diagnostic display.
             */
            @WhatsAppWebModule(moduleName = "WASmaxInGroupsParticipantRequestCodeCanBeSentOrRequestCodeCannotBeCreatedForLegalConcernsOrHasInvalidPNMixinGroup")
            public static final class NonRegisteredWaUser {
                /**
                 * The numeric error code documenting why the
                 * candidate was rejected.
                 */
                private final int errorCode;

                /**
                 * Constructs a new non-registered-user payload.
                 *
                 * @param errorCode the numeric error code
                 */
                public NonRegisteredWaUser(int errorCode) {
                    this.errorCode = errorCode;
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
                 * Tries to parse a non-registered-user payload from
                 * a {@code <participant>} child.
                 *
                 * @param node the {@code <participant>} child
                 * @return an {@link Optional} carrying the parsed
                 *         payload, or empty when the participant has
                 *         no error attribute (it's the
                 *         {@code Added}-arm of the outer disjunction)
                 */
                public static Optional<NonRegisteredWaUser> of(Node node) {
                    Objects.requireNonNull(node, "node cannot be null");
                    var error = node.getAttributeAsInt("error").orElse(-1);
                    if (error < 0) {
                        return Optional.empty();
                    }
                    return Optional.of(new NonRegisteredWaUser(error));
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) {
                        return true;
                    }
                    if (obj == null || obj.getClass() != this.getClass()) {
                        return false;
                    }
                    var that = (NonRegisteredWaUser) obj;
                    return this.errorCode == that.errorCode;
                }

                @Override
                public int hashCode() {
                    return Objects.hash(errorCode);
                }

                @Override
                public String toString() {
                    return "SmaxGroupsAddParticipantsResponse.Success.AddParticipantResult.NonRegisteredWaUser[errorCode="
                            + errorCode + ']';
                }
            }
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, or referencing a
     * non-existent group.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsAddParticipantsResponseClientError")
    final class ClientError implements SmaxGroupsAddParticipantsResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when supplied.
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
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsAddParticipantsResponseClientError",
                exports = "parseAddParticipantsResponseClientError",
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
            return "SmaxGroupsAddParticipantsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsAddParticipantsResponseServerError")
    final class ServerError implements SmaxGroupsAddParticipantsResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when supplied.
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsAddParticipantsResponseServerError",
                exports = "parseAddParticipantsResponseServerError",
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
            return "SmaxGroupsAddParticipantsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
