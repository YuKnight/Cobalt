package com.github.auties00.cobalt.model.sync;

import java.util.List;
import java.util.Objects;

/**
 * Carries the result of a SyncD app-state sync round trip.
 *
 * <p>The relay returns one {@link AppStateSyncCollectionResult} per
 * requested collection, projecting the {@code <collection/>} children
 * of the inbound {@code <sync/>} envelope. Callers iterate the list
 * to drive their per-collection retry / reconcile / snapshot-fetch
 * logic.
 *
 * <p>This carrier is a plain immutable value object (not a
 * {@link it.auties.protobuf.annotation.ProtobufMessage}) — it surfaces
 * the parsed reply to caller code and never travels on the wire.
 */
public final class AppStateSyncResult {
    /**
     * The list of per-collection results.
     */
    private final List<AppStateSyncCollectionResult> collections;

    /**
     * Constructs a new sync result.
     *
     * @param collections the per-collection results; never
     *                    {@code null}, possibly empty
     * @throws NullPointerException if {@code collections} is
     *                              {@code null}
     */
    public AppStateSyncResult(List<AppStateSyncCollectionResult> collections) {
        Objects.requireNonNull(collections, "collections cannot be null");
        this.collections = List.copyOf(collections);
    }

    /**
     * Returns the unmodifiable list of per-collection results.
     *
     * @return the results; never {@code null}, possibly empty
     */
    public List<AppStateSyncCollectionResult> collections() {
        return collections;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (AppStateSyncResult) obj;
        return Objects.equals(this.collections, that.collections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collections);
    }

    @Override
    public String toString() {
        return "AppStateSyncResult[collections=" + collections + ']';
    }
}
