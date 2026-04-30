package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.error.DisconnectReason;
import com.github.auties00.cobalt.model.sync.SyncPatchType;

import java.util.HexFormat;
import java.util.Objects;

/**
 * Thrown for failures during Web App State (also known as syncd)
 * synchronization.
 *
 * <p>Web App State is the encrypted key-value protocol WhatsApp uses
 * to keep contacts, chat metadata, settings, starred messages, the
 * blocklist, and similar shared state in sync across the primary phone
 * and every linked companion device. State is published as named
 * collections; updates are sent as patches that the receiving device
 * applies on top of the last known snapshot. Each patch carries
 * mutations whose values, indices, and ordering are protected by HMACs
 * and chained against the previous patch.
 *
 * <p>The nested subtypes describe every distinct way that pipeline can
 * fail: integrity checks at the snapshot, patch, value or index level
 * ({@link SnapshotMacMismatch}, {@link PatchMacMismatch},
 * {@link ValueMacMismatch}, {@link IndexMacMismatch}), missing
 * encryption keys ({@link MissingKey},
 * {@link MissingKeyOnAllDevices},
 * {@link TimeoutWhileWaitingForMissingKey}), decryption failures
 * ({@link DecryptionFailed}), problems with externally hosted blob
 * mutations ({@link ExternalDownloadFailed},
 * {@link ExternalDecodeFailed}), MAC computation failures
 * ({@link MacComputationFailed}), structural violations of the patch
 * stream ({@link MissingPatches}, {@link TerminalPatch},
 * {@link MissingActionTimestamp}, {@link DuplicateIndexInPatch},
 * {@link DuplicatePatchVersion}), server-side rejections
 * ({@link Conflict}, {@link RetryableServerError}), and a catch-all
 * ({@link UnexpectedError}).
 *
 * <p>Whether a failure is fatal depends on the subtype: integrity
 * failures, structural violations, and unrecoverable key losses are
 * fatal because the affected collection has to be wiped and
 * resynchronized from scratch; missing keys, transient server errors,
 * and external-blob retries are non-fatal.
 *
 * @see SnapshotMacMismatch
 * @see PatchMacMismatch
 * @see ValueMacMismatch
 * @see IndexMacMismatch
 * @see MissingKey
 * @see MissingKeyOnAllDevices
 * @see TimeoutWhileWaitingForMissingKey
 * @see MissingPatches
 * @see TerminalPatch
 * @see Conflict
 * @see RetryableServerError
 * @see DecryptionFailed
 * @see ExternalDownloadFailed
 * @see ExternalDecodeFailed
 * @see MacComputationFailed
 * @see MissingActionTimestamp
 * @see DuplicateIndexInPatch
 * @see DuplicatePatchVersion
 * @see UnexpectedError
 */
