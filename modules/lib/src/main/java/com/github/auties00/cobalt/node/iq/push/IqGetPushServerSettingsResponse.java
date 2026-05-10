package com.github.auties00.cobalt.node.iq.push;

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
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link IqGetPushServerSettingsRequest}.
 */
public sealed interface IqGetPushServerSettingsResponse extends IqOperation.Response
        permits IqGetPushServerSettingsResponse.Success, IqGetPushServerSettingsResponse.ClientError, IqGetPushServerSettingsResponse.ServerError {

    /**
     * Tries each {@link IqGetPushServerSettingsResponse} variant in priority order and returns
     * the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay.
     *                Never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebGetPushServerSettingsJob",
            exports = "getPushServerSettings", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqGetPushServerSettingsResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay returned the
     * server-side push key.
     *
     * <p>Carries the base64-encoded {@code webserverkey} attribute
     * value. WA Web stores this verbatim and surfaces it through the
     * push pipeline for inbound-payload signature validation.
     */
    @WhatsAppWebModule(moduleName = "WAWebGetPushServerSettingsJob")
    final class Success implements IqGetPushServerSettingsResponse {
        /**
         * The base64-encoded server-side push key returned by the
         * relay in the {@code webserverkey} attribute of the
         * {@code <settings/>} grandchild.
         */
        private final String webServerKey;

        /**
         * Constructs a new successful reply.
         *
         * @param webServerKey the server-side push key. Never
         *                     {@code null}
         * @throws NullPointerException if {@code webServerKey} is
         *                              {@code null}
         */
        public Success(String webServerKey) {
            this.webServerKey = Objects.requireNonNull(webServerKey, "webServerKey cannot be null");
        }

        /**
         * Returns the base64-encoded server-side push key.
         *
         * @return the key string. Never {@code null}
         */
        public String webServerKey() {
            return webServerKey;
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
        @WhatsAppWebExport(moduleName = "WAWebGetPushServerSettingsJob",
                exports = "getPushServerSettings", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var settingsChild = node.getChild("settings").orElse(null);
            if (settingsChild == null) {
                return Optional.empty();
            }
            var webServerKey = settingsChild.getAttributeAsString("webserverkey").orElse(null);
            if (webServerKey == null) {
                return Optional.empty();
            }
            return Optional.of(new Success(webServerKey));
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
            return Objects.equals(this.webServerKey, that.webServerKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(webServerKey);
        }

        @Override
        public String toString() {
            return "IqGetPushServerSettingsResponse.Success[webServerKey=" + webServerKey + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * query as malformed or unauthorised.
     */
    @WhatsAppWebModule(moduleName = "WAWebGetPushServerSettingsJob")
    final class ClientError implements IqGetPushServerSettingsResponse {
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
         * @param errorText the optional human-readable text. May be
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
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
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
        @WhatsAppWebExport(moduleName = "WAWebGetPushServerSettingsJob",
                exports = "getPushServerSettings", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqGetPushServerSettingsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure while processing the query.
     */
    @WhatsAppWebModule(moduleName = "WAWebGetPushServerSettingsJob")
    final class ServerError implements IqGetPushServerSettingsResponse {
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
         * @param errorText the optional human-readable text. May be
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
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
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
        @WhatsAppWebExport(moduleName = "WAWebGetPushServerSettingsJob",
                exports = "getPushServerSettings", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqGetPushServerSettingsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
