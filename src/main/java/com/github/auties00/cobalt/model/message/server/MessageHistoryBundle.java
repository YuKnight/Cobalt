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
 * A model class that represents a message history bundle for history sync.
 */
@ProtobufMessage(name = "Message.MessageHistoryBundle")
public final class MessageHistoryBundle implements ServerMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String mimetype;

    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    final byte[] fileSha256;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    final byte[] mediaKey;

    @ProtobufProperty(index = 4, type = ProtobufType.BYTES)
    final byte[] fileEncSha256;

    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    final String directPath;

    @ProtobufProperty(index = 6, type = ProtobufType.INT64)
    final long mediaKeyTimestamp;

    @ProtobufProperty(index = 7, type = ProtobufType.MESSAGE)
    final ContextInfo contextInfo;

    MessageHistoryBundle(String mimetype, byte[] fileSha256, byte[] mediaKey, byte[] fileEncSha256, String directPath, long mediaKeyTimestamp, ContextInfo contextInfo) {
        this.mimetype = mimetype;
        this.fileSha256 = fileSha256;
        this.mediaKey = mediaKey;
        this.fileEncSha256 = fileEncSha256;
        this.directPath = directPath;
        this.mediaKeyTimestamp = mediaKeyTimestamp;
        this.contextInfo = contextInfo;
    }

    public Optional<String> mimetype() {
        return Optional.ofNullable(mimetype);
    }

    public Optional<byte[]> fileSha256() {
        return Optional.ofNullable(fileSha256);
    }

    public Optional<byte[]> mediaKey() {
        return Optional.ofNullable(mediaKey);
    }

    public Optional<byte[]> fileEncSha256() {
        return Optional.ofNullable(fileEncSha256);
    }

    public Optional<String> directPath() {
        return Optional.ofNullable(directPath);
    }

    public long mediaKeyTimestamp() {
        return mediaKeyTimestamp;
    }

    @Override
    public Type type() {
        return Type.MESSAGE_HISTORY_BUNDLE;
    }

    @Override
    public Category category() {
        return Category.SERVER;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MessageHistoryBundle that
                && Objects.equals(mimetype, that.mimetype)
                && Objects.equals(directPath, that.directPath)
                && mediaKeyTimestamp == that.mediaKeyTimestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mimetype, directPath, mediaKeyTimestamp);
    }

    @Override
    public String toString() {
        return "MessageHistoryBundle[mimetype=" + mimetype + ']';
    }
}
