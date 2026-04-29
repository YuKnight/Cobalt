package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGroupsGetInviteGroupInfoRequest}.
 *
 * <p>Reply variants are tried in priority order
 * ({@link Success} first, then {@link ClientError}, then
 * {@link ServerError}); {@link #of(Node)} returns the first variant
 * whose schema matches the inbound stanza, or
 * {@link Optional#empty()} when none match.
 *
 * @implNote {@code WASmaxGroupsGetInviteGroupInfoRPC.sendGetInviteGroupInfoRPC}
 *           tries the same priority order and throws
 *           {@code SmaxParsingFailure} on no match. Cobalt returns
 *           {@link Optional#empty()} instead so callers can decide
 *           whether to throw via {@code .orElseThrow(...)} or branch
 *           via pattern matching.
 */
public sealed interface SmaxGroupsGetInviteGroupInfoResponse extends SmaxOperation.Response
        permits SmaxGroupsGetInviteGroupInfoResponse.Success, SmaxGroupsGetInviteGroupInfoResponse.ClientError, SmaxGroupsGetInviteGroupInfoResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsGetInviteGroupInfoResponse} variant in priority order and returns
     * the first that parses cleanly.
     *
     * @param node the inbound IQ stanza received from the relay; never
     *             {@code null}
     * @return an {@link Optional} containing the parsed variant, or
     *         {@link Optional#empty()} when none of the documented
     *         variants matched the stanza shape
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsGetInviteGroupInfoRPC",
            exports = "sendGetInviteGroupInfoRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsGetInviteGroupInfoResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        var success = Success.of(node);
        if (success.isPresent()) {
            return success;
        }
        var clientError = ClientError.of(node);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node);
    }

    /**
     * The {@code Success} reply variant — the relay accepted the
     * invite code and returned the group's preview metadata.
     *
     * <p>Carries the group size (clamped server-side to
     * {@code [0, 19999]}) plus the opaque {@code <group>} payload that
     * encodes the rest of the {@code InviteLinkGroupInfoMixin} schema
     * (subject, picture, owner, admin list, etc.). The {@code <group>}
     * sub-node is exposed verbatim so callers can drive their own
     * projection without committing this class to the full mixin
     * schema; the typical caller will read the standard child
     * attributes via {@link Node#getAttributeAsString(String)}.
     *
     * @implNote {@code WASmaxInGroupsGetInviteGroupInfoResponseSuccess.parseGetInviteGroupInfoResponseSuccess}
     *           validates the {@code from} / {@code id} echoed
     *           attributes against the request reference, asserts
     *           {@code type=result}, then parses the {@code size}
     *           attribute and the
     *           {@code WASmaxInGroupsInviteLinkGroupInfoMixin}. Cobalt
     *           keeps the {@code size} projection (it's the only
     *           scalar callers commonly act on) and exposes the
     *           {@code <group>} node directly for everything else.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetInviteGroupInfoResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsInviteLinkGroupInfoMixin")
    final class Success implements SmaxGroupsGetInviteGroupInfoResponse {
        /**
         * The number of participants reported by the relay (range
         * {@code [0, 19999]}).
         */
        private final int groupSize;

        /**
         * The {@code <group>} child carrying the
         * {@code InviteLinkGroupInfoMixin} subtree (subject, picture,
         * owner, admins, etc.).
         */
        private final Node group;

        /**
         * Constructs a new successful reply.
         *
         * @param groupSize the participant count
         * @param group     the {@code <group>} sub-node; never
         *                  {@code null}
         * @throws NullPointerException     if {@code group} is
         *                                  {@code null}
         * @throws IllegalArgumentException if {@code groupSize} is
         *                                  negative
         */
        public Success(int groupSize, Node group) {
            if (groupSize < 0) {
                throw new IllegalArgumentException("groupSize must be non-negative");
            }
            this.groupSize = groupSize;
            this.group = Objects.requireNonNull(group, "group cannot be null");
        }

        /**
         * Returns the group's participant count.
         *
         * @return the group size
         */
        public int groupSize() {
            return groupSize;
        }

        /**
         * Returns the raw {@code <group>} sub-node carrying the
         * remaining group-info-mixin fields.
         *
         * @return the {@code <group>} node; never {@code null}
         */
        public Node group() {
            return group;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node the inbound IQ stanza
         * @return an {@link Optional} carrying the parsed variant, or
         *         {@link Optional#empty()} when the stanza does not
         *         match the success schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetInviteGroupInfoResponseSuccess",
                exports = "parseGetInviteGroupInfoResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "result")) {
                return Optional.empty();
            }
            var group = node.getChild("group").orElse(null);
            if (group == null) {
                return Optional.empty();
            }
            var size = group.getAttributeAsInt("size").orElse(0);
            var success = new Success(size, group);
            return Optional.of(success);
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
            return this.groupSize == that.groupSize && Objects.equals(this.group, that.group);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupSize, group);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetInviteGroupInfoResponse.Success[groupSize=" + groupSize
                    + ", group=" + group + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, or referencing a
     * non-existent group / revoked invite code.
     *
     * @implNote {@code WASmaxInGroupsGetInviteGroupInfoResponseClientError.parseGetInviteGroupInfoResponseClientError}
     *           parses the {@code <error code text/>} child of the IQ
     *           stanza. Cobalt exposes the parsed {@code code} and
     *           {@code text} as typed accessors; the underlying error
     *           node is preserved for callers that need richer
     *           diagnostics.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetInviteGroupInfoResponseClientError")
    final class ClientError implements SmaxGroupsGetInviteGroupInfoResponse {
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
         * @param node the inbound IQ stanza
         * @return an {@link Optional} carrying the parsed variant, or
         *         {@link Optional#empty()} when the stanza does not
         *         match the client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetInviteGroupInfoResponseClientError",
                exports = "parseGetInviteGroupInfoResponseClientError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "error")) {
                return Optional.empty();
            }
            var error = node.getChild("error").orElse(null);
            if (error == null) {
                return Optional.empty();
            }
            var code = error.getAttributeAsInt("code").orElse(-1);
            if (code < 400 || code >= 500) {
                return Optional.empty();
            }
            var text = error.getAttributeAsString("text").orElse(null);
            var clientError = new ClientError(code, text);
            return Optional.of(clientError);
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
            return "SmaxGroupsGetInviteGroupInfoResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the request. Callers
     * may retry after a backoff.
     *
     * @implNote {@code WASmaxInGroupsGetInviteGroupInfoResponseServerError.parseGetInviteGroupInfoResponseServerError}
     *           parses the {@code <error code text/>} child of the IQ
     *           stanza. Cobalt exposes the parsed {@code code} and
     *           {@code text} as typed accessors.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetInviteGroupInfoResponseServerError")
    final class ServerError implements SmaxGroupsGetInviteGroupInfoResponse {
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
         * @param node the inbound IQ stanza
         * @return an {@link Optional} carrying the parsed variant, or
         *         {@link Optional#empty()} when the stanza does not
         *         match the server-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetInviteGroupInfoResponseServerError",
                exports = "parseGetInviteGroupInfoResponseServerError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "error")) {
                return Optional.empty();
            }
            var error = node.getChild("error").orElse(null);
            if (error == null) {
                return Optional.empty();
            }
            var code = error.getAttributeAsInt("code").orElse(-1);
            if (code < 500) {
                return Optional.empty();
            }
            var text = error.getAttributeAsString("text").orElse(null);
            var serverError = new ServerError(code, text);
            return Optional.of(serverError);
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
            return "SmaxGroupsGetInviteGroupInfoResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
