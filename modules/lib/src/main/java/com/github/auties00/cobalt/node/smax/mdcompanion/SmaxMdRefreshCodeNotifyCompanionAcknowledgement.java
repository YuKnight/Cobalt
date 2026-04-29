package com.github.auties00.cobalt.node.smax.mdcompanion;

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
 * The outbound acknowledgement stanza emitted after consuming the
 * {@link SmaxMdRefreshCodeNotifyCompanionResponse} notification.
 *
 * @implNote {@code WASmaxOutMdRefreshCodeNotifyCompanionResponseAck.makeRefreshCodeNotifyCompanionResponseAck}
 *           composes
 *           {@code WASmaxOutMdNotificationClientAckMixin.mergeNotificationClientAckMixin}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMdRefreshCodeNotifyCompanionResponseAck")
@WhatsAppWebModule(moduleName = "WASmaxOutMdNotificationClientAckMixin")
public final class SmaxMdRefreshCodeNotifyCompanionAcknowledgement implements SmaxOperation.Request {
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
     * @param notificationFrom the notification's sender JID; never {@code null}
     * @param notificationType the notification type; never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxMdRefreshCodeNotifyCompanionAcknowledgement(String notificationId, Jid notificationFrom, String notificationType) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
        this.notificationType = Objects.requireNonNull(notificationType, "notificationType cannot be null");
    }

    /**
     * Constructs an ack from a parsed inbound projection.
     *
     * @param inbound the inbound projection; never {@code null}
     * @return a new acknowledgement
     * @throws NullPointerException if {@code inbound} is {@code null}
     */
    public static SmaxMdRefreshCodeNotifyCompanionAcknowledgement from(SmaxMdRefreshCodeNotifyCompanionResponse inbound) {
        Objects.requireNonNull(inbound, "inbound cannot be null");
        return new SmaxMdRefreshCodeNotifyCompanionAcknowledgement(inbound.notificationId(), inbound.notificationFrom(), "link_code_companion_reg");
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
     * @return the JID; never {@code null}
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
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutMdRefreshCodeNotifyCompanionResponseAck",
            exports = "makeRefreshCodeNotifyCompanionResponseAck",
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
        var that = (SmaxMdRefreshCodeNotifyCompanionAcknowledgement) obj;
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
        return "SmaxMdRefreshCodeNotifyCompanionAcknowledgement[notificationId=" + notificationId
                + ", notificationFrom=" + notificationFrom
                + ", notificationType=" + notificationType + ']';
    }
}
