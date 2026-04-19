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
 * Holds the fully parsed structural representation of an incoming
 * {@code <message>} stanza before the payload is decrypted.
 *
 * <p>Captures every metadata field extracted from the raw XML node:
 * the message identifier and timestamp; the addressing information
 * (including the LID/PN migration fields); the encrypted payloads; the
 * bot, business, and payment metadata; the reporting tokens; broadcast
 * participant lists; and every {@code <meta>} attribute. Decryption
 * and downstream processing operate on this structured form rather
 * than re-scanning the raw node.
 *
 * <p>WA Web produces equivalent data in separate {@code msgInfo},
 * {@code msgMeta}, {@code encs}, {@code deviceIdentity}, {@code bizInfo},
 * {@code hsmInfo}, {@code paymentInfo}, {@code rcat}, {@code msgBotInfo},
 * and {@code reportingTokenInfo} objects; Cobalt merges them into a
 * single cohesive container so callers receive one value per stanza.
 *
 * @apiNote WAWebHandleMsgParser.incomingMsgParser: the primary parser
 * for incoming message stanzas in WA Web.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
@WhatsAppWebModule(moduleName = "WAWebHandleMsgCommon")
public final class MessageReceiveStanza {
    /**
     * The {@code edit} attribute value representing the absence of
     * an edit.
     *
     * @implNote WAWebHandleMsgCommon.EDIT_ATTR.NONE.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "EDIT_ATTR",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int EDIT_NONE = 0;

    /**
     * The {@code edit} attribute value indicating a message edit.
     *
     * @implNote WAWebHandleMsgCommon.EDIT_ATTR.MESSAGE.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "EDIT_ATTR",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int EDIT_MESSAGE = 1;

    /**
     * The {@code edit} attribute value indicating a pin-in-chat
     * operation.
     *
     * @implNote WAWebHandleMsgCommon.EDIT_ATTR.PIN.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "EDIT_ATTR",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int EDIT_PIN = 2;

    /**
     * The {@code edit} attribute value indicating the sender revoked
     * the message.
     *
     * @implNote WAWebHandleMsgCommon.EDIT_ATTR.SENDER_REVOKE.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "EDIT_ATTR",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int EDIT_SENDER_REVOKE = 7;

    /**
     * The {@code edit} attribute value indicating an admin revoked the
     * message.
     *
     * @implNote WAWebHandleMsgCommon.EDIT_ATTR.ADMIN_REVOKE.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "EDIT_ATTR",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int EDIT_ADMIN_REVOKE = 8;

    /**
     * The {@code context_source} value identifying the stanza as
     * originating from a channel invitation.
     *
     * @implNote WAWebHandleMsgCommon.CONTEXT_SOURCE.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "CONTEXT_SOURCE",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String CONTEXT_SOURCE_CHANNELS_INVITATION = "channels_invitation";

    /**
     * The stanza's {@code id} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrString("id")}.
     */
    private final String id;

    /**
     * The message timestamp parsed from the {@code t} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrTime("t")}.
     */
    private final Instant timestamp;

    /**
     * The chat JID derived from the {@code from} attribute.
     *
     * <p>For 1:1 messages this equals the sender; for groups,
     * broadcasts, and status feeds this identifies the
     * group/broadcast/status chat.
     *
     * @implNote WAWebHandleMsgParser:
     * {@code jidWithTypeToWid(e.attrJidWithType("from"))}.
     */
    private final Jid chatJid;

    /**
     * The actual sender's device JID.
     *
     * <p>For 1:1 messages equals {@link #chatJid}; for
     * groups/broadcasts this is the {@code participant} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code from.isGroup()||from.isBroadcast() ? participant : from}.
     */
    private final Jid senderJid;

    /**
     * The {@code participant} attribute present on group, broadcast,
     * and status messages. Identifies the sender's device within the
     * group.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrDeviceJid("participant")}.
     */
    private final Jid participant;

    /**
     * The classified message type derived from the addressing.
     *
     * @implNote WAWebHandleMsgParser function C(): determines CHAT,
     * GROUP, PEER_BROADCAST, OTHER_BROADCAST, DIRECT_PEER_STATUS, or
     * OTHER_STATUS based on the {@code from} JID type and participant
     * presence.
     */
    private final MessageType messageType;

    /**
     * The {@code edit} attribute, defaulting to {@link #EDIT_NONE}.
     *
     * @implNote WAWebHandleMsgParser: {@code e.maybeAttrInt("edit") ?? EDIT_ATTR.NONE}.
     */
    private final int editAttribute;

