package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.Message;
import com.github.auties00.cobalt.model.message.common.ServerMessage;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A message that pins or unpins another message in a chat.
 */
@ProtobufMessage(name = "Message.PinInChatMessage")
public final class PinInChatMessage implements ServerMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey key;

    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final Type pinType;

    @ProtobufProperty(index = 3, type = ProtobufType.INT64)
    final long senderTimestampMs;

    PinInChatMessage(ChatMessageKey key, Type pinType, long senderTimestampMs) {
        this.key = key;
        this.pinType = pinType;
        this.senderTimestampMs = senderTimestampMs;
    }

    public Optional<ChatMessageKey> key() {
        return Optional.ofNullable(key);
    }

    public Type pinType() {
        return pinType;
    }

    public long senderTimestampMs() {
        return senderTimestampMs;
    }

    @Override
    public Message.Type type() {
        return Message.Type.PIN_IN_CHAT;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PinInChatMessage that
                && Objects.equals(key, that.key)
                && pinType == that.pinType
                && senderTimestampMs == that.senderTimestampMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, pinType, senderTimestampMs);
    }

    @Override
    public String toString() {
        return "PinInChatMessage[" +
                "key=" + key +
                ", pinType=" + pinType +
                ", senderTimestampMs=" + senderTimestampMs +
                ']';
    }

    @ProtobufEnum(name = "Message.PinInChatMessage.Type")
    public enum Type {
        UNKNOWN_TYPE(0),
        PIN_FOR_ALL(1),
        UNPIN_FOR_ALL(2);

        final int index;

        Type(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
