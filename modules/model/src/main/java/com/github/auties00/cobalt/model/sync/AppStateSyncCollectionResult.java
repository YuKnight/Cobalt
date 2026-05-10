package com.github.auties00.cobalt.model.sync;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Carries the per-collection outcome of a SyncD app-state sync round
 * trip.
 *
 * <p>Each entry surfaces the wire-derived {@link AppStateSyncStatus},
 * the relay-issued collection version, the list of encoded patch
 * payloads (when the relay shipped patches), and the encoded snapshot
 * payload (when the relay shipped a snapshot). The patch and snapshot
 * payloads are kept as raw bytes — caller code routes them through
 * the SyncD decoder to obtain {@code SyncdPatch} / {@code SyncdSnapshot}
 * protobuf instances.
 *
 * <p>This carrier is a plain immutable value object (not a
 * {@link it.auties.protobuf.annotation.ProtobufMessage}) — it surfaces
 * the parsed reply to caller code and never travels on the wire.
 */
public final class AppStateSyncCollectionResult {
    /**
     * The collection name.
     */
    private final String name;

    /**
     * The wire-derived sync status.
     */
    private final AppStateSyncStatus status;

    /**
     * The relay-issued collection version, or {@code null} when the
     * relay omitted it (typically alongside an error status).
     */
    private final Long version;

    /**
     * The list of encoded patch payloads when the relay shipped a
     * {@code <patches/>} child.
     */
    private final List<byte[]> patches;

    /**
     * The encoded snapshot payload (an
     * {@code ExternalBlobReference} pointing at the actual snapshot
     * blob in MMS) when the relay shipped a {@code <snapshot/>}
     * child, or {@code null} when absent.
     */
    private final byte[] snapshot;

    /**
     * Constructs a new collection-level result.
     *
     * @param name     the collection name; never {@code null}
     * @param status   the wire-derived status; never {@code null}
     * @param version  the relay-issued version, or {@code null}
     * @param patches  the encoded patch payloads; {@code null} is
     *                 treated as an empty list
     * @param snapshot the encoded snapshot payload, or {@code null}
     * @throws NullPointerException if {@code name} or {@code status}
     *                              is {@code null}
     */
    public AppStateSyncCollectionResult(String name, AppStateSyncStatus status, Long version,
                                        List<byte[]> patches, byte[] snapshot) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.version = version;
        this.patches = patches == null ? List.of() : List.copyOf(patches);
        this.snapshot = snapshot == null ? null : snapshot.clone();
    }

    /**
     * Returns the collection name.
     *
     * @return the name; never {@code null}
     */
    public String name() {
        return name;
    }

    /**
     * Returns the wire-derived sync status.
     *
     * @return the status; never {@code null}
     */
    public AppStateSyncStatus status() {
        return status;
    }

    /**
     * Returns the relay-issued collection version.
     *
     * @return an {@link OptionalLong} containing the version, or
     *         empty when the relay omitted it
     */
    public OptionalLong version() {
        return version == null ? OptionalLong.empty() : OptionalLong.of(version);
    }

    /**
     * Returns the unmodifiable list of encoded patch payloads.
     *
     * @return the patches; never {@code null}, possibly empty
     */
    public List<byte[]> patches() {
        return patches;
    }

    /**
     * Returns a defensive copy of the encoded snapshot payload.
     *
     * @return an {@link Optional} containing a clone of the snapshot
     *         bytes, or empty when the relay shipped no snapshot
     */
    public Optional<byte[]> snapshot() {
        return snapshot == null ? Optional.empty() : Optional.of(snapshot.clone());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (AppStateSyncCollectionResult) obj;
        return Objects.equals(this.name, that.name)
                && this.status == that.status
                && Objects.equals(this.version, that.version)
                && Objects.equals(this.patches, that.patches)
                && Arrays.equals(this.snapshot, that.snapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, status, version, patches, Arrays.hashCode(snapshot));
    }

    @Override
    public String toString() {
        return "AppStateSyncCollectionResult[name=" + name
                + ", status=" + status
                + ", version=" + version
                + ", patches=" + patches.size()
                + ", snapshot=" + (snapshot == null ? "null" : "byte[" + snapshot.length + "]") + ']';
    }
}