    /**
     * The sender's push name from the {@code notify} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.maybeAttrString("notify")}.
     */
    private final String pushName;

    /**
     * The message category. The only defined value is {@code "peer"}
     * for peer protocol messages.
     *
     * @implNote WAWebHandleMsgCommon.MSG_CATEGORY.
     */
    private final String category;

    /**
     * The {@code offline} attribute value, present on messages
     * delivered while the client was offline.
     *
     * @implNote WAWebHandleMsgParser: {@code e.maybeAttrString("offline")}.
     */
    private final String offline;

    /**
     * The addressing mode for group messages: either {@code "pn"}
     * (phone number) or {@code "lid"}.
     *
     * @implNote WAWebHandleMsgCommon.STANZA_MSG_ADDRESSING_MODE.
     */
    private final String addressingMode;

    /**
     * Whether the stanza has an {@code <hsm>} child indicating a highly
     * structured message (business template).
     *
     * @implNote WAWebHandleMsgParser: {@code e.hasChild("hsm")}.
     */
    private final boolean isHsm;

    /**
     * The optional {@code count} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.maybeAttrInt("count")}.
     */
    private final Integer count;

    /**
     * The sender's phone number JID from the {@code sender_pn}
     * attribute. Used on LID-addressed groups to carry the PN mapping.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrUserJid("sender_pn")}.
     */
    private final Jid senderPn;

    /**
     * The sender's LID from the {@code sender_lid} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrUserJid("sender_lid")}.
     */
    private final Jid senderLid;

    /**
     * The recipient's phone number from the {@code recipient_pn}
     * attribute, present on peer messages.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrUserJid("recipient_pn")}.
     */
    private final Jid recipientPn;

    /**
     * The recipient's LID from the {@code recipient_lid} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrLidUserJid("recipient_lid")}.
     */
    private final Jid recipientLid;

    /**
     * The peer recipient's phone number from the
     * {@code peer_recipient_pn} attribute, used on peer broadcast
     * messages.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrUserJid("peer_recipient_pn")}.
     */
    private final Jid peerRecipientPn;

    /**
     * The peer recipient's LID from the {@code peer_recipient_lid}
     * attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrLidUserJid("peer_recipient_lid")}.
     */
    private final Jid peerRecipientLid;

    /**
     * The peer recipient's username from the
     * {@code peer_recipient_username} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.maybeAttrString("peer_recipient_username")}.
     */
    private final String peerRecipientUsername;

    /**
     * The most recent LID known for the recipient from the
     * {@code recipient_latest_lid} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrLidUserJid("recipient_latest_lid")}.
     */
    private final Jid recipientLatestLid;

    /**
     * The recipient's username from the {@code recipient_username}
     * attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.maybeAttrString("recipient_username")}.
     */
    private final String recipientUsername;

    /**
     * The group participant's phone number from the
     * {@code participant_pn} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrUserJid("participant_pn")}.
     */
    private final Jid participantPn;

    /**
     * The group participant's LID from the {@code participant_lid}
     * attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.attrLidUserJid("participant_lid")}.
     */
    private final Jid participantLid;

    /**
     * The group participant's username from the
     * {@code participant_username} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.maybeAttrString("participant_username")}.
     */
    private final String participantUsername;

    /**
     * The sender's username from the {@code username} attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.maybeAttrString("username")}.
     */
    private final String username;

    /**
     * The sender's display name from the {@code display_name}
     * attribute.
     *
     * @implNote WAWebHandleMsgParser: {@code e.maybeAttrString("display_name")}.
     */
    private final String displayName;

    /**
     * The stanza's {@code type} attribute. Defined values are
     * {@code "text"}, {@code "media"}, {@code "medianotify"},
     * {@code "pay"}, {@code "poll"}, {@code "reaction"}, and
     * {@code "event"}.
     *
     * @implNote WAWebHandleMsgCommon.STANZA_MSG_TYPES.
     */
    private final String stanzaType;

    /**
     * Whether the stanza has an {@code <unavailable>} child indicating
     * the payload is absent (fanout placeholder).
     *
     * @implNote WAWebHandleMsgParser function b(): {@code e.hasChild("unavailable")}.
     */
    private final boolean unavailable;

    /**
     * Whether the {@code <unavailable>} child carries {@code hosted="true"},
     * meaning a hosted-device fanout placeholder.
     *
     * @implNote WAWebHandleMsgParser: {@code unavailable.maybeAttrString("hosted") === "true"}.
     */
    private final boolean hostedUnavailable;

