package com.github.auties00.cobalt.node.smax.mdcompanion;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqErrorResponseMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxMdGetCountryCodeRPC.sendGetCountryCodeRPC}
 *           tries {@code GetCountryCodeResponse} → {@code Error} in
 *           order; Cobalt subdivides the {@code Error} side into
 *           {@code ClientError} / {@code ServerError}.
 */
public sealed interface SmaxMdGetCountryCodeResponse extends SmaxOperation.Response
        permits SmaxMdGetCountryCodeResponse.Success, SmaxMdGetCountryCodeResponse.ClientError, SmaxMdGetCountryCodeResponse.ServerError {

    /**
     * Tries each {@link SmaxMdGetCountryCodeResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxMdGetCountryCodeRPC",
            exports = "sendGetCountryCodeRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxMdGetCountryCodeResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay echoed back the
     * geo-derived ISO country code.
     *
     * @implNote {@code WASmaxInMdGetCountryCodeResponseGetCountryCodeResponse.parseGetCountryCodeResponseGetCountryCodeResponse}
     *           extracts the {@code <country_code iso=…/>} attribute.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInMdGetCountryCodeResponseGetCountryCodeResponse")
    final class Success implements SmaxMdGetCountryCodeResponse {
        /**
         * The ISO 3166-1 alpha-2 country code.
         */
        private final String countryCodeIso;

        /**
         * Constructs a new successful reply.
         *
         * @param countryCodeIso the ISO country code; never {@code null}
         * @throws NullPointerException if {@code countryCodeIso} is {@code null}
         */
        public Success(String countryCodeIso) {
            this.countryCodeIso = Objects.requireNonNull(countryCodeIso, "countryCodeIso cannot be null");
        }

        /**
         * Returns the ISO country code.
         *
         * @return the code; never {@code null}
         */
        public String countryCodeIso() {
            return countryCodeIso;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInMdGetCountryCodeResponseGetCountryCodeResponse",
                exports = "parseGetCountryCodeResponseGetCountryCodeResponse",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var countryCode = node.getChild("country_code").orElse(null);
            if (countryCode == null) {
                return Optional.empty();
            }
            var iso = countryCode.getAttributeAsString("iso").orElse(null);
            if (iso == null) {
                return Optional.empty();
            }
            return Optional.of(new Success(iso));
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
            return Objects.equals(this.countryCodeIso, that.countryCodeIso);
        }

        @Override
        public int hashCode() {
            return Objects.hash(countryCodeIso);
        }

        @Override
        public String toString() {
            return "SmaxMdGetCountryCodeResponse.Success[countryCodeIso=" + countryCodeIso + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request with a {@code 4xx} bad-request or internal-error mapped
     * to client range.
     *
     * @implNote {@code WASmaxInMdGetCountryCodeResponseError.parseGetCountryCodeResponseError}
     *           composes
     *           {@code parseIQErrorResponseMixin} with
     *           {@code WASmaxInMdCountryCodeErrors.parseCountryCodeErrors}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInMdGetCountryCodeResponseError")
    final class ClientError implements SmaxMdGetCountryCodeResponse {
        /**
         * The numeric error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be {@code null}
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
         * @return an {@link Optional} carrying the text
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
        @WhatsAppWebExport(moduleName = "WASmaxInMdGetCountryCodeResponseError",
                exports = "parseGetCountryCodeResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            if (!SmaxIqErrorResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
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
            return "SmaxMdGetCountryCodeResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered
     * a {@code 5xx} transient internal failure.
     *
     * @implNote layered onto Cobalt's domain via the shared
     *           {@link SmaxBaseServerErrorMixin}.
     */
    final class ServerError implements SmaxMdGetCountryCodeResponse {
        /**
         * The numeric error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be {@code null}
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
         * @return an {@link Optional} carrying the text
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
            return "SmaxMdGetCountryCodeResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
