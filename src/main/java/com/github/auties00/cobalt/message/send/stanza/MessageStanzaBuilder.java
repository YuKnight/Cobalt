package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.message.addressing.MessageAddressingMode;
import com.github.auties00.cobalt.message.addressing.LidMessageAddressingMode;
import com.github.auties00.cobalt.message.addressing.MixedMessageAddressingMode;
import com.github.auties00.cobalt.message.addressing.PhoneNumberMessageAddressingMode;
import com.github.auties00.cobalt.message.send.encryption.MessageDeviceEncryption;
import com.github.auties00.cobalt.message.send.encryption.MessageEncryptedPayload;
import com.github.auties00.cobalt.message.send.MessageSendInput;
import com.github.auties00.cobalt.model.info.DeviceContextInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.model.Message;
import com.github.auties00.cobalt.model.message.model.MessageContainer;
import com.github.auties00.cobalt.model.message.standard.*;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds message stanzas according to the WhatsApp protocol specification.
 */
public final class MessageStanzaBuilder {
    private static final String ENC_VERSION = "2";

    private MessageStanzaBuilder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Builds a message stanza for a private (1:1) chat.
     *
     * @param messageId         the unique message ID
     * @param recipientJid      the recipient chat JID (user, not device)
     * @param deviceEncryptions encryptions for each target device
     * @param phash             the participant hash (null for single device)
     * @param addressingMode    the addressing mode (PN, LID, or mixed)
     * @param deviceIdentity    signed device identity bytes for prekey messages (null if not needed)
     * @param message           the message container
     * @param options           the send options
     * @return the built node builder
     */
    public static NodeBuilder buildPrivateMessageStanza(
            String messageId,
            Jid recipientJid,
            List<MessageDeviceEncryption> deviceEncryptions,
            String phash,
            MessageAddressingMode addressingMode,
            byte[] deviceIdentity,
            MessageContainer message,
            MessageSendInput.Chat options
    ) {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(recipientJid, "recipientJid cannot be null");
        Objects.requireNonNull(deviceEncryptions, "deviceEncryptions cannot be null");
        Objects.requireNonNull(options, "options cannot be null");

        var messageType = determineMessageType(message);

        var builder = new NodeBuilder()
                .description("message")
                .attribute("id", messageId)
                .attribute("type", messageType);

        // Add addressing mode attributes
        addAddressingModeAttributes(builder, addressingMode);

        // Add edit attribute with protocol value (1=revoke, 7=edit, 14=pin)
        if (options instanceof MessageSendInput.Chat.Edit edit) {
            builder.attribute("edit", edit.editType().protocolValue());
        }

        // Add bot-specific attributes
        if (options instanceof MessageSendInput.Chat.Bot) {
            builder.attribute("device_fanout", "false");
        }

        // Single device optimization: send directly to device JID, no participants wrapper
        if (deviceEncryptions.size() == 1 && !(options instanceof MessageSendInput.Chat.Bot)) {
            var singleDevice = deviceEncryptions.getFirst();
            builder.attribute("to", singleDevice.deviceJid());

            var children = new ArrayList<Node>();
            children.add(buildEncNode(singleDevice.payload(), message, options));

            if (deviceIdentity != null && singleDevice.isPreKeyMessage()) {
                children.add(buildDeviceIdentityNode(deviceIdentity));
            }

            // Add optional nodes
            addBotNode(children, options);
            addBizNode(children, options);
            addMetaNode(children, message, options);
            addReportingTokenNode(children, options);
            addSenderContentBindingNode(children, options);
            addTcTokenNode(children, options);
            addCtwaAttributionNode(children, options);

            builder.content(children);
        } else {
            // Multi-device: send to chat JID with participants wrapper
            builder.attribute("to", recipientJid)
                    .attribute("phash", phash);

            var children = new ArrayList<Node>();

            // Build participants node with enc nodes inside
            children.add(buildParticipantsNodeWithEnc(deviceEncryptions, message, options));

            // Add device identity if any prekey messages
            boolean hasPreKeyMessage = deviceEncryptions.stream().anyMatch(MessageDeviceEncryption::isPreKeyMessage);
            if (deviceIdentity != null && hasPreKeyMessage) {
                children.add(buildDeviceIdentityNode(deviceIdentity));
            }

            // Add optional nodes
            addBotNode(children, options);
            addBizNode(children, options);
            addMetaNode(children, message, options);
            addReportingTokenNode(children, options);
            addSenderContentBindingNode(children, options);
            addTcTokenNode(children, options);
            addCtwaAttributionNode(children, options);

            builder.content(children);
        }

        return builder;
    }

