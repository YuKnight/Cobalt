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
 * Typed container for a single one-time pre-key entry inside the
 * {@code <list/>} wrapper.
 */
@WhatsAppWebModule(moduleName = "WAWebSignalUtilsApi")
public final class IqUploadPreKeysPreKey {
    /**
     * The pre-key identifier — encoded as a three-byte
     * big-endian unsigned integer in the {@code <id/>}
     * grandchild.
     */
    private final int id;

    /**
     * The pre-key public-key bytes carried by the
     * {@code <value/>} grandchild.
     */
    private final byte[] publicKey;

    /**
     * Constructs a new pre-key entry.
     *
     * @param id        the pre-key identifier
     * @param publicKey the pre-key public-key bytes; never
     *                  {@code null}
     * @throws NullPointerException if {@code publicKey} is
     *                              {@code null}
     */
    public IqUploadPreKeysPreKey(int id, byte[] publicKey) {
        this.id = id;
        this.publicKey = Objects.requireNonNull(publicKey, "publicKey cannot be null");
    }

    /**
     * Returns the pre-key identifier.
     *
     * @return the identifier
     */
    public int id() {
        return id;
    }

    /**
     * Returns the pre-key public-key bytes.
     *
     * @return the public-key bytes; never {@code null}
     */
    public byte[] publicKey() {
        return publicKey;
    }

    /**
     * Renders this pre-key as the {@code <key/>} subtree.
     *
     * @return the rendered node
     */
    @WhatsAppWebExport(moduleName = "WAWebSignalUtilsApi",
            exports = "xmppPreKey", adaptation = WhatsAppAdaptation.DIRECT)
    public Node toNode() {
        var idNode = new NodeBuilder()
                .description("id")
                .content(DataUtils.intToBytes(id, 3))
                .build();
        var valueNode = new NodeBuilder()
                .description("value")
                .content(publicKey)
                .build();
        return new NodeBuilder()
                .description("key")
                .content(idNode, valueNode)
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
        var that = (IqUploadPreKeysPreKey) obj;
        return this.id == that.id
                && Arrays.equals(this.publicKey, that.publicKey);
    }

    @Override
    public int hashCode() {
        return 31 * Integer.hashCode(id) + Arrays.hashCode(publicKey);
    }

    @Override
    public String toString() {
        return "IqUploadPreKeysPreKey[id=" + id
                + ", publicKey=" + Arrays.toString(publicKey) + ']';
    }
}
