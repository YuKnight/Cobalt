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
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 *
 * <p>Carries the group JID plus the typed flag set selected by the
 * caller. Mutually exclusive pairs ({@code locked} vs {@code unlocked},
 * {@code announcement} vs {@code notAnnouncement}, etc.) are not
 * checked client-side — the relay rejects invalid combinations with
 * a {@link SmaxGroupsSetPropertyResponse.ClientError}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsSetPropertyRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsSetPropertyRequest implements SmaxOperation.Request {
    /**
     * The group JID whose property is being toggled.
     */
    private final Jid groupJid;

    /**
     * Whether to emit a {@code <locked/>} child (chat-info edits become
     * admin-only).
     */
    private final boolean locked;

    /**
     * Whether to emit an {@code <announcement/>} child (only admins may
     * post).
     */
    private final boolean announcement;

    /**
     * Whether to emit a {@code <no_frequently_forwarded/>} child
     * (disables the frequently-forwarded label).
     */
    private final boolean noFrequentlyForwarded;

    /**
     * The optional ephemeral-message expiration in seconds; non-null
     * triggers an {@code <ephemeral expiration trigger/>} child.
     */
    private final Integer ephemeralExpiration;

    /**
     * The optional ephemeral-message trigger value (paired with
     * {@link #ephemeralExpiration}). May be {@code null}.
     */
    private final Integer ephemeralTrigger;

    /**
     * Whether to emit an {@code <unlocked/>} child (chat-info edits
     * become non-admin-allowed).
     */
    private final boolean unlocked;

    /**
     * Whether to emit a {@code <not_announcement/>} child (any
     * participant may post).
     */
    private final boolean notAnnouncement;

    /**
     * Whether to emit a {@code <frequently_forwarded_ok/>} child
     * (re-enables the frequently-forwarded label).
     */
    private final boolean frequentlyForwardedOk;

    /**
     * Whether to emit a {@code <not_ephemeral/>} child (turns off
     * ephemeral messages).
     */
    private final boolean notEphemeral;

    /**
     * The optional membership-approval-mode value
     * ({@code "on"}/{@code "off"}); non-null triggers a
     * {@code <membership_approval_mode group_join_mode="..."/>} child.
     */
    private final String membershipApprovalGroupJoinMode;

    /**
     * Whether to emit an {@code <allow_admin_reports/>} child (admins
     * may report messages).
     */
    private final boolean allowAdminReports;

    /**
     * Whether to emit a {@code <not_allow_admin_reports/>} child.
     */
    private final boolean notAllowAdminReports;

    /**
     * Whether to emit an {@code <allow_non_admin_sub_group_creation/>}
     * child (community sub-group creation by non-admins).
     */
    private final boolean allowNonAdminSubGroupCreation;

    /**
     * Whether to emit a {@code <not_allow_non_admin_sub_group_creation/>}
     * child.
     */
    private final boolean notAllowNonAdminSubGroupCreation;

    /**
     * Whether to emit a {@code <group_history/>} child (turn on shared
     * history for new joiners).
     */
    private final boolean groupHistory;

    /**
     * Whether to emit a {@code <no_group_history/>} child.
     */
    private final boolean noGroupHistory;

    /**
     * Constructs a new {@link SmaxGroupsSetPropertyRequest}.
     *
     * @param groupJid                          the group JID; never
     *                                          {@code null}
     * @param locked                            whether to set locked
     * @param announcement                      whether to set
     *                                          announcement-only
     * @param noFrequentlyForwarded             whether to disable the
     *                                          frequently-forwarded
     *                                          label
     * @param ephemeralExpiration               optional ephemeral
     *                                          expiration in seconds;
     *                                          {@code null} skips the
     *                                          {@code <ephemeral/>}
     *                                          child
     * @param ephemeralTrigger                  optional ephemeral
     *                                          trigger value; ignored
     *                                          when
     *                                          {@code ephemeralExpiration}
     *                                          is {@code null}
     * @param unlocked                          whether to set unlocked
     * @param notAnnouncement                   whether to revert
     *                                          announcement-only
     * @param frequentlyForwardedOk             whether to re-enable the
     *                                          frequently-forwarded
     *                                          label
     * @param notEphemeral                      whether to turn off
     *                                          ephemeral messages
     * @param membershipApprovalGroupJoinMode   optional
     *                                          membership-approval
     *                                          mode; {@code null} skips
     *                                          the
     *                                          {@code <membership_approval_mode/>}
     *                                          child
     * @param allowAdminReports                 whether to allow admin
     *                                          reports
     * @param notAllowAdminReports              whether to disallow
     *                                          admin reports
     * @param allowNonAdminSubGroupCreation     whether to allow
     *                                          non-admin sub-group
     *                                          creation
     * @param notAllowNonAdminSubGroupCreation  whether to disallow
     *                                          non-admin sub-group
     *                                          creation
     * @param groupHistory                      whether to enable
     *                                          shared group history
     * @param noGroupHistory                    whether to disable
     *                                          shared group history
     * @throws NullPointerException if {@code groupJid} is {@code null}
     */
    public SmaxGroupsSetPropertyRequest(Jid groupJid,
                   boolean locked,
                   boolean announcement,
                   boolean noFrequentlyForwarded,
                   Integer ephemeralExpiration,
                   Integer ephemeralTrigger,
                   boolean unlocked,
                   boolean notAnnouncement,
                   boolean frequentlyForwardedOk,
                   boolean notEphemeral,
                   String membershipApprovalGroupJoinMode,
                   boolean allowAdminReports,
                   boolean notAllowAdminReports,
                   boolean allowNonAdminSubGroupCreation,
                   boolean notAllowNonAdminSubGroupCreation,
                   boolean groupHistory,
                   boolean noGroupHistory) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        this.locked = locked;
        this.announcement = announcement;
        this.noFrequentlyForwarded = noFrequentlyForwarded;
        this.ephemeralExpiration = ephemeralExpiration;
        this.ephemeralTrigger = ephemeralTrigger;
        this.unlocked = unlocked;
        this.notAnnouncement = notAnnouncement;
        this.frequentlyForwardedOk = frequentlyForwardedOk;
        this.notEphemeral = notEphemeral;
        this.membershipApprovalGroupJoinMode = membershipApprovalGroupJoinMode;
        this.allowAdminReports = allowAdminReports;
        this.notAllowAdminReports = notAllowAdminReports;
        this.allowNonAdminSubGroupCreation = allowNonAdminSubGroupCreation;
        this.notAllowNonAdminSubGroupCreation = notAllowNonAdminSubGroupCreation;
        this.groupHistory = groupHistory;
        this.noGroupHistory = noGroupHistory;
    }

    /**
     * Returns the group JID.
     *
     * @return the group JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns whether the request flips the group to locked.
     *
     * @return {@code true} when the {@code <locked/>} child is emitted
     */
    public boolean locked() {
        return locked;
    }

    /**
     * Returns whether the request flips the group to announcement-only.
     *
     * @return {@code true} when the {@code <announcement/>} child is
     *         emitted
     */
    public boolean announcement() {
        return announcement;
    }

    /**
     * Returns whether the request disables the frequently-forwarded
     * label.
     *
     * @return {@code true} when the {@code <no_frequently_forwarded/>}
     *         child is emitted
     */
    public boolean noFrequentlyForwarded() {
        return noFrequentlyForwarded;
    }

    /**
     * Returns the optional ephemeral expiration value.
     *
     * @return an {@link Optional} carrying the expiration in seconds,
     *         or empty when no {@code <ephemeral/>} child is emitted
     */
    public Optional<Integer> ephemeralExpiration() {
        return Optional.ofNullable(ephemeralExpiration);
    }

    /**
     * Returns the optional ephemeral trigger value.
     *
     * @return an {@link Optional} carrying the trigger, or empty when
     *         the request omits it
     */
    public Optional<Integer> ephemeralTrigger() {
        return Optional.ofNullable(ephemeralTrigger);
    }

    /**
     * Returns whether the request flips the group to unlocked.
     *
     * @return {@code true} when the {@code <unlocked/>} child is
     *         emitted
     */
    public boolean unlocked() {
        return unlocked;
    }

    /**
     * Returns whether the request reverts announcement-only mode.
     *
     * @return {@code true} when the {@code <not_announcement/>} child
     *         is emitted
     */
    public boolean notAnnouncement() {
        return notAnnouncement;
    }

    /**
     * Returns whether the request re-enables the frequently-forwarded
     * label.
     *
     * @return {@code true} when the {@code <frequently_forwarded_ok/>}
     *         child is emitted
     */
    public boolean frequentlyForwardedOk() {
        return frequentlyForwardedOk;
    }

    /**
     * Returns whether the request turns off ephemeral messages.
     *
     * @return {@code true} when the {@code <not_ephemeral/>} child is
     *         emitted
     */
    public boolean notEphemeral() {
        return notEphemeral;
    }

    /**
     * Returns the optional membership-approval mode value.
     *
     * @return an {@link Optional} carrying the join-mode value, or
     *         empty when the request omits the
     *         {@code <membership_approval_mode/>} child
     */
    public Optional<String> membershipApprovalGroupJoinMode() {
        return Optional.ofNullable(membershipApprovalGroupJoinMode);
    }

    /**
     * Returns whether the request enables admin reports.
     *
     * @return {@code true} when the {@code <allow_admin_reports/>}
     *         child is emitted
     */
    public boolean allowAdminReports() {
        return allowAdminReports;
    }

    /**
     * Returns whether the request disables admin reports.
     *
     * @return {@code true} when the {@code <not_allow_admin_reports/>}
     *         child is emitted
     */
    public boolean notAllowAdminReports() {
        return notAllowAdminReports;
    }

    /**
     * Returns whether the request allows non-admin sub-group creation.
     *
     * @return {@code true} when the
     *         {@code <allow_non_admin_sub_group_creation/>} child is
     *         emitted
     */
    public boolean allowNonAdminSubGroupCreation() {
        return allowNonAdminSubGroupCreation;
    }

    /**
     * Returns whether the request forbids non-admin sub-group creation.
     *
     * @return {@code true} when the
     *         {@code <not_allow_non_admin_sub_group_creation/>} child
     *         is emitted
     */
    public boolean notAllowNonAdminSubGroupCreation() {
        return notAllowNonAdminSubGroupCreation;
    }

    /**
     * Returns whether the request enables shared group history.
     *
     * @return {@code true} when the {@code <group_history/>} child is
     *         emitted
     */
    public boolean groupHistory() {
        return groupHistory;
    }

    /**
     * Returns whether the request disables shared group history.
     *
     * @return {@code true} when the {@code <no_group_history/>} child
     *         is emitted
     */
    public boolean noGroupHistory() {
        return noGroupHistory;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         selected toggle children
     *
     * @implNote {@code WASmaxOutGroupsSetPropertyRequest.makeSetPropertyRequest}
     *           composes
     *           {@code WASmaxOutGroupsBaseSetGroupMixin} with
     *           {@code WASmaxOutGroupsBaseIQSetRequestMixin} over the
     *           selected boolean toggle children, the
     *           {@code <ephemeral/>} child (when
     *           {@link #ephemeralExpiration} is set), and the
     *           {@code <membership_approval_mode/>} child (when
     *           {@link #membershipApprovalGroupJoinMode} is set).
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsSetPropertyRequest",
            exports = "makeSetPropertyRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        if (locked) {
            children.add(new NodeBuilder().description("locked").build());
        }
        if (announcement) {
            children.add(new NodeBuilder().description("announcement").build());
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
        if (unlocked) {
            children.add(new NodeBuilder().description("unlocked").build());
        }
        if (notAnnouncement) {
            children.add(new NodeBuilder().description("not_announcement").build());
        }
        if (frequentlyForwardedOk) {
            children.add(new NodeBuilder().description("frequently_forwarded_ok").build());
        }
        if (notEphemeral) {
            children.add(new NodeBuilder().description("not_ephemeral").build());
        }
        if (membershipApprovalGroupJoinMode != null) {
            var membershipNode = new NodeBuilder()
                    .description("membership_approval_mode")
                    .attribute("group_join_mode", membershipApprovalGroupJoinMode)
                    .build();
            children.add(membershipNode);
        }
        if (allowAdminReports) {
            children.add(new NodeBuilder().description("allow_admin_reports").build());
        }
        if (notAllowAdminReports) {
            children.add(new NodeBuilder().description("not_allow_admin_reports").build());
        }
        if (allowNonAdminSubGroupCreation) {
            children.add(new NodeBuilder().description("allow_non_admin_sub_group_creation").build());
        }
        if (notAllowNonAdminSubGroupCreation) {
            children.add(new NodeBuilder().description("not_allow_non_admin_sub_group_creation").build());
        }
        if (groupHistory) {
            children.add(new NodeBuilder().description("group_history").build());
        }
        if (noGroupHistory) {
            children.add(new NodeBuilder().description("no_group_history").build());
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(children);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsSetPropertyRequest) obj;
        return this.locked == that.locked
                && this.announcement == that.announcement
                && this.noFrequentlyForwarded == that.noFrequentlyForwarded
                && this.unlocked == that.unlocked
                && this.notAnnouncement == that.notAnnouncement
                && this.frequentlyForwardedOk == that.frequentlyForwardedOk
                && this.notEphemeral == that.notEphemeral
                && this.allowAdminReports == that.allowAdminReports
                && this.notAllowAdminReports == that.notAllowAdminReports
                && this.allowNonAdminSubGroupCreation == that.allowNonAdminSubGroupCreation
                && this.notAllowNonAdminSubGroupCreation == that.notAllowNonAdminSubGroupCreation
                && this.groupHistory == that.groupHistory
                && this.noGroupHistory == that.noGroupHistory
                && Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.ephemeralExpiration, that.ephemeralExpiration)
                && Objects.equals(this.ephemeralTrigger, that.ephemeralTrigger)
                && Objects.equals(this.membershipApprovalGroupJoinMode, that.membershipApprovalGroupJoinMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, locked, announcement, noFrequentlyForwarded, ephemeralExpiration,
                ephemeralTrigger, unlocked, notAnnouncement, frequentlyForwardedOk, notEphemeral,
                membershipApprovalGroupJoinMode, allowAdminReports, notAllowAdminReports,
                allowNonAdminSubGroupCreation, notAllowNonAdminSubGroupCreation, groupHistory, noGroupHistory);
    }

    @Override
    public String toString() {
        return "SmaxGroupsSetPropertyRequest[groupJid=" + groupJid
                + ", locked=" + locked
                + ", announcement=" + announcement
                + ", noFrequentlyForwarded=" + noFrequentlyForwarded
                + ", ephemeralExpiration=" + ephemeralExpiration
                + ", ephemeralTrigger=" + ephemeralTrigger
                + ", unlocked=" + unlocked
                + ", notAnnouncement=" + notAnnouncement
                + ", frequentlyForwardedOk=" + frequentlyForwardedOk
                + ", notEphemeral=" + notEphemeral
                + ", membershipApprovalGroupJoinMode=" + membershipApprovalGroupJoinMode
                + ", allowAdminReports=" + allowAdminReports
                + ", notAllowAdminReports=" + notAllowAdminReports
                + ", allowNonAdminSubGroupCreation=" + allowNonAdminSubGroupCreation
                + ", notAllowNonAdminSubGroupCreation=" + notAllowNonAdminSubGroupCreation
                + ", groupHistory=" + groupHistory
                + ", noGroupHistory=" + noGroupHistory
                + ']';
    }
}
