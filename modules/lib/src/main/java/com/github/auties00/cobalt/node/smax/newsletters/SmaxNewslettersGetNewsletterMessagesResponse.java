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
 * response to a {@link SmaxNewslettersGetNewsletterMessagesRequest}.
 */
public sealed interface SmaxNewslettersGetNewsletterMessagesResponse extends SmaxOperation.Response
        permits SmaxNewslettersGetNewsletterMessagesResponse.Success, SmaxNewslettersGetNewsletterMessagesResponse.ClientError, SmaxNewslettersGetNewsletterMessagesResponse.ServerError {

    /**
     * Tries each {@link SmaxNewslettersGetNewsletterMessagesResponse} variant in priority order and returns
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
    @WhatsAppWebExport(moduleName = "WASmaxNewslettersGetNewsletterMessagesRPC",
            exports = "sendGetNewsletterMessagesRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxNewslettersGetNewsletterMessagesResponse> of(Node node, Node request) {
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
     * requested message slice.
     *
     * <p>The {@code <messages>} envelope echoes the optional
     * {@code jid} and {@code t} attributes (when the relay knows the
     * newsletter's last activity timestamp). Each {@code <message>}
     * child carries a {@code server_id}, an optional original message
     * envelope, and a variable bundle of add-ons (reactions, polls,
     * forwards, views, etc.) which Cobalt exposes as the raw child
     * {@link Node} for further inspection.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterMessagesResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersNewsletterMessageResponsePayloadMixin")
    final class Success implements SmaxNewslettersGetNewsletterMessagesResponse {
        /**
         * The optional newsletter JID echoed by the relay on the
         * {@code <messages>} envelope.
         */
        private final Jid newsletterJid;

        /**
         * The optional unix-second timestamp echoed by the relay.
         */
        private final Long timestamp;

        /**
         * The list of message entries returned by the relay.
         */
        private final List<NewsletterMessage> messages;

        /**
         * Constructs a new successful reply.
         *
         * @param newsletterJid the optional echoed JID; may be
         *                      {@code null}
         * @param timestamp     the optional echoed timestamp; may be
         *                      {@code null}
         * @param messages      the message entries; never {@code null}
         */
        public Success(Jid newsletterJid, Long timestamp, List<NewsletterMessage> messages) {
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
         * Returns the message entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<NewsletterMessage> messages() {
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterMessagesResponseSuccess",
                exports = "parseGetNewsletterMessagesResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            if (!node.hasAttribute("from", Jid.userServer().toString())) {
                return Optional.empty();
            }
            var messagesNode = node.getChild("messages").orElse(null);
            if (messagesNode == null) {
                return Optional.empty();
            }
            var jid = messagesNode.getAttributeAsJid("jid").orElse(null);
            var tOpt = messagesNode.getAttributeAsLong("t");
            Long timestamp = null;
            if (tOpt.isPresent()) {
                var tv = tOpt.getAsLong();
                if (tv < 0) {
                    return Optional.empty();
                }
                timestamp = tv;
            }
            var entries = new ArrayList<NewsletterMessage>();
            for (var messageNode : messagesNode.getChildren("message")) {
                var entry = NewsletterMessage.of(messageNode).orElse(null);
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
            return "SmaxNewslettersGetNewsletterMessagesResponse.Success[newsletterJid="
                    + newsletterJid + ", timestamp=" + timestamp
                    + ", messages=" + messages + ']';
        }
    }

    /**
     * One newsletter message entry. Projects the canonical
     * {@code <message id? server_id t? is_sender?>} envelope into a
     * typed bundle and exposes the underlying {@link Node} so callers
     * can drill into the variable-shape add-on children (reactions,
     * polls, responses, forwards, views, paid-partnership content).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersNewsletterMessageHistoryWithAddOnsMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersNewsletterMessageHistoryMixin")
    final class NewsletterMessage {
        /**
         * The optional client-supplied stanza id of the original
         * message.
         */
        private final String stanzaId;

        /**
         * The server-assigned monotonic message id within the
         * newsletter.
         */
        private final long serverId;

        /**
         * The optional unix-second timestamp of the message.
         */
        private final Long timestamp;

        /**
         * Whether the message was authored by the connected client.
         */
        private final boolean fromSelf;

        /**
         * The raw underlying {@link Node}. Exposed so callers can
         * project the variable-shape add-on children.
         */
        private final Node raw;

        /**
         * Constructs a new newsletter message entry.
         *
         * @param stanzaId  the optional stanza id; may be {@code null}
         * @param serverId  the server-assigned id
         * @param timestamp the optional unix-second timestamp; may be
         *                  {@code null}
         * @param fromSelf  whether the message was authored by self
         * @param raw       the underlying {@link Node}; never
         *                  {@code null}
         */
        public NewsletterMessage(String stanzaId, long serverId, Long timestamp, boolean fromSelf, Node raw) {
            this.stanzaId = stanzaId;
            this.serverId = serverId;
            this.timestamp = timestamp;
            this.fromSelf = fromSelf;
            this.raw = Objects.requireNonNull(raw, "raw cannot be null");
        }

        /**
         * Returns the optional stanza id.
         *
         * @return an {@link Optional} carrying the stanza id, or empty
         *         when omitted
         */
        public Optional<String> stanzaId() {
            return Optional.ofNullable(stanzaId);
        }

        /**
         * Returns the server-assigned message id.
         *
         * @return the server id
         */
        public long serverId() {
            return serverId;
        }

        /**
         * Returns the optional unix-second timestamp.
         *
         * @return an {@link Optional} carrying the timestamp, or empty
         *         when omitted
         */
        public Optional<Long> timestamp() {
            return Optional.ofNullable(timestamp);
        }

        /**
         * Returns whether the message was authored by the connected
         * client.
         *
         * @return {@code true} when the {@code is_sender="true"}
         *         attribute was present
         */
        public boolean fromSelf() {
            return fromSelf;
        }

        /**
         * Returns the underlying {@link Node} for downstream
         * inspection of the add-on children.
         *
         * @return the raw node; never {@code null}
         */
        public Node raw() {
            return raw;
        }

        /**
         * Tries to parse a {@link NewsletterMessage} from a
         * {@code <message>} node.
         *
         * @param messageNode the source {@code <message>} node; never
         *                    {@code null}
         * @return an {@link Optional} carrying the parsed entry, or
         *         empty when the node does not match the schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersNewsletterMessageHistoryWithAddOnsMixin",
                exports = "parseNewsletterMessageHistoryWithAddOnsMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<NewsletterMessage> of(Node messageNode) {
            if (!messageNode.hasDescription("message")) {
                return Optional.empty();
            }
            var stanzaId = messageNode.getAttributeAsString("id").orElse(null);
            var serverIdOpt = messageNode.getAttributeAsLong("server_id");
            if (serverIdOpt.isEmpty()) {
                return Optional.empty();
            }
            var serverId = serverIdOpt.getAsLong();
            if (serverId < 99 || serverId > 2147476647L) {
                return Optional.empty();
            }
            Long timestamp = null;
            var tOpt = messageNode.getAttributeAsLong("t");
            if (tOpt.isPresent()) {
                var tv = tOpt.getAsLong();
                if (tv < 0) {
                    return Optional.empty();
                }
                timestamp = tv;
            }
            var fromSelf = messageNode.hasAttribute("is_sender", "true");
            return Optional.of(new NewsletterMessage(stanzaId, serverId, timestamp, fromSelf, messageNode));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (NewsletterMessage) obj;
            return this.serverId == that.serverId
                    && this.fromSelf == that.fromSelf
                    && Objects.equals(this.stanzaId, that.stanzaId)
                    && Objects.equals(this.timestamp, that.timestamp)
                    && Objects.equals(this.raw, that.raw);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stanzaId, serverId, timestamp, fromSelf, raw);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterMessagesResponse.NewsletterMessage[stanzaId="
                    + stanzaId + ", serverId=" + serverId
                    + ", timestamp=" + timestamp + ", fromSelf=" + fromSelf + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request as malformed, unauthorised, or referencing a
     * non-existent newsletter / invite.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterMessagesResponseClientError")
    final class ClientError implements SmaxNewslettersGetNewsletterMessagesResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterMessagesResponseClientError",
                exports = "parseGetNewsletterMessagesResponseClientError",
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
            return "SmaxNewslettersGetNewsletterMessagesResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure while processing the request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterMessagesResponseServerError")
    final class ServerError implements SmaxNewslettersGetNewsletterMessagesResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterMessagesResponseServerError",
                exports = "parseGetNewsletterMessagesResponseServerError",
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
            return "SmaxNewslettersGetNewsletterMessagesResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
