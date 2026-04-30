package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Holds the fully parsed structural representation of an incoming {@code <message>}
 * stanza before the payload is decrypted.
 *
 * <p>Captures every metadata field extracted from the raw XML node: identifier and
 * timestamp, addressing information (including the LID/PN migration fields), the
 * encrypted payloads, the bot/business/payment metadata, the reporting tokens, the
 * broadcast participant lists, and every {@code <meta>} attribute. Decryption and
 * downstream processing operate on this structured form rather than re-scanning the
 * raw node.
 *
 * @implNote WA Web produces equivalent data in separate {@code msgInfo},
 * {@code msgMeta}, {@code encs}, {@code deviceIdentity}, {@code bizInfo},
 * {@code hsmInfo}, {@code paymentInfo}, {@code rcat}, {@code msgBotInfo}, and
 * {@code reportingTokenInfo} objects. Cobalt merges them into a single cohesive
 * container.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
@WhatsAppWebModule(moduleName = "WAWebHandleMsgCommon")
public final class MessageReceiveStanza {
    /**
     * {@code edit} attribute value representing the absence of an edit.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "EDIT_ATTR",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int EDIT_NONE = 0;

    /**
     * {@code edit} attribute value indicating a message edit.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "EDIT_ATTR",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int EDIT_MESSAGE = 1;

    /**
     * {@code edit} attribute value indicating a pin-in-chat operation.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "EDIT_ATTR",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int EDIT_PIN = 2;

    /**
     * {@code edit} attribute value indicating that the sender revoked the message.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "EDIT_ATTR",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int EDIT_SENDER_REVOKE = 7;

    /**
     * {@code edit} attribute value indicating that an admin revoked the message.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "EDIT_ATTR",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int EDIT_ADMIN_REVOKE = 8;

    /**
     * {@code context_source} value identifying a stanza originating from a channel
     * invitation.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "CONTEXT_SOURCE",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String CONTEXT_SOURCE_CHANNELS_INVITATION = "channels_invitation";

    /**
     * Stanza's {@code id} attribute.
     */
    private final String id;

    /**
     * Message timestamp parsed from the {@code t} attribute.
     */
    private final Instant timestamp;

    /**
     * Chat JID derived from the {@code from} attribute. For 1:1 messages this equals
     * the sender; for groups, broadcasts, and status feeds this identifies the
     * group/broadcast/status chat.
     */
    private final Jid chatJid;

    /**
     * Actual sender's device JID. For 1:1 messages this equals {@link #chatJid}; for
     * groups and broadcasts it is the {@code participant} attribute.
     */
    private final Jid senderJid;

    /**
     * {@code participant} attribute present on group, broadcast, and status messages,
     * identifying the sender's device within the group.
     */
    private final Jid participant;

    /**
     * Classified message type derived from the addressing.
     */
    private final MessageType messageType;

    /**
     * {@code edit} attribute, defaulting to {@link #EDIT_NONE}.
     */
    private final int editAttribute;

    /**
     * Sender's push name from the {@code notify} attribute.
     */
    private final String pushName;

    /**
     * Message category. The only defined value is {@code "peer"} for peer protocol
     * messages.
     */
    private final String category;

    /**
     * {@code offline} attribute value, present on messages delivered while the client
     * was offline.
     */
    private final String offline;

    /**
     * Addressing mode for group messages: either {@code "pn"} (phone number) or
     * {@code "lid"}.
     */
    private final String addressingMode;

    /**
     * Whether the stanza has an {@code <hsm>} child indicating a highly structured
     * message (business template).
     */
    private final boolean isHsm;

    /**
     * Optional {@code count} attribute.
     */
    private final Integer count;

    /**
     * Sender's phone number JID from the {@code sender_pn} attribute. Used on
     * LID-addressed groups to carry the PN mapping.
     */
    private final Jid senderPn;

    /**
     * Sender's LID from the {@code sender_lid} attribute.
     */
    private final Jid senderLid;

    /**
     * Recipient's phone number from the {@code recipient_pn} attribute, present on
     * peer messages.
     */
    private final Jid recipientPn;

