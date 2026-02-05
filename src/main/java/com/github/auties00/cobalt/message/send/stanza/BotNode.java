package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.message.send.bot.BusinessBotType;
import com.github.auties00.cobalt.message.encryption.MessageEncryption;
import com.github.auties00.cobalt.message.send.bot.BotMessageType;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.MessageContainer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.Objects;

/**
 * Builds bot nodes for message stanzas.
 * <p>
 * Bot nodes contain encrypted content for bot recipients in group messages.
 *
 * @apiNote WAWebSendGroupSkmsgJob function L
 */
public final class BotNode {
    private static final System.Logger LOGGER = System.getLogger("BotNode");

    private final MessageEncryption encryption;

    public BotNode(MessageEncryption encryption) {
        this.encryption = Objects.requireNonNull(encryption, "encryption cannot be null");
    }

    /**
     * Builds a bot node with encrypted content.
     *
     * @param botType the bot message type
     * @param botJid  the bot JID
     * @param message the message to encrypt for the bot
     * @return the result containing the node and whether identity is needed, or null on failure
     *
     * @apiNote WAWebSendGroupSkmsgJob function L
     */
    public Result build(BotMessageType botType, Jid botJid, MessageContainer message) {
        try {
            var payload = encryption.encryptForDevice(botJid, message);
            var node = buildWithParticipant(botType, botJid, payload.type().protocolValue(), payload.ciphertext());
            return new Result(node, payload.isPreKeyMessage());
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to encrypt bot message for {0}: {1}", botJid, e.getMessage());
            return null;
        }
    }

    /**
     * Builds a simple bot node without encryption (for attribute-only cases).
     *
     * @param botType           the bot message type
     * @param businessBotType        the business bot type, or null
     * @param clientThreadId    the client thread ID, or null
     * @param fbidBotPersonaType the FBID bot persona type, or null
     * @return the bot node
     *
     * @apiNote WAWebSendMsgCreateFanoutStanza.createFanoutMsgStanza
     */
    public static Node build(
            BotMessageType botType,
            BusinessBotType businessBotType,
            String clientThreadId,
            String fbidBotPersonaType
    ) {
        if (botType == null) {
            return null;
        }

        return new NodeBuilder()
                .description("bot")
                .attribute("type", botType.value())
                .attribute("local_automated_type", businessBotType != null ? String.valueOf(businessBotType.value()) : null)
                .attribute("client_thread_id", clientThreadId)
                .attribute("persona_type", fbidBotPersonaType)
                .build();
    }

    private Node buildWithParticipant(BotMessageType botType, Jid botJid, String encType, byte[] ciphertext) {
        var encNode = new NodeBuilder()
                .description("enc")
                .attribute("v", "2")
                .attribute("type", encType)
                .content(ciphertext)
                .build();

        var toNode = new NodeBuilder()
                .description("to")
                .attribute("jid", botJid)
                .content(encNode)
                .build();

        return new NodeBuilder()
                .description("bot")
                .attribute("type", botType.value())
                .content(toNode)
                .build();
    }

    /**
     * Result of building a bot node with encryption.
     *
     * @param node          the bot node
     * @param needsIdentity whether the encryption requires an identity node
     */
    public record Result(Node node, boolean needsIdentity) {}
}
