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
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGroupsCreateRequest}.
 */
public sealed interface SmaxGroupsCreateResponse extends SmaxOperation.Response
        permits SmaxGroupsCreateResponse.Success, SmaxGroupsCreateResponse.GroupAlreadyExists,
                SmaxGroupsCreateResponse.ClientError, SmaxGroupsCreateResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsCreateResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to
     *                validate echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsCreateRPC",
            exports = "sendCreateRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsCreateResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var alreadyExists = GroupAlreadyExists.of(node, request);
        if (alreadyExists.isPresent()) {
            return alreadyExists;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant — the relay materialised the
     * group and returned its full metadata.
     *
     * <p>Carries the typed identity triple
     * ({@code groupId}, {@code groupCreator}, {@code groupCreation}),
     * the optional sync-token / sync-owner pair
     * ({@code groupST}/{@code groupSO}), every
     * {@code <create/>} child the relay echoed, and the
     * non-empty list of participant rows.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsCreateResponseSuccess")
    final class Success implements SmaxGroupsCreateResponse {
        /**
         * The new group's user-component ID.
         */
        private final String groupId;

        /**
         * The full group JID (built from
         * {@code groupId@g.us}).
         */
        private final Jid groupJid;

        /**
         * The user who created the group.
         */
        private final Jid groupCreator;

        /**
         * The creation timestamp (seconds since epoch).
         */
        private final long groupCreation;

        /**
         * The optional creation sync-time mixin (s_t).
         */
        private final Long groupSyncTime;

        /**
         * The optional sync-owner mixin (s_o).
         */
        private final Jid groupSyncOwner;

        /**
         * The new group's subject.
         */
        private final String subject;

        /**
         * The optional description ID echoed by the relay.
         */
        private final String descriptionId;

        /**
         * The optional description-error string echoed by the relay
         * (e.g. {@code "406"} or {@code "500"}); {@code null} when
         * the description committed cleanly.
         */
        private final String descriptionError;

        /**
         * Whether the relay echoed a {@code <locked/>} child.
         */
        private final boolean locked;

        /**
         * Whether the relay echoed an {@code <announcement/>}
         * child.
         */
        private final boolean announcement;

        /**
         * Whether the relay echoed a {@code <parent/>} child.
         */
        private final boolean parent;

        /**
         * Whether the relay echoed a
         * {@code <no_frequently_forwarded/>} child.
         */
        private final boolean noFrequentlyForwarded;

        /**
         * The optional ephemeral expiration echoed by the relay.
         */
        private final Integer ephemeralExpiration;

        /**
         * The optional ephemeral trigger echoed by the relay.
         */
        private final Integer ephemeralTrigger;

        /**
         * Whether the relay echoed a
         * {@code <membership_approval_mode/>} child.
         */
        private final boolean membershipApprovalMode;

        /**
         * Whether the relay echoed a {@code <breakout/>} child.
         */
        private final boolean breakout;

        /**
         * The optional linked parent community JID echoed by the
         * relay.
         */
        private final Jid linkedParentJid;

        /**
         * Whether the relay echoed a {@code <hidden_group/>} child.
         */
        private final boolean hiddenGroup;

        /**
         * Whether the relay echoed an
         * {@code <allow_non_admin_sub_group_creation/>} child.
         */
        private final boolean allowNonAdminSubGroupCreation;

        /**
         * Whether the relay echoed a {@code <group_history/>}
         * child.
         */
        private final boolean groupHistory;

        /**
         * Whether the relay echoed a {@code <capi/>} child.
         */
        private final boolean capi;

        /**
         * The seed-participant rows.
         */
        private final List<ResponseParticipant> participants;

        /**
         * The raw {@code <group/>} child carrying the remaining
         * mixin metadata that Cobalt does not yet project (addressing
         * mode, subject-owner identity, member-add / link / share
         * history mixins, dedup attribute echo).
         */
        private final Node group;

        /**
         * Constructs a success reply.
         *
         * @param groupId                       the user-component ID;
         *                                      never {@code null}
         * @param groupJid                      the full group JID;
         *                                      never {@code null}
         * @param groupCreator                  the creator JID;
         *                                      never {@code null}
         * @param groupCreation                 the creation timestamp
         * @param groupSyncTime                 the optional s_t
         *                                      mixin; may be
         *                                      {@code null}
         * @param groupSyncOwner                the optional s_o
         *                                      mixin; may be
         *                                      {@code null}
         * @param subject                       the subject; never
         *                                      {@code null}
         * @param descriptionId                 the optional
         *                                      description ID; may
         *                                      be {@code null}
         * @param descriptionError              the optional
         *                                      description-error
         *                                      string; may be
         *                                      {@code null}
         * @param locked                        whether
         *                                      {@code <locked/>}
         *                                      was echoed
         * @param announcement                  whether
         *                                      {@code <announcement/>}
         *                                      was echoed
         * @param parent                        whether
         *                                      {@code <parent/>}
         *                                      was echoed
         * @param noFrequentlyForwarded         whether
         *                                      {@code <no_frequently_forwarded/>}
         *                                      was echoed
         * @param ephemeralExpiration           the optional
         *                                      ephemeral expiration;
         *                                      may be {@code null}
         * @param ephemeralTrigger              the optional
         *                                      ephemeral trigger;
         *                                      may be {@code null}
         * @param membershipApprovalMode        whether
         *                                      {@code <membership_approval_mode/>}
         *                                      was echoed
         * @param breakout                      whether
         *                                      {@code <breakout/>}
         *                                      was echoed
         * @param linkedParentJid               the optional linked
         *                                      parent community JID;
         *                                      may be {@code null}
         * @param hiddenGroup                   whether
         *                                      {@code <hidden_group/>}
         *                                      was echoed
         * @param allowNonAdminSubGroupCreation whether
         *                                      {@code <allow_non_admin_sub_group_creation/>}
         *                                      was echoed
         * @param groupHistory                  whether
         *                                      {@code <group_history/>}
         *                                      was echoed
         * @param capi                          whether
         *                                      {@code <capi/>} was
         *                                      echoed
         * @param participants                  the seed-participant
         *                                      rows; never
         *                                      {@code null} and must
         *                                      be non-empty
         * @param group                         the raw
         *                                      {@code <group/>}
         *                                      sub-node; never
         *                                      {@code null}
         * @throws NullPointerException     if any non-nullable
         *                                  argument is {@code null}
         * @throws IllegalArgumentException when {@code participants}
         *                                  is empty
         */
        public Success(String groupId,
                       Jid groupJid,
                       Jid groupCreator,
                       long groupCreation,
                       Long groupSyncTime,
                       Jid groupSyncOwner,
                       String subject,
                       String descriptionId,
                       String descriptionError,
                       boolean locked,
                       boolean announcement,
                       boolean parent,
                       boolean noFrequentlyForwarded,
                       Integer ephemeralExpiration,
                       Integer ephemeralTrigger,
                       boolean membershipApprovalMode,
                       boolean breakout,
                       Jid linkedParentJid,
                       boolean hiddenGroup,
                       boolean allowNonAdminSubGroupCreation,
                       boolean groupHistory,
                       boolean capi,
                       List<ResponseParticipant> participants,
                       Node group) {
            this.groupId = Objects.requireNonNull(groupId, "groupId cannot be null");
            this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
            this.groupCreator = Objects.requireNonNull(groupCreator, "groupCreator cannot be null");
            this.groupCreation = groupCreation;
            this.groupSyncTime = groupSyncTime;
            this.groupSyncOwner = groupSyncOwner;
            this.subject = Objects.requireNonNull(subject, "subject cannot be null");
            this.descriptionId = descriptionId;
            this.descriptionError = descriptionError;
            this.locked = locked;
            this.announcement = announcement;
            this.parent = parent;
            this.noFrequentlyForwarded = noFrequentlyForwarded;
            this.ephemeralExpiration = ephemeralExpiration;
            this.ephemeralTrigger = ephemeralTrigger;
            this.membershipApprovalMode = membershipApprovalMode;
            this.breakout = breakout;
            this.linkedParentJid = linkedParentJid;
            this.hiddenGroup = hiddenGroup;
            this.allowNonAdminSubGroupCreation = allowNonAdminSubGroupCreation;
            this.groupHistory = groupHistory;
            this.capi = capi;
            Objects.requireNonNull(participants, "participants cannot be null");
            if (participants.isEmpty()) {
                throw new IllegalArgumentException("participants must contain at least one entry");
            }
            this.participants = List.copyOf(participants);
            this.group = Objects.requireNonNull(group, "group cannot be null");
        }

        /**
         * Returns the new group's user-component ID.
         *
         * @return the group ID; never {@code null}
         */
        public String groupId() {
            return groupId;
        }

        /**
         * Returns the full group JID.
         *
         * @return the group JID; never {@code null}
         */
        public Jid groupJid() {
            return groupJid;
        }

        /**
         * Returns the creator JID.
         *
         * @return the creator JID; never {@code null}
         */
        public Jid groupCreator() {
            return groupCreator;
        }

        /**
         * Returns the creation timestamp.
         *
         * @return the creation timestamp (seconds since epoch)
         */
        public long groupCreation() {
            return groupCreation;
        }

        /**
         * Returns the optional s_t mixin value.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when omitted
         */
        public Optional<Long> groupSyncTime() {
            return Optional.ofNullable(groupSyncTime);
        }

        /**
         * Returns the optional s_o mixin JID.
         *
         * @return an {@link Optional} carrying the JID, or empty
         *         when omitted
         */
        public Optional<Jid> groupSyncOwner() {
            return Optional.ofNullable(groupSyncOwner);
        }

        /**
         * Returns the subject.
         *
         * @return the subject; never {@code null}
         */
        public String subject() {
            return subject;
        }

        /**
         * Returns the optional description ID.
         *
         * @return an {@link Optional} carrying the ID, or empty
         *         when omitted
         */
        public Optional<String> descriptionId() {
            return Optional.ofNullable(descriptionId);
        }

        /**
         * Returns the optional description-error string echoed by
         * the relay.
         *
         * @return an {@link Optional} carrying the error string, or
         *         empty when the description committed cleanly
         */
        public Optional<String> descriptionError() {
            return Optional.ofNullable(descriptionError);
        }

        /**
         * Returns whether the {@code <locked/>} child was echoed.
         *
         * @return {@code true} when the marker is present
         */
        public boolean locked() {
            return locked;
        }

        /**
         * Returns whether the {@code <announcement/>} child was
         * echoed.
         *
         * @return {@code true} when the marker is present
         */
        public boolean announcement() {
            return announcement;
        }

        /**
         * Returns whether the {@code <parent/>} child was echoed.
         *
         * @return {@code true} when the marker is present
         */
        public boolean parent() {
            return parent;
        }

        /**
         * Returns whether the
         * {@code <no_frequently_forwarded/>} child was echoed.
         *
         * @return {@code true} when the marker is present
         */
        public boolean noFrequentlyForwarded() {
            return noFrequentlyForwarded;
        }

        /**
         * Returns the optional ephemeral expiration echoed by the
         * relay.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when omitted
         */
        public Optional<Integer> ephemeralExpiration() {
            return Optional.ofNullable(ephemeralExpiration);
        }

        /**
         * Returns the optional ephemeral trigger echoed by the
         * relay.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when omitted
         */
        public Optional<Integer> ephemeralTrigger() {
            return Optional.ofNullable(ephemeralTrigger);
        }

        /**
         * Returns whether the
         * {@code <membership_approval_mode/>} child was echoed.
         *
         * @return {@code true} when the marker is present
         */
        public boolean membershipApprovalMode() {
            return membershipApprovalMode;
        }

        /**
         * Returns whether the {@code <breakout/>} child was echoed.
         *
         * @return {@code true} when the marker is present
         */
        public boolean breakout() {
            return breakout;
        }

        /**
         * Returns the optional linked parent community JID.
         *
         * @return an {@link Optional} carrying the JID, or empty
         *         when omitted
         */
        public Optional<Jid> linkedParentJid() {
            return Optional.ofNullable(linkedParentJid);
        }

        /**
         * Returns whether the {@code <hidden_group/>} child was
         * echoed.
         *
         * @return {@code true} when the marker is present
         */
        public boolean hiddenGroup() {
            return hiddenGroup;
        }

        /**
         * Returns whether the
         * {@code <allow_non_admin_sub_group_creation/>} child was
         * echoed.
         *
         * @return {@code true} when the marker is present
         */
        public boolean allowNonAdminSubGroupCreation() {
            return allowNonAdminSubGroupCreation;
        }

        /**
         * Returns whether the {@code <group_history/>} child was
         * echoed.
         *
         * @return {@code true} when the marker is present
         */
        public boolean groupHistory() {
            return groupHistory;
        }

        /**
         * Returns whether the {@code <capi/>} child was echoed.
         *
         * @return {@code true} when the marker is present
         */
        public boolean capi() {
            return capi;
        }

        /**
         * Returns the seed-participant rows.
         *
         * @return an unmodifiable list of participant rows; never
         *         empty
         */
        public List<ResponseParticipant> participants() {
            return participants;
        }

        /**
         * Returns the raw {@code <group/>} sub-node.
         *
         * <p>Exposed for callers that need to read the secondary
         * mixin metadata (addressing-mode, subject-owner identity,
         * member-add / link / share-history) that Cobalt does not
         * yet project as typed accessors.
         *
         * @return the raw {@code <group/>} node; never {@code null}
         */
        public Node group() {
            return group;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the success
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsCreateResponseSuccess",
                exports = "parseCreateResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "result")) {
                return Optional.empty();
            }
            var requestId = request.getAttributeAsString("id").orElse(null);
            if (requestId == null) {
                return Optional.empty();
            }
            if (!node.hasAttribute("id", requestId)) {
                return Optional.empty();
            }
            var fromAttr = node.getAttributeAsString("from").orElse(null);
            if (fromAttr == null || !fromAttr.endsWith("g.us")) {
                return Optional.empty();
            }
            var group = node.getChild("group").orElse(null);
            if (group == null) {
                return Optional.empty();
            }
            var groupId = group.getAttributeAsString("id").orElse(null);
            if (groupId == null) {
                return Optional.empty();
            }
            var groupCreator = group.getAttributeAsJid("creator").orElse(null);
            if (groupCreator == null) {
                return Optional.empty();
            }
            if (group.getAttributeAsLong("creation").isEmpty()) {
                return Optional.empty();
            }
            var groupCreation = group.getAttributeAsLong("creation").getAsLong();
            Long groupSyncTime = null;
            if (group.getAttributeAsLong("s_t").isPresent()) {
                groupSyncTime = group.getAttributeAsLong("s_t").getAsLong();
            }
            var groupSyncOwner = group.getAttributeAsJid("s_o").orElse(null);
            var subject = group.getAttributeAsString("subject").orElse(null);
            if (subject == null) {
                return Optional.empty();
            }
            var groupJid = Jid.of(groupId, JidServer.groupOrCommunity());
            var description = group.getChild("description").orElse(null);
            String descriptionId = null;
            String descriptionError = null;
            if (description != null) {
                descriptionId = description.getAttributeAsString("id").orElse(null);
                descriptionError = description.getAttributeAsString("error").orElse(null);
            }
            var locked = group.getChild("locked").isPresent();
            var announcement = group.getChild("announcement").isPresent();
            var parent = group.getChild("parent").isPresent();
            var noFrequentlyForwarded = group.getChild("no_frequently_forwarded").isPresent();
            Integer ephemeralExpiration = null;
            Integer ephemeralTrigger = null;
            var ephemeral = group.getChild("ephemeral").orElse(null);
            if (ephemeral != null) {
                ephemeralExpiration = ephemeral.getAttributeAsInt("expiration").orElse(0);
                if (ephemeral.getAttributeAsInt("trigger").isPresent()) {
                    ephemeralTrigger = ephemeral.getAttributeAsInt("trigger").getAsInt();
                }
            }
            var membershipApprovalMode = group.getChild("membership_approval_mode").isPresent();
            var breakout = group.getChild("breakout").isPresent();
            Jid linkedParentJid = null;
            var linkedParent = group.getChild("linked_parent").orElse(null);
            if (linkedParent != null) {
                linkedParentJid = linkedParent.getAttributeAsJid("jid").orElse(null);
            }
            var hiddenGroup = group.getChild("hidden_group").isPresent();
            var allowNonAdmin = group.getChild("allow_non_admin_sub_group_creation").isPresent();
            var groupHistory = group.getChild("group_history").isPresent();
            var capi = group.getChild("capi").isPresent();
            var participants = new ArrayList<ResponseParticipant>();
            for (var participantNode : group.getChildren("participant")) {
                var parsed = ResponseParticipant.of(participantNode).orElse(null);
                if (parsed == null) {
                    return Optional.empty();
                }
                participants.add(parsed);
            }
            if (participants.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Success(groupId, groupJid, groupCreator, groupCreation, groupSyncTime,
                    groupSyncOwner, subject, descriptionId, descriptionError, locked, announcement, parent,
                    noFrequentlyForwarded, ephemeralExpiration, ephemeralTrigger, membershipApprovalMode,
                    breakout, linkedParentJid, hiddenGroup, allowNonAdmin, groupHistory, capi, participants,
                    group));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Success) obj;
            return this.groupCreation == that.groupCreation
                    && this.locked == that.locked
                    && this.announcement == that.announcement
                    && this.parent == that.parent
                    && this.noFrequentlyForwarded == that.noFrequentlyForwarded
                    && this.membershipApprovalMode == that.membershipApprovalMode
                    && this.breakout == that.breakout
                    && this.hiddenGroup == that.hiddenGroup
                    && this.allowNonAdminSubGroupCreation == that.allowNonAdminSubGroupCreation
                    && this.groupHistory == that.groupHistory
                    && this.capi == that.capi
                    && Objects.equals(this.groupId, that.groupId)
                    && Objects.equals(this.groupJid, that.groupJid)
                    && Objects.equals(this.groupCreator, that.groupCreator)
                    && Objects.equals(this.groupSyncTime, that.groupSyncTime)
                    && Objects.equals(this.groupSyncOwner, that.groupSyncOwner)
                    && Objects.equals(this.subject, that.subject)
                    && Objects.equals(this.descriptionId, that.descriptionId)
                    && Objects.equals(this.descriptionError, that.descriptionError)
                    && Objects.equals(this.ephemeralExpiration, that.ephemeralExpiration)
                    && Objects.equals(this.ephemeralTrigger, that.ephemeralTrigger)
                    && Objects.equals(this.linkedParentJid, that.linkedParentJid)
                    && Objects.equals(this.participants, that.participants)
                    && Objects.equals(this.group, that.group);
        }

        @Override
        public int hashCode() {
            var primary = Objects.hash(groupId, groupJid, groupCreator, groupCreation, groupSyncTime,
                    groupSyncOwner, subject, descriptionId, descriptionError, locked, announcement, parent,
                    noFrequentlyForwarded, ephemeralExpiration, ephemeralTrigger);
            var secondary = Objects.hash(membershipApprovalMode, breakout, linkedParentJid, hiddenGroup,
                    allowNonAdminSubGroupCreation, groupHistory, capi, participants);
            return primary * 31 + secondary;
        }

        @Override
        public String toString() {
            return "SmaxGroupsCreateResponse.Success[groupId=" + groupId
                    + ", groupJid=" + groupJid
                    + ", groupCreator=" + groupCreator
                    + ", groupCreation=" + groupCreation
                    + ", groupSyncTime=" + groupSyncTime
                    + ", groupSyncOwner=" + groupSyncOwner
                    + ", subject=" + subject
                    + ", descriptionId=" + descriptionId
                    + ", descriptionError=" + descriptionError
                    + ", locked=" + locked
                    + ", announcement=" + announcement
                    + ", parent=" + parent
                    + ", noFrequentlyForwarded=" + noFrequentlyForwarded
                    + ", ephemeralExpiration=" + ephemeralExpiration
                    + ", ephemeralTrigger=" + ephemeralTrigger
                    + ", membershipApprovalMode=" + membershipApprovalMode
                    + ", breakout=" + breakout
                    + ", linkedParentJid=" + linkedParentJid
                    + ", hiddenGroup=" + hiddenGroup
                    + ", allowNonAdminSubGroupCreation=" + allowNonAdminSubGroupCreation
                    + ", groupHistory=" + groupHistory
                    + ", capi=" + capi
                    + ", participants=" + participants + ']';
        }

        /**
         * Single seed-participant echo row inside a {@link Success}.
         *
         * <p>The relay returns either a successfully-added
         * participant ({@link #notRegisteredOnWa()} == {@code false},
         * with {@link #username()} populated when the relay echoed
         * the username mixin) or a not-registered marker
         * ({@link #notRegisteredOnWa()} == {@code true}) — the
         * discriminator is whether the
         * {@code WASmaxInGroupsCreateParticipantAddedResponseMixin}
         * parser succeeded versus the
         * {@code WASmaxInGroupsNonRegisteredWaUserParticipantErrorLidResponseMixin}
         * parser succeeded. Cobalt collapses the alternation into a
         * single class with a flag to keep the response shape
         * stable across both branches.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsCreateParticipantAddedResponseMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsNonRegisteredWaUserParticipantErrorLidResponseMixin")
        public static final class ResponseParticipant {
            /**
             * The participant JID echoed by the relay (when the
             * participant was successfully added) or {@code null}
             * for not-registered entries.
             */
            private final Jid jid;

            /**
             * The optional phone-number mixin echoed by the relay
             * (always present on not-registered entries; optional
             * on added entries).
             */
            private final Jid phoneNumber;

            /**
             * The optional username echoed by the relay.
             */
            private final String username;

            /**
             * Whether the relay surfaced the not-registered marker
             * (i.e. parsed via
             * {@code NonRegisteredWaUserParticipantErrorLidResponseMixin}
             * rather than
             * {@code CreateParticipantAddedResponseMixin}).
             */
            private final boolean notRegisteredOnWa;

            /**
             * Constructs a participant echo row.
             *
             * @param jid               the participant JID; may be
             *                          {@code null} for
             *                          not-registered rows
             * @param phoneNumber       the optional phone JID
             * @param username          the optional username
             * @param notRegisteredOnWa whether the participant is a
             *                          non-registered WA user
             */
            public ResponseParticipant(Jid jid, Jid phoneNumber, String username, boolean notRegisteredOnWa) {
                this.jid = jid;
                this.phoneNumber = phoneNumber;
                this.username = username;
                this.notRegisteredOnWa = notRegisteredOnWa;
            }

            /**
             * Returns the participant JID, when present.
             *
             * @return an {@link Optional} carrying the JID, or
             *         empty for not-registered rows
             */
            public Optional<Jid> jid() {
                return Optional.ofNullable(jid);
            }

            /**
             * Returns the optional phone-number JID.
             *
             * @return an {@link Optional} carrying the phone JID,
             *         or empty when omitted
             */
            public Optional<Jid> phoneNumber() {
                return Optional.ofNullable(phoneNumber);
            }

            /**
             * Returns the optional username.
             *
             * @return an {@link Optional} carrying the username,
             *         or empty when omitted
             */
            public Optional<String> username() {
                return Optional.ofNullable(username);
            }

            /**
             * Returns whether the participant is a non-registered WA
             * user.
             *
             * @return {@code true} when the relay surfaced the
             *         not-registered marker
             */
            public boolean notRegisteredOnWa() {
                return notRegisteredOnWa;
            }

            /**
             * Tries to parse a participant echo row.
             *
             * @param participantNode the
             *                        {@code <participant/>} child
             * @return an {@link Optional} carrying the parsed row
             */
            static Optional<ResponseParticipant> of(Node participantNode) {
                var jid = participantNode.getAttributeAsJid("jid").orElse(null);
                var phoneNumber = participantNode.getAttributeAsJid("phone_number").orElse(null);
                var username = participantNode.getAttributeAsString("username").orElse(null);
                var notRegistered = jid == null && phoneNumber != null;
                if (jid == null && !notRegistered) {
                    return Optional.empty();
                }
                return Optional.of(new ResponseParticipant(jid, phoneNumber, username, notRegistered));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (ResponseParticipant) obj;
                return this.notRegisteredOnWa == that.notRegisteredOnWa
                        && Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.phoneNumber, that.phoneNumber)
                        && Objects.equals(this.username, that.username);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, phoneNumber, username, notRegisteredOnWa);
            }

            @Override
            public String toString() {
                return "SmaxGroupsCreateResponse.Success.ResponseParticipant[jid=" + jid
                        + ", phoneNumber=" + phoneNumber
                        + ", username=" + username
                        + ", notRegisteredOnWa=" + notRegisteredOnWa + ']';
            }
        }
    }

    /**
     * The {@code GroupAlreadyExists} reply variant — the relay
     * detected an existing group whose creator + dedup-token tuple
     * matches the request and returned the existing group's JID
     * instead of creating a new one.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsCreateResponseGroupAlreadyExists")
    final class GroupAlreadyExists implements SmaxGroupsCreateResponse {
        /**
         * The existing group JID surfaced by the relay.
         */
        private final Jid groupJid;

        /**
         * Constructs a group-already-exists reply.
         *
         * @param groupJid the existing group JID; never
         *                 {@code null}
         * @throws NullPointerException if {@code groupJid} is
         *                              {@code null}
         */
        public GroupAlreadyExists(Jid groupJid) {
            this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        }

        /**
         * Returns the existing group JID.
         *
         * @return the group JID; never {@code null}
         */
        public Jid groupJid() {
            return groupJid;
        }

        /**
         * Tries to parse a {@link GroupAlreadyExists} variant from
         * the given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         already-exists schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsCreateResponseGroupAlreadyExists",
                exports = "parseCreateResponseGroupAlreadyExists",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<GroupAlreadyExists> of(Node node, Node request) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "result")) {
                return Optional.empty();
            }
            var requestId = request.getAttributeAsString("id").orElse(null);
            if (requestId == null) {
                return Optional.empty();
            }
            if (!node.hasAttribute("id", requestId)) {
                return Optional.empty();
            }
            var fromAttr = node.getAttributeAsString("from").orElse(null);
            if (fromAttr == null || !fromAttr.endsWith("g.us")) {
                return Optional.empty();
            }
            var group = node.getChild("group").orElse(null);
            if (group == null) {
                return Optional.empty();
            }
            var groupJid = group.getAttributeAsJid("jid").orElse(null);
            if (groupJid == null) {
                return Optional.empty();
            }
            return Optional.of(new GroupAlreadyExists(groupJid));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (GroupAlreadyExists) obj;
            return Objects.equals(this.groupJid, that.groupJid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupJid);
        }

        @Override
        public String toString() {
            return "SmaxGroupsCreateResponse.GroupAlreadyExists[groupJid=" + groupJid + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, or bumping a per-creator
     * group-count cap.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsCreateResponseClientError")
    final class ClientError implements SmaxGroupsCreateResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ClientError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsCreateResponseClientError",
                exports = "parseCreateResponseClientError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ClientError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ClientError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGroupsCreateResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsCreateResponseServerError")
    final class ServerError implements SmaxGroupsCreateResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ServerError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsCreateResponseServerError",
                exports = "parseCreateResponseServerError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ServerError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ServerError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGroupsCreateResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
