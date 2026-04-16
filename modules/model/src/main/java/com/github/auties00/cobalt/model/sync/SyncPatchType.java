package com.github.auties00.cobalt.model.sync;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Partitions the app state sync stream into independent collections, each
 * with its own version counter and integrity hash.
 *
 * <p>WhatsApp groups sync mutations into a small fixed set of collections
 * so that critical mutations (such as a block list change) can be fetched
 * first without being blocked by bulkier regular mutations. The names of
 * these collections, represented by the constants on this enum, are part
 * of the wire protocol and are used as keys in sync messages and requests.
 *
 * <p>The {@code toString()} form uses the lowercase name (for example
 * {@code "critical_block"}) because this is the form accepted by the
 * server; {@link #toBytes()} caches the byte encoding of that form for
 * reuse on hot paths.
 */
@ProtobufEnum
public enum SyncPatchType {
    /**
     * Critical collection dedicated to block list related mutations, which
     * must be applied before regular collections to enforce the user's
     * privacy choices as early as possible after login.
     */
    CRITICAL_BLOCK(0),

    /**
     * Critical collection dedicated to low priority critical mutations
     * that still need to be applied before the regular collections.
     */
    CRITICAL_UNBLOCK_LOW(1),

    /**
     * Regular collection carrying high priority non critical mutations.
     */
    REGULAR_HIGH(2),

    /**
     * Regular collection carrying low priority non critical mutations.
     */
    REGULAR_LOW(3),

    /**
     * Regular collection carrying the bulk of non critical mutations.
     */
    REGULAR(4);

    /**
     * Lookup table mapping the lowercase name string to the corresponding
     * enum constant, used by {@link #of(String)}.
     */
    private static final Map<String, SyncPatchType> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(SyncPatchType::toString, Function.identity()));

    /**
     * Protobuf wire index for this enum constant.
     */
    final int index;

    /**
     * Cached byte encoding of the lowercase name, reused to avoid
     * allocating on every serialisation.
     */
    private final byte[] bytes;

    /**
     * Constructs a new enum constant with the given protobuf wire index.
     *
     * @param index the protobuf wire index
     */
    SyncPatchType(@ProtobufEnumIndex int index) {
        this.index = index;
        this.bytes = toString().getBytes();
    }

    /**
     * Returns the protobuf wire index for this enum constant.
     *
     * @return the protobuf wire index
     */
    public int index() {
        return index;
    }

    /**
     * Resolves a collection by its lowercase wire name.
     *
     * @param name the lowercase name, or {@code null}
     * @return an optional containing the matching constant, empty if the
     *         name is {@code null} or does not match a known collection
     */
    public static Optional<SyncPatchType> of(String name) {
        return name == null ? Optional.empty() : Optional.ofNullable(BY_NAME.get(name));
    }

    /**
     * Returns the lowercase wire name of this collection.
     *
     * @return the lowercase collection name
     */
    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the cached byte encoding of the lowercase wire name.
     *
     * <p>The returned array is the cached buffer; callers must not mutate
     * it.
     *
     * @return the cached name bytes
     */
    public byte[] toBytes() {
        return bytes;
    }

    /**
     * Returns whether this collection is classified as a critical
     * collection.
     *
     * <p>During bootstrap, only critical collections
     * ({@link #CRITICAL_BLOCK} and {@link #CRITICAL_UNBLOCK_LOW}) are
     * processed from server sync notifications so that privacy and safety
     * related mutations are applied before the rest of the app state.
     *
     * @return {@code true} if this is a critical collection
     */
    public boolean isCritical() {
        return this == CRITICAL_BLOCK || this == CRITICAL_UNBLOCK_LOW;
    }
}
