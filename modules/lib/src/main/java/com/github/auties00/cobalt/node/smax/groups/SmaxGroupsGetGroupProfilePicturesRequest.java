package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
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
 * The outbound stanza variant — wraps a list of
 * {@link SmaxGroupsGetGroupProfilePicturesRequest.PictureRequest} entries inside a {@code <pictures/>}
 * wrapper inside the canonical
 * {@code <iq xmlns="w:g2" type="get">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetGroupProfilePicturesRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetGroupProfilePicturesProfilePicturesRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetGroupOrServerMixinGroup")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetServerMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsParentOrSubGroupMixinGroup")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsParentGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsSubGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsProfilePictureIdMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsProfilePictureTypeMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsProfilePictureQueryMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsSubGroupHintMixin")
public final class SmaxGroupsGetGroupProfilePicturesRequest implements SmaxOperation.Request {
    /**
     * The optional addressing override — when supplied, the IQ is
     * sent to the given group; otherwise it goes to the implicit
     * {@code g.us} server.
     */
    private final Jid baseGroupJid;

    /**
     * The optional sub-group hint surfaced as the
     * {@code linked_groups_membership_hint} attribute on the
     * {@code <pictures>} wrapper.
     */
    private final Jid linkedGroupsMembershipHint;

    /**
     * The list of per-picture requests. The relay accepts between
     * {@code 1} and {@code 1000} entries.
     */
    private final List<PictureRequest> pictures;

    /**
     * Constructs a request.
     *
     * @param baseGroupJid               optional IQ-{@code to} group
     *                                   override; {@code null} routes
     *                                   to {@code g.us}
     * @param linkedGroupsMembershipHint optional sub-group hint;
     *                                   may be {@code null}
     * @param pictures                   the per-picture requests;
     *                                   never {@code null}, never
     *                                   empty
     * @throws NullPointerException     if {@code pictures} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code pictures} is empty
     */
    public SmaxGroupsGetGroupProfilePicturesRequest(Jid baseGroupJid, Jid linkedGroupsMembershipHint, List<PictureRequest> pictures) {
        Objects.requireNonNull(pictures, "pictures cannot be null");
        if (pictures.isEmpty()) {
            throw new IllegalArgumentException("pictures cannot be empty");
        }
        this.baseGroupJid = baseGroupJid;
        this.linkedGroupsMembershipHint = linkedGroupsMembershipHint;
        this.pictures = List.copyOf(pictures);
    }

    /**
     * Returns the optional IQ-{@code to} group override.
     *
     * @return an {@link Optional} carrying the group JID, or empty
     *         when the IQ is routed to {@code g.us}
     */
    public Optional<Jid> baseGroupJid() {
        return Optional.ofNullable(baseGroupJid);
    }

    /**
     * Returns the optional sub-group hint.
     *
     * @return an {@link Optional} carrying the hint group JID, or
     *         empty when the caller did not supply one
     */
    public Optional<Jid> linkedGroupsMembershipHint() {
        return Optional.ofNullable(linkedGroupsMembershipHint);
    }