    /**
     * Recipient's LID from the {@code recipient_lid} attribute.
     */
    private final Jid recipientLid;

    /**
     * Peer recipient's phone number from the {@code peer_recipient_pn} attribute,
     * used on peer broadcast messages.
     */
    private final Jid peerRecipientPn;

    /**
     * Peer recipient's LID from the {@code peer_recipient_lid} attribute.
     */
    private final Jid peerRecipientLid;

    /**
     * Peer recipient's username from the {@code peer_recipient_username} attribute.
     */
    private final String peerRecipientUsername;

    /**
     * Most recent LID known for the recipient from the {@code recipient_latest_lid}
     * attribute.
     */
    private final Jid recipientLatestLid;

    /**
     * Recipient's username from the {@code recipient_username} attribute.
     */
    private final String recipientUsername;

    /**
     * Group participant's phone number from the {@code participant_pn} attribute.
     */
    private final Jid participantPn;

    /**
     * Group participant's LID from the {@code participant_lid} attribute.
     */
    private final Jid participantLid;

    /**
     * Group participant's username from the {@code participant_username} attribute.
     */
    private final String participantUsername;

    /**
     * Sender's username from the {@code username} attribute.
     */
    private final String username;

    /**
     * Sender's display name from the {@code display_name} attribute.
     */
    private final String displayName;

    /**
     * Stanza's {@code type} attribute. Defined values are {@code "text"},
     * {@code "media"}, {@code "medianotify"}, {@code "pay"}, {@code "poll"},
     * {@code "reaction"}, and {@code "event"}.
     */
    private final String stanzaType;

    /**
     * Whether the stanza has an {@code <unavailable>} child indicating the payload is
     * absent (fanout placeholder).
     */
    private final boolean unavailable;

    /**
     * Whether the {@code <unavailable>} child carries {@code hosted="true"}, meaning
     * a hosted-device fanout placeholder.
     */
    private final boolean hostedUnavailable;

    /**
     * Whether the {@code <unavailable>} child carries {@code type="view_once"},
     * meaning a view-once fanout placeholder.
     */
    private final boolean viewOnceUnavailable;

    /**
     * {@code polltype} attribute from the {@code <meta>} node. Defined values are
     * {@code "creation"}, {@code "quiz_creation"}, {@code "vote"},
     * {@code "result_snapshot"}, and {@code "edit"}.
     */
    private final String pollType;

    /**
     * {@code event_type} attribute from the {@code <meta>} node. Defined values are
     * {@code "creation"}, {@code "response"}, and {@code "edit"}. Populated only when
     * the stanza type is {@code "event"}.
     */
    private final String eventType;

    /**
     * {@code origin} attribute from the {@code <meta>} node. The only defined value
     * is {@code "ctwa"} (click-to-WhatsApp ads).
     */
    private final String origin;

    /**
     * Whether the stanza has a {@code <url_number>} child.
     */
    private final boolean urlNumber;

    /**
     * Whether the stanza has a {@code <url_text>} child.
     */
    private final boolean urlText;

    /**
     * Whether the {@code <meta>} node has {@code status_mentioned="true"}.
     */
    private final boolean statusMentioned;

    /**
     * {@code appdata} attribute from the {@code <meta>} node. Defined values are
     * {@code "default"}, {@code "member_tag"}, and {@code "group_history"}.
     */
    private final String appdata;

    /**
     * {@code biz_source} attribute from the {@code <meta>} node.
     */
    private final String bizSource;

    /**
     * {@code thread_msg_id} attribute from the {@code <meta>} node, identifying the
     * parent message of a comment thread.
     */
    private final String threadMsgId;

    /**
     * {@code thread_msg_sender_jid} attribute from the {@code <meta>} node.
     */
    private final Jid threadMsgSenderJid;

    /**
     * {@code target_id} attribute from the {@code <meta>} node, referencing the
     * parent message for addon messages (reactions, poll votes, etc.).
     */
    private final String targetId;

    /**
     * {@code target_sender_jid} attribute from the {@code <meta>} node.
     */
    private final Jid targetSenderJid;

