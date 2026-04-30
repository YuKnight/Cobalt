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
 * Outbound Waffle {@code encrypted_payload_request} IQ. Carries the encrypted action that
 * the relay forwards to the Facebook side together with the RSA encryption metadata, the
 * client wall clock and the linked Facebook account id.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleEncryptedPayloadRequestRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleBaseIQGetRequestMixin")
public final class SmaxWaffleEncryptedPayloadRequestRequest implements SmaxOperation.Request {
    /**
     * The RSA encryption metadata subtree.
     */
    private final SmaxWaffleRsaEncryptionMetadata encryptionMetadata;

    /**
     * The client wall clock at request time, in seconds since the UNIX epoch.
     */
    private final long timestamp;

    /**
     * The linked Facebook account id, as opaque bytes.
     */
    private final byte[] fbid;

    /**
     * The opaque encrypted action bytes representing the CRUD operation that the relay
     * should run on the Facebook side.
     */
    private final byte[] action;

    /**
     * Constructs a request.
     *
     * @param encryptionMetadata the RSA encryption metadata; never
     *                           {@code null}
     * @param timestamp          the UNIX epoch seconds at request time
     * @param fbid               the linked FB id bytes; never
     *                           {@code null}
     * @param action             the opaque encrypted action bytes;
     *                           never {@code null}
     * @throws NullPointerException if any object argument is
     *                              {@code null}
     */
    public SmaxWaffleEncryptedPayloadRequestRequest(SmaxWaffleRsaEncryptionMetadata encryptionMetadata, long timestamp,
                   byte[] fbid, byte[] action) {
        this.encryptionMetadata = Objects.requireNonNull(encryptionMetadata, "encryptionMetadata cannot be null");
        this.timestamp = timestamp;
        this.fbid = Objects.requireNonNull(fbid, "fbid cannot be null");
        this.action = Objects.requireNonNull(action, "action cannot be null");
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
     * Returns the opaque encrypted action bytes.
     *
     * @return the action bytes; never {@code null}
     */
    public byte[] action() {
        return action;
    }

    /**
     * Builds the outbound {@code <iq xmlns="waffle" smax_id="47" type="get">} envelope wrapping
     * the encryption metadata, timestamp, fbid and encrypted action children.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutWaffleEncryptedPayloadRequestRequest",
            exports = "makeEncryptedPayloadRequestRequest", adaptation = WhatsAppAdaptation.DIRECT)
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
        var actionNode = new NodeBuilder()
                .description("action")
                .content(action)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "waffle")
                .attribute("smax_id", 47)
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(encryptionMetadataNode, timestampNode, fbidNode, actionNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxWaffleEncryptedPayloadRequestRequest) obj;
        return this.timestamp == that.timestamp
                && Objects.equals(this.encryptionMetadata, that.encryptionMetadata)
                && Arrays.equals(this.fbid, that.fbid)
                && Arrays.equals(this.action, that.action);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(encryptionMetadata, timestamp);
        result = 31 * result + Arrays.hashCode(fbid);
        result = 31 * result + Arrays.hashCode(action);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxWaffleEncryptedPayloadRequestRequest[encryptionMetadata=" + encryptionMetadata
                + ", timestamp=" + timestamp
                + ", fbid=" + (fbid != null ? fbid.length + " bytes" : "null")
                + ", action=" + (action != null ? action.length + " bytes" : "null") + ']';
    }
}
