package com.github.auties00.cobalt.node.iq.encrypt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.util.DataUtils;
import com.github.auties00.cobalt.util.RandomIdUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The outbound stanza variant — wraps the registration id,
 * identity key, one-time pre-key list and signed pre-key in the
 * canonical {@code <iq xmlns="encrypt" type="set"
 * to="s.whatsapp.net">} envelope.
 *
 * <p>Legacy IQ RPC: uploads the local user's Signal pre-key bundle to the
 * relay so that incoming Signal sessions established by remote senders
 * can locate a fresh one-time pre-key for the local device.
 *
 * <p>Issues {@code <iq xmlns="encrypt" type="set" to="s.whatsapp.net">}
 * carrying a flat list of children: {@code <registration/>} (the local
 * device's registration id, big-endian, four bytes), {@code <type/>}
 * (the single-byte Signal key-bundle type marker), {@code <identity/>}
 * (the local long-term identity public key, thirty-two bytes),
 * {@code <list/>} (a wrapper containing one {@code <key/>} per uploaded
 * one-time pre-key, each carrying a three-byte big-endian
 * {@code <id/>} and a thirty-two-byte {@code <value/>}), and
 * {@code <skey/>} (the current signed pre-key — a three-byte
 * {@code <id/>}, a thirty-two-byte {@code <value/>}, and a sixty-four
 * byte {@code <signature/>}).
 */
@WhatsAppWebModule(moduleName = "WAWebUploadPreKeysJob")
public final class IqUploadPreKeysRequest implements IqOperation.Request {
    /**
     * The local device's registration id — encoded as a four-byte
     * big-endian unsigned integer in the {@code <registration/>}
     * child.
     */
    private final int registrationId;

    /**
     * The single-byte Signal key-bundle type marker carried by
     * the {@code <type/>} child.
     */
    private final byte keyBundleType;

    /**
     * The local long-term identity public key — thirty-two bytes
     * carried verbatim by the {@code <identity/>} child.
     */
    private final byte[] identityPublicKey;

    /**
     * The list of one-time pre-keys carried by the
     * {@code <list/>} wrapper.
     */
    private final List<IqUploadPreKeysPreKey> preKeys;

    /**
     * The current signed pre-key carried by the {@code <skey/>}
     * child.
     */
    private final IqUploadPreKeysSignedPreKey signedPreKey;

    /**
     * Constructs a request for the supplied bundle.
     *
     * @param registrationId    the local device's registration id;
     *                          must be non-negative
     * @param keyBundleType     the Signal key-bundle type marker
     * @param identityPublicKey the local identity public key bytes;
     *                          never {@code null}
     * @param preKeys           the one-time pre-keys to upload;
     *                          never {@code null} and never empty
     * @param signedPreKey      the current signed pre-key; never
     *                          {@code null}
     * @throws NullPointerException     if any reference argument is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code preKeys} is empty
     */
    public IqUploadPreKeysRequest(int registrationId, byte keyBundleType, byte[] identityPublicKey,
                                  List<IqUploadPreKeysPreKey> preKeys, IqUploadPreKeysSignedPreKey signedPreKey) {
        Objects.requireNonNull(identityPublicKey, "identityPublicKey cannot be null");
        Objects.requireNonNull(preKeys, "preKeys cannot be null");
        Objects.requireNonNull(signedPreKey, "signedPreKey cannot be null");
        if (preKeys.isEmpty()) {
            throw new IllegalArgumentException("preKeys cannot be empty");
        }
        this.registrationId = registrationId;
        this.keyBundleType = keyBundleType;
        this.identityPublicKey = identityPublicKey;
        this.preKeys = List.copyOf(preKeys);
        this.signedPreKey = signedPreKey;
    }

    /**
     * Returns the registration id carried by this request.
     *
     * @return the registration id
     */
    public int registrationId() {
        return registrationId;
    }

    /**
     * Returns the Signal key-bundle type marker.
     *
     * @return the type marker
     */
    public byte keyBundleType() {
        return keyBundleType;
    }

    /**
     * Returns the identity public key bytes.
     *
     * @return the thirty-two-byte identity public key; never
     *         {@code null}
     */
    public byte[] identityPublicKey() {
        return identityPublicKey;
    }

    /**
     * Returns the list of one-time pre-keys carried by this
     * request.
     *
     * @return an unmodifiable list of pre-keys; never {@code null}
     */
    public List<IqUploadPreKeysPreKey> preKeys() {
        return preKeys;
    }

    /**
     * Returns the signed pre-key carried by this request.
     *
     * @return the signed pre-key; never {@code null}
     */
    public IqUploadPreKeysSignedPreKey signedPreKey() {
        return signedPreKey;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the pre-key payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUploadPreKeysJob",
            exports = "uploadPreKeys", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var registrationNode = new NodeBuilder()
                .description("registration")
                .content(DataUtils.intToBytes(registrationId, 4))
                .build();
        var typeBytes = new byte[]{keyBundleType};
        var typeNode = new NodeBuilder()
                .description("type")
                .content(typeBytes)
                .build();
        var identityNode = new NodeBuilder()
                .description("identity")
                .content(identityPublicKey)
                .build();
        var keyNodes = new ArrayList<Node>(preKeys.size());
        for (var preKey : preKeys) {
            keyNodes.add(preKey.toNode());
        }
        var listNode = new NodeBuilder()
                .description("list")
                .content(keyNodes)
                .build();
        var skeyNode = signedPreKey.toNode();
        return new NodeBuilder()
                .description("iq")
                .attribute("id", RandomIdUtils.newId())
                .attribute("xmlns", "encrypt")
                .attribute("type", "set")
                .attribute("to", JidServer.user())
                .content(registrationNode, typeNode, identityNode, listNode, skeyNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqUploadPreKeysRequest) obj;
        return this.registrationId == that.registrationId
                && this.keyBundleType == that.keyBundleType
                && Arrays.equals(this.identityPublicKey, that.identityPublicKey)
                && Objects.equals(this.preKeys, that.preKeys)
                && Objects.equals(this.signedPreKey, that.signedPreKey);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(registrationId, keyBundleType, preKeys, signedPreKey);
        result = 31 * result + Arrays.hashCode(identityPublicKey);
        return result;
    }

    @Override
    public String toString() {
        return "IqUploadPreKeysRequest[registrationId=" + registrationId
                + ", keyBundleType=" + keyBundleType
                + ", identityPublicKey=" + Arrays.toString(identityPublicKey)
                + ", preKeys=" + preKeys
                + ", signedPreKey=" + signedPreKey + ']';
    }
}