    /**
     * {@code target_chat_jid} attribute from the {@code <meta>} node.
     */
    private final Jid targetChatJid;

    /**
     * {@code target_chat_jid_lid} attribute from the {@code <meta>} node.
     */
    private final Jid targetChatJidLid;

    /**
     * Whether the {@code <meta>} node has {@code capi="true"}.
     */
    private final boolean capi;

    /**
     * {@code context_source} attribute from the {@code <meta>} node. The known value
     * is {@code "channels_invitation"}.
     */
    private final String contextSource;

    /**
     * Sender's country code parsed from the {@code <meta>} node's
     * {@code sender_country_code} attribute.
     */
    private final String senderCountryCode;

    /**
     * List of encrypted payloads parsed from {@code <enc>} child nodes.
     */
    private final List<MessageReceiveEncryptedPayload> encs;

    /**
     * Device identity bytes from the {@code <device-identity>} child, used for ADV
     * validation of companion devices.
     */
    private final byte[] deviceIdentity;

    /**
     * Parsed bot info from the {@code <bot>} child.
     */
    private final MessageReceiveBotInfo botInfo;

    /**
     * Parsed business info from stanza attributes and the {@code <biz>} child.
     */
    private final MessageReceiveBizInfo bizInfo;

    /**
     * Parsed reporting token info from the {@code <reporting>} child.
     */
    private final MessageReceiveReportingInfo reportingInfo;

    /**
     * List of broadcast contact list participants from the {@code <participants>}
     * child. Populated for PEER_BROADCAST and DIRECT_PEER_STATUS messages.
     */
    private final List<MessageReceiveBroadcastParticipant> bclParticipants;

    /**
     * Parsed payment info from the {@code <pay>} and {@code <transaction>} children.
     */
    private final MessageReceivePaymentInfo paymentInfo;

    /**
     * Content bytes of the {@code <rcat>} child node, used for content binding
     * verification.
     */
    private final byte[] rcat;

    /**
     * Stanza-level {@code eph_setting} attribute, present on OTHER_BROADCAST messages
     * for ephemeral message settings.
     */
    private final String ephSetting;

    /**
     * {@code tag} attribute from the {@code <hsm>} child node.
     */
    private final String hsmTag;

    /**
     * {@code category} attribute from the {@code <hsm>} child node.
     */
    private final String hsmCategory;

