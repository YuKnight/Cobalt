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
 * The inbound notification. The relay's PSA-class push carrying the
 * {@code <surfaces/>} subtree.
 */
@WhatsAppWebModule(moduleName = "WASmaxInQpSurfacesQPNotificationRequest")
@WhatsAppWebModule(moduleName = "WASmaxInQpSurfacesQPSurfacesMixin")
@WhatsAppWebModule(moduleName = "WASmaxInQpSurfacesServerNotificationMixin")
public final class SmaxQpQpNotificationResponse implements SmaxOperation.Response {
    /**
     * The notification id. Echoed verbatim into the ack stanza.
     */
    private final String notificationId;

    /**
     * The notification sender JID. Becomes the ack's {@code to}.
     */
    private final Jid notificationFrom;

    /**
     * The notification type. Always {@code "psa"} on the QP surface
     * channel but kept as a typed field so the ack can echo it.
     */
    private final String notificationType;

    /**
     * The raw {@code <surfaces/>} subtree carrying the QP surfaces /
     * promotion / triggers payload. Consumers re-parse it through the
     * dedicated protobuf pipeline.
     */
    private final Node surfacesNode;

    /**
     * Constructs a new inbound projection.
     *
     * @param notificationId   the notification id. Never {@code null}
     * @param notificationFrom the notification sender JID. Never
     *                         {@code null}
     * @param notificationType the notification type. Never
     *                         {@code null}
     * @param surfacesNode     the raw {@code <surfaces/>} subtree;
     *                         never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxQpQpNotificationResponse(String notificationId, Jid notificationFrom, String notificationType,
                   Node surfacesNode) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
        this.notificationType = Objects.requireNonNull(notificationType, "notificationType cannot be null");
        this.surfacesNode = Objects.requireNonNull(surfacesNode, "surfacesNode cannot be null");
    }

    /**
     * Returns the notification id.
     *
     * @return the id. Never {@code null}
     */
    public String notificationId() {
        return notificationId;
    }

    /**
     * Returns the notification sender JID.
     *
     * @return the sender JID. Never {@code null}
     */
    public Jid notificationFrom() {
        return notificationFrom;
    }

    /**
     * Returns the notification type.
     *
     * @return the type. Never {@code null}
     */
    public String notificationType() {
        return notificationType;
    }

    /**
     * Returns the raw {@code <surfaces/>} subtree.
     *
     * @return the {@code <surfaces/>} node. Never {@code null}
     */
    public Node surfacesNode() {
        return surfacesNode;
    }

    /**
     * Tries to parse an {@link SmaxQpQpNotificationResponse} projection from the given
     * {@code <notification/>} stanza.
     *
     * @param node the inbound notification stanza. Never {@code null}
     * @return an {@link Optional} carrying the projection, or empty
     *         when the stanza doesn't match the expected shape
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInQpSurfacesQPNotificationRequest",
            exports = "parseQPNotificationRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxQpQpNotificationResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("notification")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("type", "psa")) {
            return Optional.empty();
        }
        var id = node.getAttributeAsString("id").orElse(null);
        if (id == null) {
            return Optional.empty();
        }
        var from = node.getAttributeAsJid("from").orElse(null);
        if (from == null) {
            return Optional.empty();
        }
        var surfaces = node.getChild("surfaces").orElse(null);
        if (surfaces == null) {
            return Optional.empty();
        }
        return Optional.of(new SmaxQpQpNotificationResponse(id, from, "psa", surfaces));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxQpQpNotificationResponse) obj;
        return Objects.equals(this.notificationId, that.notificationId)
                && Objects.equals(this.notificationFrom, that.notificationFrom)
                && Objects.equals(this.notificationType, that.notificationType)
                && Objects.equals(this.surfacesNode, that.surfacesNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, notificationFrom, notificationType, surfacesNode);
    }

    @Override
    public String toString() {
        return "SmaxQpQpNotificationResponse[notificationId=" + notificationId
                + ", notificationFrom=" + notificationFrom
                + ", notificationType=" + notificationType + ']';
    }
}
