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
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGroupsJoinLinkedGroupRequest}.
 *
 * <p>{@link GroupJoinRequestSuccess} is tried before {@link Success}
 * because both share the same envelope shape — the
 * {@code <membership_approval_request/>} marker is the only
 * discriminator.
 */
public sealed interface SmaxGroupsJoinLinkedGroupResponse extends SmaxOperation.Response
        permits SmaxGroupsJoinLinkedGroupResponse.GroupJoinRequestSuccess, SmaxGroupsJoinLinkedGroupResponse.Success,
                SmaxGroupsJoinLinkedGroupResponse.ClientError, SmaxGroupsJoinLinkedGroupResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsJoinLinkedGroupResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxGroupsJoinLinkedGroupRPC",
            exports = "sendJoinLinkedGroupRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsJoinLinkedGroupResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var groupJoinRequestSuccess = GroupJoinRequestSuccess.of(node, request);
        if (groupJoinRequestSuccess.isPresent()) {
            return groupJoinRequestSuccess;
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
     * has accepted the join request but the sub-group has membership
     * approval enabled, so the caller is not yet a participant.
     *
     * <p>The {@code <membership_approval_request/>} marker is the
     * sole discriminator versus the plain {@link Success}; no
     * additional payload is exposed because the marker carries no
     * attributes.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsJoinLinkedGroupResponseGroupJoinRequestSuccess")
    final class GroupJoinRequestSuccess implements SmaxGroupsJoinLinkedGroupResponse {
        /**
         * Constructs a new join-request-success reply.
         */
        public GroupJoinRequestSuccess() {
        }

        /**
         * Tries to parse a {@link GroupJoinRequestSuccess} variant
         * from the given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         join-request-success schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsJoinLinkedGroupResponseGroupJoinRequestSuccess",
                exports = "parseJoinLinkedGroupResponseGroupJoinRequestSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<GroupJoinRequestSuccess> of(Node node, Node request) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (node.getChild("membership_approval_request").isEmpty()) {
                return Optional.empty();
            }
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
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
            return "SmaxGroupsJoinLinkedGroupResponse.GroupJoinRequestSuccess[]";
        }
    }

    /**
     * The {@code Success} reply variant — the caller has been admitted
     * to the sub-group directly without going through membership
     * approval.
     *
     * <p>Carries no payload beyond the envelope echo.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsJoinLinkedGroupResponseSuccess")
    final class Success implements SmaxGroupsJoinLinkedGroupResponse {
        /**
         * Constructs a new successful reply.
         */
        public Success() {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsJoinLinkedGroupResponseSuccess",
                exports = "parseJoinLinkedGroupResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
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
            return "SmaxGroupsJoinLinkedGroupResponse.Success[]";
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, or referencing a
     * non-existent or unjoinable sub-group.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsJoinLinkedGroupResponseClientError")
    final class ClientError implements SmaxGroupsJoinLinkedGroupResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
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
         * Tries to parse a {@link ClientError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsJoinLinkedGroupResponseClientError",
                exports = "parseJoinLinkedGroupResponseClientError",
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
            return "SmaxGroupsJoinLinkedGroupResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsJoinLinkedGroupResponseServerError")
    final class ServerError implements SmaxGroupsJoinLinkedGroupResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
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
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsJoinLinkedGroupResponseServerError",
                exports = "parseJoinLinkedGroupResponseServerError",
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
            return "SmaxGroupsJoinLinkedGroupResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
