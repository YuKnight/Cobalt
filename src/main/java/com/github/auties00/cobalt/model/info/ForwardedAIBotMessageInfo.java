package com.github.auties00.cobalt.model.info;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Information about a forwarded AI bot message.
 * <p>
 * Per WhatsApp Web ContextInfo.forwardedAiBotMessageInfo: tracks the origin
 * of a forwarded AI bot message including the bot name, JID, and creator.
 *
 * @apiNote WAWebProtobufsE2E.pb.ContextInfo.ForwardedAIBotMessageInfo
 */
@ProtobufMessage(name = "ContextInfo.ForwardedAIBotMessageInfo")
public final class ForwardedAIBotMessageInfo implements Info {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String botName;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String botJid;

    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String creatorName;

    ForwardedAIBotMessageInfo(String botName, String botJid, String creatorName) {
        this.botName = botName;
        this.botJid = botJid;
        this.creatorName = creatorName;
    }

    public Optional<String> botName() {
        return Optional.ofNullable(botName);
    }

    public Optional<String> botJid() {
        return Optional.ofNullable(botJid);
    }

    public Optional<String> creatorName() {
        return Optional.ofNullable(creatorName);
    }
}
