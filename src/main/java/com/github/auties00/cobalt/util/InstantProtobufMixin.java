package com.github.auties00.cobalt.util;

import it.auties.protobuf.annotation.ProtobufDeserializer;
import it.auties.protobuf.annotation.ProtobufMixin;
import it.auties.protobuf.annotation.ProtobufSerializer;

import java.time.Instant;

// TODO: Add date API into protobuf built-ins
@ProtobufMixin
public final class InstantProtobufMixin {
    @ProtobufSerializer
    public static Long toMillis(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    @ProtobufDeserializer
    public static Instant fromMillis(Long value) {
        return value == null ? null : Instant.ofEpochMilli(value);
    }
}
