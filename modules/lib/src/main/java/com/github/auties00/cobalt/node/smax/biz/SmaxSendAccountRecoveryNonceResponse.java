package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxSendAccountRecoveryNonceRequest}.
 */
public sealed interface SmaxSendAccountRecoveryNonceResponse extends SmaxOperation.Response
        permits SmaxSendAccountRecoveryNonceResponse.Success, SmaxSendAccountRecoveryNonceResponse.ClientError, SmaxSendAccountRecoveryNonceResponse.ServerError {

    /**
     * Tries each {@link SmaxSendAccountRecoveryNonceResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxBizCtwaAdAccountSendAccountRecoveryNonceRPC",
            exports = "sendSendAccountRecoveryNonceRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxSendAccountRecoveryNonceResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay accepted the
     * request and tried (or actually managed) to dispatch the
     * recovery email; the embedded {@code status} indicates which.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountSendAccountRecoveryNonceResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountSendAccountRecoveryNonceResponseMixin")
    final class Success implements SmaxSendAccountRecoveryNonceResponse {
        /**
         * The {@code <Result><status>...</status></Result>} content.
         * Either {@link SmaxSendAccountRecoveryNonceStatus#SUCCESS}
         * (recovery email dispatched) or
         * {@link SmaxSendAccountRecoveryNonceStatus#FAIL} (the relay
         * tried and gave up).
         */
        private final SmaxSendAccountRecoveryNonceStatus status;

        /**
         * Constructs a new successful reply.
         *
         * @param status the recovery-dispatch status; never
         *               {@code null}
         * @throws NullPointerException if {@code status} is
         *                              {@code null}
         */
        public Success(SmaxSendAccountRecoveryNonceStatus status) {
            this.status = Objects.requireNonNull(status, "status cannot be null");
        }

        /**
         * Returns the recovery-dispatch status.
         *
         * @return the status; never {@code null}
         */
        public SmaxSendAccountRecoveryNonceStatus status() {
            return status;
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountSendAccountRecoveryNonceResponseSuccess",
                exports = "parseSendAccountRecoveryNonceResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountSendAccountRecoveryNonceResponseMixin",
                exports = "parseSendAccountRecoveryNonceResponseMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var result = node.getChild("Result").orElse(null);
            if (result == null) {
                return Optional.empty();
            }
            var statusNode = result.getChild("status").orElse(null);
            if (statusNode == null) {
                return Optional.empty();
            }
            // WASmaxInBizCtwaAdAccountSendAccountRecoveryNonceResponseMixin.parseSendAccountRecoveryNonceResponseMixin:
            // var r = WASmaxParseUtils.contentStringEnum(n.value, ENUM_FAIL_SUCCESS)
            var statusText = statusNode.toContentString().orElse(null);
            if (statusText == null) {
                return Optional.empty();
            }
            var status = SmaxSendAccountRecoveryNonceStatus.of(statusText).orElse(null);
            if (status == null) {
                return Optional.empty();
            }
            return Optional.of(new Success(status));
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
            return Objects.equals(this.status, that.status);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status);
        }

        @Override
        public String toString() {
            return "SmaxSendAccountRecoveryNonceResponse.Success[status=" + status + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request with a documented common-ad-account error code in the
     * {@code 4xx} range.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountSendAccountRecoveryNonceResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountCommonAdAccountErrors")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountHackBaseIQErrorResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountIQErrorBadRequestMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountIQErrorForbiddenMixin")
    final class ClientError implements SmaxSendAccountRecoveryNonceResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountSendAccountRecoveryNonceResponseError",
                exports = "parseSendAccountRecoveryNonceResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountCommonAdAccountErrors",
                exports = "parseCommonAdAccountErrors",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountIQErrorBadRequestMixin",
                exports = "parseIQErrorBadRequestMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountIQErrorForbiddenMixin",
                exports = "parseIQErrorForbiddenMixin",
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
            return "SmaxSendAccountRecoveryNonceResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure ({@code 5xx}) while processing the
     * request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountSendAccountRecoveryNonceResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountCommonAdAccountErrors")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountHackBaseIQErrorResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountIQErrorInternalServerErrorMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountIQErrorServiceUnavailableMixin")
    final class ServerError implements SmaxSendAccountRecoveryNonceResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountSendAccountRecoveryNonceResponseError",
                exports = "parseSendAccountRecoveryNonceResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountCommonAdAccountErrors",
                exports = "parseCommonAdAccountErrors",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountIQErrorInternalServerErrorMixin",
                exports = "parseIQErrorInternalServerErrorMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountIQErrorServiceUnavailableMixin",
                exports = "parseIQErrorServiceUnavailableMixin",
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
            return "SmaxSendAccountRecoveryNonceResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
