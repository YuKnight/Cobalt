package com.github.auties00.cobalt.node.smax.psa;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxPsaChatBlockSetRPC.sendChatBlockSetRPC} tries
 *           {@code Success} → {@code ServerError} in order and throws
 *           on no-match. Cobalt returns {@link Optional#empty()} on
 *           no-match.
 */
public sealed interface SmaxPsaChatBlockSetResponse extends SmaxOperation.Response
        permits SmaxPsaChatBlockSetResponse.Success, SmaxPsaChatBlockSetResponse.ServerError {

    /**
     * Tries each {@link SmaxPsaChatBlockSetResponse} variant in priority order and
     * returns the first that parses cleanly.
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
    @WhatsAppWebExport(moduleName = "WASmaxPsaChatBlockSetRPC",
            exports = "sendChatBlockSetRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxPsaChatBlockSetResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant — the relay accepted the
     * write and echoed back the resulting blocking status.
     *
     * @implNote {@code WASmaxInPsaChatBlockSetResponseSuccess.parseChatBlockSetResponseSuccess}
     *           validates the IQ-result envelope, asserts the
     *           {@code <blocking>} child exists, then projects its
     *           {@code status} attribute through
     *           {@code ENUM_BLOCKED_UNBLOCKED}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPsaChatBlockSetResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInPsaIQResultResponseMixin")
    final class Success implements SmaxPsaChatBlockSetResponse {
        /**
         * The blocking status echoed by the relay after applying the
         * action.
         */
        private final SmaxPsaChatBlockGetBlockingStatus blockingStatus;

        /**
         * Constructs a new successful reply.
         *
         * @param blockingStatus the blocking status; never {@code null}
         * @throws NullPointerException if {@code blockingStatus} is
         *                              {@code null}
         */
        public Success(SmaxPsaChatBlockGetBlockingStatus blockingStatus) {
            this.blockingStatus = Objects.requireNonNull(blockingStatus, "blockingStatus cannot be null");
        }

        /**
         * Returns the blocking status echoed by the relay.
         *
         * @return the blocking status; never {@code null}
         */
        public SmaxPsaChatBlockGetBlockingStatus blockingStatus() {
            return blockingStatus;
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
        @WhatsAppWebExport(moduleName = "WASmaxInPsaChatBlockSetResponseSuccess",
                exports = "parseChatBlockSetResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var blocking = node.getChild("blocking").orElse(null);
            if (blocking == null) {
                return Optional.empty();
            }
            var statusAttr = blocking.getAttributeAsString("status").orElse(null);
            if (statusAttr == null) {
                return Optional.empty();
            }
            var status = SmaxPsaChatBlockGetBlockingStatus.ofWire(statusAttr).orElse(null);
            if (status == null) {
                return Optional.empty();
            }
            return Optional.of(new Success(status));
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
            return this.blockingStatus == that.blockingStatus;
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockingStatus);
        }

        @Override
        public String toString() {
            return "SmaxPsaChatBlockSetResponse.Success[blockingStatus=" + blockingStatus + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered an
     * internal failure while processing the request.
     *
     * <p>The PSA domain projects four documented {@code 5xx}/{@code 4xx}
     * variants ({@code internal-server-error/500},
     * {@code request-timeout/408}, {@code service-unavailable/503},
     * {@code rate-overlimit/429}); Cobalt collapses them into the
     * single {@code (errorCode, errorText)} pair.
     *
     * @implNote {@code WASmaxInPsaChatBlockSetResponseServerError.parseChatBlockSetResponseServerError}
     *           composes the error-envelope check with
     *           {@code WASmaxInPsaChatBlockError.parseChatBlockError}.
     *           Cobalt routes through the shared
     *           {@link SmaxBaseServerErrorMixin#parseServerError(Node, Node)}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPsaChatBlockSetResponseServerError")
    @WhatsAppWebModule(moduleName = "WASmaxInPsaChatBlockError")
    final class ServerError implements SmaxPsaChatBlockSetResponse {
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
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
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
         *         empty when the stanza does not match the server-error
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPsaChatBlockSetResponseServerError",
                exports = "parseChatBlockSetResponseServerError",
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
            return "SmaxPsaChatBlockSetResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
