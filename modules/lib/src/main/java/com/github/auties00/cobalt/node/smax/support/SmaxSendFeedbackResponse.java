package com.github.auties00.cobalt.node.smax.support;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxSupportMessageFeedbackSendFeedbackRPC.sendSendFeedbackRPC}
 *           tries {@code Success} → {@code Error} in order. The WA
 *           Web {@code Error} variant is itself a disjunction over
 *           {@code IQErrorBadRequest} (400) /
 *           {@code IQErrorInternalServerError} (500) /
 *           {@code IQErrorRateOverlimit} (429); Cobalt splits the
 *           disjunction by code into the standard
 *           {@code ClientError} ({@code [400, 500)}) and
 *           {@code ServerError} ({@code [500, ...)}) buckets.
 */
public sealed interface SmaxSendFeedbackResponse extends SmaxOperation.Response
        permits SmaxSendFeedbackResponse.Success, SmaxSendFeedbackResponse.ClientError, SmaxSendFeedbackResponse.ServerError {

    /**
     * Tries each {@link SmaxSendFeedbackResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxSupportMessageFeedbackSendFeedbackRPC",
            exports = "sendSendFeedbackRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxSendFeedbackResponse> of(Node node, Node request) {
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
     * feedback and emitted the {@code <result status="Success"/>}
     * acknowledgement.
     *
     * @implNote {@code WASmaxInSupportMessageFeedbackSendFeedbackResponseSuccess.parseSendFeedbackResponseSuccess}
     *           validates the {@code <iq>} envelope, extracts the
     *           {@code <result>} child, and asserts
     *           {@code status == "Success"}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSupportMessageFeedbackSendFeedbackResponseSuccess")
    final class Success implements SmaxSendFeedbackResponse {
        /**
         * The acknowledgement {@code status} string, always the
         * literal {@code "Success"}.
         */
        private final String resultStatus;

        /**
         * Constructs a new successful reply.
         *
         * @param resultStatus the status; never {@code null}
         * @throws NullPointerException if {@code resultStatus} is
         *                              {@code null}
         */
        public Success(String resultStatus) {
            this.resultStatus = Objects.requireNonNull(resultStatus, "resultStatus cannot be null");
        }

        /**
         * Returns the result status.
         *
         * @return the status; never {@code null}
         */
        public String resultStatus() {
            return resultStatus;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInSupportMessageFeedbackSendFeedbackResponseSuccess",
                exports = "parseSendFeedbackResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "result")) {
                return Optional.empty();
            }
            var requestId = request.getAttributeAsString("id").orElse(null);
            if (requestId == null || !node.hasAttribute("id", requestId)) {
                return Optional.empty();
            }
            var resultChild = node.getChild("result").orElse(null);
            if (resultChild == null) {
                return Optional.empty();
            }
            var status = resultChild.getAttributeAsString("status").orElse(null);
            if (!"Success".equals(status)) {
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
            return Objects.equals(this.resultStatus, that.resultStatus);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resultStatus);
        }

        @Override
        public String toString() {
            return "SmaxSendFeedbackResponse.Success[resultStatus=" + resultStatus + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected
     * the feedback as malformed ({@code 400}) or rate-limited
     * ({@code 429}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSupportMessageFeedbackSendFeedbackResponseError")
    final class ClientError implements SmaxSendFeedbackResponse {
        /**
         * The numeric error code ({@code 400} or {@code 429}).
         */
        private final int errorCode;

        /**
         * The optional error text ({@code "bad-request"} /
         * {@code "rate-overlimit"}).
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
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
        @WhatsAppWebExport(moduleName = "WASmaxInSupportMessageFeedbackSendFeedbackResponseError",
                exports = "parseSendFeedbackResponseError",
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxSendFeedbackResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay
     * encountered a transient internal failure ({@code 500}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSupportMessageFeedbackSendFeedbackResponseError")
    final class ServerError implements SmaxSendFeedbackResponse {
        /**
         * The numeric error code (typically {@code 500}).
         */
        private final int errorCode;

        /**
         * The optional error text ({@code "internal-server-error"}).
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
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
        @WhatsAppWebExport(moduleName = "WASmaxInSupportMessageFeedbackSendFeedbackResponseError",
                exports = "parseSendFeedbackResponseError",
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxSendFeedbackResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
