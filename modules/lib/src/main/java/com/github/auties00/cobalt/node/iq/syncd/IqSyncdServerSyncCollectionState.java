package com.github.auties00.cobalt.node.iq.syncd;

/**
 * Per-collection sync state derived from the {@code <collection/>} wire
 * shape returned by the syncd relay.
 *
 * <p>WA Web's parser maps the combination of the {@code type="error"}
 * attribute, the {@code <error code/>} child code, and the optional
 * {@code has_more_patches} attribute onto one of these constants. Cobalt
 * mirrors the same projection verbatim.
 */
public enum IqSyncdServerSyncCollectionState {
    /**
     * The collection synced cleanly and the relay has no further
     * patches to deliver.
     */
    SUCCESS,

    /**
     * The collection synced cleanly but the relay has additional
     * patches queued. The caller must issue a follow-up sync to
     * fetch them.
     */
    SUCCESS_HAS_MORE,

    /**
     * The relay rejected the local patches because the collection
     * has diverged. The caller must reconcile against the relay's
     * snapshot and re-upload.
     */
    CONFLICT,

    /**
     * Like {@link #CONFLICT}, but the relay has additional patches
     * queued after the divergence point. The caller must reconcile
     * and then issue a follow-up sync.
     */
    CONFLICT_HAS_MORE,

    /**
     * The relay rejected the collection with a fatal error
     * ({@code 400}, {@code 404} or {@code 405}). The caller must
     * not retry without intervention.
     */
    ERROR_FATAL,

    /**
     * The relay rejected the collection with a transient error.
     * The caller may retry after the optional server backoff.
     */
    ERROR_RETRY
}
