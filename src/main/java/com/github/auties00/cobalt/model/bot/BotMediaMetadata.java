package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Metadata about media in bot messages.
 *
 * @apiNote WAWebProtobufsE2E.pb.BotMediaMetadata
 */
@ProtobufMessage(name = "BotMediaMetadata")
public final class BotMediaMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String fileSha256;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String mediaKey;

    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String fileEncSha256;

    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String directPath;

    @ProtobufProperty(index = 5, type = ProtobufType.INT64)
    final Long mediaKeyTimestamp;

    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    final String mimetype;

    @ProtobufProperty(index = 7, type = ProtobufType.UINT64)
    final Long fileLength;

    @ProtobufProperty(index = 8, type = ProtobufType.UINT32)
    final Integer width;

    @ProtobufProperty(index = 9, type = ProtobufType.UINT32)
    final Integer height;

    BotMediaMetadata(
            String fileSha256,
            String mediaKey,
            String fileEncSha256,
            String directPath,
            Long mediaKeyTimestamp,
            String mimetype,
            Long fileLength,
            Integer width,
            Integer height
    ) {
        this.fileSha256 = fileSha256;
        this.mediaKey = mediaKey;
        this.fileEncSha256 = fileEncSha256;
        this.directPath = directPath;
        this.mediaKeyTimestamp = mediaKeyTimestamp;
        this.mimetype = mimetype;
        this.fileLength = fileLength;
        this.width = width;
        this.height = height;
    }

    public Optional<String> fileSha256() {
        return Optional.ofNullable(fileSha256);
    }

    public Optional<String> mediaKey() {
        return Optional.ofNullable(mediaKey);
    }

    public Optional<String> fileEncSha256() {
        return Optional.ofNullable(fileEncSha256);
    }

    public Optional<String> directPath() {
        return Optional.ofNullable(directPath);
    }

    public Optional<Long> mediaKeyTimestamp() {
        return Optional.ofNullable(mediaKeyTimestamp);
    }

    public Optional<String> mimetype() {
        return Optional.ofNullable(mimetype);
    }

    public Optional<Long> fileLength() {
        return Optional.ofNullable(fileLength);
    }

    public Optional<Integer> width() {
        return Optional.ofNullable(width);
    }

    public Optional<Integer> height() {
        return Optional.ofNullable(height);
    }
}
