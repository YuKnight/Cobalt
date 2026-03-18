package com.github.auties00.cobalt.sync.crypto;

import javax.crypto.KDF;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Objects;

/**
 * Holds the five derived encryption keys used for app state sync mutations.
 *
 * <p>Keys are derived from a sync key via HKDF-SHA256 with info string
 * {@code "WhatsApp Mutation Keys"}, producing 160 bytes which are split into
 * five 32-byte keys: indexKey, valueEncryptionKey, valueMacKey, snapshotMacKey,
 * and patchMacKey.
 *
 * <p>Implements {@link AutoCloseable} to allow secure destruction of key material.
 *
 * @implNote WAWebSyncdCryptoHelper.generateEncryptionKeysUnmemoized,
 *           WAWebSyncdCryptoConst (HKDF_INFO, DERIVED_KEY_LENGTH, key offset constants),
 *           WAWebSyncdCrypto.generateEncryptionKeys (memoized wrapper)
 */
public final class MutationKeys implements AutoCloseable {
    private static final String HKDF_INFO = "WhatsApp Mutation Keys"; // WAWebSyncdCryptoConst.HKDF_INFO
    private static final int DERIVED_KEY_LENGTH = 160; // WAWebSyncdCryptoConst.DERIVED_KEY_LENGTH

    private final SecretKeySpec indexKey;
    private final SecretKeySpec valueEncryptionKey;
    private final SecretKeySpec valueMacKey;
    private final SecretKeySpec snapshotMacKey;
    private final SecretKeySpec patchMacKey;


    /**
     * Constructs a new set of mutation keys from the five derived key specs.
     *
     * @param indexKey            the key for computing index MACs (HMAC-SHA256)
     * @param valueEncryptionKey  the key for AES-CBC encryption/decryption
     * @param valueMacKey         the key for computing value MACs (HMAC-SHA512)
     * @param snapshotMacKey      the key for computing snapshot MACs (HMAC-SHA256)
     * @param patchMacKey         the key for computing patch MACs (HMAC-SHA256)
     * @implNote WAWebSyncdCryptoHelper.generateEncryptionKeysUnmemoized
     */
    private MutationKeys(SecretKeySpec indexKey, SecretKeySpec valueEncryptionKey, SecretKeySpec valueMacKey, SecretKeySpec snapshotMacKey, SecretKeySpec patchMacKey) {
        this.indexKey = Objects.requireNonNull(indexKey, "Index key cannot be null");
        this.valueEncryptionKey = Objects.requireNonNull(valueEncryptionKey, "Value encryption key cannot be null");
        this.valueMacKey = Objects.requireNonNull(valueMacKey, "Value MAC key cannot be null");
        this.snapshotMacKey = Objects.requireNonNull(snapshotMacKey, "Snapshot MAC key cannot be null") ;
        this.patchMacKey = Objects.requireNonNull(patchMacKey, "Patch MAC key cannot be null");
    }

