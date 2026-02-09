package com.github.auties00.cobalt.message.addon;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.util.SecureBytes;

import javax.crypto.Cipher;
import javax.crypto.KDF;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Service for dual encryption of add-on messages (poll votes, CAG reactions, ...).
 * <p>
 * Add-on messages require two layers of encryption:
 * <ol>
 *   <li><b>Inner encryption (AES-GCM):</b> Encrypts the actual content using a use-case
 *       secret derived from the parent message's messageSecret via HKDF.</li>
 *   <li><b>Outer encryption (Signal):</b> The wrapped message is then encrypted with
 *       the standard Signal protocol for E2E delivery.</li>
 * </ol>
 * <p>
 * This dual encryption ensures that:
 * <ul>
 *   <li>Each add-on is bound to a specific parent message via stanzaId</li>
 *   <li>Identity binding to both original sender and add-on sender</li>
 *   <li>Context separation for different use-case types</li>
 *   <li>Forward secrecy with new secret per parent message</li>
 * </ul>
 */
public final class MessageAddonEncryption {
    private static final int AES_GCM_IV_SIZE = 12;
    private static final int AES_GCM_TAG_SIZE = 128; // bits
    private static final int HKDF_OUTPUT_SIZE = 32;
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final String HKDF_ALGORITHM = "HKDF-SHA256";

    private MessageAddonEncryption() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Encrypts add-on content with dual encryption.
     *
     * @param plaintext      the add-on content to encrypt
     * @param messageSecret  the parent message's 32-byte secret
     * @param stanzaId       the parent message's stanza ID
     * @param originalSender the JID of the original message sender (poll creator)
     * @param addonSender    the JID of the add-on sender (voter/reactor)
     * @param useCaseType    the type of add-on
     * @return the encrypted result containing ciphertext and IV
     */
    public static MessageEncryptedAddon encrypt(
            byte[] plaintext,
            byte[] messageSecret,
            String stanzaId,
            Jid originalSender,
            Jid addonSender,
            MessageAddonType useCaseType
    ) {
        Objects.requireNonNull(plaintext, "plaintext cannot be null");
        Objects.requireNonNull(messageSecret, "messageSecret cannot be null");
        Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
        Objects.requireNonNull(originalSender, "originalSender cannot be null");
        Objects.requireNonNull(addonSender, "addonSender cannot be null");
        Objects.requireNonNull(useCaseType, "useCaseType cannot be null");

        if (messageSecret.length != 32) {
            throw new IllegalArgumentException("messageSecret must be 32 bytes");
        }

        try {
            // Build use-case info
            var useCaseSecret = deriveUseCaseSecret(messageSecret, stanzaId, originalSender, addonSender, useCaseType);

            // Generate random IV
            var iv = SecureBytes.random(AES_GCM_IV_SIZE);

            // Encrypt with AES-256-GCM
            var cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            var keySpec = new SecretKeySpec(useCaseSecret, "AES");
            var gcmSpec = new GCMParameterSpec(AES_GCM_TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            // Add AAD if required by use-case type (e.g., poll votes)
            // AAD format: stanzaId + "\0" + addonSenderJid
            // This binds the ciphertext to the specific stanza and sender
            if (useCaseType.usesAad()) {
                var aad = buildAad(stanzaId, addonSender);
                cipher.updateAAD(aad);
            }

            var ciphertext = cipher.doFinal(plaintext);

            return new MessageEncryptedAddon(ciphertext, iv);

        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Failed to encrypt add-on", e);
        }
    }

    /**
     * Decrypts add-on content.
     *
     * @param encryptedAddon the encrypted add-on
     * @param messageSecret  the parent message's 32-byte secret
     * @param stanzaId       the parent message's stanza ID
     * @param originalSender the JID of the original message sender
     * @param addonSender    the JID of the add-on sender
     * @param useCaseType    the type of add-on
     * @return the decrypted plaintext
     */
    public static byte[] decrypt(
            MessageEncryptedAddon encryptedAddon,
            byte[] messageSecret,
            String stanzaId,
            Jid originalSender,
            Jid addonSender,
            MessageAddonType useCaseType
    ) {
        Objects.requireNonNull(encryptedAddon, "encryptedAddon cannot be null");
        Objects.requireNonNull(messageSecret, "messageSecret cannot be null");
        Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
        Objects.requireNonNull(originalSender, "originalSender cannot be null");
        Objects.requireNonNull(addonSender, "addonSender cannot be null");
        Objects.requireNonNull(useCaseType, "useCaseType cannot be null");

        if (messageSecret.length != 32) {
            throw new IllegalArgumentException("messageSecret must be 32 bytes");
        }

        try {
            // Build use-case info
            var useCaseSecret = deriveUseCaseSecret(messageSecret, stanzaId, originalSender, addonSender, useCaseType);

            // Decrypt with AES-256-GCM
            var cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            var keySpec = new SecretKeySpec(useCaseSecret, "AES");
            var gcmSpec = new GCMParameterSpec(AES_GCM_TAG_SIZE, encryptedAddon.iv());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            // Add AAD if required by use-case type (must match encryption)
            // AAD mismatch will cause authentication failure
            if (useCaseType.usesAad()) {
                var aad = buildAad(stanzaId, addonSender);
                cipher.updateAAD(aad);
            }

            return cipher.doFinal(encryptedAddon.ciphertext());

        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Failed to decrypt add-on", e);
        }
    }


    /**
     * Derives the use case secret
     *
     * @param messageSecret  the parent message's 32-byte secret
     * @param stanzaId       the parent message's stanza ID
     * @param originalSender the JID of the original message sender (poll creator)
     * @param addonSender    the JID of the add-on sender (voter/reactor)
     * @param useCaseType    the type of add-on
     * @return the derived use case secret
     */
    private static byte[] deriveUseCaseSecret(
            byte[] messageSecret,
            String stanzaId,
            Jid originalSender,
            Jid addonSender,
            MessageAddonType useCaseType
    ) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // Build use-case info
        var info = buildUseCaseInfo(stanzaId, originalSender, addonSender, useCaseType);

        // Derive use-case secret via HKDF
        var kdf = KDF.getInstance(HKDF_ALGORITHM);
        var secretKey = new SecretKeySpec(messageSecret, "AES");
        var params = HKDFParameterSpec.expandOnly(secretKey, info, MessageAddonEncryption.HKDF_OUTPUT_SIZE);

        return kdf.deriveData(params);
    }

