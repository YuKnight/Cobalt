package com.github.auties00.cobalt.sync.crypto;

import com.github.auties00.cobalt.exception.WhatsAppWebAppStateSyncException;
import com.github.auties00.cobalt.model.message.system.appstate.AppStateSyncKeyData;
import com.github.auties00.cobalt.model.signal.KeyId;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.data.SyncdPatch;
import com.github.auties00.cobalt.model.sync.data.SyncdSnapshot;
import com.github.auties00.cobalt.store.WhatsAppStore;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.SequencedCollection;

/**
 * Verifies the integrity of sync mutations using HMAC-SHA256 based MACs.
 *
 * <p>Provides per-patch verification of snapshot MACs and patch MACs,
 * matching the WhatsApp Web {@code WAWebSyncdAntiTampering} module behavior.
 * Each patch is verified individually with its own value MACs and the
 * wire-provided snapshot MAC, rather than batch verification across all patches.
 *
 * <p>The actual MAC computation is delegated to static methods
 * ({@link #computeSnapshotMac} and {@link #computePatchMac}) which implement
 * the HMAC-SHA256 formulas from {@code WAWebSyncdEncryptionManager}.
 *
 * @implNote WAWebSyncdAntiTampering (computeLtHashAndValidateSnapshot, computeLtHashAndValidatePatch,
 *           computeOutgoingSnapshotAndPatchMacs, validateSnapshotMac, validatePatchMac),
 *           WAWebSyncdEncryptionManager (generateSnapshotMac, generatePatchMac)
 */
public final class MutationIntegrityVerifier {
    /**
     * The WhatsApp store for key lookups and collection state queries.
     *
     * @implNote ADAPTED: WAWebSyncdAntiTampering — module-level imports replaced with
     *           constructor DI per Cobalt architecture
     */
    private final WhatsAppStore store;

    /**
     * Constructs a new integrity verifier.
     *
     * @implNote ADAPTED: WAWebSyncdAntiTampering — module-level imports of
     *           {@code WAWebSyncdKeyCache}, {@code WAWebSyncdCrypto}, {@code WAWebEncryptionManagerSelector},
     *           and {@code WAWebGetCollectionVersion} are replaced by constructor DI of the
     *           {@link WhatsAppStore} which provides equivalent functionality
     * @param store the WhatsApp store for key lookups and collection state queries
     */
    public MutationIntegrityVerifier(WhatsAppStore store) {
        this.store = store;
    }

