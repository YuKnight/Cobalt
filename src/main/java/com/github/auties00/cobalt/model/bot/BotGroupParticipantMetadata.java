package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Metadata about a bot participant in a group.
 *
 * @apiNote WAWebProtobufsAICommon.pb.BotGroupParticipantMetadata
 */
@ProtobufMessage(name = "BotGroupParticipantMetadata")
public final class BotGroupParticipantMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String botFbid;

    BotGroupParticipantMetadata(String botFbid) {
        this.botFbid = botFbid;
    }

    public Optional<String> botFbid() {
        return Optional.ofNullable(botFbid);
    }
}
