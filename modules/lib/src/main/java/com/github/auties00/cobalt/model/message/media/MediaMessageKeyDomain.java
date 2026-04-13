package com.github.auties00.cobalt.model.message.media;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

@ProtobufEnum(name = "Message.MediaKeyDomain")
public enum MediaMessageKeyDomain {
    UNSET(0),
    E2EE_CHAT(1),
    STATUS(2),
    CAPI(3),
    BOT(4);

    MediaMessageKeyDomain(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    final int index;

    public int index() {
        return this.index;
    }
}
