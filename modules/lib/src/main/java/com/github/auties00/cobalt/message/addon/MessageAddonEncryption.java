package com.github.auties00.cobalt.message.addon;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.util.DataUtils;

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
 * Shared AES-GCM primitive for every WhatsApp addon that is dual-encrypted:
 * poll votes, CAG reactions, encrypted comments, event responses, event
 * edits, poll edits, poll add-options, and message edits.
 *
 * <p>Addon messages live inside the outer Signal envelope but carry their own
 * inner ciphertext so the server cannot correlate the addon content with the
 * parent message. The inner layer uses a 32-byte secret derived via
 * HKDF-SHA256 from the parent message's {@code messageSecret}, with the
 * parent stanza id, original sender JID, addon sender JID, and use-case label
 * mixed into the info parameter. The result is an AES-256-GCM ciphertext with
 * a random 12-byte IV and a 128-bit auth tag. Poll votes and event responses
 * additionally authenticate the stanza id and addon sender as AAD so the
 * server cannot rebind a vote from one user to another.
 *
 * @implNote The JS module also owns the dispatch from {@code WAWebMsgType.MsgKind}
 * values to the matching protobuf spec and use case. Cobalt leaves that
 * dispatch to the callers (see {@link EncMessageFactory}) and exposes only
 * the crypto primitive here.
 */
@WhatsAppWebModule(moduleName = "WAWebAddonEncryption")
@WhatsAppWebModule(moduleName = "WAUseCaseSecret")
public final class MessageAddonEncryption {
    /**
     * Size of the AES-GCM initialization vector in bytes.
     */
    private static final int AES_GCM_IV_SIZE = 12;

    /**
     * Size of the AES-GCM authentication tag in bits.
     */
    private static final int AES_GCM_TAG_SIZE = 128;

    /**
     * Expected output size of the HKDF-derived use-case secret in bytes.
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "createUseCaseSecret",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final int HKDF_OUTPUT_SIZE = 32;

    /**
     * JCA algorithm identifier for AES-GCM with no padding.
     */
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";

    /**
     * JCA algorithm identifier for HKDF-SHA256.
     */
    private static final String HKDF_ALGORITHM = "HKDF-SHA256";

