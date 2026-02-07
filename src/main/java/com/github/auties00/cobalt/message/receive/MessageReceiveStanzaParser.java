package com.github.auties00.cobalt.message.receive;

import com.github.auties00.cobalt.message.receive.addressing.LidMessageAddressingMode;
import com.github.auties00.cobalt.message.receive.addressing.MessageAddressingMode;
import com.github.auties00.cobalt.message.receive.addressing.PhoneNumberMessageAddressingMode;
import com.github.auties00.cobalt.message.send.crypto.MessageSignalEncryptionType;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;

import java.util.*;

/**
 * Parses incoming message stanzas according to the WhatsApp protocol specification.
 * <p>
 * Message stanza structure:
 * <pre>{@code
 * <message id="{msg_id}" from="{sender}" to="{recipient}" type="{type}" t="{timestamp}"
 *          participant="{sender_in_group}" addressing_mode="{mode}"
 *          participant_pn="{pn}" participant_lid="{lid}"
 *          sender_pn="{pn}" sender_lid="{lid}"
 *          recipient_pn="{pn}" recipient_lid="{lid}"
 *          peer_recipient_pn="{pn}" peer_recipient_lid="{lid}"
 *          recipient_latest_lid="{lid}" recipient_username="{username}"
 *          username="{username}" display_name="{name}">
 *   <enc v="2" type="{pkmsg|msg|skmsg}" count="{retry_count}">{ciphertext}</enc>
 *   <enc v="2" type="{pkmsg|msg}">{fallback_ciphertext}</enc>
 *   <device-identity>{signed_device_identity}</device-identity>
 *   <meta view_once="true" ephemeral="{duration}" polltype="{type}" sender_country_code="{code}"/>
 *   <bot type="{type}" local_automated_type="{auto}" client_thread_id="{id}" edit="{type}"/>
 *   <participants><to jid="{jid}" eph_setting="{setting}"/></participants>
 *   <unavailable type="{view_once}" hosted="{true}"/>
 *   <verified_name>{certificate}</verified_name>
 *   <biz privacy_mode_ts="{ts}" actual_actors="{n}" host_storage="{n}" native_flow_name="{name}"/>
 *   <hsm tag="{tag}" category="{category}"/>
 *   <pay type="{send|request|invite}"/>
 *   <transaction>{payment_data}</transaction>
 *   <rcat>{rate_category}</rcat>
 *   <reporting><reporting_token v="{version}">{token}</reporting_token><reporting_tag>{tag}</reporting_tag></reporting>
 * </message>
 * }</pre>
 */
public final class MessageReceiveStanzaParser {

