package com.github.auties00.cobalt.model.sync;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * Per collection integrity state, pairing the current monotonic version
 * number with the running LT-Hash used by app state sync to detect
 * tampering or missed mutations.
 *
 * <p>Each collection type (see {@link SyncPatchType}) has its own
 * {@code SyncHashValue}. The LT-Hash is updated incrementally as mutations
 * are applied, and the resulting value is compared against the server's
 * snapshot hash at the end of every patch round to verify that the local
 * state remains consistent.
 *
 * <p>The class is mutable to allow in place updates during patch
 * application, and implements {@link Cloneable} so that a copy can be
 * taken before attempting an operation that may need to be rolled back.
 */
@ProtobufMessage
public final class SyncHashValue implements Cloneable{
    /**
     * Collection type this hash value belongs to.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    final SyncPatchType type;

    /**
     * Monotonically increasing version counter for the collection; each
     * applied patch increments it.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.UINT64)
    long version;

    /**
     * 128 byte LT-Hash accumulator for this collection, updated
     * incrementally as mutations are applied.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    byte[] hash;

    /**
     * Constructs a new hash value with explicit initial state.
     *
     * @param type the collection type
     * @param version the initial version counter
     * @param hash the initial LT-Hash accumulator
     */
    SyncHashValue(SyncPatchType type, long version, byte[] hash) {
        this.type = type;
        this.version = version;
        this.hash = hash;
    }

    /**
     * Constructs a new hash value for a collection that has never been
     * synced, starting at version zero with a zero filled 128 byte hash.
     *
     * @param type the collection type
     */
    public SyncHashValue(SyncPatchType type) {
        this.type = type;
        this.version = 0;
        this.hash = new byte[128];
    }

    /**
     * Returns the collection type this hash value belongs to.
     *
     * @return the collection type
     */
    public SyncPatchType type() {
        return type;
    }

    /**
     * Returns the current version counter.
     *
     * @return the version counter
     */
    public long version() {
        return version;
    }

    /**
     * Sets the current version counter.
     *
     * @param version the new version counter
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Returns the current LT-Hash accumulator.
     *
     * <p>The returned array is the live buffer; callers that need to
     * retain a copy across subsequent mutations should clone it.
     *
     * @return the LT-Hash bytes
     */
    public byte[] hash() {
        return hash;
    }

    /**
     * Sets the current LT-Hash accumulator.
     *
     * @param hash the new LT-Hash bytes
     */
    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    /**
     * Returns an independent copy of this hash value, with a cloned hash
     * array so that subsequent mutations on either instance do not affect
     * the other.
     *
     * @return an independent clone of this hash value
     */
    @Override
    public SyncHashValue clone() {
        return new SyncHashValue(type, version, hash.clone());
    }
}