    /**
     * Whether the {@code <unavailable>} child carries
     * {@code type="view_once"}, meaning a view-once fanout placeholder.
     *
     * @implNote WAWebHandleMsgParser: {@code unavailable.maybeAttrString("type") === "view_once"}.
     */
    private final boolean viewOnceUnavailable;

    /**
     * The {@code polltype} attribute from the {@code <meta>} node.
     * Defined values are {@code "creation"}, {@code "quiz_creation"},
     * {@code "vote"}, {@code "result_snapshot"}, and {@code "edit"}.
     *
     * @implNote WAWebHandleMsgCommon.POLL_TYPES.
     */
    private final String pollType;

    /**
     * The {@code event_type} attribute from the {@code <meta>} node.
     * Defined values are {@code "creation"}, {@code "response"}, and
     * {@code "edit"}. Populated only when the stanza type is
     * {@code "event"}.
     *
     * @implNote WAWebHandleMsgCommon.EVENT_TYPES.
     */
    private final String eventType;

    /**
     * The {@code origin} attribute from the {@code <meta>} node. The
     * only defined value is {@code "ctwa"} (click-to-WhatsApp ads).
     *
     * @implNote WAWebHandleMsgCommon.STANZA_MSG_ORIGIN.
     */
    private final String origin;

    /**
     * Whether the stanza has a {@code <url_number>} child.
     *
     * @implNote WAWebHandleMsgParser: {@code e.hasChild("url_number")}.
     */
    private final boolean urlNumber;

    /**
     * Whether the stanza has a {@code <url_text>} child.
     *
     * @implNote WAWebHandleMsgParser: {@code e.hasChild("url_text")}.
     */
    private final boolean urlText;

    /**
     * Whether the {@code <meta>} node has {@code status_mentioned="true"}.
     *
     * @implNote WAWebHandleMsgParser: {@code meta.maybeAttrString("status_mentioned") === "true"}.
     */
    private final boolean statusMentioned;

    /**
     * The {@code appdata} attribute from the {@code <meta>} node.
     * Defined values are {@code "default"}, {@code "member_tag"},
     * and {@code "group_history"}.
     *
     * @implNote WAWebHandleMsgCommon.APPDATA.
     */
    private final String appdata;

    /**
     * The {@code biz_source} attribute from the {@code <meta>} node.
     *
     * @implNote WAWebHandleMsgCommon.BIZ_SOURCE_ATTR.
     */
    private final String bizSource;

    /**
     * The {@code thread_msg_id} attribute from the {@code <meta>} node,
     * identifying the parent message of a comment thread.
     *
     * @implNote WAWebHandleMsgParser: {@code meta.attrString("thread_msg_id")}.
     */
    private final String threadMsgId;

    /**
     * The {@code thread_msg_sender_jid} attribute from the
     * {@code <meta>} node.
     *
     * @implNote WAWebHandleMsgParser: {@code meta.attrJidWithType("thread_msg_sender_jid")}.
     */
    private final Jid threadMsgSenderJid;

    /**
     * The {@code target_id} attribute from the {@code <meta>} node,
     * referencing the parent message for addon messages (reactions,
     * poll votes, etc.).
     *
     * @implNote WAWebHandleMsgParser: {@code meta.attrString("target_id")}.
     */
    private final String targetId;

    /**
     * The {@code target_sender_jid} attribute from the {@code <meta>}
     * node.
     *
     * @implNote WAWebHandleMsgParser: {@code meta.attrJidWithType("target_sender_jid")}.
     */
    private final Jid targetSenderJid;

    /**
     * The {@code target_chat_jid} attribute from the {@code <meta>}
     * node.
     *
     * @implNote WAWebHandleMsgParser: {@code meta.attrJidWithType("target_chat_jid")}.
     */
    private final Jid targetChatJid;

    /**
     * The {@code target_chat_jid_lid} attribute from the {@code <meta>}
     * node.
     *
     * @implNote WAWebHandleMsgParser: {@code meta.attrJidWithType("target_chat_jid_lid")}.
     */
    private final Jid targetChatJidLid;

    /**
     * Whether the {@code <meta>} node has {@code capi="true"}.
     *
     * @implNote WAWebHandleMsgParser: {@code meta.attrString("capi") === "true"}.
     */
    private final boolean capi;

    /**
     * The {@code context_source} attribute from the {@code <meta>}
     * node. The known value is {@code "channels_invitation"}.
     *
     * @implNote WAWebHandleMsgCommon.CONTEXT_SOURCE.
     */
    private final String contextSource;