    /**
     * Verifies the snapshot MAC and returns the computed snapshot MAC for use
     * in subsequent patch MAC verification.
     *
     * <p>Snapshot MAC is computed as:
     * {@code HMAC-SHA256(snapshotMacKey, ltHash || to64BitNetworkOrder(version) || UTF8(collectionName))}
     *
     * <p>In WA Web, this corresponds to {@code computeLtHashAndValidateSnapshot} which
     * also computes the LT-Hash internally before calling the internal
     * {@code validateSnapshotMac} (function G with {@code isSnapshot=true}).
     * In Cobalt, the LT-Hash computation is separated into the caller
     * ({@code WebAppStateService.computeNewLTHash}) and this method only performs
     * the MAC validation. When the MAC mismatches in snapshot mode, a fatal
     * {@link WhatsAppWebAppStateSyncException.SnapshotMacMismatch} is thrown.
     *
     * @implNote WAWebSyncdAntiTampering.computeLtHashAndValidateSnapshot — calls
     *           {@code validateSnapshotMac} (function G) with {@code isSnapshot=true};
     *           on mismatch throws {@code SyncdFatalError("unable to validate snapshot mac")}
     * @param collectionName the collection type
     * @param version the collection version
     * @param snapshot the decoded snapshot
     * @param expectedHash the computed LT-Hash
     * @return the computed snapshot MAC, or {@code null} if MAC verification is disabled
     */
    public byte[] verifySnapshotMac(SyncPatchType collectionName, long version, SyncdSnapshot snapshot, byte[] expectedHash) {
        if (!store.checkPatchMacs()) {
            return null;
        }

        // Per WA Web: missing snapshot MAC is a fatal validation error, not a skip
        var mac = snapshot.mac();
        if(mac.isEmpty()) {
            throw new WhatsAppWebAppStateSyncException.SnapshotMacMismatch(collectionName, version);
        }

        var keyId = snapshot.keyId() // WAWebSyncdAntiTampering.computeLtHashAndValidateSnapshot: n.keyId
                .flatMap(KeyId::id);
        if(keyId.isEmpty()) {
            throw new WhatsAppWebAppStateSyncException.UnexpectedError(
                    "Snapshot missing key id for " + collectionName + " at version " + version,
                    null
            );
        }

        var keyData = store.findWebAppStateKeyById(keyId.get()) // WAWebSyncdAntiTampering: getKeyData(a.id) -> SyncdMissingKeyError
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.MissingKey(keyId.get()))
                .keyData()
                .flatMap(AppStateSyncKeyData::keyData)
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "Snapshot sync key had no key data for " + collectionName + " at version " + version,
                        null
                ));

        try (var keys = MutationKeys.ofSyncKey(keyData)) { // WAWebSyncdAntiTampering: generateEncryptionKeys(c)
            var expectedMac = computeSnapshotMac(keys.snapshotMacKey(), expectedHash, version, collectionName); // WAWebSyncdEncryptionManager.generateSnapshotMac
            if (!MessageDigest.isEqual(mac.get(), expectedMac)) { // WAWebSyncdAntiTampering.validateSnapshotMac: arrayBuffersEqual(T, t)
                throw new WhatsAppWebAppStateSyncException.SnapshotMacMismatch(collectionName, version); // WAWebSyncdAntiTampering: SyncdFatalError("unable to validate snapshot mac")
            }
            return expectedMac;
        }
    }

    /**
     * Verifies the integrity of a single patch by checking both the patch MAC
     * and the snapshot MAC.
     *
     * <p>Per WhatsApp Web, patch MAC mismatch is fatal (throws), but snapshot MAC mismatch
     * is non-fatal -- the collection is marked as mac-mismatch and processing continues.
     *
     * <p>Per WA Web function G ({@code validateSnapshotMac}): when the collection is
     * already in mac-mismatch state ({@code isCollectionInMacMismatchFatal}), the
     * snapshot MAC validation is skipped entirely for patches. This avoids repeated
     * mismatch logging for a collection that is already known to be inconsistent.
     *
     * @implNote WAWebSyncdAntiTampering.computeLtHashAndValidatePatch — calls
     *           {@code validatePatchMac} (function j, fatal on mismatch) then
     *           {@code validateSnapshotMac} (function G with {@code isSnapshot=false},
     *           non-fatal on mismatch for patches, marks collection as mac-mismatch).
     *           Function G checks {@code isCollectionInMacMismatchFatal} and skips
     *           snapshot MAC validation if the collection is already in mismatch state.
     * @param collectionName the collection type for MAC computation
     * @param patch          the wire patch with its MAC fields
     * @param computedLtHash the locally computed LT-Hash after applying this patch's mutations
     * @param patchValueMacs the value MACs from only this patch's mutations, in order
     * @return {@code true} if the snapshot MAC matched (or was not checked),
     *         {@code false} if the snapshot MAC mismatched (collection should be marked mac-mismatch)
     * @throws WhatsAppWebAppStateSyncException.PatchMacMismatch if the wire patch MAC does not match
     */
    public boolean verifyPatchIntegrity(SyncPatchType collectionName, SyncdPatch patch, byte[] computedLtHash, SequencedCollection<byte[]> patchValueMacs) {
        if (!store.checkPatchMacs()) { // ADAPTED: Cobalt-specific toggle, no WA Web equivalent
            return true;
        }

        var keyId = patch.keyId() // WAWebSyncdAntiTampering.computeLtHashAndValidatePatch: l = t.keyId, f = l.id
                .flatMap(KeyId::id);
        if (keyId.isEmpty()) {
            throw new WhatsAppWebAppStateSyncException.UnexpectedError(
                    "Patch missing key id for " + collectionName,
                    null
            );
        }

        var keyData = store.findWebAppStateKeyById(keyId.get()) // WAWebSyncdAntiTampering.computeLtHashAndValidatePatch: getKeyData(f) -> SyncdMissingKeyError
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.MissingKey(keyId.get()))
                .keyData()
                .flatMap(AppStateSyncKeyData::keyData)
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "Patch sync key had no key data for " + collectionName,
                        null
                ));

        long patchVersion = patch.version() // WAWebSyncdAntiTampering.computeLtHashAndValidatePatch: _ = t.version
                .map(version -> version.version().orElse(0L))
                .orElse(0L);

        try (var keys = MutationKeys.ofSyncKey(keyData)) { // WAWebSyncdAntiTampering.computeLtHashAndValidatePatch: generateEncryptionKeys(g)
            var wireSnapshotMac = patch.snapshotMac().orElse(null); // WAWebSyncdAntiTampering.computeLtHashAndValidatePatch: p = t.snapshotMac

            // Step 1: Verify wire patchMac using wire snapshotMac as input (patch MAC first per WA Web)
            // WAWebSyncdAntiTampering.validatePatchMac (function j/K)
            var wirePatchMac = patch.patchMac().orElse(null); // WAWebSyncdAntiTampering.computeLtHashAndValidatePatch: m = t.patchMac
            if (wirePatchMac != null) {
                var expectedPatchMac = computePatchMac(keys.patchMacKey(), wireSnapshotMac, patchValueMacs, patchVersion, collectionName); // WAWebSyncdAntiTampering.validatePatchMac: generatePatchMac(n, r, a, l, e)
                if (!MessageDigest.isEqual(wirePatchMac, expectedPatchMac)) { // WAWebSyncdAntiTampering.validatePatchMac: arrayBuffersEqual(c, t)
                    throw new WhatsAppWebAppStateSyncException.PatchMacMismatch(collectionName, patchVersion); // WAWebSyncdAntiTampering.validatePatchMac: SyncdFatalError("unable to validate patch mac")
                }
            }

            // Step 2: Verify wire snapshotMac against locally computed LT-Hash
            // WAWebSyncdAntiTampering.validateSnapshotMac (function G) with isSnapshot=false
            // Per WA Web function G: if the collection is already in mac-mismatch state,
            // skip the snapshot MAC validation entirely
            var alreadyInMacMismatch = store.findWebAppState(collectionName).macMismatch(); // WAWebSyncdAntiTampering.validateSnapshotMac: getIsCollectionInMacMismatchFatalInTransaction(e)
            if (alreadyInMacMismatch) { // WAWebSyncdAntiTampering.validateSnapshotMac: if (E && k) return
                return true;
            }

            if (wireSnapshotMac != null) {
                var expectedSnapshotMac = computeSnapshotMac(keys.snapshotMacKey(), computedLtHash, patchVersion, collectionName); // WAWebSyncdAntiTampering.validateSnapshotMac: generateSnapshotMac(n, r, u, e)
                if (!MessageDigest.isEqual(wireSnapshotMac, expectedSnapshotMac)) { // WAWebSyncdAntiTampering.validateSnapshotMac: arrayBuffersEqual(T, t)
                    return false; // ADAPTED: WAWebSyncdAntiTampering.validateSnapshotMac — non-fatal path: updateIsCollectionInMacMismatchFatalInTransaction
                }
            }
        }
        return true;
    }

    /**
     * Computes the snapshot MAC.
     *
     * <p>Formula: {@code HMAC-SHA256(snapshotMacKey, ltHash || version8 || collectionUtf8)}
     * where {@code version8} is an 8-byte big-endian encoding produced by
     * {@code WAWebSyncdCryptoUtils.to64BitNetworkOrder} (upper 4 bytes zeroed,
     * lower 4 bytes are the version as a big-endian uint32).
     *
     * @implNote WAWebSyncdEncryptionManager.generateSnapshotMac — {@code hmacSha256(snapshotMacKey,
     *           combine([ltHash, to64BitNetworkOrder(version), toUtf8(collection).buffer]))}.
     *           Called from {@code WAWebSyncdAntiTampering.validateSnapshotMac} (function G)
     *           and {@code WAWebSyncdAntiTampering.computeOutgoingSnapshotAndPatchMacs} (function Q/X)
     * @param snapshotMacKey the HMAC key for snapshot MAC
     * @param ltHash the computed LT-Hash
     * @param version the snapshot or patch version
     * @param type the collection type
     * @return the computed snapshot MAC
     */
    public static byte[] computeSnapshotMac(SecretKeySpec snapshotMacKey, byte[] ltHash, long version, SyncPatchType type) {
        try {
            var mac = Mac.getInstance("HmacSHA256"); // WAWebSyncdEncryptionManager: hmacSha256(snapshotMacKey, ...)
            mac.init(snapshotMacKey); // WAWebSyncdEncryptionManager: a.snapshotMacKey

            mac.update(ltHash); // WAWebSyncdEncryptionManager: combine([t, ...]) — ltHash first

            // @implNote WAWebSyncdCryptoUtils.to64BitNetworkOrder — inlined for efficiency (avoids temporary 8-byte array).
            // Equivalent to: mac.update(SyncKeyUtils.to64BitNetworkOrder(version));
            // WAWebSyncdCryptoUtils.to64BitNetworkOrder: 8 bytes, upper 4 zeroed, lower 4 big-endian uint32
            mac.update((byte) (version >> 56));
            mac.update((byte) (version >> 48));
            mac.update((byte) (version >> 40));
            mac.update((byte) (version >> 32));
            mac.update((byte) (version >> 24));
            mac.update((byte) (version >> 16));
            mac.update((byte) (version >> 8));
            mac.update((byte) version);

            mac.update(type.toBytes()); // WAWebSyncdEncryptionManager: toUtf8(collectionName).buffer

            return mac.doFinal();
        } catch (GeneralSecurityException exception) {
            throw new WhatsAppWebAppStateSyncException.MacComputationFailed(exception);
        }
    }

    /**
     * Computes the patch MAC.
     *
     * <p>Formula: {@code HMAC-SHA256(patchMacKey, snapshotMac || valueMac1 || ... || valueMacN || version8 || collectionUtf8)}
     * where {@code version8} is an 8-byte big-endian encoding produced by
     * {@code WAWebSyncdCryptoUtils.to64BitNetworkOrder}.
     *
     * @implNote WAWebSyncdEncryptionManager.generatePatchMac — {@code hmacSha256(patchMacKey,
     *           combine([snapshotMac].concat(valueMacs, [to64BitNetworkOrder(version), toUtf8(collection).buffer])))}.
     *           Called from {@code WAWebSyncdAntiTampering.validatePatchMac} (function j/K)
     *           and {@code WAWebSyncdAntiTampering.computeOutgoingSnapshotAndPatchMacs} (function Q/X)
     * @param patchMacKey the HMAC key for patch MAC
     * @param snapshotMac the snapshot MAC (may be {@code null})
     * @param valueMacs the individual value MACs from mutations
     * @param version the patch version
     * @param type the collection type
     * @return the computed patch MAC
     */
    public static byte[] computePatchMac(SecretKeySpec patchMacKey, byte[] snapshotMac, SequencedCollection<byte[]> valueMacs, long version, SyncPatchType type) {
        try {
            var mac = Mac.getInstance("HmacSHA256"); // WAWebSyncdEncryptionManager: hmacSha256(patchMacKey, ...)
            mac.init(patchMacKey); // WAWebSyncdEncryptionManager: i.patchMacKey

            // WAWebSyncdEncryptionManager: combine([t].concat(n, [u, s])) — snapshotMac first
            if (snapshotMac != null) {
                mac.update(snapshotMac);
            }

            // WAWebSyncdEncryptionManager: .concat(n, ...) — individual valueMacs
            for (var valueMac : valueMacs) {
                mac.update(valueMac);
            }

            // @implNote WAWebSyncdCryptoUtils.to64BitNetworkOrder — inlined for efficiency (avoids temporary 8-byte array).
            // Equivalent to: mac.update(SyncKeyUtils.to64BitNetworkOrder(version));
            // WAWebSyncdCryptoUtils.to64BitNetworkOrder: 8 bytes, upper 4 zeroed, lower 4 big-endian uint32
            mac.update((byte) (version >> 56));
            mac.update((byte) (version >> 48));
            mac.update((byte) (version >> 40));
            mac.update((byte) (version >> 32));
            mac.update((byte) (version >> 24));
            mac.update((byte) (version >> 16));
            mac.update((byte) (version >> 8));
            mac.update((byte) version);

            mac.update(type.toBytes()); // WAWebSyncdEncryptionManager: toUtf8(collection).buffer

            return mac.doFinal();
        } catch (GeneralSecurityException exception) {
            throw new WhatsAppWebAppStateSyncException.MacComputationFailed(exception);
        }
    }
}
