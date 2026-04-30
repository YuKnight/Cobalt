package com.github.auties00.cobalt.node.smax.stats;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxStatsSendBufferRPC.sendSendBufferRPC} tries
 *           {@code Success} → {@code ErrorNoRetry} →
 *           {@code ErrorRetry}.
 */
public sealed interface SmaxStatsSendBufferResponse extends SmaxOperation.Response
        permits SmaxStatsSendBufferResponse.Success, SmaxStatsSendBufferResponse.ErrorNoRetry, SmaxStatsSendBufferResponse.ErrorRetry {

    /**
     * Tries each {@link SmaxStatsSendBufferResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza. Never {@code null}
     * @param request the original outbound stanza. Never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxStatsSendBufferRPC",
            exports = "sendSendBufferRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxStatsSendBufferResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var noRetry = ErrorNoRetry.of(node, request);
        if (noRetry.isPresent()) {
            return noRetry;
        }
        return ErrorRetry.of(node, request);
    }

    /**
     * The {@code Success} reply variant. The relay accepted the
     * batch.
     *
     * <p>Carries no payload beyond the envelope echo: the
     * {@code <iq type="result">} validator is the only check.
     *
     * @implNote {@code WASmaxInStatsSendBufferResponseSuccess.parseSendBufferResponseSuccess}
     *           delegates to
     *           {@code WASmaxInStatsIQResultResponseMixin.parseIQResultResponseMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInStatsSendBufferResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInStatsIQResultResponseMixin")
    final class Success implements SmaxStatsSendBufferResponse {
        /**
         * Constructs a new successful reply.
         */
        public Success() {
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInStatsSendBufferResponseSuccess",
                exports = "parseSendBufferResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
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
            return "SmaxStatsSendBufferResponse.Success[]";
        }
    }

    /**
     * The {@code ErrorNoRetry} reply variant. A permanent rejection
     * that the local client must NOT retry.
     *
     * <p>Carries one of three documented {@code (code, text)} pairs:
     * {@code (400, "bad-request")},
     * {@code (406, "not-acceptable")}, or
     * {@code (501, "feature-not-implemented")}. The
     * {@code 406} sub-shape additionally exposes a
     * {@code <field name reason/>} grandchild lifted into the
     * {@code (fieldName, fieldReason)} pair below.
     *
     * @implNote {@code WASmaxInStatsSendBufferResponseErrorNoRetry.parseSendBufferResponseErrorNoRetry}
     *           projects the {@code <error/>} child through
     *           {@code WASmaxInStatsSendBufferNoRetryError} . a
     *           disjunction over {@code IQErrorBadRequest},
     *           {@code IQErrorNotAcceptable}, and
     *           {@code IQErrorFeatureNotImplemented}. Cobalt
     *           collapses the three sub-mixins into the named
     *           variant below plus the optional
     *           not-acceptable-only {@code field} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInStatsSendBufferResponseErrorNoRetry")
    @WhatsAppWebModule(moduleName = "WASmaxInStatsSendBufferNoRetryError")
    @WhatsAppWebModule(moduleName = "WASmaxInStatsIQErrorBadRequestMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInStatsIQErrorNotAcceptableMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInStatsIQErrorFeatureNotImplementedMixin")
    final class ErrorNoRetry implements SmaxStatsSendBufferResponse {
        /**
         * The numeric error code. One of {@code 400}, {@code 406},
         * or {@code 501}.
         */
        private final int errorCode;

        /**
         * The error text. One of {@code "bad-request"},
         * {@code "not-acceptable"}, or
         * {@code "feature-not-implemented"}.
         */
        private final String errorText;

        /**
         * The optional {@code <field name="…"/>} grandchild
         * surfaced by the {@code 406 not-acceptable} sub-shape;
         * {@code null} for the other two codes.
         */
        private final String fieldName;

        /**
         * The optional {@code <field reason="…"/>} grandchild
         * surfaced by the {@code 406 not-acceptable} sub-shape.
         */
        private final String fieldReason;

        /**
         * Constructs a new no-retry error reply.
         *
         * @param errorCode   the numeric error code
         * @param errorText   the optional error text. May be
         *                    {@code null}
         * @param fieldName   the optional field name. May be
         *                    {@code null}
         * @param fieldReason the optional field reason. May be
         *                    {@code null}
         */
        public ErrorNoRetry(int errorCode, String errorText, String fieldName, String fieldReason) {
            this.errorCode = errorCode;
            this.errorText = errorText;
            this.fieldName = fieldName;
            this.fieldReason = fieldReason;
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Returns the optional {@code field} name (only present on
         * the {@code 406 not-acceptable} sub-shape).
         *
         * @return an {@link Optional} carrying the field name
         */
        public Optional<String> fieldName() {
            return Optional.ofNullable(fieldName);
        }

        /**
         * Returns the optional {@code field} reason (only present
         * on the {@code 406 not-acceptable} sub-shape).
         *
         * @return an {@link Optional} carrying the field reason
         */
        public Optional<String> fieldReason() {
            return Optional.ofNullable(fieldReason);
        }

        /**
         * Tries to parse an {@link ErrorNoRetry} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInStatsSendBufferResponseErrorNoRetry",
                exports = "parseSendBufferResponseErrorNoRetry",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ErrorNoRetry> of(Node node, Node request) {
            // 4xx → ClientError envelope, 5xx → ServerError envelope.
            var clientEnvelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            var serverEnvelope = clientEnvelope == null
                    ? SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null)
                    : null;
            var envelope = clientEnvelope != null ? clientEnvelope : serverEnvelope;
            if (envelope == null) {
                return Optional.empty();
            }
            var code = envelope.code();
            var text = envelope.text();
            String fieldName = null;
            String fieldReason = null;
            if (code == 400 && "bad-request".equals(text)) {
                // IQErrorBadRequestMixin
            } else if (code == 406 && "not-acceptable".equals(text)) {
                // IQErrorNotAcceptableMixin. Also carries optional <field name reason/>
                var errorChild = node.getChild("error").orElse(null);
                if (errorChild != null) {
                    var fieldNode = errorChild.getChild("field").orElse(null);
                    if (fieldNode != null) {
                        fieldName = fieldNode.getAttributeAsString("name").orElse(null);
                        fieldReason = fieldNode.getAttributeAsString("reason").orElse(null);
                        if (fieldName == null || fieldReason == null) {
                            return Optional.empty();
                        }
                    }
                }
            } else if (code == 501 && "feature-not-implemented".equals(text)) {
                // IQErrorFeatureNotImplementedMixin
            } else {
                // Unknown code/text pair. Not a documented variant.
                return Optional.empty();
            }
            return Optional.of(new ErrorNoRetry(code, text, fieldName, fieldReason));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ErrorNoRetry) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText)
                    && Objects.equals(this.fieldName, that.fieldName)
                    && Objects.equals(this.fieldReason, that.fieldReason);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText, fieldName, fieldReason);
        }

        @Override
        public String toString() {
            return "SmaxStatsSendBufferResponse.ErrorNoRetry[errorCode=" + errorCode
                    + ", errorText=" + errorText
                    + ", fieldName=" + fieldName
                    + ", fieldReason=" + fieldReason + ']';
        }
    }

    /**
     * The {@code ErrorRetry} reply variant. A transient
     * {@code 503 service-unavailable} rejection. The local client
     * must re-buffer the batch and retry on the next flush window.
     *
     * @implNote {@code WASmaxInStatsSendBufferResponseErrorRetry.parseSendBufferResponseErrorRetry}
     *           projects the {@code <error/>} child through
     *           {@code WASmaxInStatsIQErrorServiceUnavailableMixin}
     *           which asserts the literal
     *           {@code (code=503, text="service-unavailable")} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInStatsSendBufferResponseErrorRetry")
    @WhatsAppWebModule(moduleName = "WASmaxInStatsIQErrorServiceUnavailableMixin")
    final class ErrorRetry implements SmaxStatsSendBufferResponse {
        /**
         * Constructs a new retry-error reply. The shape carries no
         * payload beyond the asserted {@code (503,
         * "service-unavailable")} pair.
         */
        public ErrorRetry() {
        }

        /**
         * Returns the numeric error code. Always {@code 503}.
         *
         * @return the code
         */
        public int errorCode() {
            return 503;
        }

        /**
         * Returns the error text. Always
         * {@code "service-unavailable"}.
         *
         * @return the text
         */
        public String errorText() {
            return "service-unavailable";
        }

        /**
         * Tries to parse an {@link ErrorRetry} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInStatsSendBufferResponseErrorRetry",
                exports = "parseSendBufferResponseErrorRetry",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ErrorRetry> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            if (envelope.code() != 503 || !"service-unavailable".equals(envelope.text())) {
                return Optional.empty();
            }
            return Optional.of(new ErrorRetry());
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
            return ErrorRetry.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxStatsSendBufferResponse.ErrorRetry[]";
        }
    }
}