    /**
     * Builds a message stanza for a group chat using Sender Key encryption.
     *
     * @param messageId              the unique message ID
     * @param groupJid               the group JID
     * @param senderKeyEncryption    the sender key encrypted message
     * @param senderKeyDistributions encryptions for devices needing sender key distribution
     * @param phash                  the participant hash
     * @param deviceIdentity         signed device identity bytes (null if not needed)
     * @param message                the message container
     * @param options                the send options
     * @return the built node builder
     */
    public static NodeBuilder buildGroupMessageStanza(
            String messageId,
            Jid groupJid,
            MessageEncryptedPayload senderKeyEncryption,
            List<MessageDeviceEncryption> senderKeyDistributions,
            String phash,
            byte[] deviceIdentity,
            MessageContainer message,
            MessageSendInput.Chat options
    ) {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderKeyEncryption, "senderKeyEncryption cannot be null");
        Objects.requireNonNull(options, "options cannot be null");

        var messageType = determineMessageType(message);

        var builder = new NodeBuilder()
                .description("message")
                .attribute("id", messageId)
                .attribute("to", groupJid)
                .attribute("type", messageType)
                .attribute("phash", phash);

        // Add edit attribute with protocol value
        if (options instanceof MessageSendInput.Chat.Edit edit) {
            builder.attribute("edit", edit.editType().protocolValue());
        }

        var children = new ArrayList<Node>();

        // Add skmsg enc node (for all group members with sender key)
        children.add(buildEncNode(senderKeyEncryption, message, options));

        // Build participants node if we have sender key distributions
        if (senderKeyDistributions != null && !senderKeyDistributions.isEmpty()) {
            children.add(buildParticipantsNodeWithEnc(senderKeyDistributions, message, options));

            // Add device identity if needed
            boolean hasPreKeyMessage = senderKeyDistributions.stream().anyMatch(MessageDeviceEncryption::isPreKeyMessage);
            if (deviceIdentity != null && hasPreKeyMessage) {
                children.add(buildDeviceIdentityNode(deviceIdentity));
            }
        }

        // Add optional nodes
        addBotNode(children, options);
        addBizNode(children, options);
        addMetaNode(children, message, options);
        addReportingTokenNode(children, options);
        addSenderContentBindingNode(children, options);
        addTcTokenNode(children, options);
        addCtwaAttributionNode(children, options);

        builder.content(children);