    /**
     * Constructs a new parsed stanza record.
     *
     * <p>Intended to be called by {@link MessageReceiveStanzaParser} after completing
     * its extraction pass; callers outside the parser rarely need to invoke it
     * directly.
     *
     * @param id                    the stanza identifier
     * @param timestamp             the message timestamp
     * @param chatJid               the chat JID from the {@code from} attribute
     * @param senderJid             the sender's device JID
     * @param participant           the participant attribute, or {@code null}
     * @param messageType           the classified message type
     * @param editAttribute         the edit attribute value
     * @param pushName              the sender push name, or {@code null}
     * @param category              the message category, or {@code null}
     * @param offline               the offline attribute, or {@code null}
     * @param addressingMode        the addressing mode, or {@code null}
     * @param isHsm                 whether the stanza carries an HSM child
     * @param count                 the count attribute, or {@code null}
     * @param senderPn              the sender phone-number JID, or {@code null}
     * @param senderLid             the sender LID, or {@code null}
     * @param recipientPn           the recipient phone-number JID, or {@code null}
     * @param recipientLid          the recipient LID, or {@code null}
     * @param peerRecipientPn       the peer recipient phone-number JID, or {@code null}
     * @param peerRecipientLid      the peer recipient LID, or {@code null}
     * @param peerRecipientUsername the peer recipient username, or {@code null}
     * @param recipientLatestLid    the latest known recipient LID, or {@code null}
     * @param recipientUsername     the recipient username, or {@code null}
     * @param participantPn         the participant phone-number JID, or {@code null}
     * @param participantLid        the participant LID, or {@code null}
     * @param participantUsername   the participant username, or {@code null}
     * @param username              the sender username, or {@code null}
     * @param displayName           the sender display name, or {@code null}
     * @param stanzaType            the stanza type
     * @param unavailable           whether the stanza is unavailable
     * @param hostedUnavailable     whether the unavailable placeholder is hosted
     * @param viewOnceUnavailable   whether the unavailable placeholder is a view-once
     * @param pollType              the poll type, or {@code null}
     * @param eventType             the event type, or {@code null}
     * @param origin                the origin attribute, or {@code null}
     * @param urlNumber             whether the stanza has a url_number child
     * @param urlText               whether the stanza has a url_text child
     * @param statusMentioned       whether the status mentions the user
     * @param appdata               the appdata attribute, or {@code null}
     * @param bizSource             the biz_source attribute, or {@code null}
     * @param threadMsgId           the thread parent message id, or {@code null}
     * @param threadMsgSenderJid    the thread parent sender JID, or {@code null}
     * @param targetId              the target message id, or {@code null}
     * @param targetSenderJid       the target sender JID, or {@code null}
     * @param targetChatJid         the target chat JID, or {@code null}
     * @param targetChatJidLid      the target chat LID, or {@code null}
     * @param capi                  whether the stanza has capi="true"
     * @param contextSource         the context_source value, or {@code null}
     * @param senderCountryCode     the sender country code, or {@code null}
     * @param encs                  the list of encrypted payloads
     * @param deviceIdentity        the device identity bytes, or {@code null}
     * @param botInfo               the parsed bot info, or {@code null}
     * @param bizInfo               the parsed biz info, or {@code null}
     * @param reportingInfo         the parsed reporting info, or {@code null}
     * @param bclParticipants       the broadcast participant list
     * @param paymentInfo           the parsed payment info, or {@code null}
     * @param ephSetting            the stanza-level eph_setting, or {@code null}
     * @param rcat                  the rcat content bytes, or {@code null}
     * @param hsmTag                the HSM tag, or {@code null}
     * @param hsmCategory           the HSM category, or {@code null}
     * @throws NullPointerException if any non-nullable argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public MessageReceiveStanza(
            String id,
            Instant timestamp,
            Jid chatJid,
            Jid senderJid,
            Jid participant,
            MessageType messageType,
            int editAttribute,
            String pushName,
            String category,
            String offline,
            String addressingMode,
            boolean isHsm,
            Integer count,
            Jid senderPn,
            Jid senderLid,
            Jid recipientPn,
            Jid recipientLid,
            Jid peerRecipientPn,
            Jid peerRecipientLid,
            String peerRecipientUsername,
            Jid recipientLatestLid,
            String recipientUsername,
            Jid participantPn,
            Jid participantLid,
            String participantUsername,
            String username,
            String displayName,
            String stanzaType,
            boolean unavailable,
            boolean hostedUnavailable,
            boolean viewOnceUnavailable,
            String pollType,
            String eventType,
            String origin,
            boolean urlNumber,
            boolean urlText,
            boolean statusMentioned,
            String appdata,
            String bizSource,
            String threadMsgId,
            Jid threadMsgSenderJid,
            String targetId,
            Jid targetSenderJid,
            Jid targetChatJid,
            Jid targetChatJidLid,
            boolean capi,
            String contextSource,
            String senderCountryCode,
            List<MessageReceiveEncryptedPayload> encs,
            byte[] deviceIdentity,
            MessageReceiveBotInfo botInfo,
            MessageReceiveBizInfo bizInfo,
            MessageReceiveReportingInfo reportingInfo,
            List<MessageReceiveBroadcastParticipant> bclParticipants,
            MessageReceivePaymentInfo paymentInfo,
            String ephSetting,
            byte[] rcat,
            String hsmTag,
            String hsmCategory
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.chatJid = Objects.requireNonNull(chatJid, "chatJid");
        this.senderJid = Objects.requireNonNull(senderJid, "senderJid");
        this.participant = participant;
        this.messageType = Objects.requireNonNull(messageType, "messageType");
        this.editAttribute = editAttribute;
        this.pushName = pushName;
        this.category = category;
        this.offline = offline;
        this.addressingMode = addressingMode;
        this.isHsm = isHsm;
        this.count = count;
        this.senderPn = senderPn;
        this.senderLid = senderLid;
        this.recipientPn = recipientPn;
        this.recipientLid = recipientLid;
        this.peerRecipientPn = peerRecipientPn;
        this.peerRecipientLid = peerRecipientLid;
        this.peerRecipientUsername = peerRecipientUsername;
        this.recipientLatestLid = recipientLatestLid;
        this.recipientUsername = recipientUsername;
        this.participantPn = participantPn;
        this.participantLid = participantLid;
        this.participantUsername = participantUsername;
        this.username = username;
        this.displayName = displayName;
        this.stanzaType = Objects.requireNonNull(stanzaType, "stanzaType");
        this.unavailable = unavailable;
        this.hostedUnavailable = hostedUnavailable;
        this.viewOnceUnavailable = viewOnceUnavailable;
        this.pollType = pollType;
        this.eventType = eventType;
        this.origin = origin;
        this.urlNumber = urlNumber;
        this.urlText = urlText;
        this.statusMentioned = statusMentioned;
        this.appdata = appdata;
        this.bizSource = bizSource;
        this.threadMsgId = threadMsgId;
        this.threadMsgSenderJid = threadMsgSenderJid;
        this.targetId = targetId;
        this.targetSenderJid = targetSenderJid;
        this.targetChatJid = targetChatJid;
        this.targetChatJidLid = targetChatJidLid;
        this.capi = capi;
        this.contextSource = contextSource;
        this.senderCountryCode = senderCountryCode;
        this.encs = List.copyOf(Objects.requireNonNull(encs, "encs"));
        this.deviceIdentity = deviceIdentity;
        this.botInfo = botInfo;
        this.bizInfo = bizInfo;
        this.reportingInfo = reportingInfo;
        this.bclParticipants = bclParticipants != null ? List.copyOf(bclParticipants) : List.of();
        this.paymentInfo = paymentInfo;
        this.ephSetting = ephSetting;
        this.rcat = rcat;
        this.hsmTag = hsmTag;
        this.hsmCategory = hsmCategory;
    }

    /**
     * Returns the stanza's {@code id} attribute.
     *
     * @return the stanza identifier
     */
    public String id() { return id; }

