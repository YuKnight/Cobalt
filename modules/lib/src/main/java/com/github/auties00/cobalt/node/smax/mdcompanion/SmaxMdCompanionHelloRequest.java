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
 * The outbound stanza variant. Wraps the companion-hello payload in
 * the canonical {@code <iq xmlns="md" type="set" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMdCompanionHelloRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutMdBaseIQSetRequestMixin")
public final class SmaxMdCompanionHelloRequest implements SmaxOperation.Request {
    /**
     * The companion device's user JID. Routed verbatim into
     * {@code <link_code_companion_reg jid=…>}.
     */
    private final Jid linkCodeCompanionRegJid;

    /**
     * Optional flag controlling whether the primary device should
     * surface a push notification when the pairing reference arrives.
     */
    private final String linkCodeCompanionRegShouldShowPushNotification;

    /**
     * The wrapped companion-side ephemeral public key bytes
     * ({@code <link_code_pairing_wrapped_companion_ephemeral_pub/>}).
     */
    private final byte[] linkCodePairingWrappedCompanionEphemeralPub;

    /**
     * The companion's server-auth public-key bytes
     * ({@code <companion_server_auth_key_pub/>}).
     */
    private final byte[] companionServerAuthKeyPub;

    /**
     * The companion-platform identifier bytes
     * ({@code <companion_platform_id/>}).
     */
    private final byte[] companionPlatformId;

    /**
     * The companion-platform display-name bytes
     * ({@code <companion_platform_display/>}).
     */
    private final byte[] companionPlatformDisplay;

    /**
     * Optional pairing-nonce element bytes
     * ({@code <link_code_pairing_nonce/>}); may be {@code null} when
     * the caller does not advertise a nonce.
     */
    private final byte[] linkCodePairingNonce;

    /**
     * Constructs a new companion-hello request.
     *
     * @param linkCodeCompanionRegJid                       the companion JID; never {@code null}
     * @param linkCodeCompanionRegShouldShowPushNotification the optional push-notification flag; may be {@code null}
     * @param linkCodePairingWrappedCompanionEphemeralPub   the wrapped ephemeral pubkey; never {@code null}
     * @param companionServerAuthKeyPub                     the server-auth pubkey; never {@code null}
     * @param companionPlatformId                           the platform-id bytes; never {@code null}
     * @param companionPlatformDisplay                      the platform-display bytes; never {@code null}
     * @param linkCodePairingNonce                          the optional pairing nonce bytes; may be {@code null}
     * @throws NullPointerException if any of the required arguments is {@code null}
     */
    public SmaxMdCompanionHelloRequest(Jid linkCodeCompanionRegJid,
                   String linkCodeCompanionRegShouldShowPushNotification,
                   byte[] linkCodePairingWrappedCompanionEphemeralPub,
                   byte[] companionServerAuthKeyPub,
                   byte[] companionPlatformId,
                   byte[] companionPlatformDisplay,
                   byte[] linkCodePairingNonce) {
        this.linkCodeCompanionRegJid = Objects.requireNonNull(linkCodeCompanionRegJid, "linkCodeCompanionRegJid cannot be null");
        this.linkCodeCompanionRegShouldShowPushNotification = linkCodeCompanionRegShouldShowPushNotification;
        this.linkCodePairingWrappedCompanionEphemeralPub = Objects.requireNonNull(linkCodePairingWrappedCompanionEphemeralPub, "linkCodePairingWrappedCompanionEphemeralPub cannot be null");
        this.companionServerAuthKeyPub = Objects.requireNonNull(companionServerAuthKeyPub, "companionServerAuthKeyPub cannot be null");
        this.companionPlatformId = Objects.requireNonNull(companionPlatformId, "companionPlatformId cannot be null");
        this.companionPlatformDisplay = Objects.requireNonNull(companionPlatformDisplay, "companionPlatformDisplay cannot be null");
        this.linkCodePairingNonce = linkCodePairingNonce;
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
     * Returns the optional push-notification flag.
     *
     * @return an {@link Optional} carrying the flag, or empty when
     *         the caller did not advertise it
     */
    public Optional<String> linkCodeCompanionRegShouldShowPushNotification() {
        return Optional.ofNullable(linkCodeCompanionRegShouldShowPushNotification);
    }

    /**
     * Returns the wrapped companion ephemeral pubkey bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] linkCodePairingWrappedCompanionEphemeralPub() {
        return linkCodePairingWrappedCompanionEphemeralPub;
    }

    /**
     * Returns the server-auth pubkey bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] companionServerAuthKeyPub() {
        return companionServerAuthKeyPub;
    }

    /**
     * Returns the companion-platform-id bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] companionPlatformId() {
        return companionPlatformId;
    }

    /**
     * Returns the companion-platform-display bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] companionPlatformDisplay() {
        return companionPlatformDisplay;
    }

    /**
     * Returns the optional pairing-nonce bytes.
     *
     * @return an {@link Optional} carrying the bytes, or empty when
     *         the caller did not advertise a nonce
     */
    public Optional<byte[]> linkCodePairingNonce() {
        return Optional.ofNullable(linkCodePairingNonce);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <link_code_companion_reg/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutMdCompanionHelloRequest",
            exports = "makeCompanionHelloRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var wrappedEphemeralNode = new NodeBuilder()
                .description("link_code_pairing_wrapped_companion_ephemeral_pub")
                .content(linkCodePairingWrappedCompanionEphemeralPub)
                .build();
        var serverAuthKeyNode = new NodeBuilder()
                .description("companion_server_auth_key_pub")
                .content(companionServerAuthKeyPub)
                .build();
        var platformIdNode = new NodeBuilder()
                .description("companion_platform_id")
                .content(companionPlatformId)
                .build();
        var platformDisplayNode = new NodeBuilder()
                .description("companion_platform_display")
                .content(companionPlatformDisplay)
                .build();
        var regBuilder = new NodeBuilder()
                .description("link_code_companion_reg")
                .attribute("jid", linkCodeCompanionRegJid)
                .attribute("stage", "companion_hello");
        if (linkCodeCompanionRegShouldShowPushNotification != null) {
            regBuilder.attribute("should_show_push_notification", linkCodeCompanionRegShouldShowPushNotification);
        }
        if (linkCodePairingNonce != null) {
            var nonceNode = new NodeBuilder()
                    .description("link_code_pairing_nonce")
                    .content(linkCodePairingNonce)
                    .build();
            regBuilder.content(wrappedEphemeralNode, serverAuthKeyNode, platformIdNode, platformDisplayNode, nonceNode);
        } else {
            regBuilder.content(wrappedEphemeralNode, serverAuthKeyNode, platformIdNode, platformDisplayNode);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "md")
                .attribute("to", "s.whatsapp.net")
                .attribute("type", "set")
                .content(regBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdCompanionHelloRequest) obj;
        return Objects.equals(this.linkCodeCompanionRegJid, that.linkCodeCompanionRegJid)
                && Objects.equals(this.linkCodeCompanionRegShouldShowPushNotification, that.linkCodeCompanionRegShouldShowPushNotification)
                && Arrays.equals(this.linkCodePairingWrappedCompanionEphemeralPub, that.linkCodePairingWrappedCompanionEphemeralPub)
                && Arrays.equals(this.companionServerAuthKeyPub, that.companionServerAuthKeyPub)
                && Arrays.equals(this.companionPlatformId, that.companionPlatformId)
                && Arrays.equals(this.companionPlatformDisplay, that.companionPlatformDisplay)
                && Arrays.equals(this.linkCodePairingNonce, that.linkCodePairingNonce);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(linkCodeCompanionRegJid, linkCodeCompanionRegShouldShowPushNotification);
        result = 31 * result + Arrays.hashCode(linkCodePairingWrappedCompanionEphemeralPub);
        result = 31 * result + Arrays.hashCode(companionServerAuthKeyPub);
        result = 31 * result + Arrays.hashCode(companionPlatformId);
        result = 31 * result + Arrays.hashCode(companionPlatformDisplay);
        result = 31 * result + Arrays.hashCode(linkCodePairingNonce);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxMdCompanionHelloRequest[linkCodeCompanionRegJid=" + linkCodeCompanionRegJid
                + ", linkCodeCompanionRegShouldShowPushNotification=" + linkCodeCompanionRegShouldShowPushNotification
                + ", linkCodePairingWrappedCompanionEphemeralPub=" + Arrays.toString(linkCodePairingWrappedCompanionEphemeralPub)
                + ", companionServerAuthKeyPub=" + Arrays.toString(companionServerAuthKeyPub)
                + ", companionPlatformId=" + Arrays.toString(companionPlatformId)
                + ", companionPlatformDisplay=" + Arrays.toString(companionPlatformDisplay)
                + ", linkCodePairingNonce=" + Arrays.toString(linkCodePairingNonce) + ']';
    }
}
