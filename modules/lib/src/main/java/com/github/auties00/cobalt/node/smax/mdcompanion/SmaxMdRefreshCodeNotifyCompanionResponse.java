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
 * The inbound projection of the
 * {@code <notification type="link_code_companion_reg"
 * stage="refresh_code">} stanza.
 *
 * @implNote {@code WASmaxInMdRefreshCodeNotifyCompanionRequest.parseRefreshCodeNotifyCompanionRequest}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInMdRefreshCodeNotifyCompanionRequest")
@WhatsAppWebModule(moduleName = "WASmaxInMdServerNotificationMixin")
public final class SmaxMdRefreshCodeNotifyCompanionResponse implements SmaxOperation.Response {
    /**
     * The notification id (echoed back into the ack).
     */
    private final String notificationId;

    /**
     * The notification {@code from} JID (always the WA server domain).
     */
    private final Jid notificationFrom;

    /**
     * Optional {@code force_manual_refresh} flag . {@code "true"}
     * when the rotation was manually requested by the primary device,
     * {@code "false"} (or absent) when it is part of the natural
     * cadence.
     */
    private final String linkCodeCompanionRegForceManualRefresh;

    /**
     * The new pairing-reference bytes echoed by the relay.
     */
    private final byte[] linkCodePairingRef;

    /**
     * Constructs a new {@code SmaxMdRefreshCodeNotifyCompanionResponse} projection.
     *
     * @param notificationId                         the notification id; never {@code null}
     * @param notificationFrom                       the notification from JID; never {@code null}
     * @param linkCodeCompanionRegForceManualRefresh the optional force-manual-refresh flag; may be {@code null}
     * @param linkCodePairingRef                     the new pairing-reference bytes; never {@code null}
     * @throws NullPointerException if any of the required arguments is {@code null}
     */
    public SmaxMdRefreshCodeNotifyCompanionResponse(String notificationId,
                   Jid notificationFrom,
                   String linkCodeCompanionRegForceManualRefresh,
                   byte[] linkCodePairingRef) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
        this.linkCodeCompanionRegForceManualRefresh = linkCodeCompanionRegForceManualRefresh;
        this.linkCodePairingRef = Objects.requireNonNull(linkCodePairingRef, "linkCodePairingRef cannot be null");
    }

    /**
     * Returns the notification id.
     *
     * @return the id; never {@code null}
     */
    public String notificationId() {
        return notificationId;
    }

    /**
     * Returns the notification from JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid notificationFrom() {
        return notificationFrom;
    }

    /**
     * Returns the optional force-manual-refresh flag.
     *
     * @return an {@link Optional} carrying the value, or empty when
     *         the relay omitted it
     */
    public Optional<String> linkCodeCompanionRegForceManualRefresh() {
        return Optional.ofNullable(linkCodeCompanionRegForceManualRefresh);
    }

    /**
     * Returns the new pairing-reference bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] linkCodePairingRef() {
        return linkCodePairingRef;
    }

    /**
     * Tries to parse an {@link SmaxMdRefreshCodeNotifyCompanionResponse} projection.
     *
     * @param node the inbound notification stanza; never {@code null}
     * @return an {@link Optional} carrying the projection
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInMdRefreshCodeNotifyCompanionRequest",
            exports = "parseRefreshCodeNotifyCompanionRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxMdRefreshCodeNotifyCompanionResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("notification")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("type", "link_code_companion_reg")) {
            return Optional.empty();
        }
        var from = node.getAttributeAsJid("from").orElse(null);
        if (from == null || !"s.whatsapp.net".equals(from.server().toString())) {
            return Optional.empty();
        }
        var id = node.getAttributeAsString("id").orElse(null);
        if (id == null) {
            return Optional.empty();
        }
        var reg = node.getChild("link_code_companion_reg").orElse(null);
        if (reg == null || !reg.hasAttribute("stage", "refresh_code")) {
            return Optional.empty();
        }
        var forceManualRefresh = reg.getAttributeAsString("force_manual_refresh").orElse(null);
        if (forceManualRefresh != null && !"true".equals(forceManualRefresh) && !"false".equals(forceManualRefresh)) {
            return Optional.empty();
        }
        var pairingRef = reg.getChild("link_code_pairing_ref")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (pairingRef == null) {
            return Optional.empty();
        }
        return Optional.of(new SmaxMdRefreshCodeNotifyCompanionResponse(id, from, forceManualRefresh, pairingRef));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdRefreshCodeNotifyCompanionResponse) obj;
        return Objects.equals(this.notificationId, that.notificationId)
                && Objects.equals(this.notificationFrom, that.notificationFrom)
                && Objects.equals(this.linkCodeCompanionRegForceManualRefresh, that.linkCodeCompanionRegForceManualRefresh)
                && Arrays.equals(this.linkCodePairingRef, that.linkCodePairingRef);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(notificationId, notificationFrom, linkCodeCompanionRegForceManualRefresh);
        result = 31 * result + Arrays.hashCode(linkCodePairingRef);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxMdRefreshCodeNotifyCompanionResponse[notificationId=" + notificationId
                + ", notificationFrom=" + notificationFrom
                + ", linkCodeCompanionRegForceManualRefresh=" + linkCodeCompanionRegForceManualRefresh
                + ", linkCodePairingRef=" + Arrays.toString(linkCodePairingRef) + ']';
    }
}
