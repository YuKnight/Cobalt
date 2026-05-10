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
 * response to a {@link SmaxRequestSilentNonceRequest}.
 */
public sealed interface SmaxRequestSilentNonceResponse extends SmaxOperation.Response
        permits SmaxRequestSilentNonceResponse.Success, SmaxRequestSilentNonceResponse.RecoveryRequired,
        SmaxRequestSilentNonceResponse.ClientError, SmaxRequestSilentNonceResponse.ServerError {

    /**
     * Tries each {@link SmaxRequestSilentNonceResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxBizAccessTokenRequestSilentNonceRPC",
            exports = "sendRequestSilentNonceRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxRequestSilentNonceResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var recoveryRequired = RecoveryRequired.of(node, request);
        if (recoveryRequired.isPresent()) {
            return recoveryRequired;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant. The relay supplied a silent
     * nonce token directly without requiring an account-recovery
     * confirmation step.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenHackBaseIQResultResponseMixin")
    final class Success implements SmaxRequestSilentNonceResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceResponseSuccess",
                exports = "parseRequestSilentNonceResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenHackBaseIQResultResponseMixin",
                exports = "parseHackBaseIQResultResponseMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var result = node.getChild("result").orElse(null);
            if (result == null) {
                return Optional.empty();
            }
            if (!result.hasAttribute("status", "Success")) {
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
            return "SmaxRequestSilentNonceResponse.Success[]";
        }
    }

    /**
     * The {@code RecoveryRequired} reply variant. The relay refused
     * to issue a silent nonce because the user must first confirm
     * account ownership via an email recovery code.
     *
     * <p>Carries the email address the relay sent the recovery code
     * to, so the UI can route the user toward the correct inbox.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceResponseRecoveryRequired")
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenHackBaseIQResultResponseMixin")
    final class RecoveryRequired implements SmaxRequestSilentNonceResponse {
        /**
         * The email address the relay sent the recovery code to.
         */
        private final String email;

        /**
         * Constructs a new recovery-required reply.
         *
         * @param email the email address the recovery code was
         *              dispatched to; never {@code null}
         * @throws NullPointerException if {@code email} is
         *                              {@code null}
         */
        public RecoveryRequired(String email) {
            this.email = Objects.requireNonNull(email, "email cannot be null");
        }

        /**
         * Returns the recovery-code email address.
         *
         * @return the email address; never {@code null}
         */
        public String email() {
            return email;
        }

        /**
         * Tries to parse a {@link RecoveryRequired} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         recovery-required schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceResponseRecoveryRequired",
                exports = "parseRequestSilentNonceResponseRecoveryRequired",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenHackBaseIQResultResponseMixin",
                exports = "parseHackBaseIQResultResponseMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<RecoveryRequired> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var result = node.getChild("result").orElse(null);
            if (result == null) {
                return Optional.empty();
            }
            if (!result.hasAttribute("status", "RecoveryRequired")) {
                return Optional.empty();
            }
            var email = result.getAttributeAsString("email").orElse(null);
            if (email == null) {
                return Optional.empty();
            }
            return Optional.of(new RecoveryRequired(email));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (RecoveryRequired) obj;
            return Objects.equals(this.email, that.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email);
        }

        @Override
        public String toString() {
            return "SmaxRequestSilentNonceResponse.RecoveryRequired[email=" + email + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request as malformed, unauthorised, or with a transient
     * client-recoverable code.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceErrors")
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenIQErrorBadRequestMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenIQErrorForbiddenMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenHackBaseIQErrorResponseMixin")
    final class ClientError implements SmaxRequestSilentNonceResponse {
        /**
         * The numeric server-side error code ({@code 400} or
         * {@code 403}).
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceResponseError",
                exports = "parseRequestSilentNonceResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceErrors",
                exports = "parseRequestSilentNonceErrors",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenIQErrorBadRequestMixin",
                exports = "parseIQErrorBadRequestMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenIQErrorForbiddenMixin",
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
            return "SmaxRequestSilentNonceResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure ({@code 500} / {@code 503}) while
     * processing the request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceErrors")
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenIQErrorInternalServerErrorMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenIQErrorServiceUnavailableMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenHackBaseIQErrorResponseMixin")
    final class ServerError implements SmaxRequestSilentNonceResponse {
        /**
         * The numeric server-side error code ({@code 500} or
         * {@code 503}).
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceResponseError",
                exports = "parseRequestSilentNonceResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenRequestSilentNonceErrors",
                exports = "parseRequestSilentNonceErrors",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenIQErrorInternalServerErrorMixin",
                exports = "parseIQErrorInternalServerErrorMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenIQErrorServiceUnavailableMixin",
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
            return "SmaxRequestSilentNonceResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
