package com.github.auties00.cobalt.sync.exchange;

import com.github.auties00.cobalt.exception.WhatsAppWebAppStateSyncException;
import com.github.auties00.cobalt.model.media.ExternalBlobReference;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.data.SyncdPatch;

import java.util.*;

/**
 * Represents the parsed response from a sync request, containing either
 * a snapshot reference (as an {@link ExternalBlobReference} to be downloaded)
 * or a collection of patches.
 *
 * <p>Per WhatsApp Web {@code WAWebSyncdResponseParser.h}: each collection in
 * a batched response may independently succeed or fail. The collection-level
 * error state is captured in {@link #collectionError()} so that callers can
 * process successful collections even when some fail.
 *
 * @implNote WAWebSyncdResponseParser.syncResponseParser (return value structure),
 *           WAWebSyncdResponseParser.h (per-collection error state),
 *           WASyncdConst.CollectionState (state mapped to hasMore/exceptions)
 */
public final class MutationSyncResponse {
    private final SyncPatchType collectionName;
    private final long version;
    private final boolean hasMore;
    private final SequencedCollection<SyncdPatch> patches;
    private final ExternalBlobReference snapshotReference;
    private final WhatsAppWebAppStateSyncException collectionError;

    /**
     * Constructs a new sync response.
     *
     * @implNote WAWebSyncdResponseParser.syncResponseParser (return value)
     * @param collectionName the sync collection type
     * @param version the collection version
     * @param hasMore whether more patches are available
     * @param patches the patches in this response
     * @param snapshotReference the external blob reference for the snapshot, or {@code null}
     */
    public MutationSyncResponse(
            SyncPatchType collectionName,
            long version,
            boolean hasMore,
            SequencedCollection<SyncdPatch> patches,
            ExternalBlobReference snapshotReference
    ) {
        this(collectionName, version, hasMore, patches, snapshotReference, null);
    }

    /**
     * Constructs a new sync response with an optional collection-level error.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdResponseParser.h}: when a collection
     * node has {@code type="error"}, the error state is captured on the response
     * object rather than thrown, so that other collections in a batch can still
     * be processed.
     *
     * @implNote WAWebSyncdResponseParser.h (collection error state),
     *           WAWebSyncdServerSync.S (pre-filter ErrorRetry/ErrorFatal/Blocked)
     * @param collectionName  the sync collection type
     * @param version         the collection version
     * @param hasMore         whether more patches are available
     * @param patches         the patches in this response
     * @param snapshotReference the external blob reference for the snapshot, or {@code null}
     * @param collectionError the collection-level error, or {@code null} if successful
     */
    public MutationSyncResponse(
            SyncPatchType collectionName,
            long version,
            boolean hasMore,
            SequencedCollection<SyncdPatch> patches,
            ExternalBlobReference snapshotReference,
            WhatsAppWebAppStateSyncException collectionError
    ) {
        this.collectionName = Objects.requireNonNull(collectionName);
        this.version = version;
        this.hasMore = hasMore;
        this.patches = patches;
        this.snapshotReference = snapshotReference;
        this.collectionError = collectionError;
    }

    /**
     * Returns whether this response contains a snapshot reference.
     *
     * @return {@code true} if a snapshot reference is present
     */
    public boolean isSnapshot() {
        return snapshotReference != null;
    }

    /**
     * Returns the sync collection type.
     *
     * @return the collection name
     */
    public SyncPatchType collectionName() {
        return collectionName;
    }

    /**
     * Returns the collection version.
     *
     * @return the version number
     */
    public long version() {
        return version;
    }

    /**
     * Returns whether more patches are available from the server.
     *
     * @return {@code true} if more patches are available
     */
    public boolean hasMore() {
        return hasMore;
    }

    /**
     * Returns the patches in this response.
     *
     * @return an unmodifiable collection of patches
     */
    public SequencedCollection<SyncdPatch> patches() {
        return patches == null ? List.of() : Collections.unmodifiableSequencedCollection(patches);
    }

    /**
     * Returns the external blob reference for the snapshot, if present.
     *
     * @return an optional containing the snapshot reference
     */
    public Optional<ExternalBlobReference> snapshotReference() {
        return Optional.ofNullable(snapshotReference);
    }

    /**
     * Returns the collection-level error, if present.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdResponseParser.h}: when the
     * collection node has {@code type="error"}, the error is captured here
     * rather than thrown, so batched responses can process other collections
     * independently.
     *
     * @implNote WAWebSyncdResponseParser.h (collection error state),
     *           WAWebSyncdServerSync.S (pre-filter by state)
     * @return an optional containing the collection-level error
     */
    public Optional<WhatsAppWebAppStateSyncException> collectionError() {
        return Optional.ofNullable(collectionError);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MutationSyncResponse that
               && version == that.version
               && hasMore == that.hasMore
               && collectionName == that.collectionName
               && Objects.equals(patches, that.patches)
               && Objects.equals(snapshotReference, that.snapshotReference)
               && Objects.equals(collectionError, that.collectionError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionName, version, hasMore, patches, snapshotReference, collectionError);
    }

    @Override
    public String toString() {
        return "MutationSyncResponse[" +
               "collectionName=" + collectionName + ", " +
               "version=" + version + ", " +
               "hasMore=" + hasMore + ", " +
               "patches=" + patches + ", " +
               "snapshotReference=" + snapshotReference + ", " +
               "collectionError=" + collectionError + ']';
    }
}
