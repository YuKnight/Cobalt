package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.message.send.bot.BotMessageSecret;
import com.github.auties00.cobalt.message.send.bot.BotProtobufTransform;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryptedPayload;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryption;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.ChatMessageContextInfo;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainerSpec;
import com.github.auties00.cobalt.model.message.MessageKey;
import com.github.auties00.cobalt.model.message.system.ProtocolMessage;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.security.GeneralSecurityException;
import java.util.Objects;

/**
 * Builds the {@code <bot>} stanza child node for bot messages.
 *
 * <p>Bot messages are encrypted separately to the bot's device and included in the
 * stanza as a {@code <bot>} node alongside the regular {@code <enc>} or
 * {@code <participants>} nodes. The metadata-only variant ({@link #buildMetadata}) carries
 * stanza-level routing attributes without an encrypted payload.
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgCreateFanoutStanza")
@WhatsAppWebModule(moduleName = "WAWebSendGroupSkmsgJob")
public final class BotStanza {
    /**
     * Logger for bot encryption failure diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(BotStanza.class.getName());

    /**
     * Encryption service used to encrypt the bot body to the bot device.
     */
    private final MessageEncryption encryption;

    /**
     * Bot protobuf transform service applied before encryption.
     */
    private final BotProtobufTransform protobufTransform;

    /**
     * Creates a new bot stanza builder.
     *
     * @param encryption        the encryption service
     * @param protobufTransform the bot protobuf transform service
     * @throws NullPointerException if any argument is {@code null}
     */
    public BotStanza(MessageEncryption encryption, BotProtobufTransform protobufTransform) {
        this.encryption = Objects.requireNonNull(encryption, "encryption");
        this.protobufTransform = Objects.requireNonNull(protobufTransform, "protobufTransform");
    }

    /**
     * Builds the {@code <bot>} node for the given message, or returns {@code null} if no
     * bot is involved.
     *
     * <p>Derives the bot JID, feedback flag and message secret from the message info and
     * chat JID, applies bot protobuf transforms, and encrypts the result to the bot
     * device.
     *
     * @param messageInfo the outgoing message
     * @param chatJid     the target chat JID
     * @return the bot node, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node build(ChatMessageInfo messageInfo, Jid chatJid) {
        var botJid = resolveBotJid(messageInfo, chatJid);
        if (botJid == null) {
            return null;
        }

        var isFeedback = isBotFeedback(messageInfo);
        var container = messageInfo.message();

        var messageSecret = container.messageContextInfo()
                .flatMap(ChatMessageContextInfo::messageSecret)
                .orElse(null);
        byte[] botSecret = null;
        if (messageSecret != null) {
            try {
                botSecret = BotMessageSecret.derive(messageSecret);
            } catch (GeneralSecurityException e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Failed to derive bot message secret: {0}", e.getMessage());
            }
        }

        protobufTransform.transformForCapi(container, botSecret);
        if (isFbidBot(botJid)) {
            protobufTransform.transformForFbidBot(container);
        }
        protobufTransform.transformForBot(container);

        var plaintext = MessageContainerSpec.encode(container);
        MessageEncryptedPayload payload;
        try {
            payload = encryption.encryptForDevice(botJid, plaintext);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Bot encryption failed for {0}: {1}", botJid, e.getMessage());
            return null;
        }

        var encNode = new NodeBuilder()
                .description("enc")
                .attribute("v", String.valueOf(MessageEncryption.CIPHERTEXT_VERSION))
                .attribute("type", payload.type().protocolValue())
                .content(payload.ciphertext())
                .build();
        var toNode = new NodeBuilder()
                .description("to")
                .attribute("jid", botJid)
                .content(encNode)
                .build();
        return new NodeBuilder()
                .description("bot")
                .attribute("type", isFeedback ? "feedback" : null)
                .content(toNode)
                .build();
    }

    /**
     * Builds the metadata-only {@code <bot>} node that carries bot invocation type,
     * business bot classification, AI thread ID, and AI mode selection attributes.
     *
     * <p>This node is separate from the encrypted bot body built by
     * {@link #build(ChatMessageInfo, Jid)}. It carries stanza-level metadata that the
     * server uses for routing and analytics. Returns {@code null} when no attribute is
     * set.
     *
     * @param botMsgBodyType the bot message body type
     *                       ({@code "prompt"}, {@code "command"}, {@code "request_welcome"}),
     *                       or {@code null}
     * @param bizBotType     the business bot type
     *                       ({@code "1p_partial"}, {@code "3p_full"}), or {@code null}
     * @param clientThreadId the AI thread ID, or {@code null}
     * @param modeSelection  the user's AI mode selection
     *                       ({@code "default"}, {@code "think_hard"}), or {@code null}
     * @param modeSelected   the dynamic mode override string, or {@code null}
     * @return the bot metadata node, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildMetadata(
            String botMsgBodyType,
            String bizBotType,
            String clientThreadId,
            String modeSelection,
            String modeSelected
    ) {
        if (botMsgBodyType == null && bizBotType == null && clientThreadId == null
                && modeSelection == null && modeSelected == null) {
            return null;
        }

        return new NodeBuilder()
                .description("bot")
                .attribute("type", botMsgBodyType)
                .attribute("local_automated_type", bizBotType)
                .attribute("client_thread_id", clientThreadId)
                .attribute("mode_selection", modeSelection)
                .attribute("mode_selected", modeSelected)
                .build();
    }

    /**
     * Builds the metadata-only {@code <bot>} node without AI mode selection attributes.
     *
     * <p>Convenience overload that delegates to
     * {@link #buildMetadata(String, String, String, String, String)} with {@code null}
     * for {@code modeSelection} and {@code modeSelected}.
     *
     * @param botMsgBodyType the bot message body type, or {@code null}
     * @param bizBotType     the business bot type, or {@code null}
     * @param clientThreadId the AI thread ID, or {@code null}
     * @return the bot metadata node, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildMetadata(
            String botMsgBodyType,
            String bizBotType,
            String clientThreadId
    ) {
        return buildMetadata(botMsgBodyType, bizBotType, clientThreadId, null, null);
    }

    /**
     * Builds the encrypted bot node for group messages that target the open Meta AI bot.
     *
     * @param messageInfo    the outgoing message
     * @param isOpenBotGroup whether the group has the open bot enabled
     * @return the bot node, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendGroupSkmsgJob", exports = "encryptAndSendSenderKeyMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildForGroup(ChatMessageInfo messageInfo, boolean isOpenBotGroup) {
        if (!isOpenBotGroup) {
            return null;
        }

        var botJid = Jid.metaAiBotAccount();
        var container = messageInfo.message();

        var messageSecret = container.messageContextInfo()
                .flatMap(ChatMessageContextInfo::messageSecret)
                .orElse(null);
        byte[] botSecret = null;
        if (messageSecret != null) {
            try {
                botSecret = BotMessageSecret.derive(messageSecret);
            } catch (GeneralSecurityException e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Failed to derive bot message secret for open group bot: {0}", e.getMessage());
            }
        }

        protobufTransform.transformForCapi(container, botSecret);
        protobufTransform.transformForBot(container);

        var plaintext = MessageContainerSpec.encode(container);
        MessageEncryptedPayload payload;
        try {
            payload = encryption.encryptForDevice(botJid, plaintext);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Open group bot encryption failed: {0}", e.getMessage());
            return null;
        }

        var encNode = new NodeBuilder()
                .description("enc")
                .attribute("v", String.valueOf(MessageEncryption.CIPHERTEXT_VERSION))
                .attribute("type", payload.type().protocolValue())
                .content(payload.ciphertext())
                .build();
        var toNode = new NodeBuilder()
                .description("to")
                .attribute("jid", botJid)
                .content(encNode)
                .build();
        return new NodeBuilder()
                .description("bot")
                .content(toNode)
                .build();
    }

    /**
     * Resolves the bot device JID from the message and chat context.
     *
     * <p>For 1:1 bot chats the chat JID itself is the bot. For bot feedback messages
     * the bot is the sender of the original protocol message.
     *
     * @param messageInfo the outgoing message
     * @param chatJid     the chat JID
     * @return the bot device JID, or {@code null} if no bot is involved
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static Jid resolveBotJid(ChatMessageInfo messageInfo, Jid chatJid) {
        if (chatJid.hasBotServer()) {
            return chatJid;
        }

        if (isBotFeedback(messageInfo)
                && messageInfo.message().content() instanceof ProtocolMessage pm) {
            return pm.key()
                    .flatMap(MessageKey::senderJid)
                    .filter(Jid::hasBotServer)
                    .orElse(null);
        }

        return null;
    }

    /**
     * Returns whether the message is a bot feedback protocol message.
     *
     * @param messageInfo the message to check
     * @return {@code true} if the message is a bot feedback protocol message
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgGetters", exports = "getIsBotFeedbackMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isBotFeedback(ChatMessageInfo messageInfo) {
        return messageInfo.message().content() instanceof ProtocolMessage pm
                && pm.type().orElse(null) == ProtocolMessage.Type.BOT_FEEDBACK_MESSAGE;
    }

    /**
     * Returns whether the JID is an FBID bot, that is a numeric user on the bot server.
     *
     * @param jid the JID to check
     * @return {@code true} if the JID is an FBID bot
     */
    @WhatsAppWebExport(moduleName = "WAWebWid", exports = "isFbidBot",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isFbidBot(Jid jid) {
        if (!jid.hasBotServer()) {
            return false;
        }
        var user = jid.user();
        return user != null && !user.isEmpty()
                && user.chars().allMatch(Character::isDigit);
    }
}
