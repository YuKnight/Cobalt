package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;

/**
 * Metadata about bot participants in a group context.
 * <p>
 * Generated when sending messages in open bot groups to include
 * information about bot participants.
 *
 * @apiNote WAWebProtobufsAICommon.pb.BotGroupMetadata
 */
@ProtobufMessage(name = "BotGroupMetadata")
public final class BotGroupMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final List<BotGroupParticipantMetadata> participantsMetadata;

    BotGroupMetadata(List<BotGroupParticipantMetadata> participantsMetadata) {
        this.participantsMetadata = participantsMetadata;
    }

    public List<BotGroupParticipantMetadata> participantsMetadata() {
        return participantsMetadata != null ? participantsMetadata : List.of();
    }
}
