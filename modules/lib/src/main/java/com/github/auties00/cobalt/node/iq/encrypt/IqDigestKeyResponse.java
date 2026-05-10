package com.github.auties00.cobalt.node.iq.encrypt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to an {@link IqDigestKeyRequest}.
 */
@WhatsAppWebModule(moduleName = "WAWebDigestKeyJob")
public sealed interface IqDigestKeyResponse extends IqOperation.Response
        permits IqDigestKeyResponse.Success, IqDigestKeyResponse.ClientError, IqDigestKeyResponse.ServerError {

    /**
     * Tries each {@link IqDigestKeyResponse} variant in priority order and returns
     * the first that parses cleanly.
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
    @WhatsAppWebExport(moduleName = "WAWebDigestKeyJob",
            exports = "digestKey", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqDigestKeyResponse> of(Node node, Node request) {
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
     * remote-side digest of the user's Signal key bundle.
     */
    @WhatsAppWebModule(moduleName = "WAWebDigestKeyJob")
    final class Success implements IqDigestKeyResponse {
        /**
         * The remote-side registration id.
         */
        private final int registrationId;

        /**
         * The remote-side single-byte Signal key-bundle type marker.
         */
        private final byte keyBundleType;

        /**
         * The remote-side long-term identity public key — thirty-two
         * bytes.
         */
        private final byte[] identityPublicKey;

        /**
         * The remote-side current signed pre-key — id, public key,
         * signature.
         */
        private final IqUploadPreKeysSignedPreKey signedPreKey;

        /**
         * The remote-side one-time pre-key identifier list — each
         * entry carries a three-byte big-endian unsigned integer.
         */
        private final List<Integer> preKeyIds;

        /**
         * The relay's SHA-1 digest of the concatenated key material —
         * twenty bytes.
         */
        private final byte[] hash;

        /**
         * Constructs a new successful reply.
         *
         * @param registrationId    the remote-side registration id
         * @param keyBundleType     the remote-side type marker
         * @param identityPublicKey the remote-side identity key bytes;
         *                          never {@code null}
         * @param signedPreKey      the remote-side signed pre-key;
         *                          never {@code null}
         * @param preKeyIds         the remote-side pre-key identifier
         *                          list; never {@code null}
         * @param hash              the SHA-1 digest bytes; never
         *                          {@code null}
         * @throws NullPointerException if any reference argument is
         *                              {@code null}
         */
        public Success(int registrationId, byte keyBundleType,
                       byte[] identityPublicKey, IqUploadPreKeysSignedPreKey signedPreKey,
                       List<Integer> preKeyIds, byte[] hash) {
            this.registrationId = registrationId;
            this.keyBundleType = keyBundleType;
            this.identityPublicKey = Objects.requireNonNull(identityPublicKey, "identityPublicKey cannot be null");
            this.signedPreKey = Objects.requireNonNull(signedPreKey, "signedPreKey cannot be null");
            Objects.requireNonNull(preKeyIds, "preKeyIds cannot be null");
            this.preKeyIds = List.copyOf(preKeyIds);
            this.hash = Objects.requireNonNull(hash, "hash cannot be null");
        }

        /**
         * Returns the remote-side registration id.
         *
         * @return the registration id
         */
        public int registrationId() {
            return registrationId;
        }

        /**
         * Returns the remote-side key-bundle type marker.
         *
         * @return the type marker
         */
        public byte keyBundleType() {
            return keyBundleType;
        }

        /**
         * Returns the remote-side identity public key bytes.
         *
         * @return the bytes; never {@code null}
         */
        public byte[] identityPublicKey() {
            return identityPublicKey;
        }

        /**
         * Returns the remote-side signed pre-key.
         *
         * @return the signed pre-key; never {@code null}
         */
        public IqUploadPreKeysSignedPreKey signedPreKey() {
            return signedPreKey;
        }

        /**
         * Returns the unmodifiable list of remote-side pre-key
         * identifiers.
         *
         * @return the identifiers; never {@code null}
         */
        public List<Integer> preKeyIds() {
            return preKeyIds;
        }

        /**
         * Returns the relay-supplied SHA-1 digest bytes.
         *
         * @return the digest; never {@code null}
         */
        public byte[] hash() {
            return hash;
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
        @WhatsAppWebExport(moduleName = "WAWebDigestKeyJob",
                exports = "digestResponseParser", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var digestChild = node.getChild("digest").orElse(null);
            if (digestChild == null) {
                return Optional.empty();
            }
            var registrationNode = digestChild.getChild("registration").orElse(null);
            var typeNode = digestChild.getChild("type").orElse(null);
            var identityNode = digestChild.getChild("identity").orElse(null);
            var skeyNode = digestChild.getChild("skey").orElse(null);
            var listNode = digestChild.getChild("list").orElse(null);
            var hashNode = digestChild.getChild("hash").orElse(null);
            if (registrationNode == null || typeNode == null || identityNode == null
                    || skeyNode == null || listNode == null || hashNode == null) {
                return Optional.empty();
            }
            var registrationBytes = registrationNode.toContentBytes().orElse(null);
            var typeBytes = typeNode.toContentBytes().orElse(null);
            var identityBytes = identityNode.toContentBytes().orElse(null);
            var hashBytes = hashNode.toContentBytes().orElse(null);
            if (registrationBytes == null || typeBytes == null || identityBytes == null
                    || hashBytes == null || typeBytes.length < 1) {
                return Optional.empty();
            }
            var registrationId = bigEndianUnsignedInt(registrationBytes);
            var keyBundleType = typeBytes[0];
            var skeyIdNode = skeyNode.getChild("id").orElse(null);
            var skeyValueNode = skeyNode.getChild("value").orElse(null);
            var skeySignatureNode = skeyNode.getChild("signature").orElse(null);
            if (skeyIdNode == null || skeyValueNode == null || skeySignatureNode == null) {
                return Optional.empty();
            }
            var skeyIdBytes = skeyIdNode.toContentBytes().orElse(null);
            var skeyValueBytes = skeyValueNode.toContentBytes().orElse(null);
            var skeySignatureBytes = skeySignatureNode.toContentBytes().orElse(null);
            if (skeyIdBytes == null || skeyValueBytes == null || skeySignatureBytes == null) {
                return Optional.empty();
            }
            var signedPreKey = new IqUploadPreKeysSignedPreKey(
                    bigEndianUnsignedInt(skeyIdBytes), skeyValueBytes, skeySignatureBytes);
            var preKeyIds = new ArrayList<Integer>();
            for (var preKeyChild : listNode.children()) {
                var idBytes = preKeyChild.toContentBytes().orElse(null);
                if (idBytes == null) {
                    return Optional.empty();
                }
                preKeyIds.add(bigEndianUnsignedInt(idBytes));
            }
            return Optional.of(new Success(registrationId, keyBundleType, identityBytes,
                    signedPreKey, preKeyIds, hashBytes));
        }

        /**
         * Decodes the supplied byte array as a big-endian unsigned
         * integer.
         *
         * @param bytes the source bytes; may be one to four bytes long
         * @return the decoded value
         */
        private static int bigEndianUnsignedInt(byte[] bytes) {
            var result = 0;
            for (var b : bytes) {
                result = (result << 8) | (b & 0xff);
            }
            return result;
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
            return this.registrationId == that.registrationId
                    && this.keyBundleType == that.keyBundleType
                    && Arrays.equals(this.identityPublicKey, that.identityPublicKey)
                    && Objects.equals(this.signedPreKey, that.signedPreKey)
                    && Objects.equals(this.preKeyIds, that.preKeyIds)
                    && Arrays.equals(this.hash, that.hash);
        }

        @Override
        public int hashCode() {
            var result = Objects.hash(registrationId, keyBundleType, signedPreKey, preKeyIds);
            result = 31 * result + Arrays.hashCode(identityPublicKey);
            result = 31 * result + Arrays.hashCode(hash);
            return result;
        }

        @Override
        public String toString() {
            return "IqDigestKeyResponse.Success[registrationId=" + registrationId
                    + ", keyBundleType=" + keyBundleType
                    + ", identityPublicKey=" + Arrays.toString(identityPublicKey)
                    + ", signedPreKey=" + signedPreKey
                    + ", preKeyIds=" + preKeyIds
                    + ", hash=" + Arrays.toString(hash) + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * digest query as malformed (code {@code 406}) or has no record
     * for the user (code {@code 404}, "no record found").
     */
    @WhatsAppWebModule(moduleName = "WAWebDigestKeyJob")
    final class ClientError implements IqDigestKeyResponse {
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
        @WhatsAppWebExport(moduleName = "WAWebDigestKeyJob",
                exports = "digestKey", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqDigestKeyResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure (codes {@code >= 500}, including
     * {@code 503} "service unavailable") while processing the
     * digest query.
     */
    @WhatsAppWebModule(moduleName = "WAWebDigestKeyJob")
    final class ServerError implements IqDigestKeyResponse {
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
        @WhatsAppWebExport(moduleName = "WAWebDigestKeyJob",
                exports = "digestKey", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqDigestKeyResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
