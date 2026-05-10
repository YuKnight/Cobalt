package com.github.auties00.cobalt.node.iq.privacy;

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
 * Sealed family of inbound reply variants produced by the relay.
 */
public sealed interface IqSetReadReceiptResponse extends IqOperation.Response
        permits IqSetReadReceiptResponse.Success, IqSetReadReceiptResponse.ClientError, IqSetReadReceiptResponse.ServerError {

    /**
     * Tries each {@link IqSetReadReceiptResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza. Never {@code null}
     * @param request the original outbound stanza. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no documented variant matched
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSetReadReceiptJob",
            exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqSetReadReceiptResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay echoes the new
     * read-receipts category value.
     */
    @WhatsAppWebModule(moduleName = "WAWebSetReadReceiptJob")
    final class Success implements IqSetReadReceiptResponse {
        /**
         * The new read-receipts state echoed by the relay.
         */
        private final boolean enabled;

        /**
         * Constructs a successful reply.
         *
         * @param enabled the echoed read-receipts state
         */
        public Success(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Returns the echoed read-receipts state.
         *
         * @return {@code true} when read receipts are enabled,
         *         {@code false} when disabled
         */
        public boolean enabled() {
            return enabled;
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
        @WhatsAppWebExport(moduleName = "WAWebSetReadReceiptJob",
                exports = "photoResponseParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var privacy = node.getChild("privacy").orElse(null);
            if (privacy == null) {
                return Optional.empty();
            }
            var category = privacy.getChild("category").orElse(null);
            if (category == null) {
                return Optional.empty();
            }
            var name = category.getAttributeAsString("name").orElse(null);
            if (!"readreceipts".equals(name)) {
                return Optional.empty();
            }
            var value = category.getAttributeAsString("value").orElse(null);
            if (value == null) {
                return Optional.empty();
            }
            if ("all".equals(value)) {
                return Optional.of(new Success(true));
            }
            if ("none".equals(value)) {
                return Optional.of(new Success(false));
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
            var that = (Success) obj;
            return this.enabled == that.enabled;
        }

        @Override
        public int hashCode() {
            return Objects.hash(enabled);
        }

        @Override
        public String toString() {
            return "IqSetReadReceiptResponse.Success[enabled=" + enabled + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected
     * the request with a {@code 4xx} error code.
     */
    @WhatsAppWebModule(moduleName = "WAWebSetReadReceiptJob")
    final class ClientError implements IqSetReadReceiptResponse {
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
         * @param errorText the optional text. May be {@code null}
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
        @WhatsAppWebExport(moduleName = "WAWebSetReadReceiptJob",
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
            return "IqSetReadReceiptResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. Transient internal
     * failure ({@code 5xx} error code).
     */
    @WhatsAppWebModule(moduleName = "WAWebSetReadReceiptJob")
    final class ServerError implements IqSetReadReceiptResponse {
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
         * @param errorText the optional text. May be {@code null}
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
        @WhatsAppWebExport(moduleName = "WAWebSetReadReceiptJob",
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
            return "IqSetReadReceiptResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
