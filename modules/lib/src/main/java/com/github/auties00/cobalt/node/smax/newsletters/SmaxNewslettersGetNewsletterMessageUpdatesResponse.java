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
 * response to a {@link SmaxNewslettersGetNewsletterMessageUpdatesRequest}.
 *
 * @implNote {@code WASmaxNewslettersGetNewsletterMessageUpdatesRPC.sendGetNewsletterMessageUpdatesRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError} in order and throws on no-match.
 *           Cobalt returns {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxNewslettersGetNewsletterMessageUpdatesResponse extends SmaxOperation.Response
        permits SmaxNewslettersGetNewsletterMessageUpdatesResponse.Success, SmaxNewslettersGetNewsletterMessageUpdatesResponse.ClientError, SmaxNewslettersGetNewsletterMessageUpdatesResponse.ServerError {

    /**
     * Tries each {@link SmaxNewslettersGetNewsletterMessageUpdatesResponse} variant in priority order and returns
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
    @WhatsAppWebExport(moduleName = "WASmaxNewslettersGetNewsletterMessageUpdatesRPC",
            exports = "sendGetNewsletterMessageUpdatesRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxNewslettersGetNewsletterMessageUpdatesResponse> of(Node node, Node request) {
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
     * delta-of-message-updates batch.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterMessageUpdatesResponseSuccess.parseGetNewsletterMessageUpdatesResponseSuccess}
     *           validates the {@code <iq>} envelope through
     *           {@code parseIQResultResponseMixin}, asserts the
     *           {@code <message_updates>} → {@code <messages>} chain,
     *           then projects every {@code <message>} via
     *           {@code parseNewsletterMessageHistoryWithAddOnsMixin}.
     *           Cobalt reuses
     *           {@link SmaxNewslettersGetNewsletterMessagesResponse.NewsletterMessage}
     *           since both RPCs share the same per-message projection.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterMessageUpdatesResponseSuccess")
    final class Success implements SmaxNewslettersGetNewsletterMessageUpdatesResponse {
        /**
         * The optional newsletter JID echoed by the relay on the
         * {@code <messages>} block.
         */
        private final Jid newsletterJid;

        /**
         * The optional unix-second timestamp echoed by the relay.
         */
        private final Long timestamp;

        /**
         * The list of message-update entries returned by the relay.
         */
        private final List<SmaxNewslettersGetNewsletterMessagesResponse.NewsletterMessage> messages;

        /**
         * Constructs a new successful reply.
         *
         * @param newsletterJid the optional echoed JID; may be
         *                      {@code null}
         * @param timestamp     the optional echoed timestamp; may be
         *                      {@code null}
         * @param messages      the message-update entries; never
         *                      {@code null}
         */
        public Success(Jid newsletterJid,
                       Long timestamp,
                       List<SmaxNewslettersGetNewsletterMessagesResponse.NewsletterMessage> messages) {
            this.newsletterJid = newsletterJid;
            this.timestamp = timestamp;
            this.messages = List.copyOf(Objects.requireNonNullElse(messages, List.of()));
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
         * Returns the message-update entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<SmaxNewslettersGetNewsletterMessagesResponse.NewsletterMessage> messages() {
            return messages;
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterMessageUpdatesResponseSuccess",
                exports = "parseGetNewsletterMessageUpdatesResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var messageUpdates = node.getChild("message_updates").orElse(null);
            if (messageUpdates == null) {
                return Optional.empty();
            }
            var messagesNode = messageUpdates.getChild("messages").orElse(null);
            if (messagesNode == null) {
                return Optional.empty();
            }
            var jid = messagesNode.getAttributeAsJid("jid").orElse(null);
            Long timestamp = null;
            var tOpt = messagesNode.getAttributeAsLong("t");
            if (tOpt.isPresent()) {
                var tv = tOpt.getAsLong();
                if (tv < 0) {
                    return Optional.empty();
                }
                timestamp = tv;
            }
            var entries = new ArrayList<SmaxNewslettersGetNewsletterMessagesResponse.NewsletterMessage>();
            for (var messageNode : messagesNode.getChildren("message")) {
                var entry = SmaxNewslettersGetNewsletterMessagesResponse.NewsletterMessage.of(messageNode)
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
                    && Objects.equals(this.messages, that.messages);
        }

        @Override
        public int hashCode() {
            return Objects.hash(newsletterJid, timestamp, messages);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterMessageUpdatesResponse.Success[newsletterJid="
                    + newsletterJid + ", timestamp=" + timestamp
                    + ", messages=" + messages + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, or referencing a
     * non-existent newsletter.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterMessageUpdatesResponseClientError.parseGetNewsletterMessageUpdatesResponseClientError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterMessageUpdatesResponseClientError")
    final class ClientError implements SmaxNewslettersGetNewsletterMessageUpdatesResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterMessageUpdatesResponseClientError",
                exports = "parseGetNewsletterMessageUpdatesResponseClientError",
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
            return "SmaxNewslettersGetNewsletterMessageUpdatesResponse.ClientError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the request.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterMessageUpdatesResponseServerError.parseGetNewsletterMessageUpdatesResponseServerError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterMessageUpdatesResponseServerError")
    final class ServerError implements SmaxNewslettersGetNewsletterMessageUpdatesResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterMessageUpdatesResponseServerError",
                exports = "parseGetNewsletterMessageUpdatesResponseServerError",
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
            return "SmaxNewslettersGetNewsletterMessageUpdatesResponse.ServerError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }
}
