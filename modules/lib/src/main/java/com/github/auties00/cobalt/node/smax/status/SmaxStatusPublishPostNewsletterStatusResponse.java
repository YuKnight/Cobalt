package com.github.auties00.cobalt.node.smax.status;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxStatusPublishPostNewsletterStatusRequest}.
 */
public sealed interface SmaxStatusPublishPostNewsletterStatusResponse extends SmaxOperation.Response
        permits SmaxStatusPublishPostNewsletterStatusResponse.Success, SmaxStatusPublishPostNewsletterStatusResponse.Negative {

    /**
     * Tries each {@link SmaxStatusPublishPostNewsletterStatusResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound ack stanza received from the
     *                relay; never {@code null}
     * @param request the original outbound stanza. Used to
     *                validate echoed identifiers; never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxStatusPublishPostNewsletterStatusRPC",
            exports = "sendPostNewsletterStatusRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxStatusPublishPostNewsletterStatusResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var negative = Negative.of(node, request);
        if (negative.isPresent()) {
            return negative;
        }
        return Success.of(node, request);
    }

    /**
     * Validates the {@code <ack class="status"/>} envelope of a
     * publish reply by cross-checking
     * {@code from}/{@code id}/{@code class="status"}/{@code t}
     * against the request.
     *
     * @param reply   the inbound ack stanza
     * @param request the original outbound status
     * @return {@code true} when the envelope echo-checks pass
     */
    @WhatsAppWebExport(moduleName = "WASmaxInStatusPublishStatusAckMixin",
            exports = "parseStatusAckMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean validateAckEnvelope(Node reply, Node request) {
        if (!reply.hasDescription("ack")) {
            return false;
        }
        if (!reply.hasAttribute("class", "status")) {
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
        var t = tOpt.getAsLong();
        return t >= 1577865600L && t <= 4102473600L;
    }

    /**
     * The {@code Success} reply variant. The relay accepted the
     * status publish.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInStatusPublishPostNewsletterStatusResponseSuccess")
    final class Success implements SmaxStatusPublishPostNewsletterStatusResponse {
        /**
         * The optional server-id assigned by the relay (set only on
         * brand-new client-id-only publishes).
         */
        private final Long serverId;

        /**
         * The unix-second timestamp of the ack.
         */
        private final long timestamp;

        /**
         * Constructs a new successful reply.
         *
         * @param serverId  the optional server-id; may be
         *                  {@code null}
         * @param timestamp the unix-second timestamp
         */
        public Success(Long serverId, long timestamp) {
            this.serverId = serverId;
            this.timestamp = timestamp;
        }

        /**
         * Returns the optional server-id.
         *
         * @return an {@link Optional} carrying the server-id, or
         *         empty when omitted
         */
        public Optional<Long> serverId() {
            return Optional.ofNullable(serverId);
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
        @WhatsAppWebExport(moduleName = "WASmaxInStatusPublishPostNewsletterStatusResponseSuccess",
                exports = "parsePostNewsletterStatusResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!validateAckEnvelope(node, request)) {
                return Optional.empty();
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
            var timestamp = node.getAttributeAsLong("t").orElse(-1);
            return Optional.of(new Success(serverId, timestamp));
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
                    && Objects.equals(this.serverId, that.serverId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serverId, timestamp);
        }

        @Override
        public String toString() {
            return "SmaxStatusPublishPostNewsletterStatusResponse.Success[serverId=" + serverId
                    + ", timestamp=" + timestamp + ']';
        }
    }

    /**
     * The {@code Negative} reply variant. The relay rejected the
     * status publish with an application-level error code, optionally
     * carrying a retry backoff.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInStatusPublishPostNewsletterStatusResponseNegative")
    @WhatsAppWebModule(moduleName = "WASmaxInStatusPublishStatusNegativeAckMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInStatusPublishStatusApplicationNegativeAckMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInStatusPublishMessageNackRetryAttributesMixin")
    final class Negative implements SmaxStatusPublishPostNewsletterStatusResponse {
        /**
         * The relay-supplied error code carried as the {@code error}
         * attribute on the ack.
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
        @WhatsAppWebExport(moduleName = "WASmaxInStatusPublishPostNewsletterStatusResponseNegative",
                exports = "parsePostNewsletterStatusResponseNegative",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Negative> of(Node node, Node request) {
            if (!validateAckEnvelope(node, request)) {
                return Optional.empty();
            }
            var errorCode = node.getAttributeAsString("error").orElse(null);
            if (errorCode == null) {
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
            var timestamp = node.getAttributeAsLong("t").orElse(-1);
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
            return "SmaxStatusPublishPostNewsletterStatusResponse.Negative[errorCode=" + errorCode
                    + ", applicationError=" + applicationError
                    + ", backoff=" + backoff
                    + ", timestamp=" + timestamp + ']';
        }
    }
}
