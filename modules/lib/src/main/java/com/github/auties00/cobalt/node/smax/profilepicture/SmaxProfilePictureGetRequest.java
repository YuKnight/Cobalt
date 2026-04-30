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
 * The outbound {@code <iq xmlns="w:profile:picture" type="get">}
 * stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureGetRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureGetIQMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureBaseGetIQMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureServerDomainIQMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureAvatarMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureTCTokenMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureAddRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePicturePrivacyTokenContentsMixin")
public final class SmaxProfilePictureGetRequest implements SmaxOperation.Request {
    /**
     * The target entity JID. Routed verbatim into the IQ's
     * {@code target} attribute.
     */
    private final Jid iqTarget;

    /**
     * The optional {@code type} attribute on the inner
     * {@code <picture>} element ({@code "image"} for the full
     * picture or {@code "preview"} for the small preview).
     */
    private final String pictureType;

    /**
     * The optional pre-known picture id (when set, the relay short
     * -circuits to a cache lookup keyed by this id).
     */
    private final String pictureId;

    /**
     * The optional query selector ({@code "url"}, {@code "data"},
     * etc.).
     */
    private final String pictureQuery;

    /**
     * The optional add-to-group invite token.
     */
    private final String pictureInvite;

    /**
     * The optional persona id (community / Meta-AI persona).
     */
    private final String picturePersonaId;

    /**
     * The optional common group JID.
     */
    private final Jid pictureCommonGid;

    /**
     * The optional add-request payload (lift to a sub-mixin).
     */
    private final SmaxProfilePictureGetAddRequestMixin addRequestMixinArgs;

    /**
     * The optional tctoken payload.
     */
    private final SmaxProfilePictureGetTcTokenMixin tcTokenMixinArgs;

    /**
     * The optional avatar payload (when set, replaces the
     * {@code <picture>} root with a {@code <picture type="avatar">}
     * carrying {@code <avatar pose_id>×0..4} children).
     */
    private final SmaxProfilePictureGetAvatarMixin avatarMixinArgs;

    /**
     * Constructs a new picture-get request.
     *
     * @param iqTarget             the target JID; never
     *                             {@code null}
     * @param pictureType          the optional type marker
     * @param pictureId            the optional picture id
     * @param pictureQuery         the optional query selector
     * @param pictureInvite        the optional invite token
     * @param picturePersonaId     the optional persona id
     * @param pictureCommonGid     the optional common-group JID
     * @param addRequestMixinArgs  the optional add-request payload
     * @param tcTokenMixinArgs     the optional tctoken payload
     * @param avatarMixinArgs      the optional avatar payload
     * @throws NullPointerException if {@code iqTarget} is
     *                              {@code null}
     */
    public SmaxProfilePictureGetRequest(Jid iqTarget,
                   String pictureType, String pictureId, String pictureQuery,
                   String pictureInvite, String picturePersonaId, Jid pictureCommonGid,
                   SmaxProfilePictureGetAddRequestMixin addRequestMixinArgs,
                   SmaxProfilePictureGetTcTokenMixin tcTokenMixinArgs,
                   SmaxProfilePictureGetAvatarMixin avatarMixinArgs) {
        this.iqTarget = Objects.requireNonNull(iqTarget, "iqTarget cannot be null");
        this.pictureType = pictureType;
        this.pictureId = pictureId;
        this.pictureQuery = pictureQuery;
        this.pictureInvite = pictureInvite;
        this.picturePersonaId = picturePersonaId;
        this.pictureCommonGid = pictureCommonGid;
        this.addRequestMixinArgs = addRequestMixinArgs;
        this.tcTokenMixinArgs = tcTokenMixinArgs;
        this.avatarMixinArgs = avatarMixinArgs;
    }

    /**
     * Returns the target JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid iqTarget() {
        return iqTarget;
    }

    /**
     * Returns the optional type marker.
     *
     * @return an {@link Optional} carrying the marker
     */
    public Optional<String> pictureType() {
        return Optional.ofNullable(pictureType);
    }

    /**
     * Returns the optional picture id.
     *
     * @return an {@link Optional} carrying the id
     */
    public Optional<String> pictureId() {
        return Optional.ofNullable(pictureId);
    }

    /**
     * Returns the optional query selector.
     *
     * @return an {@link Optional} carrying the selector
     */
    public Optional<String> pictureQuery() {
        return Optional.ofNullable(pictureQuery);
    }

    /**
     * Returns the optional invite token.
     *
     * @return an {@link Optional} carrying the token
     */
    public Optional<String> pictureInvite() {
        return Optional.ofNullable(pictureInvite);
    }

