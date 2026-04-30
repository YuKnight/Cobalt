package com.github.auties00.cobalt.node.smax.privatestats;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxPrivatestatsSignCredentialRPC.sendSignCredentialRPC}
 *           tries {@code Success} → {@code ErrorNoRetry} →
 *           {@code ErrorRetry}.
 */
public sealed interface SmaxPrivatestatsSignCredentialResponse extends SmaxOperation.Response
        permits SmaxPrivatestatsSignCredentialResponse.Success, SmaxPrivatestatsSignCredentialResponse.ErrorNoRetry, SmaxPrivatestatsSignCredentialResponse.ErrorRetry {

    /**
     * Tries each {@link SmaxPrivatestatsSignCredentialResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza. Never {@code null}
     * @param request the original outbound stanza. Never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxPrivatestatsSignCredentialRPC",
            exports = "sendSignCredentialRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxPrivatestatsSignCredentialResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var noRetry = ErrorNoRetry.of(node, request);
        if (noRetry.isPresent()) {
            return noRetry;
        }
        return ErrorRetry.of(node, request);
    }

    /**
     * The {@code Success} reply variant. The relay signed the
     * blinded credential and returned the signature, the ACS public
     * key, and the DLEQ proof bytes.
     *
     * @implNote {@code WASmaxInPrivatestatsSignCredentialResponseSuccess.parseSignCredentialResponseSuccess}
     *           validates the IQ-result envelope (echoed
     *           {@code id} / {@code from=request.to} / {@code type="result"}),
     *           then projects the {@code <sign_credential t>} child
     *           plus its grandchildren:
     *           {@code <signed_credential>{32 bytes}</signed_credential>},
     *           {@code <acs_public_key>{32 bytes}</acs_public_key>},
     *           {@code <dleq_proof>}{@code <c>{32 bytes}</c>}{@code <s>{32 bytes}</s></dleq_proof>},
     *           {@code <project_name>{string}</project_name>}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsSignCredentialResponseSuccess")
    final class Success implements SmaxPrivatestatsSignCredentialResponse {
        /**
         * The Unix-epoch timestamp echoed on the
         * {@code <sign_credential>} child's {@code t} attribute.
         */
        private final long signCredentialT;

        /**
         * The 32-byte signed credential bytes.
         */
        private final byte[] signedCredentialElementValue;

        /**
         * The 32-byte ACS public key bytes.
         */
        private final byte[] acsPublicKeyElementValue;

        /**
         * The 32-byte DLEQ proof {@code c} component.
         */
        private final byte[] dleqProofCElementValue;

        /**
         * The 32-byte DLEQ proof {@code s} component.
         */
        private final byte[] dleqProofSElementValue;

        /**
         * The project-name echo.
         */
        private final String projectNameElementValue;

        /**
         * Constructs a new success reply.
         *
         * @param signCredentialT             the timestamp
         * @param signedCredentialElementValue the signed credential
         *                                    bytes. Never
         *                                    {@code null}. Exactly
         *                                    {@code 32} bytes
         * @param acsPublicKeyElementValue    the ACS public key
         *                                    bytes. Never
         *                                    {@code null}. Exactly
         *                                    {@code 32} bytes
         * @param dleqProofCElementValue      the DLEQ proof
         *                                    {@code c} bytes;
         *                                    never {@code null};
         *                                    exactly {@code 32}
         *                                    bytes
         * @param dleqProofSElementValue      the DLEQ proof
         *                                    {@code s} bytes;
         *                                    never {@code null};
         *                                    exactly {@code 32}
         *                                    bytes
         * @param projectNameElementValue     the project name echo;
         *                                    never {@code null}
         * @throws NullPointerException if any required argument is
         *                              {@code null}
         */
        public Success(long signCredentialT,
                       byte[] signedCredentialElementValue,
                       byte[] acsPublicKeyElementValue,
                       byte[] dleqProofCElementValue,
                       byte[] dleqProofSElementValue,
                       String projectNameElementValue) {
            this.signCredentialT = signCredentialT;
            this.signedCredentialElementValue = Objects.requireNonNull(signedCredentialElementValue,
                    "signedCredentialElementValue cannot be null");
            this.acsPublicKeyElementValue = Objects.requireNonNull(acsPublicKeyElementValue,
                    "acsPublicKeyElementValue cannot be null");
            this.dleqProofCElementValue = Objects.requireNonNull(dleqProofCElementValue,
                    "dleqProofCElementValue cannot be null");
            this.dleqProofSElementValue = Objects.requireNonNull(dleqProofSElementValue,
                    "dleqProofSElementValue cannot be null");
            this.projectNameElementValue = Objects.requireNonNull(projectNameElementValue,
                    "projectNameElementValue cannot be null");
        }

        /**
         * Returns the timestamp echo.
         *
         * @return the timestamp
         */
        public long signCredentialT() {
            return signCredentialT;
        }

        /**
         * Returns the signed credential bytes.
         *
         * @return the bytes. Never {@code null}
         */
        public byte[] signedCredentialElementValue() {
            return signedCredentialElementValue;
        }

        /**
         * Returns the ACS public key bytes.
         *
         * @return the bytes. Never {@code null}
         */
        public byte[] acsPublicKeyElementValue() {
            return acsPublicKeyElementValue;
        }

        /**
         * Returns the DLEQ proof {@code c} component.
         *
         * @return the bytes. Never {@code null}
         */
        public byte[] dleqProofCElementValue() {
            return dleqProofCElementValue;
        }

        /**
         * Returns the DLEQ proof {@code s} component.
         *
         * @return the bytes. Never {@code null}
         */
        public byte[] dleqProofSElementValue() {
            return dleqProofSElementValue;
        }

        /**
         * Returns the project-name echo.
         *
         * @return the project name. Never {@code null}
         */
        public String projectNameElementValue() {
            return projectNameElementValue;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPrivatestatsSignCredentialResponseSuccess",
                exports = "parseSignCredentialResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var signCredential = node.getChild("sign_credential").orElse(null);
            if (signCredential == null) {
                return Optional.empty();
            }
            var t = signCredential.getAttributeAsLong("t");
            if (t.isEmpty() || t.getAsLong() < 0) {
                return Optional.empty();
            }
            var signedCredential = signCredential.getChild("signed_credential")
                    .flatMap(Node::toContentBytes)
                    .orElse(null);
            if (signedCredential == null || signedCredential.length != 32) {
                return Optional.empty();
            }
            var acsPublicKey = signCredential.getChild("acs_public_key")
                    .flatMap(Node::toContentBytes)
                    .orElse(null);
            if (acsPublicKey == null || acsPublicKey.length != 32) {
                return Optional.empty();
            }
            var dleqProof = signCredential.getChild("dleq_proof").orElse(null);
            if (dleqProof == null) {
                return Optional.empty();
            }
            var dleqProofC = dleqProof.getChild("c")
                    .flatMap(Node::toContentBytes)
                    .orElse(null);
            if (dleqProofC == null || dleqProofC.length != 32) {
                return Optional.empty();
            }
            var dleqProofS = dleqProof.getChild("s")
                    .flatMap(Node::toContentBytes)
                    .orElse(null);
            if (dleqProofS == null || dleqProofS.length != 32) {
                return Optional.empty();
            }
            var projectName = signCredential.getChild("project_name")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            if (projectName == null) {
                return Optional.empty();
            }
            return Optional.of(new Success(t.getAsLong(), signedCredential, acsPublicKey,
                    dleqProofC, dleqProofS, projectName));
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
                    && Arrays.equals(this.signedCredentialElementValue, that.signedCredentialElementValue)
                    && Arrays.equals(this.acsPublicKeyElementValue, that.acsPublicKeyElementValue)
                    && Arrays.equals(this.dleqProofCElementValue, that.dleqProofCElementValue)
                    && Arrays.equals(this.dleqProofSElementValue, that.dleqProofSElementValue)
                    && Objects.equals(this.projectNameElementValue, that.projectNameElementValue);
        }

        @Override
        public int hashCode() {
            var result = Objects.hash(signCredentialT, projectNameElementValue);
            result = 31 * result + Arrays.hashCode(signedCredentialElementValue);
            result = 31 * result + Arrays.hashCode(acsPublicKeyElementValue);
            result = 31 * result + Arrays.hashCode(dleqProofCElementValue);
            result = 31 * result + Arrays.hashCode(dleqProofSElementValue);
            return result;
        }

        @Override
        public String toString() {
            return "SmaxPrivatestatsSignCredentialResponse.Success[signCredentialT=" + signCredentialT
                    + ", signedCredentialElementValue="
                    + Arrays.toString(signedCredentialElementValue)
                    + ", acsPublicKeyElementValue="
                    + Arrays.toString(acsPublicKeyElementValue)
                    + ", dleqProofCElementValue=" + Arrays.toString(dleqProofCElementValue)
                    + ", dleqProofSElementValue=" + Arrays.toString(dleqProofSElementValue)
                    + ", projectNameElementValue=" + projectNameElementValue + ']';
        }
    }

    /**
     * The {@code ErrorNoRetry} reply variant. A permanent
     * rejection that the local client must NOT retry.
     *
     * <p>Carries one of three documented {@code (code, text)} pairs:
     * {@code (400, "bad-request")},
     * {@code (501, "feature-not-implemented")}, or
     * {@code (503, "service-unavailable")}.
     *
     * @implNote {@code WASmaxInPrivatestatsSignCredentialResponseErrorNoRetry.parseSignCredentialResponseErrorNoRetry}
     *           projects the {@code <error/>} child through
     *           {@code WASmaxInPrivatestatsSignCredentialNoRetryError},
     *           a disjunction over {@code IQErrorBadRequest},
     *           {@code IQErrorFeatureNotImplemented}, and
     *           {@code IQErrorServiceUnavailable}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsSignCredentialResponseErrorNoRetry")
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsSignCredentialNoRetryError")
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsIQErrorBadRequestMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsIQErrorFeatureNotImplementedMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsIQErrorServiceUnavailableMixin")
    final class ErrorNoRetry implements SmaxPrivatestatsSignCredentialResponse {
        /**
         * The numeric error code. One of {@code 400}, {@code 501},
         * or {@code 503}.
         */
        private final int errorCode;

        /**
         * The error text. One of {@code "bad-request"},
         * {@code "feature-not-implemented"}, or
         * {@code "service-unavailable"}.
         */
        private final String errorText;

        /**
         * Constructs a new no-retry error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional error text. May be
         *                  {@code null}
         */
        public ErrorNoRetry(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse an {@link ErrorNoRetry} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPrivatestatsSignCredentialResponseErrorNoRetry",
                exports = "parseSignCredentialResponseErrorNoRetry",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ErrorNoRetry> of(Node node, Node request) {
            // 4xx → ClientError envelope, 5xx → ServerError envelope.
            var clientEnvelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            var serverEnvelope = clientEnvelope == null
                    ? SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null)
                    : null;
            var envelope = clientEnvelope != null ? clientEnvelope : serverEnvelope;
            if (envelope == null) {
                return Optional.empty();
            }
            var code = envelope.code();
            var text = envelope.text();
            // Validate the code/text pair against the documented disjunction.
            if ((code == 400 && "bad-request".equals(text))
                    || (code == 501 && "feature-not-implemented".equals(text))
                    || (code == 503 && "service-unavailable".equals(text))) {
                return Optional.of(new ErrorNoRetry(code, text));
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
            var that = (ErrorNoRetry) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxPrivatestatsSignCredentialResponse.ErrorNoRetry[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ErrorRetry} reply variant. A transient
     * {@code 500 internal-server-error} rejection. The local client
     * may retry on the next flush window.
     *
     * @implNote {@code WASmaxInPrivatestatsSignCredentialResponseErrorRetry.parseSignCredentialResponseErrorRetry}
     *           projects the {@code <error/>} child through
     *           {@code WASmaxInPrivatestatsIQErrorInternalServerErrorMixin}
     *           which asserts the literal
     *           {@code (code=500, text="internal-server-error")}
     *           pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsSignCredentialResponseErrorRetry")
    @WhatsAppWebModule(moduleName = "WASmaxInPrivatestatsIQErrorInternalServerErrorMixin")
    final class ErrorRetry implements SmaxPrivatestatsSignCredentialResponse {
        /**
         * Constructs a new retry-error reply. The shape carries no
         * payload beyond the asserted {@code (500,
         * "internal-server-error")} pair.
         */
        public ErrorRetry() {
        }

        /**
         * Returns the numeric error code. Always {@code 500}.
         *
         * @return the code
         */
        public int errorCode() {
            return 500;
        }

        /**
         * Returns the error text. Always
         * {@code "internal-server-error"}.
         *
         * @return the text
         */
        public String errorText() {
            return "internal-server-error";
        }

        /**
         * Tries to parse an {@link ErrorRetry} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPrivatestatsSignCredentialResponseErrorRetry",
                exports = "parseSignCredentialResponseErrorRetry",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ErrorRetry> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            if (envelope.code() != 500 || !"internal-server-error".equals(envelope.text())) {
                return Optional.empty();
            }
            return Optional.of(new ErrorRetry());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return ErrorRetry.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxPrivatestatsSignCredentialResponse.ErrorRetry[]";
        }
    }
}
