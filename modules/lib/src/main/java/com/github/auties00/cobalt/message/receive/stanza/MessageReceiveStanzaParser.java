package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Parses incoming {@code <message>} stanzas into structured
 * {@link MessageReceiveStanza} records.
 *
 * <p>This stateless utility class extracts every metadata field from
 * the raw XML node before any decryption is attempted: addressing
 * information (including the LID/PN migration attributes), the
 * encryption payloads, bot and business metadata, reporting tokens,
 * broadcast participant lists, payment information, and every
 * {@code <meta>} attribute. The resulting record is then handed off
 * to the receive pipeline for decryption and processing.
 *
 * <p>The parsing logic mirrors WA Web's
 * {@code WAWebHandleMsgParser.incomingMsgParser}, which extracts the
 * same data into separate objects (msgInfo, msgMeta, encs,
 * deviceIdentity, bizInfo, hsmInfo, paymentInfo, rcat, msgBotInfo,
 * reportingTokenInfo). Cobalt folds them all into a single cohesive
 * parse result.
 *
 * @apiNote WAWebHandleMsgParser.incomingMsgParser: the main parser
 * for incoming message stanzas in WA Web.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveStanzaParser {

    /**
     * Prevents instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     * @implNote Java-specific utility class pattern; no WA Web counterpart.
     */
    private MessageReceiveStanzaParser() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Parses a raw {@code <message>} node into a structured
     * {@link MessageReceiveStanza}.
     *
     * <p>The {@code selfJid} argument is required for the
     * {@code isMeAccount} checks that distinguish peer-broadcast and
     * peer-status from other-broadcast and other-status. When
     * {@code null}, the parser falls back to conservative defaults
     * (treating ambiguous cases as non-self).
     *
     * @param node    the incoming {@code <message>} node
     * @param selfJid the current user's JID (nullable), used for
     *                message type classification
     * @return the parsed stanza with all extracted metadata
     *
     * @throws NullPointerException     if {@code node} is {@code null}
     * @throws IllegalArgumentException if required attributes are missing
     *
     * @implNote WAWebHandleMsgParser.incomingMsgParser: extracts
     * msgInfo, msgMeta, encs, deviceIdentity, bizInfo, hsmInfo,
     * paymentInfo, rcat, msgBotInfo, and reportingTokenInfo into
     * separate objects; Cobalt merges them into a single result.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static MessageReceiveStanza parse(Node node, Jid selfJid) {
        Objects.requireNonNull(node, "node cannot be null");

        // WAWebHandleMsgParser.incomingMsgParser
        // Extracts the required core attributes identifying the stanza

        var id = node.getRequiredAttributeAsString("id");
        var timestampSeconds = node.getRequiredAttributeAsLong("t");
        var timestamp = Instant.ofEpochSecond(timestampSeconds);
        var fromJid = node.getRequiredAttributeAsJid("from");

        var stanzaType = node.getRequiredAttributeAsString("type");
        var editAttribute = node.getAttributeAsInt("edit", 0);
        var pushName = node.getAttributeAsString("notify", null);
        var category = node.getAttributeAsString("category", null);
        var offline = node.getAttributeAsString("offline", null);
        var addressingMode = node.getAttributeAsString("addressing_mode", null);

        // WAWebHandleMsgParser.incomingMsgParser
        // Reads the optional participant attribute for group/broadcast/status messages

        var participant = node.getAttributeAsJid("participant", null);

        // WAWebHandleMsgParser.incomingMsgParser
        // Reads the LID/PN migration attributes attached at stanza level

        var senderPn = node.getAttributeAsJid("sender_pn", null);
        var senderLid = node.getAttributeAsJid("sender_lid", null);
        var recipientPn = node.getAttributeAsJid("recipient_pn", null);
        var recipientLid = node.getAttributeAsJid("recipient_lid", null);
        var peerRecipientPn = node.getAttributeAsJid("peer_recipient_pn", null);
        var peerRecipientLid = node.getAttributeAsJid("peer_recipient_lid", null);
        var peerRecipientUsername = node.getAttributeAsString("peer_recipient_username", null);
        var recipientLatestLid = node.getAttributeAsJid("recipient_latest_lid", null);
        var recipientUsername = node.getAttributeAsString("recipient_username", null);
        var participantPn = node.getAttributeAsJid("participant_pn", null);
        var participantLid = node.getAttributeAsJid("participant_lid", null);
        var participantUsername = node.getAttributeAsString("participant_username", null);
        var username = node.getAttributeAsString("username", null);
        var displayName = node.getAttributeAsString("display_name", null);

        // WAWebHandleMsgParser.incomingMsgParser
        // Reads the optional count attribute

        var count = node.getAttributeAsInt("count", null);

        // WAWebHandleMsgParser.incomingMsgParser
        // Detects a highly structured message by checking for the hsm child

        var isHsm = node.getChild("hsm").isPresent();

        // WAWebHandleMsgParser function y()
        // Resolves the actual device sender from the from/participant pair

        var senderJid = resolveSender(fromJid, participant);

        // WAWebHandleMsgParser.incomingMsgParser
        // Parses every enc child into a structured encrypted payload

        var encs = parseEncryptedPayloads(node);

        // WAWebHandleMsgParser function C()
        // Classifies the message type based on addressing and self-identity

        var messageType = resolveMessageType(fromJid, participant, selfJid, encs, category);

        // WAWebHandleMsgParser.incomingMsgParser
        // Reads the device identity bytes used for ADV validation of companion senders

        var deviceIdentity = node.getChild("device-identity")
                .flatMap(Node::toContentBytes)
                .orElse(null);

        // WAWebHandleMsgParser function b()
        // Detects an unavailable fanout placeholder and classifies its kind

        var unavailableNode = node.getChild("unavailable", null);
        var unavailable = unavailableNode != null;
        var hostedUnavailable = unavailable
                && "true".equals(unavailableNode.getAttributeAsString("hosted").orElse(null));
        var viewOnceUnavailable = unavailable
                && "view_once".equals(unavailableNode.getAttributeAsString("type").orElse(null));

        // WAWebHandleMsgParser.incomingMsgParser
        // Extracts the structured metadata from the optional meta child

        var metaNode = node.getChild("meta", null);
        String pollType = null;
        String eventType = null;
        String origin = null;
        var statusMentioned = false;
        String appdata = null;
        String bizSource = null;
        String threadMsgId = null;
        Jid threadMsgSenderJid = null;
        String targetId = null;
        Jid targetSenderJid = null;
        Jid targetChatJid = null;
        Jid targetChatJidLid = null;
        var capi = false;
        String contextSource = null;
        String senderCountryCode = null;
        if (metaNode != null) {
            // WAWebHandleMsgParser function C()
            // Populates pollType only when the stanza type is poll

            if ("poll".equals(stanzaType)) {
                pollType = metaNode.getAttributeAsString("polltype", null);
            }

            // WAWebHandleMsgParser function C()
            // Populates eventType only when the stanza type is event

            if ("event".equals(stanzaType)) {
                eventType = metaNode.getAttributeAsString("event_type", null);
            }
            origin = metaNode.getAttributeAsString("origin", null);
            statusMentioned = "true".equals(
                    metaNode.getAttributeAsString("status_mentioned").orElse(null));
            appdata = metaNode.getAttributeAsString("appdata", null);
            bizSource = metaNode.getAttributeAsString("biz_source", null);
            threadMsgId = metaNode.getAttributeAsString("thread_msg_id", null);
            threadMsgSenderJid = metaNode.getAttributeAsJid("thread_msg_sender_jid", null);
            targetId = metaNode.getAttributeAsString("target_id", null);
            targetSenderJid = metaNode.getAttributeAsJid("target_sender_jid", null);
            targetChatJid = metaNode.getAttributeAsJid("target_chat_jid", null);
            targetChatJidLid = metaNode.getAttributeAsJid("target_chat_jid_lid", null);
            capi = "true".equals(
                    metaNode.getAttributeAsString("capi").orElse(null));
            contextSource = metaNode.getAttributeAsString("context_source", null);
            senderCountryCode = metaNode.getAttributeAsString("sender_country_code", null);
        }

        // WAWebHandleMsgParser.incomingMsgParser
        // Detects the presence of url_number and url_text children

        var urlNumber = node.getChild("url_number").isPresent();
        var urlText = node.getChild("url_text").isPresent();

        // WAWebHandleMsgParser function b()
        // Parses the bot child into bot info

        var botInfo = parseBotInfo(node);

        // WAWebHandleMsgParser function v()
        // Parses the biz child and verified name attributes into biz info

        var bizInfo = parseBizInfo(node);

        // WAWebHandleMsgParser function k()
        // Parses the reporting child into reporting info

        var reportingInfo = parseReportingInfo(node);

        // WAWebHandleMsgParser function y()
        // Parses the participants child into the broadcast contact list

        var bclParticipants = parseBroadcastParticipants(node);

        // WAWebHandleMsgParser function R()
        // Parses pay and transaction children into payment info

        var paymentInfo = parsePaymentInfo(node);

        // WAWebHandleMsgParser.incomingMsgParser
        // Reads the stanza-level ephemeral setting for OTHER_BROADCAST

        var ephSetting = node.getAttributeAsString("eph_setting", null);

        // WAWebHandleMsgParser.incomingMsgParser
        // Extracts the optional rcat content bytes for content-binding verification

        var rcat = node.getChild("rcat")
                .flatMap(Node::toContentBytes)
                .orElse(null);

        // WAWebHandleMsgParser function R()
        // Reads the optional tag and category from the hsm child

        var hsmNode = node.getChild("hsm", null);
        String hsmTag = null;
        String hsmCategory = null;
        if (hsmNode != null) {
            hsmTag = hsmNode.getAttributeAsString("tag", null);
            hsmCategory = hsmNode.getAttributeAsString("category", null);
        }

        // WAWebHandleMsgParser.incomingMsgParser
        // Assembles the parsed fields into the composite result record

        return new MessageReceiveStanza(
                id,
                timestamp,
                fromJid,
                senderJid,
                participant,
                messageType,
                editAttribute,
                pushName,
                category,
                offline,
                addressingMode,
                isHsm,
                count,
                senderPn,
                senderLid,
                recipientPn,
                recipientLid,
                peerRecipientPn,
                peerRecipientLid,
                peerRecipientUsername,
                recipientLatestLid,
                recipientUsername,
                participantPn,
                participantLid,
                participantUsername,
                username,
                displayName,
                stanzaType,
                unavailable,
                hostedUnavailable,
                viewOnceUnavailable,
                pollType,
                eventType,
                origin,
                urlNumber,
                urlText,
                statusMentioned,
                appdata,
                bizSource,
                threadMsgId,
                threadMsgSenderJid,
                targetId,
                targetSenderJid,
                targetChatJid,
                targetChatJidLid,
                capi,
                contextSource,
                senderCountryCode,
                encs,
                deviceIdentity,
                botInfo,
                bizInfo,
                reportingInfo,
                bclParticipants,
                paymentInfo,
                ephSetting,
                rcat,
                hsmTag,
                hsmCategory
        );
    }

    /**
     * Resolves the actual sender JID from the stanza's addressing.
     *
     * <p>For group, broadcast, and status messages the sender is the
     * {@code participant} attribute. For 1:1 chat messages the sender
     * is the {@code from} attribute.
     *
     * @param fromJid     the {@code from} attribute JID
     * @param participant the {@code participant} attribute JID, or {@code null}
     * @return the resolved sender JID
     *
     * @throws IllegalArgumentException if a group/broadcast message
     *                                  is missing its participant
     *
     * @implNote WAWebHandleMsgParser function y():
     * {@code sender = (from.isGroup() || from.isBroadcast()) ? participant : from}.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static Jid resolveSender(Jid fromJid, Jid participant) {
        // WAWebHandleMsgParser function y()
        // Selects participant for group/broadcast messages and from otherwise

        if (fromJid.hasGroupOrCommunityServer() || fromJid.hasBroadcastServer()) {
            if (participant == null) {
                throw new IllegalArgumentException(
                        "Group/broadcast/status message from " + fromJid
                                + " missing participant attribute");
            }
            return participant;
        }
        return fromJid;
    }

    /**
     * Classifies the message type based on the stanza's addressing
     * and the current user's JID.
     *
     * <p>The classification mirrors WA Web's
     * {@code WAWebHandleMsgParser function C()}, which produces the
     * value used by the downstream processing pipeline to choose
     * DSM-unwrapping rules, receipt types, and placeholder generation.
     *
     * @param fromJid     the {@code from} attribute JID
     * @param participant the {@code participant} attribute JID, or {@code null}
     * @param selfJid     the current user's JID, or {@code null}
     * @param encs        the parsed encrypted payloads, used for the
     *                    isDirect check on status messages
     * @param category    the stanza message category
     * @return the classified message type
     *
     * @implNote WAWebHandleMsgParser function C(): determines CHAT,
     * GROUP, PEER_BROADCAST, OTHER_BROADCAST, DIRECT_PEER_STATUS, or
     * OTHER_STATUS based on the from JID type and participant
     * presence, with Cobalt extending the CHAT case into PEER_CHAT
     * when category is peer.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static MessageType resolveMessageType(
            Jid fromJid,
            Jid participant,
            Jid selfJid,
            List<MessageReceiveEncryptedPayload> encs,
            String category) {
        // WAWebHandleMsgParser function C()
        // Classifies user/LID/bot senders as CHAT, or PEER_CHAT when category is peer

        if (fromJid.hasUserServer() || fromJid.hasLidServer() || fromJid.hasBotServer()) {
            if("peer".equals(category)) {
                return MessageType.PEER_CHAT;
            } else {
                return MessageType.CHAT;
            }
        }

        // WAWebHandleMsgParser function C()
        // Classifies group/community senders as GROUP

        if (fromJid.hasGroupOrCommunityServer()) {
            return MessageType.GROUP;
        }

        // WAWebHandleMsgParser function C()
        // Disambiguates broadcast types based on self-identity and status vs regular broadcast

        if (fromJid.hasBroadcastServer()) {
            var isStatus = fromJid.isStatusBroadcastAccount();
            var isSelf = isMeAccount(participant, selfJid);
            if (!isStatus) {
                return isSelf ? MessageType.PEER_BROADCAST : MessageType.OTHER_BROADCAST;
            }

            // WAWebHandleMsgParser function C()
            // Direct-peer status requires a self-originated broadcast with all enc payloads non-SKMSG

            var isDirect = encs.stream().noneMatch(enc ->
                    enc.e2eType().isSenderKeyMessage());
            if (isSelf && isDirect) {
                return MessageType.DIRECT_PEER_STATUS;
            }
            return MessageType.OTHER_STATUS;
        }

        return MessageType.CHAT;
    }

    /**
     * Returns whether the given participant JID represents the logged-in
     * user's account.
     *
     * <p>Comparison is done on user-level JIDs so that companion-device
     * addressing is treated as the same account as the primary.
     *
     * @param participant the participant JID, or {@code null}
     * @param selfJid     the current user's JID, or {@code null}
     * @return {@code true} when participant and selfJid denote the
     *         same account
     *
     * @implNote WAWebHandleMsgParser function y(): uses
     * {@code isMeAccount(participant)} which compares the participant's
     * user-level JID against the logged-in user.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isMeAccount(Jid participant, Jid selfJid) {
        // WAWebHandleMsgParser function y()
        // Returns true when both JIDs are non-null and share the same user-level identity

        return selfJid != null
                && participant != null
                && participant.toUserJid().equals(selfJid.toUserJid());
    }

    /**
     * Parses every {@code <enc>} child of the message node into an
     * encrypted payload record.
     *
     * <p>Each enc node carries a Signal encryption type, an optional
     * media type, the raw ciphertext bytes, a retry count, and an
     * optional {@code decrypt-fail="hide"} attribute.
     *
     * @param node the parent {@code <message>} node
     * @return the list of parsed encrypted payloads
     *
     * @implNote WAWebHandleMsgParser.incomingMsgParser: maps each enc
     * child extracting type, mediatype, ciphertext, count, and
     * decrypt-fail.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static List<MessageReceiveEncryptedPayload> parseEncryptedPayloads(Node node) {
        // WAWebHandleMsgParser.incomingMsgParser
        // Iterates every enc child and decodes its attributes and content

        var encNodes = node.getChildren("enc");
        var payloads = new ArrayList<MessageReceiveEncryptedPayload>(encNodes.size());
        for (var encNode : encNodes) {
            var typeStr = encNode.getRequiredAttributeAsString("type");
            var e2eType = MessageEncryptionType.fromProtocolValue(typeStr);
            var encMediaType = encNode.getAttributeAsString("mediatype", null);
            var ciphertext = encNode.toContentBytes().orElse(null);

            // WAWebHandleMsgParser.incomingMsgParser
            // Skips encs with no content bytes to avoid producing empty payloads

            if (ciphertext == null || ciphertext.length == 0) {
                continue;
            }
            var retryCount = encNode.getAttributeAsInt("count", 0);
            var hideFail = "hide".equals(
                    encNode.getAttributeAsString("decrypt-fail").orElse(null));
            payloads.add(new MessageReceiveEncryptedPayload(
                    e2eType, encMediaType, ciphertext, retryCount, hideFail));
        }
        return payloads;
    }

    /**
     * Parses the {@code <bot>} child into a {@link MessageReceiveBotInfo},
     * when present.
     *
     * <p>Extracts the bot sender timestamp, the bot edit target id,
     * the bot edit type, the body type, and the business bot
     * classification.
     *
     * @param node the parent {@code <message>} node
     * @return the parsed bot info, or {@code null} if no bot child
     *         exists
     *
     * @implNote WAWebHandleMsgParser function b(): parses the bot node
     * to extract botSenderTimestampMs, botEditTargetId, botEditType,
     * botMsgBodyType, and bizBotType.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MessageReceiveBotInfo parseBotInfo(Node node) {
        // WAWebHandleMsgParser function b()
        // Returns null when no bot child is present

        var botNode = node.getChild("bot", null);
        if (botNode == null) {
            return null;
        }

        // WAWebHandleMsgParser function b()
        // Extracts the bot metadata attributes

        var senderTimestampMs = botNode.getAttributeAsString("sender_timestamp_ms", null);
        var editTargetId = botNode.getAttributeAsString("edit_target_id", null);
        var editType = botNode.getAttributeAsString("edit", null);
        var bodyType = botNode.getAttributeAsString("type", null);
        var bizBotType = botNode.getAttributeAsString("biz_bot", null);
        return new MessageReceiveBotInfo(
                senderTimestampMs,
                editTargetId,
                editType,
                bodyType,
                bizBotType
        );
    }

    /**
     * Parses the business information from the message node into a
     * {@link MessageReceiveBizInfo}, when present.
     *
     * <p>Merges stanza-level attributes ({@code verified_name} int,
     * {@code verified_level} string, and the {@code <verified_name>}
     * child's bytes) with the {@code <biz>} node's attributes
     * ({@code actual_actors}, {@code host_storage},
     * {@code privacy_mode_ts}, {@code native_flow_name},
     * {@code campaign_id}) and the child element presence checks for
     * buttons, list, and hsm envelopes.
     *
     * @param node the parent {@code <message>} node
     * @return the parsed biz info, or {@code null} if neither a
     *         {@code verified_name} attribute nor a {@code <biz>} child
     *         is present
     *
     * @implNote WAWebHandleMsgParser function v(): parses verified_name,
     * verified_level, biz node (actual_actors, host_storage,
     * privacy_mode_ts, native_flow_name, campaign_id, button/list/hsm
     * envelope flags).
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MessageReceiveBizInfo parseBizInfo(Node node) {
        // WAWebHandleMsgParser function v()
        // Reads the stanza-level verified_name attributes and the verified_name child

        var verifiedNameSerial = node.getAttributeAsInt("verified_name", null);
        var verifiedLevel = node.getAttributeAsString("verified_level", null);
        var verifiedNameCert = node.getChild("verified_name")
                .flatMap(Node::toContentBytes)
                .orElse(null);

        // WAWebHandleMsgParser function v()
        // Returns null when no verified-name or biz data is present at all

        var bizNode = node.getChild("biz", null);
        if (verifiedNameSerial == null && verifiedLevel == null
                && verifiedNameCert == null && bizNode == null) {
            return null;
        }

        // WAWebHandleMsgParser function v()
        // Extracts the biz node attributes when the child is present

        Integer actualActors = null;
        Integer hostStorage = null;
        Integer privacyModeTs = null;
        String nativeFlowName = null;
        String campaignId = null;
        var verifiedButtonsEnvelope = false;
        var verifiedListEnvelope = false;
        var verifiedHsmEnvelope = false;

        if (bizNode != null) {
            actualActors = bizNode.getAttributeAsInt("actual_actors", null);
            hostStorage = bizNode.getAttributeAsInt("host_storage", null);
            privacyModeTs = bizNode.getAttributeAsInt("privacy_mode_ts", null);
            campaignId = bizNode.getAttributeAsString("campaign_id", null);
            nativeFlowName = resolveNativeFlowName(bizNode);
            verifiedButtonsEnvelope = bizNode.getChild("buttons").isPresent();
            verifiedListEnvelope = bizNode.getChild("list").isPresent();

            // WAWebHandleMsgParser function v()
            // hsm envelope presence is detected on the message node itself, not the biz node

            verifiedHsmEnvelope = node.getChild("hsm").isPresent();
        }

        // WAWebHandleMsgParser function v()
        // Assembles the parsed biz info with -1 as the default serial when absent

        return new MessageReceiveBizInfo(
                verifiedNameCert,
                verifiedNameSerial != null ? verifiedNameSerial : -1,
                verifiedLevel,
                nativeFlowName,
                campaignId,
                actualActors,
                hostStorage,
                privacyModeTs,
                verifiedButtonsEnvelope,
                verifiedListEnvelope,
                verifiedHsmEnvelope
        );
    }

    /**
     * Resolves the native flow name from the {@code <biz>} node.
     *
     * <p>First checks for a nested
     * {@code <interactive><native_flow name="..."/></interactive>}
     * structure and falls back to a direct {@code native_flow_name}
     * attribute on the biz node.
     *
     * @param bizNode the {@code <biz>} child node
     * @return the native flow name, or {@code null} if not present
     *
     * @implNote WAWebHandleMsgParser function v(): resolves
     * native_flow_name from either the nested interactive/native_flow
     * structure or the direct attribute.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static String resolveNativeFlowName(Node bizNode) {
        // WAWebHandleMsgParser function v()
        // Prefers the nested interactive.native_flow name when available

        var interactiveNode = bizNode.getChild("interactive", null);
        if (interactiveNode != null) {
            var nativeFlowNode = interactiveNode.getChild("native_flow", null);
            if (nativeFlowNode != null) {
                var name = nativeFlowNode.getAttributeAsString("name", null);
                if (name != null) {
                    return name;
                }
            }
        }

        // WAWebHandleMsgParser function v()
        // Falls back to the direct native_flow_name attribute on the biz node

        return bizNode.getAttributeAsString("native_flow_name", null);
    }

    /**
     * Parses the {@code <reporting>} child into a
     * {@link MessageReceiveReportingInfo}, when present.
     *
     * <p>Extracts the reporting token bytes and version from the
     * {@code <reporting_token>} child and the reporting tag bytes
     * from the {@code <reporting_tag>} child.
     *
     * @param node the parent {@code <message>} node
     * @return the parsed reporting info, or {@code null} if no
     *         reporting child exists
     *
     * @implNote WAWebHandleMsgParser function k(): parses the reporting
     * node to extract reporting_token (bytes plus version),
     * reporting_tag (bytes), and stanzaTs.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MessageReceiveReportingInfo parseReportingInfo(Node node) {
        // WAWebHandleMsgParser function k()
        // Returns null when the reporting child is absent

        var reportingNode = node.getChild("reporting", null);
        if (reportingNode == null) {
            return null;
        }

        // WAWebHandleMsgParser function k()
        // Preserves the parent stanza timestamp alongside the reporting token

        var stanzaTs = Instant.ofEpochSecond(node.getRequiredAttributeAsLong("t"));

        // WAWebHandleMsgParser function k()
        // Extracts the reporting token bytes and version from the reporting_token child

        var tokenNode = reportingNode.getChild("reporting_token", null);
        byte[] reportingToken = null;
        var version = 0;
        if (tokenNode != null) {
            reportingToken = tokenNode.toContentBytes().orElse(null);
            version = tokenNode.getAttributeAsInt("v", 0);
        }

        // WAWebHandleMsgParser function k()
        // Extracts the reporting tag bytes from the reporting_tag child

        var reportingTag = reportingNode.getChild("reporting_tag")
                .flatMap(Node::toContentBytes)
                .orElse(null);

        return new MessageReceiveReportingInfo(
                stanzaTs,
                reportingToken,
                version,
                reportingTag
        );
    }

    /**
     * Parses the payment information from the {@code <pay>} and
     * {@code <transaction>} children of the message stanza.
     *
     * <p>Both {@code <pay>} and {@code <transaction>} appear as direct
     * children of the message node (siblings, not nested). When both
     * are absent the function returns {@code null}; when the
     * transaction child is present it takes precedence over pay.
     *
     * @param node the parent {@code <message>} node
     * @return the parsed payment info, or {@code null} if neither
     *         pay nor transaction child exists
     *
     * @implNote WAWebHandleMsgParser function R(): parses pay node
     * type, receiver JID string, and transaction
     * currency/amount/status/timestamp, and detects novi/futureproof
     * transactions.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MessageReceivePaymentInfo parsePaymentInfo(Node node) {
        // WAWebHandleMsgParser function R()
        // Retrieves the sibling pay and transaction children

        var payNode = node.getChild("pay", null);
        var transactionNode = node.getChild("transaction", null);

        if (payNode == null && transactionNode == null) {
            return null;
        }

        // WAWebHandleMsgParser function R()
        // When the transaction child is present it carries the authoritative payment fields

        if (transactionNode != null) {
            var currency = transactionNode.getAttributeAsString("currency", null);
            var amount1000 = transactionNode.getAttributeAsLong("amount_1000", null);
            var status = transactionNode.getAttributeAsString("status", null);
            var ts = transactionNode.getAttributeAsLong("t", null);
            var receiver = transactionNode.getAttributeAsString("receiver", null);
            return new MessageReceivePaymentInfo(
                    false,
                    receiver,
                    currency,
                    amount1000,
                    status,
                    ts
            );
        }

        // WAWebHandleMsgParser function R()
        // Falls back to the pay child, branching on the pay type attribute

        var payType = payNode.getAttributeAsString("type", null);
        if ("send".equals(payType)) {
            var currency = payNode.getAttributeAsString("currency", null);
            var amount1000 = payNode.getAttributeAsLong("amount_1000", null);
            var receiver = payNode.getAttributeAsString("receiver", null);
            if (receiver == null) {
                receiver = node.getAttributeAsString("recipient", null);
            }
            var ts = node.getAttributeAsLong("t", null);
            return new MessageReceivePaymentInfo(
                    false,
                    receiver,
                    currency,
                    amount1000,
                    null,
                    ts
            );
        }

        // WAWebHandleMsgParser function R()
        // Request and invite pay types have no usable payment data

        return null;
    }

    /**
     * Parses the {@code <participants>} child into a list of
     * {@link MessageReceiveBroadcastParticipant} entries.
     *
     * <p>Each {@code <to>} child within the participants node
     * represents one recipient of the broadcast.
     *
     * @param node the parent {@code <message>} node
     * @return the list of broadcast participants, empty if the
     *         participants child is absent
     *
     * @implNote WAWebHandleMsgParser function y(): maps each
     * {@code <to>} child within {@code <participants>} to extract
     * jid, eph_setting, peer_recipient_lid, peer_recipient_pn,
     * peer_recipient_username, and recipient_latest_lid.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static List<MessageReceiveBroadcastParticipant> parseBroadcastParticipants(Node node) {
        // WAWebHandleMsgParser function y()
        // Returns an empty list when the participants child is absent

        var participantsNode = node.getChild("participants", null);
        if (participantsNode == null) {
            return List.of();
        }

        // WAWebHandleMsgParser function y()
        // Iterates each to child and extracts its recipient and mapping attributes

        var toNodes = participantsNode.getChildren("to");
        var participants = new ArrayList<MessageReceiveBroadcastParticipant>(toNodes.size());
        for (var toNode : toNodes) {
            var jid = toNode.getRequiredAttributeAsJid("jid");
            var ephSetting = toNode.getAttributeAsString("eph_setting", null);
            var peerRecipientLid = toNode.getAttributeAsJid("peer_recipient_lid", null);
            var peerRecipientPn = toNode.getAttributeAsJid("peer_recipient_pn", null);
            var peerRecipientUsername = toNode.getAttributeAsString("peer_recipient_username", null);
            var recipientLatestLid = toNode.getAttributeAsJid("recipient_latest_lid", null);
            participants.add(new MessageReceiveBroadcastParticipant(
                    jid,
                    ephSetting,
                    peerRecipientLid,
                    peerRecipientPn,
                    peerRecipientUsername,
                    recipientLatestLid
            ));
        }
        return participants;
    }
}
