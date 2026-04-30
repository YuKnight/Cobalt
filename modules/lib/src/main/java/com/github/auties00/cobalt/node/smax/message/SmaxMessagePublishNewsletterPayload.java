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
 * Sealed disjunction over the publish-payload addressing modes.
 * either a "client + server id" reference (publishing a
 * question-response, reaction, reaction-revoke, or poll-vote tied to
 * a previously-published message identified by its server-id) or a
 * "client id only" reference (publishing a brand-new message,
 * carrying optional msg-meta-origin and sender content-type media
 * RCAT children).
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishClientNewsletterAndServerOrNewsletterIDMixinGroup")
public sealed interface SmaxMessagePublishNewsletterPayload permits SmaxMessagePublishNewsletterPayload.WithServerId, SmaxMessagePublishNewsletterPayload.WithClientIdOnly {

    /**
     * The "client id + server id" payload. Used for publishing a
     * question-response / reaction / reaction-revoke / poll-vote
     * that references a previously-published message.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishNewsletterClientAndServerIDMixin")
    @WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishNewsletterQuestionResponsePublishOrReactionOrReactionRevokeOrPollVoteMixinGroup")
    final class WithServerId implements SmaxMessagePublishNewsletterPayload {
        /**
         * The locally-generated stanza id assigned to the publish.
         */
        private final String stanzaId;

        /**
         * The server-id of the message the publish targets.
         */
        private final long messageServerId;

        /**
         * The inner content payload as a fully-built {@link Node}.
         * one of the disjunctive
         * {@code WASmaxOutMessagePublishNewsletterQuestionResponsePublish},
         * {@code WASmaxOutMessagePublishNewsletterReaction},
         * {@code WASmaxOutMessagePublishNewsletterReactionRevoke},
         * {@code WASmaxOutMessagePublishNewsletterPollVote} variants.
         *
         * <p>Cobalt accepts a pre-built node here because the four
         * variants ship distinct child schemas; the caller selects
         * the variant by constructing the appropriate node tree.
         */
        private final Node innerContent;

        /**
         * Constructs a new "client + server id" payload.
         *
         * @param stanzaId        the publish stanza id; never
         *                        {@code null}
         * @param messageServerId the server-id of the targeted
         *                        message
         * @param innerContent    the variant-shaped child payload;
         *                        never {@code null}
         * @throws NullPointerException if {@code stanzaId} or
         *                              {@code innerContent} is
         *                              {@code null}
         */
        public WithServerId(String stanzaId, long messageServerId, Node innerContent) {
            this.stanzaId = Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
            this.messageServerId = messageServerId;
            this.innerContent = Objects.requireNonNull(innerContent, "innerContent cannot be null");
        }

        /**
         * Returns the publish stanza id.
         *
         * @return the id; never {@code null}
         */
        public String stanzaId() {
            return stanzaId;
        }

        /**
         * Returns the targeted message server-id.
         *
         * @return the server id
         */
        public long messageServerId() {
            return messageServerId;
        }

        /**
         * Returns the inner content node.
         *
         * @return the node; never {@code null}
         */
        public Node innerContent() {
            return innerContent;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (WithServerId) obj;
            return this.messageServerId == that.messageServerId
                    && Objects.equals(this.stanzaId, that.stanzaId)
                    && Objects.equals(this.innerContent, that.innerContent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stanzaId, messageServerId, innerContent);
        }

        @Override
        public String toString() {
            return "SmaxMessagePublishNewsletterPayload.WithServerId[stanzaId=" + stanzaId
                    + ", messageServerId=" + messageServerId
                    + ", innerContent=" + innerContent + ']';
        }
    }

    /**
     * The "client id only" payload. Used for publishing a brand-new
     * message, optionally carrying msg-meta-origin and sender
     * content-type-media RCAT children alongside the inner client-id
     * content.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishNewsletterClientIDMixin")
    @WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishNewsletterClientIdContent")
    @WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishMsgMetaOriginMixin")
    @WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishSenderContentTypeMediaRCATMixin")
    final class WithClientIdOnly implements SmaxMessagePublishNewsletterPayload {
        /**
         * The locally-generated stanza id assigned to the publish.
         */
        private final String stanzaId;

        /**
         * The optional msg-meta-origin marker child as a pre-built
         * node, or {@code null} when the publish is not an
         * origin-tagged broadcast.
         */
        private final Node msgMetaOrigin;

        /**
         * The optional {@code <plaintext mediatype="url"/>} +
         * {@code <rcat>...</rcat>} pair as a pre-built sender
         * content-type-media RCAT child, or {@code null} when the
         * publish does not carry a media payload.
         */
        private final Node senderContentTypeMediaRcat;

        /**
         * The inner client-id content payload as a fully-built node.
         */
        private final Node clientIdContent;

        /**
         * Constructs a new "client id only" payload.
         *
         * @param stanzaId                   the publish stanza id;
         *                                   never {@code null}
         * @param msgMetaOrigin              the optional
         *                                   msg-meta-origin child;
         *                                   may be {@code null}
         * @param senderContentTypeMediaRcat the optional sender
         *                                   content-type media RCAT
         *                                   child; may be
         *                                   {@code null}
         * @param clientIdContent            the inner client-id
         *                                   content node; never
         *                                   {@code null}
         * @throws NullPointerException if {@code stanzaId} or
         *                              {@code clientIdContent} is
         *                              {@code null}
         */
        public WithClientIdOnly(String stanzaId,
                                Node msgMetaOrigin,
                                Node senderContentTypeMediaRcat,
                                Node clientIdContent) {
            this.stanzaId = Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
            this.msgMetaOrigin = msgMetaOrigin;
            this.senderContentTypeMediaRcat = senderContentTypeMediaRcat;
            this.clientIdContent = Objects.requireNonNull(clientIdContent, "clientIdContent cannot be null");
        }

        /**
         * Returns the publish stanza id.
         *
         * @return the id; never {@code null}
         */
        public String stanzaId() {
            return stanzaId;
        }

        /**
         * Returns the optional msg-meta-origin child.
         *
         * @return an {@link Optional} carrying the node, or empty
         *         when the publish is not an origin-tagged broadcast
         */
        public Optional<Node> msgMetaOrigin() {
            return Optional.ofNullable(msgMetaOrigin);
        }

        /**
         * Returns the optional sender content-type-media RCAT child.
         *
         * @return an {@link Optional} carrying the node, or empty
         *         when no media payload is being sent
         */
        public Optional<Node> senderContentTypeMediaRcat() {
            return Optional.ofNullable(senderContentTypeMediaRcat);
        }

        /**
         * Returns the inner client-id content node.
         *
         * @return the node; never {@code null}
         */
        public Node clientIdContent() {
            return clientIdContent;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (WithClientIdOnly) obj;
            return Objects.equals(this.stanzaId, that.stanzaId)
                    && Objects.equals(this.msgMetaOrigin, that.msgMetaOrigin)
                    && Objects.equals(this.senderContentTypeMediaRcat, that.senderContentTypeMediaRcat)
                    && Objects.equals(this.clientIdContent, that.clientIdContent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stanzaId, msgMetaOrigin, senderContentTypeMediaRcat, clientIdContent);
        }

        @Override
        public String toString() {
            return "SmaxMessagePublishNewsletterPayload.WithClientIdOnly[stanzaId=" + stanzaId
                    + ", msgMetaOrigin=" + msgMetaOrigin
                    + ", senderContentTypeMediaRcat=" + senderContentTypeMediaRcat
                    + ", clientIdContent=" + clientIdContent + ']';
        }
    }
}
