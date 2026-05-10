package com.github.auties00.cobalt.node.iq.encrypt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.util.DataUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * Typed container for the {@code <skey/>} subtree carrying the
 * current signed pre-key.
 */
@WhatsAppWebModule(moduleName = "WAWebSignalUtilsApi")
public final class IqUploadPreKeysSignedPreKey {
    /**
     * The signed-pre-key identifier — encoded as a three-byte
     * big-endian unsigned integer in the {@code <id/>}
     * grandchild.
     */
    private final int id;

    /**
     * The signed-pre-key public-key bytes carried by the
     * {@code <value/>} grandchild.
     */
    private final byte[] publicKey;

    /**
     * The detached signature over {@code publicKey} produced by
     * the local identity key — sixty-four bytes carried by the
     * {@code <signature/>} grandchild.
     */
    private final byte[] signature;

    /**
     * Constructs a new signed-pre-key entry.
     *
     * @param id        the signed-pre-key identifier
     * @param publicKey the public-key bytes; never {@code null}
     * @param signature the detached signature; never {@code null}
     * @throws NullPointerException if either reference argument is
     *                              {@code null}
     */
    public IqUploadPreKeysSignedPreKey(int id, byte[] publicKey, byte[] signature) {
        this.id = id;
        this.publicKey = Objects.requireNonNull(publicKey, "publicKey cannot be null");
        this.signature = Objects.requireNonNull(signature, "signature cannot be null");
    }

    /**
     * Returns the signed-pre-key identifier.
     *
     * @return the identifier
     */
    public int id() {
        return id;
    }

    /**
     * Returns the signed-pre-key public-key bytes.
     *
     * @return the public-key bytes; never {@code null}
     */
    public byte[] publicKey() {
        return publicKey;
    }

    /**
     * Returns the detached signature bytes.
     *
     * @return the signature bytes; never {@code null}
     */
    public byte[] signature() {
        return signature;
    }

    /**
     * Renders this signed-pre-key as the {@code <skey/>} subtree.
     *
     * @return the rendered node
     */
    @WhatsAppWebExport(moduleName = "WAWebSignalUtilsApi",
            exports = "xmppSignedPreKey", adaptation = WhatsAppAdaptation.DIRECT)
    public Node toNode() {
        var idNode = new NodeBuilder()
                .description("id")
                .content(DataUtils.intToBytes(id, 3))
                .build();
        var valueNode = new NodeBuilder()
                .description("value")
                .content(publicKey)
                .build();
        var signatureNode = new NodeBuilder()
                .description("signature")
                .content(signature)
                .build();
        return new NodeBuilder()
                .description("skey")
                .content(idNode, valueNode, signatureNode)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqUploadPreKeysSignedPreKey) obj;
        return this.id == that.id
                && Arrays.equals(this.publicKey, that.publicKey)
                && Arrays.equals(this.signature, that.signature);
    }

    @Override
    public int hashCode() {
        var result = Integer.hashCode(id);
        result = 31 * result + Arrays.hashCode(publicKey);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }

    @Override
    public String toString() {
        return "IqUploadPreKeysSignedPreKey[id=" + id
                + ", publicKey=" + Arrays.toString(publicKey)
                + ", signature=" + Arrays.toString(signature) + ']';
    }
}
