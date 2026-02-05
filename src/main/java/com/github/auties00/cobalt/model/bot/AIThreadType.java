package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

/**
 * AI thread types for bot conversations.
 *
 * @apiNote WAWebProtobufsAICommon.pb.AIThreadInfo$AIThreadClientInfo$AIThreadType
 */
@ProtobufEnum
public enum AIThreadType {
    UNKNOWN(0),
    DEFAULT(1),
    INCOGNITO(2);

    final int index;

    AIThreadType(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
