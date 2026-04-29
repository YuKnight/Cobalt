package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
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
public sealed interface IqVerifyPostcodeResponse extends IqOperation.Response
        permits IqVerifyPostcodeResponse.Success, IqVerifyPostcodeResponse.ClientError, IqVerifyPostcodeResponse.ServerError {

    /**
     * Tries each {@link IqVerifyPostcodeResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or empty
     *         when no documented variant matched
     * @throws NullPointerException if either argument is {@code null}
     */
    static Optional<? extends IqVerifyPostcodeResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — projects the {@code result_code}
     * verdict and the optional encrypted location-name echo.
     */
    final class Success implements IqVerifyPostcodeResponse {
        /**
         * Closed set of {@code result_code} values documented by the
         * relay. WA Web's reference parser throws a 500-status error
         * if the value falls outside this enum.
         */
        public enum ResultCode {
            /**
             * The postcode was successfully decrypted and resolves to
             * a serviceable address.
             */
            SUCCESS("success"),

            /**
             * The postcode could not be decrypted or did not pass
             * validation.
             */
            INVALID_POSTCODE("invalid_postcode"),

            /**
             * The postcode resolved to an address outside the
             * merchant's serviceable area.
             */
            UNSERVICEABLE_LOCATION("unserviceable_location");

            /**
             * The wire string carried by the {@code <result_code/>}
             * element.
             */
            private final String wireValue;

            /**
             * Constructs a constant from its wire string.
             *
             * @param wireValue the wire string; never {@code null}
             */
            ResultCode(String wireValue) {
                this.wireValue = wireValue;
            }

            /**
             * Returns the wire string for this constant.
             *
             * @return the wire string; never {@code null}
             */
            public String wireValue() {
                return wireValue;
            }

            /**
             * Looks up the constant for the given wire string.
             *
             * @param wireValue the wire string; may be {@code null}
             * @return an {@link Optional} carrying the matching
             *         constant, or empty when the value is unknown
             */
            public static Optional<ResultCode> of(String wireValue) {
                if (wireValue == null) {
                    return Optional.empty();
                }
                for (var value : values()) {
                    if (value.wireValue.equals(wireValue)) {
                        return Optional.of(value);
                    }
                }
                return Optional.empty();
            }
        }

        /**
         * The verdict returned by the relay.
         */
        private final ResultCode resultCode;

        /**
         * The optional encrypted location-name echo.
         */
        private final String encryptedLocationName;

        /**
         * Constructs a successful reply.
         *
         * @param resultCode            the verdict; never {@code null}
         * @param encryptedLocationName the optional encrypted echo;
         *                              may be {@code null}
         * @throws NullPointerException if {@code resultCode} is
         *                              {@code null}
         */
        public Success(ResultCode resultCode, String encryptedLocationName) {
            this.resultCode = Objects.requireNonNull(resultCode, "resultCode cannot be null");
            this.encryptedLocationName = encryptedLocationName;
        }

        /**
         * Returns the verdict.
         *
         * @return the verdict; never {@code null}
         */
        public ResultCode resultCode() {
            return resultCode;
        }

        /**
         * Returns the optional encrypted location-name echo.
         *
         * @return an {@link Optional} carrying the echo
         */
        public Optional<String> encryptedLocationName() {
            return Optional.ofNullable(encryptedLocationName);
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the success
         *         schema or the {@code result_code} is unknown
         */
        @WhatsAppWebExport(moduleName = "WAWebVerifyPostcodeJob",
                exports = "VerifyPostcode", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var resultCodeNode = node.getChild("result_code").orElse(null);
            if (resultCodeNode == null) {
                return Optional.empty();
            }
            var resultCodeValue = resultCodeNode.toContentString().orElse(null);
            var resultCode = ResultCode.of(resultCodeValue).orElse(null);
            if (resultCode == null) {
                return Optional.empty();
            }
            var encryptedLocationName = node.getChild("encrypted_location_name")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            return Optional.of(new Success(resultCode, encryptedLocationName));
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
            return this.resultCode == that.resultCode
                    && Objects.equals(this.encryptedLocationName, that.encryptedLocationName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resultCode, encryptedLocationName);
        }

        @Override
        public String toString() {
            return "IqVerifyPostcodeResponse.Success[resultCode=" + resultCode
                    + ", encryptedLocationName=" + encryptedLocationName + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    final class ClientError implements IqVerifyPostcodeResponse {
        /**
         * The numeric error code.
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
         * Returns the human-readable error text, when supplied.
         *
         * @return an {@link Optional} carrying the error text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the client-error
         *         schema
         */
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
            return "IqVerifyPostcodeResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    final class ServerError implements IqVerifyPostcodeResponse {
        /**
         * The numeric error code.
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
         * Returns the human-readable error text, when supplied.
         *
         * @return an {@link Optional} carrying the error text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the server-error
         *         schema
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
            return "IqVerifyPostcodeResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
