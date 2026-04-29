package com.github.auties00.cobalt.node.smax.mdcompanion;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqErrorResponseMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the companion-finish payload
 * in the canonical {@code <iq xmlns="md" type="set" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMdCompanionFinishRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutMdBaseIQSetRequestMixin")
public final class SmaxMdCompanionFinishRequest implements SmaxOperation.Request {
    /**
     * The companion device's user JID.
     */
    private final Jid linkCodeCompanionRegJid;

    /**
     * The wrapped pairing key-bundle bytes
     * ({@code <link_code_pairing_wrapped_key_bundle/>}).
     */
    private final byte[] linkCodePairingWrappedKeyBundle;

    /**
     * The companion identity-public-key bytes
     * ({@code <companion_identity_public/>}).
     */
    private final byte[] companionIdentityPublic;

    /**
     * The pairing-reference bytes echoed from the
     * {@link SmaxMdCompanionHello} reply.
     */
    private final byte[] linkCodePairingRef;

    /**
     * Constructs a new companion-finish request.
     *
     * @param linkCodeCompanionRegJid          the companion JID; never {@code null}
     * @param linkCodePairingWrappedKeyBundle  the wrapped key-bundle bytes; never {@code null}
     * @param companionIdentityPublic          the identity-public-key bytes; never {@code null}
     * @param linkCodePairingRef               the pairing-reference bytes; never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxMdCompanionFinishRequest(Jid linkCodeCompanionRegJid,
                   byte[] linkCodePairingWrappedKeyBundle,
                   byte[] companionIdentityPublic,
                   byte[] linkCodePairingRef) {
        this.linkCodeCompanionRegJid = Objects.requireNonNull(linkCodeCompanionRegJid, "linkCodeCompanionRegJid cannot be null");
        this.linkCodePairingWrappedKeyBundle = Objects.requireNonNull(linkCodePairingWrappedKeyBundle, "linkCodePairingWrappedKeyBundle cannot be null");
        this.companionIdentityPublic = Objects.requireNonNull(companionIdentityPublic, "companionIdentityPublic cannot be null");
        this.linkCodePairingRef = Objects.requireNonNull(linkCodePairingRef, "linkCodePairingRef cannot be null");
    }

    /**
     * Returns the companion JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid linkCodeCompanionRegJid() {
        return linkCodeCompanionRegJid;
    }

    /**
     * Returns the wrapped key-bundle bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] linkCodePairingWrappedKeyBundle() {
        return linkCodePairingWrappedKeyBundle;
    }

    /**
     * Returns the identity-public-key bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] companionIdentityPublic() {
        return companionIdentityPublic;
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
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <link_code_companion_reg/>} payload
     *
     * @implNote {@code WASmaxOutMdCompanionFinishRequest.makeCompanionFinishRequest}
     *           emits {@code <iq xmlns="md" to="s.whatsapp.net"
     *           type="set" id=…>} with a single
     *           {@code <link_code_companion_reg jid stage="companion_finish">}
     *           payload nested over the wrapped key-bundle, identity
     *           pubkey and pairing-ref children.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutMdCompanionFinishRequest",
            exports = "makeCompanionFinishRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var wrappedKeyBundleNode = new NodeBuilder()
                .description("link_code_pairing_wrapped_key_bundle")
                .content(linkCodePairingWrappedKeyBundle)
                .build();
        var identityPublicNode = new NodeBuilder()
                .description("companion_identity_public")
                .content(companionIdentityPublic)
                .build();
        var pairingRefNode = new NodeBuilder()
                .description("link_code_pairing_ref")
                .content(linkCodePairingRef)
                .build();
        var regNode = new NodeBuilder()
                .description("link_code_companion_reg")
                .attribute("jid", linkCodeCompanionRegJid)
                .attribute("stage", "companion_finish")
                .content(wrappedKeyBundleNode, identityPublicNode, pairingRefNode)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "md")
                .attribute("to", "s.whatsapp.net")
                .attribute("type", "set")
                .content(regNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdCompanionFinishRequest) obj;
        return Objects.equals(this.linkCodeCompanionRegJid, that.linkCodeCompanionRegJid)
                && Arrays.equals(this.linkCodePairingWrappedKeyBundle, that.linkCodePairingWrappedKeyBundle)
                && Arrays.equals(this.companionIdentityPublic, that.companionIdentityPublic)
                && Arrays.equals(this.linkCodePairingRef, that.linkCodePairingRef);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(linkCodeCompanionRegJid);
        result = 31 * result + Arrays.hashCode(linkCodePairingWrappedKeyBundle);
        result = 31 * result + Arrays.hashCode(companionIdentityPublic);
        result = 31 * result + Arrays.hashCode(linkCodePairingRef);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxMdCompanionFinishRequest[linkCodeCompanionRegJid=" + linkCodeCompanionRegJid
                + ", linkCodePairingWrappedKeyBundle=" + Arrays.toString(linkCodePairingWrappedKeyBundle)
                + ", companionIdentityPublic=" + Arrays.toString(companionIdentityPublic)
                + ", linkCodePairingRef=" + Arrays.toString(linkCodePairingRef) + ']';
    }
}
