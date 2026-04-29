package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 */
public sealed interface SmaxGroupsGetParticipatingGroupsResponse extends SmaxOperation.Response
        permits SmaxGroupsGetParticipatingGroupsResponse.Success, SmaxGroupsGetParticipatingGroupsResponse.ClientError, SmaxGroupsGetParticipatingGroupsResponse.ServerError {

    /**
     * Tries each variant in priority order.
     *
     * @param node    the inbound IQ stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsGetParticipatingGroupsRPC",
            exports = "sendGetParticipatingGroupsRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsGetParticipatingGroupsResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node);
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
     * The {@code Success} reply variant — carries the
     * {@code <groups>} wrapper with one {@code <group>} child per
     * participating group.
     *
     * @implNote {@code WASmaxInGroupsGetParticipatingGroupsResponseSuccess.parseGetParticipatingGroupsResponseSuccess}
     *           projects every {@code <group>} child via
     *           {@code WASmaxInGroupsGroupInfoOrTruncatedGroupInfoGroupInfoMixinGroup}.
     *           Cobalt exposes the raw {@code <group>} nodes; callers
     *           use {@link Node} accessors to read group attributes.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetParticipatingGroupsResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupInfoOrTruncatedGroupInfoGroupInfoMixinGroup")
    final class Success implements SmaxGroupsGetParticipatingGroupsResponse {
        /**
         * The list of {@code <group>} children carried by the
         * {@code <groups>} wrapper.
         */
        private final List<Node> groups;

        /**
         * Constructs a new successful reply.
         *
         * @param groups the participating-group nodes; never
         *               {@code null}
         */
        public Success(List<Node> groups) {
            this.groups = List.copyOf(Objects.requireNonNullElse(groups, List.of()));
        }

        /**
         * Returns the participating group nodes.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<Node> groups() {
            return groups;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node the inbound IQ stanza
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetParticipatingGroupsResponseSuccess",
                exports = "parseGetParticipatingGroupsResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "result")) {
                return Optional.empty();
            }
            var groupsWrapper = node.getChild("groups").orElse(null);
            if (groupsWrapper == null) {
                return Optional.empty();
            }
            var groups = groupsWrapper.streamChildren("group").toList();
            return Optional.of(new Success(groups));
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
            return Objects.equals(this.groups, that.groups);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groups);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetParticipatingGroupsResponse.Success[groups=" + groups + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetParticipatingGroupsResponseClientError")
    final class ClientError implements SmaxGroupsGetParticipatingGroupsResponse {
        /**
         * The numeric error code.
         */
        private final int errorCode;

        /**
         * The optional error text.
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetParticipatingGroupsResponseClientError",
                exports = "parseGetParticipatingGroupsResponseClientError",
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
            return "SmaxGroupsGetParticipatingGroupsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetParticipatingGroupsResponseServerError")
    final class ServerError implements SmaxGroupsGetParticipatingGroupsResponse {
        /**
         * The numeric error code.
         */
        private final int errorCode;

        /**
         * The optional error text.
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetParticipatingGroupsResponseServerError",
                exports = "parseGetParticipatingGroupsResponseServerError",
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
            return "SmaxGroupsGetParticipatingGroupsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
