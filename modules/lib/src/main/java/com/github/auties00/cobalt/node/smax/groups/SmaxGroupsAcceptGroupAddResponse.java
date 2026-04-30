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
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxGroupsAcceptGroupAddRPC.sendAcceptGroupAddRPC}
 *           tries {@code GroupJoinRequestSuccess} →
 *           {@code Success} → {@code ClientError} →
 *           {@code ServerError}.
 */
public sealed interface SmaxGroupsAcceptGroupAddResponse extends SmaxOperation.Response
        permits SmaxGroupsAcceptGroupAddResponse.GroupJoinRequestSuccess, SmaxGroupsAcceptGroupAddResponse.Success,
        SmaxGroupsAcceptGroupAddResponse.ClientError, SmaxGroupsAcceptGroupAddResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsAcceptGroupAddResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no variant matched
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsAcceptGroupAddRPC",
            exports = "sendAcceptGroupAddRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsAcceptGroupAddResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var groupJoin = GroupJoinRequestSuccess.of(node, request);
        if (groupJoin.isPresent()) {
            return groupJoin;
        }
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
     * The {@code GroupJoinRequestSuccess} reply variant — the relay
     * accepted the {@code accept} but the group's
     * membership-approval mode rerouted the caller into the
     * pending-approval queue. Carries the same envelope as
     * {@link Success} plus a {@code <membership_approval_request/>}
     * child marker.
     *
     * @implNote {@code WASmaxInGroupsAcceptGroupAddResponseGroupJoinRequestSuccess.parseAcceptGroupAddResponseGroupJoinRequestSuccess}
     *           validates the IQ-result envelope and asserts the
     *           {@code <membership_approval_request/>} child exists.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsAcceptGroupAddResponseGroupJoinRequestSuccess")
    final class GroupJoinRequestSuccess implements SmaxGroupsAcceptGroupAddResponse {
        /**
         * Constructs a new group-join-pending reply.
         */
        public GroupJoinRequestSuccess() {
        }

        /**
         * Tries to parse a {@link GroupJoinRequestSuccess} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsAcceptGroupAddResponseGroupJoinRequestSuccess",
                exports = "parseAcceptGroupAddResponseGroupJoinRequestSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<GroupJoinRequestSuccess> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            if (node.getChild("membership_approval_request").isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new GroupJoinRequestSuccess());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return GroupJoinRequestSuccess.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxGroupsAcceptGroupAddResponse.GroupJoinRequestSuccess[]";
        }
    }

    /**
     * The {@code Success} reply variant — the relay added the caller
     * to the group as a regular participant.
     *
     * @implNote {@code WASmaxInGroupsAcceptGroupAddResponseSuccess.parseAcceptGroupAddResponseSuccess}
     *           validates the IQ-result envelope only.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsAcceptGroupAddResponseSuccess")
    final class Success implements SmaxGroupsAcceptGroupAddResponse {
        /**
         * Constructs a new successful reply.
         */
        public Success() {
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsAcceptGroupAddResponseSuccess",
                exports = "parseAcceptGroupAddResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            // The membership_approval_request child discriminates GroupJoinRequestSuccess from the plain Success
            if (node.getChild("membership_approval_request").isPresent()) {
                return Optional.empty();
            }
            return Optional.of(new Success());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return Success.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxGroupsAcceptGroupAddResponse.Success[]";
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * accept as malformed, expired, or referencing a non-existent
     * pending request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsAcceptGroupAddResponseClientError")
    final class ClientError implements SmaxGroupsAcceptGroupAddResponse {
        /**
         * The numeric error code.
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
         * @param errorText the optional error text; may be
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
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsAcceptGroupAddResponseClientError",
                exports = "parseAcceptGroupAddResponseClientError",
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
            return "SmaxGroupsAcceptGroupAddResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — transient relay-side
     * failure.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsAcceptGroupAddResponseServerError")
    final class ServerError implements SmaxGroupsAcceptGroupAddResponse {
        /**
         * The numeric error code.
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
         * @param errorText the optional error text; may be
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
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsAcceptGroupAddResponseServerError",
                exports = "parseAcceptGroupAddResponseServerError",
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
            return "SmaxGroupsAcceptGroupAddResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