    private MessageReceiveStanzaParser() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Parses a message node into a structured ParsedMessageStanza.
     *
     * @param messageNode the incoming message node
     * @return the parsed message stanza
     * @throws IllegalArgumentException if the node is invalid or missing required fields
     */
    public static ParsedMessageStanza parse(Node messageNode) {
        Objects.requireNonNull(messageNode, "messageNode cannot be null");

        if (!messageNode.hasDescription("message")) {
            throw new IllegalArgumentException("Expected message node, got: " + messageNode.description());
        }

        // Required attributes
        var id = messageNode.getRequiredAttributeAsString("id");
        var from = messageNode.getRequiredAttributeAsJid("from");
        var type = messageNode.getAttributeAsString("type", "text");
        var timestamp = messageNode.getRequiredAttributeAsLong("t");

        // Optional attributes
        var to = messageNode.getAttributeAsJid("to");
        var participant = messageNode.getAttributeAsJid("participant");
        var participantPn = messageNode.getAttributeAsJid("participant_pn");
        var participantLid = messageNode.getAttributeAsJid("participant_lid");
        var notify = messageNode.getAttributeAsString("notify");
        var edit = messageNode.getAttributeAsInt("edit");
        var retryCount = messageNode.getAttributeAsInt("count");
        var offline = messageNode.getAttributeAsString("offline");
        var category = messageNode.getAttributeAsString("category");

        // Additional JID mapping attributes
        var senderPn = messageNode.getAttributeAsJid("sender_pn");
        var senderLid = messageNode.getAttributeAsJid("sender_lid");
        var recipientPn = messageNode.getAttributeAsJid("recipient_pn");
        var recipientLid = messageNode.getAttributeAsJid("recipient_lid");
        var peerRecipientPn = messageNode.getAttributeAsJid("peer_recipient_pn");
        var peerRecipientLid = messageNode.getAttributeAsJid("peer_recipient_lid");
        var recipientLatestLid = messageNode.getAttributeAsJid("recipient_latest_lid");
        var recipientUsername = messageNode.getAttributeAsString("recipient_username");

        // Username/display name attributes
        var username = messageNode.getAttributeAsString("username");
        var displayName = messageNode.getAttributeAsString("display_name");
        var participantUsername = messageNode.getAttributeAsString("participant_username");
        var peerRecipientUsername = messageNode.getAttributeAsString("peer_recipient_username");

        // Addressing mode
        var addressingMode = parseAddressingMode(messageNode);

        // Parse enc nodes
        var encNodes = parseEncNodes(messageNode);

        // Parse optional child nodes
        var deviceIdentity = messageNode.getChild("device-identity")
                .flatMap(Node::toContentBytes);
        var meta = parseMeta(messageNode);
        var bot = parseBot(messageNode);
        var unavailable = parseUnavailable(messageNode);
        var participants = parseParticipants(messageNode);
        var bizInfo = parseBizInfo(messageNode);
        var hsmInfo = parseHsmInfo(messageNode);
        var paymentInfo = parsePaymentInfo(messageNode);
        var reportingInfo = parseReportingInfo(messageNode);
        var verifiedNameCert = messageNode.getChild("verified_name")
                .flatMap(Node::toContentBytes);
        var rcat = messageNode.getChild("rcat")
                .flatMap(Node::toContentBytes);

        return new ParsedMessageStanza(
                id,
                from,
                to,
                type,
                timestamp,
                participant,
                participantPn,
                participantLid,
                senderPn,
                senderLid,
                recipientPn,
                recipientLid,
                peerRecipientPn,
                peerRecipientLid,
                recipientLatestLid,
                recipientUsername,
                username,
                displayName,
                participantUsername,
                peerRecipientUsername,
                addressingMode,
                encNodes,
                deviceIdentity,
                meta,
                bot,
                unavailable,
                participants,
                bizInfo,
                hsmInfo,
                paymentInfo,
                reportingInfo,
                verifiedNameCert,
                rcat,
                notify,
                edit,
                retryCount,
                offline,
                category
        );
    }

    private static Optional<MessageAddressingMode> parseAddressingMode(Node messageNode) {
        var modeValue = messageNode.getAttributeAsString("addressing_mode");
        if (modeValue.isEmpty()) {
            return Optional.empty();
        }

        var peerPn = messageNode.getAttributeAsJid("peer_recipient_pn", null);
        var peerLid = messageNode.getAttributeAsJid("peer_recipient_lid");
        var recipientPn = messageNode.getAttributeAsJid("recipient_pn", null);
        var username = messageNode.getAttributeAsString("peer_recipient_username", null);

        return switch (modeValue.get()) {
            case MessageAddressingMode.PHONE_NUMBER_VALUE -> Optional.of(
                    new PhoneNumberMessageAddressingMode(
                            messageNode.getRequiredAttributeAsJid("from"),
                            peerLid.orElse(null),
                            username
                    )
            );
            case MessageAddressingMode.LID_VALUE -> {
                var lidJid = peerLid.or(() -> messageNode.getAttributeAsJid("from"));
                yield lidJid.map(lid -> (MessageAddressingMode) new LidMessageAddressingMode(
                        lid,
                        recipientPn,
                        peerPn,
                        username
                ));
            }
            default -> Optional.empty();
        };
    }

    private static List<EncNode> parseEncNodes(Node messageNode) {
        var encNodes = new ArrayList<EncNode>();

        for (var encNode : messageNode.getChildren("enc")) {
            var typeStr = encNode.getRequiredAttributeAsString("type");
            var type = MessageSignalEncryptionType.fromProtocolValue(typeStr);
            var version = encNode.getAttributeAsInt("v", 2);
            var count = encNode.getAttributeAsLong("count");
            var mediatype = encNode.getAttributeAsString("mediatype");
            var decryptFail = encNode.getAttributeAsString("decrypt-fail");
            var hideFail = decryptFail.map(s -> s.equals("hide")).orElse(false);
            var ciphertext = encNode.toContentBytes()
                    .orElseThrow(() -> new IllegalArgumentException("enc node missing ciphertext content"));

            encNodes.add(new EncNode(
                    type,
                    ciphertext,
                    version,
                    count.isPresent() ? Optional.of((int) count.getAsLong()) : Optional.empty(),
                    mediatype,
                    decryptFail,
                    hideFail
            ));
        }

        return encNodes;
    }

