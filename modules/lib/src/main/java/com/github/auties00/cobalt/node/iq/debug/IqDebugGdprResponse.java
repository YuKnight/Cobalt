package com.github.auties00.cobalt.node.iq.debug;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 */
public sealed interface IqDebugGdprResponse extends IqOperation.Response
        permits IqDebugGdprResponse.Success, IqDebugGdprResponse.ClientError, IqDebugGdprResponse.ServerError {

    /**
     * Tries each {@link IqDebugGdprResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no documented variant matched
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebDebugGDPR",
            exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqDebugGdprResponse> of(Node node, Node request) {
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
     * cancel request. Carries the post-cancel GDPR status string
     * and the optional {@code expiration} timestamp echoed by the
     * relay (typically {@code 0} after a cancel).
     *
     * @implNote The reply is parsed by
     *           {@code useWAWebGdprStatus.GdprStatusWapParser}
     *           which is a hook-side projection that surfaces the
     *           {@code <gdpr>} grandchild's {@code status} and
     *           {@code expiration} attributes. Cobalt mirrors that
     *           shape with optional fields since the cancel path
     *           does not always populate the full status payload.
     */
    @WhatsAppWebModule(moduleName = "WAWebDebugGDPR")
    final class Success implements IqDebugGdprResponse {
        /**
         * The optional GDPR status string echoed by the relay
         * (e.g. {@code "none"} after a successful cancel).
         */
        private final String status;

        /**
         * The optional expiration timestamp echoed by the relay
         * (seconds since epoch). Typically {@code 0} or absent
         * after a cancel.
         */
        private final Long expirationSeconds;

        /**
         * Constructs a successful reply.
         *
         * @param status            the optional status string; may
         *                          be {@code null}
         * @param expirationSeconds the optional expiration
         *                          timestamp; may be {@code null}
         */
        public Success(String status, Long expirationSeconds) {
            this.status = status;
            this.expirationSeconds = expirationSeconds;
        }

        /**
         * Returns the optional GDPR status string.
         *
         * @return an {@link Optional} carrying the status, or
         *         empty
         */
        public Optional<String> status() {
            return Optional.ofNullable(status);
        }

        /**
         * Returns the optional expiration timestamp.
         *
         * @return an {@link Optional} carrying the timestamp, or
         *         empty
         */
        public Optional<Long> expirationSeconds() {
            return Optional.ofNullable(expirationSeconds);
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WAWebDebugGDPR",
                exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var gdpr = node.getChild("gdpr").orElse(null);
            if (gdpr == null) {
                return Optional.of(new Success(null, null));
            }
            var status = gdpr.getAttributeAsString("status").orElse(null);
            var expirationAttr = gdpr.getAttributeAsLong("expiration");
            Long expirationSeconds = expirationAttr.isPresent()
                    ? expirationAttr.getAsLong()
                    : null;
            return Optional.of(new Success(status, expirationSeconds));
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
            return Objects.equals(this.status, that.status)
                    && Objects.equals(this.expirationSeconds, that.expirationSeconds);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, expirationSeconds);
        }

        @Override
        public String toString() {
            return "IqDebugGdprResponse.Success[status=" + status
                    + ", expirationSeconds=" + expirationSeconds + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — {@code 4xx} rejection
     * (typically {@code 404} when no GDPR request is in flight).
     */
    @WhatsAppWebModule(moduleName = "WAWebDebugGDPR")
    final class ClientError implements IqDebugGdprResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional text; may be {@code null}
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
         * @return an {@link Optional} carrying the text, or empty
         *         when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebDebugGDPR",
                exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqDebugGdprResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — {@code 5xx} transient
     * failure.
     */
    @WhatsAppWebModule(moduleName = "WAWebDebugGDPR")
    final class ServerError implements IqDebugGdprResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional text; may be {@code null}
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
         * @return an {@link Optional} carrying the text, or empty
         *         when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebDebugGDPR",
                exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqDebugGdprResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
