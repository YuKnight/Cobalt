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
 * The inbound projection of the {@code <iq><pair-success/></iq>}
 * stanza.
 */
@WhatsAppWebModule(moduleName = "WASmaxInMdSetRegRequest")
public final class SmaxMdSetRegResponse implements SmaxOperation.Response {
    /**
     * The {@code id} attribute of the inbound IQ stanza.
     */
    private final String iqId;

    /**
     * The device-identity content bytes
     * ({@code <device-identity/>}).
     */
    private final byte[] pairSuccessDeviceIdentity;

    /**
     * The assigned device JID
     * ({@code <device jid=…/>}).
     */
    private final Jid pairSuccessDeviceJid;

    /**
     * The optional assigned device lid
     * ({@code <device lid=…/>}).
     */
    private final Jid pairSuccessDeviceLid;

    /**
     * The optional {@code beta} attribute on {@code <device/>}
     * ({@code "true"} or {@code "false"}).
     */
    private final String pairSuccessDeviceBeta;

    /**
     * The platform name
     * ({@code <platform name=…/>}).
     */
    private final String pairSuccessPlatformName;

    /**
     * The optional business-account display name
     * ({@code <biz name=…/>}).
     */
    private final String pairSuccessBizName;

    /**
     * The optional client-properties content bytes
     * ({@code <client-props/>}).
     */
    private final byte[] pairSuccessClientProps;

    /**
     * The optional encryption-metadata projection
     * ({@code <encryption-metadata/>}).
     */
    private final SmaxMdSetRegEncryptionMetadata pairSuccessEncryptionMetadata;

    /**
     * Constructs a new {@code SmaxMdSetRegResponse} projection.
     *
     * @param iqId                          the IQ id; never {@code null}
     * @param pairSuccessDeviceIdentity     the device-identity bytes; never {@code null}
     * @param pairSuccessDeviceJid          the device JID; never {@code null}
     * @param pairSuccessDeviceLid          the optional device lid; may be {@code null}
     * @param pairSuccessDeviceBeta         the optional beta flag; may be {@code null}
     * @param pairSuccessPlatformName       the platform name; never {@code null}
     * @param pairSuccessBizName            the optional biz name; may be {@code null}
     * @param pairSuccessClientProps        the optional client-props bytes; may be {@code null}
     * @param pairSuccessEncryptionMetadata the optional encryption metadata; may be {@code null}
     * @throws NullPointerException if any of the required arguments is {@code null}
     */
    public SmaxMdSetRegResponse(String iqId,
                   byte[] pairSuccessDeviceIdentity,
                   Jid pairSuccessDeviceJid,
                   Jid pairSuccessDeviceLid,
                   String pairSuccessDeviceBeta,
                   String pairSuccessPlatformName,
                   String pairSuccessBizName,
                   byte[] pairSuccessClientProps,
                   SmaxMdSetRegEncryptionMetadata pairSuccessEncryptionMetadata) {
        this.iqId = Objects.requireNonNull(iqId, "iqId cannot be null");
        this.pairSuccessDeviceIdentity = Objects.requireNonNull(pairSuccessDeviceIdentity, "pairSuccessDeviceIdentity cannot be null");
        this.pairSuccessDeviceJid = Objects.requireNonNull(pairSuccessDeviceJid, "pairSuccessDeviceJid cannot be null");
        this.pairSuccessDeviceLid = pairSuccessDeviceLid;
        this.pairSuccessDeviceBeta = pairSuccessDeviceBeta;
        this.pairSuccessPlatformName = Objects.requireNonNull(pairSuccessPlatformName, "pairSuccessPlatformName cannot be null");
        this.pairSuccessBizName = pairSuccessBizName;
        this.pairSuccessClientProps = pairSuccessClientProps;
        this.pairSuccessEncryptionMetadata = pairSuccessEncryptionMetadata;
    }

    /**
     * Returns the IQ id.
     *
     * @return the id; never {@code null}
     */
    public String iqId() {
        return iqId;
    }

    /**
     * Returns the device-identity bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] pairSuccessDeviceIdentity() {
        return pairSuccessDeviceIdentity;
    }

    /**
     * Returns the assigned device JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid pairSuccessDeviceJid() {
        return pairSuccessDeviceJid;
    }

    /**
     * Returns the optional device lid.
     *
     * @return an {@link Optional} carrying the lid, or empty when
     *         the relay omitted it
     */
    public Optional<Jid> pairSuccessDeviceLid() {
        return Optional.ofNullable(pairSuccessDeviceLid);
    }

    /**
     * Returns the optional beta flag.
     *
     * @return an {@link Optional} carrying the value, or empty when
     *         the relay omitted it
     */
    public Optional<String> pairSuccessDeviceBeta() {
        return Optional.ofNullable(pairSuccessDeviceBeta);
    }

    /**
     * Returns the platform name.
     *
     * @return the name; never {@code null}
     */
    public String pairSuccessPlatformName() {
        return pairSuccessPlatformName;
    }

