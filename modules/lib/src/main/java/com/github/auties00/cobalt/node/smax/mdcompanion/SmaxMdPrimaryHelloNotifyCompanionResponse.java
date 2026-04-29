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
 * {@code <notification type="link_code_companion_reg">} stanza.
 *
 * @implNote {@code WASmaxInMdPrimaryHelloNotifyCompanionRequest.parsePrimaryHelloNotifyCompanionRequest}
 *           validates the {@code <notification type="link_code_companion_reg"
 *           from="s.whatsapp.net">} envelope, asserts
 *           {@code stage="primary_hello"} on the
 *           {@code <link_code_companion_reg/>} child, and extracts
 *           three byte payloads:
 *           {@code <link_code_pairing_wrapped_primary_ephemeral_pub/>},
 *           {@code <primary_identity_pub/>}, and
 *           {@code <link_code_pairing_ref/>}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInMdPrimaryHelloNotifyCompanionRequest")
@WhatsAppWebModule(moduleName = "WASmaxInMdServerNotificationMixin")
public final class SmaxMdPrimaryHelloNotifyCompanionResponse implements SmaxOperation.Response {
    /**
     * The notification id (echoed back into the ack).
     */
    private final String notificationId;

    /**
     * The notification {@code from} JID (always the WA server domain).
     */
    private final Jid notificationFrom;

    /**
     * The wrapped primary ephemeral pubkey bytes.
     */
    private final byte[] linkCodePairingWrappedPrimaryEphemeralPub;

    /**
     * The primary identity pubkey bytes.
     */
    private final byte[] primaryIdentityPub;

    /**
     * The pairing-reference bytes echoed by the relay.
     */
    private final byte[] linkCodePairingRef;

    /**
     * Constructs a new {@code SmaxMdPrimaryHelloNotifyCompanionResponse} projection.
     *
     * @param notificationId                              the notification id; never {@code null}
     * @param notificationFrom                            the notification from JID; never {@code null}
     * @param linkCodePairingWrappedPrimaryEphemeralPub   the wrapped primary ephemeral pubkey; never {@code null}
     * @param primaryIdentityPub                          the primary identity pubkey; never {@code null}
     * @param linkCodePairingRef                          the pairing-reference bytes; never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxMdPrimaryHelloNotifyCompanionResponse(String notificationId,
                   Jid notificationFrom,
                   byte[] linkCodePairingWrappedPrimaryEphemeralPub,
                   byte[] primaryIdentityPub,
                   byte[] linkCodePairingRef) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.notificationFrom = Objects.requireNonNull(notificationFrom, "notificationFrom cannot be null");
        this.linkCodePairingWrappedPrimaryEphemeralPub = Objects.requireNonNull(linkCodePairingWrappedPrimaryEphemeralPub, "linkCodePairingWrappedPrimaryEphemeralPub cannot be null");
        this.primaryIdentityPub = Objects.requireNonNull(primaryIdentityPub, "primaryIdentityPub cannot be null");
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
     * Returns the wrapped primary ephemeral pubkey bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] linkCodePairingWrappedPrimaryEphemeralPub() {
        return linkCodePairingWrappedPrimaryEphemeralPub;
    }

    /**
     * Returns the primary identity pubkey bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] primaryIdentityPub() {
        return primaryIdentityPub;
    }

    /**
     * Returns the pairing-reference bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] linkCodePairingRef() {
        return linkCodePairingRef;
    }

    /**
     * Tries to parse an {@link SmaxMdPrimaryHelloNotifyCompanionResponse} projection.
     *
     * @param node the inbound notification stanza; never {@code null}
     * @return an {@link Optional} carrying the projection
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInMdPrimaryHelloNotifyCompanionRequest",
            exports = "parsePrimaryHelloNotifyCompanionRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxMdPrimaryHelloNotifyCompanionResponse> of(Node node) {
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
        if (reg == null || !reg.hasAttribute("stage", "primary_hello")) {
            return Optional.empty();
        }
        var wrappedEphemeral = reg.getChild("link_code_pairing_wrapped_primary_ephemeral_pub")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (wrappedEphemeral == null) {
            return Optional.empty();
        }
        var identityPub = reg.getChild("primary_identity_pub")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (identityPub == null) {
            return Optional.empty();
        }
        var pairingRef = reg.getChild("link_code_pairing_ref")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (pairingRef == null) {
            return Optional.empty();
        }
        return Optional.of(new SmaxMdPrimaryHelloNotifyCompanionResponse(id, from, wrappedEphemeral, identityPub, pairingRef));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdPrimaryHelloNotifyCompanionResponse) obj;
        return Objects.equals(this.notificationId, that.notificationId)
                && Objects.equals(this.notificationFrom, that.notificationFrom)
                && Arrays.equals(this.linkCodePairingWrappedPrimaryEphemeralPub, that.linkCodePairingWrappedPrimaryEphemeralPub)
                && Arrays.equals(this.primaryIdentityPub, that.primaryIdentityPub)
                && Arrays.equals(this.linkCodePairingRef, that.linkCodePairingRef);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(notificationId, notificationFrom);
        result = 31 * result + Arrays.hashCode(linkCodePairingWrappedPrimaryEphemeralPub);
        result = 31 * result + Arrays.hashCode(primaryIdentityPub);
        result = 31 * result + Arrays.hashCode(linkCodePairingRef);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxMdPrimaryHelloNotifyCompanionResponse[notificationId=" + notificationId
                + ", notificationFrom=" + notificationFrom
                + ", linkCodePairingWrappedPrimaryEphemeralPub=" + Arrays.toString(linkCodePairingWrappedPrimaryEphemeralPub)
                + ", primaryIdentityPub=" + Arrays.toString(primaryIdentityPub)
                + ", linkCodePairingRef=" + Arrays.toString(linkCodePairingRef) + ']';
    }
}
