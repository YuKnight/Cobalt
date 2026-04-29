package com.github.auties00.cobalt.node.smax.biz;

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
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxUpdatePreferenceRequest}.
 *
 * @implNote {@code WASmaxBizMsgUserFeedbackUpdatePreferenceRPC.sendUpdatePreferenceRPC}
 *           tries {@code Success} → {@code InvalidRequest} →
 *           {@code ServerError} parsers in order. The
 *           {@code InvalidRequest} arm carries the {@code 4xx}-code
 *           subset
 *           ({@code NotAcceptable} / {@code BadRequest} /
 *           {@code Forbidden} / {@code RateOverlimit}); Cobalt
 *           collapses it under the standard
 *           {@code ClientError} name to mirror the rest of the SMAX
 *           surface.
 */
public sealed interface SmaxUpdatePreferenceResponse extends SmaxOperation.Response
        permits SmaxUpdatePreferenceResponse.Success, SmaxUpdatePreferenceResponse.ClientError, SmaxUpdatePreferenceResponse.ServerError {

    /**
     * Tries each {@link SmaxUpdatePreferenceResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxBizMsgUserFeedbackUpdatePreferenceRPC",
            exports = "sendUpdatePreferenceRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxUpdatePreferenceResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — the relay accepted the
     * preference update.
     *
     * @implNote {@code WASmaxInBizMsgUserFeedbackUpdatePreferenceResponseSuccess.parseUpdatePreferenceResponseSuccess}
     *           validates the {@code <iq from id type="result">}
     *           envelope and projects nothing else.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceResponseSuccess")
    final class Success implements SmaxUpdatePreferenceResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceResponseSuccess",
                exports = "parseUpdatePreferenceResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
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
            return "SmaxUpdatePreferenceResponse.Success[]";
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, rate-limited, or
     * not-acceptable for the active user.
     *
     * @implNote {@code WASmaxInBizMsgUserFeedbackUpdatePreferenceResponseInvalidRequest.parseUpdatePreferenceResponseInvalidRequest}
     *           routes the {@code <error/>} child through
     *           {@code WASmaxInBizMsgUserFeedbackUpdatePreferenceReqErrors}
     *           which collapses {@code NotAcceptable},
     *           {@code BadRequest}, {@code Forbidden}, and
     *           {@code RateOverlimit} into a single tagged
     *           disjunction. Cobalt collapses to the raw
     *           {@code (code, text)} pair via the shared
     *           {@link SmaxBaseServerErrorMixin#parseClientError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceResponseInvalidRequest")
    @WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceReqErrors")
    final class ClientError implements SmaxUpdatePreferenceResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceResponseInvalidRequest",
                exports = "parseUpdatePreferenceResponseInvalidRequest",
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
            return "SmaxUpdatePreferenceResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure ({@code 5xx}) while processing the
     * request.
     *
     * @implNote {@code WASmaxInBizMsgUserFeedbackUpdatePreferenceResponseServerError.parseUpdatePreferenceResponseServerError}
     *           routes through
     *           {@code WASmaxInBizMsgUserFeedbackUpdatePreferenceServerErrors}.
     *           Cobalt routes through the shared
     *           {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceResponseServerError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceServerErrors")
    final class ServerError implements SmaxUpdatePreferenceResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceResponseServerError",
                exports = "parseUpdatePreferenceResponseServerError",
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
            return "SmaxUpdatePreferenceResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
