package com.github.auties00.cobalt.node.iq.syncd;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * A single {@code <collection/>} entry in an outbound syncd sync IQ.
 *
 * <p>Each entry asks the relay either for a snapshot (when {@link #version()}
 * is empty) or for the patches above a known version. When the local client
 * also has pending mutations to push, an encrypted patch payload is
 * attached via {@link #patch()}.
 */
public final class IqSyncdServerSyncRequestCollection {
    /**
     * The collection name (e.g. {@code "regular"}, {@code "regular_high"}).
     */
    private final String name;

    /**
     * The locally-known collection version, or {@code null} when the
     * caller has never synced this collection (in which case the
     * relay returns a fresh snapshot).
     */
    private final Long version;

    /**
     * The encoded {@code SyncdPatch} protobuf carrying local
     * mutations to push, or {@code null} when this entry only fetches
     * remote state.
     */
    private final byte[] patch;

    /**
     * Constructs a new outbound collection entry.
     *
     * @param name    the collection name. Never {@code null}
     * @param version the locally-known version, or {@code null} when
     *                the caller has never synced this collection
     * @param patch   the encoded {@code SyncdPatch} bytes carrying
     *                local mutations, or {@code null} when this entry
     *                fetches only
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public IqSyncdServerSyncRequestCollection(String name, Long version, byte[] patch) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.version = version;
        this.patch = patch;
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
     * Returns the locally-known collection version.
     *
     * @return an {@link Optional} containing the version, or
     *         {@link Optional#empty()} when the caller has never
     *         synced this collection
     */
    public Optional<Long> version() {
        return Optional.ofNullable(version);
    }

    /**
     * Returns the encoded {@code SyncdPatch} bytes carrying local
     * mutations.
     *
     * @return an {@link Optional} containing the patch bytes, or
     *         {@link Optional#empty()} when this entry only fetches
     *         remote state
     */
    public Optional<byte[]> patch() {
        return Optional.ofNullable(patch);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSyncdServerSyncRequestCollection) obj;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.version, that.version)
                && Arrays.equals(this.patch, that.patch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, Arrays.hashCode(patch));
    }

    @Override
    public String toString() {
        return "IqSyncdServerSyncRequestCollection[name=" + name
                + ", version=" + version
                + ", patch=" + (patch == null ? "null" : "byte[" + patch.length + "]") + ']';
    }
}