    /**
     * Returns the message timestamp.
     *
     * @return the message timestamp
     */
    public Instant timestamp() { return timestamp; }

    /**
     * Returns the chat JID derived from the {@code from} attribute.
     *
     * @return the chat JID
     */
    public Jid chatJid() { return chatJid; }

    /**
     * Returns the actual sender's device JID.
     *
     * @return the sender's device JID
     */
    public Jid senderJid() { return senderJid; }

    /**
     * Returns the optional {@code participant} attribute.
     *
     * @return an {@link Optional} wrapping the participant JID
     */
    public Optional<Jid> participant() { return Optional.ofNullable(participant); }

    /**
     * Returns the classified message type.
     *
     * @return the message type
     */
    public MessageType messageType() { return messageType; }

    /**
     * Returns the {@code edit} attribute value.
     *
     * @return the edit attribute integer
     */
    public int editAttribute() { return editAttribute; }

    /**
     * Returns the optional push name from the {@code notify} attribute.
     *
     * @return an {@link Optional} wrapping the push name
     */
    public Optional<String> pushName() { return Optional.ofNullable(pushName); }

    /**
     * Returns the optional message category.
     *
     * @return an {@link Optional} wrapping the category string
     */
    public Optional<String> category() { return Optional.ofNullable(category); }

    /**
     * Returns the optional {@code offline} attribute.
     *
     * @return an {@link Optional} wrapping the offline value
     */
    public Optional<String> offline() { return Optional.ofNullable(offline); }

    /**
     * Returns the optional addressing mode ({@code "pn"} or {@code "lid"}).
     *
     * @return an {@link Optional} wrapping the addressing mode
     */
    public Optional<String> addressingMode() { return Optional.ofNullable(addressingMode); }

    /**
     * Returns whether the stanza has an {@code <hsm>} child.
     *
     * @return {@code true} if the HSM child is present
     */
    public boolean isHsm() { return isHsm; }

