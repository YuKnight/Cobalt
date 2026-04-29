package com.github.auties00.cobalt.node.smax.qp;

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
 * The outbound acknowledgement stanza — emitted by the client back
 * through the socket pipeline after consuming the {@link SmaxQpQpNotificationResponse}
 * notification.
 *
 * @implNote {@code WASmaxOutQpSurfacesQPNotificationResponseAck.makeQPNotificationResponseAck}
 *           composes
 *           {@code WASmaxOutQpSurfacesNotificationClientAckMixin} —
 *           an {@code <ack id to class="notification" type/>} stanza
 *           echoing the notification's {@code id},
 *           {@code from} (as the ack's {@code to}) and {@code type}
 *           attributes.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutQpSurfacesQPNotificationResponseAck")
@WhatsAppWebModule(moduleName = "WASmaxOutQpSurfacesNotificationClientAckMixin")
public final class SmaxQpQpNotificationAcknowledgement implements SmaxOperation.Request {
    /**
     * The {@code id} of the notification being acknowledged.
     */
    private final String notificationId;

    /**
     * The {@code from} of the notification (becomes the ack's
     * {@code to}).
     */
    private final Jid notificationFrom;

    /**
     * The {@code type} of the notification (echoed back into the
     * ack).
     */
    private final String notificationType;

    /**
     * Constructs an acknowledgement.
     *
     * @param notificationId   the notification id; never {@code null}
     * @param notificationFrom the notification's sender JID; never
     *                         {@code null}
     * @param notificationType the notification type; never
     *                         {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxQpQpNotificationAcknowledgement(String notificationId, Jid notificationFrom, String notificationType) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
        this.notificationType = Objects.requireNonNull(notificationType, "notificationType cannot be null");
    }

    /**
     * Constructs an acknowledgement from a parsed {@link SmaxQpQpNotificationResponse}
     * notification.
     *
     * @param inbound the inbound notification; never {@code null}
     * @return a new acknowledgement
     * @throws NullPointerException if {@code inbound} is {@code null}
     */
    public static SmaxQpQpNotificationAcknowledgement from(SmaxQpQpNotificationResponse inbound) {
        Objects.requireNonNull(inbound, "inbound cannot be null");
        return new SmaxQpQpNotificationAcknowledgement(inbound.notificationId(),
                inbound.notificationFrom(), inbound.notificationType());
    }

    /**
     * Constructs an acknowledgement from a raw inbound notification
     * stanza, lifting the {@code id}/{@code from}/{@code type}
     * attributes verbatim.
     *
     * @param notification the inbound notification stanza; never
     *                     {@code null}
     * @return a new acknowledgement
     * @throws NullPointerException     if {@code notification} is
     *                                  {@code null}
     * @throws IllegalArgumentException if the notification is missing
     *                                  one of the required echoed
     *                                  attributes
     */
    public static SmaxQpQpNotificationAcknowledgement from(Node notification) {
        Objects.requireNonNull(notification, "notification cannot be null");
        var id = notification.getAttributeAsString("id")
                .orElseThrow(() -> new IllegalArgumentException("notification is missing id attribute"));
        var from = notification.getAttributeAsJid("from")
                .orElseThrow(() -> new IllegalArgumentException("notification is missing from attribute"));
        var type = notification.getAttributeAsString("type")
                .orElseThrow(() -> new IllegalArgumentException("notification is missing type attribute"));
        return new SmaxQpQpNotificationAcknowledgement(id, from, type);
    }

    /**
     * Returns the notification id being acknowledged.
     *
     * @return the id; never {@code null}
     */
    public String notificationId() {
        return notificationId;
    }

    /**
     * Returns the notification sender JID.
     *
     * @return the sender JID; never {@code null}
     */
    public Jid notificationFrom() {
        return notificationFrom;
    }

    /**
     * Returns the notification type.
     *
     * @return the type; never {@code null}
     */
    public String notificationType() {
        return notificationType;
    }

    /**
     * Builds the outbound ack stanza.
     *
     * @return a {@link NodeBuilder} carrying the ack envelope
     *
     * @implNote {@code WASmaxOutQpSurfacesNotificationClientAckMixin.mergeNotificationClientAckMixin}
     *           produces {@code <ack id=STANZA_ID(t)
     *           to=USER_JID(n) class="notification"
     *           type=CUSTOM_STRING(r)/>}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutQpSurfacesQPNotificationResponseAck",
            exports = "makeQPNotificationResponseAck",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        return new NodeBuilder()
                .description("ack")
                .attribute("id", notificationId)
                .attribute("to", notificationFrom)
                .attribute("class", "notification")
                .attribute("type", notificationType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxQpQpNotificationAcknowledgement) obj;
        return Objects.equals(this.notificationId, that.notificationId)
                && Objects.equals(this.notificationFrom, that.notificationFrom)
                && Objects.equals(this.notificationType, that.notificationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, notificationFrom, notificationType);
    }

    @Override
    public String toString() {
        return "SmaxQpQpNotificationAcknowledgement[notificationId=" + notificationId
                + ", notificationFrom=" + notificationFrom
                + ", notificationType=" + notificationType + ']';
    }
}
