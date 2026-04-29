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
 * The regular (non-hosted) companion's pair-success reply, carrying
 * the signed device-identity bundle plus the optional key-attestation
 * and gpia material.
 *
 * @implNote {@code WASmaxOutMdRegularCompanionSetRegResponseBundleMixin.mergeRegularCompanionSetRegResponseBundleMixin}
 *           wraps {@code <iq id to="s.whatsapp.net" type="result">}
 *           around {@code <pair-device-sign>} →
 *           {@code <device-identity key-index=…/>} plus optional
 *           {@code <key_attestation key_id?/>} and {@code <gpia/>}
 *           children.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMdSetRegResponseClientResponse")
@WhatsAppWebModule(moduleName = "WASmaxOutMdRegularCompanionSetRegResponseBundleMixin")
public final class SmaxMdSetRegResponseClient implements SmaxOperation.Request {
    /**
     * The id of the inbound IQ being replied to.
     */
    private final String iqId;

    /**
     * The {@code key-index} attribute on {@code <device-identity/>}.
     */
    private final int deviceIdentityKeyIndex;

    /**
     * The signed device-identity content bytes.
     */
    private final byte[] deviceIdentity;

    /**
     * The optional key-attestation content bytes
     * ({@code <key_attestation/>}).
     */
    private final byte[] keyAttestation;

    /**
     * The optional key-attestation {@code key_id} attribute.
     */
    private final String keyAttestationKeyId;

    /**
     * The optional gpia content bytes ({@code <gpia/>}).
     */
    private final byte[] gpia;

    /**
     * Constructs a new regular pair-success reply.
     *
     * @param iqId                   the IQ id; never {@code null}
     * @param deviceIdentityKeyIndex the device-identity key-index
     * @param deviceIdentity         the signed device-identity bytes; never {@code null}
     * @param keyAttestation         the optional key-attestation bytes; may be {@code null}
     * @param keyAttestationKeyId    the optional key_id attribute; may be {@code null}
     * @param gpia                   the optional gpia bytes; may be {@code null}
     * @throws NullPointerException if {@code iqId} or {@code deviceIdentity} is {@code null}
     */
    public SmaxMdSetRegResponseClient(String iqId, int deviceIdentityKeyIndex, byte[] deviceIdentity,
                          byte[] keyAttestation, String keyAttestationKeyId, byte[] gpia) {
        this.iqId = Objects.requireNonNull(iqId, "iqId cannot be null");
        this.deviceIdentityKeyIndex = deviceIdentityKeyIndex;
        this.deviceIdentity = Objects.requireNonNull(deviceIdentity, "deviceIdentity cannot be null");
        this.keyAttestation = keyAttestation;
        this.keyAttestationKeyId = keyAttestationKeyId;
        this.gpia = gpia;
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
     * Returns the device-identity key-index.
     *
     * @return the key-index
     */
    public int deviceIdentityKeyIndex() {
        return deviceIdentityKeyIndex;
    }

    /**
     * Returns the signed device-identity bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] deviceIdentity() {
        return deviceIdentity;
    }

    /**
     * Returns the optional key-attestation bytes.
     *
     * @return an {@link Optional} carrying the bytes
     */
    public Optional<byte[]> keyAttestation() {
        return Optional.ofNullable(keyAttestation);
    }

    /**
     * Returns the optional key-attestation key_id.
     *
     * @return an {@link Optional} carrying the value
     */
    public Optional<String> keyAttestationKeyId() {
        return Optional.ofNullable(keyAttestationKeyId);
    }

    /**
     * Returns the optional gpia bytes.
     *
     * @return an {@link Optional} carrying the bytes
     */
    public Optional<byte[]> gpia() {
        return Optional.ofNullable(gpia);
    }

    /**
     * Builds the outbound regular pair-success reply stanza.
     *
     * @return a {@link NodeBuilder} carrying the reply envelope
     *
     * @implNote {@code WASmaxOutMdSetRegResponseClientResponse.makeSetRegResponseClientResponse}
     *           composes
     *           {@code mergeRegularCompanionSetRegResponseBundleMixin}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutMdSetRegResponseClientResponse",
            exports = "makeSetRegResponseClientResponse",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var deviceIdentityNode = new NodeBuilder()
                .description("device-identity")
                .attribute("key-index", deviceIdentityKeyIndex)
                .content(deviceIdentity)
                .build();
        var pairDeviceSignBuilder = new NodeBuilder()
                .description("pair-device-sign");
        if (keyAttestation != null) {
            var keyAttestationBuilder = new NodeBuilder()
                    .description("key_attestation")
                    .content(keyAttestation);
            if (keyAttestationKeyId != null) {
                keyAttestationBuilder.attribute("key_id", keyAttestationKeyId);
            }
            if (gpia != null) {
                var gpiaNode = new NodeBuilder()
                        .description("gpia")
                        .content(gpia)
                        .build();
                pairDeviceSignBuilder.content(deviceIdentityNode, keyAttestationBuilder.build(), gpiaNode);
            } else {
                pairDeviceSignBuilder.content(deviceIdentityNode, keyAttestationBuilder.build());
            }
        } else if (gpia != null) {
            var gpiaNode = new NodeBuilder()
                    .description("gpia")
                    .content(gpia)
                    .build();
            pairDeviceSignBuilder.content(deviceIdentityNode, gpiaNode);
        } else {
            pairDeviceSignBuilder.content(deviceIdentityNode);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("id", iqId)
                .attribute("to", "s.whatsapp.net")
                .attribute("type", "result")
                .content(pairDeviceSignBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdSetRegResponseClient) obj;
        return this.deviceIdentityKeyIndex == that.deviceIdentityKeyIndex
                && Objects.equals(this.iqId, that.iqId)
                && Arrays.equals(this.deviceIdentity, that.deviceIdentity)
                && Arrays.equals(this.keyAttestation, that.keyAttestation)
                && Objects.equals(this.keyAttestationKeyId, that.keyAttestationKeyId)
                && Arrays.equals(this.gpia, that.gpia);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(iqId, deviceIdentityKeyIndex, keyAttestationKeyId);
        result = 31 * result + Arrays.hashCode(deviceIdentity);
        result = 31 * result + Arrays.hashCode(keyAttestation);
        result = 31 * result + Arrays.hashCode(gpia);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxMdSetRegResponseClient[iqId=" + iqId
                + ", deviceIdentityKeyIndex=" + deviceIdentityKeyIndex
                + ", deviceIdentity=" + Arrays.toString(deviceIdentity)
                + ", keyAttestation=" + Arrays.toString(keyAttestation)
                + ", keyAttestationKeyId=" + keyAttestationKeyId
                + ", gpia=" + Arrays.toString(gpia) + ']';
    }
}
