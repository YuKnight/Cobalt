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
 * @implNote {@code WASmaxGroupsGetLinkedGroupsParticipantsRPC.sendGetLinkedGroupsParticipantsRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError} in order and throws on no-match;
 *           Cobalt returns {@link Optional#empty()} instead.
 */
public sealed interface SmaxGroupsGetLinkedGroupsParticipantsResponse extends SmaxOperation.Response
        permits SmaxGroupsGetLinkedGroupsParticipantsResponse.Success, SmaxGroupsGetLinkedGroupsParticipantsResponse.ClientError, SmaxGroupsGetLinkedGroupsParticipantsResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsGetLinkedGroupsParticipantsResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsGetLinkedGroupsParticipantsRPC",
            exports = "sendGetLinkedGroupsParticipantsRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsGetLinkedGroupsParticipantsResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — carries the union of
     * participants across the addressed community's sub-groups.
     *
     * @implNote {@code WASmaxInGroupsGetLinkedGroupsParticipantsResponseSuccess.parseGetLinkedGroupsParticipantsResponseSuccess}
     *           validates the IQ envelope, then projects every
     *           {@code <participant>} child via
     *           {@code WASmaxInGroupsParticipantWithJidMixin} +
     *           {@code WASmaxInGroupsPhoneNumberMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetLinkedGroupsParticipantsResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsParticipantWithJidMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsPhoneNumberMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupAddressingModeMixin")
    final class Success implements SmaxGroupsGetLinkedGroupsParticipantsResponse {
        /**
         * The list of participants spanning the addressed
         * community's sub-groups.
         */
        private final List<Participant> participants;

        /**
         * Constructs a new successful reply.
         *
         * @param participants the participant list; never
         *                     {@code null}
         * @throws NullPointerException if {@code participants} is
         *                              {@code null}
         */
        public Success(List<Participant> participants) {
            Objects.requireNonNull(participants, "participants cannot be null");
            this.participants = List.copyOf(participants);
        }

        /**
         * Returns the participant list.
         *
         * @return an unmodifiable list of participants; never
         *         {@code null}
         */
        public List<Participant> participants() {
            return participants;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetLinkedGroupsParticipantsResponseSuccess",
                exports = "parseGetLinkedGroupsParticipantsResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var wrapper = node.getChild("linked_groups_participants").orElse(null);
            if (wrapper == null) {
                return Optional.empty();
            }
            var participantNodes = wrapper.getChildren("participant");
            var participants = new ArrayList<Participant>(participantNodes.size());
            for (var participantNode : participantNodes) {
                var participant = Participant.of(participantNode).orElse(null);
                if (participant == null) {
                    return Optional.empty();
                }
                participants.add(participant);
            }
            return Optional.of(new Success(participants));
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
            return Objects.equals(this.participants, that.participants);
        }

        @Override
        public int hashCode() {
            return Objects.hash(participants);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetLinkedGroupsParticipantsResponse.Success[participants="
                    + participants + ']';
        }
    }

    /**
     * Per-participant projection — carries the addressing JID and
     * the optional phone-number JID surfaced by the relay's PN-LID
     * mapping.
     *
     * @implNote {@code parseGetLinkedGroupsParticipantsResponseSuccessLinkedGroupsParticipantsParticipant}
     *           composes
     *           {@code WASmaxInGroupsParticipantWithJidMixin}
     *           ({@code jid}) with the optional
     *           {@code WASmaxInGroupsPhoneNumberMixin}
     *           ({@code phone_number}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsParticipantWithJidMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsPhoneNumberMixin")
    final class Participant {
        /**
         * The participant's primary JID.
         */
        private final Jid jid;

        /**
         * The participant's resolved phone-number JID, when the
         * relay supplied the PN-LID mapping.
         */
        private final Jid phoneNumber;

        /**
         * Constructs a participant.
         *
         * @param jid         the participant JID; never
         *                    {@code null}
         * @param phoneNumber the optional phone-number JID; may be
         *                    {@code null}
         * @throws NullPointerException if {@code jid} is
         *                              {@code null}
         */
        public Participant(Jid jid, Jid phoneNumber) {
            this.jid = Objects.requireNonNull(jid, "jid cannot be null");
            this.phoneNumber = phoneNumber;
        }

        /**
         * Returns the participant's primary JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid jid() {
            return jid;
        }

        /**
         * Returns the participant's phone-number JID when supplied
         * by the relay.
         *
         * @return an {@link Optional} carrying the phone-number JID
         */
        public Optional<Jid> phoneNumber() {
            return Optional.ofNullable(phoneNumber);
        }

        /**
         * Tries to parse a {@link Participant} from the given
         * {@code <participant/>} child.
         *
         * @param node the {@code <participant/>} child node
         * @return an {@link Optional} carrying the parsed
         *         participant, or empty when the child does not
         *         carry a {@code jid} attribute
         */
        public static Optional<Participant> of(Node node) {
            Objects.requireNonNull(node, "node cannot be null");
            if (!node.hasDescription("participant")) {
                return Optional.empty();
            }
            var jid = node.getAttributeAsJid("jid").orElse(null);
            if (jid == null) {
                return Optional.empty();
            }
            var phoneNumber = node.getAttributeAsJid("phone_number").orElse(null);
            return Optional.of(new Participant(jid, phoneNumber));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Participant) obj;
            return Objects.equals(this.jid, that.jid)
                    && Objects.equals(this.phoneNumber, that.phoneNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid, phoneNumber);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetLinkedGroupsParticipantsResponse.Participant[jid=" + jid
                    + ", phoneNumber=" + phoneNumber + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     *
     * @implNote {@code WASmaxInGroupsGetLinkedGroupsParticipantsResponseClientError.parseGetLinkedGroupsParticipantsResponseClientError}
     *           parses the {@code <error code text/>} child via the
     *           shared base mixin.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetLinkedGroupsParticipantsResponseClientError")
    final class ClientError implements SmaxGroupsGetLinkedGroupsParticipantsResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetLinkedGroupsParticipantsResponseClientError",
                exports = "parseGetLinkedGroupsParticipantsResponseClientError",
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
            return "SmaxGroupsGetLinkedGroupsParticipantsResponse.ClientError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     *
     * @implNote {@code WASmaxInGroupsGetLinkedGroupsParticipantsResponseServerError.parseGetLinkedGroupsParticipantsResponseServerError}
     *           delegates to the shared base mixin.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetLinkedGroupsParticipantsResponseServerError")
    final class ServerError implements SmaxGroupsGetLinkedGroupsParticipantsResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetLinkedGroupsParticipantsResponseServerError",
                exports = "parseGetLinkedGroupsParticipantsResponseServerError",
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
            return "SmaxGroupsGetLinkedGroupsParticipantsResponse.ServerError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }
}
