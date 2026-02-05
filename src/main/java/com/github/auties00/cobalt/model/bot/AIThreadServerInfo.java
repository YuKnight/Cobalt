package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Server-provided information for an AI thread.
 *
 * @apiNote WAWebProtobufsAICommon.pb.AIThreadInfo$AIThreadServerInfo
 */
@ProtobufMessage(name = "AIThreadInfo$AIThreadServerInfo")
public final class AIThreadServerInfo {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String title;

    AIThreadServerInfo(String title) {
        this.title = title;
    }

    public Optional<String> title() {
        return Optional.ofNullable(title);
    }
}
