package com.github.auties00.cobalt.node.iq.stats;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link IqIssuePrivateStatsTokenRequest}.
 */
public sealed interface IqIssuePrivateStatsTokenResponse extends IqOperation.Response
        permits IqIssuePrivateStatsTokenResponse.Success, IqIssuePrivateStatsTokenResponse.ClientError, IqIssuePrivateStatsTokenResponse.ServerError {

    /**
     * Tries each {@link IqIssuePrivateStatsTokenResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay.
     *                Never {@code null}
     * @param request the original outbound stanza. Used to
     *                validate echoed identifiers. Never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxPrivatestatsSignCredentialRPC",
            exports = "sendSignCredentialRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqIssuePrivateStatsTokenResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay accepted the
     * blinded credential and returned a signed credential together
     * with the DLEQ proof and the relay's published public key.
     *
     * <p>The signed credential, the public key, and both DLEQ
     * coordinates ({@code c}, {@code s}) are 32-byte elliptic-curve
     * scalars. The {@code signCredentialT} timestamp records the
     * relay-side wall-clock when the signature was minted.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsSignCredentialResponseSuccess")
    final class Success implements IqIssuePrivateStatsTokenResponse {
        /**
         * Relay-side mint timestamp (seconds since epoch).
         */
        private final long signCredentialT;

        /**
         * Raw 32-byte signed-credential elliptic-curve scalar.
         */
        private final byte[] signedCredential;

        /**
         * Raw 32-byte ACS public key the relay published when
         * signing the credential.
         */
        private final byte[] acsPublicKey;

        /**
         * Raw 32-byte DLEQ proof {@code c} coordinate.
         */
        private final byte[] dleqProofC;

        /**
         * Raw 32-byte DLEQ proof {@code s} coordinate.
         */
        private final byte[] dleqProofS;

        /**
         * Echoed project-name string (UTF-8) carried under the
         * {@code <project_name>} grandchild.
         */
        private final String projectName;

        /**
         * Constructs a new successful reply.
         *
         * @param signCredentialT  relay-side mint timestamp
         * @param signedCredential the 32-byte signed credential
         *                         scalar
         * @param acsPublicKey     the 32-byte ACS public key
         * @param dleqProofC       the 32-byte DLEQ proof {@code c}
         *                         coordinate
         * @param dleqProofS       the 32-byte DLEQ proof {@code s}
         *                         coordinate
         * @param projectName      the echoed project name. Never
         *                         {@code null}
         * @throws NullPointerException if any byte-array argument
         *                              or {@code projectName} is
         *                              {@code null}
         */
        public Success(long signCredentialT, byte[] signedCredential, byte[] acsPublicKey,
                       byte[] dleqProofC, byte[] dleqProofS, String projectName) {
            this.signCredentialT = signCredentialT;
            this.signedCredential = Objects.requireNonNull(signedCredential, "signedCredential cannot be null").clone();
            this.acsPublicKey = Objects.requireNonNull(acsPublicKey, "acsPublicKey cannot be null").clone();
            this.dleqProofC = Objects.requireNonNull(dleqProofC, "dleqProofC cannot be null").clone();
            this.dleqProofS = Objects.requireNonNull(dleqProofS, "dleqProofS cannot be null").clone();
            this.projectName = Objects.requireNonNull(projectName, "projectName cannot be null");
        }

        /**
         * Returns the relay-side mint timestamp.
         *
         * @return the mint timestamp in seconds since epoch
         */
        public long signCredentialT() {
            return signCredentialT;
        }

        /**
         * Returns a defensive copy of the signed-credential bytes.
         *
         * @return a clone of the signed-credential scalar. Never
         *         {@code null}
         */
        public byte[] signedCredential() {
            return signedCredential.clone();
        }

        /**
         * Returns a defensive copy of the ACS public key bytes.
         *
         * @return a clone of the public-key scalar. Never
         *         {@code null}
         */
        public byte[] acsPublicKey() {
            return acsPublicKey.clone();
        }

        /**
         * Returns a defensive copy of the DLEQ proof {@code c}
         * coordinate.
         *
         * @return a clone of the {@code c} scalar. Never
         *         {@code null}
         */
        public byte[] dleqProofC() {
            return dleqProofC.clone();
        }

        /**
         * Returns a defensive copy of the DLEQ proof {@code s}
         * coordinate.
         *
         * @return a clone of the {@code s} scalar. Never
         *         {@code null}
         */
        public byte[] dleqProofS() {
            return dleqProofS.clone();
        }

        /**
         * Returns the echoed project name.
         *
         * @return the project-name string. Never {@code null}
         */
        public String projectName() {
            return projectName;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPrivatestatsSignCredentialResponseSuccess",
                exports = "parseSignCredentialResponseSuccess", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var signCredential = node.getChild("sign_credential").orElse(null);
            if (signCredential == null) {
                return Optional.empty();
            }
            var t = signCredential.getAttributeAsLong("t", -1L);
            if (t < 0L) {
                return Optional.empty();
            }
            var signedCredentialBytes = signCredential.getChild("signed_credential")
                    .flatMap(Node::toContentBytes)
                    .orElse(null);
            if (signedCredentialBytes == null || signedCredentialBytes.length != 32) {
                return Optional.empty();
            }
            var acsPublicKeyBytes = signCredential.getChild("acs_public_key")
                    .flatMap(Node::toContentBytes)
                    .orElse(null);
            if (acsPublicKeyBytes == null || acsPublicKeyBytes.length != 32) {
                return Optional.empty();
            }
            var dleqProof = signCredential.getChild("dleq_proof").orElse(null);
            if (dleqProof == null) {
                return Optional.empty();
            }
            var dleqCBytes = dleqProof.getChild("c")
                    .flatMap(Node::toContentBytes)
                    .orElse(null);
            if (dleqCBytes == null || dleqCBytes.length != 32) {
                return Optional.empty();
            }
            var dleqSBytes = dleqProof.getChild("s")
                    .flatMap(Node::toContentBytes)
                    .orElse(null);
            if (dleqSBytes == null || dleqSBytes.length != 32) {
                return Optional.empty();
            }
            var projectNameValue = signCredential.getChild("project_name")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            if (projectNameValue == null) {
                return Optional.empty();
            }
            return Optional.of(new Success(t, signedCredentialBytes, acsPublicKeyBytes,
                    dleqCBytes, dleqSBytes, projectNameValue));
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
            return this.signCredentialT == that.signCredentialT
                    && Arrays.equals(this.signedCredential, that.signedCredential)
                    && Arrays.equals(this.acsPublicKey, that.acsPublicKey)
                    && Arrays.equals(this.dleqProofC, that.dleqProofC)
                    && Arrays.equals(this.dleqProofS, that.dleqProofS)
                    && Objects.equals(this.projectName, that.projectName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(signCredentialT, Arrays.hashCode(signedCredential),
                    Arrays.hashCode(acsPublicKey), Arrays.hashCode(dleqProofC),
                    Arrays.hashCode(dleqProofS), projectName);
        }

        @Override
        public String toString() {
            return "IqIssuePrivateStatsTokenResponse.Success[signCredentialT=" + signCredentialT
                    + ", signedCredentialLength=" + signedCredential.length
                    + ", acsPublicKeyLength=" + acsPublicKey.length
                    + ", dleqProofCLength=" + dleqProofC.length
                    + ", dleqProofSLength=" + dleqProofS.length
                    + ", projectName=" + projectName + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected
     * the credential issuance request as malformed or otherwise
     * non-retryable.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsSignCredentialResponseErrorNoRetry")
    final class ClientError implements IqIssuePrivateStatsTokenResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied
         * one.
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
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPrivatestatsSignCredentialResponseErrorNoRetry",
                exports = "parseSignCredentialResponseErrorNoRetry",
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
            return "IqIssuePrivateStatsTokenResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered
     * a transient internal failure while issuing the credential.
     * The caller may retry after a backoff.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsSignCredentialResponseErrorRetry")
    final class ServerError implements IqIssuePrivateStatsTokenResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied
         * one.
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
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPrivatestatsSignCredentialResponseErrorRetry",
                exports = "parseSignCredentialResponseErrorRetry",
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
            return "IqIssuePrivateStatsTokenResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
