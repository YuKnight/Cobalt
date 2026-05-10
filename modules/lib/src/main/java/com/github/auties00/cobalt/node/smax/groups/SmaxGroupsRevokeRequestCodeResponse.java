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
 * response to a {@link SmaxGroupsRevokeRequestCodeRequest}.
 */
public sealed interface SmaxGroupsRevokeRequestCodeResponse extends SmaxOperation.Response
        permits SmaxGroupsRevokeRequestCodeResponse.Success, SmaxGroupsRevokeRequestCodeResponse.ClientError, SmaxGroupsRevokeRequestCodeResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsRevokeRequestCodeResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to
     *                validate echoed identifiers; never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsRevokeRequestCodeRPC",
            exports = "sendRevokeRequestCodeRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsRevokeRequestCodeResponse> of(Node node, Node request) {
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
     * revocation and returned a per-participant outcome list.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsRevokeRequestCodeResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupAddressingModeMixin")
    final class Success implements SmaxGroupsRevokeRequestCodeResponse {
        /**
         * The optional {@code addressing_mode} echo on the IQ
         * envelope.
         */
        private final String addressingMode;

        /**
         * The per-participant outcome entries.
         */
        private final List<RevokeParticipantResult> participants;

        /**
         * Constructs a new successful reply.
         *
         * @param addressingMode the optional addressing-mode echo;
         *                       may be {@code null}
         * @param participants   the per-participant outcomes; never
         *                       {@code null}
         * @throws NullPointerException if {@code participants} is
         *                              {@code null}
         */
        public Success(String addressingMode, List<RevokeParticipantResult> participants) {
            this.addressingMode = addressingMode;
            Objects.requireNonNull(participants, "participants cannot be null");
            this.participants = List.copyOf(participants);
        }

        /**
         * Returns the optional addressing-mode echo.
         *
         * @return an {@link Optional} carrying the addressing
         *         mode, or empty when the relay omitted it
         */
        public Optional<String> addressingMode() {
            return Optional.ofNullable(addressingMode);
        }

        /**
         * Returns the per-participant outcome entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<RevokeParticipantResult> participants() {
            return participants;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsRevokeRequestCodeResponseSuccess",
                exports = "parseRevokeRequestCodeResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var revoke = node.getChild("revoke").orElse(null);
            if (revoke == null) {
                return Optional.empty();
            }
            var addressingMode = node.getAttributeAsString("addressing_mode").orElse(null);
            var participants = new ArrayList<RevokeParticipantResult>();
            for (var participantNode : revoke.getChildren("participant")) {
                var participant = RevokeParticipantResult.of(participantNode).orElse(null);
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
            return "SmaxGroupsRevokeRequestCodeResponse.Success[addressingMode=" + addressingMode
                    + ", participants=" + participants + ']';
        }

        /**
         * The per-participant outcome entry produced by the relay
         * for a single revoked candidate.
         *
         * <p>WhatsApp Web models each entry as the mandatory
         * {@code jid} attribute plus three optional projections:
         * the literal {@code error="404"} marker (signalling that
         * the candidate had no outstanding request to revoke), the
         * {@code phone_number} echo from the
         * {@code WASmaxInGroupsPhoneNumberMixin}, and the
         * {@code username} echo from the
         * {@code WASmaxInGroupsUsernameAttMixin}. Cobalt surfaces
         * all three as {@link Optional} accessors.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsRevokeRequestCodeResponseSuccess")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsPhoneNumberMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsUsernameAttMixin")
        public static final class RevokeParticipantResult {
            /**
             * The participant JID.
             */
            private final Jid jid;

            /**
             * The optional literal {@code error="404"} marker.
             * When present, the relay reports that the candidate
             * had no outstanding membership request to revoke.
             */
            private final String error;

            /**
             * The optional echoed {@code phone_number} attribute.
             */
            private final String phoneNumber;

            /**
             * The optional echoed {@code username} attribute.
             */
            private final String username;

            /**
             * Constructs a new outcome entry.
             *
             * @param jid         the participant JID; never
             *                    {@code null}
             * @param error       the optional {@code error="404"}
             *                    marker; may be {@code null}
             * @param phoneNumber the optional phone-number echo;
             *                    may be {@code null}
             * @param username    the optional username echo; may
             *                    be {@code null}
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public RevokeParticipantResult(Jid jid, String error, String phoneNumber, String username) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.error = error;
                this.phoneNumber = phoneNumber;
                this.username = username;
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
             * Returns the optional {@code error="404"} marker.
             *
             * @return an {@link Optional} carrying the literal
             *         error code, or empty when the revocation
             *         succeeded for this candidate
             */
            public Optional<String> error() {
                return Optional.ofNullable(error);
            }

            /**
             * Returns the optional phone-number echo.
             *
             * @return an {@link Optional} carrying the phone
             *         number
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
             * Tries to parse an outcome entry from a single
             * {@code <participant>} child.
             *
             * @param node the {@code <participant>} child; never
             *             {@code null}
             * @return an {@link Optional} carrying the parsed
             *         entry, or empty when the node shape does
             *         not match
             * @throws NullPointerException if {@code node} is
             *                              {@code null}
             */
            @WhatsAppWebExport(moduleName = "WASmaxInGroupsRevokeRequestCodeResponseSuccess",
                    exports = "parseRevokeRequestCodeResponseSuccessRevokeParticipant",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<RevokeParticipantResult> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("participant")) {
                    return Optional.empty();
                }
                var jid = node.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var error = node.getAttributeAsString("error").orElse(null);
                var phoneNumber = node.getAttributeAsString("phone_number").orElse(null);
                var username = node.getAttributeAsString("username").orElse(null);
                return Optional.of(new RevokeParticipantResult(jid, error, phoneNumber, username));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (RevokeParticipantResult) obj;
                return Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.error, that.error)
                        && Objects.equals(this.phoneNumber, that.phoneNumber)
                        && Objects.equals(this.username, that.username);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, error, phoneNumber, username);
            }

            @Override
            public String toString() {
                return "SmaxGroupsRevokeRequestCodeResponse.Success.RevokeParticipantResult[jid=" + jid
                        + ", error=" + error
                        + ", phoneNumber=" + phoneNumber
                        + ", username=" + username + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected
     * the request as malformed, unauthorised, or referencing a
     * non-existent group.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsRevokeRequestCodeResponseClientError")
    final class ClientError implements SmaxGroupsRevokeRequestCodeResponse {
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
         * @param errorText the optional human-readable text; may
         *                  be {@code null}
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
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsRevokeRequestCodeResponseClientError",
                exports = "parseRevokeRequestCodeResponseClientError",
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
            return "SmaxGroupsRevokeRequestCodeResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay
     * encountered a transient internal failure while processing
     * the request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsRevokeRequestCodeResponseServerError")
    final class ServerError implements SmaxGroupsRevokeRequestCodeResponse {
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
         * @param errorText the optional human-readable text; may
         *                  be {@code null}
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
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsRevokeRequestCodeResponseServerError",
                exports = "parseRevokeRequestCodeResponseServerError",
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
            return "SmaxGroupsRevokeRequestCodeResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
