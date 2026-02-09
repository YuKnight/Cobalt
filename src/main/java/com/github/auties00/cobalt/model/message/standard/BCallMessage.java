package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A model class that represents a business call message.
 */
@ProtobufMessage(name = "Message.BCallMessage")
public final class BCallMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String sessionId;

    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final MediaType mediaType;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    final byte[] masterKey;

    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String caption;

    BCallMessage(String sessionId, MediaType mediaType, byte[] masterKey, String caption) {
        this.sessionId = sessionId;
        this.mediaType = mediaType;
        this.masterKey = masterKey;
        this.caption = caption;
    }

    public Optional<String> sessionId() {
        return Optional.ofNullable(sessionId);
    }

    public MediaType mediaType() {
        return mediaType != null ? mediaType : MediaType.UNKNOWN;
    }

    public Optional<byte[]> masterKey() {
        return Optional.ofNullable(masterKey);
    }

    public Optional<String> caption() {
        return Optional.ofNullable(caption);
    }

    @Override
    public Type type() {
        return Type.BCALL;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BCallMessage that
                && Objects.equals(sessionId, that.sessionId)
                && mediaType == that.mediaType
                && Objects.equals(caption, that.caption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, mediaType, caption);
    }

    @Override
    public String toString() {
        return "BCallMessage[sessionId=" + sessionId + ", mediaType=" + mediaType + ']';
    }

    @ProtobufEnum(name = "Message.BCallMessage.MediaType")
    public enum MediaType {
        UNKNOWN(0),
        AUDIO(1),
        VIDEO(2);

        final int index;

        MediaType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
