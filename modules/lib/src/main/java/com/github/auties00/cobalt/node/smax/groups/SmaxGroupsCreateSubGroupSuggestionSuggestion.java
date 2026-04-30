package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
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
 * Sealed alternation modelling the suggestion-body oneof.
 *
 * @implNote {@code WASmaxOutGroupsSuggestionForCreateSubGroupSuggestionNewGroupOrCreateSubGroupSuggestionExistingGroupsMixinGroup.mergeSuggestionForCreateSubGroupSuggestionNewGroupOrCreateSubGroupSuggestionExistingGroupsMixinGroup}
 *           branches on {@code createSubGroupSuggestionSuggestionForNewGroup}
 *           vs
 *           {@code createSubGroupSuggestionSuggestionForExistingGroups}.
 *           Cobalt models the branch with this sealed interface
 *           permitting {@link NewGroup} and {@link ExistingGroups}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsSuggestionForCreateSubGroupSuggestionNewGroupOrCreateSubGroupSuggestionExistingGroupsMixinGroup")
public sealed interface SmaxGroupsCreateSubGroupSuggestionSuggestion permits SmaxGroupsCreateSubGroupSuggestionSuggestion.NewGroup, SmaxGroupsCreateSubGroupSuggestionSuggestion.ExistingGroups {
    /**
     * Merges this suggestion's children/attributes into the supplied
     * {@code <sub_group_suggestion/>} {@link NodeBuilder}.
     *
     * @param builder the target builder; never {@code null}
     */
    void contributeTo(NodeBuilder builder);

    /**
     * Suggestion body for a brand-new sub-group spun up inside the
     * parent community.
     *
     * @implNote {@code WASmaxOutGroupsCreateSubGroupSuggestionSuggestionForNewGroupMixin.mergeCreateSubGroupSuggestionSuggestionForNewGroupMixin}
     *           emits {@code <subject>VALUE</subject>}, plus optional
     *           {@code <description>} (with optional inner
     *           {@code <body/>}), {@code <locked/>},
     *           {@code <announcement/>}, {@code <hidden_group/>},
     *           {@code <membership_approval_mode/>}; the optional
     *           member-add / link / share-history mixins are merged
     *           onto the root attributes. Cobalt collapses the
     *           remaining mixins into typed fields.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutGroupsCreateSubGroupSuggestionSuggestionForNewGroupMixin")
    final class NewGroup implements SmaxGroupsCreateSubGroupSuggestionSuggestion {
        /**
         * The subject (display name) of the proposed sub-group.
         */
        private final String subject;

        /**
         * The optional description body; {@code null} omits the
         * {@code <description><body/></description>} child entirely.
         */
        private final String descriptionBody;

        /**
         * Whether to attach a {@code <locked/>} marker (chat-info
         * edits become admin-only).
         */
        private final boolean locked;

        /**
         * Whether to attach an {@code <announcement/>} marker (only
         * admins may post).
         */
        private final boolean announcement;

        /**
         * Whether to attach a {@code <hidden_group/>} marker (the
         * sub-group is hidden from the community directory).
         */
        private final boolean hiddenGroup;

        /**
         * The optional membership-approval join-mode attribute;
         * {@code null} omits the
         * {@code <membership_approval_mode/>} child entirely.
         */
        private final String membershipApprovalGroupJoinMode;

        /**
         * The optional member-add-mode mixin attribute attached to
         * the {@code <sub_group_suggestion/>} root.
         */
        private final String memberAddMode;

        /**
         * The optional member-link-mode mixin attribute attached to
         * the {@code <sub_group_suggestion/>} root.
         */
        private final String memberLinkMode;

        /**
         * The optional member-share-group-history-mode mixin
         * attribute attached to the {@code <sub_group_suggestion/>}
         * root.
         */
        private final String memberShareGroupHistoryMode;

        /**
         * Constructs a new-group suggestion body.
         *
         * @param subject                          the subject; never
         *                                         {@code null}
         * @param descriptionBody                  the optional
         *                                         description body;
         *                                         may be {@code null}
         * @param locked                           see
         *                                         {@link #locked()}
         * @param announcement                     see
         *                                         {@link #announcement()}
         * @param hiddenGroup                      see
         *                                         {@link #hiddenGroup()}
         * @param membershipApprovalGroupJoinMode  the optional
         *                                         membership-approval
         *                                         join-mode value;
         *                                         may be {@code null}
         * @param memberAddMode                    the optional
         *                                         member-add mode;
         *                                         may be {@code null}
         * @param memberLinkMode                   the optional
         *                                         member-link mode;
         *                                         may be {@code null}
         * @param memberShareGroupHistoryMode      the optional
         *                                         member-share-history
         *                                         mode; may be
         *                                         {@code null}
         * @throws NullPointerException if {@code subject} is
         *                              {@code null}
         */
        public NewGroup(String subject,
                        String descriptionBody,
                        boolean locked,
                        boolean announcement,
                        boolean hiddenGroup,
                        String membershipApprovalGroupJoinMode,
                        String memberAddMode,
                        String memberLinkMode,
                        String memberShareGroupHistoryMode) {
            this.subject = Objects.requireNonNull(subject, "subject cannot be null");
            this.descriptionBody = descriptionBody;
            this.locked = locked;
            this.announcement = announcement;
            this.hiddenGroup = hiddenGroup;
            this.membershipApprovalGroupJoinMode = membershipApprovalGroupJoinMode;
            this.memberAddMode = memberAddMode;
            this.memberLinkMode = memberLinkMode;
            this.memberShareGroupHistoryMode = memberShareGroupHistoryMode;
        }