    /**
     * Derives the five mutation keys from the given sync key data using HKDF-SHA256.
     *
     * <p>The derivation performs extract-then-expand with no salt, info string
     * {@code "WhatsApp Mutation Keys"}, and output length 160 bytes. The resulting
     * bytes are split at offsets 0, 32, 64, 96, 128 matching the WA Web constants
     * {@code INDEX_KEY_END}, {@code VALUE_ENCRYPTION_KEY_END}, {@code VALUE_MAC_KEY_END},
     * {@code SNAPSHOT_MAC_KEY_END}, and {@code PATCH_MAC_KEY_END}.
     *
     * @param syncKey the raw sync key data (must be 32 bytes)
     * @return a new {@code MutationKeys} instance with the five derived keys
     * @throws NullPointerException     if {@code syncKey} is {@code null}
     * @throws IllegalArgumentException if {@code syncKey} is not 32 bytes
     * @implNote WAWebSyncdCryptoHelper.generateEncryptionKeysUnmemoized,
     *           WACryptoHkdf.extractAndExpand,
     *           WAWebSyncdCryptoConst (INDEX_KEY_END=32, VALUE_ENCRYPTION_KEY_END=64,
     *           VALUE_MAC_KEY_END=96, SNAPSHOT_MAC_KEY_END=128, PATCH_MAC_KEY_END=160)
     */
    public static MutationKeys ofSyncKey(byte[] syncKey) {
        if (syncKey == null) {
            throw new NullPointerException("Sync key cannot be null");
        }

        if (syncKey.length != 32) {
            throw new IllegalArgumentException("Sync key must be 32 bytes, got " + syncKey.length);
        }

        try {
            var kdf = KDF.getInstance("HKDF-SHA256"); // ADAPTED: WACryptoHkdf.extractAndExpand
            var params = HKDFParameterSpec.ofExtract() // WACryptoHkdf.extractAndExpand — extract with null salt
                    .addIKM(syncKey) // WASyncdKeyTypes.fromSyncKeyData (identity)
                    .thenExpand(HKDF_INFO.getBytes(StandardCharsets.UTF_8), DERIVED_KEY_LENGTH); // WAWebSyncdCryptoConst.HKDF_INFO, DERIVED_KEY_LENGTH
            var derivedBytes = kdf.deriveData(params);

            return new MutationKeys(
                    new SecretKeySpec(derivedBytes, 0, 32, "HmacSHA256"), // WAWebSyncdCryptoConst: [0, INDEX_KEY_END)
                    new SecretKeySpec(derivedBytes, 32, 32, "AES"), // WAWebSyncdCryptoConst: [INDEX_KEY_END, VALUE_ENCRYPTION_KEY_END)
                    new SecretKeySpec(derivedBytes, 64, 32, "HmacSHA512"), // WAWebSyncdCryptoConst: [VALUE_ENCRYPTION_KEY_END, VALUE_MAC_KEY_END)
                    new SecretKeySpec(derivedBytes, 96, 32, "HmacSHA256"), // WAWebSyncdCryptoConst: [VALUE_MAC_KEY_END, SNAPSHOT_MAC_KEY_END)
                    new SecretKeySpec(derivedBytes, 128, 32, "HmacSHA256") // WAWebSyncdCryptoConst: [SNAPSHOT_MAC_KEY_END, PATCH_MAC_KEY_END)
            );
        } catch (GeneralSecurityException e) {
            throw new InternalError("Failed to derive keys", e);
        }
    }

    /**
     * Destroys all key material held by this instance.
     *
     * <p>Silently ignores {@link DestroyFailedException} since not all JCE providers
     * support key destruction.
     *
     * @implNote ADAPTED: Java-specific resource cleanup, no WA Web equivalent
     */
    @Override
    public void close() {
        try {
            indexKey.destroy();
        }catch (DestroyFailedException _) {

        }
        try {
            valueEncryptionKey.destroy();
        }catch (DestroyFailedException _) {

        }
        try {
            valueMacKey.destroy();
        }catch (DestroyFailedException _) {

        }
        try {
            snapshotMacKey.destroy();
        }catch (DestroyFailedException _) {

        }
        try {
            patchMacKey.destroy();
        }catch (DestroyFailedException _) {

        }
    }

    /**
     * Returns a string representation that does not leak key material.
     *
     * @return the fixed string {@code "AppStateSyncKeys"}
     */
    @Override
    public String toString() {
        return "AppStateSyncKeys";
    }

    /**
     * Returns the index key used for HMAC-SHA256 index MAC computation.
     *
     * @return the index key spec
     * @implNote WAWebSyncdCryptoHelper.generateEncryptionKeysUnmemoized — indexKey slice
     */
    public SecretKeySpec indexKey() {
        return indexKey;
    }

    /**
     * Returns the value encryption key used for AES-CBC encryption/decryption.
     *
     * @return the value encryption key spec
     * @implNote WAWebSyncdCryptoHelper.generateEncryptionKeysUnmemoized — valueEncryptionKey slice
     */
    public SecretKeySpec valueEncryptionKey() {
        return valueEncryptionKey;
    }

    /**
     * Returns the value MAC key used for HMAC-SHA512 value MAC computation.
     *
     * @return the value MAC key spec
     * @implNote WAWebSyncdCryptoHelper.generateEncryptionKeysUnmemoized — valueMacKey slice
     */
    public SecretKeySpec valueMacKey() {
        return valueMacKey;
    }

    /**
     * Returns the snapshot MAC key used for HMAC-SHA256 snapshot MAC computation.
     *
     * @return the snapshot MAC key spec
     * @implNote WAWebSyncdCryptoHelper.generateEncryptionKeysUnmemoized — snapshotMacKey slice
     */
    public SecretKeySpec snapshotMacKey() {
        return snapshotMacKey;
    }

    /**
     * Returns the patch MAC key used for HMAC-SHA256 patch MAC computation.
     *
     * @return the patch MAC key spec
     * @implNote WAWebSyncdCryptoHelper.generateEncryptionKeysUnmemoized — patchMacKey slice
     */
    public SecretKeySpec patchMacKey() {
        return patchMacKey;
    }
}
