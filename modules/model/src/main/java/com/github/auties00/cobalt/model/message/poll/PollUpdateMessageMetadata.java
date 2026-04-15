package com.github.auties00.cobalt.model.message.poll;

import com.github.auties00.cobalt.model.message.Message;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

@ProtobufMessage(name = "Message.PollUpdateMessageMetadata")
public final class PollUpdateMessageMetadata implements Message {

    PollUpdateMessageMetadata() {
    }
}
