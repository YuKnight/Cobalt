package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A model class that represents a scheduled call edit message.
 */
@ProtobufMessage(name = "Message.ScheduledCallEditMessage")
public final class ScheduledCallEditMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey key;

    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final EditType editType;

    ScheduledCallEditMessage(ChatMessageKey key, EditType editType) {
        this.key = key;
        this.editType = editType;
    }

    public Optional<ChatMessageKey> key() {
        return Optional.ofNullable(key);
    }

    public EditType editType() {
        return editType != null ? editType : EditType.UNKNOWN;
    }

    @Override
    public Type type() {
        return Type.SCHEDULED_CALL_EDIT;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ScheduledCallEditMessage that
                && Objects.equals(key, that.key)
                && editType == that.editType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, editType);
    }

    @Override
    public String toString() {
        return "ScheduledCallEditMessage[editType=" + editType + ']';
    }

    @ProtobufEnum(name = "Message.ScheduledCallEditMessage.EditType")
    public enum EditType {
        UNKNOWN(0),
        CANCEL(1);

        final int index;

        EditType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