    private static Optional<MetaNode> parseMeta(Node messageNode) {
        return messageNode.getChild("meta").map(metaNode -> {
            var viewOnce = metaNode.getAttributeAsBool("view_once", false);
            var ephemeral = metaNode.getAttributeAsLong("ephemeral");
            var pollType = metaNode.getAttributeAsString("polltype");
            var origin = metaNode.getAttributeAsString("origin");
            var bizSource = metaNode.getAttributeAsString("biz_source");
            var senderCountryCode = metaNode.getAttributeAsString("sender_country_code");
            var statusSetting = metaNode.getAttributeAsString("status_setting");
            var statusMentioned = metaNode.getAttributeAsBool("status_mentioned", false);
            var threadMsgId = metaNode.getAttributeAsString("thread_msg_id");
            var threadMsgSenderJid = metaNode.getAttributeAsJid("thread_msg_sender_jid");
            var targetId = metaNode.getAttributeAsString("target_id");
            var targetSenderJid = metaNode.getAttributeAsJid("target_sender_jid");
            var targetChatJid = metaNode.getAttributeAsJid("target_chat_jid");
            var targetChatJidLid = metaNode.getAttributeAsJid("target_chat_jid_lid");
            var capi = metaNode.getAttributeAsBool("capi", false);
            var eventType = metaNode.getAttributeAsString("event_type");
            var contextSource = metaNode.getAttributeAsString("context_source");
            var appdata = metaNode.getAttributeAsString("appdata");

            return new MetaNode(
                    viewOnce,
                    ephemeral.isPresent() ? Optional.of((int) ephemeral.getAsLong()) : Optional.empty(),
                    pollType,
                    origin,
                    bizSource,
                    senderCountryCode,
                    statusSetting,
                    statusMentioned,
                    threadMsgId,
                    threadMsgSenderJid,
                    targetId,
                    targetSenderJid,
                    targetChatJid,
                    targetChatJidLid,
                    capi,
                    eventType,
                    contextSource,
                    appdata
            );
        });
    }

    private static Optional<BotNode> parseBot(Node messageNode) {
        return messageNode.getChild("bot").map(botNode -> new BotNode(
                botNode.getAttributeAsString("type"),
                botNode.getAttributeAsString("local_automated_type"),
                botNode.getAttributeAsString("client_thread_id"),
                botNode.getAttributeAsString("sender_timestamp_ms"),
                botNode.getAttributeAsString("edit_target_id"),
                botNode.getAttributeAsString("edit"),
                botNode.getAttributeAsString("biz_bot")
        ));
    }

    private static Optional<UnavailableNode> parseUnavailable(Node messageNode) {
        return messageNode.getChild("unavailable").map(unavailableNode -> new UnavailableNode(
                unavailableNode.getAttributeAsString("type").map(t -> t.equals("view_once")).orElse(false),
                unavailableNode.getAttributeAsBool("hosted", false)
        ));
    }

    private static List<ParticipantNode> parseParticipants(Node messageNode) {
        var participantsNode = messageNode.getChild("participants");
        if (participantsNode.isEmpty()) {
            return List.of();
        }

        var participants = new ArrayList<ParticipantNode>();
        for (var toNode : participantsNode.get().getChildren("to")) {
            var jid = toNode.getAttributeAsJid("jid");
            if (jid.isEmpty()) {
                continue;
            }

            var ephSetting = toNode.getAttributeAsString("eph_setting");
            var peerRecipientLid = toNode.getAttributeAsJid("peer_recipient_lid");
            var peerRecipientPn = toNode.getAttributeAsJid("peer_recipient_pn");
            var peerRecipientUsername = toNode.getAttributeAsString("peer_recipient_username");
            var recipientLatestLid = toNode.getAttributeAsJid("recipient_latest_lid");

            participants.add(new ParticipantNode(
                    jid.get(),
                    ephSetting,
                    peerRecipientLid,
                    peerRecipientPn,
                    peerRecipientUsername,
                    recipientLatestLid
            ));
        }

        return participants;
    }

