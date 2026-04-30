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
 * response to a {@link SmaxNewslettersGetNewsletterStatusesRequest}.
 *
 * @implNote {@code WASmaxNewslettersGetNewsletterStatusesRPC.sendGetNewsletterStatusesRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError} in order and throws on no-match. Cobalt
 *           returns {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxNewslettersGetNewsletterStatusesResponse extends SmaxOperation.Response
        permits SmaxNewslettersGetNewsletterStatusesResponse.Success, SmaxNewslettersGetNewsletterStatusesResponse.ClientError, SmaxNewslettersGetNewsletterStatusesResponse.ServerError {

    /**
     * Tries each {@link SmaxNewslettersGetNewsletterStatusesResponse} variant in priority order and returns
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
    @WhatsAppWebExport(moduleName = "WASmaxNewslettersGetNewsletterStatusesRPC",
            exports = "sendGetNewsletterStatusesRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxNewslettersGetNewsletterStatusesResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay returned the
     * requested status slice.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterStatusesResponseSuccess.parseGetNewsletterStatusesResponseSuccess}
     *           validates the {@code <iq from id type="result">}
     *           envelope (echo-checking against {@code s.whatsapp.net}),
     *           asserts the {@code <statuses>} child exists, then
     *           projects every {@code <status>} via
     *           {@code parseStatusNewsletterHistoryWithAddOnsMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterStatusesResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersNewsletterStatusResponsePayloadMixin")
    final class Success implements SmaxNewslettersGetNewsletterStatusesResponse {
        /**
         * The optional newsletter JID echoed by the relay.
         */
        private final Jid newsletterJid;

        /**
         * The optional unix-second timestamp echoed by the relay.
         */
        private final Long timestamp;

        /**
         * The list of status entries returned by the relay.
         */
        private final List<NewsletterStatus> statuses;

        /**
         * Constructs a new successful reply.
         *
         * @param newsletterJid the optional echoed JID; may be
         *                      {@code null}
         * @param timestamp     the optional echoed timestamp; may be
         *                      {@code null}
         * @param statuses      the status entries; never {@code null}
         */
        public Success(Jid newsletterJid, Long timestamp, List<NewsletterStatus> statuses) {
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
         * Returns the status entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<NewsletterStatus> statuses() {
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterStatusesResponseSuccess",
                exports = "parseGetNewsletterStatusesResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            if (!node.hasAttribute("from", Jid.userServer().toString())) {
                return Optional.empty();
            }
            var statusesNode = node.getChild("statuses").orElse(null);
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
            var entries = new ArrayList<NewsletterStatus>();
            for (var statusNode : statusesNode.getChildren("status")) {
                var entry = NewsletterStatus.of(statusNode).orElse(null);
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
            return "SmaxNewslettersGetNewsletterStatusesResponse.Success[newsletterJid="
                    + newsletterJid + ", timestamp=" + timestamp
                    + ", statuses=" + statuses + ']';
        }
    }

    /**
     * One newsletter status entry. Projects the canonical
     * {@code <status>} envelope into a typed bundle and exposes the
     * underlying {@link Node} for downstream add-on parsing.
     *
     * @implNote {@code WASmaxInNewslettersStatusNewsletterHistoryWithAddOnsMixin.parseStatusNewsletterHistoryWithAddOnsMixin}
     *           composes
     *           {@code parseStatusNewsletterHistoryMixin} (the
     *           top-level scalars and original status content) with
     *           {@code parseStatusNewsletterReactionsMixin} and
     *           {@code parseStatusNewsletterViewsCountsMixin}. Cobalt
     *           projects the top-level scalars and exposes the raw
     *           {@link Node} for the add-on subtree rather than
     *           recreating every nested mixin as a typed final class.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersStatusNewsletterHistoryWithAddOnsMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersStatusNewsletterHistoryMixin")
    final class NewsletterStatus {
        /**
         * The raw underlying {@link Node}. Exposed so callers can
         * project the variable-shape add-on children (reactions,
         * views).
         */
        private final Node raw;

        /**
         * Constructs a new newsletter status entry.
         *
         * @param raw the underlying {@link Node}; never {@code null}
         */
        public NewsletterStatus(Node raw) {
            this.raw = Objects.requireNonNull(raw, "raw cannot be null");
        }

        /**
         * Returns the underlying {@link Node}.
         *
         * @return the raw node; never {@code null}
         */
        public Node raw() {
            return raw;
        }

        /**
         * Tries to parse a {@link NewsletterStatus} from a
         * {@code <status>} node.
         *
         * @param statusNode the source {@code <status>} node; never
         *                   {@code null}
         * @return an {@link Optional} carrying the parsed entry, or
         *         empty when the node does not match the schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersStatusNewsletterHistoryWithAddOnsMixin",
                exports = "parseStatusNewsletterHistoryWithAddOnsMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<NewsletterStatus> of(Node statusNode) {
            if (!statusNode.hasDescription("status")) {
                return Optional.empty();
            }
            return Optional.of(new NewsletterStatus(statusNode));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (NewsletterStatus) obj;
            return Objects.equals(this.raw, that.raw);
        }

        @Override
        public int hashCode() {
            return Objects.hash(raw);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterStatusesResponse.NewsletterStatus[raw=" + raw + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request as malformed, unauthorised, or referencing a
     * non-existent newsletter.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterStatusesResponseClientError.parseGetNewsletterStatusesResponseClientError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterStatusesResponseClientError")
    final class ClientError implements SmaxNewslettersGetNewsletterStatusesResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterStatusesResponseClientError",
                exports = "parseGetNewsletterStatusesResponseClientError",
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
            return "SmaxNewslettersGetNewsletterStatusesResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure while processing the request.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterStatusesResponseServerError.parseGetNewsletterStatusesResponseServerError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterStatusesResponseServerError")
    final class ServerError implements SmaxNewslettersGetNewsletterStatusesResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterStatusesResponseServerError",
                exports = "parseGetNewsletterStatusesResponseServerError",
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
            return "SmaxNewslettersGetNewsletterStatusesResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
