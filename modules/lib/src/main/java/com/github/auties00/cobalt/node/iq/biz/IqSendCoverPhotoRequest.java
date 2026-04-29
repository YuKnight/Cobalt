package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Arrays;
import java.util.Objects;

/**
 * The outbound stanza variant.
 */
public final class IqSendCoverPhotoRequest implements IqOperation.Request {
    /**
     * The upload id returned by the mediaWeb upload service.
     */
    private final long id;

    /**
     * The upload timestamp returned by the mediaWeb upload service
     * (Unix seconds).
     */
    private final long ts;

    /**
     * The opaque upload token — used by the relay to validate that the
     * upload artefact still exists and belongs to the calling user.
     */
    private final byte[] token;

    /**
     * Constructs a request.
     *
     * @param id    the upload id
     * @param ts    the upload timestamp
     * @param token the upload token; never {@code null}
     * @throws NullPointerException if {@code token} is {@code null}
     */
    public IqSendCoverPhotoRequest(long id, long ts, byte[] token) {
        this.id = id;
        this.ts = ts;
        Objects.requireNonNull(token, "token cannot be null");
        this.token = token.clone();
    }

    /**
     * Returns the upload id.
     *
     * @return the id
     */
    public long id() {
        return id;
    }

    /**
     * Returns the upload timestamp.
     *
     * @return the timestamp
     */
    public long ts() {
        return ts;
    }

    /**
     * Returns a defensive copy of the upload token.
     *
     * @return the token; never {@code null}
     */
    public byte[] token() {
        return token.clone();
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBusinessProfileJob",
            exports = "sendCoverPhoto", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var coverPhotoNode = new NodeBuilder()
                .description("cover_photo")
                .attribute("op", "update")
                .attribute("id", String.valueOf(id))
                .attribute("ts", String.valueOf(ts))
                .attribute("token", token)
                .build();
        var businessProfileNode = new NodeBuilder()
                .description("business_profile")
                .attribute("v", "3")
                .attribute("mutation_type", "delta")
                .content(coverPhotoNode)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(businessProfileNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSendCoverPhotoRequest) obj;
        return this.id == that.id
                && this.ts == that.ts
                && Arrays.equals(this.token, that.token);
    }

    @Override
    public int hashCode() {
        var h = Objects.hash(id, ts);
        return 31 * h + Arrays.hashCode(token);
    }

    @Override
    public String toString() {
        return "IqSendCoverPhotoRequest[id=" + id + ", ts=" + ts + ']';
    }
}