    /**
     * Returns the optional {@code count} attribute.
     *
     * @return an {@link Optional} wrapping the count value
     */
    public Optional<Integer> count() { return Optional.ofNullable(count); }

    /**
     * Returns the optional sender phone-number JID.
     *
     * @return an {@link Optional} wrapping the sender PN JID
     */
    public Optional<Jid> senderPn() { return Optional.ofNullable(senderPn); }

    /**
     * Returns the optional sender LID.
     *
     * @return an {@link Optional} wrapping the sender LID
     */
    public Optional<Jid> senderLid() { return Optional.ofNullable(senderLid); }

    /**
     * Returns the optional recipient phone-number JID.
     *
     * @return an {@link Optional} wrapping the recipient PN JID
     */
    public Optional<Jid> recipientPn() { return Optional.ofNullable(recipientPn); }

    /**
     * Returns the optional recipient LID.
     *
     * @return an {@link Optional} wrapping the recipient LID
     */
    public Optional<Jid> recipientLid() { return Optional.ofNullable(recipientLid); }

    /**
     * Returns the optional peer recipient phone-number JID.
     *
     * @return an {@link Optional} wrapping the peer recipient PN JID
     */
    public Optional<Jid> peerRecipientPn() { return Optional.ofNullable(peerRecipientPn); }

    /**
     * Returns the optional peer recipient LID.
     *
     * @return an {@link Optional} wrapping the peer recipient LID
     */
    public Optional<Jid> peerRecipientLid() { return Optional.ofNullable(peerRecipientLid); }

    /**
     * Returns the optional peer recipient username.
     *
     * @return an {@link Optional} wrapping the peer recipient username
     */
    public Optional<String> peerRecipientUsername() { return Optional.ofNullable(peerRecipientUsername); }

    /**
     * Returns the optional latest LID known for the recipient.
     *
     * @return an {@link Optional} wrapping the latest recipient LID
     */
    public Optional<Jid> recipientLatestLid() { return Optional.ofNullable(recipientLatestLid); }

    /**
     * Returns the optional recipient username.
     *
     * @return an {@link Optional} wrapping the recipient username
     */
    public Optional<String> recipientUsername() { return Optional.ofNullable(recipientUsername); }

    /**
     * Returns the optional group participant phone-number JID.
     *
     * @return an {@link Optional} wrapping the participant PN JID
     */
    public Optional<Jid> participantPn() { return Optional.ofNullable(participantPn); }

    /**
     * Returns the optional group participant LID.
     *
     * @return an {@link Optional} wrapping the participant LID
     */
    public Optional<Jid> participantLid() { return Optional.ofNullable(participantLid); }

    /**
     * Returns the optional group participant username.
     *
     * @return an {@link Optional} wrapping the participant username
     */
    public Optional<String> participantUsername() { return Optional.ofNullable(participantUsername); }

    /**
     * Returns the optional sender username.
     *
     * @return an {@link Optional} wrapping the sender username
     */
    public Optional<String> username() { return Optional.ofNullable(username); }

    /**
     * Returns the optional sender display name.
     *
     * @return an {@link Optional} wrapping the sender display name
     */
    public Optional<String> displayName() { return Optional.ofNullable(displayName); }

    /**
     * Returns the stanza's {@code type} attribute.
     *
     * @return the stanza type
     */
    public String stanzaType() { return stanzaType; }

    /**
     * Returns whether the message is an unavailable fanout placeholder.
     *
     * @return {@code true} if the stanza has an {@code <unavailable>} child
     */
    public boolean isUnavailable() { return unavailable; }

    /**
     * Returns whether the unavailable placeholder is for a hosted device.
     *
     * @return {@code true} if hosted
     */
    public boolean isHostedUnavailable() { return hostedUnavailable; }

    /**
     * Returns whether the unavailable placeholder is for a view-once message.
     *
     * @return {@code true} if view-once
     */
    public boolean isViewOnceUnavailable() { return viewOnceUnavailable; }

    /**
     * Returns the optional {@code polltype} attribute from the {@code <meta>} node.
     *
     * @return an {@link Optional} wrapping the poll type
     */
    public Optional<String> pollType() { return Optional.ofNullable(pollType); }

