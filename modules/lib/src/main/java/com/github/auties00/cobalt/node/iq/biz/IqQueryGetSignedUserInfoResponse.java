package com.github.auties00.cobalt.node.iq.biz;

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
 * response to an {@link IqQueryGetSignedUserInfoRequest}.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryGetSignedUserInfoJob")
public sealed interface IqQueryGetSignedUserInfoResponse extends IqOperation.Response
        permits IqQueryGetSignedUserInfoResponse.Success, IqQueryGetSignedUserInfoResponse.ClientError, IqQueryGetSignedUserInfoResponse.ServerError {

    /**
     * Tries each {@link IqQueryGetSignedUserInfoResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or empty
     *         when no documented variant matched
     * @throws NullPointerException if either argument is {@code null}
     */
    static Optional<? extends IqQueryGetSignedUserInfoResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — the relay returned the signed
     * user-info bundle (every field is optional on the wire).
     */
    final class Success implements IqQueryGetSignedUserInfoResponse {
        /**
         * The merchant's phone number in E.164 form, when the relay
         * supplied one.
         */
        private final String phoneNumber;

        /**
         * The unix-epoch second at which the signature in
         * {@link #phoneNumberSignature} expires.
         */
        private final String phoneNumberSignatureExpiration;

        /**
         * The opaque signature blob over the merchant's phone number.
         */
        private final String phoneNumberSignature;

        /**
         * The merchant's claimed business domain, when set.
         */
        private final String businessDomain;

        /**
         * Constructs a successful reply.
         *
         * @param phoneNumber                    the optional phone
         *                                       number; may be
         *                                       {@code null}
         * @param phoneNumberSignatureExpiration the optional TTL
         *                                       timestamp; may be
         *                                       {@code null}
         * @param phoneNumberSignature           the optional signature
         *                                       blob; may be
         *                                       {@code null}
         * @param businessDomain                 the optional business
         *                                       domain claim; may be
         *                                       {@code null}
         */
        public Success(String phoneNumber, String phoneNumberSignatureExpiration,
                       String phoneNumberSignature, String businessDomain) {
            this.phoneNumber = phoneNumber;
            this.phoneNumberSignatureExpiration = phoneNumberSignatureExpiration;
            this.phoneNumberSignature = phoneNumberSignature;
            this.businessDomain = businessDomain;
        }

        /**
         * Returns the merchant's phone number, when supplied.
         *
         * @return an {@link Optional} carrying the phone number
         */
        public Optional<String> phoneNumber() {
            return Optional.ofNullable(phoneNumber);
        }

        /**
         * Returns the signature TTL timestamp, when supplied.
         *
         * @return an {@link Optional} carrying the timestamp
         */
        public Optional<String> phoneNumberSignatureExpiration() {
            return Optional.ofNullable(phoneNumberSignatureExpiration);
        }

        /**
         * Returns the opaque phone-number signature blob, when
         * supplied.
         *
         * @return an {@link Optional} carrying the signature
         */
        public Optional<String> phoneNumberSignature() {
            return Optional.ofNullable(phoneNumberSignature);
        }

        /**
         * Returns the merchant's claimed business domain, when set.
         *
         * @return an {@link Optional} carrying the business domain
         */
        public Optional<String> businessDomain() {
            return Optional.ofNullable(businessDomain);
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the success
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryGetSignedUserInfoJob",
                exports = "QueryGetSignedUserInfo", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var signedUserInfo = node.getChild("signed_user_info").orElse(null);
            if (signedUserInfo == null) {
                return Optional.of(new Success(null, null, null, null));
            }
            var phoneNumber = signedUserInfo.getChild("phone_number")
                    .flatMap(Node::toContentString).orElse(null);
            var ttl = signedUserInfo.getChild("ttl_timestamp")
                    .flatMap(Node::toContentString).orElse(null);
            var signature = signedUserInfo.getChild("phone_number_signature")
                    .flatMap(Node::toContentString).orElse(null);
            var domain = signedUserInfo.getChild("business_domain")
                    .flatMap(Node::toContentString).orElse(null);
            return Optional.of(new Success(phoneNumber, ttl, signature, domain));
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
            return Objects.equals(this.phoneNumber, that.phoneNumber)
                    && Objects.equals(this.phoneNumberSignatureExpiration, that.phoneNumberSignatureExpiration)
                    && Objects.equals(this.phoneNumberSignature, that.phoneNumberSignature)
                    && Objects.equals(this.businessDomain, that.businessDomain);
        }

        @Override
        public int hashCode() {
            return Objects.hash(phoneNumber, phoneNumberSignatureExpiration,
                    phoneNumberSignature, businessDomain);
        }

        @Override
        public String toString() {
            return "IqQueryGetSignedUserInfoResponse.Success[phoneNumber=" + phoneNumber
                    + ", phoneNumberSignatureExpiration=" + phoneNumberSignatureExpiration
                    + ", phoneNumberSignature=" + phoneNumberSignature
                    + ", businessDomain=" + businessDomain + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, or referencing a non-existent
     * business JID.
     */
    final class ClientError implements IqQueryGetSignedUserInfoResponse {
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
         *         empty when the stanza does not match the
         *         client-error schema
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
            return "IqQueryGetSignedUserInfoResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay returned a
     * transient internal-failure status while processing the request.
     */
    final class ServerError implements IqQueryGetSignedUserInfoResponse {
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
         *         empty when the stanza does not match the
         *         server-error schema
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
            return "IqQueryGetSignedUserInfoResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
