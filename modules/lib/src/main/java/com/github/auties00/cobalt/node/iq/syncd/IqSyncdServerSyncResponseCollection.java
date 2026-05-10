package com.github.auties00.cobalt.node.iq.syncd;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedCollection;

/**
 * A single {@code <collection/>} entry projected from the inbound syncd
 * sync IQ response.
 *
 * <p>Each entry carries the {@link IqSyncdServerSyncCollectionState wire
 * state} derived by {@link IqSyncdServerSyncResponse} from the
 * {@code <collection type? error? has_more_patches?>} attributes, plus
 * the optional {@code version} attribute and the optional
 * {@code <snapshot/>} / {@code <patches/>} payloads.
 */
public final class IqSyncdServerSyncResponseCollection {
    /**
     * The collection name.
     */
    private final String name;

    /**
     * The wire-derived collection state.
     */
    private final IqSyncdServerSyncCollectionState state;

    /**
     * The relay-issued collection version, or {@code null} when the
     * relay omitted it (typically alongside an error state).
     */
    private final Long version;

    /**
     * The list of encoded patch payloads when the relay returned a
     * {@code <patches/>} child. Never {@code null}, possibly empty.
     */
    private final List<byte[]> patches;

    /**
     * The encoded snapshot payload when the relay returned a
     * {@code <snapshot/>} child, or {@code null} when absent. The
     * payload is the encoded {@code ExternalBlobReference} protobuf
     * pointing at the actual snapshot blob in MMS.
     */
    private final byte[] snapshot;

    /**
     * Constructs a new inbound collection projection.
     *
     * @param name     the collection name. Never {@code null}
     * @param state    the wire-derived state. Never {@code null}
     * @param version  the relay-issued version, or {@code null}
     * @param patches  the encoded patch payloads. {@code null} is
     *                 treated as an empty list
     * @param snapshot the encoded snapshot payload, or {@code null}
     * @throws NullPointerException if {@code name} or {@code state}
     *                              is {@code null}
     */
    public IqSyncdServerSyncResponseCollection(String name,
                                               IqSyncdServerSyncCollectionState state,
                                               Long version,
                                               List<byte[]> patches,
                                               byte[] snapshot) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.state = Objects.requireNonNull(state, "state cannot be null");
        this.version = version;
        this.patches = patches == null ? List.of() : patches;
        this.snapshot = snapshot;
    }

    /**
     * Returns the collection name.
     *
     * @return the name. Never {@code null}
     */
    public String name() {
        return name;
    }

    /**
     * Returns the wire-derived collection state.
     *
     * @return the state. Never {@code null}
     */
    public IqSyncdServerSyncCollectionState state() {
        return state;
    }

    /**
     * Returns the relay-issued collection version.
     *
     * @return an {@link Optional} containing the version, or
     *         {@link Optional#empty()} when absent
     */
    public Optional<Long> version() {
        return Optional.ofNullable(version);
    }

    /**
     * Returns the list of encoded patch payloads.
     *
     * @return an unmodifiable view of the patches. Never {@code null},
     *         possibly empty
     */
    public SequencedCollection<byte[]> patches() {
        return Collections.unmodifiableSequencedCollection(patches);
    }

    /**
     * Returns the encoded snapshot payload.
     *
     * @return an {@link Optional} containing the snapshot bytes, or
     *         {@link Optional#empty()} when absent
     */
    public Optional<byte[]> snapshot() {
        return Optional.ofNullable(snapshot);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSyncdServerSyncResponseCollection) obj;
        return Objects.equals(this.name, that.name)
                && this.state == that.state
                && Objects.equals(this.version, that.version)
                && Objects.equals(this.patches, that.patches)
                && Arrays.equals(this.snapshot, that.snapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, state, version, patches, Arrays.hashCode(snapshot));
    }

    @Override
    public String toString() {
        return "IqSyncdServerSyncResponseCollection[name=" + name
                + ", state=" + state
                + ", version=" + version
                + ", patches=" + patches.size()
                + ", snapshot=" + (snapshot == null ? "null" : "byte[" + snapshot.length + "]") + ']';
    }
}