    /**
     * Builds the use-case info for HKDF derivation.
     * <p>
     * Format: stanzaId || originalSender || addonSender || useCaseType
     */
    private static byte[] buildUseCaseInfo(
            String stanzaId,
            Jid originalSender,
            Jid addonSender,
            MessageAddonType useCaseType
    ) {
        var stanzaIdBytes = stanzaId.getBytes(StandardCharsets.UTF_8);
        var originalSenderBytes = originalSender.toString().getBytes(StandardCharsets.UTF_8);
        var addonSenderBytes = addonSender.toString().getBytes(StandardCharsets.UTF_8);
        var useCaseBytes = useCaseType.value().getBytes(StandardCharsets.UTF_8);

        var info = new byte[stanzaIdBytes.length + originalSenderBytes.length + addonSenderBytes.length + useCaseBytes.length];
        var offset = 0;

        System.arraycopy(stanzaIdBytes, 0, info, offset, stanzaIdBytes.length);
        offset += stanzaIdBytes.length;

        System.arraycopy(originalSenderBytes, 0, info, offset, originalSenderBytes.length);
        offset += originalSenderBytes.length;

        System.arraycopy(addonSenderBytes, 0, info, offset, addonSenderBytes.length);
        offset += addonSenderBytes.length;

        System.arraycopy(useCaseBytes, 0, info, offset, useCaseBytes.length);
        // No need to change offset

        return info;
    }

    /**
     * Builds the AAD (Additional Authenticated Data) for poll vote encryption.
     * <p>
     * Format: stanzaId + "\0" + addonSenderJid
     * <p>
     * This prevents vote substitution attacks where an attacker might try
     * to copy an encrypted vote from one user to another.
     *
     * @param stanzaId    the parent message's stanza ID
     * @param addonSender the JID of the voter
     * @return the AAD bytes
     */
    private static byte[] buildAad(String stanzaId, Jid addonSender) {
        var stanzaIdBytes = stanzaId.getBytes(StandardCharsets.UTF_8);
        var senderBytes = addonSender.toString().getBytes(StandardCharsets.UTF_8);

        var aad = new byte[stanzaIdBytes.length + 1 + senderBytes.length];
        var offset = 0;

        System.arraycopy(stanzaIdBytes, 0, aad, offset, stanzaIdBytes.length);
        offset += stanzaIdBytes.length;

        aad[offset] = 0x00;
        offset++;

        System.arraycopy(senderBytes, 0, aad, offset, senderBytes.length);
        // No need to change offset

        return aad;
    }
}
