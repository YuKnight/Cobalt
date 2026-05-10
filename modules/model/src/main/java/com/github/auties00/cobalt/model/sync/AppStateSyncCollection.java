package com.github.auties00.cobalt.model.sync;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Carries one entry of a SyncD app-state sync round trip — the
 * collection name, the locally-known version, and the optional encoded
 * {@code SyncdPatch} bytes that ship local mutations.
 *
 * <p>Each entry asks the relay either for a snapshot (when
 * {@link #version()} is empty) or for the patches above a known
 * version. When the local client has pending mutations to push as
 * well, the encoded patch payload is attached via {@link #patch()}.
 *
 * <p>This carrier is a plain immutable value object (not a
 * {@link it.auties.protobuf.annotation.ProtobufMessage}) — it travels
 * from caller code to the wire-encoder and is never serialized as
 * protobuf itself.
 */
public final class AppStateSyncCollection {
    /**
     * The collection name (e.g. {@code "regular"}, {@code "regular_high"}).
     */
    private final String name;

    /**
     * The locally-known collection version as a boxed {@link Long} so
     * callers can express "never synced before" as {@code null}.
     */
    private final Long version;

    /**
     * The encoded {@code SyncdPatch} protobuf bytes carrying local
     * mutations to push, or {@code null} when this entry only fetches
     * remote state.
     */
    private final byte[] patch;

    /**
     * Constructs a new collection entry.
     *
     * @param name    the collection name; never {@code null}
     * @param version the locally-known version, or {@code null} when
     *                the caller has never synced this collection
     * @param patch   the encoded patch bytes, or {@code null} when
     *                this entry fetches only
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public AppStateSyncCollection(String name, Long version, byte[] patch) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.version = version;
        this.patch = patch == null ? null : patch.clone();
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
     * Returns the locally-known collection version.
     *
     * @return an {@link OptionalLong} containing the version, or
     *         empty when the caller has never synced this collection
     */
    public OptionalLong version() {
        return version == null ? OptionalLong.empty() : OptionalLong.of(version);
    }

    /**
     * Returns a defensive copy of the encoded patch bytes.
     *
     * @return an {@link Optional} containing a clone of the patch
     *         bytes, or empty when this entry only fetches remote
     *         state
     */
    public Optional<byte[]> patch() {
        return patch == null ? Optional.empty() : Optional.of(patch.clone());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (AppStateSyncCollection) obj;
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
        return "AppStateSyncCollection[name=" + name
                + ", version=" + version
                + ", patch=" + (patch == null ? "null" : "byte[" + patch.length + "]") + ']';
    }
}
