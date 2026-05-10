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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxNewslettersMyAddOnsRequest}.
 */
public sealed interface SmaxNewslettersMyAddOnsResponse extends SmaxOperation.Response
        permits SmaxNewslettersMyAddOnsResponse.Success, SmaxNewslettersMyAddOnsResponse.ClientError, SmaxNewslettersMyAddOnsResponse.ServerError {

    /**
     * Tries each {@link SmaxNewslettersMyAddOnsResponse} variant in priority order and returns
     * the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza, used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or empty
     *         when no documented variant matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxNewslettersMyAddOnsRPC",
            exports = "sendMyAddOnsRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxNewslettersMyAddOnsResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay returned the user's
     * per-newsletter add-on list.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersMyAddOnsResponseSuccess")
    final class Success implements SmaxNewslettersMyAddOnsResponse {
        /**
         * The per-newsletter blocks returned by the relay.
         */
        private final List<NewsletterBlock> blocks;

        /**
         * Constructs a new successful reply.
         *
         * @param blocks the per-newsletter blocks; never {@code null}
         */
        public Success(List<NewsletterBlock> blocks) {
            this.blocks = List.copyOf(Objects.requireNonNullElse(blocks, List.of()));
        }

        /**
         * Returns the per-newsletter blocks.
         *
         * @return an unmodifiable list of blocks; never {@code null}
         */
        public List<NewsletterBlock> blocks() {
            return blocks;
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersMyAddOnsResponseSuccess",
                exports = "parseMyAddOnsResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var myAddOns = node.getChild("my_addons").orElse(null);
            if (myAddOns == null) {
                return Optional.empty();
            }
            var blocks = new ArrayList<NewsletterBlock>();
            for (var messagesNode : myAddOns.getChildren("messages")) {
                var block = NewsletterBlock.of(messagesNode).orElse(null);
                if (block == null) {
                    return Optional.empty();
                }
                blocks.add(block);
            }
            return Optional.of(new Success(blocks));
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
            return Objects.equals(this.blocks, that.blocks);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blocks);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersMyAddOnsResponse.Success[blocks=" + blocks + ']';
        }

        /**
         * One per-newsletter block returned by the relay. Projects a
         * {@code <messages jid="<newsletterJid>"><message ...>*</messages>}
         * sub-tree into a typed pair of {@code (newsletterJid,
         * messages)}.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInNewslettersMyAddOnsResponseSuccess")
        public static final class NewsletterBlock {
            /**
             * The newsletter JID this block belongs to.
             */
            private final Jid newsletterJid;

            /**
             * The per-message add-on entries projected from the
             * {@code <messages>} child.
             */
            private final List<MessageAddOns> messages;

            /**
             * Constructs a new block.
             *
             * @param newsletterJid the newsletter JID; never
             *                      {@code null}
             * @param messages      the message entries; never
             *                      {@code null}
             */
            public NewsletterBlock(Jid newsletterJid, List<MessageAddOns> messages) {
                this.newsletterJid = Objects.requireNonNull(newsletterJid, "newsletterJid cannot be null");
                this.messages = List.copyOf(Objects.requireNonNullElse(messages, List.of()));
            }

            /**
             * Returns the newsletter JID for this block.
             *
             * @return the newsletter JID; never {@code null}
             */
            public Jid newsletterJid() {
                return newsletterJid;
            }

            /**
             * Returns the per-message add-on entries.
             *
             * @return an unmodifiable list; never {@code null}
             */
            public List<MessageAddOns> messages() {
                return messages;
            }

            /**
             * Tries to parse a {@link NewsletterBlock} from a
             * {@code <messages>} child.
             *
             * @param messagesNode the {@code <messages>} child; never
             *                     {@code null}
             * @return an {@link Optional} carrying the parsed block, or
             *         empty when the child does not match the schema
             */
            @WhatsAppWebExport(moduleName = "WASmaxInNewslettersMyAddOnsResponseSuccess",
                    exports = "parseMyAddOnsResponseSuccessMyAddonsMessages",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<NewsletterBlock> of(Node messagesNode) {
                if (!messagesNode.hasDescription("messages")) {
                    return Optional.empty();
                }
                var jid = messagesNode.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var messages = new ArrayList<MessageAddOns>();
                for (var messageNode : messagesNode.getChildren("message")) {
                    var entry = MessageAddOns.of(messageNode).orElse(null);
                    if (entry == null) {
                        return Optional.empty();
                    }
                    messages.add(entry);
                }
                return Optional.of(new NewsletterBlock(jid, messages));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (NewsletterBlock) obj;
                return Objects.equals(this.newsletterJid, that.newsletterJid)
                        && Objects.equals(this.messages, that.messages);
            }

            @Override
            public int hashCode() {
                return Objects.hash(newsletterJid, messages);
            }

            @Override
            public String toString() {
                return "SmaxNewslettersMyAddOnsResponse.Success.NewsletterBlock[newsletterJid="
                        + newsletterJid + ", messages=" + messages + ']';
            }
        }

        /**
         * One per-message add-on entry. Projects a
         * {@code <message server_id><reaction?/><votes?/></message>}
         * sub-tree into a typed bundle.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInNewslettersNewsletterMessageMyAddOnsMixin")
        public static final class MessageAddOns {
            /**
             * The server-assigned message id within the newsletter.
             */
            private final long serverId;

            /**
             * The optional own-reaction projection; {@code null} when
             * absent.
             */
            private final MyReaction reaction;

            /**
             * The optional own-poll-vote projection; {@code null} when
             * absent.
             */
            private final MyPollVote pollVote;

            /**
             * Constructs a new message add-on bundle.
             *
             * @param serverId the server-assigned message id
             * @param reaction the optional own-reaction; may be
             *                 {@code null}
             * @param pollVote the optional own-poll-vote; may be
             *                 {@code null}
             */
            public MessageAddOns(long serverId, MyReaction reaction, MyPollVote pollVote) {
                this.serverId = serverId;
                this.reaction = reaction;
                this.pollVote = pollVote;
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
             * Returns the optional own-reaction projection.
             *
             * @return an {@link Optional} carrying the reaction, or
             *         empty when the relay omitted it
             */
            public Optional<MyReaction> reaction() {
                return Optional.ofNullable(reaction);
            }

            /**
             * Returns the optional own-poll-vote projection.
             *
             * @return an {@link Optional} carrying the poll vote, or
             *         empty when the relay omitted it
             */
            public Optional<MyPollVote> pollVote() {
                return Optional.ofNullable(pollVote);
            }

            /**
             * Tries to parse a {@link MessageAddOns} from a
             * {@code <message>} child.
             *
             * @param messageNode the {@code <message>} child; never
             *                    {@code null}
             * @return an {@link Optional} carrying the parsed entry, or
             *         empty when the child does not match the schema
             */
            @WhatsAppWebExport(moduleName = "WASmaxInNewslettersNewsletterMessageMyAddOnsMixin",
                    exports = "parseNewsletterMessageMyAddOnsMixin",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<MessageAddOns> of(Node messageNode) {
                if (!messageNode.hasDescription("message")) {
                    return Optional.empty();
                }
                var serverIdOpt = messageNode.getAttributeAsLong("server_id");
                if (serverIdOpt.isEmpty()) {
                    return Optional.empty();
                }
                var serverId = serverIdOpt.getAsLong();
                if (serverId < 99 || serverId > 2147476647L) {
                    return Optional.empty();
                }
                var reaction = MyReaction.of(messageNode).orElse(null);
                var pollVote = MyPollVote.of(messageNode).orElse(null);
                return Optional.of(new MessageAddOns(serverId, reaction, pollVote));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (MessageAddOns) obj;
                return this.serverId == that.serverId
                        && Objects.equals(this.reaction, that.reaction)
                        && Objects.equals(this.pollVote, that.pollVote);
            }

            @Override
            public int hashCode() {
                return Objects.hash(serverId, reaction, pollVote);
            }

            @Override
            public String toString() {
                return "SmaxNewslettersMyAddOnsResponse.Success.MessageAddOns[serverId="
                        + serverId + ", reaction=" + reaction + ", pollVote=" + pollVote + ']';
            }
        }

        /**
         * The user's own reaction on a newsletter message .
         * {@code <reaction code t/>}.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInNewslettersNewsletterMyReactionMixin")
        public static final class MyReaction {
            /**
             * The emoji code chosen by the user.
             */
            private final String code;

            /**
             * The unix-second timestamp when the reaction was placed.
             */
            private final long timestamp;

            /**
             * Constructs a new own-reaction projection.
             *
             * @param code      the emoji code; never {@code null}
             * @param timestamp the unix-second timestamp
             */
            public MyReaction(String code, long timestamp) {
                this.code = Objects.requireNonNull(code, "code cannot be null");
                this.timestamp = timestamp;
            }

            /**
             * Returns the emoji code.
             *
             * @return the code; never {@code null}
             */
            public String code() {
                return code;
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
             * Tries to parse a {@link MyReaction} from a
             * {@code <message>} sub-tree.
             *
             * @param messageNode the parent {@code <message>} child;
             *                    never {@code null}
             * @return an {@link Optional} carrying the parsed
             *         reaction, or empty when absent or malformed
             */
            @WhatsAppWebExport(moduleName = "WASmaxInNewslettersNewsletterMyReactionMixin",
                    exports = "parseNewsletterMyReactionMixin",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<MyReaction> of(Node messageNode) {
                var reactionNode = messageNode.getChild("reaction").orElse(null);
                if (reactionNode == null) {
                    return Optional.empty();
                }
                var code = reactionNode.getAttributeAsString("code").orElse(null);
                if (code == null) {
                    return Optional.empty();
                }
                var tOpt = reactionNode.getAttributeAsLong("t");
                if (tOpt.isEmpty()) {
                    return Optional.empty();
                }
                var t = tOpt.getAsLong();
                if (t < 0) {
                    return Optional.empty();
                }
                return Optional.of(new MyReaction(code, t));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (MyReaction) obj;
                return this.timestamp == that.timestamp
                        && Objects.equals(this.code, that.code);
            }

            @Override
            public int hashCode() {
                return Objects.hash(code, timestamp);
            }

            @Override
            public String toString() {
                return "SmaxNewslettersMyAddOnsResponse.Success.MyReaction[code="
                        + code + ", timestamp=" + timestamp + ']';
            }
        }

        /**
         * The user's own poll-vote projection .
         * {@code <votes t><vote/>+</votes>} where every {@code <vote/>}
         * carries an opaque 32-byte option id as its content.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInNewslettersNewsletterMyPollVoteMixin")
        public static final class MyPollVote {
            /**
             * The unix-second timestamp when the vote was cast.
             */
            private final long timestamp;

            /**
             * The list of opaque 32-byte option ids the user selected.
             */
            private final List<byte[]> votes;

            /**
             * Constructs a new own-poll-vote projection.
             *
             * @param timestamp the unix-second timestamp
             * @param votes     the option ids; never {@code null}
             */
            public MyPollVote(long timestamp, List<byte[]> votes) {
                this.timestamp = timestamp;
                this.votes = List.copyOf(Objects.requireNonNullElse(votes, List.of()));
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
             * Returns the opaque 32-byte option ids the user selected.
             *
             * @return an unmodifiable list of byte arrays; never
             *         {@code null}
             */
            public List<byte[]> votes() {
                return votes;
            }

            /**
             * Tries to parse a {@link MyPollVote} from a
             * {@code <message>} sub-tree.
             *
             * @param messageNode the parent {@code <message>} child;
             *                    never {@code null}
             * @return an {@link Optional} carrying the parsed vote
             *         block, or empty when absent or malformed
             */
            @WhatsAppWebExport(moduleName = "WASmaxInNewslettersNewsletterMyPollVoteMixin",
                    exports = "parseNewsletterMyPollVoteMixin",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<MyPollVote> of(Node messageNode) {
                var votesNode = messageNode.getChild("votes").orElse(null);
                if (votesNode == null) {
                    return Optional.empty();
                }
                var tOpt = votesNode.getAttributeAsLong("t");
                if (tOpt.isEmpty()) {
                    return Optional.empty();
                }
                var t = tOpt.getAsLong();
                if (t < 0) {
                    return Optional.empty();
                }
                var voteIds = new ArrayList<byte[]>();
                for (var voteNode : votesNode.getChildren("vote")) {
                    var content = voteNode.toContentBytes().orElse(null);
                    if (content == null || content.length != 32) {
                        return Optional.empty();
                    }
                    voteIds.add(content);
                }
                return Optional.of(new MyPollVote(t, voteIds));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (MyPollVote) obj;
                if (this.timestamp != that.timestamp) {
                    return false;
                }
                if (this.votes.size() != that.votes.size()) {
                    return false;
                }
                for (var i = 0; i < this.votes.size(); i++) {
                    if (!Arrays.equals(this.votes.get(i), that.votes.get(i))) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public int hashCode() {
                var result = Long.hashCode(timestamp);
                for (var vote : votes) {
                    result = 31 * result + Arrays.hashCode(vote);
                }
                return result;
            }

            @Override
            public String toString() {
                return "SmaxNewslettersMyAddOnsResponse.Success.MyPollVote[timestamp="
                        + timestamp + ", votes=" + votes.size() + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request as malformed, unauthorised, or referencing a
     * non-existent newsletter.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersMyAddOnsResponseClientError")
    final class ClientError implements SmaxNewslettersMyAddOnsResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersMyAddOnsResponseClientError",
                exports = "parseMyAddOnsResponseClientError",
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
            return "SmaxNewslettersMyAddOnsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure while processing the request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInNewslettersMyAddOnsResponseServerError")
    final class ServerError implements SmaxNewslettersMyAddOnsResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInNewslettersMyAddOnsResponseServerError",
                exports = "parseMyAddOnsResponseServerError",
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
            return "SmaxNewslettersMyAddOnsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
