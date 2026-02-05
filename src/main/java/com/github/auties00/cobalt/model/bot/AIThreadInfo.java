package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * AI thread information for bot conversations.
 * <p>
 * Contains both server-provided and client-provided thread metadata
 * for AI-driven conversation threads.
 *
 * @apiNote WAWebProtobufsAICommon.pb.AIThreadInfo
 */
@ProtobufMessage(name = "AIThreadInfo")
public final class AIThreadInfo {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final AIThreadServerInfo serverInfo;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    final AIThreadClientInfo clientInfo;

    AIThreadInfo(AIThreadServerInfo serverInfo, AIThreadClientInfo clientInfo) {
        this.serverInfo = serverInfo;
        this.clientInfo = clientInfo;
    }

    public Optional<AIThreadServerInfo> serverInfo() {
        return Optional.ofNullable(serverInfo);
    }

    public Optional<AIThreadClientInfo> clientInfo() {
        return Optional.ofNullable(clientInfo);
    }
}
