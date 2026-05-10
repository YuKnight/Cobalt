package com.github.auties00.cobalt.stream.notification.group;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.chat.ChatEphemeralTimer;
import com.github.auties00.cobalt.model.chat.ChatMetadata;
import com.github.auties00.cobalt.model.chat.ChatPolicy;
import com.github.auties00.cobalt.model.chat.community.CommunityMetadata;
import com.github.auties00.cobalt.model.chat.group.GroupMetadata;
import com.github.auties00.cobalt.model.chat.group.GroupParticipant;
import com.github.auties00.cobalt.model.chat.group.GroupParticipantBuilder;
import com.github.auties00.cobalt.model.chat.group.GroupPartipantRole;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.stream.SocketStream;
import com.github.auties00.cobalt.wam.WamService;
import com.github.auties00.cobalt.wam.event.GroupJoinCEventBuilder;

import java.time.Instant;
import java.util.LinkedHashSet;

/**
 * Handles incoming {@code w:gp2} group notification stanzas by parsing the
 * notification's child action nodes and applying each mutation to the local
 * chat and group metadata stores.
 *
 * <p>The handler follows a two-phase approach: first it applies all
 * inline mutations that can be derived directly from the stanza (subject
 * change, participant add/remove, setting toggles, etc.), then it triggers
 * a full metadata refresh for every group JID referenced in the notification
 * to ensure convergence with the server state.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleGroupNotification")
public final class NotificationGroupStreamHandler implements SocketStream.Handler {

    /**
     * Logger for diagnostic messages emitted during notification handling.
     */
    private static final System.Logger LOGGER = System.getLogger(NotificationGroupStreamHandler.class.getName());

    /**
     * The WhatsApp client instance providing access to the store and network
     * operations required for group metadata queries and ACK sending.
     */
    private final WhatsAppClient whatsapp;

    /**
     * The WAM telemetry service used to commit group-join events.
     */
    private final WamService wamService;

    /**
     * Constructs a new handler with the given WhatsApp client.
     *
     * @param whatsapp   the non-{@code null} client instance
     * @param wamService the WAM telemetry service used to commit group-join events
     */
    public NotificationGroupStreamHandler(WhatsAppClient whatsapp, WamService wamService) {
        this.whatsapp = whatsapp;
        this.wamService = wamService;
    }

    /**
     * Entry point invoked by the {@link SocketStream} dispatcher for every
     * incoming node. Filters for {@code notification} nodes with
     * {@code type="w:gp2"} and delegates to {@link #handleNotification}.
     *
     * @param node the incoming stanza node
     */
    @Override
    public void handle(Node node) {
        if (!node.hasDescription("notification") || !node.hasAttribute("type", "w:gp2")) {
            return;
        }

        try {
            handleNotification(node);
        } catch (Throwable throwable) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Cannot handle w:gp2 notification {0}: {1}",
                    node.getAttributeAsString("id", "<missing>"),
                    throwable.getMessage());
        } finally {
            sendNotificationAck(node);
        }
    }

    /**
     * Parses the top-level notification node, extracts the group JID, and
     * dispatches each child action to {@link #handleAction}. If the first
     * child is a {@code groups_dirty} node the handler delegates to a full
     * metadata refresh instead of inline processing.
     *
     * <p>After all actions have been processed, every referenced group JID
     * (including those found in sub-elements such as linked groups and
     * subgroup suggestions) is refreshed to ensure convergence.
     *
     * @param node the {@code notification} stanza
     */
    private void handleNotification(Node node) {
        var groupJid = node.getAttributeAsJid("from").orElse(null);
        if (groupJid == null || !groupJid.hasGroupOrCommunityServer()) {
            return;
        }

        if (node.hasChild("groups_dirty")) {
            refreshGroup(groupJid);
            return;
        }

        // ADAPTED: Cobalt finds-or-creates the Chat locally instead of
        // relying on WAWebSchemaChat.getChatTable
        var chat = whatsapp.store()
                .findChatByJid(groupJid)
                .orElseGet(() -> whatsapp.store().addNewChat(groupJid));
        var notificationTimestamp = resolveInstant(node, "t");
        if (notificationTimestamp != null) {
            chat.setConversationTimestamp(notificationTimestamp);
            chat.setLastMsgTimestamp(notificationTimestamp);
        }
        var relatedGroups = new LinkedHashSet<Jid>();
        relatedGroups.add(groupJid);

        for (var action : node.children()) {
            handleAction(node, chat, groupJid, action, relatedGroups);
        }

        // ADAPTED: Cobalt refreshes all related groups after inline mutations
        for (var relatedGroup : relatedGroups) {
            refreshGroup(relatedGroup);
        }
    }

    /**
     * Dispatches a single action child node to the appropriate handler based
     * on the action's tag name. The tag names correspond exactly to the
     * {@code WAWebHandleGroupNotificationConst.GROUP_NOTIFICATION_TAG}
     * constants.
     *
     * @param notification the parent notification stanza
     * @param chat         the local chat entity for the group
     * @param groupJid     the JID of the group
     * @param action       the action child node
     * @param relatedGroups collector for JIDs that need metadata refresh
     */
    private void handleAction(Node notification, Chat chat, Jid groupJid, Node action, LinkedHashSet<Jid> relatedGroups) {
        collectRelatedGroups(action, relatedGroups);

        switch (action.description()) {
            case "create" -> applyCreate(notification, chat, groupJid, action);
            case "add" -> applyParticipants(groupJid, action, GroupParticipantMutation.ADD);
            case "remove" -> applyParticipants(groupJid, action, GroupParticipantMutation.REMOVE);
            case "promote", "linked_group_promote" -> applyParticipants(groupJid, action, GroupParticipantMutation.PROMOTE);
            case "demote", "linked_group_demote" -> applyParticipants(groupJid, action, GroupParticipantMutation.DEMOTE);
            case "modify" -> applyParticipants(groupJid, action, GroupParticipantMutation.MODIFY);
            case "subject" -> applySubject(notification, chat, groupJid, action);
            case "description" -> applyDescription(notification, chat, groupJid, action);
            case "locked" -> applyRestrict(groupJid, true);
            case "unlocked" -> applyRestrict(groupJid, false);
            case "announcement" -> applyAnnounce(groupJid, true);
            case "not_announcement" -> applyAnnounce(groupJid, false);
            case "no_frequently_forwarded" -> applyNoFrequentlyForwarded(groupJid, true);
            case "frequently_forwarded_ok" -> applyNoFrequentlyForwarded(groupJid, false);
            case "ephemeral" -> applyEphemeral(notification, chat, groupJid, action);
            case "not_ephemeral" -> clearEphemeral(chat, groupJid);
            case "growth_locked" -> applyGrowthLock(groupJid, action);
            case "growth_unlocked" -> clearGrowthLock(groupJid);
            case "link" -> applyLink(groupJid, action);
            case "unlink" -> applyUnlink(groupJid, action);
            case "membership_approval_mode" -> applyMembershipApproval(action, groupJid);
            case "allow_admin_reports" -> applyReportToAdmin(groupJid, true);
            case "not_allow_admin_reports" -> applyReportToAdmin(groupJid, false);
            case "allow_non_admin_sub_group_creation" -> applyNonAdminSubgroupCreation(groupJid, true);
            case "not_allow_non_admin_sub_group_creation" -> applyNonAdminSubgroupCreation(groupJid, false);
            case "member_add_mode" -> applyMemberAddMode(groupJid, action);
            case "auto_add_disabled" -> applyGeneralChatAutoAddDisabled(groupJid, true);
            case "group_safety_check" -> applyGroupSafetyCheck(groupJid, true);
            case "suspended" -> applySuspended(chat, groupJid, true);
            case "unsuspended" -> applySuspended(chat, groupJid, false);
            case "delete" -> {
                whatsapp.store().removeChatMetadata(groupJid);
                chat.setTerminated(true);
            }
            // Actions that WA Web parses but whose side-effects are
            // handled via system message generation and DB updates that
            // Cobalt covers through the post-loop metadata refresh.
            case "invite",
                 "revoke",
                 "membership_approval_request",
                 "reports",
                 "created_membership_requests",
                 "revoked_membership_requests",
                 "created_sub_group_suggestion",
                 "revoked_sub_group_suggestions",
                 "change_number",
                 "missing_participant_identification"
                 -> LOGGER.log(System.Logger.Level.DEBUG,
                    "Handling w:gp2 action {0} conservatively via metadata refresh",
                    action.description());
            default -> LOGGER.log(System.Logger.Level.DEBUG,
                    "Ignoring unsupported w:gp2 action {0}", action.description());
        }
    }

    /**
     * Handles the {@code create} action by extracting all group metadata
     * fields from the enclosed {@code <group>} element and applying them to
     * both the local {@link Chat} and the {@link ChatMetadata} store.
     *
     * <p>The implementation mirrors the WA Web {@code I} function which
     * constructs a full {@code groupInfo} object from the create stanza,
     * including subject, description, creator, all boolean settings,
     * ephemeral configuration, and the initial participant list.
     *
     * @param notification the parent notification stanza
     * @param chat         the local chat entity
     * @param groupJid     the JID of the created group
     * @param action       the {@code create} action node
     */
    private void applyCreate(Node notification, Chat chat, Jid groupJid, Node action) {
        var groupNode = action.getChild("group").orElse(action);
        // author is null or is not the current PN user, the recipient was
        // added to a group they did not create, so a GroupJoinC telemetry
        // event is committed. The property list is empty (WA Web definition:
        // GroupJoinC:[158,{},[1,1,1],"regular"]).
        var notificationAuthor = notification.getAttributeAsJid("participant").orElse(null);
        var mePnUser = whatsapp.store().jid().orElse(null);
        if (notificationAuthor == null
                || mePnUser == null
                || !notificationAuthor.toUserJid().equals(mePnUser.toUserJid())) {
            wamService.commit(new GroupJoinCEventBuilder().build());
        }

        var subject = groupNode.getAttributeAsString("subject", null);
        if (subject != null) {
            chat.setName(subject);
        }

        var creation = groupNode.getAttributeAsLong("creation", (Long) null);
        chat.setCreatedAt(creation == null ? null : Instant.ofEpochSecond(creation));
        chat.setCreatedBy(groupNode.getAttributeAsString("creator", null));
        chat.setDescription(resolveCreateDescriptionBody(groupNode));
        chat.setSupport(groupNode.hasChild("support"));
        chat.setDefaultSubgroup(groupNode.hasChild("default_sub_group"));
        chat.setArchived(false);
        chat.setSuspended(groupNode.hasChild("suspended"));
        chat.setTerminated(false);

        applyEphemeral(notification, chat, groupJid, groupNode);

        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            metadata.clearParticipants();
            metadata.addAllParticipants(parseParticipants(groupNode, GroupParticipantMutation.ADD));

            setSubject(metadata, subject);

            var createDescBody = resolveCreateDescriptionBody(groupNode);
            var createDescId = resolveCreateDescriptionId(groupNode);
            setDescription(metadata, createDescBody, createDescId,
                    resolveInstant(groupNode, "t"),
                    groupNode.getAttributeAsJid("participant").orElse(null));

            applyRestrict(metadata, groupNode.hasChild("locked"));
            applyAnnounce(metadata, groupNode.hasChild("announcement"));
            applyNoFrequentlyForwarded(metadata, groupNode.hasChild("no_frequently_forwarded"));

            var approvalState = groupNode.getChild("membership_approval_mode")
                    .flatMap(mam -> mam.getChild("group_join"))
                    .flatMap(gj -> gj.getAttributeAsString("state"))
                    .orElse(null);
            applyMembershipApproval(metadata, "on".equals(approvalState));

            var memberAddModeStr = groupNode.getChild("member_add_mode")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            applyMemberAddMode(metadata, "admin_add".equals(memberAddModeStr));

            var memberLinkModeContent = groupNode.getChild("member_link_mode")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            applyMemberLinkMode(metadata, memberLinkModeContent);

            applyLimitSharingEnabled(metadata, groupNode.hasChild("limit_sharing_enabled"));

            // notification <create> action node, not from the nested <group> element
            applyGeneralChatAutoAddDisabled(metadata, action.hasChild("auto_add_disabled"));

            if (metadata instanceof GroupMetadata groupMetadata) {
                groupMetadata.setSupport(groupNode.hasChild("support"));
                groupMetadata.setDefaultSubgroup(groupNode.hasChild("default_sub_group"));
                groupMetadata.setGeneralSubgroup(groupNode.hasChild("general_chat"));
                groupMetadata.setHiddenSubgroup(groupNode.hasChild("hidden_group"));
                groupMetadata.setGroupSafetyCheck(groupNode.hasChild("group_safety_check"));
                groupMetadata.setHasCapi(groupNode.hasChild("capi"));
                var size = groupNode.getAttributeAsInt("size", (Integer) null);
                if (size != null) {
                    groupMetadata.setSize(size);
                }

                groupNode.getChild("linked_parent")
                        .flatMap(lp -> lp.getAttributeAsJid("jid"))
                        .ifPresent(groupMetadata::setParentCommunityJid);

                var groupAdder = notification.getAttributeAsJid("participant").orElse(null);
                if (groupAdder != null) {
                    groupMetadata.setGroupAdder(groupAdder.toUserJid());
                }
            } else if (metadata instanceof CommunityMetadata communityMetadata) {
                // communities have these flags in the create stanza
                communityMetadata.setSupport(groupNode.hasChild("support"));
                communityMetadata.setDefaultSubgroup(groupNode.hasChild("default_sub_group"));
                communityMetadata.setGeneralSubgroup(groupNode.hasChild("general_chat"));
                communityMetadata.setHiddenSubgroup(groupNode.hasChild("hidden_group"));
                communityMetadata.setGroupSafetyCheck(groupNode.hasChild("group_safety_check"));
                communityMetadata.setHasCapi(groupNode.hasChild("capi"));
                var size = groupNode.getAttributeAsInt("size", (Integer) null);
                if (size != null) {
                    communityMetadata.setSize(size);
                }

                communityMetadata.setAllowNonAdminSubGroupCreation(
                        groupNode.hasChild("allow_non_admin_sub_group_creation"));

                // <parent default_membership_approval_mode="request_required">
                var parentClosed = groupNode.getChild("parent")
                        .flatMap(parent -> parent.getAttributeAsString("default_membership_approval_mode"))
                        .map("request_required"::equals)
                        .orElse(false);
                communityMetadata.setParentGroupClosed(parentClosed);
            }
        }
    }

    /**
     * Applies the {@code memberLinkMode} content string extracted from the
     * {@code <member_link_mode>} child of a create stanza to the metadata.
     * The WA Web parser maps {@code "admin_link"} to {@code ADMIN_LINK} and
     * {@code "all_member_link"} to {@code ALL_MEMBER_LINK}, ignoring unknown
     * values.
     *
     * @param metadata the metadata to update
     * @param content  the content string, or {@code null} if absent
     */
    private void applyMemberLinkMode(ChatMetadata metadata, String content) {
        if (content == null) {
            return;
        }
        if (metadata instanceof GroupMetadata groupMetadata) {
            if ("admin_link".equals(content)) {
                groupMetadata.setMemberLinkMode(ChatPolicy.ADMINS);
            } else if ("all_member_link".equals(content)) {
                groupMetadata.setMemberLinkMode(ChatPolicy.ANYONE);
            }
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            if ("admin_link".equals(content)) {
                communityMetadata.setMemberLinkModeAdminOnly(true);
            } else if ("all_member_link".equals(content)) {
                communityMetadata.setMemberLinkModeAdminOnly(false);
            }
        }
    }

    /**
     * Applies the {@code limitSharingEnabled} flag to the metadata.
     *
     * @param metadata the metadata to update
     * @param value    whether link/media sharing is limited to admins
     */
    private void applyLimitSharingEnabled(ChatMetadata metadata, boolean value) {
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setLimitSharingEnabled(value);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setLimitSharingEnabled(value);
        }
    }

    /**
     * Applies the {@code generalChatAutoAddDisabled} flag directly to a
     * metadata instance. Used both by the top-level {@code auto_add_disabled}
     * action and by the create flow which reads the nested child.
     *
     * @param metadata the metadata to update
     * @param value    whether auto-add to the general chat is disabled
     */
    private void applyGeneralChatAutoAddDisabled(ChatMetadata metadata, boolean value) {
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setGeneralChatAutoAddDisabled(value);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setGeneralChatAutoAddDisabled(value);
        }
    }

    /**
     * Handles the {@code subject} action by updating both the chat name and
     * the group metadata subject, subject timestamp, and subject author.
     *
     * @param notification the parent notification stanza
     * @param chat         the local chat entity
     * @param groupJid     the JID of the group
     * @param action       the {@code subject} action node
     */
    private void applySubject(Node notification, Chat chat, Jid groupJid, Node action) {
        var subject = action.getAttributeAsString("subject", null);
        if (subject == null) {
            return;
        }

        chat.setName(subject);
        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            setSubject(metadata, subject);
            setSubjectTimestamp(metadata, resolveInstant(action, "s_t", "t"), action.getAttributeAsJid("s_o").orElse(notification.getAttributeAsJid("participant").orElse(null)));
        }
    }

    /**
     * Handles the {@code description} action by extracting the description
     * body and identifier and applying them to both the chat and metadata.
     * When the action contains a {@code <delete>} child, the description is
     * cleared but the description identifier is still preserved.
     *
     * @param notification the parent notification stanza
     * @param chat         the local chat entity
     * @param groupJid     the JID of the group
     * @param action       the {@code description} action node
     */
    private void applyDescription(Node notification, Chat chat, Jid groupJid, Node action) {
        var deleted = action.hasChild("delete");
        var description = deleted ? null : resolveDescriptionBody(action);
        var descriptionId = resolveDescriptionId(action);
        var timestamp = resolveInstant(action, "t");
        if (timestamp == null) {
            timestamp = resolveInstant(notification, "t");
        }
        var author = action.getAttributeAsJid("participant").orElse(notification.getAttributeAsJid("participant").orElse(null));

        chat.setDescription(description);
        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            setDescription(metadata, description, descriptionId, timestamp, author);
        }
    }

    /**
     * Applies the {@code restrict} (locked/unlocked) toggle to group or
     * community metadata.
     *
     * @param groupJid   the JID of the group
     * @param restricted whether metadata editing is restricted to admins
     */
    private void applyRestrict(Jid groupJid, boolean restricted) {
        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            applyRestrict(metadata, restricted);
        }
    }

    /**
     * Applies the {@code restrict} setting directly to a metadata instance.
     *
     * @param metadata   the metadata to update
     * @param restricted whether metadata editing is restricted to admins
     */
    private void applyRestrict(ChatMetadata metadata, boolean restricted) {
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setRestrict(ChatPolicy.of(restricted));
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setRestrict(restricted);
        }
    }

    /**
     * Applies the {@code announce} (announcement/not_announcement) toggle
     * to group or community metadata.
     *
     * @param groupJid        the JID of the group
     * @param announcementOnly whether only admins can send messages
     */
    private void applyAnnounce(Jid groupJid, boolean announcementOnly) {
        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            applyAnnounce(metadata, announcementOnly);
        }
    }

    /**
     * Applies the {@code announce} setting directly to a metadata instance.
     *
     * @param metadata         the metadata to update
     * @param announcementOnly whether only admins can send messages
     */
    private void applyAnnounce(ChatMetadata metadata, boolean announcementOnly) {
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setAnnounce(ChatPolicy.of(announcementOnly));
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setAnnounce(announcementOnly);
        }
    }

    /**
     * Applies the {@code no_frequently_forwarded} /
     * {@code frequently_forwarded_ok} toggle to group or community metadata.
     *
     * @param groupJid the JID of the group
     * @param value    whether frequently forwarded messages are blocked
     */
    private void applyNoFrequentlyForwarded(Jid groupJid, boolean value) {
        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            applyNoFrequentlyForwarded(metadata, value);
        }
    }

    /**
     * Applies the {@code noFrequentlyForwarded} setting directly to a
     * metadata instance.
     *
     * @param metadata the metadata to update
     * @param value    whether frequently forwarded messages are blocked
     */
    private void applyNoFrequentlyForwarded(ChatMetadata metadata, boolean value) {
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setNoFrequentlyForwarded(value);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setNoFrequentlyForwarded(value);
        }
    }

    /**
     * Handles the {@code ephemeral} action by extracting the expiration
     * duration and applying it to both the chat and group metadata.
     *
     * @param notification the parent notification or group node
     * @param chat         the local chat entity
     * @param groupJid     the JID of the group
     * @param action       the {@code ephemeral} action node
     */
    private void applyEphemeral(Node notification, Chat chat, Jid groupJid, Node action) {
        var ephemeralNode = action.getChild("ephemeral").orElse(null);
        Node expirationSource;
        if (ephemeralNode != null) {
            expirationSource = ephemeralNode;
        } else {
            expirationSource = action;
        }

        var expiration = expirationSource.getAttributeAsInt("expiration", (Integer) null);
        if (expiration == null) {
            return;
        }

        var timer = ChatEphemeralTimer.of(expiration);
        var timestamp = resolveInstant(action, "t");
        if (timestamp == null) {
            timestamp = resolveInstant(notification, "t");
        }

        chat.setEphemeralExpiration(timer);
        if (timestamp != null) {
            chat.setEphemeralSettingTimestamp(timestamp);
        }

        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            metadata.setEphemeralExpiration(timer);
        }
    }

    /**
     * Handles the {@code not_ephemeral} action by clearing the ephemeral
     * timer on both the chat and group metadata.
     *
     * @param chat     the local chat entity
     * @param groupJid the JID of the group
     */
    private void clearEphemeral(Chat chat, Jid groupJid) {
        chat.setEphemeralExpiration(null);
        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            metadata.setEphemeralExpiration(null);
        }
    }

    /**
     * Handles the {@code growth_locked} action by extracting the lock
     * expiration timestamp and lock type from the action attributes.
     *
     * @param groupJid the JID of the group
     * @param action   the {@code growth_locked} action node
     */
    private void applyGrowthLock(Jid groupJid, Node action) {
        var metadata = currentMetadata(groupJid);
        var expiration = resolveInstant(action, "expiration");
        var type = action.getAttributeAsString("type", null);
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setGrowthLockExpiration(expiration);
            groupMetadata.setGrowthLockType(type);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setGrowthLockExpiration(expiration);
            communityMetadata.setGrowthLockType(type);
        }
    }

    /**
     * Handles the {@code growth_unlocked} action by clearing the growth lock
     * fields on the metadata.
     *
     * @param groupJid the JID of the group
     */
    private void clearGrowthLock(Jid groupJid) {
        var metadata = currentMetadata(groupJid);
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setGrowthLockExpiration(null);
            groupMetadata.setGrowthLockType(null);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setGrowthLockExpiration(null);
            communityMetadata.setGrowthLockType(null);
        }
    }

    /**
     * Handles the {@code link} action by extracting the link type and
     * updating the parent community reference when a {@code parent_group}
     * link is established.
     *
     * @param groupJid the JID of the group
     * @param action   the {@code link} action node
     */
    private void applyLink(Jid groupJid, Node action) {
        var linkType = action.getAttributeAsString("link_type", null);
        var metadata = currentMetadata(groupJid);
        if (metadata instanceof GroupMetadata groupMetadata && "parent_group".equals(linkType)) {
            action.getChildren("group").stream()
                    .map(child -> child.getAttributeAsJid("jid").orElse(null))
                    .filter(related -> related != null && related.hasGroupOrCommunityServer())
                    .findFirst()
                    .ifPresent(groupMetadata::setParentCommunityJid);
        }
    }

    /**
     * Handles the {@code unlink} action by clearing the parent community
     * reference when a {@code parent_group} unlink occurs.
     *
     * @param groupJid the JID of the group
     * @param action   the {@code unlink} action node
     */
    private void applyUnlink(Jid groupJid, Node action) {
        var unlinkType = action.getAttributeAsString("unlink_type", null);
        var metadata = currentMetadata(groupJid);
        if (metadata instanceof GroupMetadata groupMetadata && "parent_group".equals(unlinkType)) {
            groupMetadata.setParentCommunityJid(null);
        }
    }

    /**
     * Handles the {@code membership_approval_mode} action by extracting the
     * {@code state} from the {@code <group_join>} child.
     *
     * @param action   the {@code membership_approval_mode} action node
     * @param groupJid the JID of the group
     */
    private void applyMembershipApproval(Node action, Jid groupJid) {
        var state = action.getChild("group_join")
                .flatMap(child -> child.getAttributeAsString("state"))
                .orElse(null);
        var enabled = "on".equals(state);
        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            applyMembershipApproval(metadata, enabled);
        }
    }

    /**
     * Applies the membership approval mode directly to a metadata instance.
     *
     * @param metadata the metadata to update
     * @param enabled  whether membership approval is required
     */
    private void applyMembershipApproval(ChatMetadata metadata, boolean enabled) {
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setMembershipApprovalMode(ChatPolicy.of(enabled));
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setMembershipApprovalMode(enabled);
        }
    }

    /**
     * Handles the {@code allow_admin_reports} / {@code not_allow_admin_reports}
     * action toggle.
     *
     * @param groupJid the JID of the group
     * @param value    whether reporting to admin is enabled
     */
    private void applyReportToAdmin(Jid groupJid, boolean value) {
        var metadata = currentMetadata(groupJid);
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setReportToAdminMode(value);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setReportToAdminMode(value);
        }
    }

    /**
     * Handles the {@code allow_non_admin_sub_group_creation} /
     * {@code not_allow_non_admin_sub_group_creation} action toggle.
     *
     * @param groupJid the JID of the group
     * @param value    whether non-admin subgroup creation is allowed
     */
    private void applyNonAdminSubgroupCreation(Jid groupJid, boolean value) {
        var metadata = currentMetadata(groupJid);
        if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setAllowNonAdminSubGroupCreation(value);
        }
    }

    /**
     * Handles the {@code member_add_mode} action by extracting the content
     * string and checking whether it is {@code "admin_add"}.
     *
     * @param groupJid the JID of the group
     * @param action   the {@code member_add_mode} action node
     */
    private void applyMemberAddMode(Jid groupJid, Node action) {
        var adminOnly = "admin_add".equals(action.toContentString().orElse(null));
        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            applyMemberAddMode(metadata, adminOnly);
        }
    }

    /**
     * Applies the member-add-mode setting directly to a metadata instance.
     *
     * @param metadata  the metadata to update
     * @param adminOnly whether only admins can add members
     */
    private void applyMemberAddMode(ChatMetadata metadata, boolean adminOnly) {
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setMemberAddMode(ChatPolicy.of(adminOnly));
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setMemberAddModeAdminOnly(adminOnly);
        }
    }

    /**
     * Handles the {@code auto_add_disabled} action by setting the
     * {@code generalChatAutoAddDisabled} flag on the metadata.
     *
     * @param groupJid the JID of the group
     * @param value    whether auto-add to general chat is disabled
     */
    private void applyGeneralChatAutoAddDisabled(Jid groupJid, boolean value) {
        var metadata = currentMetadata(groupJid);
        if (metadata != null) {
            applyGeneralChatAutoAddDisabled(metadata, value);
        }
    }

    /**
     * Handles the {@code group_safety_check} action by setting the
     * corresponding flag on the metadata.
     *
     * @param groupJid the JID of the group
     * @param value    whether the group safety check flag is set
     */
    private void applyGroupSafetyCheck(Jid groupJid, boolean value) {
        var metadata = currentMetadata(groupJid);
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setGroupSafetyCheck(value);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setGroupSafetyCheck(value);
        }
    }

    /**
     * Handles the {@code suspended} / {@code unsuspended} action by updating
     * both the chat and the metadata suspended flag.
     *
     * @param chat     the local chat entity
     * @param groupJid the JID of the group
     * @param value    whether the group is suspended
     */
    private void applySuspended(Chat chat, Jid groupJid, boolean value) {
        chat.setSuspended(value);
        var metadata = currentMetadata(groupJid);
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setSuspended(value);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setSuspended(value);
        }
    }

    /**
     * Parses participant child nodes and applies the mutation (add, remove,
     * promote, demote, or modify) to the metadata's participant set.
     *
     * @param groupJid the JID of the group
     * @param action   the action node containing {@code <participant>} children
     * @param mutation the type of participant change
     */
    private void applyParticipants(Jid groupJid, Node action, GroupParticipantMutation mutation) {
        var metadata = currentMetadata(groupJid);
        if (metadata == null) {
            return;
        }

        for (var participant : parseParticipants(action, mutation)) {
            switch (mutation) {
                case ADD, MODIFY -> {
                    metadata.removeParticipant(participant.userJid());
                    metadata.addParticipant(participant);
                }
                case REMOVE -> metadata.removeParticipant(participant.userJid());
                case PROMOTE, DEMOTE -> {
                    metadata.removeParticipant(participant.userJid());
                    metadata.addParticipant(participant);
                }
            }
        }
    }

    /**
     * Parses all {@code <participant>} child nodes of the given action into
     * a set of {@link GroupParticipant} objects with the role determined by
     * the mutation type and the {@code type} attribute.
     *
     * @param action   the action node containing participant children
     * @param mutation the type of participant change
     * @return an ordered set of parsed participants
     */
    private LinkedHashSet<GroupParticipant> parseParticipants(Node action, GroupParticipantMutation mutation) {
        var participants = new LinkedHashSet<GroupParticipant>();
        for (var participantNode : action.getChildren("participant")) {
            var jid = participantNode.getAttributeAsJid("jid").orElse(null);
            if (jid == null) {
                continue;
            }

            // GROUP_PARTICIPANT_TYPES mapping
            var role = switch (mutation) {
                case PROMOTE -> GroupPartipantRole.ADMIN;
                case DEMOTE -> GroupPartipantRole.USER;
                default -> parseRole(participantNode.getAttributeAsString("type", null));
            };

            participants.add(new GroupParticipantBuilder()
                    .userJid(jid.toUserJid())
                    .rank(role)
                    .build());
        }
        return participants;
    }

    /**
     * Parses a participant type string into a {@link GroupPartipantRole},
     * falling back to {@link GroupPartipantRole#USER} for unknown or absent
     * values.
     *
     * @param type the type string from the {@code type} attribute, or
     *             {@code null}
     * @return the corresponding role, never {@code null}
     */
    private GroupPartipantRole parseRole(String type) {
        if (type == null || type.isBlank()) {
            return GroupPartipantRole.USER;
        }

        try {
            return GroupPartipantRole.of(type);
        } catch (RuntimeException exception) {
            return GroupPartipantRole.USER;
        }
    }

    /**
     * Collects all group JIDs referenced in the action's children (including
     * {@code <group>} children, {@code <sub_group_suggestion>} children, and
     * {@code context_group_jid} / {@code parent_group_jid} attributes) into
     * the related groups set for post-processing refresh.
     *
     * @param action        the action node to scan
     * @param relatedGroups the mutable set to collect JIDs into
     */
    private void collectRelatedGroups(Node action, LinkedHashSet<Jid> relatedGroups) {
        action.streamChildren("group")
                .map(child -> child.getAttributeAsJid("jid").orElse(null))
                .filter(jid -> jid != null && jid.hasGroupOrCommunityServer())
                .forEach(relatedGroups::add);

        action.streamChildren("sub_group_suggestion")
                .map(child -> child.getAttributeAsJid("jid").orElse(null))
                .filter(jid -> jid != null && jid.hasGroupOrCommunityServer())
                .forEach(relatedGroups::add);

        action.getAttributeAsJid("context_group_jid")
                .filter(Jid::hasGroupOrCommunityServer)
                .ifPresent(relatedGroups::add);
        action.getAttributeAsJid("parent_group_jid")
                .filter(Jid::hasGroupOrCommunityServer)
                .ifPresent(relatedGroups::add);
    }

    /**
     * Looks up the current group or community metadata from the store for
     * the given JID.
     *
     * @param groupJid the JID of the group or community
     * @return the metadata, or {@code null} if not found
     */
    private ChatMetadata currentMetadata(Jid groupJid) {
        return whatsapp.store().findChatMetadata(groupJid).orElse(null);
    }

    /**
     * Sets the subject on a metadata instance if the subject is non-null.
     *
     * @param metadata the metadata to update
     * @param subject  the new subject, or {@code null} to skip
     */
    private void setSubject(ChatMetadata metadata, String subject) {
        if (subject == null) {
            return;
        }

        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setSubject(subject);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setSubject(subject);
        }
    }

    /**
     * Sets the subject timestamp and author on a metadata instance.
     *
     * @param metadata  the metadata to update
     * @param timestamp the instant the subject was changed, or {@code null}
     * @param author    the JID of the subject author, or {@code null}
     */
    private void setSubjectTimestamp(ChatMetadata metadata, Instant timestamp, Jid author) {
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setSubjectTimestamp(timestamp);
            groupMetadata.setSubjectAuthorJid(author);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setSubjectTimestamp(timestamp);
            communityMetadata.setSubjectAuthorJid(author);
        }
    }

    /**
     * Sets the description, description identifier, timestamp, and author on
     * a metadata instance.
     *
     * @param metadata    the metadata to update
     * @param description the new description text, or {@code null} if cleared
     * @param id          the server-assigned description revision identifier
     * @param timestamp   the instant the description was changed
     * @param author      the JID of the description author
     */
    private void setDescription(ChatMetadata metadata, String description, String id, Instant timestamp, Jid author) {
        if (metadata instanceof GroupMetadata groupMetadata) {
            groupMetadata.setDescription(description);
            groupMetadata.setDescriptionId(id);
            groupMetadata.setDescriptionTimestamp(timestamp);
            groupMetadata.setDescriptionAuthorJid(author);
        } else if (metadata instanceof CommunityMetadata communityMetadata) {
            communityMetadata.setDescription(description);
            communityMetadata.setDescriptionId(id);
            communityMetadata.setDescriptionTimestamp(timestamp);
            communityMetadata.setDescriptionAuthorJid(author);
        }
    }

    /**
     * Extracts the description body for a {@code create} action by
     * navigating the {@code description > body} child path, matching the
     * WA Web {@code g()} helper function.
     *
     * @param groupNode the {@code <group>} element from the create stanza
     * @return the description body text, or {@code null} if not present
     */
    private String resolveCreateDescriptionBody(Node groupNode) {
        return groupNode.getChild("description")
                .flatMap(desc -> desc.getChild("body"))
                .flatMap(Node::toContentString)
                .orElse(null);
    }

    /**
     * Extracts the description identifier for a {@code create} action by
     * reading the {@code id} attribute from the {@code <description>} child,
     * matching the WA Web {@code g()} helper function.
     *
     * @param groupNode the {@code <group>} element from the create stanza
     * @return the description identifier, or {@code null} if not present
     */
    private String resolveCreateDescriptionId(Node groupNode) {
        return groupNode.getChild("description")
                .flatMap(desc -> desc.getAttributeAsString("id"))
                .orElse(null);
    }

    /**
     * Extracts the description body from a non-create description action
     * node by reading the {@code <body>} child's content.
     *
     * @param node the description action node
     * @return the body text, or {@code null} if no body is present
     */
    private String resolveDescriptionBody(Node node) {
        return node.getChild("body")
                .flatMap(Node::toContentString)
                .orElse(null);
    }

    /**
     * Extracts the description identifier from a description action node.
     *
     * @param node the description action node
     * @return the description identifier, or {@code null} if absent
     */
    private String resolveDescriptionId(Node node) {
        return node.getAttributeAsString("id", null);
    }

    /**
     * Resolves the first available timestamp from the given attribute keys,
     * returning an {@link Instant} if a positive epoch-seconds value is
     * found.
     *
     * @param node the node to read attributes from
     * @param keys the attribute key names to try in order
     * @return the resolved instant, or {@code null} if no valid timestamp is
     *         found
     */
    private Instant resolveInstant(Node node, String... keys) {
        for (var key : keys) {
            var epoch = node.getAttributeAsLong(key, (Long) null);
            if (epoch != null && epoch > 0) {
                return Instant.ofEpochSecond(epoch);
            }
        }
        return null;
    }

    /**
     * Triggers a full metadata refresh for the given group JID by querying
     * the server. Any failure is logged at {@code DEBUG} level and
     * suppressed.
     *
     * @param groupJid the JID of the group to refresh
     */
    private void refreshGroup(Jid groupJid) {
        try {
            whatsapp.queryChatMetadata(groupJid);
        } catch (Throwable throwable) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Cannot refresh group metadata for {0}: {1}",
                    groupJid,
                    throwable.getMessage());
        }
    }

    /**
     * Enumerates the types of participant mutations that can be applied to
     * the group metadata's participant set.
     */
    private enum GroupParticipantMutation {
        /** A participant was added to the group. */
        ADD,
        /** A participant was removed from the group. */
        REMOVE,
        /** A participant was promoted to admin. */
        PROMOTE,
        /** A participant was demoted from admin. */
        DEMOTE,
        /** A participant's metadata was modified. */
        MODIFY
    }

    /**
     * Sends an acknowledgement stanza for the processed group notification.
     * The ACK is constructed with hardcoded {@code class="notification"} and
     * {@code type="w:gp2"} values matching the WA Web implementation.
     *
     * @param node the original notification node
     */
    private void sendNotificationAck(Node node) {
        var stanzaId = node.getAttributeAsString("id", null);
        var stanzaFrom = node.getAttributeAsJid("from", null);
        if (stanzaId == null || stanzaFrom == null) {
            return;
        }

        whatsapp.sendNodeWithNoResponse(new NodeBuilder()
                .description("ack")
                .attribute("id", stanzaId)
                .attribute("class", "notification")
                .attribute("to", stanzaFrom)
                .attribute("type", "w:gp2")
                .attribute("participant", node.getAttributeAsJid("participant", null))
                .build());
    }
}