@WhatsAppWebModule(moduleName = "WAWebSyncdError")
public sealed abstract class WhatsAppWebAppStateSyncException extends WhatsAppException
        permits WhatsAppWebAppStateSyncException.SnapshotMacMismatch,
                WhatsAppWebAppStateSyncException.PatchMacMismatch,
                WhatsAppWebAppStateSyncException.ValueMacMismatch,
                WhatsAppWebAppStateSyncException.IndexMacMismatch,
                WhatsAppWebAppStateSyncException.MissingKey,
                WhatsAppWebAppStateSyncException.MissingKeyOnAllDevices,
                WhatsAppWebAppStateSyncException.TimeoutWhileWaitingForMissingKey,
                WhatsAppWebAppStateSyncException.MissingPatches,
                WhatsAppWebAppStateSyncException.TerminalPatch,
                WhatsAppWebAppStateSyncException.Conflict,
                WhatsAppWebAppStateSyncException.RetryableServerError,
                WhatsAppWebAppStateSyncException.DecryptionFailed,
                WhatsAppWebAppStateSyncException.ExternalDownloadFailed,
                WhatsAppWebAppStateSyncException.ExternalDecodeFailed,
                WhatsAppWebAppStateSyncException.MacComputationFailed,
                WhatsAppWebAppStateSyncException.MissingActionTimestamp,
                WhatsAppWebAppStateSyncException.DuplicateIndexInPatch,
                WhatsAppWebAppStateSyncException.DuplicatePatchVersion,
                WhatsAppWebAppStateSyncException.UnexpectedError {

    /**
     * Constructs a new web app state sync exception with the specified detail message.
     *
     * @param message the detail message describing the sync failure
     */
    protected WhatsAppWebAppStateSyncException(String message) {
        super(message);
    }

    /**
     * Constructs a new web app state sync exception with a detail message and cause.
     *
     * @param message the detail message describing the sync failure
     * @param cause   the underlying cause of the sync failure
     */
    protected WhatsAppWebAppStateSyncException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Thrown when the HMAC stamped over a state snapshot does not
     * match the value Cobalt computes locally.
     *
     * <p>Snapshots are full state dumps used during initial sync and
     * after a fatal resync. A mismatch means the snapshot bytes are
     * corrupt, were tampered with, or were validated against the
     * wrong key. The collection has to be wiped and refetched.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class SnapshotMacMismatch extends WhatsAppWebAppStateSyncException {
        /**
         * The collection whose snapshot failed validation.
         */
        private final SyncPatchType collectionName;

        /**
         * The snapshot version that failed validation.
         */
        private final long version;

        /**
         * Constructs a new snapshot MAC mismatch exception.
         *
         * @param collectionName the collection whose snapshot failed validation
         * @param version        the snapshot version
         * @throws NullPointerException if {@code collectionName} is {@code null}
         */
        public SnapshotMacMismatch(SyncPatchType collectionName, long version) {
            super("Snapshot MAC mismatch for collection " + collectionName + " at version " + version);
            this.collectionName = Objects.requireNonNull(collectionName);
            this.version = version;
        }

        /**
         * Returns the collection whose snapshot failed validation.
         *
         * @return the collection identifier, never {@code null}
         */
        public SyncPatchType collectionName() {
            return collectionName;
        }

        /**
         * Returns the snapshot version that failed validation.
         *
         * @return the snapshot version
         */
        public long version() {
            return version;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when the HMAC stamped over a state patch does not match
     * the value Cobalt computes locally.
     *
     * <p>Patches are incremental updates chained on top of the latest
     * snapshot; a MAC mismatch means the patch bytes are corrupt,
     * were tampered with, or that the chain has been broken by a
     * missing predecessor. The affected collection has to be wiped
     * and resynced.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class PatchMacMismatch extends WhatsAppWebAppStateSyncException {
        /**
         * The collection whose patch failed validation.
         */
        private final SyncPatchType collectionName;

        /**
         * The patch version that failed validation.
         */
        private final long version;

        /**
         * Constructs a new patch MAC mismatch exception.
         *
         * @param collectionName the collection whose patch failed validation
         * @param version        the patch version
         * @throws NullPointerException if {@code collectionName} is {@code null}
         */
        public PatchMacMismatch(SyncPatchType collectionName, long version) {
            super("Patch MAC mismatch for collection " + collectionName + " at version " + version);
            this.collectionName = Objects.requireNonNull(collectionName);
            this.version = version;
        }

        /**
         * Returns the collection whose patch failed validation.
         *
         * @return the collection identifier, never {@code null}
         */
        public SyncPatchType collectionName() {
            return collectionName;
        }

        /**
         * Returns the patch version that failed validation.
         *
         * @return the patch version
         */
        public long version() {
            return version;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when the HMAC over the encrypted value of a single
     * mutation does not match the expected value after decryption.
     *
     * <p>The value MAC ties an encrypted mutation to its plaintext.
     * A mismatch means either the ciphertext was corrupted or the
     * decryption key is wrong; in either case the collection cannot
     * be trusted and has to be resynced.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class ValueMacMismatch extends WhatsAppWebAppStateSyncException {
        /**
         * Constructs a new value MAC mismatch exception.
         */
        public ValueMacMismatch() {
            super("Value MAC mismatch: mutation value integrity check failed");
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when the HMAC over the encrypted index of a single
     * mutation does not match the expected value after decryption.
     *
     * <p>The index MAC binds a mutation to its key in the key-value
     * store and prevents an attacker from substituting one mutation
     * for another. A mismatch is treated as data corruption and the
     * collection is resynced.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class IndexMacMismatch extends WhatsAppWebAppStateSyncException {
        /**
         * Constructs a new index MAC mismatch exception.
         */
        public IndexMacMismatch() {
            super("Index MAC mismatch: mutation index integrity check failed");
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when a sync mutation references an encryption key that
     * is not yet present locally.
     *
     * <p>App-state keys are pushed from the primary phone and rotated
     * periodically. A missing key is normally transient: requesting
     * the key from a companion that has it and waiting for the reply
     * is enough to make progress, so the failure is not fatal.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdMissingKeyError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class MissingKey extends WhatsAppWebAppStateSyncException {
        /**
         * The identifier of the missing key.
         */
        private final byte[] keyId;

        /**
         * Constructs a new missing key exception.
         *
         * @param keyId the identifier of the missing key
         * @throws NullPointerException if {@code keyId} is {@code null}
         */
        public MissingKey(byte[] keyId) {
            super("Missing sync key with id " + HexFormat.of().formatHex(
                    Objects.requireNonNull(keyId, "keyId cannot be null")));
            this.keyId = keyId;
        }

        /**
         * Returns the identifier of the missing key.
         *
         * @return the key id, never {@code null}
         */
        public byte[] keyId() {
            return keyId;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code false}
         */
        @Override
        public boolean isFatal() {
            return false;
        }
    }

    /**
     * Thrown when no companion device has the encryption key that a
     * mutation needs.
     *
     * <p>Cobalt asks every other device about a missing key before
     * concluding the key is unrecoverable. When every device has
     * answered that it does not hold the key, this exception is
     * raised: the affected collection cannot be decrypted on this
     * device without re-pairing, so the failure is fatal.
     */
    @WhatsAppWebModule(moduleName = "WAWebSyncdStoreMissingKeys")
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class MissingKeyOnAllDevices extends WhatsAppWebAppStateSyncException {
        /**
         * The identifier of the key that no companion device holds.
         */
        private final byte[] keyId;

        /**
         * Constructs a new missing-key-on-all-devices exception.
         *
         * @param keyId the identifier of the missing key
         * @throws NullPointerException if {@code keyId} is {@code null}
         */
        public MissingKeyOnAllDevices(byte[] keyId) {
            super("Missing sync key with id " + HexFormat.of().formatHex(
                    Objects.requireNonNull(keyId, "keyId cannot be null")) + " on all companion devices");
            this.keyId = keyId;
        }

        /**
         * Returns the identifier of the missing key.
         *
         * @return the key id, never {@code null}
         */
        public byte[] keyId() {
            return keyId;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when the wait for a missing app-state key expires before
     * any companion device has answered with the key.
     *
     * <p>The wait timeout is longer than the per-device round-trip,
     * so timing out means the key is effectively unobtainable. The
     * failure is fatal and the collection has to be resynced from
     * scratch.
     */
    @WhatsAppWebModule(moduleName = "WAWebSyncdStoreMissingKeys")
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class TimeoutWhileWaitingForMissingKey extends WhatsAppWebAppStateSyncException {
        /**
         * The identifier of the key whose wait timed out.
         */
        private final byte[] keyId;

        /**
         * Constructs a new timeout-while-waiting-for-missing-key exception.
         *
         * @param keyId the identifier of the missing key whose wait timed out
         * @throws NullPointerException if {@code keyId} is {@code null}
         */
        public TimeoutWhileWaitingForMissingKey(byte[] keyId) {
            super("Timeout waiting for missing sync key with id " + HexFormat.of().formatHex(
                    Objects.requireNonNull(keyId, "keyId cannot be null")));
            this.keyId = keyId;
        }

        /**
         * Returns the identifier of the missing key whose wait timed out.
         *
         * @return the key id, never {@code null}
         */
        public byte[] keyId() {
            return keyId;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when AES-GCM decryption of a mutation fails after the
     * correct key has been located.
     *
     * <p>This typically indicates corrupted ciphertext or an issue
     * with key derivation. The collection is wiped and resynced.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class DecryptionFailed extends WhatsAppWebAppStateSyncException {
        /**
         * Constructs a new decryption failed exception wrapping a cause.
         *
         * @param cause the underlying cryptographic exception
         */
        public DecryptionFailed(Throwable cause) {
            super("Failed to decrypt mutation", cause);
        }

        /**
         * Constructs a new decryption failed exception with a message and cause.
         *
         * @param message extra detail about the decryption failure
         * @param cause   the underlying cryptographic exception
         */
        public DecryptionFailed(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when downloading an external mutation blob fails.
     *
     * <p>Mutations larger than the inline limit are stored on the
     * media servers; the patch only carries a URL and the encryption
     * key. A failed download is treated as transient and the operation
     * can be retried.
     */
    @WhatsAppWebModule(moduleName = "WAWebSyncdNetCallbacksApi")
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdRetryableError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class ExternalDownloadFailed extends WhatsAppWebAppStateSyncException {
        /**
         * Constructs a new external download failed exception.
         *
         * @param cause the underlying I/O or network exception
         */
        public ExternalDownloadFailed(Throwable cause) {
            super("Failed to download external mutations", cause);
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code false}
         */
        @Override
        public boolean isFatal() {
            return false;
        }
    }

    /**
     * Thrown when an external mutation blob downloads successfully
     * but its decoded payload is malformed.
     *
     * <p>The most common cause is a stale recovery snapshot that no
     * longer matches the current schema. The operation can be
     * retried.
     */
    @WhatsAppWebModule(moduleName = "WAWebNonMessageDataRequestHandler")
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdRetryableError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class ExternalDecodeFailed extends WhatsAppWebAppStateSyncException {
        /**
         * Constructs a new external decode failed exception.
         *
         * @param cause the underlying parsing or decompression exception
         */
        public ExternalDecodeFailed(Throwable cause) {
            super("Failed to decode external mutations", cause);
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code false}
         */
        @Override
        public boolean isFatal() {
            return false;
        }
    }

    /**
     * Thrown when computing an HMAC for a sync operation fails.
     *
     * <p>This points at a problem with the JCE provider, the key
     * material, or the cryptographic state of the process and is
     * treated as fatal.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class MacComputationFailed extends WhatsAppWebAppStateSyncException {
        /**
         * Constructs a new MAC computation failed exception.
         *
         * @param cause the underlying cryptographic exception
         */
        public MacComputationFailed(Throwable cause) {
            super("Failed to compute MAC", cause);
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when a SET mutation arrives without the timestamp every
     * SyncActionValue must carry.
     *
     * <p>The validation only applies to SET mutations; REMOVE
     * mutations have no SyncActionValue and are unaffected.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class MissingActionTimestamp extends WhatsAppWebAppStateSyncException {
        /**
         * Constructs a new missing action timestamp exception.
         */
        public MissingActionTimestamp() {
            super("Missing action timestamp in sync mutation");
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when a single patch contains two mutations of the same
     * kind (two SETs or two REMOVEs) targeting the same index.
     *
     * <p>Such a patch is malformed by construction and processing
     * cannot continue safely.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class DuplicateIndexInPatch extends WhatsAppWebAppStateSyncException {
        /**
         * The collection that contained the duplicate index.
         */
        private final SyncPatchType collectionName;

        /**
         * Constructs a new duplicate index in patch exception.
         *
         * @param collectionName the affected collection
         */
        public DuplicateIndexInPatch(SyncPatchType collectionName) {
            super("Same index for multiple mutations in patch for collection " + collectionName);
            this.collectionName = collectionName;
        }

        /**
         * Returns the collection that contained the duplicate index.
         *
         * @return the collection identifier
         */
        public SyncPatchType collectionName() {
            return collectionName;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when two patches in the same collection share the same
     * version number.
     *
     * <p>Patch versions are required to be unique; a duplicate makes
     * it impossible to determine ordering and the response is
     * rejected.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class DuplicatePatchVersion extends WhatsAppWebAppStateSyncException {
        /**
         * The collection that contained the duplicate version.
         */
        private final SyncPatchType collectionName;

        /**
         * The version that appeared on more than one patch.
         */
        private final long version;

        /**
         * Constructs a new duplicate patch version exception.
         *
         * @param collectionName the affected collection
         * @param version        the duplicated version number
         */
        public DuplicatePatchVersion(SyncPatchType collectionName, long version) {
            super("Duplicate patch version " + version + " in collection " + collectionName);
            this.collectionName = Objects.requireNonNull(collectionName);
            this.version = version;
        }

        /**
         * Returns the collection that contained the duplicate version.
         *
         * @return the collection identifier, never {@code null}
         */
        public SyncPatchType collectionName() {
            return collectionName;
        }

        /**
         * Returns the duplicated version number.
         *
         * @return the version number
         */
        public long version() {
            return version;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown for sync failures that do not match any of the more
     * specific categories.
     *
     * <p>Treated as fatal because the cause is unknown and the safe
     * recovery is to wipe the affected collection and resync.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class UnexpectedError extends WhatsAppWebAppStateSyncException {
        /**
         * Constructs a new unexpected error exception wrapping a cause.
         *
         * @param cause the underlying unexpected exception
         */
        public UnexpectedError(Throwable cause) {
            super("Unexpected sync error", cause);
        }

        /**
         * Constructs a new unexpected error exception with a message and cause.
         *
         * @param message extra detail about the unexpected error
         * @param cause   the underlying unexpected exception
         */
        public UnexpectedError(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when there is a gap in the patch sequence the server
     * delivered: the lowest version in the response is greater than
     * the local version plus one.
     *
     * <p>The chain integrity is broken and the only safe recovery is
     * to wipe the collection and resync from a fresh snapshot.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class MissingPatches extends WhatsAppWebAppStateSyncException {
        /**
         * The collection that has missing patches.
         */
        private final SyncPatchType collectionName;

        /**
         * The current local version of the collection.
         */
        private final long localVersion;

        /**
         * The lowest version among the patches that did arrive.
         */
        private final long minPatchVersion;

        /**
         * Constructs a new missing patches exception.
         *
         * @param collectionName  the affected collection
         * @param localVersion    the current local version
         * @param minPatchVersion the lowest version among received patches
         * @throws NullPointerException if {@code collectionName} is {@code null}
         */
        public MissingPatches(SyncPatchType collectionName, long localVersion, long minPatchVersion) {
            super("Missing patches for collection " + collectionName
                    + ": local version " + localVersion + ", min patch version " + minPatchVersion);
            this.collectionName = Objects.requireNonNull(collectionName);
            this.localVersion = localVersion;
            this.minPatchVersion = minPatchVersion;
        }

        /**
         * Returns the collection that has missing patches.
         *
         * @return the collection identifier, never {@code null}
         */
        public SyncPatchType collectionName() {
            return collectionName;
        }

        /**
         * Returns the current local version of the collection.
         *
         * @return the local version
         */
        public long localVersion() {
            return localVersion;
        }

        /**
         * Returns the lowest version among the patches that did arrive.
         *
         * @return the minimum patch version
         */
        public long minPatchVersion() {
            return minPatchVersion;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when the server marks a patch as terminal, signaling
     * that the collection data is unrecoverable.
     *
     * <p>The exit code carries the server's reason; the collection
     * is wiped and resynchronized from scratch.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdFatalError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class TerminalPatch extends WhatsAppWebAppStateSyncException {
        /**
         * The collection that received the terminal patch.
         */
        private final SyncPatchType collectionName;

        /**
         * The exit code declaring why the data is unrecoverable.
         */
        private final DisconnectReason exitCode;

        /**
         * Constructs a new terminal patch exception.
         *
         * @param collectionName the affected collection
         * @param exitCode       the exit code carried by the patch
         * @throws NullPointerException if {@code collectionName} or {@code exitCode} is {@code null}
         */
        public TerminalPatch(SyncPatchType collectionName, DisconnectReason exitCode) {
            super("Terminal patch for collection " + collectionName + " with exit code: " + exitCode);
            this.collectionName = Objects.requireNonNull(collectionName);
            this.exitCode = Objects.requireNonNull(exitCode);
        }

        /**
         * Returns the collection that received the terminal patch.
         *
         * @return the collection identifier, never {@code null}
         */
        public SyncPatchType collectionName() {
            return collectionName;
        }

        /**
         * Returns the exit code carried by the terminal patch.
         *
         * @return the exit code, never {@code null}
         */
        public DisconnectReason exitCode() {
            return exitCode;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code true}
         */
        @Override
        public boolean isFatal() {
            return true;
        }
    }

    /**
     * Thrown when the server returns a {@code 409 Conflict} response
     * to a patch publication, meaning a newer version exists on the
     * server.
     *
     * <p>The client refetches the collection and applies the patch on
     * top of the new version, so the failure is retryable.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdRetryableError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class Conflict extends WhatsAppWebAppStateSyncException {
        /**
         * Whether the server signaled that more patches are available
         * after the conflict.
         */
        private final boolean hasMorePatches;

        /**
         * Constructs a new conflict exception.
         *
         * @param hasMorePatches whether the server signaled more patches are available
         */
        public Conflict(boolean hasMorePatches) {
            super("Server returned 409 conflict");
            this.hasMorePatches = hasMorePatches;
        }

        /**
         * Returns whether the server signaled that more patches are
         * available after the conflict.
         *
         * @return {@code true} if more patches are pending
         */
        public boolean hasMorePatches() {
            return hasMorePatches;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code false}
         */
        @Override
        public boolean isFatal() {
            return false;
        }
    }

    /**
     * Thrown when the server returns a retryable error code other
     * than {@code 409 Conflict} or one of the fatal codes.
     *
     * <p>The server can attach a backoff hint (in milliseconds) that
     * is exposed via {@link #serverBackoffMs()} and should be honored
     * before the next attempt.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdError", exports = "SyncdRetryableError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final class RetryableServerError extends WhatsAppWebAppStateSyncException {
        /**
         * The error code the server returned.
         */
        private final String errorCode;

        /**
         * The server-suggested backoff in milliseconds, or {@code null}
         * when none was provided.
         */
        private final Long serverBackoffMs;

        /**
         * Constructs a new retryable server error exception.
         *
         * @param errorCode the server error code
         */
        public RetryableServerError(String errorCode) {
            this(errorCode, null);
        }

        /**
         * Constructs a new retryable server error exception with a server-suggested backoff.
         *
         * @param errorCode       the server error code
         * @param serverBackoffMs the server-suggested backoff in milliseconds, or {@code null}
         */
        public RetryableServerError(String errorCode, Long serverBackoffMs) {
            super("Server returned retryable error code: " + errorCode);
            this.errorCode = errorCode;
            this.serverBackoffMs = serverBackoffMs;
        }

        /**
         * Returns the error code the server returned.
         *
         * @return the error code
         */
        public String errorCode() {
            return errorCode;
        }

        /**
         * Returns the server-suggested backoff in milliseconds, when one was provided.
         *
         * @return the backoff in milliseconds, or {@code null} when none was provided
         */
        public Long serverBackoffMs() {
            return serverBackoffMs;
        }

        /**
         * Returns whether the failure invalidates the current session.
         *
         * @return {@code false}
         */
        @Override
        public boolean isFatal() {
            return false;
        }
    }
}
