package com.github.auties00.cobalt.node.smax.profilepicture;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The optional {@code <picture type="avatar"><avatar
 * pose_id/>×0..4></picture>} payload overlay carried by a
 * {@link SmaxProfilePictureGetRequest}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureAvatarMixin")
public final class SmaxProfilePictureGetAvatarMixin {
    /**
     * The list of {@code <avatar pose_id/>} children. Between
     * {@code 0} and {@code 4} entries.
     */
    private final List<AvatarPose> avatarArgs;

    /**
     * Constructs a new avatar payload.
     *
     * @param avatarArgs the avatar entries; never {@code null}; at
     *                   most {@code 4} entries
     * @throws NullPointerException     if {@code avatarArgs} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code avatarArgs} has
     *                                  more than {@code 4} entries
     */
    public SmaxProfilePictureGetAvatarMixin(List<AvatarPose> avatarArgs) {
        Objects.requireNonNull(avatarArgs, "avatarArgs cannot be null");
        if (avatarArgs.size() > 4) {
            throw new IllegalArgumentException(
                    "avatarArgs must carry at most 4 entries");
        }
        this.avatarArgs = List.copyOf(avatarArgs);
    }

    /**
     * Returns the avatar entries.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<AvatarPose> avatarArgs() {
        return avatarArgs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxProfilePictureGetAvatarMixin) obj;
        return Objects.equals(this.avatarArgs, that.avatarArgs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(avatarArgs);
    }

    @Override
    public String toString() {
        return "SmaxProfilePictureGetAvatarMixin[avatarArgs=" + avatarArgs + ']';
    }

    /**
     * A single {@code <avatar pose_id/>} entry.
     */
    public static final class AvatarPose {
        /**
         * The {@code pose_id} attribute.
         */
        private final String avatarPoseId;

        /**
         * Constructs a new pose entry.
         *
         * @param avatarPoseId the pose id; never {@code null}
         * @throws NullPointerException if {@code avatarPoseId} is
         *                              {@code null}
         */
        public AvatarPose(String avatarPoseId) {
            this.avatarPoseId = Objects.requireNonNull(avatarPoseId, "avatarPoseId cannot be null");
        }

        /**
         * Returns the pose id.
         *
         * @return the id; never {@code null}
         */
        public String avatarPoseId() {
            return avatarPoseId;
        }

        /**
         * Builds the {@code <avatar pose_id/>} node.
         *
         * @return the {@link Node}
         */
        @WhatsAppWebExport(moduleName = "WASmaxOutProfilePictureAvatarMixin",
                exports = "makeAvatarAvatar",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Node toNode() {
            return new NodeBuilder()
                    .description("avatar")
                    .attribute("pose_id", avatarPoseId)
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
            var that = (AvatarPose) obj;
            return Objects.equals(this.avatarPoseId, that.avatarPoseId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(avatarPoseId);
        }

        @Override
        public String toString() {
            return "SmaxProfilePictureGetAvatarMixin.AvatarPose[avatarPoseId=" + avatarPoseId + ']';
        }
    }
}
