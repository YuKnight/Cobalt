package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.message.addressing.LidMessageAddressingMode;
import com.github.auties00.cobalt.message.addressing.MessageAddressingMode;
import com.github.auties00.cobalt.message.addressing.PhoneNumberMessageAddressingMode;
import com.github.auties00.cobalt.message.protocol.MessageSignalEncryptionType;
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
 *          participant_pn="{pn}" participant_lid="{lid}">
 *   <enc v="2" type="{pkmsg|msg|skmsg}" count="{retry_count}">{ciphertext}</enc>
 *   <enc v="2" type="{pkmsg|msg}">{fallback_ciphertext}</enc>
 *   <device-identity>{signed_device_identity}</device-identity>
 *   <meta view_once="true" ephemeral="{duration}" polltype="{type}"/>
 *   <bot type="{type}" local_automated_type="{auto}" client_thread_id="{id}"/>
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
        var notify = messageNode.getAttributeAsString("notifyStr");
        var edit = messageNode.getAttributeAsInt("edit");
        var retryCount = messageNode.getAttributeAsInt("count");
        var offline = messageNode.getAttributeAsBool("offline", false);
        var category = messageNode.getAttributeAsString("category");

        // Addressing mode
        var addressingMode = parseAddressingMode(messageNode);

        // Parse enc nodes
        var encNodes = parseEncNodes(messageNode);

        // Parse optional child nodes
        var deviceIdentity = messageNode.getChild("device-identity")
                .flatMap(Node::toContentBytes);
        var meta = parseMeta(messageNode);
        var bot = parseBot(messageNode);

        return new ParsedMessageStanza(
                id,
                from,
                to,
                type,
                timestamp,
                participant,
                participantPn,
                participantLid,
                addressingMode,
                encNodes,
                deviceIdentity,
                meta,
                bot,
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
                // For incoming messages, we construct from attributes
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
            // Edge case 58: hideFail flag suppresses duplicate notification
            var hideFail = encNode.getAttributeAsBool("hide_fail", false);
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

            return new MetaNode(
                    viewOnce,
                    ephemeral.isPresent() ? Optional.of((int) ephemeral.getAsLong()) : Optional.empty(),
                    pollType
            );
        });
    }

    private static Optional<BotNode> parseBot(Node messageNode) {
        return messageNode.getChild("bot").map(botNode -> new BotNode(
                botNode.getAttributeAsString("type"),
                botNode.getAttributeAsString("local_automated_type"),
                botNode.getAttributeAsString("client_thread_id")
        ));
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
            Optional<MessageAddressingMode> addressingMode,
            List<EncNode> encNodes,
            Optional<byte[]> deviceIdentity,
            Optional<MetaNode> meta,
            Optional<BotNode> bot,
            Optional<String> notifyStr,
            OptionalInt edit,
            OptionalInt retryCount,
            boolean offline,
            Optional<String> category
    ) {
        public ParsedMessageStanza {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(from, "from cannot be null");
            Objects.requireNonNull(type, "type cannot be null");
            encNodes = List.copyOf(encNodes);
        }

        /**
         * Returns true if this is a group or community message.
         */
        public boolean isGroupMessage() {
            return from.hasGroupOrCommunityServer();
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
            if (isGroupMessage() || isStatusMessage()) {
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
         * Returns true if duplicate notifications should be suppressed for this message.
         * Based on edge case 58: suppress if hideFail flag set OR if reaction/poll vote.
         */
        public boolean shouldSuppressDuplicateNotification() {
            return hasHideFailFlag() || isReactionOrPollVote();
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
    }

    /**
     * Parsed meta node containing message metadata.
     */
    public record MetaNode(
            boolean viewOnce,
            Optional<Integer> ephemeralDuration,
            Optional<String> pollType
    ) {
    }

    /**
     * Parsed bot node for bot-related messages.
     */
    public record BotNode(
            Optional<String> type,
            Optional<String> automatedType,
            Optional<String> clientThreadId
    ) {
    }
}
