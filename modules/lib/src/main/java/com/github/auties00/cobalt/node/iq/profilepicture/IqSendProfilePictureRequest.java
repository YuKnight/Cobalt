package com.github.auties00.cobalt.node.iq.profilepicture;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the optional
 * {@code <picture type="image">PIC_BYTES</picture>} child in the
 * canonical {@code <iq xmlns="w:profile:picture" type="set">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebSendProfilePictureJob")
public final class IqSendProfilePictureRequest implements IqOperation.Request {
    /**
     * The target group JID when updating a group profile picture.
     * {@code null} when updating the calling user's own picture.
     */
    private final Jid groupTarget;

    /**
     * The new JPEG profile-picture bytes. {@code null} clears the
     * existing picture.
     */
    private final byte[] picture;

    /**
     * Constructs a new request.
     *
     * @param groupTarget the group JID for a group-picture update,
     *                    or {@code null} for a self-picture update
     * @param picture     the new JPEG bytes, or {@code null} to
     *                    clear the existing picture
     */
    public IqSendProfilePictureRequest(Jid groupTarget, byte[] picture) {
        this.groupTarget = groupTarget;
        this.picture = picture == null ? null : picture.clone();
    }

    /**
     * Returns the optional group JID target.
     *
     * @return an {@link Optional} carrying the group JID, or empty
     *         when this is a self-picture update
     */
    public Optional<Jid> groupTarget() {
        return Optional.ofNullable(groupTarget);
    }

    /**
     * Returns a defensive copy of the optional new picture bytes.
     *
     * @return an {@link Optional} carrying a clone of the JPEG
     *         bytes, or empty when the picture is being cleared
     */
    public Optional<byte[]> picture() {
        return Optional.ofNullable(picture).map(byte[]::clone);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the optional {@code <picture>} payload
     *
     * @implNote {@code WAWebSendProfilePictureJob.default}:
     *           {@code wap("iq",{to:S_WHATSAPP_NET,
     *           target: isGroup ? CHAT_JID : DROP_ATTR,
     *           type:"set", xmlns:"w:profile:picture", id},
     *           a ? wap("picture",{type:"image"}, a) : null)}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSendProfilePictureJob",
            exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var iqBuilder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:profile:picture")
                .attribute("to", JidServer.user())
                .attribute("type", "set");
        if (groupTarget != null) {
            iqBuilder.attribute("target", groupTarget);
        }
        if (picture != null) {
            // WAWebSendProfilePictureJob: wap("picture",{type:"image"}, a)
            var pictureNode = new NodeBuilder()
                    .description("picture")
                    .attribute("type", "image")
                    .content(picture)
                    .build();
            iqBuilder.content(pictureNode);
        }
        return iqBuilder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSendProfilePictureRequest) obj;
        return Objects.equals(this.groupTarget, that.groupTarget)
                && Arrays.equals(this.picture, that.picture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupTarget, Arrays.hashCode(picture));
    }

    @Override
    public String toString() {
        var pictureLength = picture == null ? -1 : picture.length;
        return "IqSendProfilePictureRequest[groupTarget=" + groupTarget
                + ", pictureLength=" + pictureLength + ']';
    }
}
