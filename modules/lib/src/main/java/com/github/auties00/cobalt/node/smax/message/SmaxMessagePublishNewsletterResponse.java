package com.github.auties00.cobalt.node.smax.message;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxMessagePublishNewsletterRequest}.
 */
public sealed interface SmaxMessagePublishNewsletterResponse extends SmaxOperation.Response
        permits SmaxMessagePublishNewsletterResponse.Success, SmaxMessagePublishNewsletterResponse.Negative {

    /**
     * Tries each {@link SmaxMessagePublishNewsletterResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound ack stanza received from the
     *                relay; never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxMessagePublishNewsletterRPC",
            exports = "sendNewsletterRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxMessagePublishNewsletterResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var negative = Negative.of(node, request);
        if (negative.isPresent()) {
            return negative;
        }
        return Success.of(node, request);
    }

    /**
     * Validates the {@code <ack/>} envelope of a publish reply by
     * cross-checking {@code from}/{@code id}/{@code class="message"}
     * against the request.
     *
     * @param reply   the inbound ack stanza
     * @param request the original outbound message
     * @return {@code true} when the envelope echo-checks pass
     */
    @WhatsAppWebExport(moduleName = "WASmaxInMessagePublishAckMixin",
            exports = "parseAckMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean validateAckEnvelope(Node reply, Node request) {
        if (!reply.hasDescription("ack")) {
            return false;
        }
        if (!reply.hasAttribute("class", "message")) {
            return false;
        }
        var requestId = request.getAttributeAsString("id").orElse(null);
        if (requestId == null) {
            return false;
        }
        if (!reply.hasAttribute("id", requestId)) {
            return false;
        }
        var requestTo = request.getAttributeAsString("to").orElse(null);
        if (requestTo != null && !reply.hasAttribute("from", requestTo)) {
            return false;
        }
        var tOpt = reply.getAttributeAsLong("t");
        if (tOpt.isEmpty()) {
            return false;
        }
        return tOpt.getAsLong() >= 0;
    }

    /**
     * The {@code Success} reply variant. The relay accepted the
     * publish.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInMessagePublishNewsletterResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInMessagePublishNewsletterQuestionResponseOrNewsletterMessageAckMixinGroup")
    final class Success implements SmaxMessagePublishNewsletterResponse {
        /**
         * The success-disjunction variant name. Either
         * {@code "QuestionResponseAck"} or {@code "MessageAck"}.
         */
        private final String variantName;

        /**
         * The optional server-id assigned by the relay (only on
         * {@code MessageAck} when the publish carried a
         * brand-new-message client-id).
         */
        private final Long serverId;

        /**
         * The optional response-server-id assigned by the relay
         * (only on {@code QuestionResponseAck}).
         */
        private final String responseServerId;

        /**
         * The unix-second timestamp of the ack.
         */
        private final long timestamp;

        /**
         * Constructs a new successful reply.
         *
         * @param variantName      the disjunction variant name;
         *                         never {@code null}
         * @param serverId         the optional server-id; may be
         *                         {@code null}
         * @param responseServerId the optional response server-id;
         *                         may be {@code null}
         * @param timestamp        the unix-second timestamp
         * @throws NullPointerException if {@code variantName} is
         *                              {@code null}
         */
        public Success(String variantName, Long serverId, String responseServerId, long timestamp) {
            this.variantName = Objects.requireNonNull(variantName, "variantName cannot be null");
            this.serverId = serverId;
            this.responseServerId = responseServerId;
            this.timestamp = timestamp;
        }

        /**
         * Returns the disjunction variant name.
         *
         * @return the name; never {@code null}
         */
        public String variantName() {
            return variantName;
        }

        /**
         * Returns the optional server-id (set on {@code MessageAck}
         * variants for brand-new-message publishes).
         *
         * @return an {@link Optional} carrying the server-id, or
         *         empty when omitted
         */
        public Optional<Long> serverId() {
            return Optional.ofNullable(serverId);
        }

        /**
         * Returns the optional response-server-id (set on
         * {@code QuestionResponseAck} variants).
         *
         * @return an {@link Optional} carrying the
         *         response-server-id, or empty when omitted
         */
        public Optional<String> responseServerId() {
            return Optional.ofNullable(responseServerId);
        }

        /**
         * Returns the unix-second timestamp.
         *
         * @return the timestamp
         */
        public long timestamp() {
            return timestamp;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound ack stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInMessagePublishNewsletterResponseSuccess",
                exports = "parseNewsletterResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!validateAckEnvelope(node, request)) {
                return Optional.empty();
            }
            var timestamp = node.getAttributeAsLong("t").orElse(-1);
            if (timestamp < 0) {
                return Optional.empty();
            }
            var responseServerId = node.getAttributeAsString("response_server_id").orElse(null);
            if (responseServerId != null) {
                return Optional.of(new Success("QuestionResponseAck", null, responseServerId, timestamp));
            }
            Long serverId = null;
            var serverIdOpt = node.getAttributeAsLong("server_id");
            if (serverIdOpt.isPresent()) {
                var sv = serverIdOpt.getAsLong();
                if (sv < 99 || sv > 2147476647L) {
                    return Optional.empty();
                }
                serverId = sv;
            }
            return Optional.of(new Success("MessageAck", serverId, null, timestamp));
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
            return this.timestamp == that.timestamp
                    && Objects.equals(this.variantName, that.variantName)
                    && Objects.equals(this.serverId, that.serverId)
                    && Objects.equals(this.responseServerId, that.responseServerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variantName, serverId, responseServerId, timestamp);
        }

        @Override
        public String toString() {
            return "SmaxMessagePublishNewsletterResponse.Success[variantName=" + variantName
                    + ", serverId=" + serverId
                    + ", responseServerId=" + responseServerId
                    + ", timestamp=" + timestamp + ']';
        }
    }

    /**
     * The {@code Negative} reply variant. The relay rejected the
     * publish with an application-level error code, optionally
     * carrying a retry backoff.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInMessagePublishNewsletterResponseNegative")
    @WhatsAppWebModule(moduleName = "WASmaxInMessagePublishNegativeAckMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInMessagePublishApplicationNegativeAckMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInMessagePublishMessageNackRetryAttributesMixin")
    final class Negative implements SmaxMessagePublishNewsletterResponse {
        /**
         * The relay-supplied error code carried as the
         * {@code error} attribute on the ack.
         */
        private final String errorCode;

        /**
         * The optional application-level error integer.
         */
        private final Integer applicationError;

        /**
         * The optional retry backoff in seconds (0..86400).
         */
        private final Integer backoff;

        /**
         * The unix-second timestamp of the ack.
         */
        private final long timestamp;

        /**
         * Constructs a new negative reply.
         *
         * @param errorCode        the error code; never {@code null}
         * @param applicationError the optional application error;
         *                         may be {@code null}
         * @param backoff          the optional retry backoff in
         *                         seconds; may be {@code null}
         * @param timestamp        the unix-second timestamp
         * @throws NullPointerException if {@code errorCode} is
         *                              {@code null}
         */
        public Negative(String errorCode, Integer applicationError, Integer backoff, long timestamp) {
            this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
            this.applicationError = applicationError;
            this.backoff = backoff;
            this.timestamp = timestamp;
        }

        /**
         * Returns the error code.
         *
         * @return the code; never {@code null}
         */
        public String errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional application-level error integer.
         *
         * @return an {@link Optional} carrying the error, or empty
         *         when omitted
         */
        public Optional<Integer> applicationError() {
            return Optional.ofNullable(applicationError);
        }

        /**
         * Returns the optional retry backoff in seconds.
         *
         * @return an {@link Optional} carrying the backoff, or empty
         *         when omitted
         */
        public Optional<Integer> backoff() {
            return Optional.ofNullable(backoff);
        }

        /**
         * Returns the unix-second timestamp.
         *
         * @return the timestamp
         */
        public long timestamp() {
            return timestamp;
        }

        /**
         * Tries to parse a {@link Negative} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound ack stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         negative-ack schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInMessagePublishNewsletterResponseNegative",
                exports = "parseNewsletterResponseNegative",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Negative> of(Node node, Node request) {
            if (!validateAckEnvelope(node, request)) {
                return Optional.empty();
            }
            var errorCode = node.getAttributeAsString("error").orElse(null);
            if (errorCode == null) {
                return Optional.empty();
            }
            var timestamp = node.getAttributeAsLong("t").orElse(-1);
            if (timestamp < 0) {
                return Optional.empty();
            }
            Integer applicationError = null;
            var appErrOpt = node.getAttributeAsInt("application_error");
            if (appErrOpt.isPresent()) {
                var av = appErrOpt.getAsInt();
                if (av < 0) {
                    return Optional.empty();
                }
                applicationError = av;
            }
            Integer backoff = null;
            var backoffOpt = node.getAttributeAsInt("backoff");
            if (backoffOpt.isPresent()) {
                var bv = backoffOpt.getAsInt();
                if (bv < 0 || bv > 86400) {
                    return Optional.empty();
                }
                backoff = bv;
            }
            return Optional.of(new Negative(errorCode, applicationError, backoff, timestamp));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Negative) obj;
            return this.timestamp == that.timestamp
                    && Objects.equals(this.errorCode, that.errorCode)
                    && Objects.equals(this.applicationError, that.applicationError)
                    && Objects.equals(this.backoff, that.backoff);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, applicationError, backoff, timestamp);
        }

        @Override
        public String toString() {
            return "SmaxMessagePublishNewsletterResponse.Negative[errorCode=" + errorCode
                    + ", applicationError=" + applicationError
                    + ", backoff=" + backoff
                    + ", timestamp=" + timestamp + ']';
        }
    }
}
