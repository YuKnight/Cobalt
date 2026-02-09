package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;

/**
 * A model class that represents a placeholder message.
 */
@ProtobufMessage(name = "Message.PlaceholderMessage")
public final class PlaceholderMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    final PlaceholderType placeholderType;

    PlaceholderMessage(PlaceholderType placeholderType) {
        this.placeholderType = placeholderType;
    }

    public PlaceholderType placeholderType() {
        return placeholderType != null ? placeholderType : PlaceholderType.MASK_LINKED_DEVICES;
    }

    @Override
    public Type type() {
        return Type.PLACEHOLDER;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PlaceholderMessage that
                && placeholderType == that.placeholderType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeholderType);
    }

    @Override
    public String toString() {
        return "PlaceholderMessage[type=" + placeholderType + ']';
    }

    @ProtobufEnum(name = "Message.PlaceholderMessage.PlaceholderType")
    public enum PlaceholderType {
        MASK_LINKED_DEVICES(0);

        final int index;

        PlaceholderType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
