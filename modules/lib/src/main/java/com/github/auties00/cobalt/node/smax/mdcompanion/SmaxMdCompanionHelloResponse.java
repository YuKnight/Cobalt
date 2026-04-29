package com.github.auties00.cobalt.node.smax.mdcompanion;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqErrorResponseMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxMdCompanionHelloRequest}.
 *
 * @implNote {@code WASmaxMdCompanionHelloRPC.sendCompanionHelloRPC}
 *           tries {@code NotifyCompanion} → {@code Error} in order;
 *           the {@code ServerError} variant is layered on Cobalt's
 *           side via {@link SmaxBaseServerErrorMixin} for parity with
 *           the rest of the SMAX domain shape.
 */
public sealed interface SmaxMdCompanionHelloResponse extends SmaxOperation.Response
        permits SmaxMdCompanionHelloResponse.NotifyCompanion, SmaxMdCompanionHelloResponse.ClientError, SmaxMdCompanionHelloResponse.ServerError {

    /**
     * Tries each {@link SmaxMdCompanionHelloResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no documented variant matched
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxMdCompanionHelloRPC",
            exports = "sendCompanionHelloRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxMdCompanionHelloResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var notifyCompanion = NotifyCompanion.of(node, request);
        if (notifyCompanion.isPresent()) {
            return notifyCompanion;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code NotifyCompanion} reply variant — the relay accepted
     * the hello and echoes back a {@code link_code_pairing_ref} that
     * the user types on the primary device.
     *
     * @implNote {@code WASmaxInMdCompanionHelloResponseNotifyCompanion.parseCompanionHelloResponseNotifyCompanion}
     *           validates the {@code <iq type="result">} envelope,
     *           asserts {@code stage="companion_hello"} on the
     *           {@code <link_code_companion_reg/>} child, and extracts
     *           the {@code <link_code_pairing_ref/>} content bytes.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInMdCompanionHelloResponseNotifyCompanion")
    final class NotifyCompanion implements SmaxMdCompanionHelloResponse {
        /**
         * The pairing-reference bytes echoed by the relay.
         */
        private final byte[] linkCodePairingRef;

        /**
         * Constructs a new {@code NotifyCompanion} reply.
         *
         * @param linkCodePairingRef the pairing-reference bytes; never {@code null}
         * @throws NullPointerException if {@code linkCodePairingRef} is {@code null}
         */
        public NotifyCompanion(byte[] linkCodePairingRef) {
            this.linkCodePairingRef = Objects.requireNonNull(linkCodePairingRef, "linkCodePairingRef cannot be null");
        }

        /**
         * Returns the pairing-reference bytes.
         *
         * @return the bytes; never {@code null}
         */
        public byte[] linkCodePairingRef() {
            return linkCodePairingRef;
        }

        /**
         * Tries to parse a {@link NotifyCompanion} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInMdCompanionHelloResponseNotifyCompanion",
                exports = "parseCompanionHelloResponseNotifyCompanion",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<NotifyCompanion> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var reg = node.getChild("link_code_companion_reg").orElse(null);
            if (reg == null || !reg.hasAttribute("stage", "companion_hello")) {
                return Optional.empty();
            }
            var refChild = reg.getChild("link_code_pairing_ref").orElse(null);
            if (refChild == null) {
                return Optional.empty();
            }
            var refBytes = refChild.toContentBytes().orElse(null);
            if (refBytes == null) {
                return Optional.empty();
            }
            return Optional.of(new NotifyCompanion(refBytes));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (NotifyCompanion) obj;
            return Arrays.equals(this.linkCodePairingRef, that.linkCodePairingRef);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(linkCodePairingRef);
        }

        @Override
        public String toString() {
            return "SmaxMdCompanionHelloResponse.NotifyCompanion[linkCodePairingRef="
                    + Arrays.toString(linkCodePairingRef) + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request with a {@code 4xx} {@code IqMixin} error
     * (bad-request, forbidden, rate-overlimit, feature-not-available,
     * or internal-server-error mapped to client range).
     *
     * @implNote {@code WASmaxInMdCompanionHelloResponseError.parseCompanionHelloResponseError}
     *           composes
     *           {@code WASmaxInMdIQErrorResponseMixin.parseIQErrorResponseMixin}
     *           with
     *           {@code WASmaxInMdIqMixinErrors.parseIqMixinErrors}.
     *           Cobalt collapses to the bare {@code (code, text)} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInMdCompanionHelloResponseError")
    final class ClientError implements SmaxMdCompanionHelloResponse {
        /**
         * The numeric server-side error code.
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
         * @return an {@link Optional} carrying the text, or empty
         *         when the relay omitted it
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
        @WhatsAppWebExport(moduleName = "WASmaxInMdCompanionHelloResponseError",
                exports = "parseCompanionHelloResponseError",
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
            return "SmaxMdCompanionHelloResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * {@code 5xx} transient internal failure while processing the
     * request.
     *
     * @implNote Layered onto Cobalt's domain via the shared
     *           {@link SmaxBaseServerErrorMixin}; WA Web folds the
     *           same {@code 500} code into the {@code IqMixin}
     *           disjunction.
     */
    final class ServerError implements SmaxMdCompanionHelloResponse {
        /**
         * The numeric server-side error code.
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
         * @return an {@link Optional} carrying the text, or empty
         *         when the relay omitted it
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
            return "SmaxMdCompanionHelloResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
