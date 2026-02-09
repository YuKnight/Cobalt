package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.info.ContextInfo;
import com.github.auties00.cobalt.model.message.common.ContextualMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A model class that represents an album message containing multiple media items.
 */
@ProtobufMessage(name = "Message.AlbumMessage")
public final class AlbumMessage implements ContextualMessage {
    @ProtobufProperty(index = 2, type = ProtobufType.UINT32)
    final int expectedImageCount;

    @ProtobufProperty(index = 3, type = ProtobufType.UINT32)
    final int expectedVideoCount;

    @ProtobufProperty(index = 17, type = ProtobufType.MESSAGE)
    ContextInfo contextInfo;

    AlbumMessage(int expectedImageCount, int expectedVideoCount, ContextInfo contextInfo) {
        this.expectedImageCount = expectedImageCount;
        this.expectedVideoCount = expectedVideoCount;
        this.contextInfo = contextInfo;
    }

    public int expectedImageCount() {
        return expectedImageCount;
    }

    public int expectedVideoCount() {
        return expectedVideoCount;
    }

    @Override
    public Optional<ContextInfo> contextInfo() {
        return Optional.ofNullable(contextInfo);
    }

    @Override
    public void setContextInfo(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }

    @Override
    public Type type() {
        return Type.ALBUM;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AlbumMessage that
                && expectedImageCount == that.expectedImageCount
                && expectedVideoCount == that.expectedVideoCount
                && Objects.equals(contextInfo, that.contextInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedImageCount, expectedVideoCount, contextInfo);
    }

    @Override
    public String toString() {
        return "AlbumMessage[images=" + expectedImageCount + ", videos=" + expectedVideoCount + ']';
    }
}