    /**
     * Returns the optional biz name.
     *
     * @return an {@link Optional} carrying the value, or empty
     */
    public Optional<String> pairSuccessBizName() {
        return Optional.ofNullable(pairSuccessBizName);
    }

    /**
     * Returns the optional client-props bytes.
     *
     * @return an {@link Optional} carrying the bytes, or empty
     */
    public Optional<byte[]> pairSuccessClientProps() {
        return Optional.ofNullable(pairSuccessClientProps);
    }

    /**
     * Returns the optional encryption-metadata projection.
     *
     * @return an {@link Optional} carrying the projection, or empty
     */
    public Optional<SmaxMdSetRegEncryptionMetadata> pairSuccessEncryptionMetadata() {
        return Optional.ofNullable(pairSuccessEncryptionMetadata);
    }

    /**
     * Tries to parse an {@link SmaxMdSetRegResponse} projection.
     *
     * @param node the inbound IQ stanza; never {@code null}
     * @return an {@link Optional} carrying the projection, or empty
     *         when the stanza does not match the documented shape
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInMdSetRegRequest",
            exports = "parseSetRegRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxMdSetRegResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("iq")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("xmlns", "md")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("type", "set")) {
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
        var pairSuccess = node.getChild("pair-success").orElse(null);
        if (pairSuccess == null) {
            return Optional.empty();
        }
        var deviceIdentity = pairSuccess.getChild("device-identity")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (deviceIdentity == null) {
            return Optional.empty();
        }
        var deviceChild = pairSuccess.getChild("device").orElse(null);
        if (deviceChild == null) {
            return Optional.empty();
        }
        var deviceJid = deviceChild.getAttributeAsJid("jid").orElse(null);
        if (deviceJid == null) {
            return Optional.empty();
        }
        var deviceLid = deviceChild.getAttributeAsJid("lid").orElse(null);
        var deviceBeta = deviceChild.getAttributeAsString("beta").orElse(null);
        if (deviceBeta != null && !"true".equals(deviceBeta) && !"false".equals(deviceBeta)) {
            return Optional.empty();
        }
        var platformChild = pairSuccess.getChild("platform").orElse(null);
        if (platformChild == null) {
            return Optional.empty();
        }
        var platformName = platformChild.getAttributeAsString("name").orElse(null);
        if (platformName == null) {
            return Optional.empty();
        }
        var bizName = pairSuccess.getChild("biz")
                .flatMap(biz -> biz.getAttributeAsString("name"))
                .orElse(null);
        var clientProps = pairSuccess.getChild("client-props")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        var encryptionMetadata = pairSuccess.getChild("encryption-metadata")
                .flatMap(SmaxMdSetRegEncryptionMetadata::of)
                .orElse(null);
        return Optional.of(new SmaxMdSetRegResponse(id, deviceIdentity, deviceJid, deviceLid, deviceBeta,
                platformName, bizName, clientProps, encryptionMetadata));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdSetRegResponse) obj;
        return Objects.equals(this.iqId, that.iqId)
                && Arrays.equals(this.pairSuccessDeviceIdentity, that.pairSuccessDeviceIdentity)
                && Objects.equals(this.pairSuccessDeviceJid, that.pairSuccessDeviceJid)
                && Objects.equals(this.pairSuccessDeviceLid, that.pairSuccessDeviceLid)
                && Objects.equals(this.pairSuccessDeviceBeta, that.pairSuccessDeviceBeta)
                && Objects.equals(this.pairSuccessPlatformName, that.pairSuccessPlatformName)
                && Objects.equals(this.pairSuccessBizName, that.pairSuccessBizName)
                && Arrays.equals(this.pairSuccessClientProps, that.pairSuccessClientProps)
                && Objects.equals(this.pairSuccessEncryptionMetadata, that.pairSuccessEncryptionMetadata);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(iqId, pairSuccessDeviceJid, pairSuccessDeviceLid,
                pairSuccessDeviceBeta, pairSuccessPlatformName, pairSuccessBizName,
                pairSuccessEncryptionMetadata);
        result = 31 * result + Arrays.hashCode(pairSuccessDeviceIdentity);
        result = 31 * result + Arrays.hashCode(pairSuccessClientProps);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxMdSetRegResponse[iqId=" + iqId
                + ", pairSuccessDeviceIdentity=" + Arrays.toString(pairSuccessDeviceIdentity)
                + ", pairSuccessDeviceJid=" + pairSuccessDeviceJid
                + ", pairSuccessDeviceLid=" + pairSuccessDeviceLid
                + ", pairSuccessDeviceBeta=" + pairSuccessDeviceBeta
                + ", pairSuccessPlatformName=" + pairSuccessPlatformName
                + ", pairSuccessBizName=" + pairSuccessBizName
                + ", pairSuccessClientProps=" + Arrays.toString(pairSuccessClientProps)
                + ", pairSuccessEncryptionMetadata=" + pairSuccessEncryptionMetadata + ']';
    }
}
