package com.github.auties00.cobalt.sync.crypto;

import com.github.auties00.cobalt.model.sync.SyncActionDataBuilder;
import com.github.auties00.cobalt.model.sync.SyncActionDataSpec;
import com.github.auties00.cobalt.model.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * Represents the result of encrypting a sync mutation for upload to the server.
 *
 * <p>Contains the computed index MAC, the encrypted value (IV || ciphertext || value MAC),
 * the key ID, and the operation type.
 *
 * @param indexMac       the HMAC-SHA256 of the index bytes using the index key
 * @param encryptedValue the encrypted payload: IV (16 bytes) || AES-CBC ciphertext || value MAC (32 bytes)
 * @param keyId          the sync key ID used for encryption
 * @param operation      the sync operation type (SET or REMOVE)
 * @implNote WAWebSyncdEncryptMutations.syncdEncryptMutation,
 *           WAWebSyncdEncryptionManager.WASyncdEncryptionManager.encryptMutation
 */
public record EncryptedMutation(
        byte[] indexMac,
        byte[] encryptedValue,
        byte[] keyId,
        SyncdOperation operation
) {
    /**
     * Encrypts a pending mutation into the wire format expected by the server.
     *
     * <p>Performs the following steps matching WA Web's {@code syncdEncryptMutation},
     * delegating each cryptographic primitive to {@link MutationKeys}:
     * <ol>
     *   <li>Builds the {@code SyncActionData} protobuf with index, value, padding, and version</li>
     *   <li>Encodes it to protobuf bytes</li>
     *   <li>Encrypts with AES-256-CBC via {@link MutationKeys#generateCipherText(byte[])}</li>
     *   <li>Computes value MAC via {@link MutationKeys#generateMac(byte[], byte[])} (HMAC-SHA512 truncated to 32 bytes)</li>
     *   <li>Concatenates IV || ciphertext || value MAC</li>
     *   <li>Computes index MAC via {@link MutationKeys#generateIndexMac(byte[])} (HMAC-SHA256)</li>
     * </ol>
     *
     * @param patch the pending mutation to encrypt
     * @param keys  the derived mutation keys
     * @param keyId the sync key ID bytes
     * @return a new {@code EncryptedMutation} with the encrypted data
     * @throws GeneralSecurityException if any cryptographic operation fails
     * @implNote WAWebSyncdEncryptMutations.syncdEncryptMutation,
     *           WAWebSyncdMutationsCryptoUtils.generateCipherText,
     *           WAWebSyncdMutationsCryptoUtils.generateAssociatedData,
     *           WAWebSyncdMutationsCryptoUtils.generateMac,
     *           WAWebSyncdMutationsCryptoUtils.generatePadding,
     *           WAWebSyncdCrypto.generateIndexMac,
     *           WAWebSyncdCrypto.valueMacFromIndexAndValueCipherText,
     *           WAWebSyncdRequestEncode.encodeSyncActionData
     */
    public static EncryptedMutation of(
            SyncPendingMutation patch,
            MutationKeys keys,
            byte[] keyId
    ) throws GeneralSecurityException {
        // Create SyncActionData with padding — WAWebSyncdEncryptMutations.d (encodeSyncActionData)
        var mutation = patch.mutation();
        var indexBytes = mutation.index().getBytes(StandardCharsets.UTF_8); // WAArrayBufferUtils.stringToArrayBuffer(l)
        // Padding is always empty while MAX_OF_MIN_DATA_LENGTH = 0 in WA Web, so the value-length
        // argument is irrelevant (any non-negative value produces the same empty array). We pass
        // indexBytes.length and 0 here and let MutationKeys.generatePadding centralize the formula.
        var padding = MutationKeys.generatePadding(indexBytes.length, 0); // WAWebSyncdMutationsCryptoUtils.generatePadding
        var actionData = new SyncActionDataBuilder() // WAWebSyncdEncryptMutations.d: encodeSyncActionData({index, value, padding, version})
                .index(indexBytes) // WAWebSyncdEncryptMutations.d: index: e (stringToArrayBuffer result)
                .value(mutation.value()) // WAWebSyncdEncryptMutations.d: value: decodeProtobuf(SyncActionValueSpec, binarySyncAction)
                .padding(padding) // WAWebSyncdMutationsCryptoUtils.generatePadding — currently always empty (MAX_OF_MIN_DATA_LENGTH = 0)
                .version(mutation.actionVersion()) // WAWebSyncdEncryptMutations: t.version (top-level field on mutation)
                .build();

        // Encode to protobuf — WAWebSyncdRequestEncode.encodeSyncActionData
        var plaintext = SyncActionDataSpec.encode(actionData);

        // Encrypt with AES-256-CBC — WAWebSyncdMutationsCryptoUtils.generateCipherText → WACryptoAesCbc.aesCbcEncrypt
        // Returns [IV (16 bytes) || ciphertext]
        var ivAndCipherText = keys.generateCipherText(plaintext);

        // Build associated data: [opByte] || keyIdBytes — WAWebSyncdMutationsCryptoUtils.generateAssociatedData
        var associatedData = MutationKeys.generateAssociatedData(mutation.operation(), keyId);

        // Compute value MAC using HMAC-SHA-512 truncated to 32 bytes — WAWebSyncdMutationsCryptoUtils.generateMac
        var valueMac = keys.generateMac(associatedData, ivAndCipherText);

        // Concatenate IV || ciphertext || value MAC — WAWebSyncdCryptoUtils.combine([b, S])
        var encryptedValue = new byte[ivAndCipherText.length + valueMac.length];
        System.arraycopy(ivAndCipherText, 0, encryptedValue, 0, ivAndCipherText.length);
        System.arraycopy(valueMac, 0, encryptedValue, ivAndCipherText.length, valueMac.length);

        // Compute index MAC — WAWebSyncdCrypto.generateIndexMac
        var indexMacResult = keys.generateIndexMac(indexBytes);

        // Create EncryptedMutation — WAWebSyncdEncryptMutations: return {indexMac, indexAndValueCipherText}
        return new EncryptedMutation(indexMacResult, encryptedValue, keyId, mutation.operation());
    }

    /**
     * Extracts the value MAC from the encrypted value (last 32 bytes).
     *
     * <p>Delegates to {@link MutationKeys#valueMacFromIndexAndValueCipherText(byte[])}
     * to avoid duplicating the slicing logic.
     *
     * @return the 32-byte value MAC
     * @implNote WAWebSyncdCrypto.valueMacFromIndexAndValueCipherText
     */
    public byte[] valueMac() {
        return MutationKeys.valueMacFromIndexAndValueCipherText(encryptedValue);
    }
}
