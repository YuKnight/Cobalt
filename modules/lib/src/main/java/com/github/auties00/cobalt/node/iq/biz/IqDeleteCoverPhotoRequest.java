package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;

import java.util.Objects;

/**
 * The outbound stanza variant.
 *
 * <p>Legacy-IQ RPC: detaches a previously-uploaded cover photo from the
 * current merchant's business profile.
 *
 * <p>The relay accepts the request irrespective of whether the cover photo
 * actually exists; the {@code id} field carries the upload id so the relay
 * can fan the right detach event into the business profile mutation
 * pipeline.
 *
 * @implNote {@code WAWebBusinessProfileJob.deleteCoverPhoto} emits
 *           {@code <iq xmlns="w:biz" type="set">
 *           <business_profile v="3" mutation_type="delta">
 *           <cover_photo op="delete" id=ID/></business_profile*>}.
 */
@WhatsAppWebModule(moduleName = "WAWebBusinessProfileJob")
public final class IqDeleteCoverPhotoRequest implements IqOperation.Request {
    /**
     * The upload id of the cover photo to detach.
     */
    private final String id;

    /**
     * Constructs a request.
     *
     * @param id the upload id; never {@code null}
     * @throws NullPointerException if {@code id} is {@code null}
     */
    public IqDeleteCoverPhotoRequest(String id) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
    }

    /**
     * Returns the upload id.
     *
     * @return the id; never {@code null}
     */
    public String id() {
        return id;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBusinessProfileJob",
            exports = "deleteCoverPhoto", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var coverPhotoNode = new NodeBuilder()
                .description("cover_photo")
                .attribute("op", "delete")
                .attribute("id", id)
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
        var that = (IqDeleteCoverPhotoRequest) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IqDeleteCoverPhotoRequest[id=" + id + ']';
    }
}
