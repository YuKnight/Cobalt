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
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay.
 *
 * @implNote {@code WASmaxWaffleStateExistsRPC.sendStateExistsRPC}
 *           tries {@code Success} → {@code Error}; Cobalt routes the
 *           single error variant through the standard
 *           {@code ClientError}/{@code ServerError} code-range split.
 */
public sealed interface SmaxWaffleStateExistsResponse extends SmaxOperation.Response
        permits SmaxWaffleStateExistsResponse.Success, SmaxWaffleStateExistsResponse.ClientError, SmaxWaffleStateExistsResponse.ServerError {

    /**
     * Tries each {@link SmaxWaffleStateExistsResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxWaffleStateExistsRPC",
            exports = "sendStateExistsRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxWaffleStateExistsResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — the relay reported the
     * client's Waffle state machine code.
     *
     * @implNote {@code WASmaxInWaffleStateExistsResponseSuccess.parseStateExistsResponseSuccess}
     *           projects the {@code <wf_state>INT</wf_state>}
     *           content plus an optional
     *           {@code <suspended_state npr?/>} child.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleStateExistsResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleIQResultResponseMixin")
    final class Success implements SmaxWaffleStateExistsResponse {
        /**
         * The Waffle state machine's current code.
         */
        private final int wfState;

        /**
         * The "no-personal-recovery" flag, when the account is
         * suspended; absent when the relay omits the
         * {@code <suspended_state/>} child.
         */
        private final Boolean suspendedNpr;

        /**
         * {@code true} when the relay surfaced an explicit
         * {@code <suspended_state/>} child, indicating the account is
         * suspended.
         */
        private final boolean suspended;

        /**
         * Constructs a new success projection.
         *
         * @param wfState      the Waffle state machine code
         * @param suspended    {@code true} when the relay surfaced a
         *                     {@code <suspended_state/>} child
         * @param suspendedNpr the {@code npr} flag, or {@code null}
         *                     when not surfaced
         */
        public Success(int wfState, boolean suspended, Boolean suspendedNpr) {
            this.wfState = wfState;
            this.suspended = suspended;
            this.suspendedNpr = suspendedNpr;
        }

        /**
         * Returns the Waffle state machine code.
         *
         * @return the state code
         */
        public int wfState() {
            return wfState;
        }

        /**
         * Reports whether the relay surfaced an explicit
         * {@code <suspended_state/>} child.
         *
         * @return {@code true} when the account is suspended
         */
        public boolean suspended() {
            return suspended;
        }

        /**
         * Returns the {@code npr} ("no-personal-recovery") flag when
         * the relay surfaced one.
         *
         * @return an {@link Optional} carrying the flag, or empty
         */
        public Optional<Boolean> suspendedNpr() {
            return Optional.ofNullable(suspendedNpr);
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
        @WhatsAppWebExport(moduleName = "WASmaxInWaffleStateExistsResponseSuccess",
                exports = "parseStateExistsResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var wfStateNode = node.getChild("wf_state").orElse(null);
            if (wfStateNode == null) {
                return Optional.empty();
            }
            var wfState = wfStateNode.toContentString()
                    .map(String::trim)
                    .map(s -> {
                        try {
                            return Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .orElse(null);
            if (wfState == null) {
                return Optional.empty();
            }
            var suspendedNode = node.getChild("suspended_state").orElse(null);
            Boolean npr = null;
            if (suspendedNode != null) {
                var nprValue = suspendedNode.getAttributeAsString("npr").orElse(null);
                if (nprValue != null) {
                    npr = "true".equalsIgnoreCase(nprValue);
                }
            }
            return Optional.of(new Success(wfState, suspendedNode != null, npr));
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
            return this.wfState == that.wfState
                    && this.suspended == that.suspended
                    && Objects.equals(this.suspendedNpr, that.suspendedNpr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(wfState, suspended, suspendedNpr);
        }

        @Override
        public String toString() {
            return "SmaxWaffleStateExistsResponse.Success[wfState=" + wfState
                    + ", suspended=" + suspended
                    + ", suspendedNpr=" + suspendedNpr + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed or unauthorised.
     *
     * @implNote {@code WASmaxInWaffleStateExistsResponseError.parseStateExistsResponseError}
     *           routes the {@code <error/>} child through
     *           {@code WASmaxInWaffleStateExistsErrors}; Cobalt
     *           collapses to the raw {@code (code, text)} pair via
     *           the {@code 4xx} code-range filter.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleStateExistsResponseError")
    final class ClientError implements SmaxWaffleStateExistsResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInWaffleStateExistsResponseError",
                exports = "parseStateExistsResponseError",
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
            return "SmaxWaffleStateExistsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure.
     *
     * @implNote Same wire-shape as {@link ClientError}; Cobalt routes
     *           through {@link SmaxBaseServerErrorMixin} with the
     *           {@code 5xx} code-range filter.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInWaffleStateExistsResponseError")
    final class ServerError implements SmaxWaffleStateExistsResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInWaffleStateExistsResponseError",
                exports = "parseStateExistsResponseError",
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
            return "SmaxWaffleStateExistsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
