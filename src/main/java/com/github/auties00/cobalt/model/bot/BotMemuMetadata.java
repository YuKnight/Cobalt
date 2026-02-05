package com.github.auties00.cobalt.model.bot;

import com.github.auties00.cobalt.model.info.Info;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;

/**
 * Metadata for bot MEMU (Me & U) features like face images.
 *
 * @apiNote WAWebProtobufsE2E.pb.BotMemuMetadata
 */
@ProtobufMessage(name = "BotMemuMetadata")
public final class BotMemuMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final List<BotMediaMetadata> faceImages;

    BotMemuMetadata(List<BotMediaMetadata> faceImages) {
        this.faceImages = faceImages;
    }

    public List<BotMediaMetadata> faceImages() {
        return faceImages;
    }
}
