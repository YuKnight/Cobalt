package com.github.auties00.cobalt.node.smax.inappcomms;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxInAppCommsEventRPC.sendEventRPC} tries
 *           {@code Success} → {@code Error} in order.
 */
public sealed interface SmaxInAppCommsEventResponse extends SmaxOperation.Response
        permits SmaxInAppCommsEventResponse.Success, SmaxInAppCommsEventResponse.Error {

    /**
     * Tries each {@link SmaxInAppCommsEventResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxInAppCommsEventRPC",
            exports = "sendEventRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxInAppCommsEventResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        return Error.of(node, request);
    }

    /**
     * The {@code Success} reply variant — the relay accepted the
     * event report. Carries no payload beyond the envelope echo.
     *
     * @implNote {@code WASmaxInInAppCommsEventResponseSuccess.parseEventResponseSuccess}
     *           validates the IQ-result envelope only; Cobalt routes
     *           through the shared
     *           {@link SmaxIqResultResponseMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInInAppCommsEventResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInInAppCommsIQResultResponseMixin")
    final class Success implements SmaxInAppCommsEventResponse {
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
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInInAppCommsEventResponseSuccess",
                exports = "parseEventResponseSuccess",
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
            return "SmaxInAppCommsEventResponse.Success[]";
        }
    }

    /**
     * The {@code Error} reply variant — the relay rejected or
     * dropped the event report.
     *
     * <p>The InAppComms domain projects three documented
     * {@code 5xx}/{@code 4xx} variants:
     * {@code internal-server-error/500},
     * {@code request-timeout/408},
     * {@code service-unavailable/503}. Cobalt collapses them into
     * the single {@code (errorCode, errorText)} pair.
     *
     * @implNote {@code WASmaxInInAppCommsEventResponseError.parseEventResponseError}
     *           composes the IQ-error envelope check with
     *           {@code WASmaxInInAppCommsEventErrorTypes.parseEventErrorTypes}
     *           which is the disjunction of the three projected error
     *           mixins.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInInAppCommsEventResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInInAppCommsEventErrorTypes")
    final class Error implements SmaxInAppCommsEventResponse {
        /**
         * The numeric error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public Error(int errorCode, String errorText) {
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
         * Tries to parse an {@link Error} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInInAppCommsEventResponseError",
                exports = "parseEventResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Error> of(Node node, Node request) {
            // The InAppComms Error disjunction mixes 408 (client-range) and 500/503
            // (server-range), so try both helpers and accept whichever envelope matches.
            var serverEnvelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (serverEnvelope != null) {
                return Optional.of(new Error(serverEnvelope.code(), serverEnvelope.text()));
            }
            var clientEnvelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (clientEnvelope != null && clientEnvelope.code() == 408) {
                return Optional.of(new Error(clientEnvelope.code(), clientEnvelope.text()));
            }
            return Optional.empty();
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
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxInAppCommsEventResponse.Error[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
