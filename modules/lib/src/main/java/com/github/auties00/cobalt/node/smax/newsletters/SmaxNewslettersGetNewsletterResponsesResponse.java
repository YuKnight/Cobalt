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
 * response to a {@link SmaxNewslettersGetNewsletterResponsesRequest}.
 *
 * @implNote {@code WASmaxNewslettersGetNewsletterResponsesRPC.sendGetNewsletterResponsesRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError} in order and throws on no-match. Cobalt
 *           returns {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxNewslettersGetNewsletterResponsesResponse extends SmaxOperation.Response
        permits SmaxNewslettersGetNewsletterResponsesResponse.Success, SmaxNewslettersGetNewsletterResponsesResponse.ClientError, SmaxNewslettersGetNewsletterResponsesResponse.ServerError {

    /**
     * Tries each {@link SmaxNewslettersGetNewsletterResponsesResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxNewslettersGetNewsletterResponsesRPC",
            exports = "sendGetNewsletterResponsesRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxNewslettersGetNewsletterResponsesResponse> of(Node node, Node request) {
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
     * requested response slice.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterResponsesResponseSuccess.parseGetNewsletterResponsesResponseSuccess}
     *           validates the {@code <iq from id type="result">}
     *           envelope (echo-checking against the newsletter JID),
     *           asserts the {@code <question_responses>} child
     *           exists, validates the echoed
     *           {@code server_id ∈ [99, 2147476647]}, then projects
     *           every {@code <question_response>} via
     *           {@code parseGetNewsletterResponsesResponseSuccessQuestionResponsesQuestionResponse}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterResponsesResponseSuccess")
    final class Success implements SmaxNewslettersGetNewsletterResponsesResponse {
        /**
         * The newsletter JID echoed by the relay on the {@code <iq from>}.
         */
        private final Jid from;

        /**
         * The echoed server-id of the question whose responses are
         * carried in this slice.
         */
        private final long questionResponsesServerId;

        /**
         * The list of question-response entries returned by the
         * relay.
         */
        private final List<QuestionResponse> questionResponses;

        /**
         * Constructs a new successful reply.
         *
         * @param from                      the echoed newsletter JID;
         *                                  never {@code null}
         * @param questionResponsesServerId the echoed question
         *                                  server-id
         * @param questionResponses         the response entries; never
         *                                  {@code null}
         * @throws NullPointerException if {@code from} or
         *                              {@code questionResponses} is
         *                              {@code null}
         */
        public Success(Jid from, long questionResponsesServerId,
                       List<QuestionResponse> questionResponses) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
            this.questionResponsesServerId = questionResponsesServerId;
            this.questionResponses = List.copyOf(Objects.requireNonNull(questionResponses,
                    "questionResponses cannot be null"));
        }

        /**
         * Returns the echoed newsletter JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid from() {
            return from;
        }

        /**
         * Returns the echoed question server-id.
         *
         * @return the server-id
         */
        public long questionResponsesServerId() {
            return questionResponsesServerId;
        }

        /**
         * Returns the response entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<QuestionResponse> questionResponses() {
            return questionResponses;
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterResponsesResponseSuccess",
                exports = "parseGetNewsletterResponsesResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null) {
                return Optional.empty();
            }
            var questionResponsesNode = node.getChild("question_responses").orElse(null);
            if (questionResponsesNode == null) {
                return Optional.empty();
            }
            var serverIdOpt = questionResponsesNode.getAttributeAsLong("server_id");
            if (serverIdOpt.isEmpty()) {
                return Optional.empty();
            }
            var serverId = serverIdOpt.getAsLong();
            if (serverId < 99 || serverId > 2147476647L) {
                return Optional.empty();
            }
            var entries = new ArrayList<QuestionResponse>();
            for (var qr : questionResponsesNode.getChildren("question_response")) {
                var entry = QuestionResponse.of(qr).orElse(null);
                if (entry == null) {
                    return Optional.empty();
                }
                entries.add(entry);
            }
            return Optional.of(new Success(from, serverId, entries));
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
            return this.questionResponsesServerId == that.questionResponsesServerId
                    && Objects.equals(this.from, that.from)
                    && Objects.equals(this.questionResponses, that.questionResponses);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, questionResponsesServerId, questionResponses);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterResponsesResponse.Success[from=" + from
                    + ", questionResponsesServerId=" + questionResponsesServerId
                    + ", questionResponses=" + questionResponses + ']';
        }
    }

    /**
     * One question-response entry. The per-subscriber free-form
     * reply against a newsletter question post.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterResponsesResponseSuccess.parseGetNewsletterResponsesResponseSuccessQuestionResponsesQuestionResponse}
     *           composes
     *           {@code WASmaxInNewslettersQuestionResponseMessageMixin.parseQuestionResponseMessageMixin}
     *           ({@code <message id t is_sender? plaintext>} +
     *           content-type / response-server-id),
     *           {@code WASmaxInNewslettersQuestionResponseSenderMixin.parseQuestionResponseSenderMixin}
     *           ({@code <sender lid? notify_name? picture direct_path>}),
     *           and the optional
     *           {@code WASmaxInNewslettersQuestionResponseFlagsMixin.parseQuestionResponseFlagsMixin}
     *           ({@code <flags><replied/></flags>}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersQuestionResponseMessageMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersQuestionResponseSenderMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersQuestionResponseFlagsMixin")
    final class QuestionResponse {
        /**
         * The message stanza id.
         */
        private final String messageId;

        /**
         * The message timestamp (unix seconds).
         */
        private final long messageTimestamp;

        /**
         * Whether the message was authored by the connected client.
         */
        private final boolean fromSelf;

        /**
         * The optional sender LID. Present for non-self entries when
         * the relay knows the responder's anonymised LID.
         */
        private final Jid senderLid;

        /**
         * The optional sender notify-name.
         */
        private final String senderNotifyName;

        /**
         * The sender's profile-picture direct-path (always present
         * per WA Web).
         */
        private final String senderPictureDirectPath;

        /**
         * Whether the question owner has explicitly replied to this
         * response, derived from the optional {@code <replied/>}
         * flag.
         */
        private final boolean hasRepliedFlag;

        /**
         * The underlying {@link Node}. Exposed so callers can drill
         * into the variable-shape content-type / payload children.
         */
        private final Node raw;

        /**
         * Constructs a new entry.
         *
         * @param messageId               the message stanza id; never
         *                                {@code null}
         * @param messageTimestamp        the message timestamp
         * @param fromSelf                whether authored by self
         * @param senderLid               the optional sender LID; may
         *                                be {@code null}
         * @param senderNotifyName        the optional notify-name; may
         *                                be {@code null}
         * @param senderPictureDirectPath the picture direct-path;
         *                                never {@code null}
         * @param hasRepliedFlag          whether the {@code <replied/>}
         *                                marker was present
         * @param raw                     the underlying node; never
         *                                {@code null}
         * @throws NullPointerException if any non-optional argument is
         *                              {@code null}
         */
        public QuestionResponse(String messageId, long messageTimestamp, boolean fromSelf,
                                Jid senderLid, String senderNotifyName,
                                String senderPictureDirectPath, boolean hasRepliedFlag, Node raw) {
            this.messageId = Objects.requireNonNull(messageId, "messageId cannot be null");
            this.messageTimestamp = messageTimestamp;
            this.fromSelf = fromSelf;
            this.senderLid = senderLid;
            this.senderNotifyName = senderNotifyName;
            this.senderPictureDirectPath = Objects.requireNonNull(senderPictureDirectPath,
                    "senderPictureDirectPath cannot be null");
            this.hasRepliedFlag = hasRepliedFlag;
            this.raw = Objects.requireNonNull(raw, "raw cannot be null");
        }

        /**
         * Returns the message stanza id.
         *
         * @return the id; never {@code null}
         */
        public String messageId() {
            return messageId;
        }

        /**
         * Returns the message timestamp (unix seconds).
         *
         * @return the timestamp
         */
        public long messageTimestamp() {
            return messageTimestamp;
        }

        /**
         * Returns whether the message was authored by the connected
         * client.
         *
         * @return {@code true} when {@code is_sender="true"} was
         *         present
         */
        public boolean fromSelf() {
            return fromSelf;
        }

        /**
         * Returns the optional sender LID.
         *
         * @return an {@link Optional} carrying the LID, or empty when
         *         omitted
         */
        public Optional<Jid> senderLid() {
            return Optional.ofNullable(senderLid);
        }

        /**
         * Returns the optional sender notify-name.
         *
         * @return an {@link Optional} carrying the notify-name, or
         *         empty when omitted
         */
        public Optional<String> senderNotifyName() {
            return Optional.ofNullable(senderNotifyName);
        }

        /**
         * Returns the sender's profile-picture direct-path.
         *
         * @return the direct-path; never {@code null}
         */
        public String senderPictureDirectPath() {
            return senderPictureDirectPath;
        }

        /**
         * Returns whether the question owner has explicitly replied
         * to this response.
         *
         * @return {@code true} when the {@code <replied/>} flag was
         *         present
         */
        public boolean hasRepliedFlag() {
            return hasRepliedFlag;
        }

        /**
         * Returns the underlying {@link Node} for downstream
         * inspection of the content-type / payload children.
         *
         * @return the raw node; never {@code null}
         */
        public Node raw() {
            return raw;
        }

        /**
         * Tries to parse a {@link QuestionResponse} entry from a
         * {@code <question_response>} node.
         *
         * @param node the source node; never {@code null}
         * @return an {@link Optional} carrying the parsed entry, or
         *         empty when the node does not match the schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterResponsesResponseSuccess",
                exports = "parseGetNewsletterResponsesResponseSuccessQuestionResponsesQuestionResponse",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<QuestionResponse> of(Node node) {
            Objects.requireNonNull(node, "node cannot be null");
            if (!node.hasDescription("question_response")) {
                return Optional.empty();
            }
            var messageNode = node.getChild("message").orElse(null);
            if (messageNode == null) {
                return Optional.empty();
            }
            var messageId = messageNode.getAttributeAsString("id").orElse(null);
            if (messageId == null) {
                return Optional.empty();
            }
            var tOpt = messageNode.getAttributeAsLong("t");
            if (tOpt.isEmpty()) {
                return Optional.empty();
            }
            var messageT = tOpt.getAsLong();
            if (messageT < 1577865600L || messageT > 4102473600L) {
                return Optional.empty();
            }
            var fromSelf = messageNode.hasAttribute("is_sender", "true");
            var senderNode = node.getChild("sender").orElse(null);
            if (senderNode == null) {
                return Optional.empty();
            }
            var pictureNode = senderNode.getChild("picture").orElse(null);
            if (pictureNode == null) {
                return Optional.empty();
            }
            var directPath = pictureNode.getAttributeAsString("direct_path").orElse(null);
            if (directPath == null) {
                return Optional.empty();
            }
            var senderLid = senderNode.getAttributeAsJid("lid").orElse(null);
            var notifyName = senderNode.getAttributeAsString("notify_name").orElse(null);
            var flagsNode = node.getChild("flags").orElse(null);
            var hasReplied = flagsNode != null && flagsNode.getChild("replied").isPresent();
            return Optional.of(new QuestionResponse(messageId, messageT, fromSelf,
                    senderLid, notifyName, directPath, hasReplied, node));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (QuestionResponse) obj;
            return this.messageTimestamp == that.messageTimestamp
                    && this.fromSelf == that.fromSelf
                    && this.hasRepliedFlag == that.hasRepliedFlag
                    && Objects.equals(this.messageId, that.messageId)
                    && Objects.equals(this.senderLid, that.senderLid)
                    && Objects.equals(this.senderNotifyName, that.senderNotifyName)
                    && Objects.equals(this.senderPictureDirectPath, that.senderPictureDirectPath)
                    && Objects.equals(this.raw, that.raw);
        }

        @Override
        public int hashCode() {
            return Objects.hash(messageId, messageTimestamp, fromSelf, senderLid, senderNotifyName,
                    senderPictureDirectPath, hasRepliedFlag, raw);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterResponsesResponse.QuestionResponse[messageId="
                    + messageId
                    + ", messageTimestamp=" + messageTimestamp
                    + ", fromSelf=" + fromSelf
                    + ", senderLid=" + senderLid
                    + ", senderNotifyName=" + senderNotifyName
                    + ", senderPictureDirectPath=" + senderPictureDirectPath
                    + ", hasRepliedFlag=" + hasRepliedFlag + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request as malformed, unauthorised, referencing a non-existent
     * newsletter or question. The newsletter is suspended, or the
     * caller hit a rate / not-allowed limit.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterResponsesResponseClientError.parseGetNewsletterResponsesResponseClientError}
     *           routes through
     *           {@code WASmaxInNewslettersGetNewsletterResponsesClientErrors}
     *           which is itself a six-way disjunction
     *           ({@code BadRequest}/{@code Unauthorized}/{@code ItemNotFound}/{@code Suspended}/{@code RateLimited}/{@code NotAllowed});
     *           Cobalt collapses to the {@code (code, text)} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterResponsesResponseClientError")
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterResponsesClientErrors")
    final class ClientError implements SmaxNewslettersGetNewsletterResponsesResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
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
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterResponsesResponseClientError",
                exports = "parseGetNewsletterResponsesResponseClientError",
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
            return "SmaxNewslettersGetNewsletterResponsesResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure while processing the request.
     *
     * @implNote {@code WASmaxInNewslettersGetNewsletterResponsesResponseServerError.parseGetNewsletterResponsesResponseServerError}
     *           delegates to {@code parseIQErrorInternalServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersGetNewsletterResponsesResponseServerError")
    final class ServerError implements SmaxNewslettersGetNewsletterResponsesResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
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
         * Tries to parse a {@link ServerError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersGetNewsletterResponsesResponseServerError",
                exports = "parseGetNewsletterResponsesResponseServerError",
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
            return "SmaxNewslettersGetNewsletterResponsesResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
