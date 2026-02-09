package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * The plaintext event response content that gets AES-GCM encrypted
 * inside an {@link EncryptedEventResponseMessage}.
 *
 * @apiNote WAWebProtobufsE2E.pb.Message.EventResponseMessage: contains
 * the RSVP response, timestamp, and optional extra guest count.
 */
@ProtobufMessage(name = "Message.EventResponseMessage")
public final class EventResponseMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    final EventResponseType response;

    @ProtobufProperty(index = 2, type = ProtobufType.INT64)
    final long timestampMs;

    @ProtobufProperty(index = 3, type = ProtobufType.INT32)
    final Integer extraGuestCount;

    EventResponseMessage(EventResponseType response, long timestampMs, Integer extraGuestCount) {
        this.response = response;
        this.timestampMs = timestampMs;
        this.extraGuestCount = extraGuestCount;
    }

    public Optional<EventResponseType> response() {
        return Optional.ofNullable(response);
    }

    public long timestampMs() {
        return timestampMs;
    }

    public OptionalInt extraGuestCount() {
        return extraGuestCount == null ? OptionalInt.empty() : OptionalInt.of(extraGuestCount);
    }

    @Override
    public Type type() {
        return Type.EVENT_RESPONSE;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @ProtobufEnum(name = "Message.EventResponseMessage.EventResponseType")
    public enum EventResponseType {
        UNKNOWN(0),
        GOING(1),
        NOT_GOING(2),
        MAYBE(3);

        final int index;

        EventResponseType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
