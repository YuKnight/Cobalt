package com.github.auties00.cobalt.model.message.server;

import com.github.auties00.cobalt.model.info.ContextInfo;
import com.github.auties00.cobalt.model.message.common.Message;
import com.github.auties00.cobalt.model.message.common.ServerMessage;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A model class that represents a message history notice.
 */
@ProtobufMessage(name = "Message.MessageHistoryNotice")
public final class MessageHistoryNotice implements ServerMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ContextInfo contextInfo;

    MessageHistoryNotice(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }

    public Optional<ContextInfo> contextInfo() {
        return Optional.ofNullable(contextInfo);
    }

    @Override
    public Type type() {
        return Type.MESSAGE_HISTORY_NOTICE;
    }

    @Override
    public Category category() {
        return Category.SERVER;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MessageHistoryNotice that
                && Objects.equals(contextInfo, that.contextInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextInfo);
    }

    @Override
    public String toString() {
        return "MessageHistoryNotice[]";
    }
}
