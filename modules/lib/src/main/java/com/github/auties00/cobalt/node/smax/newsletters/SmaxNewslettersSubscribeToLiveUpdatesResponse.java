package com.github.auties00.cobalt.node.smax.newsletters;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxNewslettersSubscribeToLiveUpdatesRequest}.
 *
 * @implNote {@code WASmaxNewslettersSubscribeToLiveUpdatesRPC.sendSubscribeToLiveUpdatesRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError} in order and throws on no-match. Cobalt
 *           returns {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxNewslettersSubscribeToLiveUpdatesResponse extends SmaxOperation.Response
        permits SmaxNewslettersSubscribeToLiveUpdatesResponse.Success, SmaxNewslettersSubscribeToLiveUpdatesResponse.ClientError, SmaxNewslettersSubscribeToLiveUpdatesResponse.ServerError {

    /**
     * Tries each {@link SmaxNewslettersSubscribeToLiveUpdatesResponse} variant in priority order and returns
     * the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza, used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxNewslettersSubscribeToLiveUpdatesRPC",
            exports = "sendSubscribeToLiveUpdatesRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxNewslettersSubscribeToLiveUpdatesResponse> of(Node node, Node request) {
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
     * subscription and returned the chosen TTL.
     *
     * @implNote {@code WASmaxInNewslettersSubscribeToLiveUpdatesResponseSuccess.parseSubscribeToLiveUpdatesResponseSuccess}
     *           validates the {@code <iq>} envelope through
     *           {@code parseIQResultResponseMixin}. Asserts the
     *           {@code <live_updates>} child exists, then projects its
     *           {@code duration} attribute through
     *           {@code attrIntRange(value, 30, 600)}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersSubscribeToLiveUpdatesResponseSuccess")
    final class Success implements SmaxNewslettersSubscribeToLiveUpdatesResponse {
        /**
         * The relay-chosen TTL in seconds for this subscription.
         */
        private final int duration;

        /**
         * Constructs a new successful reply.
         *
         * @param duration the TTL in seconds; must lie in
         *                 {@code [30, 600]}
         */
        public Success(int duration) {
            this.duration = duration;
        }

        /**
         * Returns the relay-chosen subscription TTL in seconds.
         *
         * @return the TTL; bounded to {@code [30, 600]} by the relay
         */
        public int duration() {
            return duration;
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersSubscribeToLiveUpdatesResponseSuccess",
                exports = "parseSubscribeToLiveUpdatesResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var liveUpdates = node.getChild("live_updates").orElse(null);
            if (liveUpdates == null) {
                return Optional.empty();
            }
            var duration = liveUpdates.getAttributeAsInt("duration").orElse(-1);
            if (duration < 30 || duration > 600) {
                return Optional.empty();
            }
            return Optional.of(new Success(duration));
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
            return this.duration == that.duration;
        }

        @Override
        public int hashCode() {
            return Objects.hash(duration);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersSubscribeToLiveUpdatesResponse.Success[duration=" + duration + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * subscription as malformed, unauthorised, or referencing a
     * non-existent newsletter.
     *
     * @implNote {@code WASmaxInNewslettersSubscribeToLiveUpdatesResponseClientError.parseSubscribeToLiveUpdatesResponseClientError}
     *           parses the {@code <error code text/>} child and routes
     *           it through
     *           {@code WASmaxInNewslettersSubscribeToLiveUpdatesClientErrors}.
     *           Cobalt collapses to the raw {@code (code, text)} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersSubscribeToLiveUpdatesResponseClientError")
    final class ClientError implements SmaxNewslettersSubscribeToLiveUpdatesResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text. When the relay supplied one.
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
         *         empty when the stanza does not match the client-error
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersSubscribeToLiveUpdatesResponseClientError",
                exports = "parseSubscribeToLiveUpdatesResponseClientError",
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
            return "SmaxNewslettersSubscribeToLiveUpdatesResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure while processing the subscription.
     *
     * @implNote {@code WASmaxInNewslettersSubscribeToLiveUpdatesResponseServerError.parseSubscribeToLiveUpdatesResponseServerError}
     *           delegates to
     *           {@code WASmaxInNewslettersInternalServerErrorIQErrorResponseMixin}
     *           which Cobalt has consolidated under
     *           {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersSubscribeToLiveUpdatesResponseServerError")
    final class ServerError implements SmaxNewslettersSubscribeToLiveUpdatesResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text. When the relay supplied one.
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
         *         empty when the stanza does not match the server-error
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersSubscribeToLiveUpdatesResponseServerError",
                exports = "parseSubscribeToLiveUpdatesResponseServerError",
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
            return "SmaxNewslettersSubscribeToLiveUpdatesResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
