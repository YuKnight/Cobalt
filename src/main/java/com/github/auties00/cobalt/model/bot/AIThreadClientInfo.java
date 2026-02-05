package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Client-provided information for an AI thread.
 *
 * @apiNote WAWebProtobufsAICommon.pb.AIThreadInfo$AIThreadClientInfo
 */
@ProtobufMessage(name = "AIThreadInfo$AIThreadClientInfo")
public final class AIThreadClientInfo {
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    final AIThreadType type;

    AIThreadClientInfo(AIThreadType type) {
        this.type = type;
    }

    public Optional<AIThreadType> type() {
        return Optional.ofNullable(type);
    }
}