    /**
     * The sender's country code parsed from the {@code <meta>} node's
     * {@code sender_country_code} attribute.
     *
     * @implNote WAWebHandleMsgParser function T(): parses and validates
     * the ISO country code.
     */
    private final String senderCountryCode;

    /**
     * The list of encrypted payloads parsed from {@code <enc>} child
     * nodes.
     *
     * @implNote WAWebHandleMsgParser: maps each {@code <enc>} child to
     * an object with e2eType, encMediaType, ciphertext, retryCount,
     * and hideFail.
     */
    private final List<MessageReceiveEncryptedPayload> encs;

    /**
     * The device identity bytes from the {@code <device-identity>}
     * child, used for ADV validation of companion devices.
     *
     * @implNote WAWebHandleMsgParser: {@code deviceIdentityNode.contentBytes()}.
     */
    private final byte[] deviceIdentity;

    /**
     * The parsed bot info from the {@code <bot>} child.
     *
     * @implNote WAWebHandleMsgParser function b().
     */
    private final MessageReceiveBotInfo botInfo;

    /**
     * The parsed business info from stanza attributes and the
     * {@code <biz>} child.
     *
     * @implNote WAWebHandleMsgParser function v().
     */
    private final MessageReceiveBizInfo bizInfo;

    /**
     * The parsed reporting token info from the {@code <reporting>}
     * child.
     *
     * @implNote WAWebHandleMsgParser function k().
     */
    private final MessageReceiveReportingInfo reportingInfo;

    /**
     * The list of broadcast contact list participants from the
     * {@code <participants>} child. Populated for PEER_BROADCAST and
     * DIRECT_PEER_STATUS messages.
     *
     * @implNote WAWebHandleMsgParser function y(): maps each
     * {@code <to>} child.
     */
    private final List<MessageReceiveBroadcastParticipant> bclParticipants;

    /**
     * The parsed payment info from the {@code <pay>} and
     * {@code <transaction>} children.
     *
     * @implNote WAWebHandleMsgParser function R().
     */
    private final MessageReceivePaymentInfo paymentInfo;

    /**
     * The content bytes of the {@code <rcat>} child node, used for
     * content binding verification.
     *
     * @implNote WAWebHandleMsgParser: {@code rcat.contentBytes()}.
     */
    private final byte[] rcat;

    /**
     * The stanza-level {@code eph_setting} attribute, present on
     * OTHER_BROADCAST messages for ephemeral message settings.
     *
     * @implNote WAWebHandleMsgParser: {@code e.maybeAttrString("eph_setting")}.
     */
    private final String ephSetting;

    /**
     * The {@code tag} attribute from the {@code <hsm>} child node.
     *
     * @implNote WAWebHandleMsgParser function R(): {@code hsm.maybeAttrString("tag")}.
     */
    private final String hsmTag;

    /**
     * The {@code category} attribute from the {@code <hsm>} child node.
     *
     * @implNote WAWebHandleMsgParser function R(): {@code hsm.maybeAttrString("category")}.
     */
    private final String hsmCategory;

    /**
     * Constructs a new parsed stanza record.
     *
     * <p>This constructor is public and accepts every parsed field.
     * It is intended to be called by {@link MessageReceiveStanzaParser}
     * after completing its extraction pass; callers outside the parser
     * rarely need to invoke it directly.
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
     *
     * @throws NullPointerException if any non-nullable argument is {@code null}
     *
     * @implNote WAWebHandleMsgParser.incomingMsgParser: constructs the
     * composite parsed result object.
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
     * Returns the message timestamp parsed from the {@code t} attribute.
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
     * Returns whether the unavailable placeholder is for a view-once
     * message.
     *
     * @return {@code true} if view-once
     */
    public boolean isViewOnceUnavailable() { return viewOnceUnavailable; }

    /**
     * Returns the optional {@code polltype} attribute from the
     * {@code <meta>} node.
     *
     * @return an {@link Optional} wrapping the poll type
     */
    public Optional<String> pollType() { return Optional.ofNullable(pollType); }

    /**
     * Returns the optional {@code event_type} attribute from the
     * {@code <meta>} node.
     *
     * @return an {@link Optional} wrapping the event type
     */
    public Optional<String> eventType() { return Optional.ofNullable(eventType); }

