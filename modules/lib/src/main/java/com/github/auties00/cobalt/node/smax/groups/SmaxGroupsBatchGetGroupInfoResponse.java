package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
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
 * response to a {@link SmaxGroupsBatchGetGroupInfoRequest}.
 *
 * @implNote {@code WASmaxGroupsBatchGetGroupInfoRPC.sendBatchGetGroupInfoRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError} in order and throws on no-match;
 *           Cobalt returns {@link Optional#empty()} instead.
 */
public sealed interface SmaxGroupsBatchGetGroupInfoResponse extends SmaxOperation.Response
        permits SmaxGroupsBatchGetGroupInfoResponse.Success, SmaxGroupsBatchGetGroupInfoResponse.ClientError, SmaxGroupsBatchGetGroupInfoResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsBatchGetGroupInfoResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxGroupsBatchGetGroupInfoRPC",
            exports = "sendBatchGetGroupInfoRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsBatchGetGroupInfoResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — carries a {@code <groups>}
     * wrapper with one {@code <group/>} child per requested group.
     *
     * <p>Each {@code <group/>} child is one of four sub-shapes
     * (full {@code group_info}, truncated {@code group_info},
     * {@code group_forbidden} marker, or {@code group_not_exist}
     * marker); callers dispatch on the child's structure via
     * standard {@link Node} accessors. The wrapper is exposed as an
     * unmodifiable {@link List} of raw {@link Node}s, mirroring the
     * precedent from
     * {@link SmaxGroupsGetParticipatingGroupsResponse.Success}.
     *
     * @implNote {@code WASmaxInGroupsBatchGetGroupInfoResponseSuccess.parseBaseGetGroupInfoResponseSuccess}
     *           validates the IQ envelope, then projects every
     *           {@code <group>} child via
     *           {@code WASmaxInGroupsGroupInfoOrTruncatedGroupInfoOrGroupForbiddenOrGroupNotExistMixinGroup}.
     *           Cobalt keeps the children as raw {@link Node}s.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsBatchGetGroupInfoResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupInfoOrTruncatedGroupInfoOrGroupForbiddenOrGroupNotExistMixinGroup")
    final class Success implements SmaxGroupsBatchGetGroupInfoResponse {
        /**
         * The list of {@code <group/>} children carried by the
         * {@code <groups>} wrapper.
         */
        private final List<Node> groups;

        /**
         * Constructs a new successful reply.
         *
         * @param groups the per-group reply nodes; never
         *               {@code null}
         * @throws NullPointerException if {@code groups} is
         *                              {@code null}
         */
        public Success(List<Node> groups) {
            Objects.requireNonNull(groups, "groups cannot be null");
            this.groups = List.copyOf(groups);
        }

        /**
         * Returns the list of per-group reply nodes.
         *
         * @return an unmodifiable list of {@code <group/>} nodes;
         *         never {@code null}
         */
        public List<Node> groups() {
            return groups;
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsBatchGetGroupInfoResponseSuccess",
                exports = "parseBatchGetGroupInfoResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var groupsWrapper = node.getChild("groups").orElse(null);
            if (groupsWrapper == null) {
                return Optional.empty();
            }
            var groups = groupsWrapper.streamChildren("group").toList();
            if (groups.isEmpty()) {
                return Optional.empty();
            }
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
            return "SmaxGroupsBatchGetGroupInfoResponse.Success[groups=" + groups + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed or unauthorised.
     *
     * @implNote {@code WASmaxInGroupsBatchGetGroupInfoResponseClientError.parseBatchGetGroupInfoResponseClientError}
     *           parses the {@code <error code text/>} child via the
     *           shared base mixin; Cobalt collapses to the raw
     *           {@code (code, text)} pair via
     *           {@link SmaxBaseServerErrorMixin#parseClientError(Node, Node)}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsBatchGetGroupInfoResponseClientError")
    final class ClientError implements SmaxGroupsBatchGetGroupInfoResponse {
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
         * Tries to parse a {@link ClientError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsBatchGetGroupInfoResponseClientError",
                exports = "parseBatchGetGroupInfoResponseClientError",
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
            return "SmaxGroupsBatchGetGroupInfoResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered
     * a transient internal failure while processing the request.
     *
     * @implNote {@code WASmaxInGroupsBatchGetGroupInfoResponseServerError.parseBatchGetGroupInfoResponseServerError}
     *           delegates to {@code WASmaxInGroupsBaseServerErrorMixin}
     *           which Cobalt has consolidated under
     *           {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsBatchGetGroupInfoResponseServerError")
    final class ServerError implements SmaxGroupsBatchGetGroupInfoResponse {
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
         * Tries to parse a {@link ServerError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsBatchGetGroupInfoResponseServerError",
                exports = "parseBatchGetGroupInfoResponseServerError",
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
            return "SmaxGroupsBatchGetGroupInfoResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