        /**
         * Returns the subject text.
         *
         * @return the subject; never {@code null}
         */
        public String subject() {
            return subject;
        }

        /**
         * Returns the optional description body.
         *
         * @return an {@link Optional} carrying the description body,
         *         or empty when omitted
         */
        public Optional<String> descriptionBody() {
            return Optional.ofNullable(descriptionBody);
        }

        /**
         * Returns whether the {@code <locked/>} marker is attached.
         *
         * @return {@code true} when the marker is emitted
         */
        public boolean locked() {
            return locked;
        }

        /**
         * Returns whether the {@code <announcement/>} marker is
         * attached.
         *
         * @return {@code true} when the marker is emitted
         */
        public boolean announcement() {
            return announcement;
        }

        /**
         * Returns whether the {@code <hidden_group/>} marker is
         * attached.
         *
         * @return {@code true} when the marker is emitted
         */
        public boolean hiddenGroup() {
            return hiddenGroup;
        }

        /**
         * Returns the optional membership-approval join-mode value.
         *
         * @return an {@link Optional} carrying the join-mode value,
         *         or empty when omitted
         */
        public Optional<String> membershipApprovalGroupJoinMode() {
            return Optional.ofNullable(membershipApprovalGroupJoinMode);
        }

        /**
         * Returns the optional member-add-mode mixin value.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when omitted
         */
        public Optional<String> memberAddMode() {
            return Optional.ofNullable(memberAddMode);
        }

        /**
         * Returns the optional member-link-mode mixin value.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when omitted
         */
        public Optional<String> memberLinkMode() {
            return Optional.ofNullable(memberLinkMode);
        }

        /**
         * Returns the optional member-share-group-history-mode mixin
         * value.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when omitted
         */
        public Optional<String> memberShareGroupHistoryMode() {
            return Optional.ofNullable(memberShareGroupHistoryMode);
        }

        @Override
        public void contributeTo(NodeBuilder builder) {
            Objects.requireNonNull(builder, "builder cannot be null");
            if (memberAddMode != null) {
                builder.attribute("member_add_mode", memberAddMode);
            }
            if (memberLinkMode != null) {
                builder.attribute("member_link_mode", memberLinkMode);
            }
            if (memberShareGroupHistoryMode != null) {
                builder.attribute("member_share_group_history_mode", memberShareGroupHistoryMode);
            }
            var children = new ArrayList<Node>();
            var subjectNode = new NodeBuilder()
                    .description("subject")
                    .content(subject.getBytes(StandardCharsets.UTF_8))
                    .build();
            children.add(subjectNode);
            if (descriptionBody != null) {
                var bodyNode = new NodeBuilder()
                        .description("body")
                        .content(descriptionBody.getBytes(StandardCharsets.UTF_8))
                        .build();
                var descriptionNode = new NodeBuilder()
                        .description("description")
                        .content(bodyNode)
                        .build();
                children.add(descriptionNode);
            }
            if (locked) {
                children.add(new NodeBuilder().description("locked").build());
            }
            if (announcement) {
                children.add(new NodeBuilder().description("announcement").build());
            }
            if (hiddenGroup) {
                children.add(new NodeBuilder().description("hidden_group").build());
            }
            if (membershipApprovalGroupJoinMode != null) {
                var membershipNode = new NodeBuilder()
                        .description("membership_approval_mode")
                        .attribute("group_join_mode", membershipApprovalGroupJoinMode)
                        .build();
                children.add(membershipNode);
            }
            builder.content(children);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (NewGroup) obj;
            return this.locked == that.locked
                    && this.announcement == that.announcement
                    && this.hiddenGroup == that.hiddenGroup
                    && Objects.equals(this.subject, that.subject)
                    && Objects.equals(this.descriptionBody, that.descriptionBody)
                    && Objects.equals(this.membershipApprovalGroupJoinMode, that.membershipApprovalGroupJoinMode)
                    && Objects.equals(this.memberAddMode, that.memberAddMode)
                    && Objects.equals(this.memberLinkMode, that.memberLinkMode)
                    && Objects.equals(this.memberShareGroupHistoryMode, that.memberShareGroupHistoryMode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(subject, descriptionBody, locked, announcement, hiddenGroup,
                    membershipApprovalGroupJoinMode, memberAddMode, memberLinkMode,
                    memberShareGroupHistoryMode);
        }

        @Override
        public String toString() {
            return "SmaxGroupsCreateSubGroupSuggestionSuggestion.NewGroup[subject=" + subject
                    + ", descriptionBody=" + descriptionBody
                    + ", locked=" + locked
                    + ", announcement=" + announcement
                    + ", hiddenGroup=" + hiddenGroup
                    + ", membershipApprovalGroupJoinMode=" + membershipApprovalGroupJoinMode
                    + ", memberAddMode=" + memberAddMode
                    + ", memberLinkMode=" + memberLinkMode
                    + ", memberShareGroupHistoryMode=" + memberShareGroupHistoryMode + ']';
        }
    }

