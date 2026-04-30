package com.github.auties00.cobalt.node.smax.newsletters;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound acknowledgement stanza. Emitted by the client back
 * through the socket pipeline after consuming the {@link SmaxNewslettersLiveUpdatesNotificationResponse}
 * notification.
 *
 * @implNote {@code WASmaxOutNewslettersLiveUpdatesNotificationResponseAck.makeLiveUpdatesNotificationResponseAck}
 *           composes
 *           {@code WASmaxOutNewslettersNotificationClientAckMixin.mergeNotificationClientAckMixin}
 *           into an {@code <ack id to class="notification" type/>}
 *           stanza echoing the notification's {@code id},
 *           {@code from} (as the ack's {@code to}) and {@code type}
 *           attributes.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersLiveUpdatesNotificationResponseAck")
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersNotificationClientAckMixin")
public final class SmaxNewslettersLiveUpdatesNotificationAcknowledgement implements SmaxOperation.Request {
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
     * ack, typically {@code "newsletter"}).
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
    public SmaxNewslettersLiveUpdatesNotificationAcknowledgement(String notificationId, Jid notificationFrom, String notificationType) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
        this.notificationType = Objects.requireNonNull(notificationType, "notificationType cannot be null");
    }

    /**
     * Constructs an acknowledgement from a parsed inbound
     * notification.
     *
     * <p>Lifts the {@code id}/{@code from}/{@code type} attributes
     * verbatim from the supplied {@code <notification/>} stanza .
     * convenience factory mirroring the WA Web closure-builder
     * surface.
     *
     * @param notification the inbound notification stanza; never
     *                     {@code null}
     * @return a new acknowledgement
     * @throws NullPointerException     if {@code notification} is
     *                                  {@code null}
     * @throws IllegalArgumentException if the notification is
     *                                  missing one of the required
     *                                  echoed attributes
     */
    public static SmaxNewslettersLiveUpdatesNotificationAcknowledgement from(Node notification) {
        Objects.requireNonNull(notification, "notification cannot be null");
        var id = notification.getAttributeAsString("id")
                .orElseThrow(() -> new IllegalArgumentException("notification is missing id attribute"));
        var from = notification.getAttributeAsJid("from")
                .orElseThrow(() -> new IllegalArgumentException("notification is missing from attribute"));
        var type = notification.getAttributeAsString("type")
                .orElseThrow(() -> new IllegalArgumentException("notification is missing type attribute"));
        return new SmaxNewslettersLiveUpdatesNotificationAcknowledgement(id, from, type);
    }

    /**
     * Returns the notification id being acknowledged.
     *
     * @return the notification id; never {@code null}
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
     * @implNote {@code WASmaxOutNewslettersNotificationClientAckMixin.mergeNotificationClientAckMixin}
     *           produces
     *           {@code <ack id=STANZA_ID(t) to=JID(n)
     *           class="notification" type=CUSTOM_STRING(r)/>}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutNewslettersLiveUpdatesNotificationResponseAck",
            exports = "makeLiveUpdatesNotificationResponseAck",
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
        var that = (SmaxNewslettersLiveUpdatesNotificationAcknowledgement) obj;
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
        return "SmaxNewslettersLiveUpdatesNotificationAcknowledgement[notificationId=" + notificationId
                + ", notificationFrom=" + notificationFrom
                + ", notificationType=" + notificationType + ']';
    }
}