    /**
     * Returns the optional {@code origin} attribute from the
     * {@code <meta>} node.
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
     * Returns the optional {@code appdata} attribute from the
     * {@code <meta>} node.
     *
     * @return an {@link Optional} wrapping the appdata value
     */
    public Optional<String> appdata() { return Optional.ofNullable(appdata); }

    /**
     * Returns the optional {@code biz_source} attribute from the
     * {@code <meta>} node.
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
     * Returns the list of encrypted payloads parsed from {@code <enc>}
     * children.
     *
     * @return the encrypted payloads list
     */
    public List<MessageReceiveEncryptedPayload> encs() { return encs; }

    /**
     * Returns the optional device identity bytes used for ADV
     * validation.
     *
     * @return an {@link Optional} wrapping the device identity bytes
     */
    public Optional<byte[]> deviceIdentity() { return Optional.ofNullable(deviceIdentity); }

    /**
     * Returns the optional parsed bot info from the {@code <bot>}
     * child.
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
     * Returns the retry count from the first encrypted payload, when
     * non-zero, used to determine the retry receipt count.
     *
     * @return an {@link OptionalInt} wrapping the retry count, empty
     *         when no retries were recorded
     *
     * @implNote WAWebHandleMsg: uses {@code encs[0].retryCount} to
     * determine retry receipt count.
     */
    public OptionalInt retryCount() {
        // WAWebHandleMsg
        // Extracts the first enc's retry count and returns empty when no retries are recorded
        if (encs.isEmpty()) {
            return OptionalInt.empty();
        }
        var count = encs.getFirst().retryCount();
        return count > 0 ? OptionalInt.of(count) : OptionalInt.empty();
    }

    /**
     * Returns whether any encrypted payload has
     * {@code decrypt-fail="hide"}, used by the dedup layer to decide
     * whether to suppress placeholders on decryption failure.
     *
     * @return {@code true} if any payload hides failures
     *
     * @implNote WAWebHandleMsg function v(): checks
     * {@code encs.some(e => e.hideFail)} to determine dedup eligibility.
     */
    public boolean hasHideFailPayload() {
        // WAWebHandleMsg function v()
        // Returns true when any encrypted payload is flagged to hide decryption failures
        return encs.stream().anyMatch(MessageReceiveEncryptedPayload::hideFail);
    }

    /**
     * Returns whether the message was received while the client was
     * offline.
     *
     * @return {@code true} if the {@code offline} attribute was set
     *
     * @implNote WAWebHandleMsg: {@code msgInfo.offline != null}.
     */
    public boolean isOffline() {
        // WAWebHandleMsg
        // Returns true when the offline attribute was present on the stanza
        return offline != null;
    }

    /**
     * Returns whether this is a peer message (category is
     * {@code "peer"}).
     *
     * @return {@code true} if the category is {@code "peer"}
     *
     * @implNote WAWebHandleMsgCommon.MSG_CATEGORY.peer.
     */
    public boolean isPeer() {
        // WAWebHandleMsgCommon.MSG_CATEGORY
        // Compares the category attribute to the peer constant
        return "peer".equals(category);
    }

    /**
     * Returns whether every encrypted payload uses direct (non-SKMSG)
     * encryption.
     *
     * @return {@code true} if no SKMSG payload is present
     *
     * @implNote WAWebHandleMsgParser: {@code isDirect = encs.every(e => e.e2eType !== Skmsg)}.
     */
    public boolean isDirect() {
        // WAWebHandleMsgParser
        // Returns true when no enc payload is a sender-key group message
        return encs.stream().noneMatch(enc ->
                enc.e2eType().isSenderKeyMessage());
    }

    /**
     * Returns whether the sender is a companion device (device id
     * not zero).
     *
     * @return {@code true} if the sender is a companion device
     *
     * @implNote WAWebMsgProcessingDecryptApi: validates ADV only when
     * {@code author.device != null && author.device !== 0}.
     */
    public boolean isCompanionDevice() {
        // WAWebMsgProcessingDecryptApi
        // Returns true when the sender's device id indicates a companion rather than the primary device
        return senderJid.device() != 0;
    }

    /**
     * Returns whether any encrypted payload carries a non-zero retry
     * count.
     *
     * @return {@code true} if at least one payload is a retry
     *
     * @implNote WAWebHandleMsgParser: {@code encs.some(e => e.retryCount > 0)}.
     */
    public boolean isRetry() {
        // WAWebHandleMsgParser
        // Returns true when at least one enc payload has a positive retry count
        return encs.stream().anyMatch(enc -> enc.retryCount() > 0);
    }
}
