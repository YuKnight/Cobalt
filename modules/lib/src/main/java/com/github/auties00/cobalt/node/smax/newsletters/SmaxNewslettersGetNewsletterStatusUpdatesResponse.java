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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxNewslettersGetNewsletterStatusUpdatesRequest}.
 *
 * @implNote {@code WASmaxNewslettersGetNewsletterStatusUpdatesRPC.sendGetNewsletterStatusUpdatesRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError} in order and throws on no-match.
 *           Cobalt returns {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxNewslettersGetNewsletterStatusUpdatesResponse extends SmaxOperation.Response
        permits SmaxNewslettersGetNewsletterStatusUpdatesResponse.Success, SmaxNewslettersGetNewsletterStatusUpdatesResponse.ClientError, SmaxNewslettersGetNewsletterStatusUpdatesResponse.ServerError {

    /**
     * Tries each {@link SmaxNewslettersGetNewsletterStatusUpdatesResponse} variant in priority order and returns
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
    @WhatsAppWebExport(moduleName = "WASmaxNewslettersGetNewsletterStatusUpdatesRPC",
            exports = "sendGetNewsletterStatusUpdatesRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxNewslettersGetNewsletterStatusUpdatesResponse> of(Node node, Node request) {
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
     * delta-of-status-updates batch.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterStatusUpdatesResponseSuccess.parseGetNewsletterStatusUpdatesResponseSuccess}
     *           validates the {@code <iq>} envelope through
     *           {@code parseIQResultResponseMixin}, asserts the
     *           {@code <status_updates>} → {@code <statuses>} chain,
     *           then projects every {@code <status>} via
     *           {@code parseStatusNewsletterHistoryWithAddOnsMixin}.
     *           Cobalt reuses
     *           {@link SmaxNewslettersGetNewsletterStatusesResponse.NewsletterStatus}
     *           since both RPCs share the same per-status projection.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterStatusUpdatesResponseSuccess")
    final class Success implements SmaxNewslettersGetNewsletterStatusUpdatesResponse {
        /**
         * The optional newsletter JID echoed by the relay on the
         * {@code <statuses>} block.
         */
        private final Jid newsletterJid;

        /**
         * The optional unix-second timestamp echoed by the relay.
         */
        private final Long timestamp;

        /**
         * The list of status-update entries returned by the relay.
         */
        private final List<SmaxNewslettersGetNewsletterStatusesResponse.NewsletterStatus> statuses;

        /**
         * Constructs a new successful reply.
         *
         * @param newsletterJid the optional echoed JID; may be
         *                      {@code null}
         * @param timestamp     the optional echoed timestamp; may be
         *                      {@code null}
         * @param statuses      the status-update entries; never
         *                      {@code null}
         */
        public Success(Jid newsletterJid,
                       Long timestamp,
                       List<SmaxNewslettersGetNewsletterStatusesResponse.NewsletterStatus> statuses) {
            this.newsletterJid = newsletterJid;
            this.timestamp = timestamp;
            this.statuses = List.copyOf(Objects.requireNonNullElse(statuses, List.of()));
        }

        /**
         * Returns the optional newsletter JID echoed by the relay.
         *
         * @return an {@link Optional} carrying the JID, or empty when
         *         omitted
         */
        public Optional<Jid> newsletterJid() {
            return Optional.ofNullable(newsletterJid);
        }

        /**
         * Returns the optional unix-second timestamp echoed by the
         * relay.
         *
         * @return an {@link Optional} carrying the timestamp, or empty
         *         when omitted
         */
        public Optional<Long> timestamp() {
            return Optional.ofNullable(timestamp);
        }

        /**
         * Returns the status-update entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<SmaxNewslettersGetNewsletterStatusesResponse.NewsletterStatus> statuses() {
            return statuses;
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterStatusUpdatesResponseSuccess",
                exports = "parseGetNewsletterStatusUpdatesResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var statusUpdates = node.getChild("status_updates").orElse(null);
            if (statusUpdates == null) {
                return Optional.empty();
            }
            var statusesNode = statusUpdates.getChild("statuses").orElse(null);
            if (statusesNode == null) {
                return Optional.empty();
            }
            var jid = statusesNode.getAttributeAsJid("jid").orElse(null);
            Long timestamp = null;
            var tOpt = statusesNode.getAttributeAsLong("t");
            if (tOpt.isPresent()) {
                var tv = tOpt.getAsLong();
                if (tv < 0) {
                    return Optional.empty();
                }
                timestamp = tv;
            }
            var entries = new ArrayList<SmaxNewslettersGetNewsletterStatusesResponse.NewsletterStatus>();
            for (var statusNode : statusesNode.getChildren("status")) {
                var entry = SmaxNewslettersGetNewsletterStatusesResponse.NewsletterStatus.of(statusNode)
                        .orElse(null);
                if (entry == null) {
                    return Optional.empty();
                }
                entries.add(entry);
            }
            return Optional.of(new Success(jid, timestamp, entries));
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
            return Objects.equals(this.newsletterJid, that.newsletterJid)
                    && Objects.equals(this.timestamp, that.timestamp)
                    && Objects.equals(this.statuses, that.statuses);
        }

        @Override
        public int hashCode() {
            return Objects.hash(newsletterJid, timestamp, statuses);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterStatusUpdatesResponse.Success[newsletterJid="
                    + newsletterJid + ", timestamp=" + timestamp
                    + ", statuses=" + statuses + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, or referencing a
     * non-existent newsletter.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterStatusUpdatesResponseClientError.parseGetNewsletterStatusUpdatesResponseClientError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterStatusUpdatesResponseClientError")
    final class ClientError implements SmaxNewslettersGetNewsletterStatusUpdatesResponse {
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
         * @return an {@link Optional} carrying the text, or empty when
         *         omitted
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterStatusUpdatesResponseClientError",
                exports = "parseGetNewsletterStatusUpdatesResponseClientError",
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
            return "SmaxNewslettersGetNewsletterStatusUpdatesResponse.ClientError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the request.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterStatusUpdatesResponseServerError.parseGetNewsletterStatusUpdatesResponseServerError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterStatusUpdatesResponseServerError")
    final class ServerError implements SmaxNewslettersGetNewsletterStatusUpdatesResponse {
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
         * @return an {@link Optional} carrying the text, or empty when
         *         omitted
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterStatusUpdatesResponseServerError",
                exports = "parseGetNewsletterStatusUpdatesResponseServerError",
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
            return "SmaxNewslettersGetNewsletterStatusUpdatesResponse.ServerError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }
}