        return builder;
    }

    /**
     * Builds a message stanza for a Community Announcement Group (CAG).
     * CAG messages don't require sender key distribution to linked groups.
     *
     * @param messageId           the unique message ID
     * @param cagJid              the CAG JID
     * @param senderKeyEncryption the sender key encrypted message
     * @param phash               the participant hash
     * @param message             the message container
     * @param options             the send options
     * @return the built node builder
     */
    public static NodeBuilder buildCagMessageStanza(
            String messageId,
            Jid cagJid,
            MessageEncryptedPayload senderKeyEncryption,
            String phash,
            MessageContainer message,
            MessageSendInput.Chat options
    ) {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(cagJid, "cagJid cannot be null");
        Objects.requireNonNull(senderKeyEncryption, "senderKeyEncryption cannot be null");
        Objects.requireNonNull(options, "options cannot be null");

        var messageType = determineMessageType(message);

        var builder = new NodeBuilder()
                .description("message")
                .attribute("id", messageId)
                .attribute("to", cagJid)
                .attribute("type", messageType)
                .attribute("phash", phash);

        if (options instanceof MessageSendInput.Chat.Edit edit) {
            builder.attribute("edit", edit.editType().protocolValue());
        }

        var children = new ArrayList<Node>();

        // Add skmsg enc node
        children.add(buildEncNode(senderKeyEncryption, message, options));

        // Add optional nodes
        addBotNode(children, options);
        addBizNode(children, options);
        addMetaNode(children, message, options);
        addReportingTokenNode(children, options);
        addSenderContentBindingNode(children, options);
        addTcTokenNode(children, options);
        addCtwaAttributionNode(children, options);

        builder.content(children);

        return builder;
    }

    /**
     * Builds a message stanza for a newsletter (unencrypted).
     * Newsletter messages use a different structure - protobuf in body attribute.
     *
     * @param messageId     the unique message ID
     * @param newsletterJid the newsletter JID
     * @param plaintext     the unencrypted protobuf bytes
     * @param message       the message container
     * @return the built node builder
     */
    public static NodeBuilder buildNewsletterMessageStanza(
            String messageId,
            Jid newsletterJid,
            byte[] plaintext,
            MessageContainer message
    ) {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(newsletterJid, "newsletterJid cannot be null");
        Objects.requireNonNull(plaintext, "plaintext cannot be null");

        var messageType = determineMessageType(message);

        var builder = new NodeBuilder()
                .description("message")
                .attribute("id", messageId)
                .attribute("to", newsletterJid)
                .attribute("type", messageType);

        var children = new ArrayList<Node>();

        // Newsletter messages send plaintext protobuf in a body node
        var bodyNode = new NodeBuilder()
                .description("body")
                .content(plaintext)
                .build();
        children.add(bodyNode);

        // Add meta node (newsletters have limited metadata)
        var metaNode = buildNewsletterMetaNode(message);
        if (metaNode != null) {
            children.add(metaNode);
        }

        builder.content(children);
        return builder;
    }

    /**
     * Builds a status broadcast message stanza.
     *
     * @param messageId         the unique message ID
     * @param deviceEncryptions encryptions for each target device
     * @param phash             the participant hash
     * @param deviceIdentity    signed device identity bytes (null if not needed)
     * @param message           the message container
     * @param options           the send options
     * @return the built node builder
     */
    public static NodeBuilder buildStatusBroadcastStanza(
            String messageId,
            List<MessageDeviceEncryption> deviceEncryptions,
            String phash,
            byte[] deviceIdentity,
            MessageContainer message,
            MessageSendInput.Chat options
    ) {
        Objects.requireNonNull(messageId, "messageId cannot be null");
        Objects.requireNonNull(deviceEncryptions, "deviceEncryptions cannot be null");
        Objects.requireNonNull(options, "options cannot be null");

        var messageType = determineMessageType(message);

        var builder = new NodeBuilder()
                .description("message")
                .attribute("id", messageId)
                .attribute("to", Jid.statusBroadcastAccount())
                .attribute("type", messageType)
                .attribute("phash", phash);

        var children = new ArrayList<Node>();

        // Build participants node with enc nodes
        children.add(buildParticipantsNodeWithEnc(deviceEncryptions, message, options));

        // Add device identity if needed
        boolean hasPreKeyMessage = deviceEncryptions.stream().anyMatch(MessageDeviceEncryption::isPreKeyMessage);
        if (deviceIdentity != null && hasPreKeyMessage) {
            children.add(buildDeviceIdentityNode(deviceIdentity));
        }

        addMetaNode(children, message, options);
        builder.content(children);

        return builder;
    }

    /**
     * Builds a participants node containing device JIDs with their enc nodes.
     * Structure:
     * <pre>{@code
     * <participants>
     *   <to jid="{device_jid}">
     *     <enc v="2" type="{type}" ...>{ciphertext}</enc>
     *     <content_binding>{rcat_hmac}</content_binding>
     *   </to>
     *   ...
     * </participants>
     * }</pre>
     */
    private static Node buildParticipantsNodeWithEnc(
            List<MessageDeviceEncryption> deviceEncryptions,
            MessageContainer message,
            MessageSendInput.Chat options
    ) {
        var participantBindings = options.participantContentBindings().orElse(null);

        var toNodes = deviceEncryptions.stream()
                .map(enc -> {
                    var toChildren = new ArrayList<Node>();

                    // Add enc node
                    toChildren.add(buildEncNode(enc.payload(), message, options));

                    // Add per-participant content_binding if available
                    if (participantBindings != null) {
                        var binding = participantBindings.get(enc.deviceJid().toString());
                        if (binding != null) {
                            var contentBindingNode = new NodeBuilder()
                                    .description("content_binding")
                                    .content(binding)
                                    .build();
                            toChildren.add(contentBindingNode);
                        }
                    }

                    return new NodeBuilder()
                            .description("to")
                            .attribute("jid", enc.deviceJid())
                            .content(toChildren)
                            .build();
                })
                .toList();

        return new NodeBuilder()
                .description("participants")
                .content(toNodes)
                .build();
    }

    /**
     * Builds an enc node without a jid attribute (default/first enc).
     */
    private static Node buildEncNode(MessageEncryptedPayload payload, MessageContainer message, MessageSendInput.Chat options) {
        var builder = new NodeBuilder()
                .description("enc")
                .attribute("v", ENC_VERSION)
                .attribute("type", payload.type().protocolValue())
                .content(payload.ciphertext().toSerialized());

        addEncNodeAttributes(builder, message, options);
        return builder.build();
    }

    /**
     * Adds common attributes to enc nodes.
     */
    private static void addEncNodeAttributes(NodeBuilder builder, MessageContainer message, MessageSendInput.Chat options) {
        // Add media type
        addMediaTypeAttribute(builder, message);

        // Add decrypt-fail behavior
        addDecryptFailAttribute(builder, message, options);

        // Add native flow name for interactive messages
        options.nativeFlowName().ifPresent(flowName ->
                builder.attribute("native_flow_name", flowName));

        // Add count attribute for messages with add-on encryption
        if (options.generateMessageSecret() || options.messageSecret().isPresent()) {
            builder.attribute("count", 0);
        }
    }

    /**
     * Adds mediatype attribute to enc node if the message contains media.
     */
    private static void addMediaTypeAttribute(NodeBuilder builder, MessageContainer message) {
        if (message == null) {
            return;
        }

        String mediaType = switch (message.content()) {
            case ImageMessage _ -> "1";
            case AudioMessage audio -> audio.voiceMessage() ? "2" : "5";
            case VideoOrGifMessage _ -> "3";
            case DocumentMessage _ -> "4";
            case StickerMessage _ -> "6";
            case null, default -> null;
        };

        if (mediaType != null) {
            builder.attribute("mediatype", mediaType);
        }
    }

    /**
     * Adds decrypt-fail attribute to enc node based on message type.
     */
    private static void addDecryptFailAttribute(NodeBuilder builder, MessageContainer message, MessageSendInput.Chat options) {
        if (message == null) {
            return;
        }

        // View-once messages: hide on decrypt fail
        if (options instanceof MessageSendInput.Chat.ViewOnce || message.type() == Message.Type.VIEW_ONCE) {
            builder.attribute("decrypt-fail", "hide");
            return;
        }

        // Messages without messageSecret: show placeholder on decrypt fail
        var hasMessageSecret = message.deviceInfo()
                .flatMap(DeviceContextInfo::messageSecret)
                .isPresent() || options.messageSecret().isPresent();

        if (!hasMessageSecret) {
            builder.attribute("decrypt-fail", "placeholder");
        }
    }

    /**
     * Builds the device-identity node for prekey messages.
     */
    private static Node buildDeviceIdentityNode(byte[] deviceIdentity) {
        return new NodeBuilder()
                .description("device-identity")
                .content(deviceIdentity)
                .build();
    }

    /**
     * Adds bot node to children if the options are for a bot message.
     * <pre>{@code
     * <bot type="{feedback|prompt|command}"
     *      local_automated_type="{1p_partial|3p_full}"
     *      client_thread_id="{thread_id}"/>
     * }</pre>
     */
    private static void addBotNode(List<Node> children, MessageSendInput.Chat options) {
        if (!(options instanceof MessageSendInput.Chat.Bot bot)) {
            return;
        }

        var builder = new NodeBuilder()
                .description("bot");

        if(bot.hasType()) {
            builder.attribute("type", bot.type().value());
        }

        if(bot.hasAutomatedType()) {
            builder.attribute("local_automated_type", bot.automatedType().value());
        }

        if(bot.hasClientThreadId()) {
            builder.attribute("client_thread_id", bot.clientThreadId());
        }

        children.add(builder.build());
    }

    /**
     * Adds biz node to children if needed.
     * <pre>{@code
     * <biz host_storage="{int}"
     *      actual_actors="{int}"
     *      privacy_mode_ts="{int}"
     *      native_flow_name="{flow_name}"/>
     * }</pre>
     */
    private static void addBizNode(List<Node> children, MessageSendInput.Chat options) {
        var hasAnyBizAttr = options.bizHostStorage().isPresent()
                || options.bizActualActors().isPresent()
                || options.bizPrivacyModeTs().isPresent()
                || options.nativeFlowName().isPresent();

        if (!hasAnyBizAttr) {
            return;
        }

        var builder = new NodeBuilder().description("biz");

        options.bizHostStorage().ifPresent(value ->
                builder.attribute("host_storage", value));

        options.bizActualActors().ifPresent(value ->
                builder.attribute("actual_actors", value));

        options.bizPrivacyModeTs().ifPresent(value ->
                builder.attribute("privacy_mode_ts", value));

        options.nativeFlowName().ifPresent(flowName ->
                builder.attribute("native_flow_name", flowName));

        children.add(builder.build());
    }

    /**
     * Adds reporting_token node to children if needed.
     * <pre>{@code
     * <reporting_token>{token_data}</reporting_token>
     * }</pre>
     */
    private static void addReportingTokenNode(List<Node> children, MessageSendInput.Chat options) {
        if (!options.includeReportingToken()) {
            return;
        }

        var builder = new NodeBuilder().description("reporting_token");

        options.reportingTokenData().ifPresent(builder::content);

        children.add(builder.build());
    }

    /**
     * Adds sender_content_binding node to children if needed.
     * <pre>{@code
     * <sender_content_binding>{rcat_hmac}</sender_content_binding>
     * }</pre>
     */
    private static void addSenderContentBindingNode(List<Node> children, MessageSendInput.Chat options) {
        options.senderContentBinding().ifPresent(binding -> {
            var node = new NodeBuilder()
                    .description("sender_content_binding")
                    .content(binding)
                    .build();
            children.add(node);
        });
    }

    /**
     * Adds tctoken node to children if needed.
     * <pre>{@code
     * <tctoken>{tc_token}</tctoken>
     * }</pre>
     */
    private static void addTcTokenNode(List<Node> children, MessageSendInput.Chat options) {
        options.tcToken().ifPresent(token -> {
            var node = new NodeBuilder()
                    .description("tctoken")
                    .content(token)
                    .build();
            children.add(node);
        });
    }

    /**
     * Adds ctwa_attribution node to children if needed.
     * <pre>{@code
     * <ctwa_attribution>{attribution_data}</ctwa_attribution>
     * }</pre>
     */
    private static void addCtwaAttributionNode(List<Node> children, MessageSendInput.Chat options) {
        options.ctwaAttribution().ifPresent(attribution -> {
            var node = new NodeBuilder()
                    .description("ctwa_attribution")
                    .content(attribution)
                    .build();
            children.add(node);
        });
    }

    /**
     * Adds meta node to children if needed.
     */
    private static void addMetaNode(List<Node> children, MessageContainer message, MessageSendInput.Chat options) {
        var metaNode = buildMetaNode(message, options);
        if (metaNode != null) {
            children.add(metaNode);
        }
    }

    /**
     * Builds the meta node with optional attributes.
     * Returns null if no attributes are needed.
     */
    private static Node buildMetaNode(MessageContainer message, MessageSendInput.Chat options) {
        if (message == null && options == null) {
            return null;
        }

        var builder = new NodeBuilder().description("meta");
        var hasAttribute = false;

        if (message != null) {
            // Poll type
            var pollType = getPollType(message);
            if (pollType != null) {
                builder.attribute("polltype", pollType);
                hasAttribute = true;
            }

            // View-once
            if (message.hasType(Message.Type.VIEW_ONCE)) {
                builder.attribute("view_once", "true");
                hasAttribute = true;
            }
        }

        if (options != null) {
            // View-once from options
            if (options instanceof MessageSendInput.Chat.ViewOnce && !hasAttribute) {
                builder.attribute("view_once", "true");
                hasAttribute = true;
            }

            // Ephemeral duration
            if (options instanceof MessageSendInput.Chat.Ephemeral eph && eph.durationSeconds() > 0) {
                builder.attribute("ephemeral", String.valueOf(eph.durationSeconds()));
                hasAttribute = true;
            }
        }

        return hasAttribute ? builder.build() : null;
    }

    /**
     * Builds a simplified meta node for newsletters.
     */
    private static Node buildNewsletterMetaNode(MessageContainer message) {
        if (message == null) {
            return null;
        }

        var builder = new NodeBuilder().description("meta");
        var hasAttribute = false;

        // Poll type for newsletter polls
        var pollType = getPollType(message);
        if (pollType != null) {
            builder.attribute("polltype", pollType);
            hasAttribute = true;
        }

        return hasAttribute ? builder.build() : null;
    }

    /**
     * Adds addressing mode attributes to the message stanza.
     * Handles all addressing scenarios per protocol specification:
     * - peer_recipient_lid: For PN→LID migration in 1:1 chats
     * - peer_recipient_pn: For LID→PN fallback
     * - peer_recipient_username: If recipient has username enabled
     * - recipient_pn: For LID recipients with phone number sharing
     */
    private static void addAddressingModeAttributes(NodeBuilder builder, MessageAddressingMode addressingMode) {
        if (addressingMode == null) {
            return;
        }

        builder.attribute("addressing_mode", addressingMode.protocolValue());

        switch (addressingMode) {
            case PhoneNumberMessageAddressingMode pn -> {
                // PN mode with optional LID peer recipient for migration
                pn.peerLid().ifPresent(lid ->
                        builder.attribute("peer_recipient_lid", lid));
                pn.username().ifPresent(username ->
                        builder.attribute("peer_recipient_username", username));
            }
            case LidMessageAddressingMode lid -> {
                // LID mode with optional PN attributes
                lid.recipientPn().ifPresent(recipientPn ->
                        builder.attribute("recipient_pn", recipientPn));
                lid.peerPn().ifPresent(peerPn ->
                        builder.attribute("peer_recipient_pn", peerPn));
                lid.username().ifPresent(username ->
                        builder.attribute("peer_recipient_username", username));
            }
            case MixedMessageAddressingMode mixed -> {
                // Mixed mode includes both peer recipients
                builder.attribute("peer_recipient_pn", mixed.phoneJid());
                builder.attribute("peer_recipient_lid", mixed.lidJid());
                mixed.username().ifPresent(username ->
                        builder.attribute("peer_recipient_username", username));
            }
        }
    }

    /**
     * Determines the poll type attribute value.
     */
    private static String getPollType(MessageContainer message) {
        return switch (message.content()) {
            case PollCreationMessage _ -> "creation";
            case PollUpdateMessage _ -> "vote";
            default -> null;
        };
    }

    /**
     * Determines the message type attribute based on content.
     */
    static String determineMessageType(MessageContainer message) {
        if (message == null) {
            return "text";
        }

        return switch (message.content()) {
            case ImageMessage _,
                 VideoOrGifMessage _,
                 AudioMessage _,
                 DocumentMessage _,
                 StickerMessage _ -> "media";

            case PollCreationMessage _,
                 PollUpdateMessage _ -> "poll";

            case ReactionMessage _ -> "reaction";

            case EncryptedReactionMessage _ -> "reaction_enc";

            default -> "text";
        };
    }
}