    /**
     * Returns the per-picture requests.
     *
     * @return an unmodifiable list of picture requests; never
     *         {@code null}
     */
    public List<PictureRequest> pictures() {
        return pictures;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <pictures/>} payload
     *
     * @implNote {@code WASmaxOutGroupsGetGroupProfilePicturesRequest.makeGetGroupProfilePicturesRequest}
     *           composes {@code <pictures>} carrying
     *           {@code REPEATED_CHILD(<picture id? type? query?>, 1, 1000)}
     *           and an optional {@code linked_groups_membership_hint}
     *           attribute (via {@code WASmaxOutGroupsSubGroupHintMixin}),
     *           then merges the parent-or-sub-group disjunction onto
     *           each picture child via
     *           {@code WASmaxOutGroupsParentOrSubGroupMixinGroup} and
     *           wraps the whole stanza in either
     *           {@code WASmaxOutGroupsBaseGetGroupMixin} or
     *           {@code WASmaxOutGroupsBaseGetServerMixin} depending on
     *           the {@code BaseGetGroupOrServerMixinGroup}
     *           disjunction.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsGetGroupProfilePicturesRequest",
            exports = "makeGetGroupProfilePicturesRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var pictureNodes = new ArrayList<Node>(pictures.size());
        for (var pictureRequest : pictures) {
            var pictureBuilder = new NodeBuilder()
                    .description("picture");
            var parentGroupJid = pictureRequest.parentGroupJid().orElse(null);
            if (parentGroupJid != null) {
                pictureBuilder.attribute("parent_group_jid", parentGroupJid);
            }
            var subGroupJid = pictureRequest.subGroupJid().orElse(null);
            if (subGroupJid != null) {
                pictureBuilder.attribute("sub_group_jid", subGroupJid);
            }
            var pictureId = pictureRequest.pictureId().orElse(null);
            if (pictureId != null) {
                pictureBuilder.attribute("id", pictureId);
            }
            var pictureType = pictureRequest.pictureType().orElse(null);
            if (pictureType != null) {
                pictureBuilder.attribute("type", pictureType);
            }
            var pictureQuery = pictureRequest.pictureQuery().orElse(null);
            if (pictureQuery != null) {
                pictureBuilder.attribute("query", pictureQuery);
            }
            pictureNodes.add(pictureBuilder.build());
        }
        var picturesBuilder = new NodeBuilder()
                .description("pictures")
                .content(pictureNodes);
        if (linkedGroupsMembershipHint != null) {
            picturesBuilder.attribute("linked_groups_membership_hint", linkedGroupsMembershipHint);
        }
        var iqBuilder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("type", "get");
        if (baseGroupJid != null) {
            iqBuilder.attribute("to", baseGroupJid);
        } else {
            iqBuilder.attribute("to", JidServer.groupOrCommunity());
        }
        iqBuilder.content(picturesBuilder.build());
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
        var that = (SmaxGroupsGetGroupProfilePicturesRequest) obj;
        return Objects.equals(this.baseGroupJid, that.baseGroupJid)
                && Objects.equals(this.linkedGroupsMembershipHint, that.linkedGroupsMembershipHint)
                && Objects.equals(this.pictures, that.pictures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseGroupJid, linkedGroupsMembershipHint, pictures);
    }

    @Override
    public String toString() {
        return "SmaxGroupsGetGroupProfilePicturesRequest[baseGroupJid=" + baseGroupJid
                + ", linkedGroupsMembershipHint=" + linkedGroupsMembershipHint
                + ", pictures=" + pictures + ']';
    }

    /**
     * The per-picture sub-payload — carries either a
     * {@code parent_group_jid} or {@code sub_group_jid} addressing
     * attribute alongside optional {@code id}, {@code type} and
     * {@code query} projection hints.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetGroupProfilePicturesProfilePicturesRequestMixin")
    public static final class PictureRequest {
        /**
         * The parent-group JID — set when targeting the parent
         * group's profile picture. Mutually exclusive with
         * {@link #subGroupJid}.
         */
        private final Jid parentGroupJid;

        /**
         * The sub-group JID — set when targeting a sub-group's
         * profile picture. Mutually exclusive with
         * {@link #parentGroupJid}.
         */
        private final Jid subGroupJid;

        /**
         * The optional dehydration-hint id of a previously-known
         * picture; when supplied, the relay returns the
         * {@code did_not_change} marker instead of re-shipping
         * unchanged bytes.
         */
        private final String pictureId;

        /**
         * The optional picture type ({@code "image"} or
         * {@code "preview"}) selecting the resolution variant.
         */
        private final String pictureType;

        /**
         * The optional projection-mode hint ({@code "url"} for a
         * URL+direct_path, {@code "blob"} for inline bytes).
         */
        private final String pictureQuery;

        /**
         * Constructs a request.
         *
         * @param parentGroupJid optional parent-group JID
         * @param subGroupJid    optional sub-group JID
         * @param pictureId      optional picture id
         * @param pictureType    optional picture type
         * @param pictureQuery   optional projection mode
         * @throws IllegalArgumentException if both
         *                                  {@code parentGroupJid}
         *                                  and {@code subGroupJid}
         *                                  are supplied or both are
         *                                  {@code null}
         */
        public PictureRequest(Jid parentGroupJid, Jid subGroupJid,
                              String pictureId, String pictureType, String pictureQuery) {
            if (parentGroupJid == null && subGroupJid == null) {
                throw new IllegalArgumentException(
                        "either parentGroupJid or subGroupJid must be supplied");
            }
            if (parentGroupJid != null && subGroupJid != null) {
                throw new IllegalArgumentException(
                        "parentGroupJid and subGroupJid are mutually exclusive");
            }
            this.parentGroupJid = parentGroupJid;
            this.subGroupJid = subGroupJid;
            this.pictureId = pictureId;
            this.pictureType = pictureType;
            this.pictureQuery = pictureQuery;
        }

        /**
         * Returns the parent-group JID when this request targets a
         * parent group.
         *
         * @return an {@link Optional} carrying the parent-group JID
         */
        public Optional<Jid> parentGroupJid() {
            return Optional.ofNullable(parentGroupJid);
        }

        /**
         * Returns the sub-group JID when this request targets a
         * sub-group.
         *
         * @return an {@link Optional} carrying the sub-group JID
         */
        public Optional<Jid> subGroupJid() {
            return Optional.ofNullable(subGroupJid);
        }

        /**
         * Returns the optional dehydration-hint id.
         *
         * @return an {@link Optional} carrying the picture id
         */
        public Optional<String> pictureId() {
            return Optional.ofNullable(pictureId);
        }

        /**
         * Returns the optional picture type.
         *
         * @return an {@link Optional} carrying the picture type
         */
        public Optional<String> pictureType() {
            return Optional.ofNullable(pictureType);
        }

        /**
         * Returns the optional projection mode.
         *
         * @return an {@link Optional} carrying the projection mode
         */
        public Optional<String> pictureQuery() {
            return Optional.ofNullable(pictureQuery);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (PictureRequest) obj;
            return Objects.equals(this.parentGroupJid, that.parentGroupJid)
                    && Objects.equals(this.subGroupJid, that.subGroupJid)
                    && Objects.equals(this.pictureId, that.pictureId)
                    && Objects.equals(this.pictureType, that.pictureType)
                    && Objects.equals(this.pictureQuery, that.pictureQuery);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parentGroupJid, subGroupJid, pictureId, pictureType, pictureQuery);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetGroupProfilePicturesRequest.PictureRequest[parentGroupJid="
                    + parentGroupJid + ", subGroupJid=" + subGroupJid
                    + ", pictureId=" + pictureId + ", pictureType=" + pictureType
                    + ", pictureQuery=" + pictureQuery + ']';
        }
    }
}
