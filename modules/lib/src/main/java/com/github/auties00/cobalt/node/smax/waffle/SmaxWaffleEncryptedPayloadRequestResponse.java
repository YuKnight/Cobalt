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
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay.
 */
public sealed interface SmaxWaffleEncryptedPayloadRequestResponse extends SmaxOperation.Response
        permits SmaxWaffleEncryptedPayloadRequestResponse.Success, SmaxWaffleEncryptedPayloadRequestResponse.ClientError, SmaxWaffleEncryptedPayloadRequestResponse.ServerError {

    /**
     * Tries each {@link SmaxWaffleEncryptedPayloadRequestResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxWaffleEncryptedPayloadRequestRPC",
            exports = "sendEncryptedPayloadRequestRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxWaffleEncryptedPayloadRequestResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — the relay returned the
     * encrypted Facebook-side response plus an optional
     * {@code wf_deleted} marker.
     *
     * @implNote {@code WASmaxInWaffleEncryptedPayloadRequestResponseSuccess.parseEncryptedPayloadRequestResponseSuccess}
     *           projects the {@code <encryption_metadata/>} subtree
     *           plus an optional
     *           {@code <wf_deleted>true|false</wf_deleted>} child.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleEncryptedPayloadRequestResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleIQResultResponseMixin")
    final class Success implements SmaxWaffleEncryptedPayloadRequestResponse {
        /**
         * The relay-returned encryption metadata.
         */
        private final SmaxWaffleRsaEncryptionMetadata encryptionMetadata;

        /**
         * The {@code <wf_deleted/>} flag, when the relay surfaced
         * one.
         */
        private final Boolean wfDeleted;

        /**
         * Constructs a new success projection.
         *
         * @param encryptionMetadata the relay-returned metadata; never
         *                           {@code null}
         * @param wfDeleted          the wf-deleted flag, or
         *                           {@code null}
         * @throws NullPointerException if {@code encryptionMetadata}
         *                              is {@code null}
         */
        public Success(SmaxWaffleRsaEncryptionMetadata encryptionMetadata, Boolean wfDeleted) {
            this.encryptionMetadata = Objects.requireNonNull(encryptionMetadata, "encryptionMetadata cannot be null");
            this.wfDeleted = wfDeleted;
        }

        /**
         * Returns the relay-returned encryption metadata.
         *
         * @return the metadata; never {@code null}
         */
        public SmaxWaffleRsaEncryptionMetadata encryptionMetadata() {
            return encryptionMetadata;
        }

        /**
         * Returns the {@code wf_deleted} flag, when the relay
         * surfaced one.
         *
         * @return an {@link Optional} carrying the flag, or empty
         */
        public Optional<Boolean> wfDeleted() {
            return Optional.ofNullable(wfDeleted);
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInWaffleEncryptedPayloadRequestResponseSuccess",
                exports = "parseEncryptedPayloadRequestResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var encryptionMetadataNode = node.getChild("encryption_metadata").orElse(null);
            if (encryptionMetadataNode == null) {
                return Optional.empty();
            }
            var metadata = SmaxWaffleRsaEncryptionMetadata.of(encryptionMetadataNode).orElse(null);
            if (metadata == null) {
                return Optional.empty();
            }
            Boolean wfDeleted = null;
            var wfDeletedNode = node.getChild("wf_deleted").orElse(null);
            if (wfDeletedNode != null) {
                var content = wfDeletedNode.toContentString().map(String::trim).orElse(null);
                if (content != null) {
                    wfDeleted = "true".equalsIgnoreCase(content);
                }
            }
            return Optional.of(new Success(metadata, wfDeleted));
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
            return Objects.equals(this.encryptionMetadata, that.encryptionMetadata)
                    && Objects.equals(this.wfDeleted, that.wfDeleted);
        }

        @Override
        public int hashCode() {
            return Objects.hash(encryptionMetadata, wfDeleted);
        }

        @Override
        public String toString() {
            return "SmaxWaffleEncryptedPayloadRequestResponse.Success[encryptionMetadata="
                    + encryptionMetadata
                    + ", wfDeleted=" + wfDeleted + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleEncryptedPayloadRequestResponseError")
    final class ClientError implements SmaxWaffleEncryptedPayloadRequestResponse {
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
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInWaffleEncryptedPayloadRequestResponseError",
                exports = "parseEncryptedPayloadRequestResponseError",
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
            return "SmaxWaffleEncryptedPayloadRequestResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleEncryptedPayloadRequestResponseError")
    final class ServerError implements SmaxWaffleEncryptedPayloadRequestResponse {
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
         * Tries to parse a {@link ServerError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInWaffleEncryptedPayloadRequestResponseError",
                exports = "parseEncryptedPayloadRequestResponseError",
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
            return "SmaxWaffleEncryptedPayloadRequestResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