    private static Optional<BizInfoNode> parseBizInfo(Node messageNode) {
        return messageNode.getChild("biz").map(bizNode -> {
            var actualActors = bizNode.getAttributeAsInt("actual_actors");
            var hostStorage = bizNode.getAttributeAsInt("host_storage");
            var privacyModeTs = bizNode.getAttributeAsLong("privacy_mode_ts");
            var nativeFlowName = bizNode.getAttributeAsString("native_flow_name");
            var campaignId = bizNode.getAttributeAsString("campaign_id");
            var verifiedButtonsEnvelope = bizNode.hasChild("buttons");
            var verifiedListEnvelope = bizNode.hasChild("list");

            // Check for native flow in interactive child
            var interactiveNativeFlowName = bizNode.getChild("interactive")
                    .flatMap(i -> i.getChild("native_flow"))
                    .flatMap(nf -> nf.getAttributeAsString("name"));

            return new BizInfoNode(
                    actualActors,
                    hostStorage,
                    privacyModeTs,
                    nativeFlowName.or(() -> interactiveNativeFlowName),
                    campaignId,
                    verifiedButtonsEnvelope,
                    verifiedListEnvelope
            );
        });
    }

    private static Optional<HsmInfoNode> parseHsmInfo(Node messageNode) {
        return messageNode.getChild("hsm").map(hsmNode -> new HsmInfoNode(
                hsmNode.getAttributeAsString("tag"),
                hsmNode.getAttributeAsString("category")
        ));
    }

    private static Optional<PaymentInfoNode> parsePaymentInfo(Node messageNode) {
        var payNode = messageNode.getChild("pay");
        var transactionNode = messageNode.getChild("transaction");

        if (payNode.isEmpty() && transactionNode.isEmpty()) {
            return Optional.empty();
        }

        if (transactionNode.isPresent()) {
            // Parse transaction node - simplified for now
            return Optional.of(new PaymentInfoNode(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    true
            ));
        }

        if (payNode.isPresent()) {
            var pay = payNode.get();
            var payType = pay.getAttributeAsString("type");
            var receiver = pay.getAttributeAsString("receiver");
            var currency = pay.getAttributeAsString("currency");
            var amount1000 = pay.getAttributeAsLong("amount1000");

            return Optional.of(new PaymentInfoNode(
                    payType,
                    receiver,
                    currency,
                    amount1000.isPresent() ? Optional.of(amount1000.getAsLong()) : Optional.empty(),
                    false
            ));
        }

        return Optional.empty();
    }

    private static Optional<ReportingInfoNode> parseReportingInfo(Node messageNode) {
        return messageNode.getChild("reporting").map(reportingNode -> {
            var tokenNode = reportingNode.getChild("reporting_token");
            var tagNode = reportingNode.getChild("reporting_tag");

            return new ReportingInfoNode(
                    tokenNode.flatMap(Node::toContentBytes),
                    tokenNode.flatMap(n -> n.getAttributeAsInt("v").stream().boxed().findFirst()),
                    tagNode.flatMap(Node::toContentBytes)
            );
        });
    }

