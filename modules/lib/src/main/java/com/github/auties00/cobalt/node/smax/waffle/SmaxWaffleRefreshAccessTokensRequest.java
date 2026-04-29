package com.github.auties00.cobalt.node.smax.waffle;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleRefreshAccessTokensRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleBaseIQGetRequestMixin")
public final class SmaxWaffleRefreshAccessTokensRequest implements SmaxOperation.Request {
    /**
     * The RSA encryption metadata subtree.
     */
    private final SmaxWaffleRsaEncryptionMetadata encryptionMetadata;

    /**
     * The client's wall-clock at request time.
     */
    private final long timestamp;

    /**
     * The linked Facebook account id.
     */
    private final byte[] fbid;

    /**
     * Constructs a request.
     *
     * @param encryptionMetadata the RSA encryption metadata; never
     *                           {@code null}
     * @param timestamp          the UNIX epoch seconds at request time
     * @param fbid               the linked FB id bytes; never
     *                           {@code null}
     * @throws NullPointerException if any object argument is
     *                              {@code null}
     */
    public SmaxWaffleRefreshAccessTokensRequest(SmaxWaffleRsaEncryptionMetadata encryptionMetadata, long timestamp, byte[] fbid) {
        this.encryptionMetadata = Objects.requireNonNull(encryptionMetadata, "encryptionMetadata cannot be null");
        this.timestamp = timestamp;
        this.fbid = Objects.requireNonNull(fbid, "fbid cannot be null");
    }

    /**
     * Returns the RSA encryption metadata.
     *
     * @return the metadata; never {@code null}
     */
    public SmaxWaffleRsaEncryptionMetadata encryptionMetadata() {
        return encryptionMetadata;
    }

    /**
     * Returns the request timestamp.
     *
     * @return the UNIX epoch seconds
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Returns the linked FB id bytes.
     *
     * @return the fbid bytes; never {@code null}
     */
    public byte[] fbid() {
        return fbid;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     *
     * @implNote {@code WASmaxOutWaffleRefreshAccessTokensRequest.makeRefreshAccessTokensRequest}
     *           composes the same shape as
     *           {@link SmaxWaffleWFPingRequest#toNode()} but with
     *           {@code smax_id="46"}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutWaffleRefreshAccessTokensRequest",
            exports = "makeRefreshAccessTokensRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var encryptionMetadataNode = encryptionMetadata.toNode();
        var timestampNode = new NodeBuilder()
                .description("timestamp")
                .content(timestamp)
                .build();
        var fbidNode = new NodeBuilder()
                .description("fbid")
                .content(fbid)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "waffle")
                .attribute("smax_id", 46)
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(encryptionMetadataNode, timestampNode, fbidNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxWaffleRefreshAccessTokensRequest) obj;
        return this.timestamp == that.timestamp
                && Objects.equals(this.encryptionMetadata, that.encryptionMetadata)
                && Arrays.equals(this.fbid, that.fbid);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(encryptionMetadata, timestamp);
        result = 31 * result + Arrays.hashCode(fbid);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxWaffleRefreshAccessTokensRequest[encryptionMetadata=" + encryptionMetadata
                + ", timestamp=" + timestamp
                + ", fbid=" + (fbid != null ? fbid.length + " bytes" : "null") + ']';
    }
}
