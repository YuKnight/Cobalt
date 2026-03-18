package com.github.auties00.cobalt.model.sync;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@ProtobufEnum
public enum SyncPatchType {
    CRITICAL_BLOCK(0),
    CRITICAL_UNBLOCK_LOW(1),
    REGULAR_HIGH(2),
    REGULAR_LOW(3),
    REGULAR(4);

    private static final Map<String, SyncPatchType> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(SyncPatchType::toString, Function.identity()));

    final int index;
    private final byte[] bytes;

    SyncPatchType(@ProtobufEnumIndex int index) {
        this.index = index;
        this.bytes = toString().getBytes();
    }

    public int index() {
        return index;
    }

    public static Optional<SyncPatchType> of(String name) {
        return name == null ? Optional.empty() : Optional.ofNullable(BY_NAME.get(name));
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public byte[] toBytes() {
        return bytes;
    }

    /**
     * Returns whether this collection type is a critical collection.
     *
     * <p>Critical collections are {@link #CRITICAL_BLOCK} and
     * {@link #CRITICAL_UNBLOCK_LOW}. During bootstrap, only critical
     * collections are processed from server sync notifications.
     *
     * @return {@code true} if this is a critical collection
     * @implNote WAWebSyncdCollectionUtils.isCriticalCollection
     */
    public boolean isCritical() {
        return this == CRITICAL_BLOCK || this == CRITICAL_UNBLOCK_LOW;
    }
}