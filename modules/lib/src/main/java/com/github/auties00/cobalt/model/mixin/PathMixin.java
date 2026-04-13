package com.github.auties00.cobalt.model.mixin;

import it.auties.protobuf.annotation.ProtobufDeserializer;
import it.auties.protobuf.annotation.ProtobufMixin;
import it.auties.protobuf.annotation.ProtobufSerializer;

import java.nio.file.Path;

/**
 * A protobuf mixin that converts between {@link Path} and a {@code String} representing
 * the path.
 */
@ProtobufMixin
public final class PathMixin {
    /**
     * Converts a {@link Path} to its string representation.
     *
     * @param path the path to convert, or {@code null}
     * @return the path string, or {@code null} if {@code path} is {@code null}
     */
    @ProtobufSerializer
    public static String toStringValue(Path path) {
        return path == null ? null : path.toString();
    }

    /**
     * Converts a {@code String} value to a {@link Path}.
     *
     * @param value the path string, or {@code null}
     * @return the corresponding {@link Path}, or {@code null} if {@code value}
     *         is {@code null}
     */
    @ProtobufDeserializer
    public static Path fromString(String value) {
        return value == null ? null : Path.of(value);
    }
}