    /**
     * Private constructor preventing instantiation.
     *
     * @throws UnsupportedOperationException always
     */
    private MessageAddonEncryption() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Encrypts an addon plaintext into an inner AES-GCM ciphertext bound to
     * the parent message's secret.
     *
     * <p>Derives a fresh 32-byte use-case key from the parent secret via
     * HKDF, samples a random 12-byte IV, and encrypts the plaintext under
     * AES-256-GCM. When the {@link MessageAddonType} requires it (poll votes
     * and event responses) the stanza id and addon sender JID are
     * authenticated as AAD.
     *
     * @implNote WA Web throws {@code DualEncryptionValidationError} with
     * {@code ENCRYPTION_ERROR}. Cobalt rethrows as {@link RuntimeException}
     * because the crypto backend is JCA rather than the Web Crypto API.
     * @param plaintext      the addon payload to encrypt
     * @param messageSecret  the parent message's 32-byte {@code messageSecret}
     * @param stanzaId       the parent message's stanza id
     * @param originalSender the JID of the parent message's author
     * @param addonSender    the JID of the addon author
     * @param useCaseType    the addon use-case driving both the HKDF info
     *                       label and the AAD toggle
     * @return the ciphertext and IV, packaged for direct use in the outbound
     *         stanza
     * @throws IllegalArgumentException if {@code messageSecret} is not exactly
     *                                  32 bytes
     * @throws NullPointerException     if any argument is {@code null}
     * @throws RuntimeException         if the underlying cipher throws a
     *                                  {@link GeneralSecurityException}
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryption", exports = "encryptAddOn",
            adaptation = WhatsAppAdaptation.ADAPTED)
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
            var useCaseSecret = deriveUseCaseSecret(messageSecret, stanzaId, originalSender, addonSender, useCaseType);
            var iv = DataUtils.randomByteArray(AES_GCM_IV_SIZE);

            var cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            var keySpec = new SecretKeySpec(useCaseSecret, "AES");
            var gcmSpec = new GCMParameterSpec(AES_GCM_TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

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
     * Decrypts an addon ciphertext produced by {@link #encrypt}.
     *
     * <p>Derives the same 32-byte use-case key from the parent message's
     * secret and the addon metadata, then strips the AES-GCM layer and
     * validates the auth tag. When the {@link MessageAddonType} advertises
     * AAD usage the caller-supplied stanza id and addon sender are replayed
     * into the cipher before finalisation. A mismatch surfaces as a
     * {@link GeneralSecurityException} wrapped in a {@link RuntimeException}.
     *
     * @param encryptedAddon the ciphertext and IV produced by the sender
     * @param messageSecret  the parent message's 32-byte {@code messageSecret}
     * @param stanzaId       the parent message's stanza id
     * @param originalSender the JID of the parent message's author
     * @param addonSender    the JID of the addon author
     * @param useCaseType    the addon use-case driving both the HKDF info
     *                       label and the AAD toggle
     * @return the recovered plaintext
     * @throws IllegalArgumentException if {@code messageSecret} is not exactly
     *                                  32 bytes
     * @throws NullPointerException     if any argument is {@code null}
     * @throws RuntimeException         if the underlying cipher throws a
     *                                  {@link GeneralSecurityException}
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryption", exports = "decryptAddOn",
            adaptation = WhatsAppAdaptation.ADAPTED)
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
            var useCaseSecret = deriveUseCaseSecret(messageSecret, stanzaId, originalSender, addonSender, useCaseType);

            var cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            var keySpec = new SecretKeySpec(useCaseSecret, "AES");
            var gcmSpec = new GCMParameterSpec(AES_GCM_TAG_SIZE, encryptedAddon.iv());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

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
     * Derives the 32-byte addon key used for AES-GCM by running HKDF-SHA256
     * extract-and-expand with the parent message's secret as input keying
     * material.
     *
     * <p>The info parameter is the concatenation of the stanza id, original
     * sender JID, addon sender JID, and use-case label encoded as UTF-8.
     * Extract uses a {@code null} salt, which the JDK expands to a
     * zero-filled block of SHA-256 hash length.
     *
     * @param messageSecret  the parent message's 32-byte {@code messageSecret}
     * @param stanzaId       the parent message's stanza id
     * @param originalSender the JID of the parent message's author
     * @param addonSender    the JID of the addon author
     * @param useCaseType    the addon use-case driving the info label
     * @return the 32-byte derived addon key
     * @throws NoSuchAlgorithmException           if HKDF-SHA256 is not
     *                                            available in the configured
     *                                            JCA provider
     * @throws InvalidAlgorithmParameterException if the HKDF parameter spec
     *                                            is rejected by the provider
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "createUseCaseSecret",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static byte[] deriveUseCaseSecret(
            byte[] messageSecret,
            String stanzaId,
            Jid originalSender,
            Jid addonSender,
            MessageAddonType useCaseType
    ) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        var info = buildUseCaseInfo(stanzaId, originalSender, addonSender, useCaseType);

        var kdf = KDF.getInstance(HKDF_ALGORITHM);
        var params = HKDFParameterSpec.ofExtract()
                .addIKM(messageSecret)
                .thenExpand(info, HKDF_OUTPUT_SIZE);

        return kdf.deriveData(params);
    }

    /**
     * Builds the HKDF info parameter used to derive the addon key.
     *
     * <p>The info is the raw byte concatenation of the UTF-8 encodings of
     * {@code stanzaId || originalSender || addonSender || useCaseType.value()}.
     * The helper preallocates a single byte array and copies each component
     * in sequence without inserting any separator.
     *
     * @implNote Built in WA Web via
     * {@code WABinary.Binary.build(stanzaId, parentMsgOriginalSender, modificationSender, modificationType).readBuffer()}.
     * {@code Binary.build} writes each string argument as UTF-8 bytes
     * sequentially with no separator.
     * @param stanzaId       the parent message's stanza id
     * @param originalSender the JID of the parent message's author
     * @param addonSender    the JID of the addon author
     * @param useCaseType    the addon use-case driving the info label
     * @return the info bytes passed to HKDF expand
     */
    @WhatsAppWebExport(moduleName = "WAUseCaseSecret", exports = "createUseCaseSecret",
            adaptation = WhatsAppAdaptation.DIRECT)
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

        return info;
    }

    /**
     * Builds the AAD used for {@link MessageAddonType#POLL_VOTE} and
     * {@link MessageAddonType#EVENT_RESPONSE}.
     *
     * <p>The AAD format is {@code stanzaId || 0x00 || addonSenderJid} encoded
     * as UTF-8, where the single zero byte acts as a separator. This binding
     * prevents the server from lifting an encrypted vote or response emitted
     * by one user and replaying it as if it came from a different user.
     *
     * @param stanzaId    the parent message's stanza id
     * @param addonSender the JID of the addon author
     * @return the AAD bytes fed into {@link Cipher#updateAAD(byte[])}
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryption", exports = {"encryptAddOn", "decryptAddOn"},
            adaptation = WhatsAppAdaptation.DIRECT)
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

        return aad;
    }
}
