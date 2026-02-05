package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Metadata about a bot session.
 *
 * @apiNote WAWebProtobufsE2E.pb.BotSessionMetadata
 */
@ProtobufMessage(name = "BotSessionMetadata")
public final class BotSessionMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String sessionId;

    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final BotSessionSource sessionSource;

    BotSessionMetadata(String sessionId, BotSessionSource sessionSource) {
        this.sessionId = sessionId;
        this.sessionSource = sessionSource;
    }

    public Optional<String> sessionId() {
        return Optional.ofNullable(sessionId);
    }

    public Optional<BotSessionSource> sessionSource() {
        return Optional.ofNullable(sessionSource);
    }

    @ProtobufEnum(name = "BotSessionSource")
    public enum BotSessionSource {
        NONE(0),
        NULL_STATE(1),
        TYPEAHEAD(2),
        USER_INPUT(3),
        EMU_FLASH(4),
        EMU_FLASH_FOLLOWUP(5),
        VOICE(6),
        AI_HOME_SESSION(7);

        final int index;

        BotSessionSource(@ProtobufEnumIndex int index) {
            this.index = index;
        }
    }
}
