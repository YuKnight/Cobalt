package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGetAccountNonceRequest}.
 *
 * @implNote {@code WASmaxBizLinkingGetAccountNonceRPC.sendGetAccountNonceRPC}
 *           tries {@code Success} → {@code Error} in order. Cobalt
 *           collapses the {@code Error} arm into the
 *           {@code ClientError}/{@code ServerError} pair via the shared
 *           {@link SmaxBaseServerErrorMixin} helpers.
 */
public sealed interface SmaxGetAccountNonceResponse extends SmaxOperation.Response
        permits SmaxGetAccountNonceResponse.Success, SmaxGetAccountNonceResponse.ClientError, SmaxGetAccountNonceResponse.ServerError {

    /**
     * Tries each {@link SmaxGetAccountNonceResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxBizLinkingGetAccountNonceRPC",
            exports = "sendGetAccountNonceRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGetAccountNonceResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — the relay issued an
     * account-nonce.
     *
     * @implNote {@code WASmaxInBizLinkingGetAccountNonceResponseSuccess.parseGetAccountNonceResponseSuccess}
     *           validates the {@code <iq from id type="result">}
     *           envelope, descends into the mandatory {@code <detail>}
     *           child, asserts the mandatory {@code <nonce>}
     *           grandchild and reads its element-content, then
     *           optionally projects the
     *           {@code <request><id/></request>} echo.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingGetAccountNonceResponseSuccess")
    final class Success implements SmaxGetAccountNonceResponse {
        /**
         * The element-content of the {@code <nonce>} child of
         * {@code <detail>} — the freshly-issued account-binding nonce.
         */
        private final String nonce;

        /**
         * The optional element-content of the
         * {@code <request><id/></request>} echo; {@code null} when
         * the relay omitted the {@code <request>} grandchild.
         */
        private final String requestId;

        /**
         * Constructs a new successful reply.
         *
         * @param nonce     the issued nonce; never {@code null}
         * @param requestId the optional request-id echo; may be
         *                  {@code null}
         * @throws NullPointerException if {@code nonce} is
         *                              {@code null}
         */
        public Success(String nonce, String requestId) {
            this.nonce = Objects.requireNonNull(nonce, "nonce cannot be null");
            this.requestId = requestId;
        }

        /**
         * Returns the issued nonce.
         *
         * @return the nonce; never {@code null}
         */
        public String nonce() {
            return nonce;
        }

        /**
         * Returns the optional request-id echo.
         *
         * @return an {@link Optional} carrying the id, or empty when
         *         the relay omitted the {@code <request>} grandchild
         */
        public Optional<String> requestId() {
            return Optional.ofNullable(requestId);
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingGetAccountNonceResponseSuccess",
                exports = "parseGetAccountNonceResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var detail = node.getChild("detail").orElse(null);
            if (detail == null) {
                return Optional.empty();
            }
            var nonceNode = detail.getChild("nonce").orElse(null);
            if (nonceNode == null) {
                return Optional.empty();
            }
            var nonce = nonceNode.toContentString().orElse(null);
            if (nonce == null) {
                return Optional.empty();
            }
            String requestId = null;
            var requestNode = detail.getChild("request").orElse(null);
            if (requestNode != null) {
                var idNode = requestNode.getChild("id").orElse(null);
                if (idNode == null) {
                    return Optional.empty();
                }
                requestId = idNode.toContentString().orElse(null);
                if (requestId == null) {
                    return Optional.empty();
                }
            }
            return Optional.of(new Success(nonce, requestId));
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
            return Objects.equals(this.nonce, that.nonce)
                    && Objects.equals(this.requestId, that.requestId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nonce, requestId);
        }

        @Override
        public String toString() {
            return "SmaxGetAccountNonceResponse.Success[nonce=" + nonce
                    + ", requestId=" + requestId + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request with a {@code 4xx} error code.
     *
     * @implNote {@code WASmaxInBizLinkingGetAccountNonceResponseError.parseGetAccountNonceResponseError}
     *           routes the {@code <error/>} child through
     *           {@code WASmaxInBizLinkingAccountNonceErrors};
     *           Cobalt collapses to the raw {@code (code, text)} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingGetAccountNonceResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingAccountNonceErrors")
    final class ClientError implements SmaxGetAccountNonceResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingGetAccountNonceResponseError",
                exports = "parseGetAccountNonceResponseError",
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
            return "SmaxGetAccountNonceResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure ({@code 5xx}) while processing the
     * request.
     *
     * @implNote Sourced from the {@code 5xx} arms of
     *           {@code WASmaxInBizLinkingAccountNonceErrors}; Cobalt
     *           routes through the shared
     *           {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingGetAccountNonceResponseError")
    final class ServerError implements SmaxGetAccountNonceResponse {
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
            return "SmaxGetAccountNonceResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