    /**
     * Returns the optional persona id.
     *
     * @return an {@link Optional} carrying the id
     */
    public Optional<String> picturePersonaId() {
        return Optional.ofNullable(picturePersonaId);
    }

    /**
     * Returns the optional common-group JID.
     *
     * @return an {@link Optional} carrying the JID
     */
    public Optional<Jid> pictureCommonGid() {
        return Optional.ofNullable(pictureCommonGid);
    }

    /**
     * Returns the optional add-request payload.
     *
     * @return an {@link Optional} carrying the payload
     */
    public Optional<SmaxProfilePictureGetAddRequestMixin> addRequestMixinArgs() {
        return Optional.ofNullable(addRequestMixinArgs);
    }

    /**
     * Returns the optional tctoken payload.
     *
     * @return an {@link Optional} carrying the payload
     */
    public Optional<SmaxProfilePictureGetTcTokenMixin> tcTokenMixinArgs() {
        return Optional.ofNullable(tcTokenMixinArgs);
    }

    /**
     * Returns the optional avatar payload.
     *
     * @return an {@link Optional} carrying the payload
     */
    public Optional<SmaxProfilePictureGetAvatarMixin> avatarMixinArgs() {
        return Optional.ofNullable(avatarMixinArgs);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <picture>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutProfilePictureGetRequest",
            exports = "makeGetRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var pictureBuilder = new NodeBuilder()
                .description("picture");
        // The avatar overlay replaces the type with "avatar" and supplies the avatar children.
        var pictureChildren = new ArrayList<Node>();
        if (avatarMixinArgs != null) {
            pictureBuilder.attribute("type", "avatar");
            for (var avatarArg : avatarMixinArgs.avatarArgs()) {
                pictureChildren.add(avatarArg.toNode());
            }
        } else if (pictureType != null) {
            pictureBuilder.attribute("type", pictureType);
        }
        if (pictureId != null) {
            pictureBuilder.attribute("id", pictureId);
        }
        if (pictureQuery != null) {
            pictureBuilder.attribute("query", pictureQuery);
        }
        if (pictureInvite != null) {
            pictureBuilder.attribute("invite", pictureInvite);
        }
        if (picturePersonaId != null) {
            pictureBuilder.attribute("persona_id", picturePersonaId);
        }
        if (pictureCommonGid != null) {
            pictureBuilder.attribute("common_gid", pictureCommonGid);
        }
        if (tcTokenMixinArgs != null) {
            pictureChildren.add(tcTokenMixinArgs.toNode());
        }
        if (addRequestMixinArgs != null) {
            pictureChildren.add(addRequestMixinArgs.toNode());
        }
        if (!pictureChildren.isEmpty()) {
            pictureBuilder.content(pictureChildren);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:profile:picture")
                .attribute("to", Jid.userServer())
                .attribute("target", iqTarget)
                .attribute("type", "get")
                .content(pictureBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxProfilePictureGetRequest) obj;
        return Objects.equals(this.iqTarget, that.iqTarget)
                && Objects.equals(this.pictureType, that.pictureType)
                && Objects.equals(this.pictureId, that.pictureId)
                && Objects.equals(this.pictureQuery, that.pictureQuery)
                && Objects.equals(this.pictureInvite, that.pictureInvite)
                && Objects.equals(this.picturePersonaId, that.picturePersonaId)
                && Objects.equals(this.pictureCommonGid, that.pictureCommonGid)
                && Objects.equals(this.addRequestMixinArgs, that.addRequestMixinArgs)
                && Objects.equals(this.tcTokenMixinArgs, that.tcTokenMixinArgs)
                && Objects.equals(this.avatarMixinArgs, that.avatarMixinArgs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iqTarget, pictureType, pictureId, pictureQuery, pictureInvite,
                picturePersonaId, pictureCommonGid, addRequestMixinArgs, tcTokenMixinArgs,
                avatarMixinArgs);
    }

    @Override
    public String toString() {
        return "SmaxProfilePictureGetRequest[iqTarget=" + iqTarget
                + ", pictureType=" + pictureType
                + ", pictureId=" + pictureId
                + ", pictureQuery=" + pictureQuery
                + ", pictureInvite=" + pictureInvite
                + ", picturePersonaId=" + picturePersonaId
                + ", pictureCommonGid=" + pictureCommonGid
                + ", addRequestMixinArgs=" + addRequestMixinArgs
                + ", tcTokenMixinArgs=" + tcTokenMixinArgs
                + ", avatarMixinArgs=" + avatarMixinArgs + ']';
    }
}