    /**
     * Parsed message stanza containing all extracted data.
     */
    public record ParsedMessageStanza(
            String id,
            Jid from,
            Optional<Jid> to,
            String type,
            long timestamp,
            Optional<Jid> participant,
            Optional<Jid> participantPn,
            Optional<Jid> participantLid,
            Optional<Jid> senderPn,
            Optional<Jid> senderLid,
            Optional<Jid> recipientPn,
            Optional<Jid> recipientLid,
            Optional<Jid> peerRecipientPn,
            Optional<Jid> peerRecipientLid,
            Optional<Jid> recipientLatestLid,
            Optional<String> recipientUsername,
            Optional<String> username,
            Optional<String> displayName,
            Optional<String> participantUsername,
            Optional<String> peerRecipientUsername,
            Optional<MessageAddressingMode> addressingMode,
            List<EncNode> encNodes,
            Optional<byte[]> deviceIdentity,
            Optional<MetaNode> meta,
            Optional<BotNode> bot,
            Optional<UnavailableNode> unavailable,
            List<ParticipantNode> participants,
            Optional<BizInfoNode> bizInfo,
            Optional<HsmInfoNode> hsmInfo,
            Optional<PaymentInfoNode> paymentInfo,
            Optional<ReportingInfoNode> reportingInfo,
            Optional<byte[]> verifiedNameCert,
            Optional<byte[]> rcat,
            Optional<String> notifyStr,
            OptionalInt edit,
            OptionalInt retryCount,
            Optional<String> offline,
            Optional<String> category
    ) {
        public ParsedMessageStanza {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(from, "from cannot be null");
            Objects.requireNonNull(type, "type cannot be null");
            encNodes = List.copyOf(encNodes);
            participants = List.copyOf(participants);
        }

        /**
         * Returns true if this is a group or community message.
         */
        public boolean isGroupMessage() {
            return from.hasGroupOrCommunityServer();
        }

        /**
         * Returns true if this is a broadcast message (including status).
         */
        public boolean isBroadcastMessage() {
            return from.hasBroadcastServer();
        }

        /**
         * Returns true if this is a status broadcast message.
         */
        public boolean isStatusMessage() {
            return from.equals(Jid.statusBroadcastAccount());
        }

        /**
         * Returns true if this is a newsletter message.
         */
        public boolean isNewsletterMessage() {
            return from.hasNewsletterServer();
        }

        /**
         * Returns true if this message is unavailable (fanout placeholder).
         */
        public boolean isUnavailable() {
            return unavailable.isPresent();
        }

        /**
         * Returns true if this is a view-once unavailable message.
         */
        public boolean isViewOnceUnavailable() {
            return unavailable.map(UnavailableNode::viewOnce).orElse(false);
        }

        /**
         * Returns true if this is a hosted unavailable message.
         */
        public boolean isHostedUnavailable() {
            return unavailable.map(UnavailableNode::hosted).orElse(false);
        }

        /**
         * Returns the actual sender JID.
         * For groups, this is the participant. For 1:1, this is the from JID.
         */
        public Jid senderJid() {
            return participant.orElse(from);
        }

        /**
         * Returns the chat JID.
         * For groups, this is the from (group JID). For 1:1, this is the sender's user JID.
         */
        public Jid chatJid() {
            if (isGroupMessage() || isStatusMessage() || isBroadcastMessage()) {
                return from;
            }
            return senderJid().toUserJid();
        }

        /**
         * Returns the first enc node, if any.
         */
        public Optional<EncNode> firstEncNode() {
            return encNodes.isEmpty() ? Optional.empty() : Optional.of(encNodes.getFirst());
        }

        /**
         * Returns true if this message has an edit attribute (is an edit/revoke).
         */
        public boolean isEdit() {
            return edit.isPresent();
        }

        /**
         * Returns true if this is a view-once message.
         */
        public boolean isViewOnce() {
            return meta.map(MetaNode::viewOnce).orElse(false);
        }

        /**
         * Returns the ephemeral duration, if set.
         */
        public Optional<Integer> ephemeralDuration() {
            return meta.flatMap(MetaNode::ephemeralDuration);
        }

        /**
         * Returns true if any enc node has the hideFail flag set.
         * Per edge case 58, this suppresses duplicate notifications.
         */
        public boolean hasHideFailFlag() {
            return encNodes.stream().anyMatch(EncNode::hideFail);
        }

        /**
         * Returns true if this is a reaction or poll vote message.
         * These types should suppress duplicate notifications per edge case 58.
         */
        public boolean isReactionOrPollVote() {
            return "reaction".equals(type) || "pollvote".equals(type);
        }

        /**
         * Returns true if this is a comment message type.
         */
        public boolean isComment() {
            return "comment".equals(type);
        }

        /**
         * Returns true if this is an event response message.
         */
        public boolean isEventResponse() {
            return "event_response".equals(type);
        }

        /**
         * Returns true if this is an event edit message.
         */
        public boolean isEventEdit() {
            return "event_edit".equals(type);
        }

        /**
         * Returns true if duplicate notifications should be suppressed for this message.
         * Based on edge case 58: suppress if hideFail flag set OR if reaction/poll vote.
         */
        public boolean shouldSuppressDuplicateNotification() {
            return hasHideFailFlag() || isReactionOrPollVote();
        }

        /**
         * Returns true if this is an offline message.
         */
        public boolean isOffline() {
            return offline.isPresent();
        }

        /**
         * Returns the raw offline value as a string (can be used to get offline count).
         */
        public OptionalInt offlineCount() {
            return offline
                    .map(s -> {
                        try {
                            return OptionalInt.of(Integer.parseInt(s));
                        } catch (NumberFormatException e) {
                            return OptionalInt.empty();
                        }
                    })
                    .orElse(OptionalInt.empty());
        }

        /**
         * Returns true if this is a peer message (from own device).
         */
        public boolean isPeerMessage() {
            return category.map(c -> c.equals("peer")).orElse(false);
        }

        /**
         * Returns true if CAPI mode is enabled for this message.
         */
        public boolean isCapi() {
            return meta.map(MetaNode::capi).orElse(false);
        }

        /**
         * Returns the target message ID for reactions/edits/replies.
         */
        public Optional<String> targetId() {
            return meta.flatMap(MetaNode::targetId);
        }

        /**
         * Returns the target sender JID for reactions/edits/replies.
         */
        public Optional<Jid> targetSenderJid() {
            return meta.flatMap(MetaNode::targetSenderJid);
        }
    }

