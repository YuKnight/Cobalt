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
 * A model class that represents a status quoted message.
 */
@ProtobufMessage(name = "Message.StatusQuotedMessage")
public final class StatusQuotedMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    final StatusQuotedMessageType quotedType;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String text;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    final byte[] thumbnail;

    @ProtobufProperty(index = 4, type = ProtobufType.MESSAGE)
    final ChatMessageKey originalStatusId;

    StatusQuotedMessage(StatusQuotedMessageType quotedType, String text, byte[] thumbnail, ChatMessageKey originalStatusId) {
        this.quotedType = quotedType;
        this.text = text;
        this.thumbnail = thumbnail;
        this.originalStatusId = originalStatusId;
    }

    public StatusQuotedMessageType quotedType() {
        return quotedType != null ? quotedType : StatusQuotedMessageType.QUESTION_ANSWER;
    }

    public Optional<String> text() {
        return Optional.ofNullable(text);
    }

    public Optional<byte[]> thumbnail() {
        return Optional.ofNullable(thumbnail);
    }

    public Optional<ChatMessageKey> originalStatusId() {
        return Optional.ofNullable(originalStatusId);
    }

    @Override
    public Type type() {
        return Type.STATUS_QUOTED;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StatusQuotedMessage that
                && quotedType == that.quotedType
                && Objects.equals(text, that.text)
                && Objects.equals(originalStatusId, that.originalStatusId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quotedType, text, originalStatusId);
    }

    @Override
    public String toString() {
        return "StatusQuotedMessage[type=" + quotedType + ", text=" + text + ']';
    }

    @ProtobufEnum(name = "Message.StatusQuotedMessage.StatusQuotedMessageType")
    public enum StatusQuotedMessageType {
        QUESTION_ANSWER(1);

        final int index;

        StatusQuotedMessageType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
