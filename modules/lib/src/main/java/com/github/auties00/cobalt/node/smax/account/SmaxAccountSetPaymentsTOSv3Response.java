package com.github.auties00.cobalt.node.smax.account;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxAccountSetPaymentsTOSv3Request}.
 *
 * @implNote {@code WASmaxAccountSetPaymentsTOSv3RPC.sendSetPaymentsTOSv3RPC}
 *           tries {@code Success} → {@code Error} in order and
 *           throws on no-match. Cobalt returns
 *           {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxAccountSetPaymentsTOSv3Response extends SmaxOperation.Response
        permits SmaxAccountSetPaymentsTOSv3Response.Success, SmaxAccountSetPaymentsTOSv3Response.Error {

    /**
     * Tries each {@link SmaxAccountSetPaymentsTOSv3Response} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to
     *                validate echoed identifiers; never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxAccountSetPaymentsTOSv3RPC",
            exports = "sendSetPaymentsTOSv3RPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxAccountSetPaymentsTOSv3Response> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        return Error.of(node, request);
    }

    /**
     * Validates the {@code <iq type="result">} or
     * {@code <iq type="error">} envelope of a SetPaymentsTOSv3
     * reply by cross-checking
     * {@code from}/{@code id}/{@code type} against the request.
     *
     * @param reply        the inbound IQ stanza
     * @param request      the original outbound IQ
     * @param expectedType the expected {@code type} attribute —
     *                     either {@code "result"} or {@code "error"}
     * @return {@code true} when the envelope echo-checks pass
     *
     * @implNote {@code WASmaxInAccountIQResultResponseMixin.parseIQResultResponseMixin}
     *           and
     *           {@code WASmaxInAccountIQErrorResponseMixin.parseIQErrorResponseMixin}.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInAccountIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInAccountIQErrorResponseMixin",
            exports = "parseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean validateIqEnvelope(Node reply, Node request, String expectedType) {
        if (!reply.hasDescription("iq")) {
            return false;
        }
        if (!reply.hasAttribute("type", expectedType)) {
            return false;
        }
        var requestId = request.getAttributeAsString("id").orElse(null);
        if (requestId == null) {
            return false;
        }
        if (!reply.hasAttribute("id", requestId)) {
            return false;
        }
        var requestTo = request.getAttributeAsString("to").orElse(null);
        if (requestTo == null) {
            return false;
        }
        return reply.hasAttribute("from", requestTo);
    }

    /**
     * The {@code Success} reply variant — the relay accepted the
     * ToS-v3 acceptance.
     *
     * @implNote {@code WASmaxInAccountSetPaymentsTOSv3ResponseSuccess.parseSetPaymentsTOSv3ResponseSuccess}
     *           validates the {@code <iq type="result">} envelope,
     *           parses the optional {@code outage="1"} and
     *           {@code sandbox="1"} markers on the
     *           {@code <accept_pay/>} child, and routes the consumer
     *           variant through
     *           {@code WASmaxInAccountSetPaymentsTOSv3BRConsumerOrSetPaymentsTOSv3UPIConsumerPaymentsTOSv3ResponseMixinGroup}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInAccountSetPaymentsTOSv3ResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInAccountSetPaymentsTOSv3BRConsumerOrSetPaymentsTOSv3UPIConsumerPaymentsTOSv3ResponseMixinGroup")
    final class Success implements SmaxAccountSetPaymentsTOSv3Response {
        /**
         * Whether the {@code <accept_pay outage="1"/>} marker was
         * present.
         */
        private final boolean outage;

        /**
         * Whether the {@code <accept_pay sandbox="1"/>} marker was
         * present.
         */
        private final boolean sandbox;

        /**
         * The echoed consumer-variant name — either
         * {@code "BRConsumerPaymentsTOSv3Response"} or
         * {@code "UPIConsumerPaymentsTOSv3Response"}.
         */
        private final String consumerVariantName;

        /**
         * The echoed {@code service} literal — {@code "FBPAY"} or
         * {@code "UPI"}.
         */
        private final String service;

        /**
         * The echoed {@code <additional_notice/>} list parsed from
         * the response.
         */
        private final List<String> additionalNotices;

        /**
         * Constructs a new successful reply.
         *
         * @param outage              whether the outage marker was
         *                            present
         * @param sandbox             whether the sandbox marker was
         *                            present
         * @param consumerVariantName the consumer-variant name;
         *                            never {@code null}
         * @param service             the echoed service literal;
         *                            never {@code null}
         * @param additionalNotices   the echoed notice list; never
         *                            {@code null}
         * @throws NullPointerException if any non-primitive argument
         *                              is {@code null}
         */
        public Success(boolean outage,
                       boolean sandbox,
                       String consumerVariantName,
                       String service,
                       List<String> additionalNotices) {
            this.outage = outage;
            this.sandbox = sandbox;
            this.consumerVariantName = Objects.requireNonNull(consumerVariantName, "consumerVariantName cannot be null");
            this.service = Objects.requireNonNull(service, "service cannot be null");
            this.additionalNotices = List.copyOf(Objects.requireNonNullElse(additionalNotices, List.of()));
        }

        /**
         * Returns whether the {@code outage="1"} marker was present.
         *
         * @return {@code true} when the marker was set
         */
        public boolean outage() {
            return outage;
        }

        /**
         * Returns whether the {@code sandbox="1"} marker was
         * present.
         *
         * @return {@code true} when the marker was set
         */
        public boolean sandbox() {
            return sandbox;
        }

        /**
         * Returns the consumer-variant name.
         *
         * @return the name; never {@code null}
         */
        public String consumerVariantName() {
            return consumerVariantName;
        }

        /**
         * Returns the echoed service literal.
         *
         * @return the service; never {@code null}
         */
        public String service() {
            return service;
        }

        /**
         * Returns the echoed notice list.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<String> additionalNotices() {
            return additionalNotices;
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
        @WhatsAppWebExport(moduleName = "WASmaxInAccountSetPaymentsTOSv3ResponseSuccess",
                exports = "parseSetPaymentsTOSv3ResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!validateIqEnvelope(node, request, "result")) {
                return Optional.empty();
            }
            var acceptPay = node.getChild("accept_pay").orElse(null);
            if (acceptPay == null) {
                return Optional.empty();
            }
            var outage = acceptPay.hasAttribute("outage", "1");
            var sandbox = acceptPay.hasAttribute("sandbox", "1");
            String consumerVariantName;
            String service;
            if (acceptPay.hasAttribute("service", "FBPAY")) {
                consumerVariantName = "BRConsumerPaymentsTOSv3Response";
                service = "FBPAY";
            } else if (acceptPay.hasAttribute("service", "UPI")) {
                consumerVariantName = "UPIConsumerPaymentsTOSv3Response";
                service = "UPI";
            } else {
                return Optional.empty();
            }
            var notices = acceptPay.streamChildren("additional_notice")
                    .map(child -> child.getAttributeAsString("notice").orElse(null))
                    .filter(Objects::nonNull)
                    .toList();
            if (notices.isEmpty() || notices.size() > 10) {
                return Optional.empty();
            }
            return Optional.of(new Success(outage, sandbox, consumerVariantName, service, notices));
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
            return this.outage == that.outage
                    && this.sandbox == that.sandbox
                    && Objects.equals(this.consumerVariantName, that.consumerVariantName)
                    && Objects.equals(this.service, that.service)
                    && Objects.equals(this.additionalNotices, that.additionalNotices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(outage, sandbox, consumerVariantName, service, additionalNotices);
        }

        @Override
        public String toString() {
            return "SmaxAccountSetPaymentsTOSv3Response.Success[outage=" + outage
                    + ", sandbox=" + sandbox
                    + ", consumerVariantName=" + consumerVariantName
                    + ", service=" + service
                    + ", additionalNotices=" + additionalNotices + ']';
        }
    }

    /**
     * The {@code Error} reply variant — the relay rejected the
     * ToS-v3 acceptance with one of the documented error codes
     * (internal-server-error 500, service-unavailable 503,
     * upgrade-required 443, config-mismatch 453, forbidden 403,
     * bad-request 400).
     *
     * @implNote {@code WASmaxInAccountSetPaymentsTOSv3ResponseError.parseSetPaymentsTOSv3ResponseError}
     *           validates the {@code <iq type="error">} envelope
     *           and routes the {@code <error/>} child through
     *           {@code WASmaxInAccountSetPaymentsTosErrors.parseSetPaymentsTosErrors}.
     *           Cobalt collapses the disjunction into a single
     *           {@code (errorCode, errorText)} pair plus the
     *           variant-name label.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInAccountSetPaymentsTOSv3ResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInAccountSetPaymentsTosErrors")
    final class Error implements SmaxAccountSetPaymentsTOSv3Response {
        /**
         * The numeric error code (one of 400 / 403 / 443 / 453 /
         * 500 / 503).
         */
        private final int errorCode;

        /**
         * The optional human-readable error text echoed by the
         * relay.
         */
        private final String errorText;

        /**
         * The classified variant name — one of
         * {@code "IQErrorBadRequest"},
         * {@code "IQErrorForbidden"},
         * {@code "IQErrorPayUpgradeRequired"},
         * {@code "IQErrorConfigMismatch"},
         * {@code "IQErrorInternalServerError"},
         * {@code "IQErrorServiceUnavailable"}.
         */
        private final String variantName;

        /**
         * Constructs a new error reply.
         *
         * @param errorCode   the numeric error code
         * @param errorText   the optional human-readable text; may
         *                    be {@code null}
         * @param variantName the classified variant name; never
         *                    {@code null}
         * @throws NullPointerException if {@code variantName} is
         *                              {@code null}
         */
        public Error(int errorCode, String errorText, String variantName) {
            this.errorCode = errorCode;
            this.errorText = errorText;
            this.variantName = Objects.requireNonNull(variantName, "variantName cannot be null");
        }

        /**
         * Returns the numeric error code.
         *
         * @return the code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the text, or empty
         *         when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Returns the classified variant name.
         *
         * @return the name; never {@code null}
         */
        public String variantName() {
            return variantName;
        }

        /**
         * Tries to parse an {@link Error} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInAccountSetPaymentsTOSv3ResponseError",
                exports = "parseSetPaymentsTOSv3ResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Error> of(Node node, Node request) {
            if (!validateIqEnvelope(node, request, "error")) {
                return Optional.empty();
            }
            var errorChild = node.getChild("error").orElse(null);
            if (errorChild == null) {
                return Optional.empty();
            }
            var codeOpt = errorChild.getAttributeAsInt("code");
            if (codeOpt.isEmpty()) {
                return Optional.empty();
            }
            var code = codeOpt.getAsInt();
            var text = errorChild.getAttributeAsString("text").orElse(null);
            var variantName = classifyError(code, text);
            if (variantName == null) {
                return Optional.empty();
            }
            return Optional.of(new Error(code, text, variantName));
        }

        /**
         * Classifies the error variant by cross-checking the
         * {@code (code, text)} pair against the documented
         * {@code WASmaxInAccountIQError*Mixin} mapping.
         *
         * @param code the numeric error code
         * @param text the human-readable error text; may be
         *             {@code null}
         * @return the variant name, or {@code null} when the pair
         *         does not match any documented variant
         *
         * @implNote {@code WASmaxInAccountSetPaymentsTosErrors.parseSetPaymentsTosErrors}.
         */
        @WhatsAppWebExport(moduleName = "WASmaxInAccountSetPaymentsTosErrors",
                exports = "parseSetPaymentsTosErrors",
                adaptation = WhatsAppAdaptation.ADAPTED)
        private static String classifyError(int code, String text) {
            if (code == 400 && "bad-request".equals(text)) {
                return "IQErrorBadRequest";
            }
            if (code == 403 && "forbidden".equals(text)) {
                return "IQErrorForbidden";
            }
            if (code == 443 && "upgrade-required".equals(text)) {
                return "IQErrorPayUpgradeRequired";
            }
            if (code == 453 && "config-mismatch".equals(text)) {
                return "IQErrorConfigMismatch";
            }
            if (code == 500 && "internal-server-error".equals(text)) {
                return "IQErrorInternalServerError";
            }
            if (code == 503 && "service-unavailable".equals(text)) {
                return "IQErrorServiceUnavailable";
            }
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Error) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText)
                    && Objects.equals(this.variantName, that.variantName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText, variantName);
        }

        @Override
        public String toString() {
            return "SmaxAccountSetPaymentsTOSv3Response.Error[errorCode=" + errorCode
                    + ", errorText=" + errorText
                    + ", variantName=" + variantName + ']';
        }
    }
}
