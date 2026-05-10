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
 */
public sealed interface SmaxGetAccountNonceResponse extends SmaxOperation.Response
        permits SmaxGetAccountNonceResponse.Success, SmaxGetAccountNonceResponse.ClientError, SmaxGetAccountNonceResponse.ServerError {

    /**
     * Tries each {@link SmaxGetAccountNonceResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza. Used to validate
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
     * The {@code Success} reply variant. The relay issued an
     * account-nonce.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingGetAccountNonceResponseSuccess")
    final class Success implements SmaxGetAccountNonceResponse {
        /**
         * The element-content of the {@code <nonce>} child of
         * {@code <detail>}. The freshly-issued account-binding nonce.
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingGetAccountNonceResponseSuccess",
                exports = "parseGetAccountNonceResponseSuccessDetailRequest",
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
     * The {@code ClientError} reply variant. The relay rejected the
     * request with a {@code 4xx} error code.
     *
     * <p>Carries the parsed {@code (code, text)} pair plus the
     * optional {@code tos_version} that only the {@code 475}
     * {@code notice-required} sub-variant surfaces.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingGetAccountNonceResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingAccountNonceErrors")
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingIQErrorNoticeRequiredMixin")
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
         * The {@code tos_version} carried only by the
         * {@code notice-required} sub-variant ({@code 475}); may be
         * {@code null} for the {@code bad-request} arm.
         */
        private final Integer tosVersion;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode  the numeric error code
         * @param errorText  the optional human-readable text; may be
         *                   {@code null}
         * @param tosVersion the optional {@code tos_version}; may be
         *                   {@code null} when the error is not
         *                   {@code notice-required}
         */
        public ClientError(int errorCode, String errorText, Integer tosVersion) {
            this.errorCode = errorCode;
            this.errorText = errorText;
            this.tosVersion = tosVersion;
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
         * Returns the optional {@code tos_version}, present only on
         * the {@code (475, "notice-required")} sub-variant.
         *
         * @return an {@link Optional} carrying the version, or empty
         *         when the error is not {@code notice-required}
         */
        public Optional<Integer> tosVersion() {
            return Optional.ofNullable(tosVersion);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the given
         * inbound stanza.
         *
         * <p>Validates the {@code <iq>} envelope and extracts the
         * {@code (code, text)} pair via the shared 4xx envelope
         * helper, then validates the literal pair against the
         * documented disjunction
         * ({@code (400, "bad-request")} or
         * {@code (475, "notice-required")}). For the
         * {@code notice-required} arm the {@code <error/>} child's
         * {@code tos_version} attribute is projected through the
         * {@code [1, 65535]} range check before being surfaced.
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingAccountNonceErrors",
                exports = "parseAccountNonceErrors",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingIQErrorNoticeRequiredMixin",
                exports = "parseIQErrorNoticeRequiredMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            var code = envelope.code();
            var text = envelope.text();
            // Validate the (code, text) pair against the documented disjunction.
            Integer tosVersion = null;
            if (code == 400 && "bad-request".equals(text)) {
                // IQErrorBadRequestMixin: text="bad-request", code=400.
            } else if (code == 475 && "notice-required".equals(text)) {
                // IQErrorNoticeRequiredMixin: text="notice-required", code=475,
                // plus tos_version in [1, 65535] on the <error/> child.
                var errorChild = node.getChild("error").orElse(null);
                if (errorChild == null) {
                    return Optional.empty();
                }
                var tos = errorChild.getAttributeAsInt("tos_version");
                if (tos.isEmpty()) {
                    return Optional.empty();
                }
                var tosValue = tos.getAsInt();
                if (tosValue < 1 || tosValue > 65535) {
                    return Optional.empty();
                }
                tosVersion = tosValue;
            } else {
                // Unknown (code, text) pair; not a documented BizLinking variant.
                return Optional.empty();
            }
            return Optional.of(new ClientError(code, text, tosVersion));
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText)
                    && Objects.equals(this.tosVersion, that.tosVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText, tosVersion);
        }

        @Override
        public String toString() {
            return "SmaxGetAccountNonceResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText
                    + ", tosVersion=" + tosVersion + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure ({@code 5xx}) while processing the
     * request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingGetAccountNonceResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingAccountNonceErrors")
    @WhatsAppWebModule(moduleName = "WASmaxInBizLinkingIQErrorInternalServerErrorMixin")
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingGetAccountNonceResponseError",
                exports = "parseGetAccountNonceResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingAccountNonceErrors",
                exports = "parseAccountNonceErrors", adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingIQErrorInternalServerErrorMixin",
                exports = "parseIQErrorInternalServerErrorMixin",
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
            return "SmaxGetAccountNonceResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
