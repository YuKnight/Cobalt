package com.github.auties00.cobalt.node.smax.waffle;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay.
 */
public sealed interface SmaxWaffleGetCertificateResponse extends SmaxOperation.Response
        permits SmaxWaffleGetCertificateResponse.Success, SmaxWaffleGetCertificateResponse.ClientError, SmaxWaffleGetCertificateResponse.ServerError {

    /**
     * Tries each {@link SmaxWaffleGetCertificateResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxWaffleGetCertificateRPC",
            exports = "sendGetCertificateRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxWaffleGetCertificateResponse> of(Node node, Node request) {
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
     * Typed projection of one PEM child carried by the success reply
     *. The encryption / signature / password PEM family share the
     * same {@code (ttl, keyId?, content-bytes)} shape.
     *
     * @param ttl     the TTL in seconds; always positive
     * @param keyId   the key id (only set on the password PEM)
     * @param pem     the raw PEM bytes
     */
    record Pem(int ttl, Integer keyId, byte[] pem) {

        /**
         * Returns the optional key id.
         *
         * @return an {@link Optional} carrying the key id, or empty
         */
        public Optional<Integer> keyIdAsOptional() {
            return Optional.ofNullable(keyId);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Pem) obj;
            return this.ttl == that.ttl
                    && Objects.equals(this.keyId, that.keyId)
                    && Arrays.equals(this.pem, that.pem);
        }

        @Override
        public int hashCode() {
            var result = Objects.hash(ttl, keyId);
            result = 31 * result + Arrays.hashCode(pem);
            return result;
        }

        @Override
        public String toString() {
            return "SmaxWaffleGetCertificateResponse.Pem[ttl=" + ttl
                    + ", keyId=" + keyId
                    + ", pem=" + (pem != null ? pem.length + " bytes" : "null") + ']';
        }
    }

    /**
     * The {@code Success} reply variant. The relay returned the
     * requested PEM subset.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleGetCertificateResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleGetCertificateResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleIQResultResponseMixin")
    final class Success implements SmaxWaffleGetCertificateResponse {
        /**
         * The relay-stamped reply timestamp, in seconds since the
         * UNIX epoch.
         */
        private final long replyTimestamp;

        /**
         * The encryption PEM, when the relay supplied one.
         */
        private final Pem encryptionPem;

        /**
         * The signature PEM, when the relay supplied one.
         */
        private final Pem signaturePem;

        /**
         * The password PEM, when the relay supplied one.
         */
        private final Pem passwordPem;

        /**
         * Constructs a new success projection.
         *
         * @param replyTimestamp the relay-stamped reply timestamp
         * @param encryptionPem  the encryption PEM, or {@code null}
         * @param signaturePem   the signature PEM, or {@code null}
         * @param passwordPem    the password PEM, or {@code null}
         */
        public Success(long replyTimestamp, Pem encryptionPem, Pem signaturePem, Pem passwordPem) {
            this.replyTimestamp = replyTimestamp;
            this.encryptionPem = encryptionPem;
            this.signaturePem = signaturePem;
            this.passwordPem = passwordPem;
        }

        /**
         * Returns the relay-stamped reply timestamp.
         *
         * @return the UNIX epoch seconds
         */
        public long replyTimestamp() {
            return replyTimestamp;
        }

        /**
         * Returns the encryption PEM, when the relay supplied one.
         *
         * @return an {@link Optional} carrying the PEM, or empty
         */
        public Optional<Pem> encryptionPem() {
            return Optional.ofNullable(encryptionPem);
        }

        /**
         * Returns the signature PEM, when the relay supplied one.
         *
         * @return an {@link Optional} carrying the PEM, or empty
         */
        public Optional<Pem> signaturePem() {
            return Optional.ofNullable(signaturePem);
        }

        /**
         * Returns the password PEM, when the relay supplied one.
         *
         * @return an {@link Optional} carrying the PEM, or empty
         */
        public Optional<Pem> passwordPem() {
            return Optional.ofNullable(passwordPem);
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInWaffleGetCertificateResponseSuccess",
                exports = "parseGetCertificateResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var reply = node.getChild("reply").orElse(null);
            if (reply == null) {
                return Optional.empty();
            }
            var timestamp = reply.getAttributeAsLong("timestamp").orElse(-1L);
            if (timestamp < 1) {
                return Optional.empty();
            }
            var encryptionPem = parsePem(reply.getChild("encryption_pem").orElse(null));
            var signaturePem = parsePem(reply.getChild("signature_pem").orElse(null));
            var passwordPem = parsePem(reply.getChild("password_pem").orElse(null));
            return Optional.of(new Success(timestamp, encryptionPem, signaturePem, passwordPem));
        }

        /**
         * Parses a single PEM child node into a {@link Pem} record,
         * returning {@code null} when the node is missing or
         * malformed.
         *
         * @param pemNode the PEM child node; may be {@code null}
         * @return the parsed PEM, or {@code null}
         */
        private static Pem parsePem(Node pemNode) {
            if (pemNode == null) {
                return null;
            }
            var ttl = pemNode.getAttributeAsInt("ttl").orElse(-1);
            if (ttl < 1) {
                return null;
            }
            var keyId = pemNode.getAttributeAsInt("key_id").isPresent()
                    ? pemNode.getAttributeAsInt("key_id").getAsInt()
                    : null;
            var bytes = pemNode.toContentBytes().orElse(null);
            if (bytes == null) {
                return null;
            }
            return new Pem(ttl, keyId, bytes);
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
            return this.replyTimestamp == that.replyTimestamp
                    && Objects.equals(this.encryptionPem, that.encryptionPem)
                    && Objects.equals(this.signaturePem, that.signaturePem)
                    && Objects.equals(this.passwordPem, that.passwordPem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(replyTimestamp, encryptionPem, signaturePem, passwordPem);
        }

        @Override
        public String toString() {
            return "SmaxWaffleGetCertificateResponse.Success[replyTimestamp=" + replyTimestamp
                    + ", encryptionPem=" + encryptionPem
                    + ", signaturePem=" + signaturePem
                    + ", passwordPem=" + passwordPem + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleGetCertificateResponseError")
    final class ClientError implements SmaxWaffleGetCertificateResponse {
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
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the text, or empty
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
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInWaffleGetCertificateResponseError",
                exports = "parseGetCertificateResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "SmaxWaffleGetCertificateResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleGetCertificateResponseError")
    final class ServerError implements SmaxWaffleGetCertificateResponse {
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
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the text, or empty
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
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInWaffleGetCertificateResponseError",
                exports = "parseGetCertificateResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "SmaxWaffleGetCertificateResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
