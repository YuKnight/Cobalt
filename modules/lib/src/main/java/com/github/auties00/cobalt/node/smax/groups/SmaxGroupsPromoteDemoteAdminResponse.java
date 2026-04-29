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
 * @implNote {@code WASmaxGroupsPromoteDemoteAdminRPC.sendPromoteDemoteAdminRPC}
 *           tries {@code SuccessMultiAdmin} → {@code ClientError} →
 *           {@code ServerError}.
 */
public sealed interface SmaxGroupsPromoteDemoteAdminResponse extends SmaxOperation.Response
        permits SmaxGroupsPromoteDemoteAdminResponse.SuccessMultiAdmin, SmaxGroupsPromoteDemoteAdminResponse.ClientError, SmaxGroupsPromoteDemoteAdminResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsPromoteDemoteAdminResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsPromoteDemoteAdminRPC",
            exports = "sendPromoteDemoteAdminRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsPromoteDemoteAdminResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = SuccessMultiAdmin.of(node, request);
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
     * The {@code SuccessMultiAdmin} reply variant — the relay
     * processed the admin roster change and returned the
     * per-participant outcomes under a uniform {@code <admin>}
     * envelope.
     *
     * @implNote {@code WASmaxInGroupsPromoteDemoteAdminResponseSuccessMultiAdmin.parsePromoteDemoteAdminResponseSuccessMultiAdmin}
     *           validates the IQ-result envelope, asserts the
     *           {@code <admin>} child, parses the optional
     *           {@code addressing_mode} mixin, then projects each
     *           {@code <participant>} via the
     *           {@code ENUM_403_404_406_419} error projection.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsPromoteDemoteAdminResponseSuccessMultiAdmin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupAddressingModeMixin")
    final class SuccessMultiAdmin implements SmaxGroupsPromoteDemoteAdminResponse {
        /**
         * The optional addressing-mode echo.
         */
        private final String addressingMode;

        /**
         * The per-participant outcomes.
         */
        private final List<AdminParticipantResult> participants;

        /**
         * Constructs a new admin-roster success reply.
         *
         * @param addressingMode the optional addressing-mode echo
         * @param participants   the per-participant outcomes; never
         *                       {@code null}
         */
        public SuccessMultiAdmin(String addressingMode, List<AdminParticipantResult> participants) {
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
        public List<AdminParticipantResult> participants() {
            return participants;
        }

        /**
         * Tries to parse a {@link SuccessMultiAdmin} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsPromoteDemoteAdminResponseSuccessMultiAdmin",
                exports = "parsePromoteDemoteAdminResponseSuccessMultiAdmin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessMultiAdmin> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var admin = node.getChild("admin").orElse(null);
            if (admin == null) {
                return Optional.empty();
            }
            var addressingMode = node.getAttributeAsString("addressing_mode").orElse(null);
            var participants = new ArrayList<AdminParticipantResult>();
            for (var participantNode : admin.getChildren("participant")) {
                var participant = AdminParticipantResult.of(participantNode).orElse(null);
                if (participant == null) {
                    return Optional.empty();
                }
                participants.add(participant);
            }
            return Optional.of(new SuccessMultiAdmin(addressingMode, participants));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessMultiAdmin) obj;
            return Objects.equals(this.addressingMode, that.addressingMode)
                    && Objects.equals(this.participants, that.participants);
        }

        @Override
        public int hashCode() {
            return Objects.hash(addressingMode, participants);
        }

        @Override
        public String toString() {
            return "SmaxGroupsPromoteDemoteAdminResponse.SuccessMultiAdmin[addressingMode=" + addressingMode
                    + ", participants=" + participants + ']';
        }

        /**
         * The per-participant outcome entry for a single admin
         * roster change.
         *
         * <p>Successful entries carry an optional
         * {@code type="admin"} marker; rejected entries carry the
         * {@code error} attribute lifted from the
         * {@code ENUM_403_404_406_419} projection.
         */
        public static final class AdminParticipantResult {
            /**
             * The participant JID.
             */
            private final Jid jid;

            /**
             * The optional {@code type} marker — {@code "admin"} on
             * confirmed promotions, absent on demotions and on
             * rejected entries.
             */
            private final String type;

            /**
             * The optional rejection-error code; {@code null} on
             * success.
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
            public AdminParticipantResult(Jid jid, String type, Integer errorCode,
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
             * @return an {@link Optional} carrying the marker
             */
            public Optional<String> type() {
                return Optional.ofNullable(type);
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
            @WhatsAppWebExport(moduleName = "WASmaxInGroupsPromoteDemoteAdminResponseSuccessMultiAdmin",
                    exports = "parsePromoteDemoteAdminResponseSuccessMultiAdminAdminParticipant",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<AdminParticipantResult> of(Node node) {
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
                return Optional.of(new AdminParticipantResult(jid, type, errorCode,
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
                var that = (AdminParticipantResult) obj;
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
                return "SmaxGroupsPromoteDemoteAdminResponse.SuccessMultiAdmin.AdminParticipantResult[jid=" + jid
                        + ", type=" + type
                        + ", errorCode=" + errorCode
                        + ", phoneNumber=" + phoneNumber
                        + ", username=" + username + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsPromoteDemoteAdminResponseClientError")
    final class ClientError implements SmaxGroupsPromoteDemoteAdminResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsPromoteDemoteAdminResponseClientError",
                exports = "parsePromoteDemoteAdminResponseClientError",
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
            return "SmaxGroupsPromoteDemoteAdminResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsPromoteDemoteAdminResponseServerError")
    final class ServerError implements SmaxGroupsPromoteDemoteAdminResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsPromoteDemoteAdminResponseServerError",
                exports = "parsePromoteDemoteAdminResponseServerError",
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
            return "SmaxGroupsPromoteDemoteAdminResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
