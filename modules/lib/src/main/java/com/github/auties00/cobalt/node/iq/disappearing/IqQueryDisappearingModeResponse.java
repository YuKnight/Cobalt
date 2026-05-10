package com.github.auties00.cobalt.node.iq.disappearing;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to an {@link IqQueryDisappearingModeRequest}.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryDisappearingModeJob")
public sealed interface IqQueryDisappearingModeResponse extends IqOperation.Response
        permits IqQueryDisappearingModeResponse.Success, IqQueryDisappearingModeResponse.ClientError, IqQueryDisappearingModeResponse.ServerError {

    /**
     * Tries each {@link IqQueryDisappearingModeResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no documented variant matched
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryDisappearingModeJob",
            exports = "queryDisappearingMode",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqQueryDisappearingModeResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — carries the current
     * default disappearing-mode duration and the wall-clock at
     * which it was last applied.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryDisappearingModeJob")
    final class Success implements IqQueryDisappearingModeResponse {
        /**
         * The default disappearing-mode duration applied to newly-
         * created chats. {@link Duration#ZERO} when the feature is
         * off.
         */
        private final Duration duration;

        /**
         * The wall-clock (seconds since epoch) at which the
         * duration was last applied.
         */
        private final long appliedAtSeconds;

        /**
         * Constructs a successful reply.
         *
         * @param duration         the default duration; never
         *                         {@code null}
         * @param appliedAtSeconds the wall-clock the duration was
         *                         last applied
         * @throws NullPointerException if {@code duration} is
         *                              {@code null}
         */
        public Success(Duration duration, long appliedAtSeconds) {
            this.duration = Objects.requireNonNull(duration, "duration cannot be null");
            this.appliedAtSeconds = appliedAtSeconds;
        }

        /**
         * Returns the default disappearing-mode duration.
         *
         * @return the duration; never {@code null}
         */
        public Duration duration() {
            return duration;
        }

        /**
         * Returns the wall-clock at which the duration was last
         * applied.
         *
         * @return seconds since epoch
         */
        public long appliedAtSeconds() {
            return appliedAtSeconds;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryDisappearingModeJob",
                exports = "dmParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var dmNode = node.getChild("disappearing_mode").orElse(null);
            if (dmNode == null) {
                return Optional.empty();
            }
            var durationAttr = dmNode.getAttributeAsLong("duration");
            if (durationAttr.isEmpty()) {
                return Optional.empty();
            }
            var tAttr = dmNode.getAttributeAsLong("t");
            if (tAttr.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Success(Duration.ofSeconds(durationAttr.getAsLong()),
                    tAttr.getAsLong()));
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
            return this.appliedAtSeconds == that.appliedAtSeconds
                    && Objects.equals(this.duration, that.duration);
        }

        @Override
        public int hashCode() {
            return Objects.hash(duration, appliedAtSeconds);
        }

        @Override
        public String toString() {
            return "IqQueryDisappearingModeResponse.Success[duration=" + duration
                    + ", appliedAtSeconds=" + appliedAtSeconds + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — {@code 4xx} rejection.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryDisappearingModeJob")
    final class ClientError implements IqQueryDisappearingModeResponse {
        /**
         * The numeric server-side error code.
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
         * @param errorText the optional text; may be {@code null}
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the text, or empty
         *         when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryDisappearingModeJob",
                exports = "queryDisappearingMode",
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqQueryDisappearingModeResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — {@code 5xx} transient
     * failure.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryDisappearingModeJob")
    final class ServerError implements IqQueryDisappearingModeResponse {
        /**
         * The numeric server-side error code.
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
         * @param errorText the optional text; may be {@code null}
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the text, or empty
         *         when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryDisappearingModeJob",
                exports = "queryDisappearingMode",
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqQueryDisappearingModeResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
