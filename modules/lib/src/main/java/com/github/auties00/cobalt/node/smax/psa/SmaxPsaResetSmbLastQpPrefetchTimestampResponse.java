package com.github.auties00.cobalt.node.smax.psa;

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
 * The inbound notification. The relay's "drop your QP prefetch
 * timestamp" hint.
 */
@WhatsAppWebModule(moduleName = "WASmaxInPsaResetSmbLastQpPrefetchTimestampRequest")
@WhatsAppWebModule(moduleName = "WASmaxInPsaServerNotificationMixin")
public final class SmaxPsaResetSmbLastQpPrefetchTimestampResponse implements SmaxOperation.Response {
    /**
     * The notification id; echoed verbatim into the ack stanza.
     */
    private final String notificationId;

    /**
     * The notification sender JID. Always a user JID per the
     * {@code attrUserJid(from)} assertion.
     */
    private final Jid notificationFrom;

    /**
     * The notification type. Always the literal {@code "psa"} per
     * the {@code literal(attrString, "type", "psa")} assertion.
     */
    private final String notificationType;

    /**
     * The relay-side timestamp echoed by the
     * {@code WASmaxInPsaServerNotificationMixin} envelope ({@code t}
     * attribute, seconds since epoch).
     */
    private final long timestampSeconds;

    /**
     * The optional {@code offline} hint. When set carries the count
     * of offline notifications still queued for delivery.
     */
    private final Integer offline;

    /**
     * Constructs a new inbound projection.
     *
     * @param notificationId   the notification id; never {@code null}
     * @param notificationFrom the notification sender JID; never
     *                         {@code null}
     * @param notificationType the notification type. Always
     *                         {@code "psa"}; never {@code null}
     * @param timestampSeconds the relay-side timestamp in seconds
     * @param offline          the optional offline-queue depth; may
     *                         be {@code null}
     * @throws NullPointerException if any required argument is
     *                              {@code null}
     */
    public SmaxPsaResetSmbLastQpPrefetchTimestampResponse(String notificationId, Jid notificationFrom, String notificationType,
                   long timestampSeconds, Integer offline) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
        this.notificationType = Objects.requireNonNull(notificationType, "notificationType cannot be null");
        this.timestampSeconds = timestampSeconds;
        this.offline = offline;
    }

    /**
     * Returns the notification id.
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
     * Returns the notification type. Always {@code "psa"}.
     *
     * @return the type; never {@code null}
     */
    public String notificationType() {
        return notificationType;
    }

    /**
     * Returns the relay-side timestamp in seconds.
     *
     * @return the timestamp
     */
    public long timestampSeconds() {
        return timestampSeconds;
    }

    /**
     * Returns the optional offline-queue depth.
     *
     * @return an {@link Optional} carrying the offline depth, or
     *         empty when the relay omitted the attribute
     */
    public Optional<Integer> offline() {
        return Optional.ofNullable(offline);
    }

    /**
     * Tries to parse an {@link SmaxPsaResetSmbLastQpPrefetchTimestampResponse} projection from the given
     * {@code <notification/>} stanza.
     *
     * @param node the inbound notification stanza; never {@code null}
     * @return an {@link Optional} carrying the projection, or empty
     *         when the stanza doesn't match the expected shape
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInPsaResetSmbLastQpPrefetchTimestampRequest",
            exports = "parseResetSmbLastQpPrefetchTimestampRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxPsaResetSmbLastQpPrefetchTimestampResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("notification")) {
            return Optional.empty();
        }
        //   literal(attrString, "type", "psa")
        if (!node.hasAttribute("type", "psa")) {
            return Optional.empty();
        }
        var resetChild = node.getChild("reset_smb_last_qp_prefetch_timestamp").orElse(null);
        if (resetChild == null) {
            return Optional.empty();
        }
        var from = node.getAttributeAsJid("from").orElse(null);
        if (from == null) {
            return Optional.empty();
        }
        var type = node.getAttributeAsString("type").orElse(null);
        if (type == null) {
            return Optional.empty();
        }
        //   - attrIntRange(t, 0, void 0)
        //   - attrStanzaId(id)
        //   - optional(attrIntRange, offline, 0, 1024)
        var timestampOpt = node.getAttributeAsLong("t");
        if (timestampOpt.isEmpty()) {
            return Optional.empty();
        }
        var id = node.getAttributeAsString("id").orElse(null);
        if (id == null) {
            return Optional.empty();
        }
        var offlineAttr = node.getAttributeAsInt("offline").orElse(-1);
        var offline = offlineAttr < 0 ? null : Integer.valueOf(offlineAttr);
        return Optional.of(new SmaxPsaResetSmbLastQpPrefetchTimestampResponse(id, from, type, timestampOpt.getAsLong(), offline));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxPsaResetSmbLastQpPrefetchTimestampResponse) obj;
        return this.timestampSeconds == that.timestampSeconds
                && Objects.equals(this.notificationId, that.notificationId)
                && Objects.equals(this.notificationFrom, that.notificationFrom)
                && Objects.equals(this.notificationType, that.notificationType)
                && Objects.equals(this.offline, that.offline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, notificationFrom, notificationType, timestampSeconds, offline);
    }

    @Override
    public String toString() {
        return "SmaxPsaResetSmbLastQpPrefetchTimestampResponse[notificationId=" + notificationId
                + ", notificationFrom=" + notificationFrom
                + ", notificationType=" + notificationType
                + ", timestampSeconds=" + timestampSeconds
                + ", offline=" + offline + ']';
    }
}
