package com.github.auties00.cobalt.node.smax.support;

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
 * Sealed family of inbound reply variants.
 */
public sealed interface SmaxStatusReportV2Response extends SmaxOperation.Response
        permits SmaxStatusReportV2Response.Success, SmaxStatusReportV2Response.Error {

    /**
     * Tries each {@link SmaxStatusReportV2Response} variant in priority order.
     *
     * @param node    the inbound stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxSpamStatusReportV2RPC",
            exports = "sendStatusReportV2RPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxStatusReportV2Response> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        return Error.of(node, request);
    }

    /**
     * The {@code Success} reply variant. The relay accepted the
     * report.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSpamStatusReportV2ResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInSpamReportIdMixin")
    final class Success implements SmaxStatusReportV2Response {
        /**
         * The optional opaque report id.
         */
        private final String reportId;

        /**
         * Constructs a new successful reply.
         *
         * @param reportId the optional report id; may be {@code null}
         */
        public Success(String reportId) {
            this.reportId = reportId;
        }

        /**
         * Returns the optional report id.
         *
         * @return an {@link Optional} carrying the id, or empty when
         *         omitted
         */
        public Optional<String> reportId() {
            return Optional.ofNullable(reportId);
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on schema mismatch
         */
        @WhatsAppWebExport(moduleName = "WASmaxInSpamStatusReportV2ResponseSuccess",
                exports = "parseStatusReportV2ResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var reportId = node.getChild("report")
                    .flatMap(child -> child.getAttributeAsString("id"))
                    .orElse(null);
            return Optional.of(new Success(reportId));
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
            return Objects.equals(this.reportId, that.reportId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reportId);
        }

        @Override
        public String toString() {
            return "SmaxStatusReportV2Response.Success[reportId=" + reportId + ']';
        }
    }

    /**
     * The {@code Error} reply variant. The relay rejected the
     * report.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSpamStatusReportV2ResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInSpamIQErrorInternalServerErrorOrBadRequestOrForbiddenOrRateOverlimitMixinGroup")
    final class Error implements SmaxStatusReportV2Response {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a new error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional text; may be {@code null}
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the text, or empty when
         *         omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse an {@link Error} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on schema mismatch
         */
        @WhatsAppWebExport(moduleName = "WASmaxInSpamStatusReportV2ResponseError",
                exports = "parseStatusReportV2ResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Error> of(Node node, Node request) {
            var clientError = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (clientError != null) {
                return Optional.of(new Error(clientError.code(), clientError.text()));
            }
            var serverError = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (serverError != null) {
                return Optional.of(new Error(serverError.code(), serverError.text()));
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
            return "SmaxStatusReportV2Response.Error[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