    /**
     * Returns the optional {@code event_type} attribute from the {@code <meta>} node.
     *
     * @return an {@link Optional} wrapping the event type
     */
    public Optional<String> eventType() { return Optional.ofNullable(eventType); }

    /**
     * Returns the optional {@code origin} attribute from the {@code <meta>} node.
     *
     * @return an {@link Optional} wrapping the origin
     */
    public Optional<String> origin() { return Optional.ofNullable(origin); }

    /**
     * Returns whether the stanza has a {@code <url_number>} child.
     *
     * @return {@code true} if present
     */
    public boolean urlNumber() { return urlNumber; }

    /**
     * Returns whether the stanza has a {@code <url_text>} child.
     *
     * @return {@code true} if present
     */
    public boolean urlText() { return urlText; }

    /**
     * Returns whether the status message mentions the current user.
     *
     * @return {@code true} if mentioned
     */
    public boolean statusMentioned() { return statusMentioned; }

    /**
     * Returns the optional {@code appdata} attribute from the {@code <meta>} node.
     *
     * @return an {@link Optional} wrapping the appdata value
     */
    public Optional<String> appdata() { return Optional.ofNullable(appdata); }

    /**
     * Returns the optional {@code biz_source} attribute from the {@code <meta>} node.
     *
     * @return an {@link Optional} wrapping the biz source
     */
    public Optional<String> bizSource() { return Optional.ofNullable(bizSource); }

    /**
     * Returns the optional {@code thread_msg_id} attribute.
     *
     * @return an {@link Optional} wrapping the thread parent id
     */
    public Optional<String> threadMsgId() { return Optional.ofNullable(threadMsgId); }

    /**
     * Returns the optional {@code thread_msg_sender_jid} attribute.
     *
     * @return an {@link Optional} wrapping the thread parent sender JID
     */
    public Optional<Jid> threadMsgSenderJid() { return Optional.ofNullable(threadMsgSenderJid); }

    /**
     * Returns the optional {@code target_id} attribute.
     *
     * @return an {@link Optional} wrapping the target id
     */
    public Optional<String> targetId() { return Optional.ofNullable(targetId); }

    /**
     * Returns the optional {@code target_sender_jid} attribute.
     *
     * @return an {@link Optional} wrapping the target sender JID
     */
    public Optional<Jid> targetSenderJid() { return Optional.ofNullable(targetSenderJid); }

    /**
     * Returns the optional {@code target_chat_jid} attribute.
     *
     * @return an {@link Optional} wrapping the target chat JID
     */
    public Optional<Jid> targetChatJid() { return Optional.ofNullable(targetChatJid); }

    /**
     * Returns the optional {@code target_chat_jid_lid} attribute.
     *
     * @return an {@link Optional} wrapping the target chat LID
     */
    public Optional<Jid> targetChatJidLid() { return Optional.ofNullable(targetChatJidLid); }

    /**
     * Returns whether the {@code <meta>} node has {@code capi="true"}.
     *
     * @return {@code true} if capi is set to true
     */
    public boolean isCapi() { return capi; }

    /**
     * Returns the optional {@code context_source} attribute.
     *
     * @return an {@link Optional} wrapping the context source
     */
    public Optional<String> contextSource() { return Optional.ofNullable(contextSource); }

    /**
     * Returns the optional sender country code.
     *
     * @return an {@link Optional} wrapping the sender country code
     */
    public Optional<String> senderCountryCode() { return Optional.ofNullable(senderCountryCode); }

    /**
     * Returns the list of encrypted payloads parsed from {@code <enc>} children.
     *
     * @return the encrypted payloads list
     */
    public List<MessageReceiveEncryptedPayload> encs() { return encs; }

    /**
     * Returns the optional device identity bytes used for ADV validation.
     *
     * @return an {@link Optional} wrapping the device identity bytes
     */
    public Optional<byte[]> deviceIdentity() { return Optional.ofNullable(deviceIdentity); }

