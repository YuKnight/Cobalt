package com.github.auties00.cobalt.node.smax.message;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of outbound ack variants the client emits in
 * response to an {@link SmaxMessageDeliverNewsletterResponse} delivery — either a positive
 * {@link SuccessAck} or a negative {@link ErrorAck} carrying the
 * fixed {@code error="406"} marker.
 *
 * @implNote {@code WASmaxMessageDeliverNewsletterRPC.receiveNewsletterRPC}
 *           returns a closure pair
 *           ({@code makeNewsletterResponseSuccess},
 *           {@code makeNewsletterResponseError}) that compose
 *           {@code WASmaxOutMessageDeliverCommonAckMixin} over a
 *           bare {@code <ack/>} child (success) or one with
 *           {@code error="406"} (error).
 */
public sealed interface SmaxMessageDeliverNewsletterAcknowledgement extends SmaxOperation.Request
        permits SmaxMessageDeliverNewsletterAcknowledgement.SuccessAck, SmaxMessageDeliverNewsletterAcknowledgement.ErrorAck {

    /**
     * The positive acknowledgement variant — the client successfully
     * decoded and persisted the inbound newsletter message.
     *
     * @implNote {@code WASmaxOutMessageDeliverNewsletterResponseSuccess.makeNewsletterResponseSuccess}
     *           composes {@code WASmaxOutMessageDeliverCommonAckMixin}
     *           over a bare {@code <ack/>} child.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutMessageDeliverNewsletterResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxOutMessageDeliverCommonAckMixin")
    final class SuccessAck implements SmaxMessageDeliverNewsletterAcknowledgement {
        /**
         * The {@code id} of the message being acknowledged.
         */
        private final String stanzaId;

        /**
         * The {@code from} of the message (becomes the ack's
         * {@code to}).
         */
        private final Jid notificationFrom;

        /**
         * The {@code type} of the message (echoed back into the
         * ack).
         */
        private final String stanzaType;

        /**
         * Constructs a positive acknowledgement.
         *
         * @param stanzaId         the message id; never {@code null}
         * @param notificationFrom the message sender JID; never
         *                         {@code null}
         * @param stanzaType       the message type; never {@code null}
         * @throws NullPointerException if any argument is {@code null}
         */
        public SuccessAck(String stanzaId, Jid notificationFrom, String stanzaType) {
            this.stanzaId = Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
            this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
            this.stanzaType = Objects.requireNonNull(stanzaType, "stanzaType cannot be null");
        }

        /**
         * Returns the message id being acknowledged.
         *
         * @return the id; never {@code null}
         */
        public String stanzaId() {
            return stanzaId;
        }

        /**
         * Returns the message sender JID.
         *
         * @return the sender JID; never {@code null}
         */
        public Jid notificationFrom() {
            return notificationFrom;
        }

        /**
         * Returns the message type.
         *
         * @return the type; never {@code null}
         */
        public String stanzaType() {
            return stanzaType;
        }

        /**
         * Builds the outbound positive ack stanza.
         *
         * @param inbound the {@code <message/>} stanza being
         *                acknowledged; never {@code null} — required
         *                so the relay can correlate the ack back to
         *                the source delivery
         * @return a {@link NodeBuilder} carrying the ack envelope
         * @throws NullPointerException if {@code inbound} is
         *                              {@code null}
         *
         * @implNote {@code WASmaxOutMessageDeliverNewsletterResponseSuccess.makeNewsletterResponseSuccess}
         *           wraps {@code WASmaxOutMessageDeliverCommonAckMixin}
         *           around a bare {@code <ack/>} child, producing
         *           {@code <ack to=JID(from) class="message"
         *           id=STANZA_ID(id) type=CUSTOM_STRING(type)/>}.
         */
        @WhatsAppWebExport(moduleName = "WASmaxOutMessageDeliverNewsletterResponseSuccess",
                exports = "makeNewsletterResponseSuccess",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public NodeBuilder toNode() {
            return new NodeBuilder()
                    .description("ack")
                    .attribute("to", notificationFrom)
                    .attribute("class", "message")
                    .attribute("id", stanzaId)
                    .attribute("type", stanzaType);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessAck) obj;
            return Objects.equals(this.stanzaId, that.stanzaId)
                    && Objects.equals(this.notificationFrom, that.notificationFrom)
                    && Objects.equals(this.stanzaType, that.stanzaType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stanzaId, notificationFrom, stanzaType);
        }

        @Override
        public String toString() {
            return "SmaxMessageDeliverNewsletterAcknowledgement.SuccessAck[stanzaId=" + stanzaId
                    + ", notificationFrom=" + notificationFrom
                    + ", stanzaType=" + stanzaType + ']';
        }
    }

    /**
     * The negative acknowledgement variant — emitted by the client
     * when the inbound newsletter message could not be processed
     * (decryption failure, malformed payload, schema mismatch).
     *
     * @implNote {@code WASmaxOutMessageDeliverNewsletterResponseError.makeNewsletterResponseError}
     *           composes {@code WASmaxOutMessageDeliverCommonAckMixin}
     *           over an {@code <ack error="406"/>} child.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutMessageDeliverNewsletterResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxOutMessageDeliverCommonAckMixin")
    final class ErrorAck implements SmaxMessageDeliverNewsletterAcknowledgement {
        /**
         * The {@code id} of the message being NACK'd.
         */
        private final String stanzaId;

        /**
         * The {@code from} of the message (becomes the ack's
         * {@code to}).
         */
        private final Jid notificationFrom;

        /**
         * The {@code type} of the message (echoed back into the
         * ack).
         */
        private final String stanzaType;

        /**
         * Constructs a negative acknowledgement.
         *
         * @param stanzaId         the message id; never {@code null}
         * @param notificationFrom the message sender JID; never
         *                         {@code null}
         * @param stanzaType       the message type; never {@code null}
         * @throws NullPointerException if any argument is {@code null}
         */
        public ErrorAck(String stanzaId, Jid notificationFrom, String stanzaType) {
            this.stanzaId = Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
            this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
            this.stanzaType = Objects.requireNonNull(stanzaType, "stanzaType cannot be null");
        }

        /**
         * Returns the message id being NACK'd.
         *
         * @return the id; never {@code null}
         */
        public String stanzaId() {
            return stanzaId;
        }

        /**
         * Returns the message sender JID.
         *
         * @return the sender JID; never {@code null}
         */
        public Jid notificationFrom() {
            return notificationFrom;
        }

        /**
         * Returns the message type.
         *
         * @return the type; never {@code null}
         */
        public String stanzaType() {
            return stanzaType;
        }

        /**
         * Builds the outbound negative ack stanza.
         *
         * @param inbound the {@code <message/>} stanza being NACK'd;
         *                never {@code null}
         * @return a {@link NodeBuilder} carrying the ack envelope
         *         with the fixed {@code error="406"} attribute
         * @throws NullPointerException if {@code inbound} is
         *                              {@code null}
         *
         * @implNote {@code WASmaxOutMessageDeliverNewsletterResponseError.makeNewsletterResponseError}
         *           wraps {@code WASmaxOutMessageDeliverCommonAckMixin}
         *           around an {@code <ack error="406"/>} child.
         */
        @WhatsAppWebExport(moduleName = "WASmaxOutMessageDeliverNewsletterResponseError",
                exports = "makeNewsletterResponseError",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public NodeBuilder toNode() {
            return new NodeBuilder()
                    .description("ack")
                    .attribute("error", "406")
                    .attribute("to", notificationFrom)
                    .attribute("class", "message")
                    .attribute("id", stanzaId)
                    .attribute("type", stanzaType);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ErrorAck) obj;
            return Objects.equals(this.stanzaId, that.stanzaId)
                    && Objects.equals(this.notificationFrom, that.notificationFrom)
                    && Objects.equals(this.stanzaType, that.stanzaType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stanzaId, notificationFrom, stanzaType);
        }

        @Override
        public String toString() {
            return "SmaxMessageDeliverNewsletterAcknowledgement.ErrorAck[stanzaId=" + stanzaId
                    + ", notificationFrom=" + notificationFrom
                    + ", stanzaType=" + stanzaType + ']';
        }
    }
}
