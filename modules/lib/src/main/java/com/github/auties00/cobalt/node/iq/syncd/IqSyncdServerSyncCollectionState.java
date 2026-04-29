package com.github.auties00.cobalt.node.iq.syncd;

/**
 * Per-collection sync state derived from the {@code <collection/>} wire
 * shape returned by the syncd relay.
 *
 * <p>WA Web's parser maps the combination of the {@code type="error"}
 * attribute, the {@code <error code/>} child code, and the optional
 * {@code has_more_patches} attribute onto one of these constants. Cobalt
 * mirrors the same projection verbatim.
 *
 * @implNote {@code WAWebSyncdResponseParser.syncResponseParser} +
 *           {@code WASyncdConst.CollectionState}: WA Web's state enum
 *           additionally carries a {@code Blocked} value that is not
 *           derived from the wire response itself but assigned later by
 *           {@code WAWebSyncdServerSync} after a fatal global error.
 *           Cobalt's enum exposes only the values the response parser
 *           can produce — {@code Blocked} lives outside the wire-shape
 *           projection and is therefore omitted here.
 */
public enum IqSyncdServerSyncCollectionState {
    /**
     * The collection synced cleanly and the relay has no further
     * patches to deliver.
     *
     * @implNote {@code WASyncdConst.CollectionState.Success}: no
     *           {@code type="error"} attribute and no
     *           {@code has_more_patches} marker on the
     *           {@code <collection/>}.
     */
    SUCCESS,

    /**
     * The collection synced cleanly but the relay has additional
     * patches queued — the caller must issue a follow-up sync to
     * fetch them.
     *
     * @implNote {@code WASyncdConst.CollectionState.SuccessHasMore}:
     *           no {@code type="error"} attribute, but the
     *           {@code has_more_patches} marker is present.
     */
    SUCCESS_HAS_MORE,

    /**
     * The relay rejected the local patches because the collection
     * has diverged. The caller must reconcile against the relay's
     * snapshot and re-upload.
     *
     * @implNote {@code WASyncdConst.CollectionState.Conflict}:
     *           {@code type="error"} with {@code <error code="409"/>}
     *           and no {@code has_more_patches} marker.
     */
    CONFLICT,

    /**
     * Like {@link #CONFLICT}, but the relay has additional patches
     * queued after the divergence point — the caller must reconcile
     * and then issue a follow-up sync.
     *
     * @implNote {@code WASyncdConst.CollectionState.ConflictHasMore}:
     *           {@code type="error"} with {@code <error code="409"/>}
     *           and the {@code has_more_patches} marker present.
     */
    CONFLICT_HAS_MORE,

    /**
     * The relay rejected the collection with a fatal error
     * ({@code 400}, {@code 404} or {@code 405}); the caller must
     * not retry without intervention.
     *
     * @implNote {@code WASyncdConst.CollectionState.ErrorFatal}:
     *           {@code type="error"} with
     *           {@code <error code="400"/>} or {@code "404"}.
     */
    ERROR_FATAL,

    /**
     * The relay rejected the collection with a transient error;
     * the caller may retry after the optional server backoff.
     *
     * @implNote {@code WASyncdConst.CollectionState.ErrorRetry}:
     *           {@code type="error"} with any other error code.
     */
    ERROR_RETRY
}
