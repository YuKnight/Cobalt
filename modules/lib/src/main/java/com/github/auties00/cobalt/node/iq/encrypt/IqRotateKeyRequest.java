package com.github.auties00.cobalt.node.iq.encrypt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.util.RandomIdUtils;
import java.util.Objects;

/**
 * The outbound {@code <iq xmlns="encrypt" type="set">} stanza variant
 * — wraps the {@code <rotate><skey/>*>} payload.
 */
@WhatsAppWebModule(moduleName = "WAWebRotateKeyJob")
public final class IqRotateKeyRequest implements IqOperation.Request {
    /**
     * The freshly-generated signed pre-key being uploaded. Rendered
     * verbatim into the {@code <skey/>} grandchild.
     */
    private final IqUploadPreKeysSignedPreKey signedPreKey;

    /**
     * Constructs a new rotate-key request.
     *
     * @param signedPreKey the freshly-generated signed pre-key; never
     *                     {@code null}
     * @throws NullPointerException if {@code signedPreKey} is
     *                              {@code null}
     */
    public IqRotateKeyRequest(IqUploadPreKeysSignedPreKey signedPreKey) {
        this.signedPreKey = Objects.requireNonNull(signedPreKey, "signedPreKey cannot be null");
    }

    /**
     * Returns the signed pre-key being uploaded.
     *
     * @return the signed pre-key; never {@code null}
     */
    public IqUploadPreKeysSignedPreKey signedPreKey() {
        return signedPreKey;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <rotate>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebRotateKeyJob",
            exports = "rotateKey", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var skeyNode = signedPreKey.toNode();
        var rotateNode = new NodeBuilder()
                .description("rotate")
                .content(skeyNode)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("id", RandomIdUtils.newId())
                .attribute("xmlns", "encrypt")
                .attribute("type", "set")
                .attribute("to", JidServer.user())
                .content(rotateNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqRotateKeyRequest) obj;
        return Objects.equals(this.signedPreKey, that.signedPreKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signedPreKey);
    }

    @Override
    public String toString() {
        return "IqRotateKeyRequest[signedPreKey=" + signedPreKey + ']';
    }
}
