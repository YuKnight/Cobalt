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
 * Sealed family of outbound ack variants the client emits in
 * response to an {@link SmaxStatusDeliverIncomingNewsletterStatusResponse} status delivery — there is only a
 * single positive {@link SuccessAck} variant; the relay does not
 * expose a negative-ack shape for newsletter-status deliveries.
 *
 * @implNote {@code WASmaxStatusDeliverIncomingNewsletterStatusRPC.receiveIncomingNewsletterStatusRPC}
 *           returns a single closure ({@code makeIncomingNewsletterStatusResponseSuccess})
 *           that composes
 *           {@code WASmaxOutStatusDeliverStatusAckMixin} over a bare
 *           {@code <ack/>} child.
 */
public sealed interface SmaxStatusDeliverIncomingNewsletterStatusAcknowledgement extends SmaxOperation.Request
        permits SmaxStatusDeliverIncomingNewsletterStatusAcknowledgement.SuccessAck {

    /**
     * The positive acknowledgement variant — emitted by the client
     * after consuming the inbound newsletter-status delivery.
     *
     * @implNote {@code WASmaxOutStatusDeliverIncomingNewsletterStatusResponseSuccess.makeIncomingNewsletterStatusResponseSuccess}
     *           composes
     *           {@code WASmaxOutStatusDeliverStatusAckMixin} over a
     *           bare {@code <ack/>} child producing
     *           {@code <ack to=JID(from) class="status"
     *           id=STANZA_ID(id) type=CUSTOM_STRING(type)/>}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutStatusDeliverIncomingNewsletterStatusResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxOutStatusDeliverStatusAckMixin")
    final class SuccessAck implements SmaxStatusDeliverIncomingNewsletterStatusAcknowledgement {
        /**
         * The {@code id} of the status being acknowledged.
         */
        private final String stanzaId;

        /**
         * The {@code from} of the status (becomes the ack's
         * {@code to}).
         */
        private final Jid notificationFrom;

        /**
         * The {@code type} of the status (echoed back into the
         * ack).
         */
        private final String stanzaType;

        /**
         * Constructs a positive acknowledgement.
         *
         * @param stanzaId         the status id; never {@code null}
         * @param notificationFrom the status sender JID; never
         *                         {@code null}
         * @param stanzaType       the status type; never {@code null}
         * @throws NullPointerException if any argument is
         *                              {@code null}
         */
        public SuccessAck(String stanzaId, Jid notificationFrom, String stanzaType) {
            this.stanzaId = Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
            this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
            this.stanzaType = Objects.requireNonNull(stanzaType, "stanzaType cannot be null");
        }

        /**
         * Returns the status id being acknowledged.
         *
         * @return the id; never {@code null}
         */
        public String stanzaId() {
            return stanzaId;
        }

        /**
         * Returns the status sender JID.
         *
         * @return the sender JID; never {@code null}
         */
        public Jid notificationFrom() {
            return notificationFrom;
        }

        /**
         * Returns the status type.
         *
         * @return the type; never {@code null}
         */
        public String stanzaType() {
            return stanzaType;
        }

        /**
         * Builds the outbound positive ack stanza.
         *
         * @param inbound the {@code <status/>} stanza being
         *                acknowledged; never {@code null} —
         *                required so the relay can correlate the
         *                ack back to the source delivery
         * @return a {@link NodeBuilder} carrying the ack envelope
         * @throws NullPointerException if {@code inbound} is
         *                              {@code null}
         *
         * @implNote {@code WASmaxOutStatusDeliverIncomingNewsletterStatusResponseSuccess.makeIncomingNewsletterStatusResponseSuccess}
         *           wraps {@code WASmaxOutStatusDeliverStatusAckMixin}
         *           around a bare {@code <ack/>} child.
         */
        @WhatsAppWebExport(moduleName = "WASmaxOutStatusDeliverIncomingNewsletterStatusResponseSuccess",
                exports = "makeIncomingNewsletterStatusResponseSuccess",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public NodeBuilder toNode() {
            return new NodeBuilder()
                    .description("ack")
                    .attribute("to", notificationFrom)
                    .attribute("class", "status")
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
            return "SmaxStatusDeliverIncomingNewsletterStatusAcknowledgement.SuccessAck[stanzaId="
                    + stanzaId + ", notificationFrom=" + notificationFrom
                    + ", stanzaType=" + stanzaType + ']';
        }
    }
}
