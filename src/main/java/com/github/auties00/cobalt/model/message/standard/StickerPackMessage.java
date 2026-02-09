package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.info.ContextInfo;
import com.github.auties00.cobalt.model.message.common.ContextualMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A model class that represents a sticker pack message.
 */
@ProtobufMessage(name = "Message.StickerPackMessage")
public final class StickerPackMessage implements ContextualMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String stickerPackId;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String name;

    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String publisher;

    @ProtobufProperty(index = 4, type = ProtobufType.MESSAGE)
    final List<Sticker> stickers;

    @ProtobufProperty(index = 5, type = ProtobufType.UINT64)
    final long fileLength;

    @ProtobufProperty(index = 6, type = ProtobufType.BYTES)
    final byte[] fileSha256;

    @ProtobufProperty(index = 7, type = ProtobufType.BYTES)
    final byte[] fileEncSha256;

    @ProtobufProperty(index = 8, type = ProtobufType.BYTES)
    final byte[] mediaKey;

    @ProtobufProperty(index = 9, type = ProtobufType.STRING)
    final String directPath;

    @ProtobufProperty(index = 10, type = ProtobufType.STRING)
    final String caption;

    @ProtobufProperty(index = 11, type = ProtobufType.MESSAGE)
    ContextInfo contextInfo;

    @ProtobufProperty(index = 12, type = ProtobufType.STRING)
    final String packDescription;

    @ProtobufProperty(index = 13, type = ProtobufType.INT64)
    final long mediaKeyTimestamp;

    @ProtobufProperty(index = 22, type = ProtobufType.ENUM)
    final StickerPackOrigin stickerPackOrigin;

    StickerPackMessage(String stickerPackId, String name, String publisher, List<Sticker> stickers, long fileLength, byte[] fileSha256, byte[] fileEncSha256, byte[] mediaKey, String directPath, String caption, ContextInfo contextInfo, String packDescription, long mediaKeyTimestamp, StickerPackOrigin stickerPackOrigin) {
        this.stickerPackId = stickerPackId;
        this.name = name;
        this.publisher = publisher;
        this.stickers = stickers;
        this.fileLength = fileLength;
        this.fileSha256 = fileSha256;
        this.fileEncSha256 = fileEncSha256;
        this.mediaKey = mediaKey;
        this.directPath = directPath;
        this.caption = caption;
        this.contextInfo = contextInfo;
        this.packDescription = packDescription;
        this.mediaKeyTimestamp = mediaKeyTimestamp;
        this.stickerPackOrigin = stickerPackOrigin;
    }

    public Optional<String> stickerPackId() {
        return Optional.ofNullable(stickerPackId);
    }

    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    public Optional<String> publisher() {
        return Optional.ofNullable(publisher);
    }

    public List<Sticker> stickers() {
        return stickers;
    }

    public long fileLength() {
        return fileLength;
    }

    public Optional<String> directPath() {
        return Optional.ofNullable(directPath);
    }

    public Optional<String> caption() {
        return Optional.ofNullable(caption);
    }

    @Override
    public Optional<ContextInfo> contextInfo() {
        return Optional.ofNullable(contextInfo);
    }

    @Override
    public void setContextInfo(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }

    public Optional<String> packDescription() {
        return Optional.ofNullable(packDescription);
    }

    public long mediaKeyTimestamp() {
        return mediaKeyTimestamp;
    }

    public StickerPackOrigin stickerPackOrigin() {
        return stickerPackOrigin != null ? stickerPackOrigin : StickerPackOrigin.FIRST_PARTY;
    }

    @Override
    public Type type() {
        return Type.STICKER_PACK;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StickerPackMessage that
                && Objects.equals(stickerPackId, that.stickerPackId)
                && Objects.equals(name, that.name)
                && Objects.equals(publisher, that.publisher)
                && Objects.equals(stickers, that.stickers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stickerPackId, name, publisher, stickers);
    }

    @Override
    public String toString() {
        return "StickerPackMessage[id=" + stickerPackId + ", name=" + name + ']';
    }

    @ProtobufMessage(name = "Message.StickerPackMessage.Sticker")
    public record Sticker(
            @ProtobufProperty(index = 1, type = ProtobufType.STRING) String fileName,
            @ProtobufProperty(index = 2, type = ProtobufType.BOOL) boolean isAnimated,
            @ProtobufProperty(index = 3, type = ProtobufType.STRING) List<String> emojis,
            @ProtobufProperty(index = 4, type = ProtobufType.STRING) String accessibilityLabel,
            @ProtobufProperty(index = 5, type = ProtobufType.BOOL) boolean isLottie,
            @ProtobufProperty(index = 6, type = ProtobufType.STRING) String mimetype
    ) {
    }

    @ProtobufEnum(name = "Message.StickerPackMessage.StickerPackOrigin")
    public enum StickerPackOrigin {
        FIRST_PARTY(0),
        THIRD_PARTY(1),
        USER_CREATED(2);

        final int index;

        StickerPackOrigin(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
