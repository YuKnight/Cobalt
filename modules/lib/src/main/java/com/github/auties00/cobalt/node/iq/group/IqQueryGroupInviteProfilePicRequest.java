package com.github.auties00.cobalt.node.iq.group;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — composes either the invite-link or
 * the invite-message wire shape depending on the {@link IqQueryGroupInviteProfilePicMode}.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryGroupInviteProfilePicApi")
public final class IqQueryGroupInviteProfilePicRequest implements IqOperation.Request {
    /**
     * The dispatch mode.
     */
    private final IqQueryGroupInviteProfilePicMode mode;

    /**
     * The target group JID.
     */
    private final Jid groupJid;

    /**
     * The invite code being previewed.
     */
    private final String code;

    /**
     * The previously-cached picture identifier — the relay omits
     * the {@code <picture>} child when the cached id is still
     * authoritative. {@code null} when no cached id is supplied.
     */
    private final String pictureId;

    /**
     * The picture variant requested ({@code "preview"} for the low-
     * resolution thumbnail, {@code "image"} for the full-size
     * avatar). {@code null} when the caller defers to the relay
     * default.
     */
    private final String pictureType;

    /**
     * The picture-query mode ({@code "url"} to receive the CDN URL,
     * {@code "id"} for just the identifier). {@code null} when the
     * caller defers to the relay default.
     */
    private final String pictureQuery;

    /**
     * The inviting admin's user JID — only used in
     * {@link IqQueryGroupInviteProfilePicMode#INVITE_MESSAGE}.
     */
    private final Jid adminJid;

    /**
     * The invite-message expiration timestamp (seconds since epoch)
     * — only used in {@link IqQueryGroupInviteProfilePicMode#INVITE_MESSAGE}.
     */
    private final String expiration;

    /**
     * Constructs a new request.
     *
     * @param mode         the dispatch mode; never {@code null}
     * @param groupJid     the target group JID; never {@code null}
     * @param code         the invite code; never {@code null}
     * @param pictureId    the cached picture id, or {@code null}
     * @param pictureType  the picture variant, or {@code null}
     * @param pictureQuery the picture-query mode, or {@code null}
     * @param adminJid     the inviting admin JID; required in
     *                     {@link IqQueryGroupInviteProfilePicMode#INVITE_MESSAGE}, ignored in
     *                     {@link IqQueryGroupInviteProfilePicMode#INVITE_LINK}
     * @param expiration   the invite expiration timestamp; required
     *                     in {@link IqQueryGroupInviteProfilePicMode#INVITE_MESSAGE}, ignored
     *                     in {@link IqQueryGroupInviteProfilePicMode#INVITE_LINK}
     * @throws NullPointerException     if {@code mode},
     *                                  {@code groupJid} or
     *                                  {@code code} is {@code null},
     *                                  or if {@code adminJid} or
     *                                  {@code expiration} is
     *                                  {@code null} in
     *                                  {@link IqQueryGroupInviteProfilePicMode#INVITE_MESSAGE}
     */
    public IqQueryGroupInviteProfilePicRequest(IqQueryGroupInviteProfilePicMode mode, Jid groupJid, String code, String pictureId, String pictureType,
                   String pictureQuery, Jid adminJid, String expiration) {
        Objects.requireNonNull(mode, "mode cannot be null");
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(code, "code cannot be null");
        if (mode == IqQueryGroupInviteProfilePicMode.INVITE_MESSAGE) {
            Objects.requireNonNull(adminJid, "adminJid cannot be null in INVITE_MESSAGE mode");
            Objects.requireNonNull(expiration, "expiration cannot be null in INVITE_MESSAGE mode");
        }
        this.mode = mode;
        this.groupJid = groupJid;
        this.code = code;
        this.pictureId = pictureId;
        this.pictureType = pictureType;
        this.pictureQuery = pictureQuery;
        this.adminJid = adminJid;
        this.expiration = expiration;
    }

    /**
     * Returns the dispatch mode.
     *
     * @return the mode; never {@code null}
     */
    public IqQueryGroupInviteProfilePicMode mode() {
        return mode;
    }

    /**
     * Returns the target group JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns the invite code.
     *
     * @return the code; never {@code null}
     */
    public String code() {
        return code;
    }

    /**
     * Returns the optional cached picture identifier.
     *
     * @return an {@link Optional} carrying the id, or empty
     */
    public Optional<String> pictureId() {
        return Optional.ofNullable(pictureId);
    }

    /**
     * Returns the optional picture variant.
     *
     * @return an {@link Optional} carrying the variant, or empty
     */
    public Optional<String> pictureType() {
        return Optional.ofNullable(pictureType);
    }

    /**
     * Returns the optional picture-query mode.
     *
     * @return an {@link Optional} carrying the mode, or empty
     */
    public Optional<String> pictureQuery() {
        return Optional.ofNullable(pictureQuery);
    }