    /**
     * Suggestion body recommending that one or more existing groups
     * be linked into the parent community as sub-groups.
     *
     * @implNote {@code WASmaxOutGroupsCreateSubGroupSuggestionSuggestionForExistingGroupsMixin.mergeCreateSubGroupSuggestionSuggestionForExistingGroupsMixin}
     *           emits {@code REPEATED_CHILD(<group jid [hidden_group]>)}
     *           inside the {@code <sub_group_suggestion/>} root.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutGroupsCreateSubGroupSuggestionSuggestionForExistingGroupsMixin")
    final class ExistingGroups implements SmaxGroupsCreateSubGroupSuggestionSuggestion {
        /**
         * The candidate groups to suggest. Must be non-empty (1..1000
         * entries server-side).
         */
        private final List<Candidate> groups;

        /**
         * Constructs an existing-groups suggestion body.
         *
         * @param groups the list of candidate groups; never
         *               {@code null} and must be non-empty
         * @throws NullPointerException     if {@code groups} is
         *                                  {@code null}
         * @throws IllegalArgumentException when {@code groups} is
         *                                  empty
         */
        public ExistingGroups(List<Candidate> groups) {
            Objects.requireNonNull(groups, "groups cannot be null");
            if (groups.isEmpty()) {
                throw new IllegalArgumentException("groups must contain at least one entry");
            }
            this.groups = List.copyOf(groups);
        }

        /**
         * Returns the candidate groups.
         *
         * @return an unmodifiable list of candidate groups; never
         *         empty
         */
        public List<Candidate> groups() {
            return groups;
        }

        @Override
        public void contributeTo(NodeBuilder builder) {
            Objects.requireNonNull(builder, "builder cannot be null");
            var groupNodes = new ArrayList<Node>();
            for (var candidate : groups) {
                var groupBuilder = new NodeBuilder()
                        .description("group")
                        .attribute("jid", candidate.jid());
                if (candidate.hiddenGroup()) {
                    var hiddenNode = new NodeBuilder()
                            .description("hidden_group")
                            .build();
                    groupBuilder.content(hiddenNode);
                }
                groupNodes.add(groupBuilder.build());
            }
            builder.content(groupNodes);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ExistingGroups) obj;
            return Objects.equals(this.groups, that.groups);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groups);
        }

        @Override
        public String toString() {
            return "SmaxGroupsCreateSubGroupSuggestionSuggestion.ExistingGroups[groups=" + groups + ']';
        }

        /**
         * Single candidate sub-group entry inside an
         * {@link ExistingGroups} suggestion.
         */
        @WhatsAppWebModule(moduleName = "WASmaxOutGroupsCreateSubGroupSuggestionSuggestionForExistingGroupsMixin")
        public static final class Candidate {
            /**
             * The candidate sub-group JID.
             */
            private final Jid jid;

            /**
             * Whether to attach a {@code <hidden_group/>} marker to
             * the {@code <group/>} child.
             */
            private final boolean hiddenGroup;

            /**
             * Constructs a candidate entry.
             *
             * @param jid         the candidate JID; never
             *                    {@code null}
             * @param hiddenGroup whether to attach the hidden marker
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public Candidate(Jid jid, boolean hiddenGroup) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.hiddenGroup = hiddenGroup;
            }

            /**
             * Returns the candidate JID.
             *
             * @return the candidate JID; never {@code null}
             */
            public Jid jid() {
                return jid;
            }

            /**
             * Returns whether the {@code <hidden_group/>} marker is
             * attached.
             *
             * @return {@code true} when the marker is emitted
             */
            public boolean hiddenGroup() {
                return hiddenGroup;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Candidate) obj;
                return this.hiddenGroup == that.hiddenGroup
                        && Objects.equals(this.jid, that.jid);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, hiddenGroup);
            }

            @Override
            public String toString() {
                return "SmaxGroupsCreateSubGroupSuggestionSuggestion.ExistingGroups.Candidate[jid=" + jid
                        + ", hiddenGroup=" + hiddenGroup + ']';
            }
        }
    }
}
