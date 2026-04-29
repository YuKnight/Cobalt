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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the {@code <create/>} payload
 * in the canonical {@code <iq xmlns="w:g2" type="set" to="g.us">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsCreateRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetServerMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsCreateRequest implements SmaxOperation.Request {
    /**
     * The new group's subject (display name); transmitted as the
     * {@code subject} attribute on the {@code <iq>} via the
     * {@code NamedSubjectOrUnnamedSubjectFallbackMixinGroup}.
     */
    private final String subject;

    /**
     * The list of participants to seed the new group with. Must be
     * non-empty (1..19999 entries server-side).
     */
    private final List<RequestParticipant> participants;

    /**
     * The optional description body (transmitted under
     * {@code <description><body/></description>}).
     */
    private final String descriptionBody;

    /**
     * The optional description ID — emitted as the
     * {@code <description id/>} attribute.
     */
    private final String descriptionId;

    /**
     * Whether to attach a {@code <locked/>} child (chat-info edits
     * become admin-only).
     */
    private final boolean locked;

    /**
     * Whether to attach an {@code <announcement/>} child (only
     * admins may post).
     */
    private final boolean announcement;

    /**
     * Whether to attach a {@code <parent/>} child with the literal
     * {@code default_membership_approval_mode="request_required"}
     * attribute (the only mode value the WA Web mixin emits).
     */
    private final boolean parentDefaultMembershipApprovalMode;

    /**
     * Whether to attach a {@code <no_frequently_forwarded/>} child.
     */
    private final boolean noFrequentlyForwarded;

    /**
     * The optional ephemeral-message expiration in seconds — non-null
     * triggers an {@code <ephemeral expiration trigger/>} child.
     */
    private final Integer ephemeralExpiration;

    /**
     * The optional ephemeral-message trigger value (paired with
     * {@link #ephemeralExpiration}); may be {@code null}.
     */
    private final Integer ephemeralTrigger;

    /**
     * The optional membership-approval join-mode — non-null triggers
     * a {@code <membership_approval_mode group_join_mode/>} child.
     */
    private final String membershipApprovalGroupJoinMode;

    /**
     * Whether to attach a {@code <breakout/>} child (creates a
     * breakout sub-group of an existing community).
     */
    private final boolean breakout;

    /**
     * Whether to attach a {@code <created_as_lid/>} child.
     */
    private final boolean createdAsLid;

    /**
     * The optional addressing-mode-override value — non-null
     * triggers an {@code <addressing_mode_override mode/>} child.
     */
    private final String addressingModeOverrideMode;

    /**
     * The optional parent community JID — non-null triggers a
     * {@code <linked_parent jid/>} child that links the new group to
     * an existing community.
     */
    private final Jid linkedParentJid;

    /**
     * Whether to attach a {@code <hidden_group/>} child (the new
     * group is hidden from the community directory).
     */
    private final boolean hiddenGroup;

    /**
     * Whether to attach an
     * {@code <allow_non_admin_sub_group_creation/>} child.
     */
    private final boolean allowNonAdminSubGroupCreation;

    /**
     * Whether to attach a {@code <create_general_chat/>} child.
     */
    private final boolean createGeneralChat;

    /**
     * Whether to attach a {@code <capi/>} child.
     */
    private final boolean capi;

    /**
     * The optional dedup token attached as the {@code dedup} root
     * attribute (mixin override).
     */
    private final String dedupAttr;

    /**
     * The optional member-add-mode value attached as the root
     * attribute (mixin override).
     */
    private final String memberAddMode;

    /**
     * The optional member-link-mode value attached as the root
     * attribute (mixin override).
     */
    private final String memberLinkMode;

    /**
     * The optional member-share-group-history-mode value attached
     * as the root attribute (mixin override).
     */
    private final String memberShareGroupHistoryMode;

    /**
     * Constructs a request directly. Use {@link #builder()} for a
     * fluent alternative.
     *
     * @param subject                                  the group
     *                                                 subject; never
     *                                                 {@code null}
     * @param participants                             the list of
     *                                                 seed
     *                                                 participants;
     *                                                 never
     *                                                 {@code null}
     *                                                 and must be
     *                                                 non-empty
     * @param descriptionBody                          the optional
     *                                                 description
     *                                                 body; may be
     *                                                 {@code null}
     * @param descriptionId                            the optional
     *                                                 description ID
     *                                                 attribute; may
     *                                                 be
     *                                                 {@code null}
     * @param locked                                   see
     *                                                 {@link #locked()}
     * @param announcement                             see
     *                                                 {@link #announcement()}
     * @param parentDefaultMembershipApprovalMode      whether to
     *                                                 attach the
     *                                                 {@code <parent/>}
     *                                                 child with the
     *                                                 literal
     *                                                 default-membership-approval
     *                                                 attribute
     * @param noFrequentlyForwarded                    see
     *                                                 {@link #noFrequentlyForwarded()}
     * @param ephemeralExpiration                      the optional
     *                                                 ephemeral
     *                                                 expiration in
     *                                                 seconds; may
     *                                                 be
     *                                                 {@code null}
     * @param ephemeralTrigger                         the optional
     *                                                 ephemeral
     *                                                 trigger value;
     *                                                 may be
     *                                                 {@code null}
     * @param membershipApprovalGroupJoinMode          the optional
     *                                                 membership-approval
     *                                                 join mode
     * @param breakout                                 see
     *                                                 {@link #breakout()}
     * @param createdAsLid                             see
     *                                                 {@link #createdAsLid()}
     * @param addressingModeOverrideMode               the optional
     *                                                 addressing-mode-override
     *                                                 value
     * @param linkedParentJid                          the optional
     *                                                 parent
     *                                                 community JID;
     *                                                 may be
     *                                                 {@code null}
     * @param hiddenGroup                              see
     *                                                 {@link #hiddenGroup()}
     * @param allowNonAdminSubGroupCreation            see
     *                                                 {@link #allowNonAdminSubGroupCreation()}
     * @param createGeneralChat                        see
     *                                                 {@link #createGeneralChat()}
     * @param capi                                     see
     *                                                 {@link #capi()}
     * @param dedupAttr                                the optional
     *                                                 dedup token
     * @param memberAddMode                            the optional
     *                                                 member-add
     *                                                 mode
     * @param memberLinkMode                           the optional
     *                                                 member-link
     *                                                 mode
     * @param memberShareGroupHistoryMode              the optional
     *                                                 member-share-history
     *                                                 mode
     * @throws NullPointerException     if {@code subject} or
     *                                  {@code participants} is
     *                                  {@code null}
     * @throws IllegalArgumentException when {@code participants} is
     *                                  empty
     */
    public SmaxGroupsCreateRequest(String subject,
                   List<RequestParticipant> participants,
                   String descriptionBody,
                   String descriptionId,
                   boolean locked,
                   boolean announcement,
                   boolean parentDefaultMembershipApprovalMode,
                   boolean noFrequentlyForwarded,
                   Integer ephemeralExpiration,
                   Integer ephemeralTrigger,
                   String membershipApprovalGroupJoinMode,
                   boolean breakout,
                   boolean createdAsLid,
                   String addressingModeOverrideMode,
                   Jid linkedParentJid,
                   boolean hiddenGroup,
                   boolean allowNonAdminSubGroupCreation,
                   boolean createGeneralChat,
                   boolean capi,
                   String dedupAttr,
                   String memberAddMode,
                   String memberLinkMode,
                   String memberShareGroupHistoryMode) {
        Objects.requireNonNull(subject, "subject cannot be null");
        Objects.requireNonNull(participants, "participants cannot be null");
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("participants must contain at least one entry");
        }
        this.subject = subject;
        this.participants = List.copyOf(participants);
        this.descriptionBody = descriptionBody;
        this.descriptionId = descriptionId;
        this.locked = locked;
        this.announcement = announcement;
        this.parentDefaultMembershipApprovalMode = parentDefaultMembershipApprovalMode;
        this.noFrequentlyForwarded = noFrequentlyForwarded;
        this.ephemeralExpiration = ephemeralExpiration;
        this.ephemeralTrigger = ephemeralTrigger;
        this.membershipApprovalGroupJoinMode = membershipApprovalGroupJoinMode;
        this.breakout = breakout;
        this.createdAsLid = createdAsLid;
        this.addressingModeOverrideMode = addressingModeOverrideMode;
        this.linkedParentJid = linkedParentJid;
        this.hiddenGroup = hiddenGroup;
        this.allowNonAdminSubGroupCreation = allowNonAdminSubGroupCreation;
        this.createGeneralChat = createGeneralChat;
        this.capi = capi;
        this.dedupAttr = dedupAttr;
        this.memberAddMode = memberAddMode;
        this.memberLinkMode = memberLinkMode;
        this.memberShareGroupHistoryMode = memberShareGroupHistoryMode;
    }

    /**
     * Returns a fresh {@link Builder} for fluent construction.
     *
     * @return a new builder instance; never {@code null}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the group subject.
     *
     * @return the subject; never {@code null}
     */
    public String subject() {
        return subject;
    }

    /**
     * Returns the seed participants.
     *
     * @return an unmodifiable list of participants; never empty
     */
    public List<RequestParticipant> participants() {
        return participants;
    }

    /**
     * Returns the optional description body.
     *
     * @return an {@link Optional} carrying the body, or empty when
     *         omitted
     */
    public Optional<String> descriptionBody() {
        return Optional.ofNullable(descriptionBody);
    }

    /**
     * Returns the optional description ID.
     *
     * @return an {@link Optional} carrying the ID, or empty when
     *         omitted
     */
    public Optional<String> descriptionId() {
        return Optional.ofNullable(descriptionId);
    }

    /**
     * Returns whether the {@code <locked/>} child is attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean locked() {
        return locked;
    }

    /**
     * Returns whether the {@code <announcement/>} child is attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean announcement() {
        return announcement;
    }

    /**
     * Returns whether the parent-default membership-approval
     * marker is attached.
     *
     * @return {@code true} when the {@code <parent/>} child with
     *         the literal default-membership-approval attribute is
     *         emitted
     */
    public boolean parentDefaultMembershipApprovalMode() {
        return parentDefaultMembershipApprovalMode;
    }

    /**
     * Returns whether the {@code <no_frequently_forwarded/>} child
     * is attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean noFrequentlyForwarded() {
        return noFrequentlyForwarded;
    }

    /**
     * Returns the optional ephemeral expiration value.
     *
     * @return an {@link Optional} carrying the expiration in
     *         seconds, or empty when omitted
     */
    public Optional<Integer> ephemeralExpiration() {
        return Optional.ofNullable(ephemeralExpiration);
    }

    /**
     * Returns the optional ephemeral trigger value.
     *
     * @return an {@link Optional} carrying the trigger, or empty
     *         when omitted
     */
    public Optional<Integer> ephemeralTrigger() {
        return Optional.ofNullable(ephemeralTrigger);
    }

    /**
     * Returns the optional membership-approval join-mode value.
     *
     * @return an {@link Optional} carrying the value, or empty when
     *         omitted
     */
    public Optional<String> membershipApprovalGroupJoinMode() {
        return Optional.ofNullable(membershipApprovalGroupJoinMode);
    }

    /**
     * Returns whether the {@code <breakout/>} child is attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean breakout() {
        return breakout;
    }

    /**
     * Returns whether the {@code <created_as_lid/>} child is
     * attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean createdAsLid() {
        return createdAsLid;
    }

    /**
     * Returns the optional addressing-mode-override value.
     *
     * @return an {@link Optional} carrying the value, or empty when
     *         omitted
     */
    public Optional<String> addressingModeOverrideMode() {
        return Optional.ofNullable(addressingModeOverrideMode);
    }

    /**
     * Returns the optional parent community JID.
     *
     * @return an {@link Optional} carrying the JID, or empty when
     *         omitted
     */
    public Optional<Jid> linkedParentJid() {
        return Optional.ofNullable(linkedParentJid);
    }

    /**
     * Returns whether the {@code <hidden_group/>} child is attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean hiddenGroup() {
        return hiddenGroup;
    }

    /**
     * Returns whether the
     * {@code <allow_non_admin_sub_group_creation/>} child is
     * attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean allowNonAdminSubGroupCreation() {
        return allowNonAdminSubGroupCreation;
    }

    /**
     * Returns whether the {@code <create_general_chat/>} child is
     * attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean createGeneralChat() {
        return createGeneralChat;
    }

    /**
     * Returns whether the {@code <capi/>} child is attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean capi() {
        return capi;
    }

    /**
     * Returns the optional dedup token attribute.
     *
     * @return an {@link Optional} carrying the token, or empty when
     *         omitted
     */
    public Optional<String> dedupAttr() {
        return Optional.ofNullable(dedupAttr);
    }

    /**
     * Returns the optional member-add-mode attribute.
     *
     * @return an {@link Optional} carrying the value, or empty when
     *         omitted
     */
    public Optional<String> memberAddMode() {
        return Optional.ofNullable(memberAddMode);
    }

    /**
     * Returns the optional member-link-mode attribute.
     *
     * @return an {@link Optional} carrying the value, or empty when
     *         omitted
     */
    public Optional<String> memberLinkMode() {
        return Optional.ofNullable(memberLinkMode);
    }

    /**
     * Returns the optional member-share-group-history-mode
     * attribute.
     *
     * @return an {@link Optional} carrying the value, or empty when
     *         omitted
     */
    public Optional<String> memberShareGroupHistoryMode() {
        return Optional.ofNullable(memberShareGroupHistoryMode);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <create/>} payload
     *
     * @implNote {@code WASmaxOutGroupsCreateRequest.makeCreateRequest}
     *           composes
     *           {@code WASmaxOutGroupsBaseSetServerMixin}
     *           ({@code xmlns="w:g2"}, {@code to="g.us"}) over the
     *           {@code <create/>} root, layering up to four
     *           attribute mixins (dedup / member-add /
     *           member-link / member-share-history) and the
     *           {@code subject} attribute via the
     *           {@code NamedSubjectOrUnnamedSubjectFallbackMixinGroup}.
     *           Cobalt mirrors the same shape inline.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsCreateRequest",
            exports = "makeCreateRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var createBuilder = new NodeBuilder()
                .description("create")
                .attribute("subject", subject);
        if (dedupAttr != null) {
            createBuilder.attribute("dedup", dedupAttr);
        }
        if (memberAddMode != null) {
            createBuilder.attribute("member_add_mode", memberAddMode);
        }
        if (memberLinkMode != null) {
            createBuilder.attribute("member_link_mode", memberLinkMode);
        }
        if (memberShareGroupHistoryMode != null) {
            createBuilder.attribute("member_share_group_history_mode", memberShareGroupHistoryMode);
        }
        var children = new ArrayList<Node>();
        for (var participant : participants) {
            children.add(participant.toNode());
        }
        if (descriptionBody != null || descriptionId != null) {
            var descriptionBuilder = new NodeBuilder()
                    .description("description");
            if (descriptionId != null) {
                descriptionBuilder.attribute("id", descriptionId);
            }
            if (descriptionBody != null) {
                var bodyNode = new NodeBuilder()
                        .description("body")
                        .content(descriptionBody.getBytes(StandardCharsets.UTF_8))
                        .build();
                descriptionBuilder.content(bodyNode);
            }
            children.add(descriptionBuilder.build());
        }
        if (locked) {
            children.add(new NodeBuilder().description("locked").build());
        }
        if (announcement) {
            children.add(new NodeBuilder().description("announcement").build());
        }
        if (parentDefaultMembershipApprovalMode) {
            var parentNode = new NodeBuilder()
                    .description("parent")
                    .attribute("default_membership_approval_mode", "request_required")
                    .build();
            children.add(parentNode);
        }
        if (noFrequentlyForwarded) {
            children.add(new NodeBuilder().description("no_frequently_forwarded").build());
        }
        if (ephemeralExpiration != null) {
            var ephemeralBuilder = new NodeBuilder()
                    .description("ephemeral")
                    .attribute("expiration", ephemeralExpiration);
            if (ephemeralTrigger != null) {
                ephemeralBuilder.attribute("trigger", ephemeralTrigger);
            }
            children.add(ephemeralBuilder.build());
        }
        if (membershipApprovalGroupJoinMode != null) {
            var membershipNode = new NodeBuilder()
                    .description("membership_approval_mode")
                    .attribute("group_join_mode", membershipApprovalGroupJoinMode)
                    .build();
            children.add(membershipNode);
        }
        if (breakout) {
            children.add(new NodeBuilder().description("breakout").build());
        }
        if (createdAsLid) {
            children.add(new NodeBuilder().description("created_as_lid").build());
        }
        if (addressingModeOverrideMode != null) {
            var addressingNode = new NodeBuilder()
                    .description("addressing_mode_override")
                    .attribute("mode", addressingModeOverrideMode)
                    .build();
            children.add(addressingNode);
        }
        if (linkedParentJid != null) {
            var linkedParentNode = new NodeBuilder()
                    .description("linked_parent")
                    .attribute("jid", linkedParentJid)
                    .build();
            children.add(linkedParentNode);
        }
        if (hiddenGroup) {
            children.add(new NodeBuilder().description("hidden_group").build());
        }
        if (allowNonAdminSubGroupCreation) {
            children.add(new NodeBuilder().description("allow_non_admin_sub_group_creation").build());
        }
        if (createGeneralChat) {
            children.add(new NodeBuilder().description("create_general_chat").build());
        }
        if (capi) {
            children.add(new NodeBuilder().description("capi").build());
        }
        createBuilder.content(children);
        var createNode = createBuilder.build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", JidServer.groupOrCommunity())
                .attribute("type", "set")
                .content(createNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsCreateRequest) obj;
        return this.locked == that.locked
                && this.announcement == that.announcement
                && this.noFrequentlyForwarded == that.noFrequentlyForwarded
                && this.breakout == that.breakout
                && this.createdAsLid == that.createdAsLid
                && this.hiddenGroup == that.hiddenGroup
                && this.allowNonAdminSubGroupCreation == that.allowNonAdminSubGroupCreation
                && this.createGeneralChat == that.createGeneralChat
                && this.capi == that.capi
                && this.parentDefaultMembershipApprovalMode == that.parentDefaultMembershipApprovalMode
                && Objects.equals(this.subject, that.subject)
                && Objects.equals(this.participants, that.participants)
                && Objects.equals(this.descriptionBody, that.descriptionBody)
                && Objects.equals(this.descriptionId, that.descriptionId)
                && Objects.equals(this.ephemeralExpiration, that.ephemeralExpiration)
                && Objects.equals(this.ephemeralTrigger, that.ephemeralTrigger)
                && Objects.equals(this.membershipApprovalGroupJoinMode, that.membershipApprovalGroupJoinMode)
                && Objects.equals(this.addressingModeOverrideMode, that.addressingModeOverrideMode)
                && Objects.equals(this.linkedParentJid, that.linkedParentJid)
                && Objects.equals(this.dedupAttr, that.dedupAttr)
                && Objects.equals(this.memberAddMode, that.memberAddMode)
                && Objects.equals(this.memberLinkMode, that.memberLinkMode)
                && Objects.equals(this.memberShareGroupHistoryMode, that.memberShareGroupHistoryMode);
    }

    @Override
    public int hashCode() {
        var primary = Objects.hash(subject, participants, descriptionBody, descriptionId, locked, announcement,
                parentDefaultMembershipApprovalMode, noFrequentlyForwarded, ephemeralExpiration,
                ephemeralTrigger, membershipApprovalGroupJoinMode, breakout, createdAsLid,
                addressingModeOverrideMode, linkedParentJid);
        var secondary = Objects.hash(hiddenGroup, allowNonAdminSubGroupCreation, createGeneralChat, capi,
                dedupAttr, memberAddMode, memberLinkMode, memberShareGroupHistoryMode);
        return primary * 31 + secondary;
    }

    @Override
    public String toString() {
        return "SmaxGroupsCreateRequest[subject=" + subject
                + ", participants=" + participants
                + ", descriptionBody=" + descriptionBody
                + ", descriptionId=" + descriptionId
                + ", locked=" + locked
                + ", announcement=" + announcement
                + ", parentDefaultMembershipApprovalMode=" + parentDefaultMembershipApprovalMode
                + ", noFrequentlyForwarded=" + noFrequentlyForwarded
                + ", ephemeralExpiration=" + ephemeralExpiration
                + ", ephemeralTrigger=" + ephemeralTrigger
                + ", membershipApprovalGroupJoinMode=" + membershipApprovalGroupJoinMode
                + ", breakout=" + breakout
                + ", createdAsLid=" + createdAsLid
                + ", addressingModeOverrideMode=" + addressingModeOverrideMode
                + ", linkedParentJid=" + linkedParentJid
                + ", hiddenGroup=" + hiddenGroup
                + ", allowNonAdminSubGroupCreation=" + allowNonAdminSubGroupCreation
                + ", createGeneralChat=" + createGeneralChat
                + ", capi=" + capi
                + ", dedupAttr=" + dedupAttr
                + ", memberAddMode=" + memberAddMode
                + ", memberLinkMode=" + memberLinkMode
                + ", memberShareGroupHistoryMode=" + memberShareGroupHistoryMode + ']';
    }

    /**
     * Single seed-participant entry inside the outbound
     * {@code <create/>} payload.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutGroupsCreateRequest")
    @WhatsAppWebModule(moduleName = "WASmaxOutGroupsPermissionTokenMixin")
    public static final class RequestParticipant {
        /**
         * The participant user JID.
         */
        private final Jid jid;

        /**
         * The optional phone-number JID (mixin).
         */
        private final Jid phoneNumber;

        /**
         * The optional username string (mixin).
         */
        private final String username;

        /**
         * The optional permission-token string attached as a mixin
         * attribute.
         */
        private final String permissionToken;

        /**
         * Constructs a participant entry.
         *
         * @param jid             the participant JID; never
         *                        {@code null}
         * @param phoneNumber     the optional phone-number JID; may
         *                        be {@code null}
         * @param username        the optional username; may be
         *                        {@code null}
         * @param permissionToken the optional permission token; may
         *                        be {@code null}
         * @throws NullPointerException if {@code jid} is
         *                              {@code null}
         */
        public RequestParticipant(Jid jid, Jid phoneNumber, String username, String permissionToken) {
            this.jid = Objects.requireNonNull(jid, "jid cannot be null");
            this.phoneNumber = phoneNumber;
            this.username = username;
            this.permissionToken = permissionToken;
        }

        /**
         * Returns the participant JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid jid() {
            return jid;
        }

        /**
         * Returns the optional phone-number JID.
         *
         * @return an {@link Optional} carrying the phone JID, or
         *         empty when omitted
         */
        public Optional<Jid> phoneNumber() {
            return Optional.ofNullable(phoneNumber);
        }

        /**
         * Returns the optional username.
         *
         * @return an {@link Optional} carrying the username, or
         *         empty when omitted
         */
        public Optional<String> username() {
            return Optional.ofNullable(username);
        }

        /**
         * Returns the optional permission token.
         *
         * @return an {@link Optional} carrying the token, or empty
         *         when omitted
         */
        public Optional<String> permissionToken() {
            return Optional.ofNullable(permissionToken);
        }

        /**
         * Builds the {@code <participant/>} child node.
         *
         * @return the materialised {@link Node}
         */
        public Node toNode() {
            var builder = new NodeBuilder()
                    .description("participant")
                    .attribute("jid", jid);
            if (phoneNumber != null) {
                builder.attribute("phone_number", phoneNumber);
            }
            if (username != null) {
                builder.attribute("username", username);
            }
            if (permissionToken != null) {
                builder.attribute("permission_token", permissionToken);
            }
            return builder.build();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (RequestParticipant) obj;
            return Objects.equals(this.jid, that.jid)
                    && Objects.equals(this.phoneNumber, that.phoneNumber)
                    && Objects.equals(this.username, that.username)
                    && Objects.equals(this.permissionToken, that.permissionToken);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid, phoneNumber, username, permissionToken);
        }

        @Override
        public String toString() {
            return "SmaxGroupsCreateRequest.RequestParticipant[jid=" + jid
                    + ", phoneNumber=" + phoneNumber
                    + ", username=" + username
                    + ", permissionToken=" + permissionToken + ']';
        }
    }

    /**
     * Fluent builder for {@link SmaxGroupsCreateRequest}.
     *
     * <p>Mandatory inputs are the {@code subject} and at least one
     * participant via {@link #addParticipant(RequestParticipant)};
     * every other setter is optional. Call {@link #build()} once
     * the desired toggles have been chosen.
     */
    public static final class Builder {
        /**
         * The accumulating subject.
         */
        private String subject;

        /**
         * The accumulating participants list.
         */
        private final List<RequestParticipant> participants = new ArrayList<>();

        /**
         * Optional description body.
         */
        private String descriptionBody;

        /**
         * Optional description ID.
         */
        private String descriptionId;

        /**
         * Locked toggle.
         */
        private boolean locked;

        /**
         * Announcement toggle.
         */
        private boolean announcement;

        /**
         * Whether to attach the parent-default
         * membership-approval marker.
         */
        private boolean parentDefaultMembershipApprovalMode;

        /**
         * No-frequently-forwarded toggle.
         */
        private boolean noFrequentlyForwarded;

        /**
         * Optional ephemeral expiration (seconds).
         */
        private Integer ephemeralExpiration;

        /**
         * Optional ephemeral trigger value.
         */
        private Integer ephemeralTrigger;

        /**
         * Optional membership-approval join-mode.
         */
        private String membershipApprovalGroupJoinMode;

        /**
         * Breakout toggle.
         */
        private boolean breakout;

        /**
         * Created-as-lid toggle.
         */
        private boolean createdAsLid;

        /**
         * Optional addressing-mode-override value.
         */
        private String addressingModeOverrideMode;

        /**
         * Optional linked parent community JID.
         */
        private Jid linkedParentJid;

        /**
         * Hidden-group toggle.
         */
        private boolean hiddenGroup;

        /**
         * Allow-non-admin-sub-group-creation toggle.
         */
        private boolean allowNonAdminSubGroupCreation;

        /**
         * Create-general-chat toggle.
         */
        private boolean createGeneralChat;

        /**
         * Capi toggle.
         */
        private boolean capi;

        /**
         * Optional dedup token.
         */
        private String dedupAttr;

        /**
         * Optional member-add-mode value.
         */
        private String memberAddMode;

        /**
         * Optional member-link-mode value.
         */
        private String memberLinkMode;

        /**
         * Optional member-share-group-history-mode value.
         */
        private String memberShareGroupHistoryMode;

        /**
         * Constructs a fresh builder. Use {@link SmaxGroupsCreateRequest#builder()}
         * for the canonical entry point.
         */
        public Builder() {
        }

        /**
         * Sets the group subject.
         *
         * @param subject the subject text; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code subject} is
         *                              {@code null}
         */
        public Builder subject(String subject) {
            this.subject = Objects.requireNonNull(subject, "subject cannot be null");
            return this;
        }

        /**
         * Appends a single participant.
         *
         * @param participant the participant; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code participant} is
         *                              {@code null}
         */
        public Builder addParticipant(RequestParticipant participant) {
            Objects.requireNonNull(participant, "participant cannot be null");
            this.participants.add(participant);
            return this;
        }

        /**
         * Appends every participant from the supplied collection.
         *
         * @param entries the participants to append; never
         *                {@code null}
         * @return this builder
         * @throws NullPointerException if {@code entries} or any
         *                              entry is {@code null}
         */
        public Builder addParticipants(List<RequestParticipant> entries) {
            Objects.requireNonNull(entries, "entries cannot be null");
            for (var entry : entries) {
                addParticipant(entry);
            }
            return this;
        }

        /**
         * Sets the optional description body.
         *
         * @param descriptionBody the body text; may be
         *                        {@code null}
         * @return this builder
         */
        public Builder descriptionBody(String descriptionBody) {
            this.descriptionBody = descriptionBody;
            return this;
        }

        /**
         * Sets the optional description ID attribute.
         *
         * @param descriptionId the description ID; may be
         *                      {@code null}
         * @return this builder
         */
        public Builder descriptionId(String descriptionId) {
            this.descriptionId = descriptionId;
            return this;
        }

        /**
         * Sets the locked toggle.
         *
         * @param locked the desired flag value
         * @return this builder
         */
        public Builder locked(boolean locked) {
            this.locked = locked;
            return this;
        }

        /**
         * Sets the announcement toggle.
         *
         * @param announcement the desired flag value
         * @return this builder
         */
        public Builder announcement(boolean announcement) {
            this.announcement = announcement;
            return this;
        }

        /**
         * Sets whether the parent-default membership-approval
         * marker is attached.
         *
         * @param flag the desired flag value
         * @return this builder
         */
        public Builder parentDefaultMembershipApprovalMode(boolean flag) {
            this.parentDefaultMembershipApprovalMode = flag;
            return this;
        }

        /**
         * Sets the no-frequently-forwarded toggle.
         *
         * @param flag the desired flag value
         * @return this builder
         */
        public Builder noFrequentlyForwarded(boolean flag) {
            this.noFrequentlyForwarded = flag;
            return this;
        }

        /**
         * Sets the optional ephemeral expiration in seconds.
         *
         * @param expiration the expiration value; may be
         *                   {@code null} to omit the
         *                   {@code <ephemeral/>} child
         * @return this builder
         */
        public Builder ephemeralExpiration(Integer expiration) {
            this.ephemeralExpiration = expiration;
            return this;
        }

        /**
         * Sets the optional ephemeral trigger value.
         *
         * @param trigger the trigger value; may be {@code null}
         * @return this builder
         */
        public Builder ephemeralTrigger(Integer trigger) {
            this.ephemeralTrigger = trigger;
            return this;
        }

        /**
         * Sets the optional membership-approval join-mode.
         *
         * @param mode the mode value; may be {@code null}
         * @return this builder
         */
        public Builder membershipApprovalGroupJoinMode(String mode) {
            this.membershipApprovalGroupJoinMode = mode;
            return this;
        }

        /**
         * Sets the breakout toggle.
         *
         * @param flag the desired flag value
         * @return this builder
         */
        public Builder breakout(boolean flag) {
            this.breakout = flag;
            return this;
        }

        /**
         * Sets the created-as-lid toggle.
         *
         * @param flag the desired flag value
         * @return this builder
         */
        public Builder createdAsLid(boolean flag) {
            this.createdAsLid = flag;
            return this;
        }

        /**
         * Sets the optional addressing-mode-override value.
         *
         * @param mode the mode value; may be {@code null}
         * @return this builder
         */
        public Builder addressingModeOverrideMode(String mode) {
            this.addressingModeOverrideMode = mode;
            return this;
        }

        /**
         * Sets the optional parent community JID.
         *
         * @param jid the parent JID; may be {@code null}
         * @return this builder
         */
        public Builder linkedParentJid(Jid jid) {
            this.linkedParentJid = jid;
            return this;
        }

        /**
         * Sets the hidden-group toggle.
         *
         * @param flag the desired flag value
         * @return this builder
         */
        public Builder hiddenGroup(boolean flag) {
            this.hiddenGroup = flag;
            return this;
        }

        /**
         * Sets the allow-non-admin-sub-group-creation toggle.
         *
         * @param flag the desired flag value
         * @return this builder
         */
        public Builder allowNonAdminSubGroupCreation(boolean flag) {
            this.allowNonAdminSubGroupCreation = flag;
            return this;
        }

        /**
         * Sets the create-general-chat toggle.
         *
         * @param flag the desired flag value
         * @return this builder
         */
        public Builder createGeneralChat(boolean flag) {
            this.createGeneralChat = flag;
            return this;
        }

        /**
         * Sets the capi toggle.
         *
         * @param flag the desired flag value
         * @return this builder
         */
        public Builder capi(boolean flag) {
            this.capi = flag;
            return this;
        }

        /**
         * Sets the optional dedup token attribute.
         *
         * @param dedupAttr the dedup token; may be {@code null}
         * @return this builder
         */
        public Builder dedupAttr(String dedupAttr) {
            this.dedupAttr = dedupAttr;
            return this;
        }

        /**
         * Sets the optional member-add-mode attribute.
         *
         * @param mode the mode value; may be {@code null}
         * @return this builder
         */
        public Builder memberAddMode(String mode) {
            this.memberAddMode = mode;
            return this;
        }

        /**
         * Sets the optional member-link-mode attribute.
         *
         * @param mode the mode value; may be {@code null}
         * @return this builder
         */
        public Builder memberLinkMode(String mode) {
            this.memberLinkMode = mode;
            return this;
        }

        /**
         * Sets the optional member-share-group-history-mode
         * attribute.
         *
         * @param mode the mode value; may be {@code null}
         * @return this builder
         */
        public Builder memberShareGroupHistoryMode(String mode) {
            this.memberShareGroupHistoryMode = mode;
            return this;
        }

        /**
         * Materialises a {@link SmaxGroupsCreateRequest}.
         *
         * @return the constructed request; never {@code null}
         * @throws NullPointerException     if the subject was never
         *                                  set
         * @throws IllegalArgumentException when no participants
         *                                  were added
         */
        public SmaxGroupsCreateRequest build() {
            Objects.requireNonNull(subject, "subject must be set before build()");
            if (participants.isEmpty()) {
                throw new IllegalArgumentException("at least one participant must be added before build()");
            }
            return new SmaxGroupsCreateRequest(subject, participants, descriptionBody, descriptionId, locked, announcement,
                    parentDefaultMembershipApprovalMode, noFrequentlyForwarded, ephemeralExpiration,
                    ephemeralTrigger, membershipApprovalGroupJoinMode, breakout, createdAsLid,
                    addressingModeOverrideMode, linkedParentJid, hiddenGroup, allowNonAdminSubGroupCreation,
                    createGeneralChat, capi, dedupAttr, memberAddMode, memberLinkMode,
                    memberShareGroupHistoryMode);
        }
    }
}