    /**
     * Parsed enc node containing encryption data.
     */
    public record EncNode(
            MessageSignalEncryptionType type,
            byte[] ciphertext,
            int version,
            Optional<Integer> count,
            Optional<String> mediatype,
            Optional<String> decryptFail,
            boolean hideFail
    ) {
        public EncNode {
            Objects.requireNonNull(type, "type cannot be null");
            Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        }

        /**
         * Returns true if this is a sender key message (group encryption).
         */
        public boolean isSenderKeyMessage() {
            return type == MessageSignalEncryptionType.SKMSG;
        }

        /**
         * Returns true if this is a prekey message (new session).
         */
        public boolean isPreKeyMessage() {
            return type == MessageSignalEncryptionType.PKMSG;
        }

        /**
         * Returns the retry count, defaulting to 0.
         */
        public int retryCount() {
            return count.orElse(0);
        }
    }

    /**
     * Parsed meta node containing message metadata.
     */
    public record MetaNode(
            boolean viewOnce,
            Optional<Integer> ephemeralDuration,
            Optional<String> pollType,
            Optional<String> origin,
            Optional<String> bizSource,
            Optional<String> senderCountryCode,
            Optional<String> statusSetting,
            boolean statusMentioned,
            Optional<String> threadMsgId,
            Optional<Jid> threadMsgSenderJid,
            Optional<String> targetId,
            Optional<Jid> targetSenderJid,
            Optional<Jid> targetChatJid,
            Optional<Jid> targetChatJidLid,
            boolean capi,
            Optional<String> eventType,
            Optional<String> contextSource,
            Optional<String> appdata
    ) {
    }

    /**
     * Parsed bot node for bot-related messages.
     */
    public record BotNode(
            Optional<String> type,
            Optional<String> automatedType,
            Optional<String> clientThreadId,
            Optional<String> senderTimestampMs,
            Optional<String> editTargetId,
            Optional<String> editType,
            Optional<String> bizBotType
    ) {
    }

    /**
     * Parsed unavailable node for fanout placeholder messages.
     */
    public record UnavailableNode(
            boolean viewOnce,
            boolean hosted
    ) {
    }

    /**
     * Parsed participant node for broadcast lists.
     */
    public record ParticipantNode(
            Jid jid,
            Optional<String> ephSetting,
            Optional<Jid> peerRecipientLid,
            Optional<Jid> peerRecipientPn,
            Optional<String> peerRecipientUsername,
            Optional<Jid> recipientLatestLid
    ) {
    }

    /**
     * Parsed biz info node for business messages.
     */
    public record BizInfoNode(
            OptionalInt actualActors,
            OptionalInt hostStorage,
            OptionalLong privacyModeTs,
            Optional<String> nativeFlowName,
            Optional<String> campaignId,
            boolean verifiedButtonsEnvelope,
            boolean verifiedListEnvelope
    ) {
        /**
         * Returns true if privacy mode info is present.
         */
        public boolean hasPrivacyMode() {
            return actualActors.isPresent() && hostStorage.isPresent() && privacyModeTs.isPresent();
        }
    }

    /**
     * Parsed HSM info node for template messages.
     */
    public record HsmInfoNode(
            Optional<String> tag,
            Optional<String> category
    ) {
    }

    /**
     * Parsed payment info node.
     */
    public record PaymentInfoNode(
            Optional<String> payType,
            Optional<String> receiver,
            Optional<String> currency,
            Optional<Long> amount1000,
            boolean isTransaction
    ) {
    }

    /**
     * Parsed reporting info node for message reporting.
     */
    public record ReportingInfoNode(
            Optional<byte[]> reportingToken,
            Optional<Integer> version,
            Optional<byte[]> reportingTag
    ) {
    }
}
