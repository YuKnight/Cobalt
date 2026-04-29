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
 * @implNote {@code WASmaxGroupsPromoteDemoteRPC.sendPromoteDemoteRPC}
 *           tries {@code SuccessPromote} → {@code SuccessDemote} →
 *           {@code ClientError} → {@code ServerError}.
 */
public sealed interface SmaxGroupsPromoteDemoteResponse extends SmaxOperation.Response
        permits SmaxGroupsPromoteDemoteResponse.SuccessPromote, SmaxGroupsPromoteDemoteResponse.SuccessDemote,
        SmaxGroupsPromoteDemoteResponse.ClientError, SmaxGroupsPromoteDemoteResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsPromoteDemoteResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsPromoteDemoteRPC",
            exports = "sendPromoteDemoteRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsPromoteDemoteResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var promote = SuccessPromote.of(node, request);
        if (promote.isPresent()) {
            return promote;
        }
        var demote = SuccessDemote.of(node, request);
        if (demote.isPresent()) {
            return demote;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code SuccessPromote} reply variant — the relay processed
     * a promotion list and returned the per-participant outcomes.
     *
     * @implNote {@code WASmaxInGroupsPromoteDemoteResponseSuccessPromote.parsePromoteDemoteResponseSuccessPromote}
     *           validates the IQ-result envelope, asserts the
     *           {@code <promote>} child, parses the optional
     *           {@code addressing_mode} mixin, then projects each
     *           {@code <participant>} via the
     *           {@code ENUM_404_419} error projection
     *           ({@code 404} = not in group / not registered,
     *           {@code 419} = legal hold).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsPromoteDemoteResponseSuccessPromote")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupAddressingModeMixin")
    final class SuccessPromote implements SmaxGroupsPromoteDemoteResponse {
        /**
         * The optional addressing-mode echo.
         */
        private final String addressingMode;

        /**
         * The per-participant promotion outcomes.
         */
        private final List<PromoteParticipantResult> participants;

        /**
         * Constructs a new promotion-success reply.
         *
         * @param addressingMode the optional addressing-mode echo;
         *                       may be {@code null}
         * @param participants   the per-participant outcomes; never
         *                       {@code null}
         */
        public SuccessPromote(String addressingMode, List<PromoteParticipantResult> participants) {
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
         * Returns the per-participant promotion outcomes.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<PromoteParticipantResult> participants() {
            return participants;
        }

        /**
         * Tries to parse a {@link SuccessPromote} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsPromoteDemoteResponseSuccessPromote",
                exports = "parsePromoteDemoteResponseSuccessPromote",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessPromote> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var promote = node.getChild("promote").orElse(null);
            if (promote == null) {
                return Optional.empty();
            }
            var addressingMode = node.getAttributeAsString("addressing_mode").orElse(null);
            var participants = new ArrayList<PromoteParticipantResult>();
            for (var participantNode : promote.getChildren("participant")) {
                var participant = PromoteParticipantResult.of(participantNode).orElse(null);
                if (participant == null) {
                    return Optional.empty();
                }
                participants.add(participant);
            }
            return Optional.of(new SuccessPromote(addressingMode, participants));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessPromote) obj;
            return Objects.equals(this.addressingMode, that.addressingMode)
                    && Objects.equals(this.participants, that.participants);
        }

        @Override
        public int hashCode() {
            return Objects.hash(addressingMode, participants);
        }

        @Override
        public String toString() {
            return "SmaxGroupsPromoteDemoteResponse.SuccessPromote[addressingMode=" + addressingMode
                    + ", participants=" + participants + ']';
        }

        /**
         * The per-participant outcome entry for a single
         * promotion target.
         *
         * <p>Successful promotions carry only the JID and an
         * optional {@code type="admin"} marker; rejected
         * promotions carry the {@code error} attribute lifted from
         * the {@code ENUM_404_419} projection.
         */
        public static final class PromoteParticipantResult {
            /**
             * The participant JID.
             */
            private final Jid jid;

            /**
             * The optional {@code type} attribute — {@code "admin"}
             * when the relay confirmed the promotion, {@code null}
             * otherwise.
             */
            private final String type;

            /**
             * The optional rejection-error code; {@code null} when
             * the promotion succeeded.
             */
            private final Integer errorCode;

            /**
             * The optional phone-number echo.
             */
            private final String phoneNumber;

            /**
             * The optional username echo.
             */
            private final String username;

            /**
             * Constructs a new outcome entry.
             *
             * @param jid         the participant JID; never
             *                    {@code null}
             * @param type        the optional type marker
             * @param errorCode   the optional rejection-error code
             * @param phoneNumber the optional phone-number echo
             * @param username    the optional username echo
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public PromoteParticipantResult(Jid jid, String type, Integer errorCode,
                                            String phoneNumber, String username) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.type = type;
                this.errorCode = errorCode;
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
             * Returns the optional {@code type} marker.
             *
             * @return an {@link Optional} carrying the type marker
             */
            public Optional<String> type() {
                return Optional.ofNullable(type);
            }

            /**
             * Returns the optional rejection-error code.
             *
             * @return an {@link Optional} carrying the error code,
             *         or empty when the promotion succeeded
             */
            public Optional<Integer> errorCode() {
                return Optional.ofNullable(errorCode);
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
             * Tries to parse an outcome entry.
             *
             * @param node the {@code <participant>} child
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInGroupsPromoteDemoteResponseSuccessPromote",
                    exports = "parsePromoteDemoteResponseSuccessPromotePromoteParticipant",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<PromoteParticipantResult> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("participant")) {
                    return Optional.empty();
                }
                var jid = node.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var type = node.getAttributeAsString("type").orElse(null);
                var error = node.getAttributeAsInt("error").orElse(-1);
                var errorCode = error < 0 ? null : Integer.valueOf(error);
                var phoneNumber = node.getAttributeAsString("phone_number").orElse(null);
                var username = node.getAttributeAsString("username").orElse(null);
                return Optional.of(new PromoteParticipantResult(jid, type, errorCode,
                        phoneNumber, username));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (PromoteParticipantResult) obj;
                return Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.type, that.type)
                        && Objects.equals(this.errorCode, that.errorCode)
                        && Objects.equals(this.phoneNumber, that.phoneNumber)
                        && Objects.equals(this.username, that.username);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, type, errorCode, phoneNumber, username);
            }

            @Override
            public String toString() {
                return "SmaxGroupsPromoteDemoteResponse.SuccessPromote.PromoteParticipantResult[jid=" + jid
                        + ", type=" + type
                        + ", errorCode=" + errorCode
                        + ", phoneNumber=" + phoneNumber
                        + ", username=" + username + ']';
            }
        }
    }

    /**
     * The {@code SuccessDemote} reply variant — the relay processed
     * a demotion list and returned the per-participant outcomes.
     *
     * @implNote {@code WASmaxInGroupsPromoteDemoteResponseSuccessDemote.parsePromoteDemoteResponseSuccessDemote}
     *           projects each {@code <participant>} via the
     *           {@code ENUM_404_406} error projection
     *           ({@code 404} = not in group, {@code 406} = not an
     *           admin).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsPromoteDemoteResponseSuccessDemote")
    final class SuccessDemote implements SmaxGroupsPromoteDemoteResponse {
        /**
         * The optional addressing-mode echo.
         */
        private final String addressingMode;

        /**
         * The per-participant demotion outcomes.
         */
        private final List<DemoteParticipantResult> participants;

        /**
         * Constructs a new demotion-success reply.
         *
         * @param addressingMode the optional addressing-mode echo
         * @param participants   the per-participant outcomes; never
         *                       {@code null}
         */
        public SuccessDemote(String addressingMode, List<DemoteParticipantResult> participants) {
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
         * Returns the per-participant demotion outcomes.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<DemoteParticipantResult> participants() {
            return participants;
        }

        /**
         * Tries to parse a {@link SuccessDemote} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsPromoteDemoteResponseSuccessDemote",
                exports = "parsePromoteDemoteResponseSuccessDemote",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessDemote> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var demote = node.getChild("demote").orElse(null);
            if (demote == null) {
                return Optional.empty();
            }
            var addressingMode = node.getAttributeAsString("addressing_mode").orElse(null);
            var participants = new ArrayList<DemoteParticipantResult>();
            for (var participantNode : demote.getChildren("participant")) {
                var participant = DemoteParticipantResult.of(participantNode).orElse(null);
                if (participant == null) {
                    return Optional.empty();
                }
                participants.add(participant);
            }
            return Optional.of(new SuccessDemote(addressingMode, participants));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessDemote) obj;
            return Objects.equals(this.addressingMode, that.addressingMode)
                    && Objects.equals(this.participants, that.participants);
        }

        @Override
        public int hashCode() {
            return Objects.hash(addressingMode, participants);
        }

        @Override
        public String toString() {
            return "SmaxGroupsPromoteDemoteResponse.SuccessDemote[addressingMode=" + addressingMode
                    + ", participants=" + participants + ']';
        }

        /**
         * The per-participant outcome entry for a single demotion
         * target.
         */
        public static final class DemoteParticipantResult {
            /**
             * The participant JID.
             */
            private final Jid jid;

            /**
             * The optional rejection-error code; {@code null} when
             * the demotion succeeded.
             */
            private final Integer errorCode;

            /**
             * The optional phone-number echo.
             */
            private final String phoneNumber;

            /**
             * The optional username echo.
             */
            private final String username;

            /**
             * Constructs a new outcome entry.
             *
             * @param jid         the participant JID; never
             *                    {@code null}
             * @param errorCode   the optional rejection-error code
             * @param phoneNumber the optional phone-number echo
             * @param username    the optional username echo
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public DemoteParticipantResult(Jid jid, Integer errorCode,
                                           String phoneNumber, String username) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.errorCode = errorCode;
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
             * Returns the optional rejection-error code.
             *
             * @return an {@link Optional} carrying the error code
             */
            public Optional<Integer> errorCode() {
                return Optional.ofNullable(errorCode);
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
             * Tries to parse an outcome entry.
             *
             * @param node the {@code <participant>} child
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInGroupsPromoteDemoteResponseSuccessDemote",
                    exports = "parsePromoteDemoteResponseSuccessDemoteDemoteParticipant",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<DemoteParticipantResult> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("participant")) {
                    return Optional.empty();
                }
                var jid = node.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var error = node.getAttributeAsInt("error").orElse(-1);
                var errorCode = error < 0 ? null : Integer.valueOf(error);
                var phoneNumber = node.getAttributeAsString("phone_number").orElse(null);
                var username = node.getAttributeAsString("username").orElse(null);
                return Optional.of(new DemoteParticipantResult(jid, errorCode, phoneNumber, username));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (DemoteParticipantResult) obj;
                return Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.errorCode, that.errorCode)
                        && Objects.equals(this.phoneNumber, that.phoneNumber)
                        && Objects.equals(this.username, that.username);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, errorCode, phoneNumber, username);
            }

            @Override
            public String toString() {
                return "SmaxGroupsPromoteDemoteResponse.SuccessDemote.DemoteParticipantResult[jid=" + jid
                        + ", errorCode=" + errorCode
                        + ", phoneNumber=" + phoneNumber
                        + ", username=" + username + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsPromoteDemoteResponseClientError")
    final class ClientError implements SmaxGroupsPromoteDemoteResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsPromoteDemoteResponseClientError",
                exports = "parsePromoteDemoteResponseClientError",
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
            return "SmaxGroupsPromoteDemoteResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsPromoteDemoteResponseServerError")
    final class ServerError implements SmaxGroupsPromoteDemoteResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsPromoteDemoteResponseServerError",
                exports = "parsePromoteDemoteResponseServerError",
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
            return "SmaxGroupsPromoteDemoteResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
