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
 * A model class that represents a status sticker interaction message.
 */
@ProtobufMessage(name = "Message.StatusStickerInteractionMessage")
public final class StatusStickerInteractionMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey key;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String stickerKey;

    @ProtobufProperty(index = 3, type = ProtobufType.ENUM)
    final StatusStickerType stickerType;

    StatusStickerInteractionMessage(ChatMessageKey key, String stickerKey, StatusStickerType stickerType) {
        this.key = key;
        this.stickerKey = stickerKey;
        this.stickerType = stickerType;
    }

    public Optional<ChatMessageKey> key() {
        return Optional.ofNullable(key);
    }

    public Optional<String> stickerKey() {
        return Optional.ofNullable(stickerKey);
    }

    public StatusStickerType stickerType() {
        return stickerType != null ? stickerType : StatusStickerType.UNKNOWN;
    }

    @Override
    public Type type() {
        return Type.STATUS_STICKER_INTERACTION;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StatusStickerInteractionMessage that
                && Objects.equals(key, that.key)
                && Objects.equals(stickerKey, that.stickerKey)
                && stickerType == that.stickerType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, stickerKey, stickerType);
    }

    @Override
    public String toString() {
        return "StatusStickerInteractionMessage[type=" + stickerType + ']';
    }

    @ProtobufEnum(name = "Message.StatusStickerInteractionMessage.StatusStickerType")
    public enum StatusStickerType {
        UNKNOWN(0),
        REACTION(1);

        final int index;

        StatusStickerType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