    /**
     * Returns the optional parsed bot info from the {@code <bot>} child.
     *
     * @return an {@link Optional} wrapping the bot info
     */
    public Optional<MessageReceiveBotInfo> botInfo() { return Optional.ofNullable(botInfo); }

    /**
     * Returns the optional parsed business info.
     *
     * @return an {@link Optional} wrapping the biz info
     */
    public Optional<MessageReceiveBizInfo> bizInfo() { return Optional.ofNullable(bizInfo); }

    /**
     * Returns the optional parsed reporting token info.
     *
     * @return an {@link Optional} wrapping the reporting info
     */
    public Optional<MessageReceiveReportingInfo> reportingInfo() { return Optional.ofNullable(reportingInfo); }

    /**
     * Returns the broadcast contact list participants.
     *
     * @return the list of BCL participants
     */
    public List<MessageReceiveBroadcastParticipant> bclParticipants() { return bclParticipants; }

    /**
     * Returns the optional parsed payment info.
     *
     * @return an {@link Optional} wrapping the payment info
     */
    public Optional<MessageReceivePaymentInfo> paymentInfo() { return Optional.ofNullable(paymentInfo); }

    /**
     * Returns the optional stanza-level {@code eph_setting} attribute.
     *
     * @return an {@link Optional} wrapping the ephemeral setting
     */
    public Optional<String> ephSetting() { return Optional.ofNullable(ephSetting); }

    /**
     * Returns the optional {@code <rcat>} content bytes.
     *
     * @return an {@link Optional} wrapping the rcat bytes
     */
    public Optional<byte[]> rcat() { return Optional.ofNullable(rcat); }

    /**
     * Returns the optional HSM tag from the {@code <hsm>} child.
     *
     * @return an {@link Optional} wrapping the HSM tag
     */
    public Optional<String> hsmTag() { return Optional.ofNullable(hsmTag); }

    /**
     * Returns the optional HSM category from the {@code <hsm>} child.
     *
     * @return an {@link Optional} wrapping the HSM category
     */
    public Optional<String> hsmCategory() { return Optional.ofNullable(hsmCategory); }

    /**
     * Returns the retry count from the first encrypted payload, when non-zero, used
     * to determine the retry receipt count.
     *
     * @return an {@link OptionalInt} wrapping the retry count, empty when no retries
     *         were recorded
     */
    public OptionalInt retryCount() {
        if (encs.isEmpty()) {
            return OptionalInt.empty();
        }
        var count = encs.getFirst().retryCount();
        return count > 0 ? OptionalInt.of(count) : OptionalInt.empty();
    }

    /**
     * Returns whether any encrypted payload has {@code decrypt-fail="hide"}, used by
     * the dedup layer to decide whether to suppress placeholders on decryption
     * failure.
     *
     * @return {@code true} if any payload hides failures
     */
    public boolean hasHideFailPayload() {
        return encs.stream().anyMatch(MessageReceiveEncryptedPayload::hideFail);
    }

    /**
     * Returns whether the message was received while the client was offline.
     *
     * @return {@code true} if the {@code offline} attribute was set
     */
    public boolean isOffline() {
        return offline != null;
    }

    /**
     * Returns whether this is a peer message (category is {@code "peer"}).
     *
     * @return {@code true} if the category is {@code "peer"}
     */
    public boolean isPeer() {
        return "peer".equals(category);
    }

    /**
     * Returns whether every encrypted payload uses direct (non-SKMSG) encryption.
     *
     * @return {@code true} if no SKMSG payload is present
     */
    public boolean isDirect() {
        return encs.stream().noneMatch(enc ->
                enc.e2eType().isSenderKeyMessage());
    }

    /**
     * Returns whether the sender is a companion device (device id is not zero).
     *
     * @return {@code true} if the sender is a companion device
     */
    public boolean isCompanionDevice() {
        return senderJid.device() != 0;
    }

    /**
     * Returns whether any encrypted payload carries a non-zero retry count.
     *
     * @return {@code true} if at least one payload is a retry
     */
    public boolean isRetry() {
        return encs.stream().anyMatch(enc -> enc.retryCount() > 0);
    }
}