    /**
     * Returns the optional inviting admin JID.
     *
     * @return an {@link Optional} carrying the JID, or empty when
     *         in {@link IqQueryGroupInviteProfilePicMode#INVITE_LINK}
     */
    public Optional<Jid> adminJid() {
        return Optional.ofNullable(adminJid);
    }

    /**
     * Returns the optional invite-message expiration timestamp.
     *
     * @return an {@link Optional} carrying the expiration, or empty
     *         when in {@link IqQueryGroupInviteProfilePicMode#INVITE_LINK}
     */
    public Optional<String> expiration() {
        return Optional.ofNullable(expiration);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <picture>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryGroupInviteProfilePicApi",
            exports = "queryGroupInviteLinkProfilePic",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebQueryGroupInviteProfilePicApi",
            exports = "queryGroupInviteMessageProfilePic",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        if (mode == IqQueryGroupInviteProfilePicMode.INVITE_LINK) {
            return buildInviteLinkStanza();
        }
        return buildInviteMessageStanza();
    }

    /**
     * Builds the {@link IqQueryGroupInviteProfilePicMode#INVITE_LINK} variant.
     *
     * @return the IQ envelope builder
     *
     * @implNote {@code WAWebQueryGroupInviteProfilePicApi.queryGroupInviteLinkProfilePic}:
     *           {@code wap("iq",{to:GROUP_JID(a),type:"get",
     *           xmlns:"w:g2",id}, wap("picture",{id, type, query,
     *           invite:CUSTOM_STRING(r)}))}.
     */
    private NodeBuilder buildInviteLinkStanza() {
        // WAWebQueryGroupInviteProfilePicApi: wap("picture",{id, type, query, invite})
        var pictureBuilder = new NodeBuilder()
                .description("picture")
                .attribute("invite", code);
        if (pictureId != null) {
            pictureBuilder.attribute("id", pictureId);
        }
        if (pictureType != null) {
            pictureBuilder.attribute("type", pictureType);
        }
        if (pictureQuery != null) {
            pictureBuilder.attribute("query", pictureQuery);
        }
        // WAWebQueryGroupInviteProfilePicApi: wap("iq",{to:GROUP_JID(a),type:"get",xmlns:"w:g2",id}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "get")
                .content(pictureBuilder.build());
    }

    /**
     * Builds the {@link IqQueryGroupInviteProfilePicMode#INVITE_MESSAGE} variant.
     *
     * @return the IQ envelope builder
     *
     * @implNote {@code WAWebQueryGroupInviteProfilePicApi.queryGroupInviteMessageProfilePic}:
     *           {@code wap("iq",{to:S_WHATSAPP_NET, type:"get",
     *           target:GROUP_JID(l), xmlns:"w:profile:picture",
     *           id}, wap("picture",{id, type, query},
     *           wap("add_request",{code, expiration,
     *           admin:USER_JID(r)})))}.
     */
    private NodeBuilder buildInviteMessageStanza() {
        // WAWebQueryGroupInviteProfilePicApi: wap("add_request",{code, expiration, admin:USER_JID(r)})
        var addRequestBuilder = new NodeBuilder()
                .description("add_request")
                .attribute("code", code)
                .attribute("expiration", expiration)
                .attribute("admin", adminJid);
        // WAWebQueryGroupInviteProfilePicApi: wap("picture",{id,type,query}, ...)
        var pictureBuilder = new NodeBuilder()
                .description("picture");
        if (pictureId != null) {
            pictureBuilder.attribute("id", pictureId);
        }
        if (pictureType != null) {
            pictureBuilder.attribute("type", pictureType);
        }
        if (pictureQuery != null) {
            pictureBuilder.attribute("query", pictureQuery);
        }
        pictureBuilder.content(addRequestBuilder.build());
        // WAWebQueryGroupInviteProfilePicApi: wap("iq",{to:S_WHATSAPP_NET,type:"get",target:GROUP_JID(l),xmlns:"w:profile:picture",id}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:profile:picture")
                .attribute("to", JidServer.user())
                .attribute("target", groupJid)
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
        var that = (IqQueryGroupInviteProfilePicRequest) obj;
        return this.mode == that.mode
                && Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.code, that.code)
                && Objects.equals(this.pictureId, that.pictureId)
                && Objects.equals(this.pictureType, that.pictureType)
                && Objects.equals(this.pictureQuery, that.pictureQuery)
                && Objects.equals(this.adminJid, that.adminJid)
                && Objects.equals(this.expiration, that.expiration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, groupJid, code, pictureId, pictureType, pictureQuery,
                adminJid, expiration);
    }

    @Override
    public String toString() {
        return "IqQueryGroupInviteProfilePicRequest[mode=" + mode
                + ", groupJid=" + groupJid
                + ", code=" + code
                + ", pictureId=" + pictureId
                + ", pictureType=" + pictureType
                + ", pictureQuery=" + pictureQuery
                + ", adminJid=" + adminJid
                + ", expiration=" + expiration + ']';
    }
}
