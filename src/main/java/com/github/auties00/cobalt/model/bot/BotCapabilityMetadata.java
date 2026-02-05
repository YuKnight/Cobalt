package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;

/**
 * Metadata about bot capabilities supported by the client.
 * <p>
 * Advertises to the server which rich response features this client supports.
 * Without this metadata, the server will not send rich/structured responses.
 *
 * @apiNote WAWebProtobufsAICommon.pb.BotCapabilityMetadata
 */
@ProtobufMessage(name = "BotCapabilityMetadata")
public final class BotCapabilityMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    final List<BotCapabilityType> capabilities;

    BotCapabilityMetadata(List<BotCapabilityType> capabilities) {
        this.capabilities = capabilities;
    }

    public List<BotCapabilityType> capabilities() {
        return capabilities != null ? capabilities : List.of();
    }
}
